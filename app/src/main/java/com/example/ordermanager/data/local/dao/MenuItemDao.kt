package com.example.ordermanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ordermanager.data.local.entity.MenuItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuItemDao {

    @Query("SELECT * FROM menu_items WHERE activo = 1 AND disponible = 1 ORDER BY id ASC")
    fun getItemsDisponibles(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE activo = 1 ORDER BY id ASC")
    fun getAllItems(): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(menuItem: MenuItemEntity): Long

    @Update
    suspend fun actualizar(menuItem: MenuItemEntity): Int

    @Query("UPDATE menu_items SET disponible = :disponible WHERE id = :itemId")
    suspend fun actualizarDisponibilidad(itemId: Int, disponible: Boolean): Int

    @Query("DELETE FROM menu_items WHERE id = :itemId")
    suspend fun eliminarPorId(itemId: Int): Int

    @Delete
    suspend fun borrar(menuItem: MenuItemEntity): Int
}