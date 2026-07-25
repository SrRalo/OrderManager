package com.example.ordermanager.backend.data.repository

import com.example.ordermanager.backend.data.model.UsuarioInsert
import com.example.ordermanager.backend.data.model.UsuarioRow
import com.example.ordermanager.backend.supabase.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class SupabaseUserRepository {

    private val client = SupabaseClientProvider.client

    suspend fun fetchUsuarios(): List<UsuarioRow> {
        val usuarios = client.from("usuarios")
            .select(columns = Columns.ALL) {
                order("fecha_registro", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<UsuarioRow>()

        // Log for debugging - remove in production
        for (usuario in usuarios) {
            // In a real app, you'd use proper logging
            // println("Usuario: ${usuario.usuario}, rol: ${usuario.rol}")
        }

        return usuarios
    }

    suspend fun fetchUsuarioById(id: String): UsuarioRow? {
        return client.from("usuarios")
            .select(columns = Columns.ALL) {
                filter { eq("id", id) }
                limit(1)
            }
            .decodeSingleOrNull<UsuarioRow>()
    }

    suspend fun crearUsuario(
        id: String,
        nombres: String,
        correo: String,
        usuario: String,
        telefono: String,
        rol: String,
        latitud: Double,
        longitud: Double
    ): UsuarioRow {
        val insert = UsuarioInsert(
            id = id,
            nombres = nombres,
            correo = correo,
            usuario = usuario,
            telefono = telefono,
            rol = rol,
            activo = true,
            latitud = latitud,
            longitud = longitud
        )

        return client.from("usuarios")
            .insert(insert) {
                select()
            }
            .decodeSingle<UsuarioRow>()
    }

    suspend fun cambiarRol(userId: String, nuevoRol: String) {
        client.from("usuarios").update(
            {
                set("rol", nuevoRol)
            }
        ) {
            filter { eq("id", userId) }
        }
    }

    suspend fun desactivarUsuario(userId: String, activo: Boolean) {
        client.from("usuarios").update(
            {
                set("activo", activo)
            }
        ) {
            filter { eq("id", userId) }
        }
    }
}
