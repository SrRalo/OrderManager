package com.example.ordermanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ordermanager.ui.components.MenuItemCard
import com.example.ordermanager.ui.components.PrimaryButton
import com.example.ordermanager.ui.components.ScreenScaffold
import com.example.ordermanager.ui.theme.Background
import com.example.ordermanager.ui.theme.Primary
import com.example.ordermanager.ui.theme.Spacing
import com.example.ordermanager.ui.viewmodel.MenuViewModel

@Composable
fun MenuSeleccionScreen(
    onBack: () -> Unit,
    meseroId: String?,
    menuViewModel: MenuViewModel
) {
    val uiState = menuViewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(meseroId) {
        menuViewModel.setMeseroId(meseroId)
        menuViewModel.loadMenu()
        menuViewModel.limpiarCarrito()
    }

    val cartCount = uiState.cantidades.values.sum()
    val totalPrice = menuViewModel.getTotalCarrito()

    ScreenScaffold(
        title = null,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
        },
        bottomBar = {
            if (cartCount > 0) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Primary)
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text(
                                    text = "$cartCount ítems en el carrito",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "Total: $${String.format("%.2f", totalPrice)}",
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        PrimaryButton(
                            text = "Confirmar Pedido",
                            onClick = { menuViewModel.enviarPedido() },
                            isLoading = uiState.isLoading
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
        ) {
            Text(
                text = "Menú",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(top = Spacing.xl, bottom = Spacing.lg, start = Spacing.lg, end = Spacing.lg)
            )

            if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(Spacing.md))
            }

            if (uiState.pedidoEnviado) {
                Text(
                    text = "Pedido enviado a cocina",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(Spacing.md))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp)
            ) {
                items(uiState.itemsDisponibles) { item ->
                    MenuItemCard(
                        nombre = item.nombre,
                        precio = item.precio,
                        imagenRes = menuViewModel.getImagenRes(item.imagenRef),
                        cantidad = menuViewModel.getCantidad(item.id),
                        onAdd = { menuViewModel.addToCart(item.id) },
                        onRemove = { menuViewModel.removeFromCart(item.id) }
                    )
                }
            }
        }
    }
}
