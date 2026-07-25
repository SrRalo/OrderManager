package com.example.ordermanager.backend.data.repository

import com.example.ordermanager.backend.data.model.PedidoItemInsert
import com.example.ordermanager.backend.data.model.PedidoItemRow
import com.example.ordermanager.backend.data.model.PedidoRow
import com.example.ordermanager.backend.supabase.SupabaseClientProvider
import com.example.ordermanager.data.local.entity.PedidoEntity
import com.example.ordermanager.ui.menu.MenuItem
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.util.UUID
import kotlin.runCatching

class SupabasePedidoRepository {

    private val client = SupabaseClientProvider.client
    private val json = Json { encodeDefaults = true }

    // Shared flow to communicate new orders locally without Supabase
    private val _localOrders = MutableSharedFlow<PedidoEntity>(extraBufferCapacity = 10)
    val localOrders: SharedFlow<PedidoEntity> = _localOrders.asSharedFlow()
    
    // Memory cache for local orders to survive role switching in the same session
    private val pendingLocalOrdersCache = mutableListOf<PedidoEntity>()
    private val completedLocalOrdersCache = mutableListOf<PedidoEntity>()

    companion object {
        private var INSTANCE: SupabasePedidoRepository? = null
        fun getInstance(): SupabasePedidoRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SupabasePedidoRepository().also { INSTANCE = it }
            }
        }
    }

    suspend fun createPedido(
        meseroId: String,
        items: Map<MenuItem, Int>,
        notaGeneral: String
    ) {
        // Validate input
        if (meseroId.isBlank()) {
            throw IllegalArgumentException("meseroId cannot be blank")
        }
        if (items.isEmpty()) {
            throw IllegalArgumentException("items cannot be empty")
        }

        val total = entriesSum(items)

        // Bypass Supabase and send directly to local flow
        val productosJson = json.encodeToString(
            buildJsonArray {
                items.forEach { (menuItem, cantidad) ->
                    add(
                        buildJsonObject {
                            put("nombre", menuItem.nombre)
                            put("cantidad", cantidad)
                            put("precio", menuItem.precio * cantidad)
                        }
                    )
                }
            }
        )

        val localPedido = PedidoEntity(
            id = "LOCAL-" + UUID.randomUUID().toString(),
            cliente = "Pedido Local",
            direccion = "Mesa Local",
            productos = productosJson,
            total = total,
            tiempoEstimado = 0,
            notas = notaGeneral.ifBlank { null },
            timestamp = System.currentTimeMillis(),
            origen = "app_mesero"
        )

        // Cache it and emit it
        synchronized(pendingLocalOrdersCache) {
            pendingLocalOrdersCache.add(0, localPedido)
        }
        _localOrders.emit(localPedido)
        
        android.util.Log.d("PedidoRepository", "Pedido local creado y emitido: ${localPedido.id}")
    }

    private fun entriesSum(items: Map<MenuItem, Int>): Double {
        return items.entries.sumOf { it.key.precio * it.value }
    }

    suspend fun fetchPedidosPendientes(): List<PedidoEntity> {
        val remotePedidos = try {
            client.from("pedidos")
                .select(columns = Columns.ALL) {
                    filter { eq("estado", "pendiente") }
                    order("timestamp_creacion", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<PedidoRow>()
                .map { toPedidoEntity(it) }
        } catch (ex: Exception) {
            android.util.Log.e("PedidoRepository", "Error fetching pending from Supabase (RLS?): ${ex.message}")
            emptyList()
        }

        return synchronized(pendingLocalOrdersCache) {
            pendingLocalOrdersCache + remotePedidos
        }
    }

    suspend fun fetchPedidosCompletados(): List<PedidoEntity> {
        val remoteCompletados = try {
            client.from("pedidos")
                .select(columns = Columns.ALL) {
                    filter { eq("estado", "completado") }
                    order("timestamp_creacion", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<PedidoRow>()
                .map { toPedidoEntity(it) }
        } catch (e: Exception) {
            emptyList()
        }

        return synchronized(completedLocalOrdersCache) {
            completedLocalOrdersCache.toList() + remoteCompletados
        }
    }

    suspend fun marcarEnPreparacion(pedidoId: String) {
        if (pedidoId.startsWith("LOCAL-")) return // No-op for local
        
        runCatching {
            client.from("pedidos").update(
                {
                    set("estado", "en_preparacion")
                }
            ) {
                filter { eq("id", pedidoId) }
            }
        }
    }

    suspend fun marcarCompletado(pedidoId: String) {
        if (pedidoId.startsWith("LOCAL-")) {
            synchronized(pendingLocalOrdersCache) {
                val order = pendingLocalOrdersCache.find { it.id == pedidoId }
                if (order != null) {
                    pendingLocalOrdersCache.remove(order)
                    synchronized(completedLocalOrdersCache) {
                        completedLocalOrdersCache.add(0, order)
                    }
                }
            }
            return
        }

        runCatching {
            client.from("pedidos").update(
                {
                    set("estado", "completado")
                }
            ) {
                filter { eq("id", pedidoId) }
            }
        }
    }

    private suspend fun toPedidoEntity(row: PedidoRow): PedidoEntity {
        // Retry mechanism for fetching pedido items to handle potential timing issues
        var detalles: List<PedidoItemRow> = emptyList()
        var lastException: Exception? = null
        for (attempt in 1..3) {
            try {
                detalles = client.from("pedido_items")
                    .select(columns = Columns.ALL) {
                        filter { eq("pedido_id", row.id) }
                    }
                    .decodeList<PedidoItemRow>()
                break // Success, exit retry loop
            } catch (e: Exception) {
                lastException = e
                if (attempt < 3) {
                    // Wait before retrying, with increasing delay
                    delay(((attempt - 1) * 300).toLong()) // 0ms, 300ms, 600ms
                }
            }
        }
        // If all retries failed, detalles remains emptyList() - we'll still create the entity
        // but with empty products, which is better than crashing

        val productosJson = json.encodeToString(
            buildJsonArray {
                detalles.forEach { item ->
                    add(
                        buildJsonObject {
                            put("nombre", item.nombreItemSnapshot)
                            put("cantidad", item.cantidad)
                            // Note: We store subtotal as "precio" for backward compatibility
                            // with existing data and UI expectations
                            put("precio", item.subtotal)
                        }
                    )
                }
            }
        )

        return PedidoEntity(
            id = row.id,
            cliente = row.cliente,
            direccion = row.mesaODireccion,
            productos = productosJson,
            total = row.total,
            tiempoEstimado = 0,
            notas = row.notas,
            timestamp = runCatching { Instant.parse(row.timestampCreacion).toEpochMilli() }
                .getOrDefault(System.currentTimeMillis()),
            origen = row.origen
        )
    }
}
