package com.abhi.kshan.ui.components

import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.kshan.ui.theme.CardTitleTextStyle
import com.abhi.kshan.ui.theme.IceCyan
import com.abhi.kshan.ui.theme.JetBrainsMonoFontFamily
import com.abhi.kshan.ui.theme.KshanBackground
import com.abhi.kshan.ui.theme.KshanBorder
import com.abhi.kshan.ui.theme.KshanCyan
import com.abhi.kshan.ui.theme.KshanPillShape
import com.abhi.kshan.ui.theme.KshanSurface
import com.abhi.kshan.ui.theme.MetricLabelTextStyle
import com.abhi.kshan.ui.theme.SpaceGroteskFontFamily
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.ui.theme.TextSecondary
import com.abhi.kshan.ui.theme.TitaniumHairline
import com.abhi.kshan.ui.theme.VisionGold
import com.abhi.kshan.ui.theme.WarmChampagne
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Strict 4-Color, 2-Radius Card Component:
 * - Surface: #121620 (KshanSurface) with optional `surfaceAlpha` (1.0f standard, 0.60f for unpinned rows)
 * - Border: 1.dp solid Color.White.copy(alpha = 0.08f) on all standard cards
 *   (or explicit `customBorderColor` when `useCustomBorder = true`, e.g., pinned Kshan card)
 * - Corner Radius: Strictly 24.dp (`RoundedCornerShape(24.dp)`)
 */
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = KshanBorder,
    glowAlpha: Float = 0.25f,
    cornerRadius: Dp = 24.dp,
    surfaceAlpha: Float = 1.0f,
    useCustomBorder: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.99f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 520f),
        label = "card_press_scale"
    )

    val strictCardShape = RoundedCornerShape(24.dp)
    val resolvedBorderColor = if (useCustomBorder) {
        borderColor
    } else {
        KshanBorder
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(strictCardShape)
            .background(KshanSurface.copy(alpha = surfaceAlpha))
            .border(
                width = 1.dp,
                color = resolvedBorderColor,
                shape = strictCardShape
            )
            .padding(18.dp),
        content = content
    )
}

// Backward-compatibility alias
@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = KshanBorder,
    glowAlpha: Float = 0.25f,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) = FrostedGlassCard(
    modifier = modifier,
    borderColor = borderColor,
    glowAlpha = glowAlpha,
    cornerRadius = 24.dp,
    content = content
)

/**
 * Unified Split Card (`24.dp` radius, `#121620` surface, `1.dp` `Color.White.copy(alpha = 0.08f)` border)
 * with a `1.dp` vertical hairline divider and a centered full-pill (`RoundedCornerShape(100)`) action badge.
 */
@Composable
fun ConnectedDateCards(
    startContent: @Composable () -> Unit,
    endContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    connectorIcon: ImageVector? = null,
    connectorLabel: String = "⇄",
    accentColor: Color = KshanCyan,
    onConnectorClick: (() -> Unit)? = null
) {
    val view = LocalView.current
    val cardShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(KshanSurface)
            .border(1.dp, KshanBorder, cardShape),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 18.dp, top = 16.dp, bottom = 16.dp, end = 22.dp)
            ) {
                startContent()
            }

            // 1.dp 8% White Vertical Hairline Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .padding(vertical = 12.dp)
                    .background(KshanBorder)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 22.dp, top = 16.dp, bottom = 16.dp, end = 18.dp)
            ) {
                endContent()
            }
        }

        // Centered Full-Pill Connector / Swap Button
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(KshanPillShape)
                .background(KshanBackground)
                .border(
                    width = 1.dp,
                    color = KshanBorder,
                    shape = KshanPillShape
                )
                .then(
                    if (onConnectorClick != null) {
                        Modifier.clickable {
                            try {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            } catch (e: Exception) {
                                Log.w("ConnectedDateCards", "Haptic unavailable", e)
                            }
                            onConnectorClick()
                        }
                    } else {
                        Modifier
                    }
                )
                .testTag("unified_card_connector_pill"),
            contentAlignment = Alignment.Center
        ) {
            if (connectorIcon != null) {
                Icon(
                    imageVector = connectorIcon,
                    contentDescription = "Swap or Shift",
                    tint = KshanCyan,
                    modifier = Modifier.size(15.dp)
                )
            } else {
                Text(
                    text = connectorLabel,
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KshanCyan
                )
            }
        }
    }
}

