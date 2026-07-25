package com.example.ordermanager.ui.viewmodel

import android.app.Application
import android.util.Patterns
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

sealed interface LoginState {
    data object Idle : LoginState
    data class Form(
        val username: String = "",
        val password: String = "",
        val errorMessage: String? = null
    ) : LoginState
    data object Loading : LoginState
    data class Error(val message: String) : LoginState
}

sealed interface RegisterState {
    data object Idle : RegisterState
    data class Form(
        val nombres: String = "",
        val correo: String = "",
        val usuario: String = "",
        val contrasena: String = "",
        val confirmarContrasena: String = "",
        val telefono: String = "",
        val latitud: Double? = null,
        val longitud: Double? = null,
        val errorMessage: String? = null
    ) : RegisterState
    data object Loading : RegisterState
    data class Error(val message: String) : RegisterState
    data object Success : RegisterState
}

data class AuthState(
    val currentUser: UsuarioEntity? = null
)

enum class UserRole {
    ADMIN,
    CHEF,
    MESERO,
    SUPERVISOR
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UsuarioRepository

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = UsuarioRepository(db.usuarioDao())
    }

    fun updateLoginUsername(value: String) {
        val current = _loginState.value
        if (current is LoginState.Form) {
            _loginState.update { current.copy(username = value, errorMessage = null) }
        } else {
            _loginState.update { LoginState.Form(username = value) }
        }
    }

    fun updateLoginPassword(value: String) {
        val current = _loginState.value
        if (current is LoginState.Form) {
            _loginState.update { current.copy(password = value, errorMessage = null) }
        } else {
            _loginState.update { LoginState.Form(password = value) }
        }
    }

    fun login() {
        val state = _loginState.value
        val form = state as? LoginState.Form ?: return
        val username = form.username
        val password = form.password

        if (username.isBlank()) {
            _loginState.update { LoginState.Form(username = form.username, password = form.password, errorMessage = "Ingrese su correo o usuario") }
            return
        }
        if (password.isBlank()) {
            _loginState.update { LoginState.Form(username = form.username, password = form.password, errorMessage = "Ingrese su contraseña") }
            return
        }

        _loginState.update { LoginState.Loading }

        viewModelScope.launch {
            val result = repository.iniciarSesion(username.trim(), password)
            result.fold(
                onSuccess = { usuario ->
                    _authState.update { it.copy(currentUser = usuario) }
                    _loginState.update { LoginState.Idle }
                },
                onFailure = { error ->
                    val msg = when (error.message) {
                        "usuario_no_existe" -> "El usuario no existe"
                        "contrasena_incorrecta" -> "Contraseña incorrecta"
                        else -> error.message ?: "Error al iniciar sesión"
                    }
                    _loginState.update { LoginState.Error(msg) }
                }
            )
        }
    }

    fun updateRegisterNombres(value: String) {
        val current = _registerState.value
        if (current is RegisterState.Form) {
            _registerState.update { current.copy(nombres = value, errorMessage = null) }
        } else {
            _registerState.update { RegisterState.Form(nombres = value) }
        }
    }

    fun updateRegisterCorreo(value: String) {
        val current = _registerState.value
        if (current is RegisterState.Form) {
            _registerState.update { current.copy(correo = value, errorMessage = null) }
        } else {
            _registerState.update { RegisterState.Form(correo = value) }
        }
    }

    fun updateRegisterUsuario(value: String) {
        val current = _registerState.value
        if (current is RegisterState.Form) {
            _registerState.update { current.copy(usuario = value, errorMessage = null) }
        } else {
            _registerState.update { RegisterState.Form(usuario = value) }
        }
    }

    fun updateRegisterContrasena(value: String) {
        val current = _registerState.value
        if (current is RegisterState.Form) {
            _registerState.update { current.copy(contrasena = value, errorMessage = null) }
        } else {
            _registerState.update { RegisterState.Form(contrasena = value) }
        }
    }

    fun updateRegisterConfirmarContrasena(value: String) {
        val current = _registerState.value
        if (current is RegisterState.Form) {
            _registerState.update { current.copy(confirmarContrasena = value, errorMessage = null) }
        } else {
            _registerState.update { RegisterState.Form(confirmarContrasena = value) }
        }
    }

    fun updateRegisterTelefono(value: String) {
        val current = _registerState.value
        if (current is RegisterState.Form) {
            _registerState.update { current.copy(telefono = value, errorMessage = null) }
        } else {
            _registerState.update { RegisterState.Form(telefono = value) }
        }
    }

    fun updateLocation(lat: Double, lng: Double) {
        val current = _registerState.value
        if (current is RegisterState.Form) {
            _registerState.update { current.copy(latitud = lat, longitud = lng) }
        } else {
            _registerState.update { RegisterState.Form(latitud = lat, longitud = lng) }
        }
    }

    fun registrar() {
        val state = _registerState.value
        if (state !is RegisterState.Form) return

        if (state.nombres.isBlank()) {
            _registerState.update { state.copy(errorMessage = "Ingrese sus nombres") }
            return
        }
        if (state.correo.isBlank()) {
            _registerState.update { state.copy(errorMessage = "Ingrese su correo electrónico") }
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(state.correo).matches()) {
            _registerState.update { state.copy(errorMessage = "Formato de correo inválido") }
            return
        }
        if (state.usuario.isBlank()) {
            _registerState.update { state.copy(errorMessage = "Ingrese un nombre de usuario") }
            return
        }
        if (state.contrasena.isBlank()) {
            _registerState.update { state.copy(errorMessage = "Ingrese una contraseña") }
            return
        }
        if (state.contrasena != state.confirmarContrasena) {
            _registerState.update { state.copy(errorMessage = "Las contraseñas no coinciden") }
            return
        }
        if (state.telefono.isBlank()) {
            _registerState.update { state.copy(errorMessage = "Ingrese su teléfono") }
            return
        }
        if (state.latitud == null || state.longitud == null) {
            _registerState.update { state.copy(errorMessage = "No se pudo capturar la ubicación") }
            return
        }

        _registerState.update { RegisterState.Loading }

        viewModelScope.launch {
            val existeUsuario = repository.existeUsuario(state.usuario.trim())
            if (existeUsuario) {
                _registerState.update { state.copy(errorMessage = "El usuario ya existe") }
                return@launch
            }

            val existeCorreo = repository.existeCorreo(state.correo.trim())
            if (existeCorreo) {
                _registerState.update { state.copy(errorMessage = "El correo ya está registrado") }
                return@launch
            }

            val totalUsuarios = repository.contarUsuarios()
            val rolRegistro = if (totalUsuarios == 0) UserRole.ADMIN else UserRole.MESERO

            val usuario = UsuarioEntity(
                nombres = state.nombres.trim(),
                correo = state.correo.trim(),
                usuario = state.usuario.trim(),
                contrasena = state.contrasena,
                telefono = state.telefono.trim(),
                fechaRegistro = System.currentTimeMillis(),
                latitud = state.latitud!!,
                longitud = state.longitud!!,
                rol = rolRegistro.name.lowercase()
            )

            val result = repository.registrar(usuario)
            result.fold(
                onSuccess = {
                    _registerState.update { RegisterState.Success }
                },
                onFailure = { error ->
                    _registerState.update { state.copy(errorMessage = error.message ?: "Error al registrar") }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { repository.logout() }
        }
        _loginState.update { LoginState.Idle }
        _registerState.update { RegisterState.Idle }
        _authState.update { AuthState() }
    }

    fun currentUserRole(): UserRole {
        val roleValue = _authState.value.currentUser?.rol?.trim().orEmpty().lowercase()
        return when (roleValue) {
            "admin" -> UserRole.ADMIN
            "chef" -> UserRole.CHEF
            "supervisor" -> UserRole.SUPERVISOR
            else -> UserRole.MESERO
        }
    }

    fun canCreateUsers(): Boolean = currentUserRole() == UserRole.ADMIN

    fun canAccessTab(route: String): Boolean {
        val role = currentUserRole()
        return when (role) {
            UserRole.ADMIN -> true
            UserRole.SUPERVISOR -> route != "gestionUsuarios"
            UserRole.CHEF -> route == "pedidos" || route == "historial" || route == "perfil"
            UserRole.MESERO -> route == "crearPedido" || route == "historial" || route == "perfil"
        }
    }

    fun createUserByAdmin(
        nombres: String,
        correo: String,
        usuario: String,
        telefono: String,
        contrasenaTemporal: String,
        rol: UserRole,
        onResult: (Result<Long>) -> Unit
    ) {
        if (!canCreateUsers()) {
            onResult(Result.failure(IllegalStateException("Solo admin puede crear usuarios")))
            return
        }

        if (nombres.isBlank() || correo.isBlank() || usuario.isBlank() || telefono.isBlank() || contrasenaTemporal.isBlank()) {
            onResult(Result.failure(IllegalArgumentException("Completa todos los campos")))
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()) {
            onResult(Result.failure(IllegalArgumentException("Correo inválido")))
            return
        }

        viewModelScope.launch {
            if (repository.existeUsuario(usuario.trim())) {
                onResult(Result.failure(IllegalArgumentException("El usuario ya existe")))
                return@launch
            }

            if (repository.existeCorreo(correo.trim())) {
                onResult(Result.failure(IllegalArgumentException("El correo ya está registrado")))
                return@launch
            }

            val current = _authState.value.currentUser
            val nuevoUsuario = UsuarioEntity(
                nombres = nombres.trim(),
                correo = correo.trim(),
                usuario = usuario.trim(),
                contrasena = contrasenaTemporal,
                telefono = telefono.trim(),
                fechaRegistro = System.currentTimeMillis(),
                latitud = current?.latitud ?: 0.0,
                longitud = current?.longitud ?: 0.0,
                rol = rol.name.lowercase()
            )

            onResult(repository.registrar(nuevoUsuario))
        }
    }
}
