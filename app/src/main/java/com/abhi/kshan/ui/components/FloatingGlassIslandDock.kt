package com.abhi.kshan.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.kshan.ui.theme.KshanBorder
import com.abhi.kshan.ui.theme.KshanCyan
import com.abhi.kshan.ui.theme.KshanPillShape
import com.abhi.kshan.ui.theme.KshanSurface
import com.abhi.kshan.ui.theme.SpaceGroteskFontFamily
import com.abhi.kshan.viewmodel.ChronoTab
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Redesigned Floating Bottom Navigation Island Dock:
 * - Strict #121620 (KshanSurface) full pill (`RoundedCornerShape(100)`) with 1.dp Color.White.copy(alpha = 0.08f) border
 * - Persistent 11.sp labels on all four dock tabs ("Runway", "Planner", "Kshans", "Pulse")
 * - Active tab: #2ABFFF icon + 11.sp SemiBold text inside a full-pill #2ABFFF.copy(alpha = 0.14f) indicator
 * - Inactive tabs: Color.White.copy(alpha = 0.45f)
 */
@Composable
fun FloatingGlassIslandDock(
    selectedTab: ChronoTab,
    onTabSelected: (ChronoTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    val tabs = remember {
        listOf(
            Triple(ChronoTab.DIFFERENCE, "Runway", "tab_difference"),
            Triple(ChronoTab.DATE_MATH, "Planner", "tab_add___subtract"),
            Triple(ChronoTab.COUNTDOWN, "Kshans", "tab_countdowns"),
            Triple(ChronoTab.WIDGETS, "Pulse", "tab_widgets")
        )
    }

    val inactiveTint = Color.White.copy(alpha = 0.45f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(KshanPillShape)
                .background(KshanSurface)
                .border(
                    width = 1.dp,
                    color = KshanBorder,
                    shape = KshanPillShape
                )
                .padding(horizontal = 6.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { (tab, label, testTag) ->
                    val isSelected = tab == selectedTab

                    val activeTint by animateColorAsState(
                        targetValue = if (isSelected) KshanCyan else inactiveTint,
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
                        label = "dock_color"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(KshanPillShape)
                            .background(
                                if (isSelected) KshanCyan.copy(alpha = 0.14f)
                                else Color.Transparent
                            )
                            .then(
                                if (isSelected) {
                                    Modifier.border(
                                        width = 1.dp,
                                        color = KshanCyan.copy(alpha = 0.35f),
                                        shape = KshanPillShape
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onTabSelected(tab)
                            }
                            .testTag(testTag),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            when (tab) {
                                ChronoTab.DIFFERENCE -> LivingDifferenceIcon(
                                    isSelected = isSelected,
                                    tint = activeTint
                                )
                                ChronoTab.DATE_MATH -> LivingShiftIcon(
                                    isSelected = isSelected,
                                    tint = activeTint
                                )
                                ChronoTab.COUNTDOWN -> LivingHourglassIcon(
                                    isSelected = isSelected,
                                    tint = activeTint
                                )
                                ChronoTab.WIDGETS -> LivingWidgetsIcon(
                                    isSelected = isSelected,
                                    tint = activeTint
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Persistent 11.sp label on all four dock tabs
                            Text(
                                text = label,
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = activeTint
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LivingDifferenceIcon(
    isSelected: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val slideAnim = remember { Animatable(0f) }
    val headProgress = remember { Animatable(0f) }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            launch {
                slideAnim.snapTo(0f)
                slideAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.52f,
                        stiffness = 380f
                    )
                )
            }
            launch {
                headProgress.snapTo(0f)
                headProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
                )
            }
        } else {
            launch {
                slideAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 500f)
                )
            }
            launch {
                headProgress.animateTo(0f, animationSpec = tween(180))
            }
        }
    }

    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val maxSlide = 3.5.dp.toPx()
        val currentSlide = slideAnim.value * maxSlide
        val drawProgress = if (isSelected) headProgress.value else 0.75f

        val topStartX = w * 0.18f + currentSlide * 0.3f
        val topEndX = (w * 0.74f) + currentSlide
        val topY = h * 0.35f

        drawLine(
            color = tint,
            start = Offset(topStartX, topY),
            end = Offset(topEndX, topY),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )

        if (drawProgress > 0.05f) {
            val wingX = 3.8.dp.toPx() * drawProgress
            val wingY = 2.8.dp.toPx() * drawProgress
            drawLine(
                color = tint,
                start = Offset(topEndX - wingX, topY - wingY),
                end = Offset(topEndX, topY),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = tint,
                start = Offset(topEndX - wingX, topY + wingY),
                end = Offset(topEndX, topY),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        val bottomStartX = w * 0.82f - currentSlide * 0.3f
        val bottomEndX = (w * 0.26f) - currentSlide
        val bottomY = h * 0.65f

        drawLine(
            color = tint,
            start = Offset(bottomStartX, bottomY),
            end = Offset(bottomEndX, bottomY),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )

        if (drawProgress > 0.05f) {
            val wingX = 3.8.dp.toPx() * drawProgress
            val wingY = 2.8.dp.toPx() * drawProgress
            drawLine(
                color = tint,
                start = Offset(bottomEndX + wingX, bottomY - wingY),
                end = Offset(bottomEndX, bottomY),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = tint,
                start = Offset(bottomEndX + wingX, bottomY + wingY),
                end = Offset(bottomEndX, bottomY),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun LivingShiftIcon(
    isSelected: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val outerScale = remember { Animatable(1f) }
    val plusScale = remember { Animatable(0.9f) }
    val plusRotation = remember { Animatable(0f) }
    val minusScale = remember { Animatable(0.9f) }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            launch {
                outerScale.snapTo(0.84f)
                outerScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = 0.52f, stiffness = 420f)
                )
            }
            launch {
                plusScale.snapTo(0.3f)
                plusScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = 0.58f, stiffness = 480f)
                )
            }
            launch {
                plusRotation.snapTo(0f)
                plusRotation.animateTo(
                    targetValue = 90f,
                    animationSpec = spring(dampingRatio = 0.62f, stiffness = 400f)
                )
            }
            launch {
                delay(60)
                minusScale.snapTo(0.3f)
                minusScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = 0.58f, stiffness = 480f)
                )
            }
        } else {
            launch {
                outerScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f))
            }
            launch {
                plusRotation.animateTo(0f, animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f))
            }
            launch {
                plusScale.animateTo(0.9f, animationSpec = tween(200))
            }
            launch {
                minusScale.animateTo(0.9f, animationSpec = tween(200))
            }
        }
    }

    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        val scale = outerScale.value
        val boxDim = 16.5.dp.toPx() * scale
        val boxLeft = cx - boxDim / 2f
        val boxTop = cy - boxDim / 2f
        val corner = 4.dp.toPx()

        drawRoundRect(
            color = tint,
            topLeft = Offset(boxLeft, boxTop),
            size = Size(boxDim, boxDim),
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        )

        drawLine(
            color = tint.copy(alpha = 0.45f),
            start = Offset(boxLeft + 2.dp.toPx(), cy),
            end = Offset(boxLeft + boxDim - 2.dp.toPx(), cy),
            strokeWidth = 1.1.dp.toPx()
        )

        val plusCenterY = boxTop + boxDim * 0.27f
        val plusArm = 2.6.dp.toPx() * plusScale.value
        rotate(degrees = plusRotation.value, pivot = Offset(cx, plusCenterY)) {
            drawLine(
                color = if (isSelected) KshanCyan else tint,
                start = Offset(cx - plusArm, plusCenterY),
                end = Offset(cx + plusArm, plusCenterY),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = if (isSelected) KshanCyan else tint,
                start = Offset(cx, plusCenterY - plusArm),
                end = Offset(cx, plusCenterY + plusArm),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        val minusCenterY = boxTop + boxDim * 0.73f
        val minusArm = 2.6.dp.toPx() * minusScale.value
        drawLine(
            color = if (isSelected) KshanCyan else tint,
            start = Offset(cx - minusArm, minusCenterY),
            end = Offset(cx + minusArm, minusCenterY),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun LivingHourglassIcon(
    isSelected: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val flipDegrees by animateFloatAsState(
        targetValue = if (isSelected) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 380f),
        label = "hourglass_flip"
    )

    val fillOpacity by animateFloatAsState(
        targetValue = if (isSelected) 0.4f else 0f,
        animationSpec = tween(durationMillis = 650, delayMillis = 100),
        label = "hourglass_fill_opacity"
    )

    val sandFlow = remember { Animatable(0f) }
    LaunchedEffect(isSelected) {
        if (isSelected) {
            sandFlow.snapTo(0f)
            sandFlow.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 750)
            )
        }
    }

    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        rotate(degrees = flipDegrees, pivot = Offset(cx, cy)) {
            val framePath = Path().apply {
                moveTo(w * 0.22f, h * 0.18f)
                lineTo(w * 0.78f, h * 0.18f)
                lineTo(cx + 1.dp.toPx(), cy)
                lineTo(w * 0.78f, h * 0.82f)
                lineTo(w * 0.22f, h * 0.82f)
                lineTo(cx - 1.dp.toPx(), cy)
                close()
            }

            if (fillOpacity > 0.01f) {
                val bottomChamberFill = Path().apply {
                    moveTo(cx, cy + 1.dp.toPx())
                    lineTo(w * 0.74f, h * 0.80f)
                    lineTo(w * 0.26f, h * 0.80f)
                    close()
                }
                drawPath(
                    path = bottomChamberFill,
                    color = KshanCyan.copy(alpha = fillOpacity)
                )
            }

            drawPath(
                path = framePath,
                color = tint,
                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            )

            drawLine(
                color = tint,
                start = Offset(w * 0.18f, h * 0.18f),
                end = Offset(w * 0.82f, h * 0.18f),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = tint,
                start = Offset(w * 0.18f, h * 0.82f),
                end = Offset(w * 0.82f, h * 0.82f),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )

            if (isSelected && sandFlow.value < 0.95f) {
                val streamY = cy + (sandFlow.value * 4.dp.toPx())
                drawCircle(
                    color = KshanCyan,
                    radius = 1.1.dp.toPx(),
                    center = Offset(cx, streamY)
                )
            }
        }
    }
}

