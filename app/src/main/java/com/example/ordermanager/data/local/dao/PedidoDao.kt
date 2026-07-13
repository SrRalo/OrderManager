package com.example.ordermanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ordermanager.data.local.entity.PedidoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(pedido: PedidoEntity)

    @Query("DELETE FROM pedidos WHERE id = :pedidoId")
    suspend fun eliminar(pedidoId: String)

    @Query("SELECT * FROM pedidos ORDER BY timestamp DESC")
    fun obtenerTodos(): Flow<List<PedidoEntity>>

    @Query("SELECT * FROM pedidos WHERE id = :pedidoId LIMIT 1")
    suspend fun obtenerPorId(pedidoId: String): PedidoEntity?
}
