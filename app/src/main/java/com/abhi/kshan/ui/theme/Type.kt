package com.abhi.kshan.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.abhi.kshan.R

/**
 * Hard-bundled Google Fonts in res/font/ disabling phone OS system font overrides.
 */

// Kalam (Bold 700) strictly isolated to the single Devanagari brand emblem ("क्षण")
val KalamFont = FontFamily(
    Font(R.font.kalam, FontWeight.Bold)
)
val KalamFontFamily = KalamFont

// Comic Neue (Regular 400 & Bold 700) bundled locally for English headers, titles, cards, buttons, descriptions
val ComicNeueFontFamily = FontFamily(
    Font(R.font.comic_neue, FontWeight.Normal),
    Font(R.font.comic_neue, FontWeight.Bold)
)
val ComicFontFamily = ComicNeueFontFamily
val SansFontFamily = ComicNeueFontFamily

// Courier Prime (Regular 400 & Bold 700) mechanical Typewriter font with crisp slab serifs
// Applied to ALL numbers, dates, countdown timers, percentages, timestamps, and FPS telemetry
val CourierPrimeFontFamily = FontFamily(
    Font(R.font.courier_prime, FontWeight.Normal),
    Font(R.font.courier_prime, FontWeight.Bold)
)
val TypewriterFontFamily = CourierPrimeFontFamily
val TabularMonoFontFamily = CourierPrimeFontFamily

val TabularMonoTextStyle = TextStyle(
    fontFamily = CourierPrimeFontFamily,
    fontWeight = FontWeight.Bold,
    fontFeatureSettings = "tnum"
)

val TypewriterNumericStyle = TextStyle(
    fontFamily = CourierPrimeFontFamily,
    fontWeight = FontWeight.Bold,
    fontFeatureSettings = "tnum"
)

/**
 * Full Material 3 Typography mapping all 15 slots strictly to bundled res/font/ resources.
 */
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = ComicNeueFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.5).sp,
        color = TextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = CourierPrimeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.02).sp,
        fontFeatureSettings = "tnum",
        color = TextPrimary
    ),
    displaySmall = TextStyle(
        fontFamily = ComicNeueFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        color = TextPrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = ComicNeueFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = ComicNeueFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        color = TextPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = ComicNeueFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = ComicNeueFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.3).sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = ComicNeueFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.2).sp,
        color = NeonCyan
    ),
    titleSmall = TextStyle(
        fontFamily = ComicNeueFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = ComicNeueFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = ComicNeueFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = TextSecondary
    ),
    bodySmall = TextStyle(
        fontFamily = ComicNeueFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TextMuted
    ),
    labelLarge = TextStyle(
        fontFamily = ComicNeueFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = ComicNeueFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = CourierPrimeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.0.sp,
        fontFeatureSettings = "tnum",
        color = TextMuted
    )
)