/**
 * Ambient Hero Result Readout:
 * - Metric Label: 10.sp uppercase, letterSpacing = 1.2.sp, Color.White.copy(alpha = 0.50f)
 * - Hero Number: Pure White (#FFFFFF) JetBrains Mono (tnum)
 * - Breakdown Pill: RoundedCornerShape(100) (#121620 surface, 1.dp 8% white border)
 */
@Composable
fun AmbientHeroResult(
    mainValue: String,
    label: String,
    breakdownText: String,
    modifier: Modifier = Modifier,
    accentColor: Color = KshanCyan,
    testTag: String = "hero_main_number"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Subtle radial glow behind the hero number
        Box(
            modifier = Modifier
                .size(180.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            KshanCyan.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.02f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label.uppercase(),
                style = MetricLabelTextStyle
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = mainValue,
                style = TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 64.sp,
                    fontFeatureSettings = "tnum",
                    color = TextPrimary
                ),
                modifier = Modifier.testTag(testTag)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Full-Pill Breakdown Capsule
            Box(
                modifier = Modifier
                    .clip(KshanPillShape)
                    .background(KshanSurface)
                    .border(
                        width = 1.dp,
                        color = KshanBorder,
                        shape = KshanPillShape
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = breakdownText,
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Unified Hero Orbital Timepiece Card (Tab 3 — Kshans):
 * - 24.dp corner radius, #121620 surface, 1.dp Color.White.copy(alpha = 0.08f) border (no outer blue border)
 * - Event Title: 18.sp FontWeight.Medium sans-serif
 * - Digits (DAYS, HOURS, MINS, SECS): All Pure White (#FFFFFF) JetBrains Mono (tnum)
 * - SECS digit: 150ms vertical flip + subtle opacity pulse on each tick
 * - Real-Life Context Line directly below the ticking digits in JetBrains Mono
 * - Full-pill [ − Count Down | + Days Since ] segmented switch
 */
@Composable
fun HeroOrbitalTimepiece(
    days: Long,
    hours: Long,
    minutes: Long,
    seconds: Long,
    categoryName: String,
    eventTitle: String,
    statusText: String,
    modifier: Modifier = Modifier,
    accentColor: Color = KshanCyan,
    isCountUpMode: Boolean = false,
    canUseCountUpMode: Boolean = false,
    isAutoRolled: Boolean = false,
    isTodayCelebration: Boolean = false,
    targetYear: Int? = null,
    contextLine: String = "",
    onToggleCountUpMode: ((Boolean) -> Unit)? = null
) {
    val view = LocalView.current
    val infiniteTransition = rememberInfiniteTransition(label = "timepiece_colon")
    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colon_blink"
    )

    FrostedGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        useCustomBorder = false
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Category, Auto-Rollover / Today ✦ Badge & Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(KshanPillShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, KshanBorder, KshanPillShape)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = categoryName.uppercase(),
                        style = MetricLabelTextStyle.copy(color = KshanCyan)
                    )
                }

                if (isTodayCelebration) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(KshanPillShape)
                            .background(VisionGold.copy(alpha = 0.14f))
                            .border(1.dp, VisionGold.copy(alpha = 0.45f), KshanPillShape)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                            .testTag("hero_today_celebration_badge")
                    ) {
                        Text(
                            text = "TODAY ✦",
                            style = MetricLabelTextStyle.copy(color = VisionGold)
                        )
                    }
                } else if (isAutoRolled && targetYear != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(KshanPillShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, KshanBorder, KshanPillShape)
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "↻ NEXT: $targetYear",
                            style = MetricLabelTextStyle.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontFeatureSettings = "tnum",
                                color = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = statusText.uppercase(),
                    style = MetricLabelTextStyle
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Card Title: 18.sp FontWeight.Medium sans-serif
            Text(
                text = eventTitle,
                style = CardTitleTextStyle,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Unified Monospace Digits Readout (All Pure White #FFFFFF, JetBrains Mono tnum, 150ms vertical flip on SECS)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                DigitBlock(
                    value = if (isCountUpMode) "+${String.format("%02d", days)}" else String.format("%02d", days),
                    label = if (isCountUpMode) "DAYS SINCE" else "DAYS"
                )
                PulsingColon(alpha = colonAlpha)
                DigitBlock(value = String.format("%02d", hours), label = "HOURS")
                PulsingColon(alpha = colonAlpha)
                DigitBlock(value = String.format("%02d", minutes), label = "MINS")
                PulsingColon(alpha = colonAlpha)
                AnimatedSecondsDigitBlock(
                    secondsValue = String.format("%02d", seconds),
                    label = "SECS"
                )
            }

            // Real-Life Context Line directly below the ticking digits in JetBrains Mono
            if (contextLine.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .clip(KshanPillShape)
                        .background(KshanBackground.copy(alpha = 0.75f))
                        .border(1.dp, KshanBorder, KshanPillShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("hero_real_life_context_line")
                ) {
                    Text(
                        text = contextLine,
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.5.sp,
                            fontFeatureSettings = "tnum",
                            color = TextSecondary
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Only show the Count Down / Days Since full-pill toggle when the event has a past origin timestamp
            if (onToggleCountUpMode != null && canUseCountUpMode) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .clip(KshanPillShape)
                        .background(KshanBackground)
                        .border(1.dp, KshanBorder, KshanPillShape)
                        .padding(3.dp)
                        .testTag("countdown_mode_selector"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isDown = !isCountUpMode
                    Box(
                        modifier = Modifier
                            .clip(KshanPillShape)
                            .background(if (isDown) KshanCyan.copy(alpha = 0.16f) else Color.Transparent)
                            .then(
                                if (isDown) Modifier.border(1.dp, KshanCyan.copy(alpha = 0.55f), KshanPillShape)
                                else Modifier
                            )
                            .clickable {
                                try {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                } catch (e: Exception) {
                                    Log.w("HeroOrbitalTimepiece", "Haptic unavailable", e)
                                }
                                onToggleCountUpMode(false)
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("mode_pill_count_down")
                    ) {
                        Text(
                            text = "− Count Down",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.5.sp,
                            fontWeight = if (isDown) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isDown) KshanCyan else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(KshanPillShape)
                            .background(if (isCountUpMode) KshanCyan.copy(alpha = 0.16f) else Color.Transparent)
                            .then(
                                if (isCountUpMode) Modifier.border(1.dp, KshanCyan.copy(alpha = 0.55f), KshanPillShape)
                                else Modifier
                            )
                            .clickable {
                                try {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                } catch (e: Exception) {
                                    Log.w("HeroOrbitalTimepiece", "Haptic unavailable", e)
                                }
                                onToggleCountUpMode(true)
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("mode_pill_days_since")
                    ) {
                        Text(
                            text = "+ Days Since",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.5.sp,
                            fontWeight = if (isCountUpMode) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isCountUpMode) KshanCyan else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DigitBlock(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = TextStyle(
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum",
                color = TextPrimary // Pure White (#FFFFFF)
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label.uppercase(),
            style = MetricLabelTextStyle
        )
    }
}

/**
 * Pure White (#FFFFFF) SECS digit block with a 150ms vertical flip + subtle opacity pulse on each tick.
 */
@Composable
private fun AnimatedSecondsDigitBlock(
    secondsValue: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedContent(
            targetState = secondsValue,
            transitionSpec = {
                (slideInVertically(tween(150)) { h -> h / 2 } + fadeIn(animationSpec = tween(150), initialAlpha = 0.35f)) togetherWith
                        (slideOutVertically(tween(150)) { h -> -h / 2 } + fadeOut(tween(150)))
            },
            label = "secs_150ms_vertical_flip"
        ) { secStr ->
            Text(
                text = secStr,
                style = TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum",
                    color = TextPrimary // Pure White (#FFFFFF) — never blue
                )
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label.uppercase(),
            style = MetricLabelTextStyle
        )
    }
}

@Composable
private fun PulsingColon(alpha: Float) {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .offset(y = (-7).dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = ":",
            style = TextStyle(
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = alpha)
            )
        )
    }
}

@Composable
fun ModernPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    accentColor: Color = KshanCyan,
    icon: ImageVector? = null,
    testTag: String = "modern_pill_button"
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 600f),
        label = "pill_button_scale"
    )

    val resolvedAccent = if (isPrimary) KshanCyan else TextPrimary

    Box(
        modifier = modifier
            .scale(scale)
            .height(48.dp)
            .clip(KshanPillShape)
            .background(
                if (isPrimary) {
                    KshanCyan.copy(alpha = 0.16f)
                } else {
                    KshanSurface
                }
            )
            .border(
                width = 1.dp,
                color = if (isPrimary) KshanCyan.copy(alpha = 0.55f) else KshanBorder,
                shape = KshanPillShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    } catch (e: Exception) {
                        Log.w("CyberComponents", "Haptic feedback unavailable", e)
                    }
                    onClick()
                }
            )
            .padding(horizontal = 20.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = resolvedAccent,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp,
                color = resolvedAccent
            )
        }
    }
}

