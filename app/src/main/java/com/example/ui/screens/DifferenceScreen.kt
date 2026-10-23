package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.TimeEngine
import com.example.ui.components.AmbientHeroResult
import com.example.ui.components.ConnectedDateCards
import com.example.ui.components.CyberDatePickerDialog
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.ModernCapsuleChip
import com.example.ui.components.ModernToggle
import com.example.ui.theme.ComicNeueFontFamily
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.TabularMonoFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ChronoUiState
import java.time.LocalDate

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.platform.LocalView

@Composable
fun DifferenceScreen(
    uiState: ChronoUiState,
    onStartChanged: (LocalDate) -> Unit,
    onEndChanged: (LocalDate) -> Unit,
    onExcludeWeekendsChanged: (Boolean) -> Unit,
    onHudInteraction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val presets = remember(uiState.diffStart) {
        listOf(
            Pair("Today", LocalDate.now()),
            Pair("+1 Week", uiState.diffStart.plusWeeks(1)),
            Pair("+1 Month", uiState.diffStart.plusMonths(1)),
            Pair("+3 Months", uiState.diffStart.plusMonths(3)),
            Pair("End of Year", LocalDate.of(uiState.diffStart.year, 12, 31))
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. Connected Date Cards with Floating Center ⇄ Badge
        Box(modifier = Modifier.padding(horizontal = 22.dp)) {
            ConnectedDateCards(
                connectorLabel = "⇄",
                accentColor = NeonCyan,
                startContent = {
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
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
                                    fontFamily = ComicNeueFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeonCyan
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = uiState.diffStart.format(TimeEngine.SHORT_DATE_FORMATTER),
                                fontFamily = TabularMonoFontFamily,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = uiState.diffStart.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                endContent = {
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
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
                                    fontFamily = ComicNeueFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeonPurple
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = uiState.diffEnd.format(TimeEngine.SHORT_DATE_FORMATTER),
                                fontFamily = TabularMonoFontFamily,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = uiState.diffEnd.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Preset Chips with Edge-to-Edge scrolling (PaddingValues horizontal = 24.dp)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presets) { (label, targetDate) ->
                ModernCapsuleChip(
                    label = label,
                    isSelected = uiState.diffEnd == targetDate,
                    onClick = { onEndChanged(targetDate) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Skip Weekends Switch Card
        Box(modifier = Modifier.padding(horizontal = 22.dp)) {
            FrostedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
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
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Counts Monday to Friday only",
                                fontFamily = ComicNeueFontFamily,
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
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Ambient Hero Result (Eliminating uniform box outline, rendering luminous ambient aura)
        Box(modifier = Modifier.padding(horizontal = 22.dp)) {
            AnimatedContent(
                targetState = uiState.differenceResult.daysCount,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "diff_count_anim"
            ) { count ->
                val displayCount = if (uiState.differenceResult.isSameDay) "0" else "$count"
                AmbientHeroResult(
                    mainValue = displayCount,
                    label = if (uiState.diffExcludeWeekends) "Working Days" else "Total Days",
                    breakdownText = uiState.differenceResult.breakdownText,
                    accentColor = NeonCyan,
                    testTag = "diff_main_number"
                )
            }
        }

        // 5. 120.dp Bottom Spacer to prevent navigation dock collision
        Spacer(modifier = Modifier.height(120.dp))
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
