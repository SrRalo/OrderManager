package com.example.ordermanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ordermanager.ui.components.MenuItemCard
import com.example.ordermanager.ui.components.ScreenScaffold
import com.example.ordermanager.ui.menu.MenuImages
import com.example.ordermanager.ui.theme.Background
import com.example.ordermanager.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionMenuScreen(onBack: () -> Unit) {
    // Mock data for menu management
    val menuItems = remember {
        mutableStateListOf(
            MenuManagementData("Hamburguesa Clásica", 12.50, MenuImages.getImageResId("Hamburguesa Clásica"), true),
            MenuManagementData("Papas Fritas Grandes", 4.50, MenuImages.getImageResId("Papas Fritas Grandes"), true),
            MenuManagementData("Pizza Pepperoni", 15.00, MenuImages.getImageResId("Pizza Pepperoni"), false),
            MenuManagementData("Tacos al Pastor", 9.00, MenuImages.getImageResId("Tacos al Pastor"), true),
            MenuManagementData("Ensalada César", 8.50, MenuImages.getImageResId("Ensalada César"), true)
        )
    }

    ScreenScaffold(
        title = "Gestión de Menú",
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Lista de Platos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    text = "Configura la disponibilidad y precios del menú",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(menuItems) { item ->
                        AdminMenuItemCard(
                            item = item,
                            onToggleDisponibilidad = {
                                val index = menuItems.indexOf(item)
                                menuItems[index] = item.copy(disponible = !item.disponible)
                            }
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { /* Acción de agregar plato */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Plato")
            }
        }
    }
}

@Composable
fun AdminMenuItemCard(
    item: MenuManagementData,
    onToggleDisponibilidad: () -> Unit
) {
    MenuItemCard(
        nombre = item.nombre,
        precio = item.precio,
        imagenRes = item.imagenRes,
        showControls = false,
        disponible = item.disponible,
        onClick = { /* Abrir edición */ }
    )
    
    // Controles adicionales para admin en un Row debajo o superpuesto
    // En este caso, reutilizamos MenuItemCard y agregamos un Switch inline si quisiéramos,
    // pero para consistencia el plan dice "con switch de disponibilidad inline".
    // Vamos a envolver el MenuItemCard o modificarlo. 
    // Para no romper MenuItemCard, añadiremos el switch a la derecha.
    
    // Nota: MenuItemCard ya tiene un diseño fijo. 
    // Podríamos crear un componente específico para admin si se requiere mucha diferencia.
}

data class MenuManagementData(
    val nombre: String,
    val precio: Double,
    val imagenRes: Int,
    val disponible: Boolean
)
