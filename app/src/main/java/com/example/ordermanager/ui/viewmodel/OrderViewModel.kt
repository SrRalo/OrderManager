package com.example.ordermanager.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ordermanager.data.local.AppDatabase
import com.example.ordermanager.data.local.entity.PedidoEntity
import com.example.ordermanager.data.repository.PedidoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderUiState(
    val orders: List<PedidoEntity> = emptyList(),
    val newOrderAlert: PedidoEntity? = null
)

class OrderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PedidoRepository

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    private var orderCounter = 2

    init {
        val db = AppDatabase.getInstance(application)
        repository = PedidoRepository(db.pedidoDao())
        cargarPedidosIniciales()
        iniciarSimulacion()
    }

    private fun cargarPedidosIniciales() {
        val iniciales = listOf(
            PedidoEntity(
                id = "ORD-0001",
                cliente = "Elena Rodríguez",
                direccion = "452 Maple Ave, Suite 12",
                productos = """[{"nombre":"Pizza Pepperoni","cantidad":1,"precio":18.50},{"nombre":"Coca-Cola","cantidad":1,"precio":2.50}]""",
                total = 21.0,
                tiempoEstimado = 12,
                notas = "Sin cebolla, por favor",
                timestamp = System.currentTimeMillis() - 120000
            ),
            PedidoEntity(
                id = "ORD-0002",
                cliente = "Carlos López",
                direccion = "Calle 5 de Mayo 456, Col. Juárez",
                productos = """[{"nombre":"Hamburguesa Clásica","cantidad":2,"precio":9.99},{"nombre":"Papas Fritas","cantidad":1,"precio":4.50}]""",
                total = 24.48,
                tiempoEstimado = 18,
                timestamp = System.currentTimeMillis() - 60000
            )
        )
        _uiState.update { it.copy(orders = iniciales) }
    }

    private fun iniciarSimulacion() {
        viewModelScope.launch {
            while (true) {
                val delay = (4000..12000).random().toLong()
                delay(delay)
                val nuevo = generarPedidoMock()
                _uiState.update {
                    it.copy(
                        orders = it.orders + nuevo,
                        newOrderAlert = nuevo
                    )
                }
                Log.w("OrderManager", "Nuevo pedido: ${nuevo.id} - ${nuevo.cliente}")
            }
        }
    }

    fun marcarEnviado(pedidoId: String) {
        _uiState.update { it.copy(orders = it.orders.filter { o -> o.id != pedidoId }) }
    }

    fun dismissAlert() {
        _uiState.update { it.copy(newOrderAlert = null) }
    }

    private fun generarPedidoMock(): PedidoEntity {
        val clientes = listOf(
            "María García", "Carlos López", "Ana Martínez", "José Hernández",
            "Sofía Ramírez", "Luis Torres", "Valentina Ortiz", "Diego Castillo"
        )
        val direcciones = listOf(
            "Av. Reforma 123, Col. Centro", "Calle 5 de Mayo 456, Col. Juárez",
            "Blvd. Independencia 789, Col. Del Valle", "Av. Universidad 321, Col. Roma",
            "Calle Hidalgo 654, Col. Condesa", "Av. Insurgentes 987, Col. Polanco"
        )
        val productosBase = listOf(
            "Hamburguesa Clásica" to 2, "Papas Fritas Grandes" to 1,
            "Pizza Pepperoni" to 1, "Tacos al Pastor" to 4,
            "Ensalada César" to 1, "Refresco de Cola" to 2,
            "Agua Natural" to 1, "Flan Napolitano" to 2,
            "Quesadillas" to 3, "Burrito Supreme" to 1
        )

        orderCounter++
        val numProductos = (1..3).random()
        val seleccionados = productosBase.shuffled().take(numProductos)
        val precios = seleccionados.map { (_, cant) ->
            cant * ((40..120).random())
        }
        val productosJson = buildString {
            append("[")
            seleccionados.forEachIndexed { i, (nombre, cantidad) ->
                if (i > 0) append(",")
                val precio = precios[i]
                append("""{"nombre":"$nombre","cantidad":$cantidad,"precio":$precio}""")
            }
            append("]")
        }
        val total = precios.sum().toDouble()
        val tiempo = (10..30).random()
        val notas = if ((0..9).random() > 5) "Sin cebolla, por favor" else null

        return PedidoEntity(
            id = "ORD-${String.format("%04d", orderCounter)}",
            cliente = clientes.random(),
            direccion = direcciones.random(),
            productos = productosJson,
            total = total,
            tiempoEstimado = tiempo,
            notas = notas,
            timestamp = System.currentTimeMillis()
        )
    }
}
