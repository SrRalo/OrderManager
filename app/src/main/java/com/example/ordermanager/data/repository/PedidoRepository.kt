package com.example.ordermanager.data.repository

import com.example.ordermanager.data.local.dao.PedidoDao
import com.example.ordermanager.data.local.entity.PedidoEntity
import kotlinx.coroutines.flow.Flow

class PedidoRepository(private val pedidoDao: PedidoDao) {

    suspend fun insertar(pedido: PedidoEntity) {
        pedidoDao.insertar(pedido)
    }

    suspend fun eliminar(pedidoId: String) {
        pedidoDao.eliminar(pedidoId)
    }

    fun obtenerTodos(): Flow<List<PedidoEntity>> {
        return pedidoDao.obtenerTodos()
    }
}
