package com.example.ordermanager.backend.data.repository

import com.example.ordermanager.backend.data.model.UsuarioInsert
import com.example.ordermanager.backend.data.model.UsuarioRow
import com.example.ordermanager.backend.supabase.SupabaseClientProvider
import com.example.ordermanager.data.local.entity.UsuarioEntity
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.time.Instant

class SupabaseAuthRepository {

    private val client = SupabaseClientProvider.client

    suspend fun login(usernameOrEmail: String, password: String): UsuarioEntity {
        val profile = findProfileByUsernameOrEmail(usernameOrEmail.trim())
            ?: error("usuario_no_existe")

        client.auth.signInWith(Email) {
            email = profile.correo
            this.password = password
        }

        // Get the actual Supabase Auth user ID to ensure RLS policies work correctly
        val authUserId = client.auth.currentUserOrNull()?.id
                ?: error("No se pudo obtener el usuario autenticado")

        return UsuarioEntity(
            supabaseId = authUserId,
            nombres = profile.nombres,
            correo = profile.correo,
            usuario = profile.usuario,
            contrasena = "", // Password not stored in entity
            telefono = profile.telefono,
            fechaRegistro = System.currentTimeMillis(),
            latitud = profile.latitud,
            longitud = profile.longitud,
            rol = profile.rol
        )
    }

    suspend fun register(
        nombres: String,
        correo: String,
        usuario: String,
        contrasena: String,
        telefono: String,
        latitud: Double,
        longitud: Double,
        rol: String
    ): UsuarioEntity {
        val signUpInfo = client.auth.signUpWith(Email) {
            email = correo
            password = contrasena
        }

        val userId = signUpInfo?.id ?: client.auth.currentUserOrNull()?.id
            ?: error("No se pudo obtener el id del usuario creado en Supabase Auth")

        val profile = UsuarioInsert(
            id = userId,
            nombres = nombres,
            correo = correo,
            usuario = usuario,
            telefono = telefono,
            rol = rol,
            activo = true,
            latitud = latitud,
            longitud = longitud
        )

        client.from("usuarios").insert(profile)

        return UsuarioRow(
            id = userId,
            nombres = nombres,
            correo = correo,
            usuario = usuario,
            telefono = telefono,
            rol = rol,
            activo = true,
            latitud = latitud,
            longitud = longitud,
            fechaRegistro = Instant.now().toString()
        ).toEntity()
    }

    suspend fun findProfileByUsernameOrEmail(usernameOrEmail: String): UsuarioRow? {
        val byUsername = client.from("usuarios")
            .select(columns = Columns.ALL) {
                filter {
                    eq("usuario", usernameOrEmail)
                }
                limit(1)
            }
            .decodeSingleOrNull<UsuarioRow>()

        if (byUsername != null) return byUsername

        return client.from("usuarios")
            .select(columns = Columns.ALL) {
                filter {
                    eq("correo", usernameOrEmail)
                }
                limit(1)
            }
            .decodeSingleOrNull<UsuarioRow>()
    }

    suspend fun findProfileBySupabaseId(supabaseId: String): UsuarioRow? {
        return client.from("usuarios")
            .select(columns = Columns.ALL) {
                filter {
                    eq("id", supabaseId)
                }
                limit(1)
            }
            .decodeSingleOrNull<UsuarioRow>()
    }

    suspend fun signOut() {
        client.auth.signOut()
    }

    private fun UsuarioRow.toEntity(): UsuarioEntity {
        return UsuarioEntity(
            supabaseId = id,
            nombres = nombres,
            correo = correo,
            usuario = usuario,
            contrasena = "",
            telefono = telefono,
            fechaRegistro = System.currentTimeMillis(),
            latitud = latitud,
            longitud = longitud,
            rol = rol
        )
    }
}
