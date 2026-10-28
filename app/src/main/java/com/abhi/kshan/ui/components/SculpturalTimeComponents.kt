package com.abhi.kshan.ui.components

import android.os.SystemClock
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.kshan.data.ChronoEvent
import com.abhi.kshan.engine.TimeEngine
import com.abhi.kshan.ui.theme.JetBrainsMonoFontFamily
import com.abhi.kshan.ui.theme.KshanBackground
import com.abhi.kshan.ui.theme.KshanBorder
import com.abhi.kshan.ui.theme.KshanCyan
import com.abhi.kshan.ui.theme.KshanPillShape
import com.abhi.kshan.ui.theme.KshanSurface
import com.abhi.kshan.ui.theme.SpaceGroteskFontFamily
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.ui.theme.TextSecondary
import com.abhi.kshan.ui.theme.VisionGold
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.math.abs

/**
 * Compact 52.dp Full-Pill Horizontal Crown Scrubber ("Leica / Swiss Watch Crown").
 *
 * Strict 4-Color, 2-Radius Design System:
 * - Outer container: 52.dp height, #121620 surface, 1.dp Color.White.copy(alpha = 0.08f) border, RoundedCornerShape(100)
 * - Left target selector pill: RoundedCornerShape(100) with #2ABFFF active highlight
 * - Preserves sub-tick spring snap-back, 0..9999 hard boundary clamping, 16ms haptic rate-limiting, and TalkBack semantics.
 */
