package com.example.ordermanager.data.repository

import com.example.ordermanager.backend.data.repository.SupabaseAuthRepository
import com.example.ordermanager.data.local.dao.UsuarioDao
import com.example.ordermanager.data.local.entity.UsuarioEntity

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    private val supabaseAuthRepository = SupabaseAuthRepository()

    suspend fun registrar(usuario: UsuarioEntity): Result<Long> {
        return try {
            val remote = supabaseAuthRepository.register(
                nombres = usuario.nombres,
                correo = usuario.correo,
                usuario = usuario.usuario,
                contrasena = usuario.contrasena,
                telefono = usuario.telefono,
                latitud = usuario.latitud,
                longitud = usuario.longitud,
                rol = usuario.rol
            )
            // Save locally WITHOUT password for security
            val localId = usuarioDao.guardarOReemplazar(
                remote.copy(
                    contrasena = "" // Never store password locally
                )
            )
            val id = if (localId > 0) localId else usuarioDao.insertar(
                usuario.copy(
                    contrasena = "" // Don't store password in local entity either
                )
            )
            if (id > 0) Result.success(id)
            else Result.failure(Exception("No se pudo registrar el usuario"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun iniciarSesion(username: String, password: String): Result<UsuarioEntity> {
        return try {
            // First check if user exists locally (for performance optimization)
            // This doesn't validate password, just checks existence
            val localUserExists = usuarioDao.obtenerPorUsuarioOCorreo(username.trim()) != null

            // Always authenticate with Supabase (source of truth)
            val remoteUser = supabaseAuthRepository.login(username.trim(), password)

            // Save/update locally WITHOUT password for security
            usuarioDao.guardarOReemplazar(
                remoteUser.copy(
                    contrasena = "" // Never store password locally
                )
            )

            Result.success(remoteUser)
        } catch (e: Exception) {
            if ((e.message ?: "").contains("usuario_no_existe")) {
                Result.failure(Exception("usuario_no_existe"))
            } else {
                Result.failure(Exception("contrasena_incorrecta"))
            }
        }
    }

    suspend fun existeUsuario(username: String): Boolean {
        return supabaseAuthRepository.findProfileByUsernameOrEmail(username.trim()) != null
    }

    suspend fun existeCorreo(correo: String): Boolean {
        return supabaseAuthRepository.findProfileByUsernameOrEmail(correo.trim())?.correo == correo.trim()
    }

    suspend fun contarUsuarios(): Int {
        return usuarioDao.contarUsuarios()
    }

    suspend fun logout() {
        supabaseAuthRepository.signOut()
    }
}
