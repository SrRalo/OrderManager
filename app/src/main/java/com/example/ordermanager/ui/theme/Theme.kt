package com.example.ordermanager.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Surface,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = PrimaryLight,
    secondary = Warning,
    onSecondary = Surface,
    secondaryContainer = WarningDark,
    onSecondaryContainer = WarningLight,
    tertiary = Success,
    onTertiary = Surface,
    tertiaryContainer = SuccessDark,
    onTertiaryContainer = SuccessLight,
    background = TextPrimary,
    onBackground = Surface,
    surface = DarkSurface,
    onSurface = Surface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Warning,
    onError = Surface,
    errorContainer = WarningDark,
    onErrorContainer = WarningLight,
    inverseSurface = Surface,
    inverseOnSurface = TextPrimary,
    inversePrimary = PrimaryLight,
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Surface,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    secondary = Warning,
    onSecondary = Surface,
    secondaryContainer = WarningLight,
    onSecondaryContainer = WarningDark,
    tertiary = Success,
    onTertiary = Surface,
    tertiaryContainer = SuccessLight,
    onTertiaryContainer = SuccessDark,
    background = Background,
    surface = Surface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceAlt,
    onSurfaceVariant = TextSecondary,
    outline = Border,
    outlineVariant = BorderLight,
    error = Warning,
    onError = Surface,
    errorContainer = WarningLight,
    onErrorContainer = WarningDark
)

@Composable
fun OrderManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desactivamos color dinámico para mantener la identidad de marca solicitada
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        window.statusBarColor = colorScheme.background.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
