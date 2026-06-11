package com.example.ordermanager.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ordermanager.data.local.AppDatabase
import com.example.ordermanager.data.local.entity.UsuarioEntity
import com.example.ordermanager.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Screen { SPLASH, WELCOME, LOGIN, HOME, REGISTER }

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

data class RegisterUiState(
    val nombres: String = "",
    val correo: String = "",
    val usuario: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val telefono: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val registroExitoso: Boolean = false
)

data class AuthState(
    val currentUser: UsuarioEntity? = null,
    val currentScreen: Screen = Screen.SPLASH
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UsuarioRepository

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private val _registerUiState = MutableStateFlow(RegisterUiState())
    val registerUiState: StateFlow<RegisterUiState> = _registerUiState.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = UsuarioRepository(db.usuarioDao())
    }

    fun updateLoginUsername(value: String) {
        _loginUiState.update { it.copy(username = value, errorMessage = null) }
    }

    fun updateLoginPassword(value: String) {
        _loginUiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun login() {
        val state = _loginUiState.value

        if (state.username.isBlank()) {
            _loginUiState.update { it.copy(errorMessage = "Ingrese su correo o usuario") }
            return
        }
        if (state.password.isBlank()) {
            _loginUiState.update { it.copy(errorMessage = "Ingrese su contraseña") }
            return
        }

        _loginUiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.iniciarSesion(state.username.trim(), state.password)
            result.fold(
                onSuccess = { usuario ->
                    _loginUiState.update { it.copy(isLoading = false) }
                    _authState.update {
                        it.copy(currentUser = usuario, currentScreen = Screen.HOME)
                    }
                },
                onFailure = { error ->
                    val msg = when (error.message) {
                        "usuario_no_existe" -> "El usuario no existe"
                        "contrasena_incorrecta" -> "Contraseña incorrecta"
                        else -> error.message ?: "Error al iniciar sesión"
                    }
                    _loginUiState.update { it.copy(isLoading = false, errorMessage = msg) }
                }
            )
        }
    }

    fun updateRegisterNombres(value: String) {
        _registerUiState.update { it.copy(nombres = value, errorMessage = null) }
    }

    fun updateRegisterCorreo(value: String) {
        _registerUiState.update { it.copy(correo = value, errorMessage = null) }
    }

    fun updateRegisterUsuario(value: String) {
        _registerUiState.update { it.copy(usuario = value, errorMessage = null) }
    }

    fun updateRegisterContrasena(value: String) {
        _registerUiState.update { it.copy(contrasena = value, errorMessage = null) }
    }

    fun updateRegisterConfirmarContrasena(value: String) {
        _registerUiState.update { it.copy(confirmarContrasena = value, errorMessage = null) }
    }

    fun updateRegisterTelefono(value: String) {
        _registerUiState.update { it.copy(telefono = value, errorMessage = null) }
    }

    fun updateLocation(lat: Double, lng: Double) {
        _registerUiState.update { it.copy(latitud = lat, longitud = lng) }
    }

    fun registrar() {
        val state = _registerUiState.value

        if (state.nombres.isBlank()) {
            _registerUiState.update { it.copy(errorMessage = "Ingrese sus nombres") }
            return
        }
        if (state.correo.isBlank()) {
            _registerUiState.update { it.copy(errorMessage = "Ingrese su correo electrónico") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.correo).matches()) {
            _registerUiState.update { it.copy(errorMessage = "Formato de correo inválido") }
            return
        }
        if (state.usuario.isBlank()) {
            _registerUiState.update { it.copy(errorMessage = "Ingrese un nombre de usuario") }
            return
        }
        if (state.contrasena.isBlank()) {
            _registerUiState.update { it.copy(errorMessage = "Ingrese una contraseña") }
            return
        }
        if (state.contrasena != state.confirmarContrasena) {
            _registerUiState.update { it.copy(errorMessage = "Las contraseñas no coinciden") }
            return
        }
        if (state.telefono.isBlank()) {
            _registerUiState.update { it.copy(errorMessage = "Ingrese su teléfono") }
            return
        }
        if (state.latitud == null || state.longitud == null) {
            _registerUiState.update { it.copy(errorMessage = "No se pudo capturar la ubicación") }
            return
        }

        _registerUiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val existeUsuario = repository.existeUsuario(state.usuario.trim())
            if (existeUsuario) {
                _registerUiState.update {
                    it.copy(isLoading = false, errorMessage = "El usuario ya existe")
                }
                return@launch
            }

            val existeCorreo = repository.existeCorreo(state.correo.trim())
            if (existeCorreo) {
                _registerUiState.update {
                    it.copy(isLoading = false, errorMessage = "El correo ya está registrado")
                }
                return@launch
            }

            val usuario = UsuarioEntity(
                nombres = state.nombres.trim(),
                correo = state.correo.trim(),
                usuario = state.usuario.trim(),
                contrasena = state.contrasena,
                telefono = state.telefono.trim(),
                fechaRegistro = System.currentTimeMillis(),
                latitud = state.latitud!!,
                longitud = state.longitud!!
            )

            val result = repository.registrar(usuario)
            result.fold(
                onSuccess = {
                    _registerUiState.update {
                        it.copy(isLoading = false, registroExitoso = true)
                    }
                },
                onFailure = { error ->
                    _registerUiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Error al registrar")
                    }
                }
            )
        }
    }

    fun navigateToWelcome() {
        _authState.update { it.copy(currentScreen = Screen.WELCOME) }
    }

    fun navigateToRegister() {
        _registerUiState.update { RegisterUiState() }
        _authState.update { it.copy(currentScreen = Screen.REGISTER) }
    }

    fun navigateToLogin() {
        _loginUiState.update { LoginUiState() }
        _registerUiState.update { RegisterUiState() }
        _authState.update { it.copy(currentUser = null, currentScreen = Screen.LOGIN) }
    }

    fun navigateToHome() {
        _authState.update { it.copy(currentScreen = Screen.HOME) }
    }

    fun logout() {
        _loginUiState.update { LoginUiState() }
        _authState.update { AuthState() }
    }

    fun clearLoginError() {
        _loginUiState.update { it.copy(errorMessage = null) }
    }
}
