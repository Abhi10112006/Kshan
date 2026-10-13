package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ChronoColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = SpaceBlack,
    primaryContainer = Color(0xFF003844),
    onPrimaryContainer = NeonCyan,
    secondary = NeonPurple,
    onSecondary = SpaceBlack,
    secondaryContainer = Color(0xFF381545),
    onSecondaryContainer = NeonPurple,
    tertiary = NeonBlue,
    background = SpaceBlack,
    onBackground = TextPrimary,
    surface = SpaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SpaceCard,
    onSurfaceVariant = TextSecondary,
    outline = SpaceCardBorder,
    error = NeonPink,
    onError = SpaceBlack
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ChronoColorScheme,
        typography = Typography,
        content = content
    )
}
