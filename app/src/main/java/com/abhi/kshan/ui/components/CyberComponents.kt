package com.abhi.kshan.ui.components

import android.util.Log
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
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import android.view.HapticFeedbackConstants
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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.kshan.ui.theme.NeonCyan
import com.abhi.kshan.ui.theme.NeonEmerald
import com.abhi.kshan.ui.theme.NeonPink
import com.abhi.kshan.ui.theme.NeonPurple
import com.abhi.kshan.ui.theme.SpaceBlack
import com.abhi.kshan.ui.theme.SpaceCard
import com.abhi.kshan.ui.theme.SpaceCardBorder
import com.abhi.kshan.ui.theme.SpaceDark
import com.abhi.kshan.ui.theme.ComicNeueFontFamily
import com.abhi.kshan.ui.theme.CourierPrimeFontFamily
import com.abhi.kshan.ui.theme.TabularMonoFontFamily
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.ui.theme.TextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Top-Lit Specular Obsidian Glass Card.
 * Replaces uniform solid borders with directional specular gradient highlights
 * and a subtle top-center radial illumination.
 */
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = NeonCyan,
    glowAlpha: Float = 0.35f,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "card_press_scale"
    )

    val obsidianBase = Color(0xFF0B101E).copy(alpha = 0.85f)
    val specularBorderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.22f),
            borderColor.copy(alpha = (glowAlpha * 0.7f).coerceIn(0.12f, 0.45f)),
            Color.Transparent
        ),
        start = Offset(0f, 0f),
        end = Offset(400f, 400f)
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(cornerRadius))
            .background(obsidianBase)
            // Top-center ambient radial glow
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        borderColor.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(250f, 0f),
                    radius = 350f
                )
            )
            .border(
                width = 1.2.dp,
                brush = specularBorderBrush,
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(20.dp),
        content = content
    )
}

// Backward-compatibility alias
@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = SpaceCardBorder,
    glowAlpha: Float = 0.4f,
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
 * Connected Date Cards with floating center badge (⇄ or →).
 * Connects the Start Date and End Date cards visually without uniform box clutter.
 */
@Composable
fun ConnectedDateCards(
    startContent: @Composable () -> Unit,
    endContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    connectorIcon: ImageVector? = null,
    connectorLabel: String = "⇄",
    accentColor: Color = NeonCyan
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
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

        // Floating Overlapping Center Connector Badge
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F172A))
                .border(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        listOf(Color.White.copy(0.35f), accentColor, Color.Transparent)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (connectorIcon != null) {
                Icon(
                    imageVector = connectorIcon,
                    contentDescription = "Connector",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = connectorLabel,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
        }
    }
}

/**
 * Ambient Hero Result Readout.
 * Eliminates heavy rectangular borders and replaces them with an ambient radial aura
 * and oversized 72.sp Monospace numbers with a sleek pill capsule metadata badge.
 */
