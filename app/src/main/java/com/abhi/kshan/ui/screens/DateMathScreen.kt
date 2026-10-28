package com.abhi.kshan.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.kshan.engine.TimeEngine
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
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DateMathScreen(
    uiState: ChronoUiState,
    onBaseChanged: (LocalDate) -> Unit,
    onDaysChanged: (Long) -> Unit,
    onOperationChanged: (Boolean) -> Unit,
    onExcludeWeekendsChanged: (Boolean) -> Unit,
    onPinToKshans: (String, LocalDate) -> Unit = { _, _ -> },
    onHudInteraction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBasePicker by rememberSaveable { mutableStateOf(false) }
    var showDaysEditDialog by rememberSaveable { mutableStateOf(false) }
    var activeRulerTarget by rememberSaveable { mutableIntStateOf(1) } // 0 = Anchor, 1 = Sprint

    val shortFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }
    val heroDateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }
    val dayOfWeekFormatter = remember { DateTimeFormatter.ofPattern("EEEE") }

    val bufferMetrics = remember(uiState.mathBase, uiState.mathArrivalDate) {
        TimeEngine.calculatePlannerBufferMetrics(
            base = uiState.mathBase,
            arrival = uiState.mathArrivalDate
        )
    }

    val constellation = remember(uiState.mathBase, uiState.mathArrivalDate, bufferMetrics.totalCalendarDays) {
        TimeEngine.calculateTemporalConstellation(
            start = uiState.mathBase,
            end = uiState.mathArrivalDate,
            daysCount = bufferMetrics.totalCalendarDays
        )
    }

    if (showBasePicker) {
        CyberDatePickerDialog(
            initialDate = uiState.mathBase,
            onDateSelected = { picked ->
                onBaseChanged(picked)
                onHudInteraction("Anchor set to ${picked.format(shortFormatter)}")
            },
            onDismiss = { showBasePicker = false }
        )
    }

    if (showDaysEditDialog) {
        var textInput by rememberSaveable { mutableStateOf(uiState.mathDays.toString()) }
        AlertDialog(
            onDismissRequest = { showDaysEditDialog = false },
            containerColor = KshanSurface,
            title = {
                Text(
                    text = "Enter Sprint Duration",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
            },
            text = {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { str ->
                        val filtered = str.filter { it.isDigit() }.take(4)
                        textInput = filtered
                    },
                    label = { Text("Days (0 – 9999)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KshanCyan,
                        unfocusedBorderColor = KshanBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = KshanCyan,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("math_days_dialog_input")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = (textInput.toLongOrNull() ?: 0L).coerceIn(0L, 9999L)
                        onDaysChanged(parsed)
                        onHudInteraction("Sprint set to $parsed days")
                        showDaysEditDialog = false
                    }
                ) {
                    Text("Confirm", color = KshanCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDaysEditDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
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
            // 1. Unified Single Split Card: START ANCHOR | SPRINT DURATION
            val opSymbol = if (uiState.mathIsAdd) "+" else "−"
            ConnectedDateCards(
                startContent = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBasePicker = true }
                            .testTag("math_base_button"),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "START ANCHOR",
                            style = MetricLabelTextStyle
                        )
                        Text(
                            text = uiState.mathBase.format(shortFormatter),
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
                            text = uiState.mathBase.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                },
                endContent = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "SPRINT DURATION",
                            style = MetricLabelTextStyle
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(KshanPillShape)
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, KshanBorder, KshanPillShape)
                                    .clickable {
                                        val next = (uiState.mathDays - 1L).coerceIn(0L, 9999L)
                                        onDaysChanged(next)
                                        onHudInteraction("Sprint → $next days")
                                    }
                                    .testTag("math_days_minus"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease days",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "${uiState.mathDays}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontFeatureSettings = "tnum",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = TextPrimary,
                                modifier = Modifier
                                    .clickable { showDaysEditDialog = true }
                                    .padding(horizontal = 4.dp)
                                    .testTag("math_days_value")
                            )

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(KshanPillShape)
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, KshanBorder, KshanPillShape)
                                    .clickable {
                                        val next = (uiState.mathDays + 1L).coerceIn(0L, 9999L)
                                        onDaysChanged(next)
                                        onHudInteraction("Sprint → $next days")
                                    }
                                    .testTag("math_days_plus"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase days",
                                    tint = KshanCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "Tap number to edit",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                connectorLabel = opSymbol
            )

            // 2. Slim 52.dp Mechanical Scrubbing Ruler (Full-Pill 100%)
            val activeReadout = if (activeRulerTarget == 0) {
                uiState.mathBase.format(shortFormatter)
            } else {
                "${uiState.mathDays} days"
            }
            MechanicalScrubRuler(
                targetOptions = listOf("Anchor", "Sprint"),
                selectedTargetIndex = activeRulerTarget,
                onTargetSelected = { index ->
                    activeRulerTarget = index
                    onHudInteraction(if (index == 0) "Scrubbing Start Anchor" else "Scrubbing Sprint Duration")
                },
                activeValueReadout = activeReadout,
                accentColor = KshanCyan,
                currentNumericValue = if (activeRulerTarget == 1) uiState.mathDays else null,
                minValue = if (activeRulerTarget == 1) 0L else null,
                maxValue = if (activeRulerTarget == 1) 9999L else null,
                onStepDelta = { delta ->
                    if (activeRulerTarget == 0) {
                        val updated = uiState.mathBase.plusDays(delta.toLong())
                        onBaseChanged(updated)
                        onHudInteraction("Anchor → ${updated.format(shortFormatter)}")
                    } else {
                        val nextDays = (uiState.mathDays + delta.toLong()).coerceIn(0L, 9999L)
                        onDaysChanged(nextDays)
                        onHudInteraction("Sprint → $nextDays days")
                    }
                },
                testTag = "math_mechanical_ruler"
            )

            // 3. Real-World Sprint Chips (Full-Pill 100%)
            val presets = listOf(
                7L to "7d Sprint",
                14L to "14d Sprint",
                21L to "21d Habit",
                45L to "45d Block",
                90L to "90d Quarter"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { (days, label) ->
                    ModernCapsuleChip(
                        label = label,
                        isSelected = uiState.mathDays == days,
                        onClick = {
                            onDaysChanged(days)
                            onHudInteraction("Sprint → $label")
                        },
                        testTag = "math_preset_$days"
                    )
                }
            }

            // 4. Unified Segmented Direction Pill: [ Earlier (−) | Later (+) ]
            val isSub = !uiState.mathIsAdd
            val isAdd = uiState.mathIsAdd
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(KshanPillShape)
                    .background(KshanSurface)
                    .border(1.dp, KshanBorder, KshanPillShape)
                    .padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(KshanPillShape)
                            .background(if (isSub) KshanCyan else Color.Transparent)
                            .clickable(role = Role.Tab) {
                                onOperationChanged(false)
                                onHudInteraction("Mode: Earlier (−)")
                            }
                            .testTag("math_btn_sub"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Earlier (−)",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (isSub) Color(0xFF070A12) else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(KshanPillShape)
                            .background(if (isAdd) KshanCyan else Color.Transparent)
                            .clickable(role = Role.Tab) {
                                onOperationChanged(true)
                                onHudInteraction("Mode: Later (+)")
                            }
                            .testTag("math_btn_add"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Later (+)",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (isAdd) Color(0xFF070A12) else TextSecondary
                        )
                    }
                }
            }

            // 5. Skip Weekends Toggle Card (24.dp #121620)
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
                                text = "Advance across working days (Mon–Fri) only",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = uiState.mathExcludeWeekends,
                        onCheckedChange = {
                            onExcludeWeekendsChanged(it)
                            onHudInteraction(if (it) "Working-day sprint enabled" else "Calendar sprint enabled")
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF070A12),
                            checkedTrackColor = KshanCyan,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = KshanSurface
                        ),
                        modifier = Modifier.testTag("math_exclude_weekends")
                    )
                }
            }

            // 6. Hero Projected Target Date Readout with Subtle Orbital Ring + "+ Pin to Kshans"
            val arrival = uiState.mathArrivalDate
            val infiniteTransition = rememberInfiniteTransition(label = "plannerOrbit")
            val orbitAngle by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 28000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "orbitAngle"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Slow-rotating subtle monochrome orbital ring behind target date
                Canvas(
                    modifier = Modifier.size(190.dp)
                ) {
                    val radius = size.minDimension / 2f - 8.dp.toPx()
                    drawCircle(
                        color = Color.White.copy(alpha = 0.06f),
                        radius = radius,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawArc(
                        color = KshanCyan.copy(alpha = 0.28f),
                        startAngle = orbitAngle,
                        sweepAngle = 72f,
                        useCenter = false,
                        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                    val rad = Math.toRadians((orbitAngle + 36f).toDouble())
                    val dotX = center.x + (radius * cos(rad)).toFloat()
                    val dotY = center.y + (radius * sin(rad)).toFloat()
                    drawCircle(
                        color = KshanCyan,
                        radius = 3.dp.toPx(),
                        center = Offset(dotX, dotY)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    KshanCyan.copy(alpha = 0.07f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "PROJECTED TARGET DATE",
                        style = MetricLabelTextStyle
                    )

                    Text(
                        text = arrival.format(dayOfWeekFormatter).uppercase(),
                        style = MetricLabelTextStyle.copy(
                            color = KshanCyan
                        )
                    )

                    AnimatedContent(
                        targetState = arrival,
                        transitionSpec = {
                            (slideInVertically { h -> h / 2 } + fadeIn()).togetherWith(
                                slideOutVertically { h -> -h / 2 } + fadeOut()
                            )
                        },
                        label = "arrivalRoll"
                    ) { targetDate ->
                        Text(
                            text = targetDate.format(heroDateFormatter),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontFeatureSettings = "tnum",
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp
                            ),
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("math_calculated_arrival")
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 1-Tap "+ Pin to Kshans" Full-Pill Button
                    Box(
                        modifier = Modifier
                            .clip(KshanPillShape)
                            .background(KshanSurface)
                            .border(1.dp, KshanCyan.copy(alpha = 0.45f), KshanPillShape)
                            .clickable {
                                val title = "${uiState.mathDays}d Sprint (${arrival.format(shortFormatter)})"
                                onPinToKshans(title, arrival)
                                onHudInteraction("Pinned $title to Kshans")
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("planner_pin_to_kshans_button"),
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
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "+ Pin to Kshans",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = KshanCyan
                            )
                        }
                    }
                }
            }

            // 7. Working Days + Weekend Buffer Telemetry Card (24.dp #121620)
            val weekOfYear = arrival.get(ChronoField.ALIGNED_WEEK_OF_YEAR)
            val quarter = ((arrival.monthValue - 1) / 3) + 1
            FrostedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("planner_buffer_telemetry_card")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = bufferMetrics.formattedSummary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontFeatureSettings = "tnum",
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Week $weekOfYear  •  Day ${arrival.dayOfYear} of ${arrival.lengthOfYear()}  •  Q$quarter ${arrival.year}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontFeatureSettings = "tnum"
                        ),
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 8. Human Rhythm 3-Card Carousel
            if (bufferMetrics.totalCalendarDays > 0) {
                HumanRhythmSection(cards = constellation.cards)
            }
        }
    }
}
