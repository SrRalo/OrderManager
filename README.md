# OrderManager

Aplicación Android moderna para la gestión de pedidos en restaurantes. Diseñada para el personal de cocina (chefs, meseros, supervisores y administradores) con control de acceso basado en roles (RBAC), pedidos en tiempo real y gestión completa del menú.

## Características

- **Autenticación y Roles:** Login con email/usuario y contraseña. 4 roles: Admin, Supervisor, Chef, Mesero
- **Pedidos en Tiempo Real:** Polling cada 5 segundos vía Supabase Realtime; confirmación con countdown de 3 segundos
- **Gestión de Menú:** CRUD completo para artículos del menú con categorías, disponibilidad y soft-delete
- **Dashboard:** Pantalla de balances para Administradores y Supervisores
- **Diseño Modular:** Material Design 3 con tema personalizado (paleta roja vibrante, fondo crema/vainilla)
- **Modo Oscuro:** Toggle entre tema claro y oscuro
- ** Splash Screen:** Animación de bienvenida
- **Ubicación:** Captura de GPS durante el registro de usuario

## Tecnologías

| Categoría | Tecnología |
|-----------|------------|
| Lenguaje | Kotlin 2.2.10 |
| UI | Jetpack Compose + Material Design 3 |
| Navegación | Navigation Compose 2.8.6 |
| Backend | Supabase (Auth + PostgREST + Realtime) |
| Base de datos local | Room 2.7.1 |
| Inyección de dependencias | Hilt (via NetworkModule) |
| Serialización | Kotlinx Serialization 1.9.0 |
| Imágenes | Coil 2.7.0 |
| Animaciones | Lottie Compose 6.6.2 |
| Ubicación | Google Play Services Location 21.3.0 |
| Networking | Ktor Client 3.3.1 |
| Build | Gradle Kotlin DSL + Version Catalog |

## Requisitos Previos

- Android Studio (última versión estable)
- JDK 11+
- Proyecto Supabase (para funcionalidad backend)
- Supabase CLI (para desarrollo local y migraciones)

## Configuración

### 1. Clonar el repositorio

```bash
git clone <repository-url>
cd OrderManager
```

### 2. Configurar credenciales de Supabase

Crear o editar `local.properties` en la raíz del proyecto:

```properties
SUPABASE_URL=https://<tu-project-ref>.supabase.co
SUPABASE_ANON_KEY=<tu-anon-key>
```

> **IMPORTANTE:** `local.properties` está en `.gitignore` — nunca commitear credenciales.

### 3. Configurar el backend de Supabase

1. Crear un proyecto en [supabase.com](https://supabase.com)
2. Ejecutar las migraciones SQL en `supabase/migrations/`
3. Ejecutar el script de seed: `supabase/seed.sql`

### 4. Abrir en Android Studio

Sincronizar Gradle y esperar a que complete la constricción.

## Compilación

```bash
# Build de debug
./gradlew assembleDebug

# Build de release
./gradlew assembleRelease

# Ejecutar tests unitarios
./gradlew test

# Ejecutar tests instrumentados
./gradlew connectedAndroidTest
```

## Estructura del Proyecto

```
OrderManager/
├── app/
│   ├── build.gradle.kts
│   └── src/main/java/com/example/ordermanager/
│       ├── MainActivity.kt
│       ├── backend/                    # Supabase integration
│       │   ├── data/repository/        # Repositorios Supabase
│       │   ├── di/                     # Módulo de red
│       │   └── supabase/              # Cliente Supabase
│       ├── data/
│       │   ├── local/                  # Room DB, DAOs, Entities
│       │   └── repository/            # Repositorios de datos
│       └── ui/
│           ├── components/            # Componentes reutilizables
│           ├── menu/                  # Modelos de menú
│           ├── navigation/            # Navegación y rutas
│           ├── screens/              # Pantallas de la app
│           ├── theme/                # Diseño, colores, tipografía
│           └── viewmodel/            # ViewModels (MVVM)
├── supabase/
│   ├── migrations/                   # Migraciones SQL
│   ├── seed.sql                      # Datos iniciales
│   └── config.toml                   # Config local Supabase
└── docs/                             # Documentación adicional
```

## Credenciales de Prueba

Se crean automáticamente en el primer lanzamiento:

| Email | Usuario | Rol |
|-------|---------|-----|
| admin@test.com | admin | Admin |
| supervisor@test.com | supervisor | Supervisor |
| mesero@test.com | mesero | Mesero |
| chef@test.com | chef | Chef |

**Contraseña para todos:** `SecureTestPass123!`

## Arquitectura

- **Patrón:** MVVM (Model-View-ViewModel) con AndroidViewModel
- **Estado:** Kotlin StateFlow con MutableStateFlow
- **Navegación:** Navigation Compose con NavHost; bottom nav dinámico por rol
- **Backend:** Supabase SDK (supabase-kt) con caché local Room
- **Diseño:** Sistema de diseño personalizado con tokens (spacing, radius, colors, animations)

## Base de datos

- **Supabase:** Tablas `usuarios`, `categorias_menu`, `items_menu`, `pedidos`, `pedido_items`, `transacciones` con Row Level Security (RLS)
- **Room:** Caché offline con 3 entidades: `UsuarioEntity`, `PedidoEntity`, `MenuItemEntity`

## Licencia

Proyecto privado. Todos los derechos reservados.