package com.example.ordermanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [
        Index(value = ["correo"], unique = true),
        Index(value = ["usuario"], unique = true)
    ]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombres: String,
    @ColumnInfo(name = "correo")
    val correo: String,
    @ColumnInfo(name = "usuario")
    val usuario: String,
    @ColumnInfo(name = "contraseña")
    val contrasena: String,
    val telefono: String,
    @ColumnInfo(name = "fecha_registro")
    val fechaRegistro: Long,
    val latitud: Double,
    val longitud: Double
)
