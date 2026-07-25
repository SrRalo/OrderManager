package com.example.ordermanager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ordermanager.data.local.dao.MenuItemDao
import com.example.ordermanager.data.local.dao.PedidoDao
import com.example.ordermanager.data.local.dao.UsuarioDao
import com.example.ordermanager.data.local.entity.MenuItemEntity
import com.example.ordermanager.data.local.entity.PedidoEntity
import com.example.ordermanager.data.local.entity.UsuarioEntity

@Database(entities = [UsuarioEntity::class, PedidoEntity::class, MenuItemEntity::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun pedidoDao(): PedidoDao
    abstract fun menuItemDao(): MenuItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "order_manager_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}