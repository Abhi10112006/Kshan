package com.abhi.kshan.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.kshan.engine.CountdownStatus
import com.abhi.kshan.engine.TimeEngine
import com.abhi.kshan.ui.theme.ComicNeueFontFamily
import com.abhi.kshan.ui.theme.CourierPrimeFontFamily
import com.abhi.kshan.ui.theme.KalamFont
import com.abhi.kshan.ui.theme.TabularMonoFontFamily
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextSecondary
import com.abhi.kshan.viewmodel.ChronoUiState

enum class WidgetPreviewFormat(val label: String) {
    ORBIT_2X2("2×2 Orbit"),
    PANORAMIC_4X2("4×2 Panoramic"),
    PILL_4X1("4×1 Minimal Pill")
}

/**
 * Clean Upper-Half Phone Mockup.
 * Renders the top rounded bezel, speaker ear-piece cutout, status bar (10:42, Wi-Fi, battery),
 * dynamic cosmic wallpaper with accent nebula, and the active live widget card.
 */
@Composable
fun UpperHalfPhoneMockup(
    format: WidgetPreviewFormat,
    uiState: ChronoUiState,
    opacity: Float,
    accentColor: Color,
    showBadge: Boolean,
    showProgress: Boolean,
    showTargetDate: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(238.dp)
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(Color(0xFF070B14))
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF475569), Color(0xFF1E293B), Color.Transparent)
                ),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
            )
    ) {
        // Cosmic Wallpaper Canvas with Nebula Glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.22f),
                        Color(0xFF1E1B4B).copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.7f, size.height * 0.4f),
                    radius = size.width * 0.8f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Speaker cutout ear-piece
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF334155))
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Realistic Status Bar: 10:42, Wi-Fi, battery 98%
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "10:42",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Wi-Fi",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "98%",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Widget Preview Card inside Mockup
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (format) {
                    WidgetPreviewFormat.ORBIT_2X2 -> {
                        WidgetCardOrbit2x2(
                            uiState = uiState,
                            opacity = opacity,
                            accentColor = accentColor,
                            showBadge = showBadge,
                            showProgress = showProgress
                        )
                    }
                    WidgetPreviewFormat.PANORAMIC_4X2 -> {
                        WidgetCardPanoramic4x2(
                            uiState = uiState,
                            opacity = opacity,
                            accentColor = accentColor,
                            showBadge = showBadge,
                            showProgress = showProgress,
                            showTargetDate = showTargetDate
                        )
                    }
                    WidgetPreviewFormat.PILL_4X1 -> {
                        WidgetCardMinimalPill4x1(
                            uiState = uiState,
                            opacity = opacity,
                            accentColor = accentColor,
                            showBadge = showBadge
                        )
                    }
                }
            }
        }
    }
}

/**
 * 4×2 Panoramic Widget Preview:
 * Shows Days + Live Ticking HH:MM:SS remainder countdown timer with smooth digit flip animations,
 * or "MILESTONE REACHED" when the target instant has arrived.
 */
