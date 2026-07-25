# Plan de Implementación — OrderManager: Roles, Backend Supabase, Dashboard y API Externa

## 0. Resumen y decisiones confirmadas

| Decisión | Elección |
|---|---|
| Sincronización de pedidos | **Supabase Realtime** (websockets, instantáneo) |
| Gestión del menú | **Dinámico**: admin puede editar disponibilidad, agregar, eliminar y editar platos |
| Seguridad API externa | **API key fija** (simple, se puede evolucionar después a tokens por integración) |

**Supuesto a confirmar:** interpreto que el rol **admin** tiene acceso a *todas* las pantallas/dashboards **excepto** la pantalla de "pedidos entrantes" (exclusiva de chef), y que **además** tiene la capacidad de crear usuarios y asignarles rol/permisos (pantalla nueva de gestión de usuarios). Si la intención era otra (ej. admin sin poder crear usuarios), dímelo y ajusto la matriz de permisos.

---

## 1. Arquitectura de Roles (RBAC)

### 1.1 Matriz de permisos por pantalla

| Pantalla | Admin | Chef | Mesero |
|---|:---:|:---:|:---:|
| Splash / Welcome / Login / Register | ✅ (pre-auth) | ✅ (pre-auth) | ✅ (pre-auth) |
| MainHub (adaptado por rol) | ✅ | ✅ | ✅ |
| **PendingOrders** (pedidos entrantes) | ❌ | ✅ | ❌ |
| **CrearPedido / MenuSeleccion** (nueva) | ❌ | ❌ | ✅ |
| Balances (dashboard) | ✅ | ❌ | ❌ |
| Historial | ✅ | ✅ | ✅ (solo sus propios pedidos) |
| Perfil | ✅ (propio + gestión) | ✅ (propio, logout) | ✅ (propio, logout) |
| **GestionUsuarios** (nueva) | ✅ | ❌ | ❌ |
| **GestionMenu** (nueva, CRUD platos) | ✅ | ❌ | ❌ |

### 1.2 Modelo de datos de roles (capa app)

```kotlin
enum class UserRole { ADMIN, CHEF, MESERO }

data class AuthState(
    val userId: String? = null,
    val role: UserRole? = null,
    val nombres: String = "",
    val isLoggedIn: Boolean = false
)
```

### 1.3 Cambios en `AuthViewModel`
- Agregar `role: UserRole` al `UsuarioEntity` / modelo de sesión.
- Nueva función `hasAccess(route: String): Boolean` que consulta una tabla de permisos por rol (puede vivir en un `RolePermissions.kt` como mapa estático `Map<UserRole, Set<String>>`).
- El login ya no solo valida credenciales: **recupera el rol desde Supabase** (tabla `usuarios`, columna `rol`) y lo guarda en `authState`.

### 1.4 Navegación condicionada por rol
- `NavRoutes.kt`: agregar `GESTION_USUARIOS`, `GESTION_MENU`, `CREAR_PEDIDO`.
- `MainHubScreen`: el contenido del hub cambia según `authState.role` (tarjetas/accesos distintos).
- `BottomNavBar.kt`: dejar de ser fijo; recibir `items: List<BottomNavItem>` calculados según rol:
  - **Admin:** Balances, GestionMenu, GestionUsuarios, Historial, Perfil
  - **Chef:** Pedidos, Historial, Perfil
  - **Mesero:** CrearPedido (Menú), Historial, Perfil
- Guard a nivel de `NavHost`: si un usuario navega (deep link o back stack) a una ruta no permitida para su rol, redirigir a su pantalla home por defecto.

---

## 2. Nuevas Pantallas

### 2.1 `MenuSeleccionScreen` (mesero) — pantalla de creación de pedido
**Ruta:** `crearPedido`
**Archivo:** `ui/screens/MenuSeleccionScreen.kt`

