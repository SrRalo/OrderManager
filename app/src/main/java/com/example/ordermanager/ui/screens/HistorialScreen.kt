package com.example.ordermanager.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ordermanager.data.local.entity.PedidoEntity
import com.example.ordermanager.ui.components.EmptyState
import com.example.ordermanager.ui.theme.*
import org.json.JSONArray

private data class ProductDetail(
    val nombre: String,
    val cantidad: Int,
    val precio: Double
)

private val mockHistory = listOf(
    PedidoEntity(
        id = "ORD-0001",
        cliente = "Elena Rodríguez",
        direccion = "452 Maple Ave, Suite 12",
        productos = """[{"nombre":"Pizza Pepperoni","cantidad":1,"precio":18.50},{"nombre":"Coca-Cola","cantidad":1,"precio":2.50}]""",
        total = 21.0,
        tiempoEstimado = 12,
        timestamp = System.currentTimeMillis() - 86400000
    ),
    PedidoEntity(
        id = "ORD-0002",
        cliente = "Carlos López",
        direccion = "Calle 5 de Mayo 456, Col. Juárez",
        productos = """[{"nombre":"Hamburguesa Clásica","cantidad":2,"precio":9.99},{"nombre":"Papas Fritas","cantidad":1,"precio":4.50}]""",
        total = 24.48,
        tiempoEstimado = 18,
        timestamp = System.currentTimeMillis() - 172800000
    ),
    PedidoEntity(
        id = "ORD-0003",
        cliente = "Ana Martínez",
        direccion = "Blvd. Independencia 789, Col. Del Valle",
        productos = """[{"nombre":"Tacos al Pastor","cantidad":4,"precio":3.50},{"nombre":"Refresco de Cola","cantidad":2,"precio":2.00},{"nombre":"Flan Napolitano","cantidad":1,"precio":5.50}]""",
        total = 23.50,
        tiempoEstimado = 20,
        timestamp = System.currentTimeMillis() - 259200000
    ),
    PedidoEntity(
        id = "ORD-0004",
        cliente = "José Hernández",
        direccion = "Av. Universidad 321, Col. Roma",
        productos = """[{"nombre":"Quesadillas","cantidad":3,"precio":4.00},{"nombre":"Agua Natural","cantidad":1,"precio":1.50}]""",
        total = 13.50,
        tiempoEstimado = 15,
        timestamp = System.currentTimeMillis() - 345600000
    ),
    PedidoEntity(
        id = "ORD-0005",
        cliente = "Sofía Ramírez",
        direccion = "Calle Hidalgo 654, Col. Condesa",
        productos = """[{"nombre":"Burrito Supreme","cantidad":1,"precio":12.00},{"nombre":"Papas Fritas Grandes","cantidad":1,"precio":5.00},{"nombre":"Refresco de Cola","cantidad":1,"precio":2.00}]""",
        total = 19.00,
        tiempoEstimado = 25,
        timestamp = System.currentTimeMillis() - 432000000
    )
)

@Composable
fun HistorialScreen() {
    val expandedIds = remember { mutableStateMapOf<String, Boolean>() }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Historial de pedidos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(
                horizontal = Spacing.lg,
                vertical = Spacing.sm
            )
        )

        if (mockHistory.isEmpty()) {
            EmptyState(
                title = "Sin historial",
                subtitle = "Los pedidos completados aparecerán aquí.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = Spacing.sm)
            ) {
                items(
                    items = mockHistory,
                    key = { it.id }
                ) { order ->
                    val isExpanded = expandedIds[order.id] == true
                    HistoryCard(
                        order = order,
                        isExpanded = isExpanded,
                        onToggle = {
                            expandedIds[order.id] = !isExpanded
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    order: PedidoEntity,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val productos = remember(order.productos) {
        try {
            val arr = JSONArray(order.productos)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                ProductDetail(
                    nombre = obj.getString("nombre"),
                    cantidad = obj.getInt("cantidad"),
                    precio = obj.optDouble("precio", 0.0)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
            .padding(bottom = Spacing.md)
            .shadow(2.dp, shape = Shapes.card)
            .background(MaterialTheme.colorScheme.surface, shape = Shapes.card)
            .clickable(enabled = false, onClick = {})
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = order.id,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = order.cliente,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Total: $${String.format("%.2f", order.total)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isExpanded) "✕" else "···",
                    fontSize = if (isExpanded) 14.sp else 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = androidx.compose.animation.core.tween(300)),
            exit = shrinkVertically(animationSpec = androidx.compose.animation.core.tween(300))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Spacing.lg, end = Spacing.lg, bottom = Spacing.lg)
            ) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(
                    text = "Dirección de entrega",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = order.direccion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(
                    text = "Detalle del pedido",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Spacing.sm))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(Radius.md)
                        )
                        .padding(Spacing.md)
                ) {
                    productos.forEachIndexed { index, producto ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${producto.cantidad}x ${producto.nombre}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "$${String.format("%.2f", producto.precio)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (index < productos.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Subtotal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format("%.2f", productos.sumOf { it.precio })}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
