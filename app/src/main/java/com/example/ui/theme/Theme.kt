package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Slate900,
    primaryContainer = Slate800,
    onPrimaryContainer = CyanLight,
    secondary = AmberGold,
    onSecondary = Slate900,
    secondaryContainer = Slate800,
    onSecondaryContainer = AmberGold,
    tertiary = EmeraldGreen,
    onTertiary = Slate900,
    background = Slate900,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = Slate700,
    error = RoseRed,
    onError = Color.White
)

private val LightColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Slate900,
    primaryContainer = Slate800,
    onPrimaryContainer = CyanLight,
    secondary = AmberGold,
    onSecondary = Slate900,
    secondaryContainer = Slate800,
    onSecondaryContainer = AmberGold,
    tertiary = EmeraldGreen,
    onTertiary = Slate900,
    background = Slate900,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = Slate700,
    error = RoseRed,
    onError = Color.White
)

@Composable
fun PyMetaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