Funcionalidad:
- Grid de categorías (chips horizontales, estilo `Shapes.badge`) usando `MenuImages.kt` existente.
- Grid/lista de `MenuItemCard` (nuevo componente, reutiliza estilo de `OrderCard`) con imagen mock, nombre, precio, botón +/- cantidad. Solo se muestran ítems con `disponible = true`.
- Carrito flotante (bottom sheet o barra inferior) con resumen: total de ítems, total $, botón `PrimaryButton("Enviar pedido")`.
- Modal de confirmación (`ConfirmModal` existente) antes de enviar.
- Campo opcional de notas generales del pedido (`AppTextField`).
- Al confirmar: crea `Pedido` con `mesero_id = usuario actual`, `estado = "pendiente"`, `origen = "app_mesero"`, inserta en Supabase → dispara evento Realtime que el chef recibe en `PendingOrdersScreen`.
- Feedback visual: `NewOrderToast` reutilizado ("Pedido enviado a cocina") + animación de vaciado del carrito.

**Nuevo ViewModel:** `MenuViewModel.kt`
- `menuState: StateFlow<MenuUiState>` (categorías, items disponibles, carrito actual)
- Funciones: `loadMenu()`, `addToCart(itemId)`, `removeFromCart(itemId)`, `updateNota(texto)`, `enviarPedido()`, `limpiarCarrito()`

### 2.2 `GestionMenuScreen` (admin) — CRUD de platos
**Ruta:** `gestionMenu`
**Archivo:** `ui/screens/GestionMenuScreen.kt`

- Lista de platos existentes (reutiliza `MenuItemCard` en modo compacto) con switch de disponibilidad inline.
- FAB `+` abre `ConfirmModal`-style form (o pantalla secundaria) para crear/editar plato: nombre, descripción, precio, categoría (dropdown), selector de imagen (de las imágenes mock existentes en `MenuImages.kt`; ver sección 5.3 sobre imágenes nuevas).
- Eliminar plato: soft-delete (`activo = false`) para no romper el historial de pedidos que ya lo referencian.
- Usa `MenuViewModel` (mismo ViewModel, distinta responsabilidad: `crearItem()`, `editarItem()`, `toggleDisponibilidad()`, `eliminarItem()`).

### 2.3 `GestionUsuariosScreen` (admin) — creación de usuarios y permisos
**Ruta:** `gestionUsuarios`
**Archivo:** `ui/screens/GestionUsuariosScreen.kt`

- Lista de usuarios (nombre, rol como badge de color, estado activo/inactivo).
- FAB `+` → formulario: nombres, correo, usuario, teléfono, **selector de rol** (admin/chef/mesero), contraseña temporal.
- Acción de editar rol o desactivar usuario (no eliminar, para preservar integridad referencial con `pedidos`/`historial`).
- Nuevo ViewModel: `UserManagementViewModel.kt` → `crearUsuario()`, `cambiarRol()`, `desactivarUsuario()`, `listarUsuarios()`.

### 2.4 Actualización de `BalancesScreen` — filtros y gráficos de rendimiento
Ver sección 4 completa.

---

## 3. Modelo Relacional de Base de Datos (Supabase / PostgreSQL)

### 3.1 Diagrama de relaciones (descripción)

```
roles ──< usuarios >── pedidos ──< pedido_items >── items_menu >── categorias_menu
                          │
                          └──< transacciones
usuarios ──< items_menu (creado_por)
usuarios ──< api_keys (creado_por)
usuarios ──< audit_log
```

### 3.2 Tablas

**`roles`**
| Columna | Tipo | Notas |
|---|---|---|
| id | smallint PK | 1=admin, 2=chef, 3=mesero |
| nombre | text unique | 'admin' \| 'chef' \| 'mesero' |

