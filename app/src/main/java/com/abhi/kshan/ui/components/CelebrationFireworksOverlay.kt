package com.abhi.kshan.ui.components

import android.content.Intent
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.kshan.ui.theme.ComicNeueFontFamily
import com.abhi.kshan.ui.theme.CourierPrimeFontFamily
import com.abhi.kshan.ui.theme.KalamFont
import com.abhi.kshan.ui.theme.NeonBlue
import com.abhi.kshan.ui.theme.NeonCyan
import com.abhi.kshan.ui.theme.NeonEmerald
import com.abhi.kshan.ui.theme.NeonPink
import com.abhi.kshan.ui.theme.NeonPurple
import com.abhi.kshan.ui.theme.SpaceCard
import com.abhi.kshan.ui.theme.SpaceCardBorder
import com.abhi.kshan.ui.theme.SpaceDark
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

private val GoldMetallic = Color(0xFFFFD700)
private val DeepIndigo = Color(0xFF0A0F1D)

@Composable
fun CelebrationFireworksOverlay(
    visible: Boolean,
    eventTitle: String,
    category: String = "Celebration",
    remainingSeconds: Long = 0L,
    remainingTimeFormatted: String = "",
    powerSaverEnabled: Boolean = false,
    onDismiss: () -> Unit,
    onTriggerFireworks: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val theme = remember(category, eventTitle) { resolveCelebrationTheme(category, eventTitle) }
    val isPreviewMode = remainingSeconds > 0L

    var isCardMinimized by remember(visible) { mutableStateOf(false) }
    var soundAndHapticsEnabled by remember { mutableStateOf(true) }
    var autoFinaleShowEnabled by remember { mutableStateOf(false) }
    var liveFps by remember { mutableIntStateOf(60) }
    var activeSparksCount by remember { mutableIntStateOf(0) }

    var canvasWidth by remember { mutableFloatStateOf(1080f) }
    var canvasHeight by remember { mutableFloatStateOf(2200f) }

    val engine = remember { PyroSimulationEngine() }

    val palette = remember(theme) {
        listOf(
            theme.primaryColor,
            theme.secondaryColor,
            GoldMetallic,
            NeonCyan,
            NeonPink,
            NeonPurple,
            NeonEmerald,
            NeonBlue,
            Color(0xFFFFE066),
            Color.White
        )
    }

    fun launchSalvo(salvoCount: Int, isCardOpen: Boolean) {
        coroutineScope.launch {
            for (i in 0 until salvoCount) {
                val startX = canvasWidth * (0.14f + Random.nextFloat() * 0.72f)
                val startY = canvasHeight * 0.98f
                val targetX = if (isCardOpen) {
                    if (i % 2 == 0) canvasWidth * (0.12f + Random.nextFloat() * 0.25f)
                    else canvasWidth * (0.63f + Random.nextFloat() * 0.25f)
                } else {
                    canvasWidth * (0.12f + Random.nextFloat() * 0.76f)
                }
                val targetY = if (isCardOpen) {
                    canvasHeight * (0.10f + Random.nextFloat() * 0.16f)
                } else {
                    canvasHeight * (0.12f + Random.nextFloat() * 0.36f)
                }
                val shape = PyroBurstShape.values()[i % PyroBurstShape.values().size]
                engine.spawnRocket(
                    startX = startX,
                    startY = startY,
                    targetX = targetX,
                    targetY = targetY,
                    canvasHeight = canvasHeight,
                    palette = palette,
                    soundEnabled = soundAndHapticsEnabled,
                    view = view,
                    forcedShape = shape,
                    burstScale = if (i == salvoCount - 1) 1.2f else 1.0f
                )
                delay(155L)
            }
        }
    }

    // Initial launch sequence when opened
    LaunchedEffect(visible) {
        if (visible) {
            isCardMinimized = false
            launchSalvo(salvoCount = if (isPreviewMode) 5 else 8, isCardOpen = true)
        } else {
            autoFinaleShowEnabled = false
            engine.clearAll()
        }
    }

    // Auto-Choreographed Finale Show Mode Loop
    LaunchedEffect(visible, autoFinaleShowEnabled, powerSaverEnabled) {
        if (!visible || !autoFinaleShowEnabled) return@LaunchedEffect
        var waveIndex = 0
        while (isActive) {
            val salvoSize = if (powerSaverEnabled) 2 else (3 + (waveIndex % 3))
            launchSalvo(salvoCount = salvoSize, isCardOpen = !isCardMinimized)
            waveIndex++
            delay(if (powerSaverEnabled) 1800L else 1250L)
        }
    }

    // 60/120fps Physics & Render Loop
    var tickTrigger by remember { mutableLongStateOf(0L) }
    var starPhase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(visible, powerSaverEnabled) {
        if (!visible) return@LaunchedEffect
        var lastFrameNanos = 0L
        var fpsFrames = 0
        var fpsAccumNanos = 0L

        while (isActive) {
            withFrameNanos { frameNanos ->
                val dtNanos = if (lastFrameNanos == 0L) 16_666_667L else (frameNanos - lastFrameNanos)
                lastFrameNanos = frameNanos
                val dtScale = (dtNanos / 16_666_667f).coerceIn(0.5f, 2.4f)

                fpsFrames++
                fpsAccumNanos += dtNanos
                if (fpsAccumNanos >= 500_000_000L) {
                    liveFps = ((fpsFrames * 1_000_000_000L) / fpsAccumNanos).toInt().coerceIn(1, 144)
                    fpsFrames = 0
                    fpsAccumNanos = 0L
                }

                starPhase += 0.03f * dtScale
                engine.stepPhysics(
                    dtScale = dtScale,
                    context = context,
                    view = view,
                    soundEnabled = soundAndHapticsEnabled,
                    powerSaver = powerSaverEnabled,
                    canvasHeight = canvasHeight,
                    palette = palette
                )
                activeSparksCount = engine.activeSparksCount
                tickTrigger++
            }
        }
    }

    var lastDragLaunchMs by remember { mutableLongStateOf(0L) }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(240)),
        exit = fadeOut(tween(200)),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // 1. Full-Screen Interactive Ballistic Fireworks Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(soundAndHapticsEnabled) {
                        detectTapGestures { tapOffset ->
                            val startX = canvasWidth * (0.25f + Random.nextFloat() * 0.5f)
                            engine.spawnRocket(
                                startX = startX,
                                startY = canvasHeight * 0.98f,
                                targetX = tapOffset.x,
                                targetY = tapOffset.y,
                                canvasHeight = canvasHeight,
                                palette = palette,
                                soundEnabled = soundAndHapticsEnabled,
                                view = view
                            )
                        }
                    }
                    .pointerInput(soundAndHapticsEnabled) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val nowMs = System.currentTimeMillis()
                            if (nowMs - lastDragLaunchMs > 110L) {
                                lastDragLaunchMs = nowMs
                                val pos = change.position
                                val startX = (pos.x + (Random.nextFloat() - 0.5f) * 160f)
                                    .coerceIn(40f, canvasWidth - 40f)
                                engine.spawnRocket(
                                    startX = startX,
                                    startY = canvasHeight * 0.98f,
                                    targetX = pos.x,
                                    targetY = pos.y,
                                    canvasHeight = canvasHeight,
                                    palette = palette,
                                    soundEnabled = soundAndHapticsEnabled,
                                    view = view
                                )
                            }
                        }
                    }
            ) {
                // Read tickTrigger inside DrawScope to guarantee 60/120fps invalidation
                val frame = tickTrigger
                if (frame >= 0L) {
                    canvasWidth = size.width
                    canvasHeight = size.height
                    engine.drawScene(this, starPhase, powerSaverEnabled)
                }
            }

            // 2. Top Telemetry HUD Bar: Live FPS & Spark Count + Auto Finale Show + Sound/Haptics Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top-Left Typewriter FPS & Spark Telemetry Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(SpaceDark.copy(alpha = 0.85f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("fireworks_fps_hud")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (liveFps >= 50) NeonEmerald else GoldMetallic)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$liveFps FPS • $activeSparksCount SPARKS",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                // Top-Right Controls: Auto Show Toggle + Sound/Haptics Toggle
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Auto Finale Show Mode Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (autoFinaleShowEnabled) GoldMetallic.copy(alpha = 0.22f)
                                else SpaceDark.copy(alpha = 0.85f)
                            )
                            .border(
                                1.dp,
                                if (autoFinaleShowEnabled) GoldMetallic else SpaceCardBorder,
                                RoundedCornerShape(50)
                            )
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                autoFinaleShowEnabled = !autoFinaleShowEnabled
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("fireworks_auto_show_toggle")
                    ) {
                        Text(
                            text = if (autoFinaleShowEnabled) "🎬 Finale: ON" else "🎬 Auto Show",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (autoFinaleShowEnabled) GoldMetallic else TextSecondary
                        )
                    }

                    // Sound & Haptics Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(SpaceDark.copy(alpha = 0.85f))
                            .border(
                                1.dp,
                                if (soundAndHapticsEnabled) NeonCyan.copy(alpha = 0.55f) else SpaceCardBorder,
                                RoundedCornerShape(50)
                            )
                            .clickable {
                                soundAndHapticsEnabled = !soundAndHapticsEnabled
                                if (soundAndHapticsEnabled) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("fireworks_sound_toggle")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (soundAndHapticsEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = "Toggle Fireworks Sound and Haptics",
                                tint = if (soundAndHapticsEnabled) NeonCyan else TextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (soundAndHapticsEnabled) "SFX" else "Mute",
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (soundAndHapticsEnabled) NeonCyan else TextMuted
                            )
                        }
                    }
                }
            }

            // 3. Minimized Floating Pill Bar ("Interactive Sky Mode")
            AnimatedVisibility(
                visible = isCardMinimized,
                enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.9f),
                exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.9f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 50.dp, start = 16.dp, end = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(DeepIndigo.copy(alpha = 0.90f))
                        .border(1.dp, theme.primaryColor.copy(alpha = 0.6f), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "🎆 Tap or drag sky",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(theme.primaryColor.copy(alpha = 0.2f))
                                .clickable {
                                    PyroSoundEngine.triggerLaunchHaptic(view, soundAndHapticsEnabled)
                                    isCardMinimized = false
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("show_celebration_card_pill")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Show Card",
                                    tint = theme.primaryColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Show Card",
                                    fontFamily = ComicNeueFontFamily,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.primaryColor
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(SpaceCard)
                                .clickable {
                                    PyroSoundEngine.triggerLaunchHaptic(view, soundAndHapticsEnabled)
                                    onDismiss()
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("exit_sky_mode_pill")
                        ) {
                            Text(
                                text = "Exit",
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // 4. Center Celebration / Sky Preview Glass Card
            AnimatedVisibility(
                visible = !isCardMinimized,
                enter = fadeIn(tween(240)) + scaleIn(
                    animationSpec = spring(dampingRatio = 0.68f, stiffness = 380f),
                    initialScale = 0.88f
                ),
                exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.9f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    DeepIndigo.copy(alpha = 0.86f),
                                    SpaceDark.copy(alpha = 0.90f)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    theme.primaryColor.copy(alpha = 0.85f),
                                    theme.secondaryColor.copy(alpha = 0.75f),
                                    NeonCyan.copy(alpha = 0.7f),
                                    theme.primaryColor.copy(alpha = 0.85f)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .padding(24.dp)
                ) {
                    IconButton(
                        onClick = {
                            PyroSoundEngine.triggerLaunchHaptic(view, soundAndHapticsEnabled)
                            isCardMinimized = true
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(34.dp)
                            .testTag("minimize_card_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Minimize to Full Sky View",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OrbitalFireworksCrest(theme = theme)

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "क्षण",
                                fontFamily = KalamFont,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.primaryColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPreviewMode) "•  COUNTDOWN SKY PREVIEW" else "•  MILESTONE ARRIVED",
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = theme.primaryColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (eventTitle.isNotBlank()) "“$eventTitle”" else "“Celebration Moment”",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(theme.primaryColor.copy(alpha = 0.14f))
                                .border(1.dp, theme.primaryColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(theme.primaryColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (isPreviewMode) {
                                    Text(
                                        text = remainingTimeFormatted.ifBlank { "Live Countdown Active" },
                                        fontFamily = CourierPrimeFontFamily,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.primaryColor
                                    )
                                } else {
                                    Text(
                                        text = theme.statusPillText,
                                        fontFamily = ComicNeueFontFamily,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.primaryColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Tap anywhere on the sky to launch Crossette, Willow, Saturn & Horsetail shells",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Primary Action: Launch Salvo Rockets
                        val launchInteraction = remember { MutableInteractionSource() }
                        val isLaunchPressed by launchInteraction.collectIsPressedAsState()
                        val launchScale by animateFloatAsState(
                            targetValue = if (isLaunchPressed) 0.94f else 1f,
                            animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
                            label = "launch_btn_scale"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    scaleX = launchScale
                                    scaleY = launchScale
                                }
                                .height(50.dp)
                                .clip(RoundedCornerShape(25.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF00F2FE), Color(0xFF6366F1))
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    brush = Brush.horizontalGradient(
                                        listOf(Color.White.copy(alpha = 0.6f), Color.Transparent)
                                    ),
                                    shape = RoundedCornerShape(25.dp)
                                )
                                .clickable(
                                    interactionSource = launchInteraction,
                                    indication = null
                                ) {
                                    onTriggerFireworks()
                                    launchSalvo(salvoCount = 6, isCardOpen = true)
                                }
                                .testTag("celebration_burst_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isPreviewMode) "Launch Rockets" else theme.burstButtonText,
                                    fontFamily = ComicNeueFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Full Sky View Minimize Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(SpaceCard.copy(alpha = 0.9f))
                                .border(1.dp, theme.primaryColor.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                                .clickable {
                                    PyroSoundEngine.triggerLaunchHaptic(view, soundAndHapticsEnabled)
                                    isCardMinimized = true
                                    launchSalvo(salvoCount = 4, isCardOpen = false)
                                }
                                .testTag("full_sky_view_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.OpenInFull,
                                    contentDescription = null,
                                    tint = theme.primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Full Sky View (Minimize Card)",
                                    fontFamily = ComicNeueFontFamily,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.primaryColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Secondary Actions: Premium Animated Share & Exit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PremiumShareButton(
                                onClick = {
                                    PyroSoundEngine.triggerExplosionHaptics(
                                        context,
                                        view,
                                        soundAndHapticsEnabled,
                                        isHeavy = false
                                    )
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            theme.shareTemplate(eventTitle.ifBlank { "Event" })
                                        )
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Countdown"))
                                },
                                modifier = Modifier.weight(1f)
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(SpaceDark)
                                    .border(1.dp, TextMuted.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                                    .clickable {
                                        PyroSoundEngine.triggerLaunchHaptic(view, soundAndHapticsEnabled)
                                        onDismiss()
                                    }
                                    .testTag("celebration_dismiss_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Close Sky",
                                    fontFamily = ComicNeueFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumShareButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val coroutineScope = rememberCoroutineScope()

    val iconSpin = remember { Animatable(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "share_shimmer")
    val shimmerSweep by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "share_glow"
    )
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "share_press_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        SpaceDark,
                        NeonCyan.copy(alpha = 0.18f),
                        NeonPurple.copy(alpha = 0.18f),
                        SpaceDark
                    ),
                    start = Offset(shimmerSweep - 180f, 0f),
                    end = Offset(shimmerSweep + 180f, 120f)
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        NeonCyan.copy(alpha = glowPulse),
                        NeonPurple.copy(alpha = glowPulse),
                        NeonCyan.copy(alpha = glowPulse)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                coroutineScope.launch {
                    iconSpin.snapTo(0f)
                    iconSpin.animateTo(
                        targetValue = 360f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 350f)
                    )
                }
                onClick()
            }
            .testTag("celebration_share_button"),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = NeonCyan,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer { rotationZ = iconSpin.value }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Share",
                fontFamily = ComicNeueFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
        }
    }
}

@Composable
private fun OrbitalFireworksCrest(
    theme: CategoryCelebrationTheme,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "crest_orbital")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "crest_rot"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crest_pulse"
    )

    Box(
        modifier = modifier.size(86.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(84.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        theme.primaryColor.copy(alpha = 0.34f * pulseScale),
                        theme.secondaryColor.copy(alpha = 0.16f * pulseScale),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = 40.dp.toPx()
                ),
                radius = 40.dp.toPx(),
                center = Offset(cx, cy)
            )
            rotate(degrees = rotationAngle, pivot = Offset(cx, cy)) {
                drawCircle(
                    color = theme.primaryColor.copy(alpha = 0.65f),
                    radius = 36.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 8.dp.toPx()), 0f),
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(theme.primaryColor.copy(alpha = 0.25f), DeepIndigo)
                    )
                )
                .border(
                    width = 1.6.dp,
                    brush = Brush.sweepGradient(listOf(theme.primaryColor, theme.secondaryColor, theme.primaryColor)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = theme.icon,
                contentDescription = null,
                tint = theme.primaryColor,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
