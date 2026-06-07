# Project OrderManager

## Descripción del Proyecto
**OrderManager** es una aplicación Android moderna diseñada específicamente para chefs y personal de cocina, con el objetivo de facilitar la gestión y administración de pedidos y operaciones culinarias de manera eficiente.

## Objetivos
*   Proporcionar una interfaz intuitiva y atractiva para profesionales de la cocina.
*   Centralizar la gestión de pedidos y el estado de la cocina.
*   Ofrecer una experiencia de usuario fluida con soporte para temas claros y oscuros.

## Funcionalidades Actuales
*   **Pantalla de Inicio de Sesión (LoginScreen):**
    *   Interfaz personalizada con temática culinaria.
    *   Campos para correo electrónico y contraseña.
    *   Acceso a recuperación de contraseña y registro.
*   **Panel de Control (HomeScreen/Dashboard):**
    *   Mensaje de bienvenida personalizado ("¡Bienvenido, Chef!").
    *   Acceso rápido a las funciones principales de la cocina.
*   **Personalización de Tema:**
    *   Soporte completo para modo claro (Light Mode) y modo oscuro (Dark Mode).
    *   Cambio dinámico de tema mediante un botón en la barra superior.
*   **Navegación Fluida:**
    *   Transiciones suaves entre el login y la pantalla principal (Home) utilizando animaciones de `Crossfade`.

## Tecnologías Utilizadas
*   **Lenguaje:** Kotlin
*   **UI Framework:** Jetpack Compose (Moderno, declarativo y eficiente).
*   **Diseño:** Material Design 3 (M3) para componentes y estilizado.
*   **Gestión de Estado:** `remember`, `mutableStateOf` y flujos de navegación sencillos.
*   **Arquitectura:** Siguiendo las mejores prácticas de desarrollo Android moderno.

## Estructura de Archivos Clave
*   `MainActivity.kt`: Punto de entrada que gestiona el estado global de autenticación y el tema.
*   `LoginScreen.kt`: Interfaz de usuario para la autenticación.
*   `HomeScreen.kt`: Interfaz principal del usuario una vez autenticado.
*   `ui/theme/`: Definiciones de colores, tipografía, espaciado (`Spacing`) y radios (`Radius`) personalizados para el proyecto.
