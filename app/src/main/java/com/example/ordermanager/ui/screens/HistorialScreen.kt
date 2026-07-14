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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ordermanager.data.local.entity.PedidoEntity
import com.example.ordermanager.ui.components.EmptyState
import com.example.ordermanager.ui.theme.*
import com.example.ordermanager.ui.viewmodel.OrderViewModel
import org.json.JSONArray

private data class ProductDetail(
    val nombre: String,
    val cantidad: Int,
    val precio: Double
)

@Composable
fun HistorialScreen(
    orderViewModel: OrderViewModel
) {
    val uiState by orderViewModel.uiState.collectAsStateWithLifecycle()
    val completedOrders = uiState.completedOrders
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

        if (completedOrders.isEmpty()) {
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
                    items = completedOrders,
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
