package com.example.ordermanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ordermanager.ui.components.MenuItemCard
import com.example.ordermanager.ui.components.PrimaryButton
import com.example.ordermanager.ui.components.ScreenScaffold
import com.example.ordermanager.ui.menu.MenuImages
import com.example.ordermanager.ui.theme.Background
import com.example.ordermanager.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuSeleccionScreen(onBack: () -> Unit) {
    var cartCount by remember { mutableStateOf(0) }
    var totalPrice by remember { mutableDoubleStateOf(0.0) }

    // Mock data for now
    val menuItems = listOf(
        MenuItemData("Hamburguesa Clásica", 12.50, MenuImages.getImageResId("Hamburguesa Clásica")),
        MenuItemData("Papas Fritas Grandes", 4.50, MenuImages.getImageResId("Papas Fritas Grandes")),
        MenuItemData("Pizza Pepperoni", 15.00, MenuImages.getImageResId("Pizza Pepperoni")),
        MenuItemData("Tacos al Pastor", 9.00, MenuImages.getImageResId("Tacos al Pastor")),
        MenuItemData("Ensalada César", 8.50, MenuImages.getImageResId("Ensalada César"))
    )

    ScreenScaffold(
        title = "Nuevo Pedido",
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
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
                                Spacer(modifier = Modifier.width(8.dp))
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
                            onClick = { /* Acción de enviar pedido */ }
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
            // Categorías (Chips horizontales simplificados)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf("Todos", "Hamburguesas", "Pizzas", "Bebidas", "Postres")
                categories.forEach { category ->
                    FilterChip(
                        selected = category == "Todos",
                        onClick = { },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Text(
                text = "Selecciona los platos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(menuItems) { item ->
                    var cantidad by remember { mutableIntStateOf(0) }
                    MenuItemCard(
                        nombre = item.nombre,
                        precio = item.precio,
                        imagenRes = item.imagenRes,
                        cantidad = cantidad,
                        onAdd = {
                            cantidad++
                            cartCount++
                            totalPrice += item.precio
                        },
                        onRemove = {
                            if (cantidad > 0) {
                                cantidad--
                                cartCount--
                                totalPrice -= item.precio
                            }
                        }
                    )
                }
            }
        }
    }
}

data class MenuItemData(
    val nombre: String,
    val precio: Double,
    val imagenRes: Int
)
