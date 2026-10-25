package com.abhi.kshan.ui.components

import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.abhi.kshan.engine.PresentKshanTelemetry
import com.abhi.kshan.ui.theme.BrushedSilver
import com.abhi.kshan.ui.theme.CourierPrimeFontFamily
import com.abhi.kshan.ui.theme.IceCyan
import com.abhi.kshan.ui.theme.KalamFont
import com.abhi.kshan.ui.theme.ObsidianGlass
import com.abhi.kshan.ui.theme.SpaceDark
import com.abhi.kshan.ui.theme.SpaceGroteskFontFamily
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.ui.theme.TextSecondary
import com.abhi.kshan.ui.theme.TitaniumHairline
import com.abhi.kshan.ui.theme.WarmChampagne
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Floating "Present Moment (क्षण)" Overlay Card.
 *
 * Uses Warm Champagne (#E5C158) solar accents and quiet Titanium glass cards (#0A0E17 @ 85%)
 * with `BackHandler` and `imePadding()` so the keyboard never obscures the note input.
 */
@Composable
fun PresentKshanDropdownCard(
    visible: Boolean,
    telemetry: PresentKshanTelemetry,
    onCaptureKshan: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var customNote by remember { mutableStateOf("") }

    if (visible) {
        BackHandler { onDismiss() }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)),
        exit = fadeOut(tween(180)),
        modifier = modifier
            .fillMaxSize()
            .zIndex(10f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .imePadding()
                .testTag("present_kshan_scrim")
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f),
                    initialOffsetY = { -it / 2 }
                ) + scaleIn(
                    initialScale = 0.95f,
                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f)
                ) + fadeIn(tween(200)),
                exit = slideOutVertically(
                    animationSpec = tween(180),
                    targetOffsetY = { -it / 2 }
                ) + scaleOut(targetScale = 0.96f, animationSpec = tween(180)) + fadeOut(tween(160)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 66.dp, start = 18.dp, end = 18.dp)
            ) {
                FrostedGlassCard(
                    borderColor = WarmChampagne,
                    glowAlpha = 0.6f,
                    cornerRadius = 24.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                        .testTag("present_kshan_dropdown_card")
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Top Header: क्षण + Present Kshan
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(WarmChampagne.copy(alpha = 0.14f))
                                        .border(0.8.dp, WarmChampagne.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WbSunny,
                                        contentDescription = null,
                                        tint = WarmChampagne,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "क्षण",
                                            fontFamily = KalamFont,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WarmChampagne
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• Present Kshan",
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    }
                                    Text(
                                        text = "Live Solar & Seasonal Pulse of Today",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 11.5.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    try {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    } catch (e: Exception) {
                                        Log.w("PresentKshanCard", "Haptic unavailable", e)
                                    }
                                    onDismiss()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Present Kshan",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Today Solar Elapsed Progress (Warm Champagne Bar)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SpaceDark)
                                .border(0.8.dp, TitaniumHairline, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "TODAY'S SOLAR ARC",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.2.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = String.format(Locale.US, "%.2f%% ELAPSED", telemetry.dayElapsedPercent),
                                        fontFamily = CourierPrimeFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WarmChampagne
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = { (telemetry.dayElapsedPercent / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = WarmChampagne,
                                    trackColor = TitaniumHairline
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "%,ds lived today", telemetry.secondsIntoDay),
                                        fontFamily = CourierPrimeFontFamily,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = String.format(Locale.US, "%,ds remaining", telemetry.secondsRemainingInDay),
                                        fontFamily = CourierPrimeFontFamily,
                                        fontSize = 11.sp,
                                        color = BrushedSilver
                                    )
                                }
                            }
                        }

                        // Quiet Titanium Glass Seasonal Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SpaceDark)
                                .border(0.8.dp, TitaniumHairline, RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${telemetry.rituNameDevanagari} • ${telemetry.rituNameEnglish}",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = telemetry.cosmicSubtitle,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.5.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Optional Kshan Memory Note
                        OutlinedTextField(
                            value = customNote,
                            onValueChange = { customNote = it },
                            placeholder = {
                                Text(
                                    text = "Name this auspicious moment (optional)...",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WarmChampagne,
                                unfocusedBorderColor = TitaniumHairline,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("capture_kshan_note_input")
                        )

                        // 1-Tap "Capture This Kshan" Action Button
                        ModernPillButton(
                            text = "✦ Capture This Kshan",
                            icon = Icons.Default.AutoAwesome,
                            onClick = {
                                try {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                } catch (e: Exception) {
                                    Log.w("PresentKshanCard", "Haptic unavailable", e)
                                }
                                onCaptureKshan(customNote)
                                customNote = ""
                                onDismiss()
                            },
                            isPrimary = true,
                            accentColor = WarmChampagne,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "capture_present_kshan_button"
                        )
                    }
                }
            }
        }
    }
}

