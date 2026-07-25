package com.example.ordermanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ordermanager.backend.data.repository.SupabaseUserRepository
import com.example.ordermanager.backend.data.model.UsuarioRow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserManagementUiState(
    val usuarios: List<UsuarioRow> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val operacionExitosa: Boolean = false
)

class UserManagementViewModel : ViewModel() {

    private val userRepository = SupabaseUserRepository()

    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    init {
        listarUsuarios()
    }

    fun listarUsuarios() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            runCatching { userRepository.fetchUsuarios() }
                .onSuccess { usuarios ->
                    _uiState.update {
                        it.copy(usuarios = usuarios, isLoading = false)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Error al cargar usuarios"
                        )
                    }
                }
        }
    }

    fun cambiarRol(userId: String, nuevoRol: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { userRepository.cambiarRol(userId, nuevoRol) }
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, operacionExitosa = true) }
                    kotlinx.coroutines.delay(500)
                    listarUsuarios()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Error al cambiar rol"
                        )
                    }
                }
        }
    }

    fun setUsuarioActivo(userId: String, activo: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { userRepository.desactivarUsuario(userId, activo) }
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, operacionExitosa = true) }
                    kotlinx.coroutines.delay(500)
                    listarUsuarios()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Error al desactivar usuario"
                        )
                    }
                }
        }
    }

    fun limpiarEstado() {
        _uiState.update { it.copy(operacionExitosa = false, error = null) }
    }
}
