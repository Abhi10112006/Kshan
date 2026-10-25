package com.abhi.kshan.ui.components

import android.util.Log
import android.view.HapticFeedbackConstants
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.kshan.ui.theme.CourierPrimeFontFamily
import com.abhi.kshan.ui.theme.IceCyan
import com.abhi.kshan.ui.theme.NeonCyan
import com.abhi.kshan.ui.theme.ObsidianGlass
import com.abhi.kshan.ui.theme.SpaceBlack
import com.abhi.kshan.ui.theme.SpaceCard
import com.abhi.kshan.ui.theme.SpaceCardBorder
import com.abhi.kshan.ui.theme.SpaceDark
import com.abhi.kshan.ui.theme.SpaceGroteskFontFamily
import com.abhi.kshan.ui.theme.TabularMonoFontFamily
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.ui.theme.TextSecondary
import com.abhi.kshan.ui.theme.TitaniumHairline
import com.abhi.kshan.ui.theme.WarmChampagne
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Minimalist Luxury "Titanium & Ice" Obsidian Glass Card.
 *
 * Uses a unified, quiet Obsidian Glass fill (#0A0E17 at 85% alpha) with a clean,
 * uniform 0.8.dp Titanium Hairline Border (White.copy(alpha = 0.10f)).
 * Only actively selected cards/pills (glowAlpha >= 0.55f) receive a subtle Ice Cyan
 * (#38BDF8) or Warm Champagne (#E5C158) border.
 */
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = TitaniumHairline,
    glowAlpha: Float = 0.25f,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.988f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 520f),
        label = "card_press_scale"
    )

    val isActivelyHighlighted = glowAlpha >= 0.55f && borderColor != TitaniumHairline && borderColor != SpaceCardBorder
    val resolvedBorderColor = if (isActivelyHighlighted) {
        borderColor.copy(alpha = 0.55f)
    } else {
        TitaniumHairline
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(cornerRadius))
            .background(ObsidianGlass)
            .border(
                width = 0.8.dp,
                color = resolvedBorderColor,
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(18.dp),
        content = content
    )
}

// Backward-compatibility alias
@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = TitaniumHairline,
    glowAlpha: Float = 0.25f,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) = FrostedGlassCard(
    modifier = modifier,
    borderColor = borderColor,
    glowAlpha = glowAlpha,
    cornerRadius = cornerRadius,
    content = content
)

/**
 * Connected Date Cards with floating center badge (⇄ or +/−).
 */
@Composable
fun ConnectedDateCards(
    startContent: @Composable () -> Unit,
    endContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    connectorIcon: ImageVector? = null,
    connectorLabel: String = "⇄",
    accentColor: Color = IceCyan
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                startContent()
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                endContent()
            }
        }

        // Subtle Titanium Center Connector Badge
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFF0A0E17))
                .border(
                    width = 0.8.dp,
                    color = Color.White.copy(alpha = 0.16f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (connectorIcon != null) {
                Icon(
                    imageVector = connectorIcon,
                    contentDescription = "Connector",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = connectorLabel,
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }
        }
    }
}

/**
 * Minimalist Luxury Ambient Hero Result Readout.
 * Sits in clean, generous negative space with a soft monochrome/ice-cyan radial glow.
 */
