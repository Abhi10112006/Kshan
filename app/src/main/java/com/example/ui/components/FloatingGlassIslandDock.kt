package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.TextMuted
import com.example.viewmodel.ChronoTab
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FloatingGlassIslandDock(
    selectedTab: ChronoTab,
    onTabSelected: (ChronoTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    val tabs = remember {
        listOf(
            Triple(ChronoTab.DIFFERENCE, "Difference", "tab_difference"),
            Triple(ChronoTab.DATE_MATH, "Shift", "tab_add___subtract"),
            Triple(ChronoTab.COUNTDOWN, "Countdowns", "tab_countdowns"),
            Triple(ChronoTab.WIDGETS, "Widgets", "tab_widgets")
        )
    }

    // Requirement 3: Show name below icon ONLY when user selects any option and remain for ~1.4s
    var activeLabelTab by remember { mutableStateOf<ChronoTab?>(null) }
    var tapCounter by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedTab, tapCounter) {
        activeLabelTab = selectedTab
        delay(1400) // Stays visible for at least 1.4 seconds
        activeLabelTab = null
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Frosted Glass Capsule
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SpaceCard.copy(alpha = 0.92f),
                            SpaceDark.copy(alpha = 0.95f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.45f),
                            SpaceDark.copy(alpha = 0.2f),
                            NeonCyan.copy(alpha = 0.35f)
                        )
                    ),
                    shape = RoundedCornerShape(34.dp)
                )
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { (tab, label, testTag) ->
                    val isSelected = tab == selectedTab
                    val isLabelVisible = (activeLabelTab == tab)

                    val activeGlowColor by animateColorAsState(
                        targetValue = if (isSelected) NeonCyan else TextMuted,
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
                        label = "dock_color"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                if (isSelected) NeonCyan.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                tapCounter++
                                activeLabelTab = tab
                                onTabSelected(tab)
                            }
                            .testTag(testTag),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Living Micro-Mechanical Animated Icon
                            when (tab) {
                                ChronoTab.DIFFERENCE -> LivingDifferenceIcon(
                                    isSelected = isSelected,
                                    tint = activeGlowColor
                                )
                                ChronoTab.DATE_MATH -> LivingShiftIcon(
                                    isSelected = isSelected,
                                    tint = activeGlowColor
                                )
                                ChronoTab.COUNTDOWN -> LivingHourglassIcon(
                                    isSelected = isSelected,
                                    tint = activeGlowColor
                                )
                                ChronoTab.WIDGETS -> LivingWidgetsIcon(
                                    isSelected = isSelected,
                                    tint = activeGlowColor
                                )
                            }

                            // Dynamic Temporary Label on Select (appears for at least ~1.4s then gracefully hides)
                            AnimatedVisibility(
                                visible = isLabelVisible,
                                enter = fadeIn(animationSpec = tween(180)) +
                                        expandVertically(
                                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 450f),
                                            expandFrom = Alignment.Top
                                        ),
                                exit = fadeOut(animationSpec = tween(260)) +
                                       shrinkVertically(
                                           animationSpec = tween(260),
                                           shrinkTowards = Alignment.Top
                                       )
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = com.example.ui.theme.ComicNeueFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 1. Living Difference Icon: Top arrow slides right while bottom slides left, overshooting slightly with spring bounce, while arrowheads draw in
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
                        dampingRatio = 0.52f, // Satisfying spring bounce overshoot
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

    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val maxSlide = 4.dp.toPx()
        val currentSlide = slideAnim.value * maxSlide

        // Base arrowhead visibility when idle vs drawing in on selection
        val drawProgress = if (isSelected) headProgress.value else 0.75f

        // Top arrow pointing Right (slides right with spring overshoot)
        val topStartX = w * 0.18f + currentSlide * 0.3f
        val topEndX = (w * 0.74f) + currentSlide
        val topY = h * 0.35f

        // Stem line
        drawLine(
            color = tint,
            start = Offset(topStartX, topY),
            end = Offset(topEndX, topY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Arrowhead drawing in (simulating stroke-dashoffset)
        if (drawProgress > 0.05f) {
            val wingX = 4.2.dp.toPx() * drawProgress
            val wingY = 3.2.dp.toPx() * drawProgress
            drawLine(
                color = tint,
                start = Offset(topEndX - wingX, topY - wingY),
                end = Offset(topEndX, topY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = tint,
                start = Offset(topEndX - wingX, topY + wingY),
                end = Offset(topEndX, topY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Bottom arrow pointing Left (slides left with spring overshoot)
        val bottomStartX = w * 0.82f - currentSlide * 0.3f
        val bottomEndX = (w * 0.26f) - currentSlide
        val bottomY = h * 0.65f

        // Stem line
        drawLine(
            color = tint,
            start = Offset(bottomStartX, bottomY),
            end = Offset(bottomEndX, bottomY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Arrowhead drawing in
        if (drawProgress > 0.05f) {
            val wingX = 4.2.dp.toPx() * drawProgress
            val wingY = 3.2.dp.toPx() * drawProgress
            drawLine(
                color = tint,
                start = Offset(bottomEndX + wingX, bottomY - wingY),
                end = Offset(bottomEndX, bottomY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = tint,
                start = Offset(bottomEndX + wingX, bottomY + wingY),
                end = Offset(bottomEndX, bottomY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

// 2. Living Shift Icon: Outer rounded square scales up with spring bounce, internal + and - pop in sequentially (staggered by 60ms) and + does crisp 90deg rotation
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
                    animationSpec = spring(dampingRatio = 0.62f, stiffness = 400f) // Crisp 90° rotation
                )
            }
            launch {
                delay(60) // Staggered by 60ms
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

    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        val scale = outerScale.value
        val boxDim = 19.5.dp.toPx() * scale
        val boxLeft = cx - boxDim / 2f
        val boxTop = cy - boxDim / 2f
        val corner = 4.5.dp.toPx()

        // Outer rounded square of calculator/math grid
        drawRoundRect(
            color = tint,
            topLeft = Offset(boxLeft, boxTop),
            size = Size(boxDim, boxDim),
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        )

        // Subtle divider separating + and -
        drawLine(
            color = tint.copy(alpha = 0.45f),
            start = Offset(boxLeft + 2.dp.toPx(), cy),
            end = Offset(boxLeft + boxDim - 2.dp.toPx(), cy),
            strokeWidth = 1.2.dp.toPx()
        )

        // Upper section: + symbol with 90° rotation and scale pop
        val plusCenterY = boxTop + boxDim * 0.27f
        val plusArm = 3.2.dp.toPx() * plusScale.value
        rotate(degrees = plusRotation.value, pivot = Offset(cx, plusCenterY)) {
            // Horizontal arm
            drawLine(
                color = if (isSelected) NeonCyan else tint,
                start = Offset(cx - plusArm, plusCenterY),
                end = Offset(cx + plusArm, plusCenterY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Vertical arm
            drawLine(
                color = if (isSelected) NeonCyan else tint,
                start = Offset(cx, plusCenterY - plusArm),
                end = Offset(cx, plusCenterY + plusArm),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Lower section: - symbol popping in staggered by 60ms
        val minusCenterY = boxTop + boxDim * 0.73f
        val minusArm = 3.2.dp.toPx() * minusScale.value
        drawLine(
            color = if (isSelected) NeonCyan else tint,
            start = Offset(cx - minusArm, minusCenterY),
            end = Offset(cx + minusArm, minusCenterY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

// 3. Living Hourglass Icon: 180deg spring flip, bottom bulb fills with glowing cyan accent (fill-opacity 0 to 0.4) to simulate falling sand
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

    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        rotate(degrees = flipDegrees, pivot = Offset(cx, cy)) {
            // Full hourglass frame path
            val framePath = Path().apply {
                // Top chamber
                moveTo(w * 0.22f, h * 0.18f)
                lineTo(w * 0.78f, h * 0.18f)
                lineTo(cx + 1.2.dp.toPx(), cy)
                // Bottom chamber
                lineTo(w * 0.78f, h * 0.82f)
                lineTo(w * 0.22f, h * 0.82f)
                lineTo(cx - 1.2.dp.toPx(), cy)
                close()
            }

            // Bottom bulb filling with glowing cyan accent (fill-opacity transitioning 0 to 0.4)
            if (fillOpacity > 0.01f) {
                val bottomChamberFill = Path().apply {
                    moveTo(cx, cy + 1.2.dp.toPx())
                    lineTo(w * 0.74f, h * 0.80f)
                    lineTo(w * 0.26f, h * 0.80f)
                    close()
                }
                drawPath(
                    path = bottomChamberFill,
                    color = NeonCyan.copy(alpha = fillOpacity)
                )
            }

            // Draw outer glass contour
            drawPath(
                path = framePath,
                color = tint,
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
            )

            // Top and bottom protective caps
            drawLine(
                color = tint,
                start = Offset(w * 0.18f, h * 0.18f),
                end = Offset(w * 0.82f, h * 0.18f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = tint,
                start = Offset(w * 0.18f, h * 0.82f),
                end = Offset(w * 0.82f, h * 0.82f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Dynamic falling sand particle stream
            if (isSelected && sandFlow.value < 0.95f) {
                val streamY = cy + (sandFlow.value * 4.8.dp.toPx())
                drawCircle(
                    color = NeonCyan,
                    radius = 1.3.dp.toPx(),
                    center = Offset(cx, streamY)
                )
            }
        }
    }
}

// 4. Living Widgets Icon: 4 mini-squares expand outward, top-right tile detaches, rotates 45deg into diamond, and fills with soft cyan glow
@Composable
fun LivingWidgetsIcon(
    isSelected: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val spreadOffset by animateFloatAsState(
        targetValue = if (isSelected) 1.5.dp.value else 0f,
        animationSpec = spring(dampingRatio = 0.68f, stiffness = 420f),
        label = "widgets_spread"
    )

    val diamondRotation by animateFloatAsState(
        targetValue = if (isSelected) 45f else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "widget_diamond_rot"
    )

    val diamondDetach by animateFloatAsState(
        targetValue = if (isSelected) 2.6.dp.value else 0f,
        animationSpec = spring(dampingRatio = 0.68f, stiffness = 420f),
        label = "widget_diamond_detach"
    )

    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val tileSize = 6.8.dp.toPx()
        val corner = 2.2.dp.toPx()
        val spread = spreadOffset.dp.toPx()
        val detach = diamondDetach.dp.toPx()

        // 1. Top-Left Tile (Expands slightly top-left)
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.18f - spread, h * 0.18f - spread),
            size = Size(tileSize, tileSize),
            cornerRadius = CornerRadius(corner, corner)
        )

        // 2. Bottom-Left Tile (Expands slightly bottom-left)
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.18f - spread, h * 0.54f + spread),
            size = Size(tileSize, tileSize),
            cornerRadius = CornerRadius(corner, corner)
        )

        // 3. Bottom-Right Tile (Expands slightly bottom-right)
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.54f + spread, h * 0.54f + spread),
            size = Size(tileSize, tileSize),
            cornerRadius = CornerRadius(corner, corner)
        )

        // 4. Top-Right Tile: Detaches further, rotates 45° into diamond, fills with soft cyan glow!
        val trCenterX = (w * 0.54f + tileSize / 2f) + spread + detach
        val trCenterY = (h * 0.18f + tileSize / 2f) - spread - detach

        rotate(degrees = diamondRotation, pivot = Offset(trCenterX, trCenterY)) {
            // Soft cyan glow aura behind the rotated diamond
            if (isSelected) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.45f), Color.Transparent),
                        center = Offset(trCenterX, trCenterY),
                        radius = tileSize * 1.3f
                    ),
                    radius = tileSize * 1.3f,
                    center = Offset(trCenterX, trCenterY)
                )
            }

            drawRoundRect(
                color = if (isSelected) NeonCyan else tint,
                topLeft = Offset(trCenterX - tileSize / 2f, trCenterY - tileSize / 2f),
                size = Size(tileSize, tileSize),
                cornerRadius = CornerRadius(
                    if (isSelected) 1.6.dp.toPx() else corner,
                    if (isSelected) 1.6.dp.toPx() else corner
                )
            )
        }
    }
}
