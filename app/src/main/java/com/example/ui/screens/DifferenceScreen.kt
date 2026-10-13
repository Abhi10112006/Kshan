package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
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
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.components.ModernToggle
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ChronoUiState
import java.time.LocalDate

@Composable
fun DifferenceScreen(
    uiState: ChronoUiState,
    onStartChanged: (LocalDate) -> Unit,
    onEndChanged: (LocalDate) -> Unit,
    onExcludeWeekendsChanged: (Boolean) -> Unit,
    onHudInteraction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 22.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Date Selection Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Start Date Card
            FrostedGlassCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onHudInteraction("Tap to change your start date.")
                        showStartPicker = true
                    }
                    .testTag("diff_start_button"),
                borderColor = NeonCyan,
                glowAlpha = 0.5f,
                cornerRadius = 24.dp
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Start Date",
                            fontFamily = FontFamily.Default,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = uiState.diffStart.format(TimeEngine.SHORT_DATE_FORMATTER),
                        fontFamily = FontFamily.Default,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = uiState.diffStart.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontFamily = FontFamily.Default,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // End Date Card
            FrostedGlassCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onHudInteraction("Tap to change your destination date.")
                        showEndPicker = true
                    }
                    .testTag("diff_end_button"),
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
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = NeonPurple,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "End Date",
                            fontFamily = FontFamily.Default,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonPurple
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = uiState.diffEnd.format(TimeEngine.SHORT_DATE_FORMATTER),
                        fontFamily = FontFamily.Default,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = uiState.diffEnd.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontFamily = FontFamily.Default,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Preset Chips
        val chipsScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(chipsScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModernCapsuleChip(
                label = "Today",
                isSelected = uiState.diffEnd == LocalDate.now(),
                onClick = { onEndChanged(LocalDate.now()) }
            )
            ModernCapsuleChip(
                label = "+1 Week",
                isSelected = uiState.diffEnd == uiState.diffStart.plusWeeks(1),
                onClick = { onEndChanged(uiState.diffStart.plusWeeks(1)) }
            )
            ModernCapsuleChip(
                label = "+1 Month",
                isSelected = uiState.diffEnd == uiState.diffStart.plusMonths(1),
                onClick = { onEndChanged(uiState.diffStart.plusMonths(1)) }
            )
            ModernCapsuleChip(
                label = "+3 Months",
                isSelected = uiState.diffEnd == uiState.diffStart.plusMonths(3),
                onClick = { onEndChanged(uiState.diffStart.plusMonths(3)) }
            )
            ModernCapsuleChip(
                label = "End of Year",
                isSelected = uiState.diffEnd == LocalDate.of(uiState.diffStart.year, 12, 31),
                onClick = { onEndChanged(LocalDate.of(uiState.diffStart.year, 12, 31)) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Skip Weekends Switch Card
        FrostedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onExcludeWeekendsChanged(!uiState.diffExcludeWeekends)
                }
                .testTag("diff_weekends_toggle_row"),
            borderColor = if (uiState.diffExcludeWeekends) NeonCyan else SpaceCardBorder,
            glowAlpha = if (uiState.diffExcludeWeekends) 0.45f else 0.2f,
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
                            .background(if (uiState.diffExcludeWeekends) NeonCyan.copy(alpha = 0.2f) else SpaceCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkHistory,
                            contentDescription = null,
                            tint = if (uiState.diffExcludeWeekends) NeonCyan else TextMuted,
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
                            text = "Counts Monday to Friday only",
                            fontFamily = FontFamily.Default,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                ModernToggle(
                    checked = uiState.diffExcludeWeekends,
                    onCheckedChange = { onExcludeWeekendsChanged(it) },
                    testTag = "diff_weekends_toggle"
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Massive Result Hero Card
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = NeonCyan,
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
                    text = if (uiState.diffExcludeWeekends) "WORKING DAYS" else "TOTAL DAYS",
                    fontFamily = FontFamily.Default,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedContent(
                    targetState = uiState.differenceResult.daysCount,
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                    label = "diff_count_anim"
                ) { count ->
                    Text(
                        text = if (uiState.differenceResult.isSameDay) "0" else "$count",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        modifier = Modifier.testTag("diff_main_number")
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Friendly breakdown e.g. "2 months, 5 days apart"
                Text(
                    text = uiState.differenceResult.breakdownText,
                    fontFamily = FontFamily.Default,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = NeonCyan,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("diff_sub_result")
                )
            }
        }

        Spacer(modifier = Modifier.height(84.dp))
    }

    if (showStartPicker) {
        CyberDatePickerDialog(
            initialDate = uiState.diffStart,
            onDateSelected = onStartChanged,
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        CyberDatePickerDialog(
            initialDate = uiState.diffEnd,
            onDateSelected = onEndChanged,
            onDismiss = { showEndPicker = false }
        )
    }
}
