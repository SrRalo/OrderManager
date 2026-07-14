package com.example.ordermanager.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ordermanager.ui.navigation.BottomNavBar
import com.example.ordermanager.ui.navigation.BottomNavRoutes
import com.example.ordermanager.ui.theme.Background
import com.example.ordermanager.ui.viewmodel.OrderViewModel

@Composable
fun MainHubScreen(
    orderViewModel: OrderViewModel,
    usuario: String,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit
) {
    var currentTab by mutableStateOf(BottomNavRoutes.PEDIDOS)

    Scaffold(
        containerColor = Background,
        bottomBar = {
            BottomNavBar(
                currentRoute = currentTab,
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
                    BottomNavRoutes.BALANCES -> BalancesScreen()
                    BottomNavRoutes.HISTORIAL -> HistorialScreen(
                        orderViewModel = orderViewModel
                    )
                    BottomNavRoutes.PERFIL -> ProfileScreen(
                        negocioNombre = "Mi Restaurante",
                        telegramBotId = "@OrderManagerBot",
                        usuario = usuario,
                        isDarkMode = isDarkMode,
                        onToggleTheme = onToggleTheme,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}
