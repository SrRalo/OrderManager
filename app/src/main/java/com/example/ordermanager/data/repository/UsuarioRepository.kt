package com.example.ordermanager.data.repository

import com.example.ordermanager.data.local.dao.UsuarioDao
import com.example.ordermanager.data.local.entity.UsuarioEntity

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    suspend fun registrar(usuario: UsuarioEntity): Result<Long> {
        return try {
            val id = usuarioDao.insertar(usuario)
            if (id > 0) Result.success(id)
            else Result.failure(Exception("No se pudo registrar el usuario"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun iniciarSesion(username: String, password: String): Result<UsuarioEntity> {
        val usuario = usuarioDao.validarCredenciales(username, password)
        return if (usuario != null) {
            Result.success(usuario)
        } else {
            val existe = usuarioDao.obtenerPorUsuarioOCorreo(username)
            if (existe == null) {
                Result.failure(Exception("usuario_no_existe"))
            } else {
                Result.failure(Exception("contrasena_incorrecta"))
            }
        }
    }

    suspend fun existeUsuario(username: String): Boolean {
        return usuarioDao.obtenerPorUsuarioOCorreo(username) != null
    }

    suspend fun existeCorreo(correo: String): Boolean {
        return usuarioDao.obtenerPorCorreo(correo) != null
    }
}