@Composable
fun MechanicalScrubRuler(
    targetOptions: List<String>,
    selectedTargetIndex: Int,
    onTargetSelected: (Int) -> Unit,
    activeValueReadout: String,
    accentColor: Color,
    onStepDelta: (Int) -> Unit,
    modifier: Modifier = Modifier,
    currentNumericValue: Long? = null,
    minValue: Long? = null,
    maxValue: Long? = null,
    testTag: String = "mechanical_scrub_ruler"
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val density = LocalDensity.current
    val stepPx = remember(density) { with(density) { 12.dp.toPx() } }

    val currentOnStepDelta by rememberUpdatedState(onStepDelta)
    val latestNumericValue by rememberUpdatedState(currentNumericValue)
    val latestMinValue by rememberUpdatedState(minValue)
    val latestMaxValue by rememberUpdatedState(maxValue)

    var dragAccumulatorPx by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var committedTicks by remember { mutableIntStateOf(0) }
    var lastHapticUptimeMs by remember { mutableLongStateOf(0L) }

    val subTickVisualOffsetPx by animateFloatAsState(
        targetValue = if (isDragging) (dragAccumulatorPx % stepPx) else 0f,
        animationSpec = if (isDragging) {
            spring(dampingRatio = 1f, stiffness = 8000f)
        } else {
            spring(dampingRatio = 0.72f, stiffness = 650f)
        },
        label = "scrubber_sub_tick_snap"
    )

    fun fireThrottledMechanicalTick() {
        val nowMs = SystemClock.uptimeMillis()
        if (nowMs - lastHapticUptimeMs < 16L) return
        lastHapticUptimeMs = nowMs
        try {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (e: Exception) {
            try {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            } catch (ignored: Exception) {
                Log.w("MechanicalScrubRuler", "Haptic tick unavailable", ignored)
            }
        }
    }

    val selectedTargetName = targetOptions.getOrElse(selectedTargetIndex) { "Date" }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(KshanPillShape)
            .background(KshanSurface)
            .border(
                width = 1.dp,
                color = KshanBorder,
                shape = KshanPillShape
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .semantics {
                contentDescription = "Crown Scrubber for $selectedTargetName"
                stateDescription = activeValueReadout
                setProgress { targetValue ->
                    val delta = if (targetValue > 0f) 1 else -1
                    currentOnStepDelta(delta)
                    true
                }
                customActions = listOf(
                    CustomAccessibilityAction("Increase $selectedTargetName by 1 day") {
                        currentOnStepDelta(1)
                        true
                    },
                    CustomAccessibilityAction("Decrease $selectedTargetName by 1 day") {
                        currentOnStepDelta(-1)
                        true
                    }
                )
            }
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Compact Full-Pill Target Selector
        Row(
            modifier = Modifier
                .clip(KshanPillShape)
                .background(KshanBackground)
                .border(1.dp, KshanBorder, KshanPillShape)
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            targetOptions.forEachIndexed { idx, rawLabel ->
                val shortLabel = when {
                    rawLabel.startsWith("Origin", ignoreCase = true) -> "Origin"
                    rawLabel.startsWith("Horizon", ignoreCase = true) -> "Horizon"
                    rawLabel.startsWith("Anchor", ignoreCase = true) -> "Anchor"
                    rawLabel.startsWith("Sprint", ignoreCase = true) -> "Sprint"
                    rawLabel.startsWith("Start", ignoreCase = true) -> "Origin"
                    rawLabel.startsWith("End", ignoreCase = true) -> "Horizon"
                    rawLabel.startsWith("Base", ignoreCase = true) -> "Anchor"
                    rawLabel.startsWith("Days", ignoreCase = true) || rawLabel.startsWith("Shift", ignoreCase = true) -> "Sprint"
                    else -> rawLabel
                }
                val isSelected = idx == selectedTargetIndex
                val pillBg by animateColorAsState(
                    targetValue = if (isSelected) KshanCyan.copy(alpha = 0.16f) else Color.Transparent,
                    animationSpec = tween(160),
                    label = "scrub_target_bg"
                )
                Box(
                    modifier = Modifier
                        .clip(KshanPillShape)
                        .background(pillBg)
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    1.dp,
                                    KshanCyan.copy(alpha = 0.6f),
                                    KshanPillShape
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clickable {
                            fireThrottledMechanicalTick()
                            onTargetSelected(idx)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("scrub_target_option_$idx")
                ) {
                    Text(
                        text = shortLabel,
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) KshanCyan else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Full-Pill Horizontal Titanium Dial Strip with Center White Laser Needle
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(KshanPillShape)
                .background(KshanBackground)
                .pointerInput(selectedTargetIndex, stepPx) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                            dragAccumulatorPx = 0f
                        },
                        onDragEnd = {
                            isDragging = false
                            dragAccumulatorPx = 0f
                        },
                        onDragCancel = {
                            isDragging = false
                            dragAccumulatorPx = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()

                            val proposedDelta = -dragAmount
                            val curVal = latestNumericValue
                            val minVal = latestMinValue
                            val maxVal = latestMaxValue

                            val isBlockedAtMin = curVal != null && minVal != null && curVal <= minVal && (dragAccumulatorPx + proposedDelta) < 0f
                            val isBlockedAtMax = curVal != null && maxVal != null && curVal >= maxVal && (dragAccumulatorPx + proposedDelta) > 0f

                            if (isBlockedAtMin || isBlockedAtMax) {
                                dragAccumulatorPx = 0f
                                return@detectHorizontalDragGestures
                            }

                            dragAccumulatorPx += proposedDelta
                            val rawSteps = (dragAccumulatorPx / stepPx).toInt()
                            if (rawSteps != 0) {
                                val allowedSteps = if (curVal != null) {
                                    val minAllowed = if (minVal != null) (minVal - curVal).toInt() else Int.MIN_VALUE
                                    val maxAllowed = if (maxVal != null) (maxVal - curVal).toInt() else Int.MAX_VALUE
                                    rawSteps.coerceIn(minAllowed, maxAllowed)
                                } else {
                                    rawSteps
                                }

                                if (allowedSteps != 0) {
                                    dragAccumulatorPx -= allowedSteps * stepPx
                                    committedTicks += allowedSteps
                                    fireThrottledMechanicalTick()
                                    currentOnStepDelta(allowedSteps)
                                } else {
                                    dragAccumulatorPx = 0f
                                }
                            }
                        }
                    )
                }
                .testTag("${testTag}_canvas"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val centerX = w / 2f
                val tickSpacing = stepPx.coerceAtLeast(8f)
                val halfTicks = (w / (2f * tickSpacing)).toInt() + 3
                val visualShift = -subTickVisualOffsetPx

                for (i in -halfTicks..halfTicks) {
                    val x = centerX + (i * tickSpacing) + visualShift
                    if (x < 0f || x > w) continue

                    val distFromCenter = abs(x - centerX) / (w / 2f)
                    val visibilityAlpha = (1f - distFromCenter.coerceIn(0f, 1f)).let { it * it }

                    val logicalTick = i + committedTicks
                    val isMajor = logicalTick % 10 == 0
                    val isMedium = !isMajor && (logicalTick % 5 == 0)

                    val tickHeight = when {
                        isMajor -> h * 0.56f
                        isMedium -> h * 0.38f
                        else -> h * 0.22f
                    }
                    val strokeWidth = when {
                        isMajor -> 1.8.dp.toPx()
                        isMedium -> 1.3.dp.toPx()
                        else -> 0.9.dp.toPx()
                    }
                    val tickColor = when {
                        isMajor -> Color.White.copy(alpha = (0.68f * visibilityAlpha).coerceIn(0.06f, 0.75f))
                        isMedium -> Color.White.copy(alpha = (0.42f * visibilityAlpha).coerceIn(0.05f, 0.50f))
                        else -> TextMuted.copy(alpha = (0.34f * visibilityAlpha).coerceIn(0.04f, 0.38f))
                    }

                    val topY = (h - tickHeight) / 2f
                    drawLine(
                        color = tickColor,
                        start = Offset(x, topY),
                        end = Offset(x, topY + tickHeight),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                // Soft Left & Right Edge Vignette
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            KshanBackground,
                            Color.Transparent,
                            Color.Transparent,
                            KshanBackground
                        )
                    ),
                    size = size
                )

                // Single Crisp White Center Laser Needle
                drawLine(
                    color = Color.White,
                    start = Offset(centerX, 4.dp.toPx()),
                    end = Offset(centerX, h - 4.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Hero Wrapper with Single-Line 3-Metric Strip (preserved for backward compatibility).
 */
@Composable
fun TemporalConstellationHero(
    startDate: LocalDate,
    endDate: LocalDate,
    daysCount: Long,
    accentColor: Color,
    modifier: Modifier = Modifier,
    heroContent: @Composable () -> Unit
) {
    val metricsStrip = remember(startDate, endDate, daysCount) {
        TimeEngine.calculateCompactMetricsStrip(
            start = startDate,
            end = endDate,
            totalDays = daysCount
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            heroContent()
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .padding(horizontal = 22.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(KshanSurface)
                .border(
                    width = 1.dp,
                    color = KshanBorder,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("compact_three_metric_strip"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = metricsStrip.weeksAndDaysText,
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum",
                        color = TextPrimary
                    )
                )
                Text(
                    text = "  •  ",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 13.sp,
                    color = TextMuted
                )
                Text(
                    text = metricsStrip.workdaysText,
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum",
                        color = KshanCyan
                    )
                )
                Text(
                    text = "  •  ",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 13.sp,
                    color = TextMuted
                )
                Text(
                    text = metricsStrip.hoursText,
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum",
                        color = TextSecondary
                    )
                )
            }
        }
    }
}

/**
 * Interactive 365-Dot Matrix Grid for the Year & Season Progress Card on Tab 4 (Pulse).
 */
@Composable
fun YearDotMatrixGrid(
    currentYear: Int,
    dayOfYear: Int,
    totalDaysInYear: Int,
    savedEvents: List<ChronoEvent>,
    onDayInspected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var selectedDay by remember(currentYear, dayOfYear) { mutableIntStateOf(dayOfYear) }

    val milestonesByDay = remember(savedEvents, currentYear) {
        val map = mutableMapOf<Int, MutableList<String>>()
        savedEvents.forEach { ev ->
            try {
                val zdt = ZonedDateTime.parse(ev.targetIsoString)
                val projectedDay = TimeEngine.projectEventDayOfYear(zdt, ev.repeatsYearly, currentYear)
                if (projectedDay != null && projectedDay in 1..totalDaysInYear) {
                    map.getOrPut(projectedDay) { mutableListOf() }.add(ev.title)
                }
            } catch (e: Exception) {
                Log.w("YearDotMatrixGrid", "Skipping malformed event date: ${ev.targetIsoString}", e)
            }
        }
        map
    }

    val infiniteTransition = rememberInfiniteTransition(label = "year_matrix_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "today_dot_pulse"
    )

    val columns = 25
    val rows = (totalDaysInYear + columns - 1) / columns

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("year_dot_matrix_grid")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MatrixLegendItem(color = KshanCyan, label = "Elapsed")
                MatrixLegendItem(color = Color.White, label = "Today")
                MatrixLegendItem(color = VisionGold, label = "Kshan ★")
            }
            Text(
                text = "Tap any dot",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(216.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(KshanBackground)
                .border(1.dp, KshanBorder, RoundedCornerShape(24.dp))
                .padding(12.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(totalDaysInYear, columns, rows) {
                        detectTapGestures { tapOffset ->
                            val cellW = size.width / columns.toFloat()
                            val cellH = size.height / rows.toFloat()
                            val col = (tapOffset.x / cellW).toInt().coerceIn(0, columns - 1)
                            val row = (tapOffset.y / cellH).toInt().coerceIn(0, rows - 1)
                            val tappedDay = (row * columns + col + 1).coerceIn(1, totalDaysInYear)
                            selectedDay = tappedDay
                            try {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            } catch (e: Exception) {
                                Log.w("YearDotMatrixGrid", "Haptic tick unavailable", e)
                            }
                            val dateObj = LocalDate.ofYearDay(currentYear, tappedDay)
                            val eventsHere = milestonesByDay[tappedDay]
                            val summary = if (!eventsHere.isNullOrEmpty()) {
                                "Day $tappedDay (${dateObj.format(TimeEngine.SHORT_DATE_FORMATTER)}): ★ ${eventsHere.joinToString(", ")}"
                            } else {
                                "Day $tappedDay of $totalDaysInYear • ${dateObj.format(TimeEngine.FULL_DATE_FORMATTER)}"
                            }
                            onDayInspected(summary)
                        }
                    }
            ) {
                val cellW = size.width / columns.toFloat()
                val cellH = size.height / rows.toFloat()
                val baseRadius = (minOf(cellW, cellH) * 0.30f).coerceAtLeast(2.5f)

                for (dayIdx in 1..totalDaysInYear) {
                    val zeroBased = dayIdx - 1
                    val r = zeroBased / columns
                    val c = zeroBased % columns
                    val cx = c * cellW + cellW / 2f
                    val cy = r * cellH + cellH / 2f
                    val center = Offset(cx, cy)

                    val isMilestone = milestonesByDay.containsKey(dayIdx)
                    val isToday = dayIdx == dayOfYear
                    val isSelected = dayIdx == selectedDay
                    val isElapsed = dayIdx < dayOfYear

                    if (isSelected) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.28f),
                            radius = baseRadius * 2.3f,
                            center = center
                        )
                    }

                    when {
                        isToday -> {
                            drawCircle(
                                color = KshanCyan.copy(alpha = 0.45f * pulse),
                                radius = baseRadius * (1.8f + 0.6f * pulse),
                                center = center
                            )
                            drawCircle(
                                color = Color.White,
                                radius = baseRadius * 1.35f,
                                center = center
                            )
                        }
                        isMilestone -> {
                            drawCircle(
                                color = VisionGold.copy(alpha = 0.3f * pulse),
                                radius = baseRadius * 1.9f,
                                center = center
                            )
                            drawCircle(
                                color = VisionGold,
                                radius = baseRadius * 1.25f,
                                center = center
                            )
                        }
                        isElapsed -> {
                            drawCircle(
                                color = KshanCyan.copy(alpha = 0.78f),
                                radius = baseRadius,
                                center = center
                            )
                        }
                        else -> {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.14f),
                                radius = baseRadius * 0.88f,
                                center = center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val inspectedDate = remember(currentYear, selectedDay) {
            LocalDate.ofYearDay(currentYear, selectedDay.coerceIn(1, totalDaysInYear))
        }
        val inspectedEvents = milestonesByDay[selectedDay]

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(KshanPillShape)
                .background(KshanBackground)
                .border(
                    width = 1.dp,
                    color = if (!inspectedEvents.isNullOrEmpty()) VisionGold.copy(alpha = 0.55f) else KshanBorder,
                    shape = KshanPillShape
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (!inspectedEvents.isNullOrEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = VisionGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (!inspectedEvents.isNullOrEmpty()) {
                            "${inspectedDate.format(TimeEngine.SHORT_DATE_FORMATTER)} • ${inspectedEvents.joinToString(", ")}"
                        } else {
                            "${inspectedDate.format(TimeEngine.SHORT_DATE_FORMATTER)} • ${
                                when {
                                    selectedDay == dayOfYear -> "Today (Present Kshan)"
                                    selectedDay < dayOfYear -> "${dayOfYear - selectedDay}d ago"
                                    else -> "In ${selectedDay - dayOfYear}d"
                                }
                            }"
                        },
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (!inspectedEvents.isNullOrEmpty()) VisionGold else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "DAY $selectedDay/$totalDaysInYear",
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum",
                        color = KshanCyan
                    )
                )
            }
        }
    }
}

@Composable
private fun MatrixLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}