**`usuarios`** (reemplaza `UsuarioEntity`, vinculada a `auth.users` de Supabase)
| Columna | Tipo | Notas |
|---|---|---|
| id | uuid PK | = `auth.users.id` (Supabase Auth maneja el hash de contraseña) |
| nombres | text | |
| correo | text unique | |
| usuario | text unique | |
| telefono | text | |
| rol_id | smallint FK → roles.id | |
| activo | boolean default true | soft-disable |
| latitud, longitud | double precision | ya existente |
| fecha_registro | timestamptz default now() | |
| creado_por | uuid FK → usuarios.id nullable | admin que lo creó |

**`categorias_menu`**
| Columna | Tipo |
|---|---|
| id | serial PK |
| nombre | text |
| orden | int |

**`items_menu`**
| Columna | Tipo | Notas |
|---|---|---|
| id | serial PK | |
| nombre | text | |
| descripcion | text | |
| precio | numeric(10,2) | |
| categoria_id | int FK → categorias_menu.id | |
| imagen_ref | text | nombre del recurso local (`MenuImages`) o URL de Storage |
| disponible | boolean default true | toggle rápido del admin |
| activo | boolean default true | soft-delete |
| creado_por | uuid FK → usuarios.id | |
| fecha_creacion, fecha_actualizacion | timestamptz | |

**`pedidos`**
| Columna | Tipo | Notas |
|---|---|---|
| id | uuid PK default gen_random_uuid() | |
| numero_pedido | serial | correlativo legible |
| mesero_id | uuid FK → usuarios.id nullable | null si viene de API externa |
| chef_id | uuid FK → usuarios.id nullable | quien lo confirma/completa |
| cliente | text | |
| mesa_o_direccion | text | |
| estado | text check in ('pendiente','confirmado','en_preparacion','completado','cancelado') | |
| origen | text check in ('app_mesero','api_externa') | |
| total | numeric(10,2) | |
| notas | text | |
| timestamp_creacion | timestamptz default now() | |
| timestamp_confirmacion | timestamptz nullable | |
| timestamp_completado | timestamptz nullable | |

**`pedido_items`**
| Columna | Tipo | Notas |
|---|---|---|
| id | serial PK | |
| pedido_id | uuid FK → pedidos.id | |
| item_menu_id | int FK → items_menu.id nullable | nullable por si el ítem se borra después |
| nombre_item_snapshot | text | copia del nombre al momento del pedido (histórico) |
| cantidad | int | |
| precio_unitario_snapshot | numeric(10,2) | |
| subtotal | numeric(10,2) | |
| notas | text | |

**`transacciones`** (reemplaza los `mockSummaries`/`mockTransactions` del dashboard)
| Columna | Tipo | Notas |
|---|---|---|
| id | uuid PK | |
| pedido_id | uuid FK → pedidos.id | |
| monto | numeric(10,2) | |
| metodo_pago | text | |
| fecha | timestamptz default now() | |

**`api_keys`** (para el sistema de mensajería externo)
| Columna | Tipo |
|---|---|
| id | uuid PK |
| nombre_integracion | text |
| api_key_hash | text (hash, nunca texto plano) |
| activo | boolean default true |
| creado_por | uuid FK → usuarios.id |
| fecha_creacion, ultima_uso | timestamptz |

**`audit_log`** (opcional, recomendado para trazabilidad de acciones de admin)
| Columna | Tipo |
|---|---|
| id | serial PK |
| usuario_id | uuid FK |
| accion | text |
| entidad, entidad_id | text |
| timestamp | timestamptz default now() |

### 3.3 Row Level Security (RLS) — reglas clave
- `pedidos`: `INSERT` permitido a rol `mesero` (su propio `mesero_id`) y a la Edge Function de API externa (usando `service_role` internamente, nunca expuesta al cliente). `UPDATE` (cambiar estado) permitido solo a `chef`. `SELECT` según rol: chef ve todos los activos, mesero ve solo los suyos, admin ve historial completo pero no la cola de "pendientes" (filtrado a nivel de query en la UI, no necesariamente RLS).
- `items_menu`: `SELECT` abierto a todos los roles autenticados; `INSERT/UPDATE/DELETE` solo `admin`.
- `usuarios`: `SELECT` propio para todos; gestión completa (`INSERT` de nuevos usuarios, cambio de `rol_id`) solo `admin`.
- `transacciones`: `SELECT` solo `admin`.

