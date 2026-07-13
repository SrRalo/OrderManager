package com.example.ordermanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pedidos")
data class PedidoEntity(
    @PrimaryKey
    val id: String,
    val cliente: String,
    val direccion: String,
    val productos: String,
    val total: Double,
    val tiempoEstimado: Int,
    val notas: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
