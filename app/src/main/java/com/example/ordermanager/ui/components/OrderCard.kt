package com.example.ordermanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ordermanager.data.local.entity.PedidoEntity
import com.example.ordermanager.ui.theme.*
import org.json.JSONArray

private data class ProductInfo(
    val nombre: String,
    val cantidad: Int,
    val precio: Double = 0.0
)

@Composable
fun OrderCard(
    order: PedidoEntity,
    onMarcarEnviado: (String) -> Unit
) {
    var showModal by mutableStateOf(false)

    val productos = remember(order.productos) {
        try {
            val arr = JSONArray(order.productos)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                ProductInfo(
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
            .shadow(
                elevation = 4.dp,
                shape = Shapes.cardLarge,
                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = Shapes.cardLarge
            )
            .padding(Spacing.xl)
    ) {
        HeaderSection(cliente = order.cliente, direccion = order.direccion)

        Spacer(modifier = Modifier.height(Spacing.md))

        OrderDetailsSection(productos = productos)

        Spacer(modifier = Modifier.height(Spacing.md))

        DeliveryTimeSection(tiempoEstimado = order.tiempoEstimado)

        Spacer(modifier = Modifier.height(Spacing.md))

        Button(
            onClick = { showModal = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = Shapes.button,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Marcar como Enviado",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
        }
    }

    ConfirmModal(
        visible = showModal,
        titulo = "Confirmar Envío",
        onConfirm = {
            showModal = false
            onMarcarEnviado(order.id)
        },
        onCancel = { showModal = false }
    )
}

@Composable
private fun HeaderSection(
    cliente: String,
    direccion: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(Radius.md))
                .background(AvatarBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = AvatarIconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cliente,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = direccion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .background(BadgePendienteBg, shape = Shapes.badge)
                .padding(horizontal = Spacing.md, vertical = Spacing.xs)
        ) {
            Text(
                text = "Pendiente",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = BadgePendienteText
            )
        }
    }
}

@Composable
private fun OrderDetailsSection(
    productos: List<ProductInfo>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(Radius.md))
            .padding(Spacing.md)
    ) {
        productos.forEach { producto ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(Radius.sm))
                            .background(AvatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🍕",
                            fontSize = 18.sp
                        )
                    }
                    Text(
                        text = "${producto.cantidad}x ${producto.nombre}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "$${String.format("%.2f", producto.precio)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DeliveryTimeSection(
    tiempoEstimado: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Icon(
            imageVector = Icons.Outlined.Schedule,
            contentDescription = null,
            tint = DeliveryTimeTint,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = buildString {
                append(tiempoEstimado)
                append(" min ")
                append("entrega estimada")
            },
            style = MaterialTheme.typography.bodySmall,
            color = DeliveryTimeTint
        )
    }
}
