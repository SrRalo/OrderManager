package com.example.ordermanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "descripcion")
    val descripcion: String = "",

    @ColumnInfo(name = "precio")
    val precio: Double,

    @ColumnInfo(name = "categoria_id")
    val categoriaId: Int = 1,

    @ColumnInfo(name = "imagen_ref")
    val imagenRef: String = "",

    @ColumnInfo(name = "disponible")
    val disponible: Boolean = true,

    @ColumnInfo(name = "activo")
    val activo: Boolean = true
)