@Composable
fun LivingWidgetsIcon(
    isSelected: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val spreadOffset by animateFloatAsState(
        targetValue = if (isSelected) 1.2.dp.value else 0f,
        animationSpec = spring(dampingRatio = 0.68f, stiffness = 420f),
        label = "widgets_spread"
    )

    val diamondRotation by animateFloatAsState(
        targetValue = if (isSelected) 45f else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "widget_diamond_rot"
    )

    val diamondDetach by animateFloatAsState(
        targetValue = if (isSelected) 2.2.dp.value else 0f,
        animationSpec = spring(dampingRatio = 0.68f, stiffness = 420f),
        label = "widget_diamond_detach"
    )

    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val tileSize = 5.6.dp.toPx()
        val corner = 1.8.dp.toPx()
        val spread = spreadOffset.dp.toPx()
        val detach = diamondDetach.dp.toPx()

        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.18f - spread, h * 0.18f - spread),
            size = Size(tileSize, tileSize),
            cornerRadius = CornerRadius(corner, corner)
        )

        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.18f - spread, h * 0.54f + spread),
            size = Size(tileSize, tileSize),
            cornerRadius = CornerRadius(corner, corner)
        )

        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.54f + spread, h * 0.54f + spread),
            size = Size(tileSize, tileSize),
            cornerRadius = CornerRadius(corner, corner)
        )

        val trCenterX = (w * 0.54f + tileSize / 2f) + spread + detach
        val trCenterY = (h * 0.18f + tileSize / 2f) - spread - detach

        rotate(degrees = diamondRotation, pivot = Offset(trCenterX, trCenterY)) {
            if (isSelected) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(KshanCyan.copy(alpha = 0.45f), Color.Transparent),
                        center = Offset(trCenterX, trCenterY),
                        radius = tileSize * 1.3f
                    ),
                    radius = tileSize * 1.3f,
                    center = Offset(trCenterX, trCenterY)
                )
            }

            drawRoundRect(
                color = if (isSelected) KshanCyan else tint,
                topLeft = Offset(trCenterX - tileSize / 2f, trCenterY - tileSize / 2f),
                size = Size(tileSize, tileSize),
                cornerRadius = CornerRadius(
                    if (isSelected) 1.4.dp.toPx() else corner,
                    if (isSelected) 1.4.dp.toPx() else corner
                )
            )
        }
    }
}
