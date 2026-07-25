package com.example.ordermanager.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ordermanager.backend.data.repository.SupabasePedidoRepository
import com.example.ordermanager.data.repository.MenuItemRepository
import com.example.ordermanager.ui.menu.MenuItem
import com.example.ordermanager.ui.menu.MenuImages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class MenuUiState(
    val itemsDisponibles: List<MenuItem> = emptyList(),
    val allItems: List<MenuItem> = emptyList(),
    val cantidades: Map<Int, Int> = emptyMap(),
    val notaGeneral: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val pedidoEnviado: Boolean = false,
    val operacionExitosa: Boolean = false
)

class MenuViewModel(application: Application) : AndroidViewModel(application) {

    private val menuItemRepository = MenuItemRepository.getInstance(application)
    private val pedidoRepository = SupabasePedidoRepository.getInstance()

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private var meseroId: String? = null

    init {
        loadMenu()
    }

    fun setMeseroId(userId: String?) {
        meseroId = userId
    }

    fun loadMenu() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val items = menuItemRepository.getItemsDisponibles()
                _uiState.update {
                    it.copy(
                        itemsDisponibles = items,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "No se pudo cargar el menú: ${e.message}"
                    )
                }
            }
        }
    }

    fun addToCart(itemId: Int) {
        _uiState.update { state ->
            val currentCount = state.cantidades[itemId] ?: 0
            state.copy(
                cantidades = state.cantidades + (itemId to (currentCount + 1)),
                pedidoEnviado = false
            )
        }
    }

    fun removeFromCart(itemId: Int) {
        _uiState.update { state ->
            val currentCount = state.cantidades[itemId] ?: return@update state
            val updated = if (currentCount <= 1) {
                state.cantidades - itemId
            } else {
                state.cantidades + (itemId to (currentCount - 1))
            }
            state.copy(cantidades = updated, pedidoEnviado = false)
        }
    }

    fun updateNota(texto: String) {
        _uiState.update { it.copy(notaGeneral = texto) }
    }

    fun enviarPedido() {
        viewModelScope.launch {
            val uid = meseroId
            if (uid.isNullOrBlank()) {
                _uiState.update { it.copy(error = "No hay sesión activa para enviar el pedido") }
                return@launch
            }

            val selectedItems = getCartItemsMap()
            if (selectedItems.isEmpty()) {
                _uiState.update { it.copy(error = "Agrega al menos un ítem al carrito") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                pedidoRepository.createPedido(
                    meseroId = uid,
                    items = selectedItems,
                    notaGeneral = _uiState.value.notaGeneral
                )
                _uiState.update {
                    it.copy(
                        cantidades = emptyMap(),
                        notaGeneral = "",
                        isLoading = false,
                        pedidoEnviado = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error desconocido al enviar el pedido"
                    )
                }
            }
        }
    }

    fun limpiarCarrito() {
        _uiState.update {
            it.copy(
                cantidades = emptyMap(),
                notaGeneral = "",
                pedidoEnviado = false
            )
        }
    }

    fun getCantidad(itemId: Int): Int = _uiState.value.cantidades[itemId] ?: 0

    fun getTotalCarrito(): Double {
        val state = _uiState.value
        return state.cantidades.entries.sumOf { (itemId, cantidad) ->
            state.itemsDisponibles.find { it.id == itemId }?.precio?.times(cantidad) ?: 0.0
        }
    }

    fun getItemsEnCarrito(): List<MenuItem> {
        return _uiState.value.cantidades.keys.mapNotNull { id ->
            _uiState.value.itemsDisponibles.find { it.id == id }
        }
    }

    private fun getCartItemsMap(): Map<MenuItem, Int> {
        val state = _uiState.value
        return state.cantidades.mapNotNull { (itemId, cantidad) ->
            val item = state.itemsDisponibles.find { it.id == itemId } ?: return@mapNotNull null
            item to cantidad
        }.toMap()
    }

    fun getImagenRes(itemName: String): Int = MenuImages.getImageResId(itemName)

    fun loadAllItems() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val items = menuItemRepository.getAllItems()
                _uiState.update {
                    it.copy(allItems = items, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Error al cargar menú: ${e.message}")
                }
            }
        }
    }

    fun createItem(
        nombre: String,
        descripcion: String,
        precio: Double,
        categoriaId: Int,
        imagenRef: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                menuItemRepository.insertar(
                    MenuItem(
                        id = 0,
                        nombre = nombre,
                        descripcion = descripcion,
                        precio = precio,
                        categoriaId = categoriaId,
                        imagenRef = imagenRef,
                        disponible = true,
                        activo = true
                    )
                )
                _uiState.update { it.copy(isLoading = false, operacionExitosa = true) }
                delay(500)
                loadAllItems()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Error al crear plato")
                }
            }
        }
    }

    fun updateItem(
        itemId: Int,
        nombre: String,
        descripcion: String,
        precio: Double,
        categoriaId: Int,
        imagenRef: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                menuItemRepository.actualizar(
                    MenuItem(
                        id = itemId,
                        nombre = nombre,
                        descripcion = descripcion,
                        precio = precio,
                        categoriaId = categoriaId,
                        imagenRef = imagenRef,
                        disponible = true,
                        activo = true
                    )
                )
                _uiState.update { it.copy(isLoading = false, operacionExitosa = true) }
                delay(500)
                loadAllItems()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Error al actualizar plato")
                }
            }
        }
    }

    fun deleteItem(itemId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                menuItemRepository.eliminar(itemId)
                _uiState.update { it.copy(isLoading = false, operacionExitosa = true) }
                delay(500)
                loadAllItems()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Error al eliminar plato")
                }
            }
        }
    }

    fun toggleDisponibilidad(itemId: Int, disponible: Boolean) {
        viewModelScope.launch {
            try {
                menuItemRepository.actualizarDisponibilidad(itemId, disponible)
                loadAllItems()
            } catch (e: Exception) {
            }
        }
    }

    fun limpiarEstado() {
        _uiState.update { it.copy(operacionExitosa = false, error = null) }
    }
}
