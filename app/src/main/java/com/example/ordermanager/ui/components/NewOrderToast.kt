package com.example.ordermanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ordermanager.data.local.entity.PedidoEntity
import com.example.ordermanager.ui.theme.*
import kotlinx.coroutines.delay
import org.json.JSONArray

@Composable
fun NewOrderToast(
    order: PedidoEntity?,
    onDismiss: () -> Unit
) {
    var visible by remember(order) { mutableStateOf(order != null) }

    LaunchedEffect(order) {
        if (order != null) {
            visible = true
            delay(4000)
            visible = false
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible && order != null,
        enter = slideInVertically { -it },
        exit = slideOutVertically { -it }
    ) {
        order?.let { pedido ->
            val productos = try {
                JSONArray(pedido.productos).length()
            } catch (_: Exception) { 0 }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                    .shadow(4.dp, shape = Shapes.card)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(Spacing.lg),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = Shapes.avatar
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍽️", style = MaterialTheme.typography.titleLarge)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¡Nuevo Pedido Recibido!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${pedido.cliente} — $productos producto(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary, shape = Shapes.badge)
                            .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                    ) {
                        Text(
                            text = "${pedido.tiempoEstimado}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }
    }
}