---

## 4. Dashboard de Balances — Filtros y Gráficos

### 4.1 Nuevos filtros (barra superior o drawer de filtros)
- Rango de fechas (date range picker) — desde/hasta.
- Selector de mesero (multi-select).
- Selector de chef (multi-select).
- Selector de categoría/plato.
- Selector de estado de pedido.

Componente nuevo: `FilterBar.kt` en `ui/components/`, con chips de `AppTextField`/dropdowns siguiendo `DesignSystem.kt` (spacing, radius existentes).

### 4.2 Nuevas visualizaciones (usando datos de `historial`/`transacciones`)
1. **Ventas por período** — gráfico de líneas o barras (día/semana/mes según filtro).
2. **Top 5 platos más vendidos** — barras horizontales.
3. **Pedidos por mesero** — barras, útil para medir carga de trabajo.
4. **Tiempo promedio de preparación por chef** — calculado como `timestamp_completado - timestamp_confirmacion`.
5. **Distribución de pedidos por estado** — gráfico de dona.

### 4.3 Librería recomendada
Agregar **Vico** (`com.patrykandpatrick.vico`) a `libs.versions.toml` — es la opción más idiomática para gráficos nativos en Jetpack Compose, se integra bien con `Color.kt` (paleta rojo/crema existente) sin depender de WebView.

### 4.4 Nuevo ViewModel: `BalancesViewModel.kt`
- `filtrosState: StateFlow<FiltrosDashboard>`
- `chartDataState: StateFlow<ChartData>` (agregaciones ya calculadas para consumo directo de Vico)
- Funciones: `aplicarFiltros()`, `resetFiltros()`, `exportarResumen()` (opcional, futuro)
- Las agregaciones pesadas (sumas, promedios) se recomiendan como **vistas SQL en Supabase** (`vista_ventas_por_dia`, `vista_top_platos`, etc.) en lugar de calcularlas en el cliente, para escalar mejor.

---

## 5. Plan de Backend con Supabase

### Fase 1 — Fundaciones
1. Crear proyecto Supabase, definir variables de entorno (`SUPABASE_URL`, `SUPABASE_ANON_KEY`) — nunca hardcodeadas en el repo.
2. Agregar dependencia `supabase-kt` (BOM oficial de Supabase para Kotlin) con módulos: `Postgrest`, `Auth` (GoTrue), `Realtime`. Reemplaza el plan original de Retrofit vacío (`ApiService.kt` puede quedar deprecado o usarse solo si se necesita un endpoint REST fuera de Supabase).
3. Crear las tablas de la sección 3, con RLS activado desde el día uno.
4. Migrar autenticación: **Supabase Auth** resuelve de forma nativa el punto #12 de brechas identificadas ("cifrado de contraseñas en texto plano") — las contraseñas nunca se manejan en texto plano ni en la app ni en la base de datos.
5. Poblar tabla `roles` con los 3 valores fijos.

### Fase 2 — Repositorios remotos
1. Reemplazar `BackendRepository.kt` (vacío) por implementaciones reales: `AuthRepository` (Supabase Auth), `PedidoRepository` (Postgrest + Realtime), `MenuRepository`, `UserRepository`, `BalancesRepository`.
2. Mantener **Room como caché local/offline** (patrón "single source of truth" remoto + caché): el repositorio escribe primero a Supabase, y sincroniza a Room para que la app funcione con datos recientes incluso si hay una desconexión momentánea. Esto conserva el valor de la capa Room ya construida en vez de descartarla.