// Backward-compatibility alias
@Composable
fun CyberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    accentColor: Color = KshanCyan,
    testTag: String = "cyber_button"
) = ModernPillButton(
    text = text,
    onClick = onClick,
    modifier = modifier,
    isPrimary = isPrimary,
    accentColor = accentColor,
    testTag = testTag
)

@Composable
fun ModernToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = KshanCyan,
    testTag: String = "modern_toggle"
) {
    val view = LocalView.current
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 3.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f),
        label = "thumb_offset"
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) KshanCyan.copy(alpha = 0.20f) else KshanBackground,
        label = "track_color"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) KshanCyan.copy(alpha = 0.6f) else KshanBorder,
        label = "border_color"
    )

    Box(
        modifier = modifier
            .width(50.dp)
            .height(28.dp)
            .clip(KshanPillShape)
            .background(trackColor)
            .border(1.dp, borderColor, KshanPillShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                try {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                } catch (e: Exception) {
                    Log.w("CyberComponents", "Haptic feedback unavailable", e)
                }
                onCheckedChange(!checked)
            }
            .testTag(testTag),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(22.dp)
                .clip(CircleShape)
                .background(if (checked) KshanCyan else TextMuted)
        )
    }
}

// Backward-compatibility alias
@Composable
fun CyberToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = KshanCyan,
    testTag: String = "cyber_toggle"
) = ModernToggle(
    checked = checked,
    onCheckedChange = onCheckedChange,
    modifier = modifier,
    activeColor = activeColor,
    testTag = testTag
)

