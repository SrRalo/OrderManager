package com.example.ordermanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ordermanager.ui.navigation.NavRoutes
import com.example.ordermanager.ui.screens.LoginScreen
import com.example.ordermanager.ui.screens.MainHubScreen
import com.example.ordermanager.ui.screens.RegisterScreen
import com.example.ordermanager.ui.screens.SplashScreen
import com.example.ordermanager.ui.screens.WelcomeScreen
import com.example.ordermanager.ui.theme.OrderManagerTheme
import com.example.ordermanager.ui.viewmodel.AuthViewModel
import com.example.ordermanager.ui.viewmodel.OrderViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val orderViewModel: OrderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemTheme = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(systemTheme) }

            OrderManagerTheme(darkTheme = isDarkMode) {
                MainApp(
                    authViewModel = authViewModel,
                    orderViewModel = orderViewModel,
                    isDarkMode = isDarkMode,
                    onToggleTheme = { isDarkMode = !isDarkMode }
                )
            }
        }
    }
}

@Composable
fun MainApp(
    authViewModel: AuthViewModel,
    orderViewModel: OrderViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH
    ) {
        composable(
            route = NavRoutes.SPLASH,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(NavRoutes.WELCOME) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = NavRoutes.WELCOME,
            enterTransition = { slideInHorizontally { it / 4 } + fadeIn() },
            exitTransition = { slideOutHorizontally { it / 4 } + fadeOut() },
            popEnterTransition = { slideInHorizontally { -it / 4 } + fadeIn() },
            popExitTransition = { slideOutHorizontally { -it / 4 } + fadeOut() }
        ) {
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.LOGIN)
                },
                onNavigateToRegister = {
                    navController.navigate(NavRoutes.REGISTER)
                }
            )
        }

        composable(
            route = NavRoutes.LOGIN,
            enterTransition = { slideInHorizontally { it / 4 } + fadeIn() },
            exitTransition = { slideOutHorizontally { it / 4 } + fadeOut() },
            popEnterTransition = { slideInHorizontally { -it / 4 } + fadeIn() },
            popExitTransition = { slideOutHorizontally { -it / 4 } + fadeOut() }
        ) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(NavRoutes.MAIN_HUB) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(NavRoutes.REGISTER)
                }
            )
        }

        composable(
            route = NavRoutes.REGISTER,
            enterTransition = { slideInHorizontally { it / 4 } + fadeIn() },
            exitTransition = { slideOutHorizontally { it / 4 } + fadeOut() },
            popEnterTransition = { slideInHorizontally { -it / 4 } + fadeIn() },
            popExitTransition = { slideOutHorizontally { -it / 4 } + fadeOut() }
        ) {
            RegisterScreen(
                authViewModel = authViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.popBackStack(NavRoutes.LOGIN, inclusive = false)
                }
            )
        }

        composable(
            route = NavRoutes.MAIN_HUB,
            enterTransition = { slideInHorizontally { it / 4 } + fadeIn() },
            exitTransition = { slideOutHorizontally { it / 4 } + fadeOut() }
        ) {
            MainHubScreen(
                orderViewModel = orderViewModel,
                usuario = authViewModel.authState.value.currentUser?.usuario ?: "",
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }
    }
}