@Composable
fun AmbientHeroResult(
    mainValue: String,
    label: String,
    breakdownText: String,
    modifier: Modifier = Modifier,
    accentColor: Color = IceCyan,
    testTag: String = "hero_main_number"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Soft Monochrome / Ice-Cyan Radial Glow
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            IceCyan.copy(alpha = 0.10f),
                            Color.White.copy(alpha = 0.03f),
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
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = mainValue,
                fontFamily = TabularMonoFontFamily,
                fontSize = 68.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.testTag(testTag)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Quiet Titanium Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(ObsidianGlass)
                    .border(
                        width = 0.8.dp,
                        color = TitaniumHairline,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = breakdownText,
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Unified Hero Orbital Timepiece Card.
 *
 * - Supports a dedicated "Today ✦" celebration badge when a milestone occurs today.
 * - Disables/hides the "+ Days Since" toggle when `canUseCountUpMode == false` (strictly future events).
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
    accentColor: Color = IceCyan,
    isCountUpMode: Boolean = false,
    canUseCountUpMode: Boolean = false,
    isAutoRolled: Boolean = false,
    isTodayCelebration: Boolean = false,
    targetYear: Int? = null,
    onToggleCountUpMode: ((Boolean) -> Unit)? = null
) {
    val view = LocalView.current
    val infiniteTransition = rememberInfiniteTransition(label = "timepiece_colon")
    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colon_blink"
    )

    val effectiveAccent = if (isTodayCelebration) WarmChampagne else IceCyan

    FrostedGlassCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = effectiveAccent,
        glowAlpha = 0.6f,
        cornerRadius = 26.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Subtle Top Specular Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                effectiveAccent.copy(alpha = 0.65f),
                                Color.White.copy(alpha = 0.55f),
                                effectiveAccent.copy(alpha = 0.65f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category, Auto-Rollover / Today ✦ Badge & Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(0.8.dp, TitaniumHairline, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = categoryName.uppercase(),
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = effectiveAccent
                    )
                }

                if (isTodayCelebration) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarmChampagne.copy(alpha = 0.16f))
                            .border(0.8.dp, WarmChampagne.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                            .testTag("hero_today_celebration_badge")
                    ) {
                        Text(
                            text = "Today ✦",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WarmChampagne
                        )
                    }
                } else if (isAutoRolled && targetYear != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarmChampagne.copy(alpha = 0.14f))
                            .border(0.8.dp, WarmChampagne.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "↻ Next: $targetYear",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmChampagne
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = statusText,
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isTodayCelebration) WarmChampagne else TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Event Title
            Text(
                text = eventTitle,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Unified Monospace Digits Readout with Pulsing Colons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                DigitBlock(
                    value = if (isCountUpMode) "+${String.format("%02d", days)}" else String.format("%02d", days),
                    label = if (isCountUpMode) "DAYS SINCE" else "DAYS"
                )
                PulsingColon(alpha = colonAlpha, accentColor = effectiveAccent)
                DigitBlock(value = String.format("%02d", hours), label = "HOURS")
                PulsingColon(alpha = colonAlpha, accentColor = effectiveAccent)
                DigitBlock(value = String.format("%02d", minutes), label = "MINS")
                PulsingColon(alpha = colonAlpha, accentColor = effectiveAccent)
                DigitBlock(
                    value = String.format("%02d", seconds),
                    label = "SECS",
                    isHighlight = true,
                    highlightColor = effectiveAccent
                )
            }

            // Only show the Count Down / Days Since toggle pill when the event has a past origin timestamp
            if (onToggleCountUpMode != null && canUseCountUpMode) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(SpaceDark.copy(alpha = 0.9f))
                        .border(0.8.dp, TitaniumHairline, RoundedCornerShape(50))
                        .padding(3.dp)
                        .testTag("countdown_mode_selector"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isDown = !isCountUpMode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isDown) effectiveAccent.copy(alpha = 0.18f) else Color.Transparent)
                            .then(
                                if (isDown) Modifier.border(0.8.dp, effectiveAccent.copy(alpha = 0.55f), RoundedCornerShape(50))
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
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                            .testTag("mode_pill_count_down")
                    ) {
                        Text(
                            text = "− Count Down",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.5.sp,
                            fontWeight = if (isDown) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isDown) effectiveAccent else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isCountUpMode) effectiveAccent.copy(alpha = 0.18f) else Color.Transparent)
                            .then(
                                if (isCountUpMode) Modifier.border(0.8.dp, effectiveAccent.copy(alpha = 0.55f), RoundedCornerShape(50))
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
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                            .testTag("mode_pill_days_since")
                    ) {
                        Text(
                            text = "+ Days Since",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.5.sp,
                            fontWeight = if (isCountUpMode) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isCountUpMode) effectiveAccent else TextSecondary
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
    label: String,
    isHighlight: Boolean = false,
    highlightColor: Color = IceCyan
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = TabularMonoFontFamily,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) highlightColor else TextPrimary
        )
        Text(
            text = label,
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isHighlight) highlightColor else TextMuted
        )
    }
}

@Composable
private fun PulsingColon(alpha: Float, accentColor: Color) {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .offset(y = (-6).dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = ":",
            fontFamily = TabularMonoFontFamily,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor.copy(alpha = alpha)
        )
    }
}

@Composable
fun ModernPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    accentColor: Color = IceCyan,
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

    val resolvedAccent = if (isPrimary) accentColor else TextPrimary

    Box(
        modifier = modifier
            .scale(scale)
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(
                if (isPrimary) {
                    accentColor.copy(alpha = 0.16f)
                } else {
                    ObsidianGlass
                }
            )
            .border(
                width = 0.8.dp,
                color = if (isPrimary) accentColor.copy(alpha = 0.55f) else TitaniumHairline,
                shape = RoundedCornerShape(25.dp)
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
                fontSize = 14.5.sp,
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
    accentColor: Color = IceCyan,
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
    activeColor: Color = IceCyan,
    testTag: String = "modern_toggle"
) {
    val view = LocalView.current
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 3.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f),
        label = "thumb_offset"
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) activeColor.copy(alpha = 0.20f) else SpaceCard,
        label = "track_color"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) activeColor.copy(alpha = 0.6f) else TitaniumHairline,
        label = "border_color"
    )

    Box(
        modifier = modifier
            .width(50.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(trackColor)
            .border(0.8.dp, borderColor, RoundedCornerShape(14.dp))
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
                .background(if (checked) activeColor else TextMuted)
        )
    }
}

// Backward-compatibility alias
@Composable
fun CyberToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = IceCyan,
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
    accentColor: Color = IceCyan
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
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) accentColor.copy(alpha = 0.16f) else ObsidianGlass)
            .border(
                width = 0.8.dp,
                color = if (isSelected) accentColor.copy(alpha = 0.65f) else TitaniumHairline,
                shape = RoundedCornerShape(50)
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
    ) {
        Text(
            text = label,
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 12.5.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) accentColor else TextSecondary
        )
    }
}

/**
 * Subtle Frosted Action Icon Button for saved countdown cards.
 */
@Composable
fun FrostedActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    activeColor: Color = IceCyan,
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
            .clip(CircleShape)
            .background(
                if (isActive) activeColor.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.05f)
            )
            .border(
                width = 0.8.dp,
                color = if (isActive) activeColor.copy(alpha = 0.55f) else TitaniumHairline,
                shape = CircleShape
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
            tint = if (isActive) activeColor else TextSecondary,
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
                    color = IceCyan,
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
            containerColor = SpaceDark
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = SpaceDark,
                titleContentColor = IceCyan,
                headlineContentColor = TextPrimary,
                weekdayContentColor = TextSecondary,
                subheadContentColor = TextSecondary,
                yearContentColor = TextPrimary,
                currentYearContentColor = IceCyan,
                selectedYearContentColor = SpaceBlack,
                selectedYearContainerColor = IceCyan,
                dayContentColor = TextPrimary,
                disabledDayContentColor = TextMuted,
                selectedDayContentColor = SpaceBlack,
                selectedDayContainerColor = IceCyan,
                todayContentColor = WarmChampagne,
                todayDateBorderColor = WarmChampagne
            )
        )
    }
}
