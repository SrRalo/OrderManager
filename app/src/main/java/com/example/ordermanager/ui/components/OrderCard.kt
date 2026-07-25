package com.example.ordermanager.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ordermanager.R
import com.example.ordermanager.data.local.entity.PedidoEntity
import com.example.ordermanager.ui.menu.MenuImages
import com.example.ordermanager.ui.theme.*
import org.json.JSONArray

data class OrderProduct(
    val nombre: String,
    val cantidad: Int,
    val precio: Double
)

@Composable
fun OrderCard(
    order: PedidoEntity,
    isPendingConfirm: Boolean,
    confirmTimer: Int,
    onMarcarEnviado: (String) -> Unit,
    onCancelSend: (String) -> Unit
) {
    val products = remember(order.productos) {
        try {
            val arr = JSONArray(order.productos)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                OrderProduct(
                    nombre = obj.optString("nombre", ""),
                    cantidad = obj.optInt("cantidad", 1),
                    precio = obj.optDouble("precio", 0.0)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    val timeAgo = remember(order.timestamp) {
        val diff = System.currentTimeMillis() - order.timestamp
        val minutes = (diff / 60000).toInt()
        when {
            minutes < 1 -> "Hace unos segundos"
            minutes == 1 -> "Hace 1 min"
            else -> "Hace $minutes min"
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
        HeaderSection(
            cliente = if (order.origen == "app_mesero") "mesero" else order.cliente,
            direccion = order.direccion,
            timeAgo = timeAgo
            // Removed total parameter as user doesn't want prices shown
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        if (products.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                products.forEach { product ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(Radius.sm))
                                .background(AvatarBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = MenuImages.getImageResId(product.nombre)),
                                contentDescription = product.nombre,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        
                        Text(
                            text = "${product.cantidad}x ${product.nombre}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        if (isPendingConfirm) {
            Button(
                onClick = { onCancelSend(order.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = Shapes.button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = if (confirmTimer > 0) "Cancelar ($confirmTimer)" else "Cancelar",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Button(
                onClick = { onMarcarEnviado(order.id) },
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
    }
}

@Composable
private fun HeaderSection(
    cliente: String,
    direccion: String,
    timeAgo: String
    // Removed total parameter as user doesn't want prices shown
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
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
            if (direccion.isNotBlank() && direccion != "N/A") {
                Text(
                    text = direccion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .background(BadgePendienteBg, shape = Shapes.badge)
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs)
            ) {
                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = BadgePendienteText
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Removed total price display as user doesn't want prices shown
        }
    }
}
