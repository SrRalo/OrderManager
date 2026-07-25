package com.example.ordermanager.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ordermanager.ui.navigation.BottomNavItem
import com.example.ordermanager.ui.navigation.BottomNavBar
import com.example.ordermanager.ui.navigation.BottomNavRoutes
import com.example.ordermanager.ui.navigation.bottomNavItems
import com.example.ordermanager.ui.screens.BalancesScreen
import com.example.ordermanager.ui.screens.GestionMenuScreen
import com.example.ordermanager.ui.screens.GestionUsuariosScreen
import com.example.ordermanager.ui.screens.HistorialScreen
import com.example.ordermanager.ui.screens.MenuSeleccionScreen
import com.example.ordermanager.ui.screens.PendingOrdersScreen
import com.example.ordermanager.ui.screens.ProfileScreen
import com.example.ordermanager.ui.theme.Background
import com.example.ordermanager.ui.viewmodel.MenuViewModel
import com.example.ordermanager.ui.viewmodel.UserManagementViewModel
import com.example.ordermanager.ui.viewmodel.UserRole
import com.example.ordermanager.ui.viewmodel.OrderViewModel

@Composable
fun MainHubScreen(
    orderViewModel: OrderViewModel,
    menuViewModel: MenuViewModel,
    userManagementViewModel: UserManagementViewModel,
    currentUserId: String?,
    usuario: String,
    userRole: UserRole,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    onCreateUserByAdmin: (nombres: String, correo: String, usuario: String, telefono: String, contrasenaTemporal: String, rol: UserRole, onResult: (Result<Long>) -> Unit) -> Unit,
    onNavigateToCrearPedido: () -> Unit = {},
    onNavigateToGestionMenu: () -> Unit = {},
    onNavigateToGestionUsuarios: () -> Unit = {}
) {
    val allowedRoutes = remember(userRole) {
        when (userRole) {
            UserRole.ADMIN -> setOf(
                BottomNavRoutes.PEDIDOS,
                BottomNavRoutes.CREAR_PEDIDO,
                BottomNavRoutes.GESTION_MENU,
                BottomNavRoutes.GESTION_USUARIOS,
                BottomNavRoutes.BALANCES,
                BottomNavRoutes.HISTORIAL,
                BottomNavRoutes.PERFIL
            )
            UserRole.SUPERVISOR -> setOf(
                BottomNavRoutes.PEDIDOS,
                BottomNavRoutes.CREAR_PEDIDO,
                BottomNavRoutes.GESTION_MENU,
                BottomNavRoutes.BALANCES,
                BottomNavRoutes.HISTORIAL,
                BottomNavRoutes.PERFIL
            )
            UserRole.CHEF -> setOf(
                BottomNavRoutes.PEDIDOS,
                BottomNavRoutes.HISTORIAL,
                BottomNavRoutes.PERFIL
            )
            UserRole.MESERO -> setOf(
                BottomNavRoutes.CREAR_PEDIDO,
                BottomNavRoutes.HISTORIAL,
                BottomNavRoutes.PERFIL
            )
        }
    }
    val visibleItems: List<BottomNavItem> = remember(allowedRoutes) {
        bottomNavItems.filter { it.route in allowedRoutes }
    }
    val defaultTab = visibleItems.firstOrNull()?.route ?: BottomNavRoutes.PERFIL
    var currentTab by remember(userRole) { mutableStateOf(defaultTab) }
    var negocioNombre by remember { mutableStateOf("Mi Restaurante") }

    LaunchedEffect(allowedRoutes) {
        if (currentTab !in allowedRoutes) {
            currentTab = defaultTab
        }
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            BottomNavBar(
                currentRoute = currentTab,
                items = visibleItems,
                onItemSelected = { currentTab = it }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "tabContent"
            ) { tab ->
                when (tab) {
                    BottomNavRoutes.PEDIDOS -> PendingOrdersScreen(
                        orderViewModel = orderViewModel
                    )
                    BottomNavRoutes.CREAR_PEDIDO -> MenuSeleccionScreen(
                        onBack = { currentTab = BottomNavRoutes.PEDIDOS },
                        meseroId = currentUserId,
                        menuViewModel = menuViewModel
                    )
                    BottomNavRoutes.GESTION_MENU -> GestionMenuScreen(
                        onBack = { currentTab = BottomNavRoutes.PEDIDOS },
                        menuViewModel = menuViewModel
                    )
                    BottomNavRoutes.GESTION_USUARIOS -> GestionUsuariosScreen(
                        onBack = { currentTab = BottomNavRoutes.PEDIDOS },
                        userManagementViewModel = userManagementViewModel
                    )
                    BottomNavRoutes.BALANCES -> BalancesScreen()
                    BottomNavRoutes.HISTORIAL -> HistorialScreen(
                        orderViewModel = orderViewModel
                    )
                    BottomNavRoutes.PERFIL -> ProfileScreen(
                        negocioNombre = negocioNombre,
                        telegramBotId = "@OrderManagerBot",
                        usuario = usuario,
                        userRole = userRole,
                        isDarkMode = isDarkMode,
                        onToggleTheme = onToggleTheme,
                        onLogout = onLogout,
                        onCreateUserByAdmin = onCreateUserByAdmin,
                        onNegocioNombreChange = { negocioNombre = it }
                    )
                }
            }
        }
    }
}
