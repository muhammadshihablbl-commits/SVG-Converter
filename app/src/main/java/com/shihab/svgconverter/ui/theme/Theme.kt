package com.shihab.svgconverter.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    background = Color(0xFF0F1115),
    surface = Color(0xFF0F1115),
    surfaceVariant = Color(0xFF1E2026),
    primary = Color(0xFF8AB4F8),
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF9AA0A6),
    outlineVariant = Color(0xFF2E3138)
)

private val LightColorScheme = lightColorScheme(
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFF8F9FA),
    surfaceVariant = Color(0xFFE8EAED),
    primary = Color(0xFF1A73E8),
    onSurface = Color(0xFF202124),
    onSurfaceVariant = Color(0xFF5F6368),
    outlineVariant = Color(0xFFDADCE0)
)

@Composable
fun SvgConvertTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
