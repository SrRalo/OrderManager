package com.example.ordermanager.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ordermanager.backend.data.repository.SupabasePedidoRepository
import com.example.ordermanager.data.local.entity.PedidoEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderUiState(
    val orders: List<PedidoEntity> = emptyList(),
    val completedOrders: List<PedidoEntity> = emptyList(),
    val newOrderAlert: PedidoEntity? = null,
    val pendingConfirmOrders: Map<String, PedidoEntity> = emptyMap(),
    val confirmTimers: Map<String, Int> = emptyMap()
)

class OrderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SupabasePedidoRepository.getInstance()

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    private val sendJobs = mutableMapOf<String, Job>()
    private val cancelledOrders = mutableSetOf<String>()
    private var pollingJob: Job? = null

    init {
        startPollingPedidos()
        collectLocalOrders()
    }

    private fun collectLocalOrders() {
        viewModelScope.launch {
            repository.localOrders.collect { localOrder ->
                _uiState.update { state ->
                    // Add the local order to the top of the list if not already there
                    if (state.orders.none { it.id == localOrder.id }) {
                        state.copy(
                            orders = listOf(localOrder) + state.orders,
                            newOrderAlert = localOrder
                        )
                    } else {
                        state
                    }
                }
            }
        }
    }

    private fun startPollingPedidos() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                refreshPedidos()
                delay(5000L)
            }
        }
    }

    fun refreshPedidos() {
        viewModelScope.launch {
            runCatching {
                val pendingFromRepo = repository.fetchPedidosPendientes()
                val completedFromRepo = repository.fetchPedidosCompletados()
                
                _uiState.update { state ->
                    // 1. Remove duplicates from repo lists just in case
                    val pendingUnique = pendingFromRepo.distinctBy { it.id }
                    val completedUnique = completedFromRepo.distinctBy { it.id }
                    
                    // 2. Filter out orders that are currently in the "undo" state (pending confirmation)
                    // This prevents them from appearing in BOTH lists simultaneously
                    val filteredPending = pendingUnique.filter { it.id !in state.pendingConfirmOrders.keys }
                    
                    // 3. Detect new orders for the alert
                    val currentPendingIds = state.orders.map { it.id }.toSet()
                    val newOrder = filteredPending.firstOrNull { it.id !in currentPendingIds }
                    
                    state.copy(
                        orders = filteredPending,
                        completedOrders = completedUnique,
                        newOrderAlert = newOrder
                    )
                }
            }
        }
    }

    fun startSendConfirmation(orderId: String) {
        _uiState.update { state ->
            val order = state.orders.find { it.id == orderId } ?: return@update state
            state.copy(
                orders = state.orders.filter { it.id != orderId },
                pendingConfirmOrders = state.pendingConfirmOrders + (orderId to order),
                confirmTimers = state.confirmTimers + (orderId to 3)
            )
        }

        val job = viewModelScope.launch {
            try {
                for (remaining in 2 downTo 1) {
                    delay(1000)
                    _uiState.update { state ->
                        if (!state.confirmTimers.containsKey(orderId)) return@update state
                        state.copy(confirmTimers = state.confirmTimers + (orderId to remaining))
                    }
                }
                delay(1000)

                if (cancelledOrders.contains(orderId)) return@launch
                try {
                    repository.marcarCompletado(orderId)

                    _uiState.update { state ->
                        val updatedPending = state.pendingConfirmOrders - orderId
                        val updatedTimers = state.confirmTimers - orderId
                        state.copy(
                            pendingConfirmOrders = updatedPending,
                            confirmTimers = updatedTimers
                        )
                    }
                    refreshPedidos()
                } catch (e: Exception) {
                    // If marking as completed fails, return the order to the pending list
                    _uiState.update { state ->
                        val order = state.pendingConfirmOrders[orderId] ?: return@update state
                        val updatedPending = state.pendingConfirmOrders - orderId
                        val updatedTimers = state.confirmTimers - orderId
                        state.copy(
                            orders = listOf(order) + state.orders,
                            pendingConfirmOrders = updatedPending,
                            confirmTimers = updatedTimers
                        )
                    }
                    // Re-throw if we want the calling code to handle it, or just log it
                    // For now, we'll just log it since we've recovered the state
                    Log.e("OrderViewModel", "Failed to mark order as completed: $orderId", e)
                }
            } finally {
                sendJobs.remove(orderId)
            }
        }
        sendJobs[orderId] = job
    }

    fun cancelSend(orderId: String) {
        cancelledOrders.add(orderId)
        sendJobs[orderId]?.cancel()
        sendJobs.remove(orderId)
        _uiState.update { state ->
            val order = state.pendingConfirmOrders[orderId] ?: return@update state
            state.copy(
                pendingConfirmOrders = state.pendingConfirmOrders - orderId,
                confirmTimers = state.confirmTimers - orderId,
                orders = listOf(order) + state.orders
            )
        }
        viewModelScope.launch {
            delay(5000)
            cancelledOrders.remove(orderId)
        }
    }

    fun dismissAlert() {
        _uiState.update { it.copy(newOrderAlert = null) }
    }
}
