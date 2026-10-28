package com.abhi.kshan.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

// Strict 2-Radius Geometry System:
// 1. Every Card = 24.dp
// 2. Every Button, Chip, Segmented Control & Dock = Full Pill (100%)
val KshanCardShape = RoundedCornerShape(24.dp)
val KshanPillShape = RoundedCornerShape(100)

private val KshanShapes = Shapes(
    extraSmall = KshanPillShape,
    small = KshanPillShape,
    medium = KshanCardShape,
    large = KshanCardShape,
    extraLarge = KshanCardShape
)

private val ChronoColorScheme = darkColorScheme(
    primary = KshanCyan,
    onPrimary = KshanBackground,
    primaryContainer = KshanSurface,
    onPrimaryContainer = KshanCyan,
    secondary = KshanCyan,
    onSecondary = KshanBackground,
    secondaryContainer = KshanSurface,
    onSecondaryContainer = TextPrimary,
    tertiary = VisionGold,
    background = KshanBackground,
    onBackground = TextPrimary,
    surface = KshanSurface,
    onSurface = TextPrimary,
    surfaceVariant = KshanSurface,
    onSurfaceVariant = TextSecondary,
    outline = KshanBorder,
    error = Color(0xFFF43F5E),
    onError = KshanBackground
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ChronoColorScheme,
        typography = Typography,
        shapes = KshanShapes
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides TextStyle(
                fontFamily = SpaceGroteskFontFamily,
                color = TextPrimary
            )
        ) {
            content()
        }
    }
}
