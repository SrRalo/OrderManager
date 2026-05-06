package com.example.ordermanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import com.example.ordermanager.ui.screens.HomeScreen
import com.example.ordermanager.ui.screens.LoginScreen
import com.example.ordermanager.ui.theme.OrderManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemTheme = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(systemTheme) }
            
            OrderManagerTheme(darkTheme = isDarkMode) {
                MainApp(
                    isDarkMode = isDarkMode,
                    onToggleTheme = { isDarkMode = !isDarkMode }
                )
            }
        }
    }
}

@Composable
fun MainApp(
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    var isLoggedIn by remember { mutableStateOf(false) }

    Crossfade(targetState = isLoggedIn, label = "ScreenTransition") { loggedIn ->
        if (loggedIn) {
            HomeScreen(
                isDarkMode = isDarkMode,
                onLogout = { isLoggedIn = false },
                onToggleTheme = onToggleTheme
            )
        } else {
            LoginScreen(onLoginSuccess = { isLoggedIn = true })
        }
    }
}
