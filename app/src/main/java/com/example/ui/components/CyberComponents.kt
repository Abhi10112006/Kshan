package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = SpaceCardBorder,
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

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SpaceCard.copy(alpha = 0.88f),
                        SpaceDark.copy(alpha = 0.92f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        borderColor.copy(alpha = glowAlpha),
                        borderColor.copy(alpha = glowAlpha * 0.3f)
                    )
                ),
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
                            accentColor.copy(alpha = 0.22f),
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
                onClick = onClick
            )
            .padding(horizontal = 24.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontFamily = FontFamily.Default,
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
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Default,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) accentColor else TextSecondary
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
    val initialEpochMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialEpochMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
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
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = TextMuted,
                    fontFamily = FontFamily.Default,
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
