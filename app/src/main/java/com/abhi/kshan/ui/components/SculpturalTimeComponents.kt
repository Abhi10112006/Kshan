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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.kshan.data.ChronoEvent
import com.abhi.kshan.engine.TimeEngine
import com.abhi.kshan.ui.theme.CourierPrimeFontFamily
import com.abhi.kshan.ui.theme.IceCyan
import com.abhi.kshan.ui.theme.ObsidianGlass
import com.abhi.kshan.ui.theme.SpaceDark
import com.abhi.kshan.ui.theme.SpaceGroteskFontFamily
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.ui.theme.TextSecondary
import com.abhi.kshan.ui.theme.TitaniumHairline
import com.abhi.kshan.ui.theme.WarmChampagne
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.math.abs

/**
 * Compact 52.dp Frameless Horizontal Crown Scrubber ("Leica / Swiss Watch Crown").
 *
 * Implements all Minimalist Luxury & Final Engineering Review specifications:
 * 1. Compact 52.dp frameless horizontal strip with `[ Start | End ]` or `[ Base | Shift ]` pill
 *    on the left and titanium ruler tick marks with a single white center needle on the right.
 * 2. Sub-tick continuous visual offset `(dragAccumulatorPx % stepPx)` with spring snap-back
 *    to `0f` (`animateFloatAsState(spring())`) when the finger lifts (`onDragEnd` / `onDragCancel`).
 * 3. Hard boundary clamping (`minValue..maxValue`, e.g. `0..9999` on Shift): when at `0` and
 *    dragging to decrease (or `9999` and dragging to increase), clamps `dragAccumulatorPx = 0f`,
 *    freezes sub-tick movement, and suppresses haptics.
 * 4. 16ms haptic rate-limiting (`SystemClock.uptimeMillis()`).
 * 5. Full TalkBack accessibility semantics (`customActions` + `setProgress`).
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

    // Smooth spring snap-back to 0f when finger lifts
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
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianGlass)
            .border(
                width = 0.8.dp,
                color = TitaniumHairline,
                shape = RoundedCornerShape(16.dp)
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
        // Left Compact Target Selector Pill: [ Start | End ] or [ Base | Shift ]
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(11.dp))
                .background(SpaceDark)
                .border(0.8.dp, TitaniumHairline, RoundedCornerShape(11.dp))
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            targetOptions.forEachIndexed { idx, rawLabel ->
                // Keep pill label ultra-compact so it never crowds the dial strip
                val shortLabel = when {
                    rawLabel.startsWith("Start", ignoreCase = true) -> "Start"
                    rawLabel.startsWith("End", ignoreCase = true) -> "End"
                    rawLabel.startsWith("Base", ignoreCase = true) -> "Base"
                    rawLabel.startsWith("Days", ignoreCase = true) -> "Shift"
                    else -> rawLabel
                }
                val isSelected = idx == selectedTargetIndex
                val pillBg by animateColorAsState(
                    targetValue = if (isSelected) IceCyan.copy(alpha = 0.16f) else Color.Transparent,
                    animationSpec = tween(160),
                    label = "scrub_target_bg"
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(pillBg)
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    0.8.dp,
                                    IceCyan.copy(alpha = 0.6f),
                                    RoundedCornerShape(9.dp)
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
                        color = if (isSelected) IceCyan else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Frameless Horizontal Titanium Dial Strip with Center White Laser Needle
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF060911))
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
                            // Strictly consume horizontal drag events so parent containers never intercept
                            change.consume()

                            // Physical tape-measure polarity:
                            // Dragging thumb LEFT (dragAmount < 0) advances the tape (+1 day)
                            // Dragging thumb RIGHT (dragAmount > 0) rewinds the tape (-1 day)
                            val proposedDelta = -dragAmount
                            val curVal = latestNumericValue
                            val minVal = latestMinValue
                            val maxVal = latestMaxValue

                            // Hard boundary clamping for bounded numeric values (e.g. 0..9999 on Shift)
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
                                    // Reached hard boundary mid-drag: clamp accumulator & freeze offset
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

                // Subtract subTickVisualOffsetPx so ticks move continuously with the finger
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
                            Color(0xFF060911),
                            Color.Transparent,
                            Color.Transparent,
                            Color(0xFF060911)
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
 * Minimalist Luxury Hero Wrapper with Single-Line 3-Metric Strip.
 *
 * Replaces the cluttered curved rainbow arc and tall 3-card HorizontalPager with:
 * 1. Clean negative space around `heroContent()`.
 * 2. A quiet, single-line frosted strip below the hero result:
 *    `2w 6d  •  14 Workdays  •  480 Hours`
 *    computed dynamically via `TimeEngine.calculateCompactMetricsStrip`.
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
        // 1. Clean Hero Readout in Generous Negative Space
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            heroContent()
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Single-Line Frosted 3-Metric Strip: "2w 6d  •  14 Workdays  •  480 Hours"
        Box(
            modifier = Modifier
                .padding(horizontal = 22.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ObsidianGlass)
                .border(
                    width = 0.8.dp,
                    color = TitaniumHairline,
                    shape = RoundedCornerShape(16.dp)
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
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "  •  ",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 13.sp,
                    color = TextMuted
                )
                Text(
                    text = metricsStrip.workdaysText,
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = IceCyan
                )
                Text(
                    text = "  •  ",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 13.sp,
                    color = TextMuted
                )
                Text(
                    text = metricsStrip.hoursText,
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }
        }
    }
}

/**
 * Interactive 365-Dot Matrix Grid for the Year & Season Progress Card.
 *
 * Uses `event.repeatsYearly` via `TimeEngine.projectEventDayOfYear(zdt, ev.repeatsYearly, currentYear)`
 * so recurring annual events glow as Warm Champagne milestone stars across every year.
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

    // Map dayOfYear -> list of milestone titles projected onto currentYear using event.repeatsYearly
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
        // Legend Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MatrixLegendItem(color = IceCyan, label = "Elapsed")
                MatrixLegendItem(color = Color.White, label = "Today")
                MatrixLegendItem(color = WarmChampagne, label = "Milestone ★")
            }
            Text(
                text = "Tap any dot",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // High-Performance Single Canvas 365-Dot Matrix with Tap Hit-Testing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(216.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF060911))
                .border(0.8.dp, TitaniumHairline, RoundedCornerShape(16.dp))
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
                                color = IceCyan.copy(alpha = 0.4f * pulse),
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
                                color = WarmChampagne.copy(alpha = 0.3f * pulse),
                                radius = baseRadius * 1.9f,
                                center = center
                            )
                            drawCircle(
                                color = WarmChampagne,
                                radius = baseRadius * 1.25f,
                                center = center
                            )
                        }
                        isElapsed -> {
                            drawCircle(
                                color = IceCyan.copy(alpha = 0.78f),
                                radius = baseRadius,
                                center = center
                            )
                        }
                        else -> {
                            drawCircle(
                                color = Color(0xFF1E293B),
                                radius = baseRadius * 0.88f,
                                center = center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive Selected Dot Telemetry Bar
        val inspectedDate = remember(currentYear, selectedDay) {
            LocalDate.ofYearDay(currentYear, selectedDay.coerceIn(1, totalDaysInYear))
        }
        val inspectedEvents = milestonesByDay[selectedDay]

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ObsidianGlass)
                .border(
                    width = 0.8.dp,
                    color = if (!inspectedEvents.isNullOrEmpty()) WarmChampagne.copy(alpha = 0.55f) else TitaniumHairline,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
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
                            tint = WarmChampagne,
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
                        fontWeight = FontWeight.SemiBold,
                        color = if (!inspectedEvents.isNullOrEmpty()) WarmChampagne else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "DAY $selectedDay/$totalDaysInYear",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = IceCyan
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
