package com.example.ordermanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ordermanager.ui.screens.HomeScreen
import com.example.ordermanager.ui.screens.LoginScreen
import com.example.ordermanager.ui.screens.RegisterScreen
import com.example.ordermanager.ui.screens.SplashScreen
import com.example.ordermanager.ui.screens.WelcomeScreen
import com.example.ordermanager.ui.theme.OrderManagerTheme
import com.example.ordermanager.ui.viewmodel.AuthViewModel
import com.example.ordermanager.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemTheme = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(systemTheme) }

            OrderManagerTheme(darkTheme = isDarkMode) {
                MainApp(
                    authViewModel = authViewModel,
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
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

        Crossfade(targetState = authState.currentScreen, label = "ScreenTransition") { screen ->
        when (screen) {
            Screen.SPLASH -> SplashScreen(
                onSplashFinished = { authViewModel.navigateToWelcome() }
            )
            Screen.WELCOME -> WelcomeScreen(
                onNavigateToLogin = { authViewModel.navigateToLogin() },
                onNavigateToRegister = { authViewModel.navigateToRegister() }
            )
            Screen.LOGIN -> LoginScreen(authViewModel = authViewModel)
            Screen.HOME -> HomeScreen(
                authViewModel = authViewModel,
                isDarkMode = isDarkMode,
                onToggleTheme = onToggleTheme
            )
            Screen.REGISTER -> RegisterScreen(authViewModel = authViewModel)
        }
    }
}
