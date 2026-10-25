package com.abhi.kshan.ui.screens

import android.view.HapticFeedbackConstants
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.kshan.engine.TimeEngine
import com.abhi.kshan.ui.components.AmbientHeroResult
import com.abhi.kshan.ui.components.ConnectedDateCards
import com.abhi.kshan.ui.components.CyberDatePickerDialog
import com.abhi.kshan.ui.components.FrostedGlassCard
import com.abhi.kshan.ui.components.MechanicalScrubRuler
import com.abhi.kshan.ui.components.ModernCapsuleChip
import com.abhi.kshan.ui.components.ModernToggle
import com.abhi.kshan.ui.components.TemporalConstellationHero
import com.abhi.kshan.ui.theme.IceCyan
import com.abhi.kshan.ui.theme.SpaceCard
import com.abhi.kshan.ui.theme.SpaceGroteskFontFamily
import com.abhi.kshan.ui.theme.TabularMonoFontFamily
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.ui.theme.TextSecondary
import com.abhi.kshan.ui.theme.TitaniumHairline
import com.abhi.kshan.viewmodel.ChronoUiState
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
    val view = LocalView.current
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    // 0 = Scrub Start Date, 1 = Scrub End Date (defaults to End Date)
    var scrubTargetIndex by remember { mutableIntStateOf(1) }

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

    val activeScrubDate = if (scrubTargetIndex == 0) uiState.diffStart else uiState.diffEnd

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Connected Date Cards (Only active scrub target gets subtle Ice Cyan border)
        Box(modifier = Modifier.padding(horizontal = 22.dp)) {
            ConnectedDateCards(
                connectorLabel = "⇄",
                accentColor = IceCyan,
                startContent = {
                    val isStartSelected = scrubTargetIndex == 0
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                scrubTargetIndex = 0
                                onHudInteraction("Tap to change your start date.")
                                showStartPicker = true
                            }
                            .testTag("diff_start_button"),
                        borderColor = if (isStartSelected) IceCyan else TitaniumHairline,
                        glowAlpha = if (isStartSelected) 0.65f else 0.2f,
                        cornerRadius = 22.dp
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isStartSelected) IceCyan.copy(alpha = 0.14f)
                                            else SpaceCard
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = if (isStartSelected) IceCyan else TextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Start Date",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isStartSelected) IceCyan else TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = uiState.diffStart.format(TimeEngine.SHORT_DATE_FORMATTER),
                                fontFamily = TabularMonoFontFamily,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = uiState.diffStart.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                endContent = {
                    val isEndSelected = scrubTargetIndex == 1
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                scrubTargetIndex = 1
                                onHudInteraction("Tap to change your destination date.")
                                showEndPicker = true
                            }
                            .testTag("diff_end_button"),
                        borderColor = if (isEndSelected) IceCyan else TitaniumHairline,
                        glowAlpha = if (isEndSelected) 0.65f else 0.2f,
                        cornerRadius = 22.dp
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isEndSelected) IceCyan.copy(alpha = 0.14f)
                                            else SpaceCard
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Flag,
                                        contentDescription = null,
                                        tint = if (isEndSelected) IceCyan else TextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "End Date",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isEndSelected) IceCyan else TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = uiState.diffEnd.format(TimeEngine.SHORT_DATE_FORMATTER),
                                fontFamily = TabularMonoFontFamily,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = uiState.diffEnd.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Compact 52.dp Frameless Crown Scrubber
        Box(modifier = Modifier.padding(horizontal = 22.dp)) {
            MechanicalScrubRuler(
                targetOptions = listOf("Start", "End"),
                selectedTargetIndex = scrubTargetIndex,
                onTargetSelected = { idx ->
                    scrubTargetIndex = idx
                    onHudInteraction(
                        if (idx == 0) "Crown Scrubber focused on Start Date."
                        else "Crown Scrubber focused on End Date."
                    )
                },
                activeValueReadout = activeScrubDate.format(TimeEngine.SHORT_DATE_FORMATTER),
                accentColor = IceCyan,
                onStepDelta = { deltaDays ->
                    if (scrubTargetIndex == 0) {
                        onStartChanged(uiState.diffStart.plusDays(deltaDays.toLong()))
                    } else {
                        onEndChanged(uiState.diffEnd.plusDays(deltaDays.toLong()))
                    }
                },
                testTag = "diff_mechanical_ruler"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Preset Chips with Edge-to-Edge scrolling
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 22.dp),
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

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Skip Weekends Switch Card
        Box(modifier = Modifier.padding(horizontal = 22.dp)) {
            FrostedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onExcludeWeekendsChanged(!uiState.diffExcludeWeekends)
                    }
                    .testTag("diff_weekends_toggle_row"),
                borderColor = if (uiState.diffExcludeWeekends) IceCyan else TitaniumHairline,
                glowAlpha = if (uiState.diffExcludeWeekends) 0.6f else 0.2f,
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
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (uiState.diffExcludeWeekends) IceCyan.copy(alpha = 0.16f) else SpaceCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkHistory,
                                contentDescription = null,
                                tint = if (uiState.diffExcludeWeekends) IceCyan else TextMuted,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Skip Weekends",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Counts Monday to Friday only",
                                fontFamily = SpaceGroteskFontFamily,
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

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Clean Hero Readout + Single-Line 3-Metric Strip ("2w 6d • 14 Workdays • 480 Hours")
        TemporalConstellationHero(
            startDate = uiState.diffStart,
            endDate = uiState.diffEnd,
            daysCount = uiState.differenceResult.daysCount,
            accentColor = IceCyan
        ) {
            AnimatedContent(
                targetState = uiState.differenceResult.daysCount,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                label = "diff_count_anim"
            ) { count ->
                val displayCount = if (uiState.differenceResult.isSameDay) "0" else "$count"
                AmbientHeroResult(
                    mainValue = displayCount,
                    label = if (uiState.diffExcludeWeekends) "Working Days" else "Total Days",
                    breakdownText = uiState.differenceResult.breakdownText,
                    accentColor = IceCyan,
                    testTag = "diff_main_number"
                )
            }
        }

        // 6. Bottom Spacer for Floating Glass Dock
        Spacer(modifier = Modifier.height(110.dp))
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
