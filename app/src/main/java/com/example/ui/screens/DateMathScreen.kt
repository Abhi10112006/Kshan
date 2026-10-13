package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.TimeEngine
import com.example.ui.components.CyberDatePickerDialog
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.ModernCapsuleChip
import com.example.ui.components.ModernPillButton
import com.example.ui.components.ModernToggle
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ChronoUiState
import java.time.LocalDate

@Composable
fun DateMathScreen(
    uiState: ChronoUiState,
    onBaseChanged: (LocalDate) -> Unit,
    onDaysChanged: (Long) -> Unit,
    onOperationChanged: (Boolean) -> Unit,
    onExcludeWeekendsChanged: (Boolean) -> Unit,
    onHudInteraction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBasePicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 22.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pick a Date & Days to Shift Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Pick a Date
            FrostedGlassCard(
                modifier = Modifier
                    .weight(1.1f)
                    .clickable {
                        onHudInteraction("Tap to select your base starting date.")
                        showBasePicker = true
                    }
                    .testTag("math_base_button"),
                borderColor = NeonPurple,
                glowAlpha = 0.5f,
                cornerRadius = 24.dp
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(NeonPurple.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = NeonPurple,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Starting Date",
                            fontFamily = FontFamily.Default,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonPurple
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = uiState.mathBase.format(TimeEngine.SHORT_DATE_FORMATTER),
                        fontFamily = FontFamily.Default,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = uiState.mathBase.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontFamily = FontFamily.Default,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Days Counter Card
            FrostedGlassCard(
                modifier = Modifier.weight(0.9f),
                borderColor = if (uiState.mathIsAdd) NeonCyan else NeonPink,
                glowAlpha = 0.5f,
                cornerRadius = 24.dp
            ) {
                Column {
                    Text(
                        text = "Days to Shift",
                        fontFamily = FontFamily.Default,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (uiState.mathIsAdd) NeonCyan else NeonPink
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${uiState.mathDays}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.testTag("math_days_value")
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SpaceCard)
                                    .clickable {
                                        if (uiState.mathDays > 0) onDaysChanged(uiState.mathDays - 1)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Minus",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SpaceCard)
                                    .clickable {
                                        onDaysChanged(uiState.mathDays + 1)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Plus",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Preset Chips (+1 Wk, +2 Wks, +1 Mo, +3 Mo, +1 Yr)
        val chipsScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(chipsScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModernCapsuleChip(
                label = "+1 Week",
                isSelected = uiState.mathDays == 7L,
                onClick = { onDaysChanged(7) }
            )
            ModernCapsuleChip(
                label = "+2 Weeks",
                isSelected = uiState.mathDays == 14L,
                onClick = { onDaysChanged(14) }
            )
            ModernCapsuleChip(
                label = "+1 Month (30d)",
                isSelected = uiState.mathDays == 30L,
                onClick = { onDaysChanged(30) }
            )
            ModernCapsuleChip(
                label = "+3 Months (90d)",
                isSelected = uiState.mathDays == 90L,
                onClick = { onDaysChanged(90) }
            )
            ModernCapsuleChip(
                label = "+1 Year (365d)",
                isSelected = uiState.mathDays == 365L,
                onClick = { onDaysChanged(365) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Directional Shift Buttons: Earlier (-) vs Later (+)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ModernPillButton(
                text = "Earlier (-)",
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                onClick = { onOperationChanged(false) },
                isPrimary = !uiState.mathIsAdd,
                accentColor = NeonPink,
                modifier = Modifier
                    .weight(1f)
                    .testTag("math_btn_sub")
            )

            ModernPillButton(
                text = "Later (+)",
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = { onOperationChanged(true) },
                isPrimary = uiState.mathIsAdd,
                accentColor = NeonCyan,
                modifier = Modifier
                    .weight(1f)
                    .testTag("math_btn_add")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Skip Weekends Switch Card
        FrostedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onExcludeWeekendsChanged(!uiState.mathExcludeWeekends)
                }
                .testTag("math_weekends_toggle_row"),
            borderColor = if (uiState.mathExcludeWeekends) NeonCyan else SpaceCardBorder,
            glowAlpha = if (uiState.mathExcludeWeekends) 0.45f else 0.2f,
            cornerRadius = 20.dp
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (uiState.mathExcludeWeekends) NeonCyan.copy(alpha = 0.2f) else SpaceCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkHistory,
                            contentDescription = null,
                            tint = if (uiState.mathExcludeWeekends) NeonCyan else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Skip Weekends",
                            fontFamily = FontFamily.Default,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Counts working days only",
                            fontFamily = FontFamily.Default,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                ModernToggle(
                    checked = uiState.mathExcludeWeekends,
                    onCheckedChange = { onExcludeWeekendsChanged(it) },
                    testTag = "math_weekends_toggle"
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Hero Result Date Card
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (uiState.mathIsAdd) NeonCyan else NeonPink,
            glowAlpha = 0.4f,
            cornerRadius = 32.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "RESULT DATE",
                    fontFamily = FontFamily.Default,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                AnimatedContent(
                    targetState = uiState.mathArrivalDate,
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                    label = "arrival_anim"
                ) { arrivalDate ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = arrivalDate.format(TimeEngine.FULL_DATE_FORMATTER),
                            fontFamily = FontFamily.Default,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("math_calculated_arrival")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val shiftDesc = if (uiState.mathExcludeWeekends) {
                            "${uiState.mathDays} working days ${if (uiState.mathIsAdd) "later" else "earlier"}"
                        } else {
                            "${uiState.mathDays} days ${if (uiState.mathIsAdd) "later" else "earlier"}"
                        }

                        Text(
                            text = shiftDesc,
                            fontFamily = FontFamily.Default,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (uiState.mathIsAdd) NeonCyan else NeonPink
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(84.dp))
    }

    if (showBasePicker) {
        CyberDatePickerDialog(
            initialDate = uiState.mathBase,
            onDateSelected = onBaseChanged,
            onDismiss = { showBasePicker = false }
        )
    }
}