### Fase 3 — Realtime
1. Suscripción del `PendingOrdersScreen` (chef) al canal Realtime de la tabla `pedidos` (evento `INSERT`/`UPDATE`), reemplazando la simulación mock de pedidos cada 10s.
2. `MenuSeleccionScreen` (mesero) hace `INSERT` en `pedidos` + `pedido_items` vía Postgrest; el evento Realtime llega automáticamente al chef sin código adicional de "notificación".
3. Manejo de reconexión/backoff del canal Realtime (Supabase SDK lo soporta, pero hay que testear cortes de red).

### Fase 4 — Imágenes del menú
- Los platos existentes en el mock **mantienen sus imágenes locales** (`MenuImages.kt`, `drawable/`) para no romper la consistencia visual actual — la columna `imagen_ref` guarda el identificador del recurso local.
- Para **nuevos** platos que el admin agregue desde `GestionMenuScreen`, dos opciones (a decidir): (a) limitar la creación a seleccionar entre las imágenes mock ya disponibles, o (b) habilitar subida a **Supabase Storage** (bucket `menu-images`). Recomendado empezar con (a) por simplicidad y evolucionar a (b) si el negocio lo requiere.

### Fase 5 — Edge Function para pedidos externos
Ver sección 6.

---

## 6. Documentación de API — Pedido vía sistema de mensajería externo

### 6.1 Resumen
Un servicio externo (sistema de mensajería) envía un **JSON con un pedido** vía HTTP POST. Se implementa como **Supabase Edge Function** (Deno), que valida la API key, inserta el pedido en las tablas `pedidos`/`pedido_items` usando el `service_role` internamente (nunca expuesto al cliente externo), y el evento llega al chef vía Realtime igual que un pedido de mesero.

### 6.2 Endpoint

```
POST https://<PROJECT_REF>.supabase.co/functions/v1/crear-pedido-externo
```

### 6.3 Headers

| Header | Valor | Obligatorio |
|---|---|---|
| `Content-Type` | `application/json` | Sí |
| `x-api-key` | API key fija asignada a la integración (tabla `api_keys`) | Sí |

### 6.4 Body (request)

```json
{
  "cliente": "Juan Pérez",
  "mesa_o_direccion": "Mesa 5",
  "notas": "Sin cebolla en el segundo plato",
  "productos": [
    {
      "item_menu_id": 12,
      "nombre_fallback": "Hamburguesa clásica",
      "cantidad": 2,
      "notas": "Término medio"
    },
    {
      "item_menu_id": 7,
      "nombre_fallback": "Papas fritas",
      "cantidad": 1,
      "notas": null
    }
  ]
}
```

**Notas sobre el schema:**
- `item_menu_id`: opcional pero recomendado, referencia directa a `items_menu.id`. Si no se envía o no existe, el sistema usa `nombre_fallback` como texto libre y `precio_unitario_snapshot = 0` (o un valor configurable), marcando el pedido para revisión manual.
- `productos` no puede ser un array vacío (validación 400).
- El campo `origen` se fija automáticamente a `"api_externa"` — no se envía en el request.

### 6.5 Respuesta exitosa (201)

```json
{
  "pedido_id": "3f1a9c2e-...-uuid",
  "numero_pedido": 1042,
  "estado": "pendiente",
  "total": 27.50,
  "timestamp_creacion": "2026-07-23T15:04:00Z"
}
```

### 6.6 Errores

| Código | Causa | Body ejemplo |
|---|---|---|
| 401 | API key inválida o inactiva | `{ "error": "invalid_api_key" }` |
| 400 | JSON malformado o `productos` vacío | `{ "error": "validation_error", "detalle": "productos no puede estar vacío" }` |
| 404 | `item_menu_id` no encontrado (si se exige estricto) | `{ "error": "item_no_encontrado", "item_menu_id": 12 }` |
| 500 | Error interno (fallo de inserción) | `{ "error": "internal_error" }` |

### 6.7 Seguridad
- La API key se almacena **hasheada** en `api_keys.api_key_hash` (nunca en texto plano en la base de datos).
- Rate limiting básico recomendado a nivel de Edge Function (ej. 60 req/min por key) para evitar abuso.
- Migración futura sugerida (no bloqueante para v1): pasar de API key fija a un token rotable por integración, ya documentado como posible siguiente paso.

