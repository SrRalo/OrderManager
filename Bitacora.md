# Bitácora de Implementación

## Sesión 1 — Implementación Base del Mini Proyecto Escolar

### Fecha
07/06/2026

### Objetivo
Implementar los fundamentos de una aplicación Android con persistencia local (Room Database) que permita registro e inicio de sesión de usuarios con captura de ubicación geográfica.

---

### Cambios Realizados

#### 1. Configuración de Dependencias

**Archivo:** `gradle/libs.versions.toml`
- Se agregaron las versiones para Room (`room = "2.7.1"`), KSP (`ksp = "2.2.10-1.0.32"`), Play Services Location (`playServicesLocation = "21.3.0"`) y ViewModel Compose (`lifecycleViewmodelCompose = "2.10.0"`).
- Se registraron los libraries correspondientes: `androidx-room-runtime`, `androidx-room-ktx`, `androidx-room-compiler`, `play-services-location`, `androidx-lifecycle-viewmodel-compose`.
- Se agregó el plugin `ksp` para el procesamiento de anotaciones de Room.

**Archivo:** `build.gradle.kts` (raíz)
- Se aplicó el plugin `ksp` con `apply false`.

**Archivo:** `app/build.gradle.kts`
- Se aplicó el plugin `com.google.devtools.ksp`.
- Se agregaron dependencias de Room (`room-runtime`, `room-ktx`, `room-compiler` con KSP), Location (`play-services-location`) y ViewModel Compose.

#### 2. Permisos de Ubicación

**Archivo:** `AndroidManifest.xml`
- Se agregaron los permisos `ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION` para la captura de ubicación.

#### 3. Capa de Persistencia (Room)

**Archivo:** `data/local/entity/UsuarioEntity.kt`
- Se creó la entidad `UsuarioEntity` con los campos:
  - `id` (Long, PrimaryKey autoGenerate)
  - `nombres` (String)
  - `correo` (String, único)
  - `usuario` (String, único)
  - `contrasena` (String)
  - `telefono` (String)
  - `fechaRegistro` (Long — timestamp)
  - `latitud` (Double)
  - `longitud` (Double)
- Índices únicos en `correo` y `usuario`.

**Archivo:** `data/local/dao/UsuarioDao.kt`
- `insertar()` — inserción con `OnConflictStrategy.ABORT`.
- `obtenerPorUsuarioOCorreo()` — consulta por username o correo.
- `validarCredenciales()` — consulta por username/correo + contraseña.
- `obtenerPorUsuario()` / `obtenerPorCorreo()` — validaciones de unicidad.
- `obtenerTodos()` — Flow reactivo para listar todos.

**Archivo:** `data/local/AppDatabase.kt`
- Clase abstracta `AppDatabase` con Room database builder singleton.
- Versión 1, entidad `UsuarioEntity`.

#### 4. Capa de Repositorio

**Archivo:** `data/repository/UsuarioRepository.kt`
- `registrar()` — inserta usuario, retorna `Result<Long>`.
- `iniciarSesion()` — valida credenciales, retorna `Result<UsuarioEntity>` con mensajes de error diferenciados (`usuario_no_existe`, `contrasena_incorrecta`).
- `existeUsuario()` / `existeCorreo()` — validaciones de unicidad.

#### 5. ViewModel

**Archivo:** `ui/viewmodel/AuthViewModel.kt`
- `AuthViewModel` extiende `AndroidViewModel` para acceder al Application Context.
- `AuthState` — contiene `currentUser` (UsuarioEntity?) y `currentScreen` (Screen: LOGIN | HOME | REGISTER).
- `LoginUiState` — estado del formulario de login (username, password, error, loading).
- `RegisterUiState` — estado del formulario de registro (todos los campos, latitud, longitud, error, loading, registroExitoso).
- Funciones de acción: `login()`, `registrar()`, `logout()`.
- Funciones de navegación: `navigateToRegister()`, `navigateToLogin()`, `navigateToHome()`.
- Toda la lógica de Room se ejecuta en corrutinas (`viewModelScope.launch`).

#### 6. Pantallas

**Archivo:** `ui/screens/LoginScreen.kt`
- Campos: Usuario/Correo y Contraseña.
- Botón "Ingresar" con loading state.
- Validaciones: campos vacíos → mensaje específico, usuario no existe → "El usuario no existe", contraseña incorrecta → "Contraseña incorrecta".
- Enlace "Regístrate" → navega a RegisterScreen.
- Consume `LoginUiState` del ViewModel.

**Archivo:** `ui/screens/RegisterScreen.kt`
- Formulario completo: Nombres, Correo, Usuario, Contraseña, Confirmar Contraseña, Teléfono.
- Captura de ubicación con `FusedLocationProviderClient`:
  - Al cargar la pantalla, verifica permisos y captura ubicación automáticamente.
  - Si no hay permisos, lanza `ActivityResultContracts.RequestMultiplePermissions`.
  - Si no se ha capturado ubicación al presionar "Guardar", intenta capturar de nuevo.
- Validaciones: campos vacíos, formato de correo, contraseñas coincidentes, unicidad de usuario/correo, ubicación capturada.
- Al registrarse exitosamente: Toast + navegación a Login.
- Botón de retroceso en TopAppBar.

**Archivo:** `ui/screens/HomeScreen.kt`
- Mensaje de bienvenida personalizado: "¡Bienvenido, [Nombres o Usuario]!".
- Botón "Registrar Nuevo Usuario" → navega a RegisterScreen.
- Botón "Cerrar Sesión" → limpia estado y regresa a LoginScreen.
- Dark mode toggle conservado.

#### 7. Navegación

**Archivo:** `MainActivity.kt`
- Se instancia `AuthViewModel` con `by viewModels()`.
- `MainApp` usa `Crossfade` animado para transicionar entre LOGIN, HOME y REGISTER según `authState.currentScreen`.
- Se pasa `authViewModel` a todas las pantallas.

---

### Estructura Final del Proyecto

```
app/src/main/java/com/example/ordermanager/
├── MainActivity.kt
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   │   └── UsuarioDao.kt
│   │   └── entity/
│   │       └── UsuarioEntity.kt
│   └── repository/
│       └── UsuarioRepository.kt
└── ui/
    ├── screens/
    │   ├── HomeScreen.kt
    │   ├── LoginScreen.kt
    │   └── RegisterScreen.kt
    ├── theme/
    │   ├── Color.kt
    │   ├── DesignSystem.kt
    │   ├── Theme.kt
    │   └── Type.kt
    └── viewmodel/
        └── AuthViewModel.kt
```

### Próximos Pasos (Opcionales)
- Agregar validación de formato de teléfono.
- Mostrar la fecha de registro formateada en el perfil.
- Implementar edición de perfil de usuario.
- Agregar mapa mostrando la ubicación del registro.
- Implementar cifrado de contraseña (hash) para producción.