private data class VisionSpark(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    var color: Color,
    var alpha: Float = 1f
)

/**
 * Full-Screen Interactive Developer & Vision Showcase Modal ("✦ Vision").
 *
 * - Orbital dashed astrolabe rings are strictly constrained inside the top 120.dp `क्षण`
 *   emblem box so they never overlap or cut through the "Abhinav Yaduvanshi" title text.
 * - Uses unified Warm Champagne (#E5C158) and Brushed Silver (#CBD5E1) accents on quiet
 *   Obsidian Glass cards.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeveloperVisionModal(
    visible: Boolean,
    epochMillis: Long,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    if (visible) {
        BackHandler { onDismiss() }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(260)) + scaleIn(
            initialScale = 0.94f,
            animationSpec = spring(dampingRatio = 0.78f, stiffness = 380f)
        ),
        exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.95f, animationSpec = tween(180)),
        modifier = modifier
            .fillMaxSize()
            .zIndex(20f)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "vision_orbital_transition")
        val outerRingAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 22000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "outer_ring_angle"
        )
        val innerRingAngle by infiniteTransition.animateFloat(
            initialValue = 360f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 15000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "inner_ring_angle"
        )
        val auraPulse by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "aura_pulse"
        )

        val sparks = remember { mutableStateListOf<VisionSpark>() }
        var burstCounter by remember { mutableIntStateOf(0) }

        fun spawnVisionBurst(centerX: Float, centerY: Float, count: Int = 32) {
            val palette = listOf(WarmChampagne, BrushedSilver, IceCyan, Color.White)
            repeat(count) {
                val angle = Random.nextFloat() * (2f * Math.PI.toFloat())
                val speed = Random.nextFloat() * 6.5f + 1.5f
                sparks.add(
                    VisionSpark(
                        x = centerX,
                        y = centerY,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed,
                        radius = Random.nextFloat() * 3.8f + 1.8f,
                        color = palette[Random.nextInt(palette.size)],
                        alpha = 1f
                    )
                )
            }
            if (sparks.size > 180) {
                sparks.removeRange(0, sparks.size - 180)
            }
        }

        // 60 FPS particle physics loop strictly while `visible == true`
        LaunchedEffect(visible) {
            if (!visible) return@LaunchedEffect
            spawnVisionBurst(500f, 240f, 34)
            while (visible) {
                withFrameNanos {
                    val iterator = sparks.listIterator()
                    while (iterator.hasNext()) {
                        val s = iterator.next()
                        val nextAlpha = s.alpha - 0.024f
                        if (nextAlpha <= 0f) {
                            iterator.remove()
                        } else {
                            iterator.set(
                                s.copy(
                                    x = s.x + s.vx,
                                    y = s.y + s.vy + 0.07f,
                                    vx = s.vx * 0.97f,
                                    vy = s.vy * 0.97f,
                                    alpha = nextAlpha
                                )
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF04060C),
                            Color(0xFF080C16),
                            Color(0xFF04060C)
                        )
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        try {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        } catch (e: Exception) {
                            Log.w("DeveloperVisionModal", "Haptic unavailable", e)
                        }
                        spawnVisionBurst(offset.x, offset.y, 24)
                    }
                }
                .testTag("developer_vision_modal")
        ) {
            // Subtle Touch-Reactive Chronon Sparks Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                sparks.forEach { s ->
                    drawCircle(
                        color = s.color.copy(alpha = (s.alpha * 0.30f).coerceIn(0f, 1f)),
                        radius = s.radius * 2.2f,
                        center = Offset(s.x, s.y)
                    )
                    drawCircle(
                        color = s.color.copy(alpha = s.alpha.coerceIn(0f, 1f)),
                        radius = s.radius,
                        center = Offset(s.x, s.y)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(ObsidianGlass)
                            .border(0.8.dp, WarmChampagne.copy(alpha = 0.45f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "✦ CREATOR & VISION STUDIO",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = WarmChampagne
                        )
                    }

                    IconButton(
                        onClick = {
                            try {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            } catch (e: Exception) {
                                Log.w("DeveloperVisionModal", "Haptic unavailable", e)
                            }
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ObsidianGlass)
                            .border(0.8.dp, TitaniumHairline, CircleShape)
                            .testTag("close_vision_modal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Vision Modal",
                            tint = TextPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Strictly Contained 120.dp Emblem & Orbital Astrolabe Box (never overlaps title below)
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f

                        // Soft Champagne Radial Glow inside the 120.dp box
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    WarmChampagne.copy(alpha = 0.16f * auraPulse),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy),
                                radius = size.minDimension / 2f
                            ),
                            center = Offset(cx, cy),
                            radius = size.minDimension / 2f
                        )

                        // Outer Rotating Dashed Ring strictly within 56.dp radius
                        rotate(degrees = outerRingAngle, pivot = Offset(cx, cy)) {
                            drawCircle(
                                color = WarmChampagne.copy(alpha = 0.45f),
                                radius = 56.dp.toPx(),
                                center = Offset(cx, cy),
                                style = Stroke(
                                    width = 1.1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                                )
                            )
                        }

                        // Inner Counter-Rotating Dashed Ring strictly within 46.dp radius
                        rotate(degrees = innerRingAngle, pivot = Offset(cx, cy)) {
                            drawCircle(
                                color = BrushedSilver.copy(alpha = 0.40f),
                                radius = 46.dp.toPx(),
                                center = Offset(cx, cy),
                                style = Stroke(
                                    width = 0.9.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f)
                                )
                            )
                        }
                    }

                    // Center 82.dp Crest
                    Box(
                        modifier = Modifier
                            .size(82.dp)
                            .graphicsLayer {
                                val s = 0.98f + 0.03f * auraPulse
                                scaleX = s
                                scaleY = s
                            }
                            .clip(CircleShape)
                            .background(Color(0xFF0A0E17))
                            .border(
                                width = 1.dp,
                                color = WarmChampagne.copy(alpha = 0.55f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "क्षण",
                                fontFamily = KalamFont,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmChampagne
                            )
                            Text(
                                text = "AY",
                                fontFamily = CourierPrimeFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.4.sp,
                                color = BrushedSilver
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Developer Name & Visionary Subtitle (completely clear of any orbital rings)
                Text(
                    text = "Abhinav Yaduvanshi",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Creator & Chief Time Architect of Kshan",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = WarmChampagne,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Live Atomic Heartbeat Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(ObsidianGlass)
                        .border(0.8.dp, TitaniumHairline, RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "LIVE CHRONON EPOCH • $epochMillis ms",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrushedSilver
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Philosophy Card 1: The Soul of Kshan (Champagne Accent)
                FrostedGlassCard(
                    borderColor = TitaniumHairline,
                    glowAlpha = 0.25f,
                    cornerRadius = 22.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = WarmChampagne,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "THE VISION BEHIND KSHAN",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.3.sp,
                                color = WarmChampagne
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"In Sanskrit, 'Kshan' is not merely a mechanical tick of a clock — it is an auspicious, indivisible atom of lived consciousness. Every ordinary date calculator treats time like a sterile spreadsheet. My vision for Kshan was to sculpt time into a quiet, tactile instrument you can feel under your thumb.\"",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 13.5.sp,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Philosophy Card 2: Tactile Horology & Indian Astronomy (Silver Accent)
                FrostedGlassCard(
                    borderColor = TitaniumHairline,
                    glowAlpha = 0.25f,
                    cornerRadius = 22.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = BrushedSilver,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TACTILE HOROLOGY MEETS COSMIC RHYTHM",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.3.sp,
                                color = BrushedSilver
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Inspired by precision Leica rangefinders, Swiss watch crowns, and classical Indian Ritu seasonal astronomy, Kshan bridges analytical date math with human perspective — turning raw days into workdays, 365-dot matrices, and acoustic sky fireworks.",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 19.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Philosophy Card 3: Signature Engineering Pillars (Champagne & Silver)
                FrostedGlassCard(
                    borderColor = TitaniumHairline,
                    glowAlpha = 0.25f,
                    cornerRadius = 22.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = WarmChampagne,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SIGNATURE CRAFT BY ABHINAV YADUVANSHI",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.3.sp,
                                color = WarmChampagne
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        val pillars = listOf(
                            "✦ 52.dp Crown Haptic Scrubber",
                            "✦ 3-Metric Precision Strip",
                            "✦ Present Kshan Solar Pulse",
                            "✦ 365-Dot Year Matrix",
                            "✦ 6-Shell Ballistic Fireworks",
                            "✦ Live OS Widget Chronometer"
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pillars.forEach { badge ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(SpaceDark)
                                        .border(0.8.dp, TitaniumHairline, RoundedCornerShape(50))
                                        .padding(horizontal = 11.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = badge,
                                        fontFamily = CourierPrimeFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrushedSilver
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Interactive Celebration Spark Button inside Vision Studio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernPillButton(
                        text = if (burstCounter == 0) "Ignite Cosmic Spark" else "Spark Ignited ($burstCounter) ✦",
                        icon = Icons.Default.AutoAwesome,
                        onClick = {
                            burstCounter++
                            spawnVisionBurst(
                                centerX = Random.nextFloat() * 550f + 180f,
                                centerY = Random.nextFloat() * 420f + 160f,
                                count = 44
                            )
                        },
                        isPrimary = true,
                        accentColor = WarmChampagne,
                        modifier = Modifier.weight(1.2f),
                        testTag = "vision_ignite_spark_button"
                    )

                    ModernPillButton(
                        text = "Close",
                        onClick = onDismiss,
                        isPrimary = false,
                        accentColor = BrushedSilver,
                        modifier = Modifier.weight(0.8f),
                        testTag = "vision_bottom_close_button"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
