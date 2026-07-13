package com.example.ordermanager.backend.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val nombres: String,
    val correo: String,
    val usuario: String,
    val contrasena: String,
    val telefono: String,
    val latitud: Double,
    val longitud: Double
)

data class AuthResponse(
    val success: Boolean,
    val mensaje: String,
    val usuarioId: Long? = null
)