---

## 7. Consistencia de Diseño UI

Todas las pantallas nuevas deben reutilizar, sin crear estilos paralelos:
- **Paleta:** rojo vibrante `#E11D48` como primary, fondo crema/vainilla, naranja para warning, verde para success (`Color.kt`).
- **Componentes existentes:** `AppTextField`, `PrimaryButton`/`OutlinedPrimaryButton`, `OrderCard` (como base para `MenuItemCard`), `ScreenScaffold` (estructura de toda pantalla nueva), `ConfirmModal` (confirmaciones de envío de pedido, eliminación de plato/usuario), `NewOrderToast`, `LoadingIndicator`/`EmptyState`/`ErrorState`.
- **Shapes/Spacing/Radius:** usar `Shapes.kt` y `DesignSystem.kt` existentes — no definir nuevos radios o espaciados ad hoc.
- **Animaciones:** `Animation.kt` (`AnimSpecs`) para transiciones de las nuevas pantallas, manteniendo el mismo `slideInHorizontally + fadeIn` ya usado entre pantallas.
- **Imágenes del menú:** se reutiliza `MenuImages.kt` tal cual existe hoy, aplicado ahora al flujo real de creación de pedidos (no solo mock visual).

---

## 8. Roadmap sugerido (orden de implementación)

| Fase | Entregable | Depende de |
|---|---|---|
| 1 | Supabase: proyecto, tablas, RLS, Auth, roles | — |
| 2 | Migración de login/registro a Supabase Auth + campo rol en sesión | Fase 1 |
| 3 | Navegación condicionada por rol + BottomNavBar dinámico | Fase 2 |
| 4 | `MenuSeleccionScreen` (mesero) + `MenuViewModel` + inserción de pedidos | Fase 1-3 |
| 5 | `PendingOrdersScreen` conectado a Realtime (reemplaza mock 10s) | Fase 4 |
| 6 | `GestionMenuScreen` (admin, CRUD platos) | Fase 1, 3 |
| 7 | `GestionUsuariosScreen` (admin, crear usuarios/roles) | Fase 1, 3 |
| 8 | `BalancesScreen` con filtros + gráficos (Vico) + vistas SQL agregadas | Fase 1, datos históricos reales |
| 9 | Edge Function `crear-pedido-externo` + tabla `api_keys` + documentación entregada al equipo externo | Fase 1, 4-5 |
| 10 | Hardening: revisión de RLS, rate limiting API externa, pruebas de reconexión Realtime | Todas |

---

## 9. Preguntas abiertas para afinar el plan antes de construir

1. **Historial por rol:** ¿el mesero debe ver *todos* los pedidos en el historial o solo los que él creó? (asumí "solo los suyos" en la matriz de permisos — confírmalo).
2. **Balances del admin:** ¿necesita ver también el estado de los pedidos activos (aunque no la cola operativa de "pedidos entrantes"), por ejemplo como métrica en el dashboard, o queda totalmente fuera de su alcance?
3. **Ítems sin `item_menu_id` en la API externa:** cuando el sistema de mensajería envía un producto que no existe en el menú (`nombre_fallback`), ¿el pedido debe bloquearse hasta revisión manual del chef, o entrar igual a la cola con precio $0 para ajuste posterior?
4. **Multi-restaurante/sucursal:** ¿esta base de datos es para una sola ubicación o hay que prever `sucursal_id` desde ahora para evitar una migración dolorosa después?
5. **Offline del mesero/chef:** ¿es aceptable que la app dependa de conexión a internet en tiempo real (Supabase), o necesitas que Room siga funcionando como cola offline si se cae la red (ej. mesero toma pedido sin señal y se sincroniza después)?

Con tus respuestas a estas 5 preguntas puedo ajustar el modelo de datos y las prioridades del roadmap antes de pasar a la implementación de código.
