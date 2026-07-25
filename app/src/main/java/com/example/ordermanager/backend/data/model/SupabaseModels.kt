package com.example.ordermanager.backend.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsuarioRow(
    @SerialName("id")
    val id: String,
    @SerialName("nombres")
    val nombres: String,
    @SerialName("correo")
    val correo: String,
    @SerialName("usuario")
    val usuario: String,
    @SerialName("telefono")
    val telefono: String,
    @SerialName("rol")
    val rol: String,
    @SerialName("activo")
    val activo: Boolean,
    @SerialName("latitud")
    val latitud: Double,
    @SerialName("longitud")
    val longitud: Double,
    @SerialName("fecha_registro")
    val fechaRegistro: String? = null
)

@Serializable
data class UsuarioInsert(
    @SerialName("id")
    val id: String,
    @SerialName("nombres")
    val nombres: String,
    @SerialName("correo")
    val correo: String,
    @SerialName("usuario")
    val usuario: String,
    @SerialName("telefono")
    val telefono: String,
    @SerialName("rol")
    val rol: String,
    @SerialName("activo")
    val activo: Boolean,
    @SerialName("latitud")
    val latitud: Double,
    @SerialName("longitud")
    val longitud: Double
)

@Serializable
data class MenuItemRow(
    val id: Int,
    val nombre: String,
    val descripcion: String = "",
    val precio: Double,
    @SerialName("categoria_id")
    val categoriaId: Int = 1,
    @SerialName("imagen_ref")
    val imagenRef: String = "",
    val disponible: Boolean = true,
    val activo: Boolean = true
)

@Serializable
data class MenuItemInsert(
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    @SerialName("categoria_id")
    val categoriaId: Int,
    @SerialName("imagen_ref")
    val imagenRef: String,
    val disponible: Boolean,
    val activo: Boolean
)

@Serializable
data class PedidoRow(
    val id: String,
    val cliente: String,
    @SerialName("mesa_o_direccion")
    val mesaODireccion: String = "N/A",
    val estado: String,
    val total: Double,
    val notas: String? = null,
    @SerialName("timestamp_creacion")
    val timestampCreacion: String,
    @SerialName("mesero_id")
    val meseroId: String? = null,
    val origen: String
)

@Serializable
data class PedidoItemInsert(
    @SerialName("item_menu_id")
    val itemMenuId: Int? = null,
    @SerialName("nombre_item_snapshot")
    val nombreItemSnapshot: String,
    val cantidad: Int,
    @SerialName("precio_unitario_snapshot")
    val precioUnitarioSnapshot: Double,
    val subtotal: Double,
    val notas: String? = null
)

@Serializable
data class PedidoInsert(
    @SerialName("mesero_id")
    val meseroId: String,
    val cliente: String,
    @SerialName("mesa_o_direccion")
    val mesaODireccion: String,
    val estado: String,
    val origen: String,
    val total: Double,
    val notas: String? = null
)

@Serializable
data class PedidoItemRow(
    @SerialName("pedido_id")
    val pedidoId: String,
    @SerialName("nombre_item_snapshot")
    val nombreItemSnapshot: String,
    val cantidad: Int,
    val subtotal: Double
)
