package com.example.ordermanager.ui.menu

data class MenuItem(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val categoriaId: Int,
    val imagenRef: String,
    val disponible: Boolean = true,
    val activo: Boolean = true
)

data class CategoriaMenu(
    val id: Int,
    val nombre: String,
    val orden: Int = 0
)

data class CartItem(
    val menuItem: MenuItem,
    val cantidad: Int = 1
)