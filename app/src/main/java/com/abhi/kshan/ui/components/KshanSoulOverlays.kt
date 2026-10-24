package com.abhi.kshan.ui.components

import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.StrokeCap
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
import com.abhi.kshan.ui.theme.ComicNeueFontFamily
import com.abhi.kshan.ui.theme.CourierPrimeFontFamily
import com.abhi.kshan.ui.theme.KalamFont
import com.abhi.kshan.ui.theme.NeonBlue
import com.abhi.kshan.ui.theme.NeonCyan
import com.abhi.kshan.ui.theme.NeonEmerald
import com.abhi.kshan.ui.theme.NeonPink
import com.abhi.kshan.ui.theme.NeonPurple
import com.abhi.kshan.ui.theme.SpaceBlack
import com.abhi.kshan.ui.theme.SpaceCard
import com.abhi.kshan.ui.theme.SpaceCardBorder
import com.abhi.kshan.ui.theme.SpaceDark
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val GoldSolar = Color(0xFFFFD700)
private val AmberWarm = Color(0xFFFFA000)

/**
 * Floating "Present Moment (वर्तमान क्षण)" Overlay Card.
 *
 * Rendered at `Modifier.zIndex(10f)` over a translucent tap-outside dismiss scrim so
 * expanding it never pushes or squishes the active screen layout underneath.
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
                .background(Color.Black.copy(alpha = 0.62f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .testTag("present_kshan_scrim")
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f),
                    initialOffsetY = { -it / 2 }
                ) + scaleIn(
                    initialScale = 0.94f,
                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f)
                ) + fadeIn(tween(200)),
                exit = slideOutVertically(
                    animationSpec = tween(180),
                    targetOffsetY = { -it / 2 }
                ) + scaleOut(targetScale = 0.95f, animationSpec = tween(180)) + fadeOut(tween(160)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 68.dp, start = 18.dp, end = 18.dp)
            ) {
                // Consume clicks inside the card so tapping the card doesn't trigger the scrim dismiss
                FrostedGlassCard(
                    borderColor = GoldSolar,
                    glowAlpha = 0.6f,
                    cornerRadius = 26.dp,
                    modifier = Modifier
                        .fillMaxWidth()
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
                        // Top Header: वर्तमान क्षण + Close
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
                                        .background(GoldSolar.copy(alpha = 0.16f))
                                        .border(1.dp, GoldSolar.copy(alpha = 0.55f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WbSunny,
                                        contentDescription = null,
                                        tint = GoldSolar,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "वर्तमान क्षण • Present Kshan",
                                        fontFamily = KalamFont,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldSolar
                                    )
                                    Text(
                                        text = "Live Solar & Seasonal Pulse of Today",
                                        fontFamily = ComicNeueFontFamily,
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

                        // Today Solar Elapsed Progress
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SpaceDark.copy(alpha = 0.9f))
                                .border(1.dp, SpaceCardBorder, RoundedCornerShape(16.dp))
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
                                        fontFamily = ComicNeueFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp,
                                        color = NeonCyan
                                    )
                                    Text(
                                        text = String.format(Locale.US, "%.2f%% ELAPSED", telemetry.dayElapsedPercent),
                                        fontFamily = CourierPrimeFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldSolar
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = { (telemetry.dayElapsedPercent / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = GoldSolar,
                                    trackColor = SpaceCardBorder
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
                                        color = NeonCyan
                                    )
                                }
                            }
                        }

                        // Indian Ritu (ऋतु) & Cosmic Season Badge
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            NeonCyan.copy(alpha = 0.12f),
                                            NeonPurple.copy(alpha = 0.12f)
                                        )
                                    )
                                )
                                .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = telemetry.rituNameDevanagari,
                                        fontFamily = KalamFont,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "• ${telemetry.rituNameEnglish}",
                                        fontFamily = ComicNeueFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = telemetry.cosmicSubtitle,
                                    fontFamily = ComicNeueFontFamily,
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
                                    fontFamily = ComicNeueFontFamily,
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldSolar,
                                unfocusedBorderColor = SpaceCardBorder,
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
                            accentColor = GoldSolar,
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
 * Shares the story and creative philosophy of developer **Abhinav Yaduvanshi**
 * with interactive orbital gyroscope animations, touch-reactive chronon sparks,
 * and kinetic horology cards.
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
        enter = fadeIn(tween(280)) + scaleIn(
            initialScale = 0.92f,
            animationSpec = spring(dampingRatio = 0.76f, stiffness = 360f)
        ),
        exit = fadeOut(tween(220)) + scaleOut(targetScale = 0.94f, animationSpec = tween(200)),
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
            initialValue = 0.45f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "aura_pulse"
        )

        val sparks = remember { mutableStateListOf<VisionSpark>() }
        var burstCounter by remember { mutableIntStateOf(0) }

        fun spawnVisionBurst(centerX: Float, centerY: Float, count: Int = 36) {
            val palette = listOf(GoldSolar, NeonCyan, NeonPurple, NeonPink, Color.White)
            repeat(count) {
                val angle = Random.nextFloat() * (2f * Math.PI.toFloat())
                val speed = Random.nextFloat() * 7.5f + 1.8f
                sparks.add(
                    VisionSpark(
                        x = centerX,
                        y = centerY,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed,
                        radius = Random.nextFloat() * 4.5f + 2f,
                        color = palette[Random.nextInt(palette.size)],
                        alpha = 1f
                    )
                )
            }
            if (sparks.size > 220) {
                sparks.removeRange(0, sparks.size - 220)
            }
        }

        // 60 FPS particle physics loop while sparks are active
        LaunchedEffect(visible) {
            // Initial welcome burst
            spawnVisionBurst(500f, 380f, 42)
            while (visible) {
                withFrameNanos {
                    val iterator = sparks.listIterator()
                    while (iterator.hasNext()) {
                        val s = iterator.next()
                        val nextAlpha = s.alpha - 0.022f
                        if (nextAlpha <= 0f) {
                            iterator.remove()
                        } else {
                            iterator.set(
                                s.copy(
                                    x = s.x + s.vx,
                                    y = s.y + s.vy + 0.08f,
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
                            Color(0xFF040711),
                            Color(0xFF090E1E),
                            Color(0xFF040711)
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
                        spawnVisionBurst(offset.x, offset.y, 28)
                    }
                }
                .testTag("developer_vision_modal")
        ) {
            // Background Interactive Orbital Astrolabe & Spark Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = 210.dp.toPx()

                // Ambient Golden-Cyan Nebula
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoldSolar.copy(alpha = 0.14f * auraPulse),
                            NeonCyan.copy(alpha = 0.08f * auraPulse),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = 240.dp.toPx()
                    ),
                    center = Offset(cx, cy),
                    radius = 240.dp.toPx()
                )

                // Outer Rotating Astrolabe Ring
                rotate(degrees = outerRingAngle, pivot = Offset(cx, cy)) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                GoldSolar.copy(alpha = 0.55f),
                                Color.Transparent,
                                NeonCyan.copy(alpha = 0.55f),
                                Color.Transparent,
                                GoldSolar.copy(alpha = 0.55f)
                            ),
                            center = Offset(cx, cy)
                        ),
                        radius = 122.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(
                            width = 1.6.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
                        )
                    )
                }

                // Inner Counter-Rotating Horology Ring
                rotate(degrees = innerRingAngle, pivot = Offset(cx, cy)) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = 0.65f),
                                NeonPurple.copy(alpha = 0.35f),
                                GoldSolar.copy(alpha = 0.65f),
                                NeonCyan.copy(alpha = 0.65f)
                            ),
                            center = Offset(cx, cy)
                        ),
                        radius = 86.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(
                            width = 1.2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 10f), 0f)
                        )
                    )
                }

                // Render Interactive Chronon Sparks
                sparks.forEach { s ->
                    drawCircle(
                        color = s.color.copy(alpha = (s.alpha * 0.35f).coerceIn(0f, 1f)),
                        radius = s.radius * 2.4f,
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
                            .background(GoldSolar.copy(alpha = 0.14f))
                            .border(1.dp, GoldSolar.copy(alpha = 0.5f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "✦ CREATOR & VISION STUDIO",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = GoldSolar
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SpaceCard)
                            .border(1.dp, SpaceCardBorder, CircleShape)
                            .testTag("close_vision_modal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Vision Modal",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Sculptural Center Crest (क्षण + AY Monogram)
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .graphicsLayer {
                            val s = 0.97f + 0.05f * auraPulse
                            scaleX = s
                            scaleY = s
                        }
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF162038),
                                    Color(0xFF0A0F1E)
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                listOf(GoldSolar, NeonCyan, NeonPurple, GoldSolar)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "क्षण",
                            fontFamily = KalamFont,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White, GoldSolar, NeonCyan)
                                )
                            )
                        )
                        Text(
                            text = "ABHINAV • Y",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp,
                            color = NeonCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Developer Name & Visionary Subtitle
                Text(
                    text = "Abhinav Yaduvanshi",
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.White, GoldSolar, NeonCyan)
                        )
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Creator & Chief Time Architect of क्षण (Kshan)",
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonCyan,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Live Atomic Heartbeat Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(SpaceCard.copy(alpha = 0.9f))
                        .border(1.dp, SpaceCardBorder, RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "LIVE CHRONON EPOCH • $epochMillis ms",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldSolar
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Philosophy Card 1: The Soul of Kshan
                FrostedGlassCard(
                    borderColor = GoldSolar,
                    glowAlpha = 0.45f,
                    cornerRadius = 24.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GoldSolar,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "THE VISION BEHIND क्षण (KSHAN)",
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.4.sp,
                                color = GoldSolar
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"In Sanskrit, 'Kshan' (क्षण) is not merely a mechanical tick of a clock — it is an auspicious, indivisible atom of lived consciousness. Every ordinary date calculator treats time like a sterile spreadsheet. My vision for Kshan was to sculpt time into a living, tactile instrument you can feel under your thumb.\"",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Philosophy Card 2: Tactile Horology & Indian Astronomy
                FrostedGlassCard(
                    borderColor = NeonCyan,
                    glowAlpha = 0.45f,
                    cornerRadius = 24.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TACTILE HOROLOGY MEETS COSMIC RHYTHM",
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.4.sp,
                                color = NeonCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Inspired by precision Leica rangefinders, luxury watch crowns, and classical Indian Ritu (ऋतु) seasonal astronomy, Kshan bridges analytical date math with human emotion — turning raw days into heartbeats, lunar cycles, 365-dot matrices, and acoustic sky fireworks.",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 13.5.sp,
                            color = TextSecondary,
                            lineHeight = 19.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Signature Engineering Pillars
                FrostedGlassCard(
                    borderColor = NeonPurple,
                    glowAlpha = 0.4f,
                    cornerRadius = 24.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = NeonPink,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SIGNATURE CRAFT BY ABHINAV YADUVANSHI",
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.3.sp,
                                color = NeonPurple
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        val pillars = listOf(
                            "✦ Crown Haptic Scrubber",
                            "✦ Temporal Constellation Arc",
                            "✦ वर्तमान क्षण Ritu Pulse",
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
                                        .border(1.dp, NeonPurple.copy(alpha = 0.45f), RoundedCornerShape(50))
                                        .padding(horizontal = 11.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = badge,
                                        fontFamily = CourierPrimeFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
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
                                centerX = Random.nextFloat() * 600f + 180f,
                                centerY = Random.nextFloat() * 500f + 160f,
                                count = 52
                            )
                        },
                        isPrimary = true,
                        accentColor = GoldSolar,
                        modifier = Modifier.weight(1.2f),
                        testTag = "vision_ignite_spark_button"
                    )

                    ModernPillButton(
                        text = "Close",
                        onClick = onDismiss,
                        isPrimary = false,
                        accentColor = NeonCyan,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Tap anywhere on the starfield to release chronon sparks • Designed & Engineered by Abhinav Yaduvanshi",
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 11.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