@Composable
fun ModernCapsuleChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = KshanCyan,
    testTag: String = ""
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label = "chip_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(KshanPillShape)
            .background(if (isSelected) KshanCyan.copy(alpha = 0.16f) else KshanSurface)
            .border(
                width = 1.dp,
                color = if (isSelected) KshanCyan.copy(alpha = 0.65f) else KshanBorder,
                shape = KshanPillShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    } catch (e: Exception) {
                        Log.w("CyberComponents", "Haptic feedback unavailable", e)
                    }
                    onClick()
                }
            )
            .padding(horizontal = 15.dp, vertical = 8.dp)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    ) {
        Text(
            text = label,
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 12.5.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) KshanCyan else TextSecondary
        )
    }
}

/**
 * Full-Pill Action Icon Button for saved countdown cards (Pin button).
 */
@Composable
fun FrostedActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    activeColor: Color = KshanCyan,
    testTag: String = ""
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label = "icon_btn_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(36.dp)
            .clip(KshanPillShape)
            .background(
                if (isActive) KshanCyan.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.04f)
            )
            .border(
                width = 1.dp,
                color = if (isActive) KshanCyan.copy(alpha = 0.55f) else KshanBorder,
                shape = KshanPillShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    } catch (e: Exception) {
                        Log.w("CyberComponents", "Haptic feedback unavailable", e)
                    }
                    onClick()
                }
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) KshanCyan else TextSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    val initialEpochMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialEpochMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    } catch (e: Exception) {
                        Log.w("CyberComponents", "Haptic feedback unavailable", e)
                    }
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedLocalDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        onDateSelected(selectedLocalDate)
                    }
                    onDismiss()
                }
            ) {
                Text(
                    text = "Select Date",
                    color = KshanCyan,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    } catch (e: Exception) {
                        Log.w("CyberComponents", "Haptic feedback unavailable", e)
                    }
                    onDismiss()
                }
            ) {
                Text(
                    text = "Cancel",
                    color = TextMuted,
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 14.sp
                )
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = KshanSurface
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = KshanSurface,
                titleContentColor = KshanCyan,
                headlineContentColor = TextPrimary,
                weekdayContentColor = TextSecondary,
                subheadContentColor = TextSecondary,
                yearContentColor = TextPrimary,
                currentYearContentColor = KshanCyan,
                selectedYearContentColor = KshanBackground,
                selectedYearContainerColor = KshanCyan,
                dayContentColor = TextPrimary,
                disabledDayContentColor = TextMuted,
                selectedDayContentColor = KshanBackground,
                selectedDayContainerColor = KshanCyan,
                todayContentColor = KshanCyan,
                todayDateBorderColor = KshanCyan
            )
        )
    }
}