@Composable
fun WidgetCardPanoramic4x2(
    uiState: ChronoUiState,
    opacity: Float,
    accentColor: Color,
    showBadge: Boolean,
    showProgress: Boolean,
    showTargetDate: Boolean
) {
    val isReached = uiState.countdownTelemetry.status == CountdownStatus.TARGET_REACHED ||
            uiState.countdownTelemetry.status == CountdownStatus.EVENT_PASSED

    val infiniteTransition = rememberInfiniteTransition(label = "widget_4x2_pulse")
    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colon_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0B101E).copy(alpha = opacity))
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.22f), accentColor.copy(alpha = 0.38f), Color.Transparent)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "क्षण",
                        fontFamily = KalamFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Text(
                        text = " • Kshan",
                        fontFamily = ComicNeueFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }

                if (showBadge) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = uiState.activeEventCategory.uppercase(),
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showProgress) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = Color.White.copy(alpha = 0.15f), style = Stroke(4.dp.toPx()))
                            val sweep = if (isReached) 360f else ((365 - (uiState.countdownTelemetry.days % 365)) / 365f) * 360f
                            drawArc(
                                color = accentColor,
                                startAngle = -90f,
                                sweepAngle = sweep.coerceIn(30f, 360f),
                                useCenter = false,
                                style = Stroke(4.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = if (isReached) "✓" else "${uiState.countdownTelemetry.days}d",
                            fontFamily = TabularMonoFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    if (isReached) {
                        Text(
                            text = "MILESTONE REACHED",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${uiState.countdownTelemetry.days} DAYS • ",
                                fontFamily = TabularMonoFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            LiveAnimatedTimerPill(
                                hours = uiState.countdownTelemetry.hours,
                                minutes = uiState.countdownTelemetry.minutes,
                                seconds = uiState.countdownTelemetry.seconds,
                                accentColor = accentColor,
                                colonAlpha = colonAlpha
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = uiState.activeEventTitle,
                        fontFamily = ComicNeueFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            if (showTargetDate) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Target: ${uiState.countdownTarget.format(TimeEngine.SHORT_DATE_FORMATTER)} • Daily boundary sync",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}

/**
 * 2×2 Orbit Widget Preview:
 * Shows Days Only inside the orbital progress ring for ultra-clean compact symmetry.
 */
@Composable
fun WidgetCardOrbit2x2(
    uiState: ChronoUiState,
    opacity: Float,
    accentColor: Color,
    showBadge: Boolean,
    showProgress: Boolean
) {
    val isReached = uiState.countdownTelemetry.status == CountdownStatus.TARGET_REACHED ||
            uiState.countdownTelemetry.status == CountdownStatus.EVENT_PASSED

    Box(
        modifier = Modifier
            .size(152.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF0B101E).copy(alpha = opacity))
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.22f), accentColor.copy(alpha = 0.35f), Color.Transparent)
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showBadge) {
                Text(
                    text = uiState.activeEventCategory.uppercase(),
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (showProgress) {
                Box(
                    modifier = Modifier.size(58.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = Color.White.copy(alpha = 0.15f), style = Stroke(5.dp.toPx()))
                        val sweep = if (isReached) 360f else ((365 - (uiState.countdownTelemetry.days % 365)) / 365f) * 360f
                        drawArc(
                            color = accentColor,
                            startAngle = -90f,
                            sweepAngle = sweep.coerceIn(30f, 360f),
                            useCenter = false,
                            style = Stroke(5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isReached) "0" else "${uiState.countdownTelemetry.days}",
                            fontFamily = TabularMonoFontFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "DAYS",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            } else {
                Text(
                    text = if (isReached) "REACHED" else "${uiState.countdownTelemetry.days}d",
                    fontFamily = TabularMonoFontFamily,
                    fontSize = if (isReached) 15.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = uiState.activeEventTitle,
                fontFamily = ComicNeueFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 4×1 Minimal Pill Widget Preview:
 * Shows Days + Live Ticking HH:MM:SS countdown timer inside a sleek horizontal capsule.
 */
@Composable
fun WidgetCardMinimalPill4x1(
    uiState: ChronoUiState,
    opacity: Float,
    accentColor: Color,
    showBadge: Boolean
) {
    val isReached = uiState.countdownTelemetry.status == CountdownStatus.TARGET_REACHED ||
            uiState.countdownTelemetry.status == CountdownStatus.EVENT_PASSED

    val infiniteTransition = rememberInfiniteTransition(label = "widget_4x1_pulse")
    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pill_colon_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .clip(RoundedCornerShape(31.dp))
            .background(Color(0xFF0B101E).copy(alpha = opacity))
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.25f), accentColor.copy(alpha = 0.4f), Color.Transparent)
                ),
                shape = RoundedCornerShape(31.dp)
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "क्षण",
                    fontFamily = KalamFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uiState.activeEventTitle,
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        if (showBadge) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${uiState.activeEventCategory.uppercase()}",
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }
                    Text(
                        text = uiState.countdownTarget.format(TimeEngine.SHORT_DATE_FORMATTER),
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            if (isReached) {
                Text(
                    text = "REACHED",
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            } else {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${uiState.countdownTelemetry.days}d",
                        fontFamily = TabularMonoFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    LiveAnimatedTimerPill(
                        hours = uiState.countdownTelemetry.hours,
                        minutes = uiState.countdownTelemetry.minutes,
                        seconds = uiState.countdownTelemetry.seconds,
                        accentColor = accentColor,
                        colonAlpha = colonAlpha
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveAnimatedTimerPill(
    hours: Long,
    minutes: Long,
    seconds: Long,
    accentColor: Color,
    colonAlpha: Float
) {
    val hStr = String.format("%02d", hours)
    val mStr = String.format("%02d", minutes)
    val sStr = String.format("%02d", seconds)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = hStr,
            fontFamily = CourierPrimeFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
        Text(
            text = ":",
            fontFamily = CourierPrimeFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor.copy(alpha = colonAlpha)
        )
        Text(
            text = mStr,
            fontFamily = CourierPrimeFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
        Text(
            text = ":",
            fontFamily = CourierPrimeFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor.copy(alpha = colonAlpha)
        )
        AnimatedContent(
            targetState = sStr,
            transitionSpec = {
                (slideInVertically { height -> height / 2 } + fadeIn(tween(180)))
                    .togetherWith(slideOutVertically { height -> -height / 2 } + fadeOut(tween(180)))
            },
            label = "widget_sec_flip"
        ) { targetSec ->
            Text(
                text = targetSec,
                fontFamily = CourierPrimeFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}
