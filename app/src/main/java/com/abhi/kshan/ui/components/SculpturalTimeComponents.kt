package com.abhi.kshan.ui.components

import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.kshan.data.ChronoEvent
import com.abhi.kshan.engine.TemporalConstellationTelemetry
import com.abhi.kshan.engine.TimeEngine
import com.abhi.kshan.ui.theme.ComicNeueFontFamily
import com.abhi.kshan.ui.theme.CourierPrimeFontFamily
import com.abhi.kshan.ui.theme.NeonBlue
import com.abhi.kshan.ui.theme.NeonCyan
import com.abhi.kshan.ui.theme.NeonEmerald
import com.abhi.kshan.ui.theme.NeonPink
import com.abhi.kshan.ui.theme.NeonPurple
import com.abhi.kshan.ui.theme.SpaceBlack
import com.abhi.kshan.ui.theme.SpaceCard
import com.abhi.kshan.ui.theme.SpaceCardBorder
import com.abhi.kshan.ui.theme.SpaceDark
import com.abhi.kshan.ui.theme.TabularMonoFontFamily
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.math.abs
import kotlin.math.roundToInt

private val GoldStar = Color(0xFFFFD700)

/**
 * Tactile Mechanical Time Wheel / Scrubbing Ruler ("Leica / Luxury Watch Crown").
 *
 * Strictly consumes horizontal drag gestures via `change.consume()` so parent containers
 * never intercept the gesture, and implements physical tape-measure polarity:
 * dragging thumb LEFT (`dragAmount < 0`) moves higher ticks under the center needle (+1 day),
 * dragging thumb RIGHT (`dragAmount > 0`) moves lower ticks under the center needle (-1 day).
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
    testTag: String = "mechanical_scrub_ruler"
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val density = LocalDensity.current
    val stepPx = remember(density) { with(density) { 14.dp.toPx() } }

    val currentOnStepDelta by rememberUpdatedState(onStepDelta)
    var dragAccumulatorPx by remember { mutableFloatStateOf(0f) }
    var visualPhasePx by remember { mutableFloatStateOf(0f) }

    fun fireMechanicalTick() {
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

    FrostedGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        borderColor = accentColor,
        glowAlpha = 0.42f,
        cornerRadius = 22.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Bar: Target Selector Pill + Live Readout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Target Selector Pill (e.g. Start Date | End Date)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(SpaceDark.copy(alpha = 0.9f))
                        .border(1.dp, SpaceCardBorder, RoundedCornerShape(50))
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    targetOptions.forEachIndexed { idx, label ->
                        val isSelected = idx == selectedTargetIndex
                        val pillBg by animateColorAsState(
                            targetValue = if (isSelected) accentColor.copy(alpha = 0.24f) else Color.Transparent,
                            animationSpec = tween(180),
                            label = "scrub_target_bg"
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(pillBg)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(
                                            1.dp,
                                            accentColor.copy(alpha = 0.7f),
                                            RoundedCornerShape(50)
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable {
                                    fireMechanicalTick()
                                    onTargetSelected(idx)
                                }
                                .padding(horizontal = 11.dp, vertical = 5.dp)
                                .testTag("scrub_target_option_$idx")
                        ) {
                            Text(
                                text = label,
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) accentColor else TextSecondary
                            )
                        }
                    }
                }

                // Live Crown Readout
                Text(
                    text = activeValueReadout,
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tactile Mechanical Ruler Tape Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF060A14))
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                SpaceCardBorder.copy(alpha = 0.3f),
                                accentColor.copy(alpha = 0.55f),
                                SpaceCardBorder.copy(alpha = 0.3f)
                            )
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .pointerInput(selectedTargetIndex, stepPx) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                dragAccumulatorPx = 0f
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                // Strictly consume horizontal drag events so parent containers never intercept
                                change.consume()
                                // Physical tape-measure polarity:
                                // Dragging thumb LEFT (dragAmount < 0) advances the tape (+1 day)
                                // Dragging thumb RIGHT (dragAmount > 0) rewinds the tape (-1 day)
                                dragAccumulatorPx -= dragAmount
                                visualPhasePx = (visualPhasePx + dragAmount) % (stepPx * 10f)

                                val steps = (dragAccumulatorPx / stepPx).toInt()
                                if (steps != 0) {
                                    dragAccumulatorPx -= steps * stepPx
                                    fireMechanicalTick()
                                    currentOnStepDelta(steps)
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
                    val tickSpacing = stepPx.coerceAtLeast(10f)
                    val halfTicks = (w / (2f * tickSpacing)).toInt() + 4
                    val phaseOffset = visualPhasePx % tickSpacing
                    val phaseIndexShift = (visualPhasePx / tickSpacing).roundToInt()

                    for (i in -halfTicks..halfTicks) {
                        val x = centerX + (i * tickSpacing) + phaseOffset
                        if (x < 0f || x > w) continue

                        val distFromCenter = abs(x - centerX) / (w / 2f)
                        val visibilityAlpha = (1f - distFromCenter.coerceIn(0f, 1f)).let { it * it }

                        val logicalTick = i - phaseIndexShift
                        val isMajor = logicalTick % 10 == 0
                        val isMedium = !isMajor && (logicalTick % 5 == 0)

                        val tickHeight = when {
                            isMajor -> h * 0.58f
                            isMedium -> h * 0.40f
                            else -> h * 0.24f
                        }
                        val strokeWidth = when {
                            isMajor -> 2.2.dp.toPx()
                            isMedium -> 1.5.dp.toPx()
                            else -> 1.dp.toPx()
                        }
                        val tickColor = when {
                            distFromCenter < 0.08f -> accentColor
                            isMajor -> Color.White.copy(alpha = (0.75f * visibilityAlpha).coerceIn(0.08f, 0.85f))
                            isMedium -> accentColor.copy(alpha = (0.55f * visibilityAlpha).coerceIn(0.06f, 0.65f))
                            else -> TextMuted.copy(alpha = (0.42f * visibilityAlpha).coerceIn(0.04f, 0.45f))
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

                    // Subtle Horizontal Hairline Axis
                    drawLine(
                        color = accentColor.copy(alpha = 0.15f),
                        start = Offset(0f, h / 2f),
                        end = Offset(w, h / 2f),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Left & Right Edge Vignette Shadow
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF060A14),
                                Color.Transparent,
                                Color.Transparent,
                                Color(0xFF060A14)
                            )
                        ),
                        size = size
                    )

                    // Center Precision Watch Crown Indicator Needle
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            center = Offset(centerX, h / 2f),
                            radius = 22.dp.toPx()
                        ),
                        center = Offset(centerX, h / 2f),
                        radius = 22.dp.toPx()
                    )

                    drawLine(
                        color = accentColor,
                        start = Offset(centerX, 4.dp.toPx()),
                        end = Offset(centerX, h - 4.dp.toPx()),
                        strokeWidth = 2.6.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Top & Bottom Indicator Jewels
                    drawCircle(
                        color = Color.White,
                        radius = 3.2.dp.toPx(),
                        center = Offset(centerX, 5.dp.toPx())
                    )
                    drawCircle(
                        color = accentColor,
                        radius = 3.2.dp.toPx(),
                        center = Offset(centerX, h - 5.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Micro Instruction Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "◄ Drag left to advance (+)",
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = "CROWN SCRUBBER",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = accentColor.copy(alpha = 0.8f)
                )
                Text(
                    text = "Drag right to rewind (−) ►",
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

/**
 * Temporal Constellation & Life Context Hero Wrapper.
 *
 * Surrounds the hero number/date readout with:
 * 1. A glowing curved Visual Timeline Arc connecting Start/Base Date (left) to End/Result Date (right)
 *    with a pulsing "Today" orbital dot plotted proportionally along the span.
 * 2. A swipeable 3-Card Human & Cosmic Rhythm Micro-Carousel (`HorizontalPager`) below the hero readout.
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
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val constellation = remember(startDate, endDate, daysCount) {
        TimeEngine.calculateTemporalConstellation(
            start = startDate,
            end = endDate,
            daysCount = daysCount
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "constellation_arc_pulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "today_orb_pulse"
    )

    val pagerState = rememberPagerState(pageCount = { constellation.cards.size })

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Visual Timeline Arc + Hero Number Readout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            // Glowing Orbital Timeline Arc Canvas behind the Hero Readout
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) {
                val w = size.width
                val h = size.height
                val leftX = 24.dp.toPx()
                val rightX = (w - 24.dp.toPx()).coerceAtLeast(leftX + 40f)
                val baseY = h * 0.72f
                val peakY = h * 0.06f

                // Quadratic Bezier control point for the parabolic arch
                val controlX = (leftX + rightX) / 2f
                val controlY = peakY - (baseY - peakY) * 0.35f

                val arcPath = Path().apply {
                    moveTo(leftX, baseY)
                    quadraticTo(controlX, controlY, rightX, baseY)
                }

                // Soft outer neon halo along the timeline arc
                drawPath(
                    path = arcPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.18f),
                            accentColor.copy(alpha = 0.28f * pulsePhase),
                            NeonPurple.copy(alpha = 0.18f)
                        )
                    ),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )

                // Crisp dashed orbital trajectory line
                drawPath(
                    path = arcPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.65f),
                            Color.White.copy(alpha = 0.75f),
                            NeonPurple.copy(alpha = 0.65f)
                        )
                    ),
                    style = Stroke(
                        width = 1.6.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    )
                )

                // Start Node (Left)
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.28f),
                    radius = 9.dp.toPx(),
                    center = Offset(leftX, baseY)
                )
                drawCircle(
                    color = NeonCyan,
                    radius = 4.2.dp.toPx(),
                    center = Offset(leftX, baseY)
                )

                // End Node (Right)
                drawCircle(
                    color = NeonPurple.copy(alpha = 0.28f),
                    radius = 9.dp.toPx(),
                    center = Offset(rightX, baseY)
                )
                drawCircle(
                    color = NeonPurple,
                    radius = 4.2.dp.toPx(),
                    center = Offset(rightX, baseY)
                )

                // Plot glowing "TODAY" beacon along Quadratic Bezier if Today falls inside the span
                val t = constellation.todayProgressAlongArc
                if (t != null) {
                    val u = 1f - t
                    val bx = (u * u * leftX) + (2f * u * t * controlX) + (t * t * rightX)
                    val by = (u * u * baseY) + (2f * u * t * controlY) + (t * t * baseY)

                    drawCircle(
                        color = GoldStar.copy(alpha = 0.32f * pulsePhase),
                        radius = (14.dp.toPx()) * (0.85f + 0.25f * pulsePhase),
                        center = Offset(bx, by)
                    )
                    drawCircle(
                        color = GoldStar,
                        radius = 5.5.dp.toPx(),
                        center = Offset(bx, by)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = Offset(bx, by)
                    )
                }
            }

            // Center Hero Readout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Subtle Timeline Endpoint Labels + Today Status Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = startDate.format(TimeEngine.SHORT_DATE_FORMATTER),
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 10.5.sp,
                        color = NeonCyan.copy(alpha = 0.85f)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(SpaceCard.copy(alpha = 0.85f))
                            .border(
                                width = 1.dp,
                                color = if (constellation.isTodayInsideSpan) GoldStar.copy(alpha = 0.6f)
                                else SpaceCardBorder,
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 9.dp, vertical = 2.dp)
                    ) {
                        val todayPercentText = constellation.todayProgressAlongArc?.let {
                            "● TODAY AT ${(it * 100f).roundToInt()}% OF SPAN"
                        } ?: "◌ TODAY OUTSIDE SPAN"
                        Text(
                            text = todayPercentText,
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (constellation.isTodayInsideSpan) GoldStar else TextMuted
                        )
                    }

                    Text(
                        text = endDate.format(TimeEngine.SHORT_DATE_FORMATTER),
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 10.5.sp,
                        color = NeonPurple.copy(alpha = 0.85f)
                    )
                }

                heroContent()
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Swipeable 3-Card Human & Cosmic Rhythm Micro-Carousel
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 28.dp),
                pageSpacing = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("temporal_constellation_pager")
            ) { page ->
                val card = constellation.cards[page]
                val cardAccent = when (page) {
                    0 -> accentColor
                    1 -> NeonPurple
                    else -> GoldStar
                }

                FrostedGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            } catch (e: Exception) {
                                Log.w("TemporalConstellation", "Haptic tick unavailable", e)
                            }
                            scope.launch {
                                pagerState.animateScrollToPage((page + 1) % constellation.cards.size)
                            }
                        },
                    borderColor = cardAccent,
                    glowAlpha = 0.4f,
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = cardAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = card.title,
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.4.sp,
                                color = cardAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = card.primaryMetric,
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = card.secondaryMetric,
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = cardAccent.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = card.poeticCaption,
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 11.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3-Dot Carousel Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(constellation.cards.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val widthDp by animateFloatAsState(
                        targetValue = if (isSelected) 18f else 6f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
                        label = "pager_dot_width"
                    )
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(widthDp.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) accentColor else SpaceCardBorder)
                            .clickable {
                                scope.launch { pagerState.animateScrollToPage(index) }
                            }
                    )
                }
            }
        }
    }
}

/**
 * Interactive 365-Dot Matrix Grid for the Year & Season Progress Card.
 *
 * - Elapsed days (`1 until dayOfYear`) glow in neon cyan (`#00F5D4`).
 * - Today (`dayOfYear`) pulses white-hot (`#FFFFFF`) with an expanding cyan aura.
 * - Saved Countdown Milestones (including recurring Birthdays from past years and Holidays
 *   rolled into next year via `TimeEngine.projectEventDayOfYear`) shine as radiant Gold Stars (`#FFD700`).
 * - Tapping any dot selects that day and displays its exact calendar date + any milestone on that day.
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

    // Map dayOfYear -> list of milestone titles projected onto currentYear
    val milestonesByDay = remember(savedEvents, currentYear) {
        val map = mutableMapOf<Int, MutableList<String>>()
        savedEvents.forEach { ev ->
            try {
                val zdt = ZonedDateTime.parse(ev.targetIsoString)
                val projectedDay = TimeEngine.projectEventDayOfYear(zdt, ev.category, currentYear)
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
                MatrixLegendItem(color = NeonCyan, label = "Elapsed")
                MatrixLegendItem(color = Color.White, label = "Today")
                MatrixLegendItem(color = GoldStar, label = "Milestone ★")
            }
            Text(
                text = "Tap any dot",
                fontFamily = ComicNeueFontFamily,
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
                .background(Color(0xFF060A14))
                .border(1.dp, SpaceCardBorder, RoundedCornerShape(16.dp))
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
                                color = NeonCyan.copy(alpha = 0.4f * pulse),
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
                                color = GoldStar.copy(alpha = 0.3f * pulse),
                                radius = baseRadius * 1.9f,
                                center = center
                            )
                            drawCircle(
                                color = GoldStar,
                                radius = baseRadius * 1.25f,
                                center = center
                            )
                        }
                        isElapsed -> {
                            drawCircle(
                                color = NeonCyan.copy(alpha = 0.82f),
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
                .background(SpaceCard.copy(alpha = 0.85f))
                .border(
                    width = 1.dp,
                    color = if (!inspectedEvents.isNullOrEmpty()) GoldStar.copy(alpha = 0.6f) else NeonCyan.copy(alpha = 0.35f),
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
                            tint = GoldStar,
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
                        fontFamily = ComicNeueFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!inspectedEvents.isNullOrEmpty()) GoldStar else TextPrimary,
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
                    color = NeonCyan
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
            fontFamily = ComicNeueFontFamily,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}
