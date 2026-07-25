package com.example.ordermanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ordermanager.ui.components.AppTextField
import com.example.ordermanager.ui.components.ConfirmModal
import com.example.ordermanager.ui.components.MenuItemCard
import com.example.ordermanager.ui.components.PrimaryButton
import com.example.ordermanager.ui.components.ScreenScaffold
import com.example.ordermanager.ui.menu.MenuImages
import com.example.ordermanager.ui.menu.MenuItem
import com.example.ordermanager.ui.theme.Background
import com.example.ordermanager.ui.theme.Primary
import com.example.ordermanager.ui.theme.Spacing
import com.example.ordermanager.ui.viewmodel.MenuViewModel

private val categorias = listOf(
    1 to "Hamburguesas",
    2 to "Pizzas",
    3 to "Bebidas",
    4 to "Postres"
)

private val imagenOptions = listOf(
    "Hamburguesa Clásica",
    "Papas Fritas Grandes",
    "Pizza Pepperoni",
    "Tacos al Pastor",
    "Ensalada César",
    "Refresco de Cola",
    "Agua Natural",
    "Flan Napolitano",
    "Quesadillas",
    "Burrito Supreme"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionMenuScreen(
    onBack: () -> Unit,
    menuViewModel: MenuViewModel
) {
    val uiState = menuViewModel.uiState.collectAsStateWithLifecycle().value
    var showForm by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<MenuItem?>(null) }
    var itemToDelete by remember { mutableStateOf<MenuItem?>(null) }

    LaunchedEffect(Unit) {
        menuViewModel.loadAllItems()
    }

    LaunchedEffect(uiState.operacionExitosa) {
        if (uiState.operacionExitosa) {
            showForm = false
            editingItem = null
            menuViewModel.limpiarEstado()
        }
    }

    ScreenScaffold(
        title = "Gestión de Menú",
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
        ) {
            Column(modifier = Modifier.padding(horizontal = Spacing.lg)) {
                Text(
                    text = "Platos del menú",
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.padding(top = Spacing.xl, bottom = Spacing.lg)
                )

                if (uiState.error != null) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = Spacing.md)
                    )
                }

                if (uiState.isLoading && uiState.allItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.allItems, key = { it.id }) { item ->
                            AdminMenuItemCard(
                                item = item,
                                imagenRes = MenuImages.getImageResId(item.imagenRef),
                                onToggleDisponibilidad = {
                                    menuViewModel.toggleDisponibilidad(item.id, !item.disponible)
                                },
                                onEdit = {
                                    editingItem = item
                                    showForm = true
                                },
                                onDelete = {
                                    itemToDelete = item
                                }
                            )
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    editingItem = null
                    showForm = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Rounded.Edit, contentDescription = "Agregar o editar plato")
            }
        }
    }

    if (showForm) {
        MenuItemFormDialog(
            item = editingItem,
            onDismiss = {
                showForm = false
                editingItem = null
            },
            onConfirm = { nombre, descripcion, precio, categoriaId, imagenRef ->
                if (editingItem != null) {
                    menuViewModel.updateItem(editingItem!!.id, nombre, descripcion, precio, categoriaId, imagenRef)
                } else {
                    menuViewModel.createItem(nombre, descripcion, precio, categoriaId, imagenRef)
                }
            }
        )
    }

    itemToDelete?.let { item ->
        ConfirmModal(
            visible = true,
            titulo = "¿Eliminar \"${item.nombre}\"?",
            onConfirm = {
                menuViewModel.deleteItem(item.id)
                itemToDelete = null
            },
            onCancel = { itemToDelete = null }
        )
    }
}

@Composable
fun AdminMenuItemCard(
    item: MenuItem,
    imagenRes: Int,
    onToggleDisponibilidad: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        MenuItemCard(
            nombre = item.nombre,
            precio = item.precio,
            imagenRes = imagenRes,
            showControls = false,
            disponible = true,
            onClick = onEdit
        )

        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
            Switch(
                checked = item.disponible,
                onCheckedChange = { onToggleDisponibilidad() },
                modifier = Modifier.size(40.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Primary,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemFormDialog(
    item: MenuItem?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Int, String) -> Unit
) {
    var nombre by remember { mutableStateOf(item?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(item?.descripcion ?: "") }
    var precio by remember { mutableStateOf(if (item != null) item.precio.toString() else "") }
    var categoriaId by remember { mutableIntStateOf(item?.categoriaId ?: 1) }
    var imagenRef by remember { mutableStateOf(item?.imagenRef ?: imagenOptions.first()) }
    var expandedCategoria by remember { mutableStateOf(false) }
    var expandedImagen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (item != null) "Editar Plato" else "Nuevo Plato",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                AppTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = "Nombre"
                )
                AppTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = "Descripción"
                )
                AppTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = "Precio"
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCategoria,
                    onExpandedChange = { expandedCategoria = it }
                ) {
                    OutlinedTextField(
                        value = categorias.find { it.first == categoriaId }?.second ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoria) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategoria,
                        onDismissRequest = { expandedCategoria = false }
                    ) {
                        categorias.forEach { (id, nombre) ->
                            DropdownMenuItem(
                                text = { Text(nombre) },
                                onClick = {
                                    categoriaId = id
                                    expandedCategoria = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedImagen,
                    onExpandedChange = { expandedImagen = it }
                ) {
                    OutlinedTextField(
                        value = imagenRef,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Imagen") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedImagen) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedImagen,
                        onDismissRequest = { expandedImagen = false }
                    ) {
                        imagenOptions.forEach { nombre ->
                            DropdownMenuItem(
                                text = { Text(nombre) },
                                onClick = {
                                    imagenRef = nombre
                                    expandedImagen = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = if (item != null) "Guardar" else "Crear",
                onClick = {
                    val precioDouble = precio.toDoubleOrNull() ?: 0.0
                    onConfirm(nombre, descripcion, precioDouble, categoriaId, imagenRef)
                },
                isLoading = false
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
