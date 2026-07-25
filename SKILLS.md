# OrderManager — Skills para el agente de UI/Frontend

> Este archivo define las reglas, patrones y tecnologías que el agente debe seguir al codificar nuevas pantallas o modificar las existentes en el frontend Android (Jetpack Compube).

---

## Skill 01: Screen Structure

Toda pantalla (`Screen`) debe seguir esta estructura:

```kotlin
@Composable
fun NombreScreen(
    viewModel: NombreViewModel,
    onNavigateToX: () -> Unit,
    onNavigateToY: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // UI here usando ScreenScaffold o Column directo
}
```

Reglas:
- Los callbacks de navegación siempre son lambdas `() -> Unit`
- El ViewModel nunca se crea dentro del Screen, se recibe por parámetro
- Usar `collectAsStateWithLifecycle()` para observar estados
- No usar `AndroidViewModel` directamente en screens

---

## Skill 02: State Handling

Usar **sealed interfaces** para representar estados de UI:

```kotlin
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val retry: (() -> Unit)? = null) : UiState<Nothing>
    data object Empty : UiState<Nothing>
}
```

Patrón de uso en composable:

```kotlin
when (uiState) {
    is UiState.Loading -> LoadingIndicator()
    is UiState.Empty -> EmptyState(title = "...", subtitle = "...")
    is UiState.Error -> ErrorState(message = uiState.message, onRetry = { viewModel.retry() })
    is UiState.Success -> ContentView(data = uiState.data)
}
```

Para formularios, usar un estado `Form` sellado dentro del sealed interface:

```kotlin
sealed interface LoginState {
    data object Idle : LoginState
    data class Form(val username: String = "", ...) : LoginState
    data object Loading : LoginState
    data class Error(val message: String) : LoginState
}
```

---

## Skill 03: Navigation & Motion

### Navegación entre screens
- Todas las rutas en `NavHost` deben tener `enterTransition`, `exitTransition`, `popEnterTransition` y `popExitTransition`
- Usar `slideInHorizontally { it / 4 } + fadeIn()` para forward
- Usar `slideInHorizontally { -it / 4 } + fadeIn()` para pop (back)

### Cambio de tabs (BottomNav)
- Usar `AnimatedContent` con `fadeIn() togetherWith fadeOut()`
- El estado del tab activo se maneja con `mutableStateOf`

### Micro-interacciones
- Botones: escala 0.97→1.0 con `spring(dampingRatio = 0.5f)`
- Cards en listas: `AnimatedVisibility` con fadeIn + slideInVertically al entrar
- Modales/Dialogs: fadeIn + scaleIn

---

## Skill 04: Design Tokens

Usar exclusivamente estos tokens. **PROHIBIDO** usar valores quemados.

### Colors
```kotlin
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.onPrimary
MaterialTheme.colorScheme.background
MaterialTheme.colorScheme.surface
MaterialTheme.colorScheme.onSurface
MaterialTheme.colorScheme.onSurfaceVariant
MaterialTheme.colorScheme.outline
MaterialTheme.colorScheme.error
// etc.
```

### Spacing
```kotlin
Spacing.xs   // 4.dp
Spacing.sm   // 8.dp
Spacing.md   // 12.dp
Spacing.lg   // 16.dp
Spacing.xl   // 20.dp
Spacing.twoXl   // 24.dp
Spacing.threeXl // 32.dp
Spacing.fourXl  // 40.dp
Spacing.fiveXl  // 48.dp
```

### Shapes
```kotlin
Shapes.card   // RoundedCornerShape(16.dp) — Cards, surfaces
Shapes.input  // RoundedCornerShape(16.dp) — TextFields
Shapes.button // RoundedCornerShape(12.dp) — Buttons
Shapes.badge  // RoundedCornerShape(8.dp)  — Time badges, tags
Shapes.modal  // RoundedCornerShape(20.dp) — Dialogs
Shapes.avatar // RoundedCornerShape(9999.dp) — Circular avatars
```

### Typography
```kotlin
MaterialTheme.typography.displayLarge   // 28sp ExtraBold
MaterialTheme.typography.displayMedium  // 22sp Bold
MaterialTheme.typography.titleLarge     // 18sp Bold
MaterialTheme.typography.titleMedium    // 16sp SemiBold
MaterialTheme.typography.titleSmall     // 14sp SemiBold
MaterialTheme.typography.bodyLarge      // 16sp Normal
MaterialTheme.typography.bodyMedium     // 14sp Normal
MaterialTheme.typography.bodySmall      // 12sp Normal
MaterialTheme.typography.labelLarge     // 13sp Medium
MaterialTheme.typography.labelSmall     // 11sp Medium
```

### Animation Specs
```kotlin
AnimSpecs.default // tween(300)
AnimSpecs.slow    // tween(600, FastOutSlowInEasing)
AnimSpecs.spring  // spring(dampingRatio = 0.7f)
```

---

## Skill 05: Component Usage

| Situación | Componente | Archivo |
|-----------|-----------|---------|
| Botón principal | `PrimaryButton` | `ui/components/PrimaryButton.kt` |
| Botón outline | `OutlinedPrimaryButton` | `ui/components/PrimaryButton.kt` |
| Campo de texto | `AppTextField` | `ui/components/AppTextField.kt` |
| Loading spinner | `LoadingIndicator` | `ui/components/FeedbackComponents.kt` |
| Estado vacío | `EmptyState` | `ui/components/FeedbackComponents.kt` |
| Estado error | `ErrorState` | `ui/components/FeedbackComponents.kt` |
| Scaffold base | `ScreenScaffold` | `ui/components/ScreenScaffold.kt` |
| Card de pedido | `OrderCard` | `ui/components/OrderCard.kt` |
| Modal confirmación | `ConfirmModal` | `ui/components/ConfirmModal.kt` |
| Toast nuevo pedido | `NewOrderToast` | `ui/components/NewOrderToast.kt` |

---

## Skill 06: Form Validation

Patrón para formularios:

```kotlin
fun submit() {
    val state = _uiState.value // must be Form
    if (state.campo.isBlank()) {
        _uiState.update { it.copy(errorMessage = "Campo requerido") }
        return
    }
    // validar más campos...
    _uiState.update { Loading }
    viewModelScope.launch {
        // llamada async
    }
}
```

- Los errores se muestran con un `Text` debajo del campo o en el `supportingText` de `AppTextField`
- Usar `Patterns.EMAIL_ADDRESS` para validar correos
- El `errorMessage` se limpia cuando el usuario empieza a escribir en cualquier campo

---

## Skill 07: List Patterns

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(vertical = Spacing.sm)
) {
    items(
        items = list,
        key = { it.id }
    ) { item ->
        ItemComposable(item = item)
    }
}
```

Reglas:
- Siempre usar `key` con un identificador único
- `contentPadding` inferior para evitar que el último item quede pegado al borde
- Cada item debe ser un composable independiente (no inline)
- Items deben usar `AnimatedVisibility` para entrada animada

---

## Skill 08: Dark Mode

- Nunca usar colores hardcodeados (ej. `Color(0xFF...)` o `Color.White`)
- Siempre usar `MaterialTheme.colorScheme.*`
- El `DarkColorScheme` en `Theme.kt` ya tiene todos los roles mapeados
- Probar siempre la pantalla en modo oscuro antes de darla por terminada
- No usar `isSystemInDarkTheme()` directamente en componentes; el tema se aplica desde `OrderManagerTheme`
