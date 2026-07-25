package com.example.ordermanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ordermanager.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarOReemplazar(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios WHERE usuario = :username OR correo = :username LIMIT 1")
    suspend fun obtenerPorUsuarioOCorreo(username: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE (usuario = :username OR correo = :username) AND contraseña = :password LIMIT 1")
    suspend fun validarCredenciales(username: String, password: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE usuario = :username LIMIT 1")
    suspend fun obtenerPorUsuario(username: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun obtenerPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE supabase_id = :supabaseId LIMIT 1")
    suspend fun obtenerPorSupabaseId(supabaseId: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios")
    fun obtenerTodos(): Flow<List<UsuarioEntity>>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contarUsuarios(): Int
}
