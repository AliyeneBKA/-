package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = QcmGreen,
    onPrimary = QcmNavyDark,
    primaryContainer = QcmNavyLight,
    onPrimaryContainer = Color.White,
    secondary = QcmGreenDark,
    onSecondary = Color.White,
    background = QcmNavyDark,
    surface = QcmNavy,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = QcmNavyLight,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334155)
)

private val LightColorScheme = lightColorScheme(
    primary = QcmNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E8F0),
    onPrimaryContainer = QcmNavy,
    secondary = QcmGreen,
    onSecondary = Color.White,
    secondaryContainer = QcmGreenLight,
    onSecondaryContainer = QcmGreenDark,
    background = QcmBgLight,
    surface = QcmSurface,
    onBackground = QcmTextPrimary,
    onSurface = QcmTextPrimary,
    surfaceVariant = QcmSurfaceVariant,
    onSurfaceVariant = QcmTextSecondary,
    outline = QcmBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded #1B2A4A and #2ECC71
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
