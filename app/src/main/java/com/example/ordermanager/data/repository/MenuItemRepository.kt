package com.example.ordermanager.data.repository

import android.content.Context
import com.example.ordermanager.data.local.AppDatabase
import com.example.ordermanager.data.local.dao.MenuItemDao
import com.example.ordermanager.data.local.entity.MenuItemEntity
import com.example.ordermanager.ui.menu.MenuItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MenuItemRepository private constructor(private val menuItemDao: MenuItemDao) {

    companion object {
        @Volatile
        private var INSTANCE: MenuItemRepository? = null

        fun getInstance(context: Context): MenuItemRepository {
            return INSTANCE ?: synchronized(this) {
                val database = AppDatabase.getInstance(context)
                val instance = MenuItemRepository(database.menuItemDao())
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun getItemsDisponibles(): List<MenuItem> = withContext(Dispatchers.IO) {
        menuItemDao.getItemsDisponibles().first().map { entity ->
            MenuItem(
                id = entity.id,
                nombre = entity.nombre,
                descripcion = entity.descripcion,
                precio = entity.precio,
                categoriaId = entity.categoriaId,
                imagenRef = entity.imagenRef,
                disponible = entity.disponible,
                activo = entity.activo
            )
        }
    }

    suspend fun getAllItems(): List<MenuItem> = withContext(Dispatchers.IO) {
        menuItemDao.getAllItems().first().map { entity ->
            MenuItem(
                id = entity.id,
                nombre = entity.nombre,
                descripcion = entity.descripcion,
                precio = entity.precio,
                categoriaId = entity.categoriaId,
                imagenRef = entity.imagenRef,
                disponible = entity.disponible,
                activo = entity.activo
            )
        }
    }

    suspend fun insertar(item: MenuItem) = withContext(Dispatchers.IO) {
        menuItemDao.insertar(
            MenuItemEntity(
                id = item.id,
                nombre = item.nombre,
                descripcion = item.descripcion,
                precio = item.precio,
                categoriaId = item.categoriaId,
                imagenRef = item.imagenRef,
                disponible = item.disponible,
                activo = item.activo
            )
        )
    }

    suspend fun actualizar(item: MenuItem) = withContext(Dispatchers.IO) {
        menuItemDao.actualizar(
            MenuItemEntity(
                id = item.id,
                nombre = item.nombre,
                descripcion = item.descripcion,
                precio = item.precio,
                categoriaId = item.categoriaId,
                imagenRef = item.imagenRef,
                disponible = item.disponible,
                activo = item.activo
            )
        )
    }

    suspend fun actualizarDisponibilidad(itemId: Int, disponible: Boolean) = withContext(Dispatchers.IO) {
        menuItemDao.actualizarDisponibilidad(itemId, disponible)
    }

    suspend fun eliminar(itemId: Int) = withContext(Dispatchers.IO) {
        menuItemDao.eliminarPorId(itemId)
    }
}
