package com.abhi.kshan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.kshan.engine.TemporalContextCardItem
import com.abhi.kshan.engine.TimeEngine
import com.abhi.kshan.ui.components.AmbientHeroResult
import com.abhi.kshan.ui.components.ConnectedDateCards
import com.abhi.kshan.ui.components.CyberDatePickerDialog
import com.abhi.kshan.ui.components.FrostedGlassCard
import com.abhi.kshan.ui.components.MechanicalScrubRuler
import com.abhi.kshan.ui.components.ModernCapsuleChip
import com.abhi.kshan.ui.theme.JetBrainsMonoFontFamily
import com.abhi.kshan.ui.theme.KshanBorder
import com.abhi.kshan.ui.theme.KshanCyan
import com.abhi.kshan.ui.theme.KshanPillShape
import com.abhi.kshan.ui.theme.KshanSurface
import com.abhi.kshan.ui.theme.MetricLabelTextStyle
import com.abhi.kshan.ui.theme.SpaceGroteskFontFamily
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.ui.theme.TextSecondary
import com.abhi.kshan.viewmodel.ChronoUiState
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun DifferenceScreen(
    uiState: ChronoUiState,
    onStartChanged: (LocalDate) -> Unit,
    onEndChanged: (LocalDate) -> Unit,
    onSwapDates: () -> Unit = {},
    onExcludeWeekendsChanged: (Boolean) -> Unit,
    onFocusPaceChanged: (Int) -> Unit = {},
    onSaveAsMilestone: (String, LocalDate) -> Unit = { _, _ -> },
    onHudInteraction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartPicker by rememberSaveable { mutableStateOf(false) }
    var showEndPicker by rememberSaveable { mutableStateOf(false) }
    var activeRulerTarget by rememberSaveable { mutableIntStateOf(1) } // Default scrub target = Horizon

    val formatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }
    val shortFormatter = remember { DateTimeFormatter.ofPattern("MMM d") }
    val res = uiState.differenceResult
    val today = remember { LocalDate.now() }

    val focusMetrics = remember(uiState.diffStart, uiState.diffEnd, uiState.selectedFocusHoursPerDay) {
        TimeEngine.calculateRunwayFocusMetrics(
            start = uiState.diffStart,
            end = uiState.diffEnd,
            hoursPerDay = uiState.selectedFocusHoursPerDay
        )
    }

    val constellation = remember(uiState.diffStart, uiState.diffEnd, res.daysCount) {
        TimeEngine.calculateTemporalConstellation(
            start = uiState.diffStart,
            end = uiState.diffEnd,
            daysCount = res.daysCount
        )
    }

    if (showStartPicker) {
        CyberDatePickerDialog(
            initialDate = uiState.diffStart,
            onDateSelected = { picked ->
                onStartChanged(picked)
                onHudInteraction("Origin set to ${picked.format(formatter)}")
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        CyberDatePickerDialog(
            initialDate = uiState.diffEnd,
            onDateSelected = { picked ->
                onEndChanged(picked)
                onHudInteraction("Horizon set to ${picked.format(formatter)}")
            },
            onDismiss = { showEndPicker = false }
        )
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.widthIn(max = 600.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Unified Single Horizon Split Card (24.dp #121620)
            ConnectedDateCards(
                startContent = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showStartPicker = true }
                            .testTag("diff_start_button"),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "FROM (ORIGIN)",
                            style = MetricLabelTextStyle
                        )
                        Text(
                            text = uiState.diffStart.format(formatter),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontFeatureSettings = "tnum",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = uiState.diffStart.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                },
                endContent = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEndPicker = true }
                            .testTag("diff_end_button"),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "TO (HORIZON)",
                            style = MetricLabelTextStyle
                        )
                        Text(
                            text = uiState.diffEnd.format(formatter),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontFeatureSettings = "tnum",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = uiState.diffEnd.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                },
                connectorLabel = "⇄",
                onConnectorClick = {
                    onSwapDates()
                    onHudInteraction("Swapped Origin & Horizon")
                }
            )

            // 2. Slim 52.dp Mechanical Scrubbing Ruler (Full-Pill 100%)
            val activeReadout = if (activeRulerTarget == 0) {
                uiState.diffStart.format(formatter)
            } else {
                uiState.diffEnd.format(formatter)
            }
            MechanicalScrubRuler(
                targetOptions = listOf("Origin", "Horizon"),
                selectedTargetIndex = activeRulerTarget,
                onTargetSelected = { index ->
                    activeRulerTarget = index
                    onHudInteraction(if (index == 0) "Scrubbing Origin" else "Scrubbing Horizon")
                },
                activeValueReadout = activeReadout,
                accentColor = KshanCyan,
                onStepDelta = { deltaDays ->
                    if (activeRulerTarget == 0) {
                        val updated = uiState.diffStart.plusDays(deltaDays.toLong())
                        onStartChanged(updated)
                        onHudInteraction("Origin → ${updated.format(formatter)}")
                    } else {
                        val updated = uiState.diffEnd.plusDays(deltaDays.toLong())
                        onEndChanged(updated)
                        onHudInteraction("Horizon → ${updated.format(formatter)}")
                    }
                },
                testTag = "diff_mechanical_ruler"
            )

            // 3. Smart Horizon Presets (Full-Pill Chips)
            val endOfMonth = remember(today) { YearMonth.from(today).atEndOfMonth() }
            val next30d = remember(today) { today.plusDays(30) }
            val next90d = remember(today) { today.plusDays(90) }
            val endOfYear = remember(today) { LocalDate.of(today.year, 12, 31) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModernCapsuleChip(
                    label = "End of Month",
                    isSelected = uiState.diffStart == today && uiState.diffEnd == endOfMonth,
                    onClick = {
                        onStartChanged(today)
                        onEndChanged(endOfMonth)
                        onHudInteraction("Horizon → End of Month")
                    },
                    testTag = "preset_end_of_month"
                )
                ModernCapsuleChip(
                    label = "Next 30d Sprint",
                    isSelected = uiState.diffStart == today && uiState.diffEnd == next30d,
                    onClick = {
                        onStartChanged(today)
                        onEndChanged(next30d)
                        onHudInteraction("Horizon → Next 30d Sprint")
                    },
                    testTag = "preset_next_30d_sprint"
                )
                ModernCapsuleChip(
                    label = "90d Quarter",
                    isSelected = uiState.diffStart == today && uiState.diffEnd == next90d,
                    onClick = {
                        onStartChanged(today)
                        onEndChanged(next90d)
                        onHudInteraction("Horizon → 90d Quarter")
                    },
                    testTag = "preset_90d_quarter"
                )
                ModernCapsuleChip(
                    label = "End of Year",
                    isSelected = uiState.diffStart == today && uiState.diffEnd == endOfYear,
                    onClick = {
                        onStartChanged(today)
                        onEndChanged(endOfYear)
                        onHudInteraction("Horizon → End of Year")
                    },
                    testTag = "preset_end_of_year"
                )
            }

            // 4. Skip Weekends Toggle Card (24.dp #121620)
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = null,
                            tint = KshanCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Skip Weekends",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = SpaceGroteskFontFamily
                                ),
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Count net working days (Mon–Fri) only",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = uiState.diffExcludeWeekends,
                        onCheckedChange = {
                            onExcludeWeekendsChanged(it)
                            onHudInteraction(if (it) "Net Working Runway active" else "Full Calendar Runway active")
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF070A12),
                            checkedTrackColor = KshanCyan,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = KshanSurface
                        ),
                        modifier = Modifier.testTag("diff_exclude_weekends")
                    )
                }
            }

            // 5. Hero Result OR Today's Live Kshan Card (when From == To)
            if (res.isSameDay) {
                val remSec = uiState.presentKshanTelemetry.secondsRemainingInDay.coerceAtLeast(0)
                val remHours = remSec / 3600
                val remMins = (remSec % 3600) / 60
                val wakingShort = "${remHours}h ${remMins}m"
                val wakingSentence = TimeEngine.formatTodayWakingRunway(remSec)

                FrostedGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("today_live_kshan_card")
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "TODAY'S LIVE KSHAN",
                            style = MetricLabelTextStyle
                        )

                        Text(
                            text = wakingShort,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontFeatureSettings = "tnum",
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp
                            ),
                            color = TextPrimary,
                            modifier = Modifier.testTag("diff_main_number")
                        )

                        Text(
                            text = wakingSentence,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                        ) {
                            ModernCapsuleChip(
                                label = "Month End",
                                isSelected = false,
                                onClick = {
                                    onStartChanged(today)
                                    onEndChanged(endOfMonth)
                                    onHudInteraction("Horizon → Month End")
                                }
                            )
                            ModernCapsuleChip(
                                label = "Next 30d Sprint",
                                isSelected = false,
                                onClick = {
                                    onStartChanged(today)
                                    onEndChanged(next30d)
                                    onHudInteraction("Horizon → Next 30d Sprint")
                                }
                            )
                            ModernCapsuleChip(
                                label = "Year End",
                                isSelected = false,
                                onClick = {
                                    onStartChanged(today)
                                    onEndChanged(endOfYear)
                                    onHudInteraction("Horizon → Year End")
                                }
                            )
                        }
                    }
                }
            } else {
                val mainDisplayLabel = if (uiState.diffExcludeWeekends) {
                    "NET WORKING RUNWAY"
                } else {
                    "AVAILABLE RUNWAY"
                }

                AmbientHeroResult(
                    mainValue = if (res.isPast && res.daysCount > 0) "-${res.daysCount}" else "${res.daysCount}",
                    label = mainDisplayLabel,
                    breakdownText = res.breakdownText,
                    testTag = "diff_main_number"
                )
            }

            // 6. Interactive Focus Hours Translator Card (24.dp #121620)
            FrostedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("diff_compact_metrics_strip")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Row 1: Calendar Breakdown in JetBrains Mono
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RUNWAY CAPACITY",
                            style = MetricLabelTextStyle
                        )
                        Text(
                            text = "${focusMetrics.totalCalendarDays * 24L} Total Hours",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontFeatureSettings = "tnum"
                            ),
                            color = TextMuted
                        )
                    }

                    Text(
                        text = focusMetrics.topRowText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontFeatureSettings = "tnum",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = TextPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Hairline divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(KshanBorder)
                    )

                    // Row 2: Selectable Daily Focus Pace Chips + Deep Focus Hours Readout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DAILY FOCUS PACE",
                            style = MetricLabelTextStyle
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(4, 6, 8).forEach { pace ->
                                val isSelected = uiState.selectedFocusHoursPerDay == pace
                                Box(
                                    modifier = Modifier
                                        .clip(KshanPillShape)
                                        .background(if (isSelected) KshanCyan else KshanSurface)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) KshanCyan else KshanBorder,
                                            shape = KshanPillShape
                                        )
                                        .clickable {
                                            onFocusPaceChanged(pace)
                                            onHudInteraction("Focus pace → ${pace}h/day")
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("focus_pace_$pace"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${pace}h/day",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontFeatureSettings = "tnum",
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isSelected) Color(0xFF070A12) else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Deep Focus Hours Readout + Save as Milestone CTA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = focusMetrics.focusLineText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontFeatureSettings = "tnum",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = KshanCyan,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("runway_deep_focus_hours")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(KshanPillShape)
                                .background(KshanSurface)
                                .border(1.dp, KshanCyan.copy(alpha = 0.45f), KshanPillShape)
                                .clickable {
                                    val milestoneTitle = "Runway (${uiState.diffEnd.format(shortFormatter)})"
                                    onSaveAsMilestone(milestoneTitle, uiState.diffEnd)
                                    onHudInteraction("Saved $milestoneTitle to Kshans")
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("runway_save_milestone_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.BookmarkAdd,
                                    contentDescription = null,
                                    tint = KshanCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Save as Milestone →",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = KshanCyan
                                )
                            }
                        }
                    }
                }
            }

            // 7. Human Rhythm 3-Card Carousel
            if (res.daysCount > 0) {
                HumanRhythmSection(cards = constellation.cards)
            }
        }
    }
}

@Composable
fun HumanRhythmSection(
    cards: List<TemporalContextCardItem>,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { cards.size })

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("human_rhythm_carousel"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = KshanCyan,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "HUMAN RHYTHM TRANSLATION",
                    style = MetricLabelTextStyle
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(cards.size) { index ->
                    val isCurrent = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(width = if (isCurrent) 14.dp else 5.dp, height = 5.dp)
                            .clip(KshanPillShape)
                            .background(if (isCurrent) KshanCyan else Color.White.copy(alpha = 0.20f))
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(end = 44.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val card = cards[page]
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = card.title,
                        style = MetricLabelTextStyle
                    )
                    Text(
                        text = card.primaryMetric,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontFeatureSettings = "tnum",
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = card.secondaryMetric,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontFeatureSettings = "tnum"
                        ),
                        color = KshanCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = card.poeticCaption,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