@Composable
fun AmbientHeroResult(
    mainValue: String,
    label: String,
    breakdownText: String,
    modifier: Modifier = Modifier,
    accentColor: Color = NeonCyan,
    testTag: String = "hero_main_number"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ambient Radial Aura
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.16f),
                            accentColor.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Label e.g. "WORKING DAYS" / "TOTAL DAYS"
            Text(
                text = label.uppercase(),
                fontFamily = ComicNeueFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Oversized Monospace Readout
            Text(
                text = mainValue,
                fontFamily = TabularMonoFontFamily,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.testTag(testTag)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Sleek Capsule Metadata Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF0F172A).copy(alpha = 0.9f))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(accentColor.copy(0.5f), Color.White.copy(0.2f), accentColor.copy(0.4f))
                        ),
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Text(
                    text = breakdownText,
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Unified Hero Orbital Timepiece Card.
 * Replaces the 4 isolated dark boxes with a single panoramic chronometer featuring
 * an animated progress arc along the top edge, giant monospace digits, and pulsing colons.
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
    accentColor: Color = NeonCyan,
    isCountUpMode: Boolean = false,
    isAutoRolled: Boolean = false,
    targetYear: Int? = null,
    onToggleCountUpMode: ((Boolean) -> Unit)? = null
) {
    val view = LocalView.current
    val infiniteTransition = rememberInfiniteTransition(label = "timepiece_colon")
    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colon_blink"
    )

    FrostedGlassCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = accentColor,
        glowAlpha = 0.5f,
        cornerRadius = 28.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated glowing progress arc strip along top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                accentColor,
                                Color.White.copy(0.8f),
                                accentColor,
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Category, Auto-Rollover Badge & Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = categoryName.uppercase(),
                        fontFamily = ComicNeueFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = accentColor
                    )
                }

                if (isAutoRolled && targetYear != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonEmerald.copy(alpha = 0.16f))
                            .border(1.dp, NeonEmerald.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "↻ Next: $targetYear",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonEmerald
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = statusText,
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Event Title
            Text(
                text = eventTitle,
                fontFamily = ComicNeueFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
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
                PulsingColon(alpha = colonAlpha, accentColor = accentColor)
                DigitBlock(value = String.format("%02d", hours), label = "HOURS")
                PulsingColon(alpha = colonAlpha, accentColor = accentColor)
                DigitBlock(value = String.format("%02d", minutes), label = "MINS")
                PulsingColon(alpha = colonAlpha, accentColor = accentColor)
                DigitBlock(value = String.format("%02d", seconds), label = "SECS", isHighlight = true, highlightColor = accentColor)
            }

            if (onToggleCountUpMode != null) {
                Spacer(modifier = Modifier.height(14.dp))

                // 1-Tap Count Down (−) | Days Since (+) Mode Selector Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(SpaceDark.copy(alpha = 0.9f))
                        .border(1.dp, SpaceCardBorder, RoundedCornerShape(50))
                        .padding(3.dp)
                        .testTag("countdown_mode_selector"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isDown = !isCountUpMode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isDown) accentColor.copy(alpha = 0.22f) else Color.Transparent)
                            .then(
                                if (isDown) Modifier.border(1.dp, accentColor.copy(alpha = 0.65f), RoundedCornerShape(50))
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
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 11.5.sp,
                            fontWeight = if (isDown) FontWeight.Bold else FontWeight.Medium,
                            color = if (isDown) accentColor else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isCountUpMode) accentColor.copy(alpha = 0.22f) else Color.Transparent)
                            .then(
                                if (isCountUpMode) Modifier.border(1.dp, accentColor.copy(alpha = 0.65f), RoundedCornerShape(50))
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
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 11.5.sp,
                            fontWeight = if (isCountUpMode) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCountUpMode) accentColor else TextSecondary
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
    highlightColor: Color = NeonCyan
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
            fontFamily = ComicNeueFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
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
    accentColor: Color = NeonCyan,
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

    Box(
        modifier = modifier
            .scale(scale)
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                if (isPrimary) {
                    Brush.horizontalGradient(
                        listOf(
                            accentColor.copy(alpha = 0.25f),
                            accentColor.copy(alpha = 0.12f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            SpaceCard.copy(alpha = 0.8f),
                            SpaceDark.copy(alpha = 0.8f)
                        )
                    )
                }
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        accentColor.copy(alpha = if (isPrimary) 0.75f else 0.35f),
                        accentColor.copy(alpha = if (isPrimary) 0.45f else 0.15f)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    } catch (e: Exception) { Log.w("CyberComponents", "Haptic feedback unavailable", e) }
                    onClick()
                }
            )
            .padding(horizontal = 24.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontFamily = ComicNeueFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp,
                color = accentColor
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
    accentColor: Color = NeonCyan,
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
    activeColor: Color = NeonCyan,
    testTag: String = "modern_toggle"
) {
    val view = LocalView.current
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 2.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f),
        label = "thumb_offset"
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) activeColor.copy(alpha = 0.25f) else SpaceCard,
        label = "track_color"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) activeColor else SpaceCardBorder,
        label = "border_color"
    )

    Box(
        modifier = modifier
            .width(52.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(trackColor)
            .border(1.dp, borderColor, RoundedCornerShape(15.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                try {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                } catch (e: Exception) { Log.w("CyberComponents", "Haptic feedback unavailable", e) }
                onCheckedChange(!checked)
            }
            .testTag(testTag),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(24.dp)
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
    activeColor: Color = NeonCyan,
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
    accentColor: Color = NeonCyan
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label = "chip_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) accentColor.copy(alpha = 0.2f) else SpaceCard.copy(alpha = 0.7f))
            .border(
                1.dp,
                if (isSelected) accentColor else SpaceCardBorder,
                RoundedCornerShape(50)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    } catch (e: Exception) { Log.w("CyberComponents", "Haptic feedback unavailable", e) }
                    onClick()
                }
            )
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = label,
            fontFamily = ComicNeueFontFamily,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) accentColor else TextSecondary
        )
    }
}

/**
 * Subtle Frosted Action Icon Button for saved countdown cards.
 * Replaces harsh solid red/grey icons with soft frosted glass buttons.
 */
@Composable
fun FrostedActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    activeColor: Color = NeonCyan,
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
                if (isActive) activeColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f)
            )
            .border(
                width = 1.dp,
                color = if (isActive) activeColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    } catch (e: Exception) { Log.w("CyberComponents", "Haptic feedback unavailable", e) }
                    onClick()
                }
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) activeColor else Color.White.copy(alpha = 0.55f),
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
                    } catch (e: Exception) { Log.w("CyberComponents", "Haptic feedback unavailable", e) }
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
                    color = NeonCyan,
                    fontFamily = ComicNeueFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    } catch (e: Exception) { Log.w("CyberComponents", "Haptic feedback unavailable", e) }
                    onDismiss()
                }
            ) {
                Text(
                    text = "Cancel",
                    color = TextMuted,
                    fontFamily = ComicNeueFontFamily,
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
                titleContentColor = NeonCyan,
                headlineContentColor = TextPrimary,
                weekdayContentColor = TextSecondary,
                subheadContentColor = TextSecondary,
                yearContentColor = TextPrimary,
                currentYearContentColor = NeonCyan,
                selectedYearContentColor = SpaceBlack,
                selectedYearContainerColor = NeonCyan,
                dayContentColor = TextPrimary,
                disabledDayContentColor = TextMuted,
                selectedDayContentColor = SpaceBlack,
                selectedDayContainerColor = NeonCyan,
                todayContentColor = NeonPurple,
                todayDateBorderColor = NeonPurple
            )
        )
    }
}
