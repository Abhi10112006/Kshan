package com.example.ui.components

import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
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
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ComicNeueFontFamily
import com.example.ui.theme.CourierPrimeFontFamily
import com.example.ui.theme.KalamFont
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// -----------------------------------------------------------------------------
// Ballistic Rocket & Particle Models with Object Pooling (Zero-GC at 60/120fps)
// -----------------------------------------------------------------------------

private enum class BurstType {
    CHRYSANTHEMUM,
    WILLOW,
    RING_SATURN,
    STROBE_CRACKLE
}

private class PooledRocket {
    var active: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var targetY: Float = 0f
    var burstType: BurstType = BurstType.CHRYSANTHEMUM
    var baseColor: Color = Color.Cyan
    val tailX = FloatArray(8)
    val tailY = FloatArray(8)
    var tailCount: Int = 0

    fun reset() {
        active = false
        tailCount = 0
    }
}

private class PooledParticle {
    var active: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var prevX: Float = 0f
    var prevY: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var alpha: Float = 1f
    var size: Float = 3.5f
    var color: Color = Color.White
    var decay: Float = 0.015f
    var friction: Float = 0.975f
    var gravity: Float = 0.32f
    var isStrobe: Boolean = false
    var hasSplit: Boolean = false
    var trailLengthMultiplier: Float = 1.5f

    fun reset() {
        active = false
        hasSplit = false
        isStrobe = false
    }
}

private class Star(
    val xRatio: Float,
    val yRatio: Float,
    val baseAlpha: Float,
    val baseRadius: Float,
    val twinkleSpeed: Float,
    val phase: Float
)

/**
 * Procedural Synthesizer for Pyrotechnic Launch Whoosh & Explosion Booms/Crackles.
 */
private object PyroSoundEngine {
    private const val SAMPLE_RATE = 22050

    fun playLaunchWhoosh() {
        try {
            val durationMs = 190
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            var lp = 0f
            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val env = sin(progress * PI).toFloat()
                val white = (Random.nextFloat() * 2f - 1f)
                val cutoff = 0.08f + progress * 0.28f
                lp += cutoff * (white - lp)
                val whistle = sin(2.0 * PI * (320.0 + progress * 580.0) * i / SAMPLE_RATE).toFloat() * 0.25f
                val sample = ((lp * 0.75f + whistle) * env * 11000f).toInt().coerceIn(-32767, 32767)
                buffer[i] = sample.toShort()
            }
            playPcmBuffer(buffer)
        } catch (_: Exception) {}
    }

    fun playExplosionBoom(isCrackle: Boolean) {
        try {
            val durationMs = if (isCrackle) 360 else 280
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            var lowPass = 0f
            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val decayEnv = (1f - progress) * (1f - progress)
                val noise = (Random.nextFloat() * 2f - 1f)
                // Deep sub-bass thump + noise burst
                lowPass += 0.12f * (noise - lowPass)
                val subThump = sin(2.0 * PI * (95.0 * (1.0 - progress * 0.6)) * i / SAMPLE_RATE).toFloat()
                var cracklePop = 0f
                if (isCrackle && progress > 0.25f && Random.nextFloat() > 0.96f) {
                    cracklePop = (Random.nextFloat() * 2f - 1f) * 1.4f
                }
                val combined = (lowPass * 0.7f + subThump * 0.6f + cracklePop) * decayEnv
                val sample = (combined * 18500f).toInt().coerceIn(-32767, 32767)
                buffer[i] = sample.toShort()
            }
            playPcmBuffer(buffer)
        } catch (_: Exception) {}
    }

    private fun playPcmBuffer(buffer: ShortArray) {
        val attr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val format = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        val track = AudioTrack.Builder()
            .setAudioAttributes(attr)
            .setAudioFormat(format)
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(buffer, 0, buffer.size)
        track.play()
    }
}

/**
 * Modernized Ballistic Celebration Fireworks Overlay.
 */
@Composable
fun CelebrationFireworksOverlay(
    visible: Boolean,
    eventTitle: String,
    category: String,
    remainingSeconds: Long,
    remainingTimeFormatted: String,
    onDismiss: () -> Unit,
    onTriggerFireworks: () -> Unit = {}
) {
    if (!visible) return

    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val theme = remember(category, eventTitle) { resolveCelebrationTheme(category, eventTitle) }
    val isGoalArrived = remainingSeconds <= 0L

    var isCardMinimized by remember { mutableStateOf(false) }
    var soundAndHapticsEnabled by remember { mutableStateOf(true) }
    var liveFps by remember { mutableFloatStateOf(60f) }
    var activeParticleCount by remember { mutableIntStateOf(0) }

    // 120 Twinkling Starfield
    val stars = remember {
        List(120) {
            Star(
                xRatio = Random.nextFloat(),
                yRatio = Random.nextFloat(),
                baseAlpha = Random.nextFloat() * 0.65f + 0.25f,
                baseRadius = Random.nextFloat() * 1.8f + 0.8f,
                twinkleSpeed = Random.nextFloat() * 0.045f + 0.015f,
                phase = Random.nextFloat() * (2f * PI.toFloat())
            )
        }
    }

    // Pre-allocated Object Pools
    val maxRockets = 50
    val maxParticles = 2500
    val rocketPool = remember { Array(maxRockets) { PooledRocket() } }
    val particlePool = remember { Array(maxParticles) { PooledParticle() } }

    var canvasWidth by remember { mutableFloatStateOf(1080f) }
    var canvasHeight by remember { mutableFloatStateOf(2200f) }
    var tickTrigger by remember { mutableIntStateOf(0) }

    fun triggerHaptic(isExplosion: Boolean) {
        if (!soundAndHapticsEnabled) return
        try {
            if (isExplosion) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                val vibrator = context.getSystemService(Vibrator::class.java)
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(38L, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
        } catch (_: Exception) {}
    }

    fun spawnMicroSplit(x: Float, y: Float, baseColor: Color) {
        var spawned = 0
        for (p in particlePool) {
            if (spawned >= 3) break
            if (p.active) continue
            p.active = true
            p.x = x
            p.y = y
            p.prevX = x
            p.prevY = y
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 4.5f + 2.0f
            p.vx = cos(angle) * speed
            p.vy = sin(angle) * speed
            p.alpha = 1f
            p.size = 2.6f
            p.color = if (spawned == 0) Color.White else baseColor
            p.decay = 0.035f
            p.friction = 0.95f
            p.gravity = 0.25f
            p.isStrobe = true
            p.hasSplit = true
            p.trailLengthMultiplier = 1.2f
            spawned++
        }
    }

    fun burstAt(px: Float, py: Float, burstType: BurstType, burstColor: Color) {
        triggerHaptic(isExplosion = true)
        if (soundAndHapticsEnabled) {
            coroutineScope.launch(Dispatchers.Default) {
                PyroSoundEngine.playExplosionBoom(isCrackle = burstType == BurstType.STROBE_CRACKLE)
            }
        }

        val count = when (burstType) {
            BurstType.CHRYSANTHEMUM -> 140
            BurstType.WILLOW -> 160
            BurstType.RING_SATURN -> 125
            BurstType.STROBE_CRACKLE -> 110
        }

        var spawned = 0
        for (p in particlePool) {
            if (spawned >= count) break
            if (p.active) continue

            p.reset()
            p.active = true
            p.x = px
            p.y = py
            p.prevX = px
            p.prevY = py
            p.alpha = 1f

            when (burstType) {
                BurstType.CHRYSANTHEMUM -> {
                    val angle = Random.nextFloat() * 2f * PI.toFloat()
                    val speed = Random.nextFloat() * 9.5f + 2.2f
                    p.vx = cos(angle) * speed
                    p.vy = sin(angle) * speed
                    p.size = Random.nextFloat() * 3.8f + 2.2f
                    p.color = when {
                        spawned % 5 == 0 -> Color.White
                        spawned % 2 == 0 -> burstColor
                        else -> NeonCyan
                    }
                    p.decay = Random.nextFloat() * 0.013f + 0.009f
                    p.friction = 0.975f
                    p.gravity = 0.35f
                    p.isStrobe = spawned % 4 == 0
                    p.trailLengthMultiplier = 2.2f
                }
                BurstType.WILLOW -> {
                    val angle = Random.nextFloat() * 2f * PI.toFloat()
                    val speed = Random.nextFloat() * 6.8f + 1.2f
                    p.vx = cos(angle) * speed
                    p.vy = sin(angle) * speed - 1.2f
                    p.size = Random.nextFloat() * 3.2f + 2.0f
                    p.color = if (Random.nextFloat() > 0.3f) Color(0xFFFFD700) else NeonCyan
                    p.decay = Random.nextFloat() * 0.008f + 0.006f // Long weeping life
                    p.friction = 0.95f
                    p.gravity = 0.24f
                    p.isStrobe = true
                    p.trailLengthMultiplier = 3.2f
                }
                BurstType.RING_SATURN -> {
                    if (spawned < 80) {
                        // Outer Neon Cyan/Purple Ring
                        val angle = (spawned.toFloat() / 80f) * 2f * PI.toFloat()
                        val speed = 8.2f + Random.nextFloat() * 0.5f
                        p.vx = cos(angle) * speed
                        p.vy = sin(angle) * speed * 0.48f
                        p.size = 3.5f
                        p.color = if (spawned % 2 == 0) NeonCyan else NeonPurple
                        p.decay = 0.014f
                        p.friction = 0.982f
                        p.gravity = 0.16f
                        p.trailLengthMultiplier = 1.8f
                    } else {
                        // Inner Tilted Gold Ring
                        val idx = spawned - 80
                        val angle = (idx.toFloat() / 45f) * 2f * PI.toFloat()
                        val speed = 4.6f + Random.nextFloat() * 0.4f
                        val rawVx = cos(angle) * speed
                        val rawVy = sin(angle) * speed
                        p.vx = rawVx * 0.85f - rawVy * 0.35f
                        p.vy = rawVx * 0.35f + rawVy * 0.65f
                        p.size = 3.8f
                        p.color = Color(0xFFFFD700)
                        p.decay = 0.015f
                        p.friction = 0.98f
                        p.gravity = 0.14f
                        p.isStrobe = true
                        p.trailLengthMultiplier = 1.6f
                    }
                }
                BurstType.STROBE_CRACKLE -> {
                    val angle = Random.nextFloat() * 2f * PI.toFloat()
                    val speed = Random.nextFloat() * 10.5f + 2.5f
                    p.vx = cos(angle) * speed
                    p.vy = sin(angle) * speed
                    p.size = Random.nextFloat() * 4.2f + 2.4f
                    p.color = if (Random.nextBoolean()) Color(0xFFFF8C00) else NeonPink
                    p.decay = Random.nextFloat() * 0.018f + 0.012f
                    p.friction = 0.965f
                    p.gravity = 0.38f
                    p.isStrobe = true
                    p.hasSplit = false // Will split into 3 micro-sparks before dying
                    p.trailLengthMultiplier = 2.4f
                }
            }
            spawned++
        }
    }

    fun spawnRocket(
        startX: Float,
        startY: Float,
        targetX: Float,
        targetY: Float,
        type: BurstType,
        color: Color
    ) {
        val rocket = rocketPool.firstOrNull { !it.active } ?: rocketPool[0]
        rocket.reset()
        rocket.active = true
        rocket.x = startX
        rocket.y = startY
        val clampedTargetY = targetY.coerceIn(canvasHeight * 0.10f, canvasHeight * 0.65f)
        val dx = targetX - startX
        val dy = clampedTargetY - startY

        // Physics: smooth upward velocity to reach apex under gravity 0.20f
        val g = 0.20f
        val distY = abs(dy).coerceAtLeast(150f)
        val timeToApex = sqrt((2f * distY / g).toDouble()).toFloat().coerceAtLeast(28f)
        rocket.vy = -g * timeToApex
        rocket.vx = (dx / timeToApex).coerceIn(-14f, 14f)
        rocket.targetY = clampedTargetY
        rocket.burstType = type
        rocket.baseColor = color
        rocket.tailCount = 1
        rocket.tailX[0] = startX
        rocket.tailY[0] = startY

        triggerHaptic(isExplosion = false)
        if (soundAndHapticsEnabled) {
            coroutineScope.launch(Dispatchers.Default) {
                PyroSoundEngine.playLaunchWhoosh()
            }
        }
    }

    fun launchSalvo(salvoCount: Int, isCardOpen: Boolean) {
        val colors = listOf(Color(0xFFFFD700), NeonCyan, NeonPurple, NeonPink, NeonEmerald)
        coroutineScope.launch {
            // Immediate high-visibility sky burst above card so user sees instant pyrotechnics
            if (isCardOpen) {
                burstAt(
                    px = canvasWidth * (0.28f + Random.nextFloat() * 0.44f),
                    py = canvasHeight * 0.16f,
                    burstType = BurstType.entries[Random.nextInt(BurstType.entries.size)],
                    burstColor = colors[Random.nextInt(colors.size)]
                )
            }
            repeat(salvoCount) { idx ->
                val flankX = if (idx % 2 == 0) {
                    canvasWidth * (0.12f + Random.nextFloat() * 0.22f)
                } else {
                    canvasWidth * (0.66f + Random.nextFloat() * 0.22f)
                }
                val targetX = if (isCardOpen) {
                    // Aim into upper sky above/around card
                    canvasWidth * (0.15f + Random.nextFloat() * 0.70f)
                } else {
                    canvasWidth * (0.18f + Random.nextFloat() * 0.64f)
                }
                val targetY = if (isCardOpen) {
                    canvasHeight * (0.12f + Random.nextFloat() * 0.14f)
                } else {
                    canvasHeight * (0.16f + Random.nextFloat() * 0.34f)
                }
                spawnRocket(
                    startX = flankX,
                    startY = canvasHeight * 0.96f,
                    targetX = targetX,
                    targetY = targetY,
                    type = BurstType.entries[idx % BurstType.entries.size],
                    color = colors[idx % colors.size]
                )
                delay(140L)
            }
        }
    }

    // Initial launch on open
    LaunchedEffect(visible, isGoalArrived) {
        if (!visible) return@LaunchedEffect
        delay(80)
        if (isGoalArrived) {
            launchSalvo(salvoCount = 6, isCardOpen = !isCardMinimized)
        } else {
            launchSalvo(salvoCount = 3, isCardOpen = !isCardMinimized)
        }
    }

    // Continuous 60/120fps Physics & Animation Loop
    LaunchedEffect(visible) {
        var lastTimeNanos = 0L
        var frameCounter = 0
        var fpsAccumTime = 0f

        while (visible) {
            withFrameNanos { now ->
                val dt = if (lastTimeNanos == 0L) 0.016f else ((now - lastTimeNanos) / 1_000_000_000f).coerceIn(0.004f, 0.045f)
                lastTimeNanos = now

                // FPS telemetry calculation
                frameCounter++
                fpsAccumTime += dt
                if (fpsAccumTime >= 0.35f) {
                    liveFps = (frameCounter / fpsAccumTime).coerceIn(30f, 120f)
                    frameCounter = 0
                    fpsAccumTime = 0f
                }

                val stepScale = (dt / 0.0166f).coerceIn(0.5f, 2.2f)

                // 1. Update Ascending Rockets
                for (r in rocketPool) {
                    if (!r.active) continue

                    // Shift 8-point comet tail history
                    if (r.tailCount < 8) {
                        r.tailX[r.tailCount] = r.x
                        r.tailY[r.tailCount] = r.y
                        r.tailCount++
                    } else {
                        for (i in 0 until 7) {
                            r.tailX[i] = r.tailX[i + 1]
                            r.tailY[i] = r.tailY[i + 1]
                        }
                        r.tailX[7] = r.x
                        r.tailY[7] = r.y
                    }

                    r.x += r.vx * stepScale
                    r.y += r.vy * stepScale
                    r.vy += 0.20f * stepScale

                    if (r.y <= r.targetY || r.vy >= 0f) {
                        burstAt(r.x, r.y, r.burstType, r.baseColor)
                        r.reset()
                    }
                }

                // 2. Update Pyrotechnic Particles
                var activeCount = 0
                for (p in particlePool) {
                    if (!p.active) continue
                    activeCount++
                    p.prevX = p.x
                    p.prevY = p.y
                    p.x += p.vx * stepScale
                    p.y += p.vy * stepScale
                    p.vx *= p.friction
                    p.vy = p.vy * p.friction + p.gravity * stepScale
                    p.alpha -= p.decay * stepScale

                    // Strobe Crackle split right before decay
                    if (p.isStrobe && !p.hasSplit && p.alpha in 0.18f..0.28f && Random.nextFloat() > 0.65f) {
                        p.hasSplit = true
                        spawnMicroSplit(p.x, p.y, p.color)
                    }

                    if (p.alpha <= 0.01f || p.y > canvasHeight + 60f) {
                        p.reset()
                    }
                }
                activeParticleCount = activeCount

                // Increment frame trigger to force Compose Canvas redraw
                tickTrigger++
            }
        }
    }

    var lastDragLaunchTime by remember { mutableStateOf(0L) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Pure OLED Black
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val colors = listOf(Color(0xFFFFD700), NeonCyan, NeonPurple, NeonPink, NeonEmerald)
                    val burstType = BurstType.entries[Random.nextInt(BurstType.entries.size)]
                    spawnRocket(
                        startX = (offset.x + (Random.nextFloat() - 0.5f) * 160f).coerceIn(40f, canvasWidth - 40f),
                        startY = canvasHeight * 0.96f,
                        targetX = offset.x,
                        targetY = offset.y,
                        type = burstType,
                        color = colors[Random.nextInt(colors.size)]
                    )
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val now = System.currentTimeMillis()
                    if (now - lastDragLaunchTime > 95L) {
                        lastDragLaunchTime = now
                        val offset = change.position
                        val colors = listOf(Color(0xFFFFD700), NeonCyan, NeonPurple, NeonPink, NeonEmerald)
                        val burstType = BurstType.entries[Random.nextInt(BurstType.entries.size)]
                        spawnRocket(
                            startX = (offset.x + (Random.nextFloat() - 0.5f) * 140f).coerceIn(40f, canvasWidth - 40f),
                            startY = canvasHeight * 0.96f,
                            targetX = offset.x,
                            targetY = offset.y,
                            type = burstType,
                            color = colors[Random.nextInt(colors.size)]
                        )
                    }
                }
            }
    ) {
        // 1. Starfield & Additive BlendMode.Plus Pyrotechnic Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            // CRITICAL: Read tickTrigger inside DrawScope so Compose invalidates every single frame!
            val currentTick = tickTrigger
            if (currentTick < 0) return@Canvas

            canvasWidth = size.width
            canvasHeight = size.height

            // Render 120 Twinkling Background Stars
            val timeSec = System.currentTimeMillis() / 1000f
            for (star in stars) {
                val alphaOsc = (sin(timeSec * star.twinkleSpeed * 12f + star.phase) + 1f) * 0.5f
                val currentAlpha = (star.baseAlpha * (0.35f + alphaOsc * 0.65f)).coerceIn(0.08f, 1f)
                val currentRadius = star.baseRadius * (0.85f + alphaOsc * 0.35f)
                drawCircle(
                    color = Color.White.copy(alpha = currentAlpha),
                    radius = currentRadius,
                    center = Offset(star.xRatio * size.width, star.yRatio * size.height)
                )
            }

            // Render Ascending Rockets with Glowing Golden Comet Tails (Additive Blending)
            for (r in rocketPool) {
                if (!r.active) continue

                for (i in 0 until r.tailCount - 1) {
                    val p1 = Offset(r.tailX[i], r.tailY[i])
                    val p2 = Offset(r.tailX[i + 1], r.tailY[i + 1])
                    val tailFraction = (i + 1).toFloat() / r.tailCount.toFloat()
                    drawLine(
                        color = Color(0xFFFF8C00).copy(alpha = tailFraction * 0.75f),
                        start = p1,
                        end = p2,
                        strokeWidth = 6.5f * tailFraction,
                        cap = StrokeCap.Round,
                        blendMode = BlendMode.Plus
                    )
                    drawLine(
                        color = Color(0xFFFFD700).copy(alpha = tailFraction * 0.95f),
                        start = p1,
                        end = p2,
                        strokeWidth = 3.2f * tailFraction,
                        cap = StrokeCap.Round,
                        blendMode = BlendMode.Plus
                    )
                }

                // Bright White-Hot Rocket Head
                drawCircle(
                    color = Color(0xFFFF8C00).copy(alpha = 0.65f),
                    radius = 11f,
                    center = Offset(r.x, r.y),
                    blendMode = BlendMode.Plus
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.5f,
                    center = Offset(r.x, r.y),
                    blendMode = BlendMode.Plus
                )
            }

            // Render Exploding Pyrotechnic Particles with Motion Trails & Additive Blending
            for (p in particlePool) {
                if (!p.active) continue
                val pAlpha = p.alpha.coerceIn(0f, 1f)
                val strobeFlash = p.isStrobe && ((currentTick + p.hashCode()) % 5 == 0)
                val coreColor = if (strobeFlash) {
                    Color.White.copy(alpha = (pAlpha * 1.2f).coerceAtMost(1f))
                } else {
                    p.color.copy(alpha = pAlpha)
                }

                // Motion Trail Streak
                val tailStart = Offset(
                    x = p.x - p.vx * p.trailLengthMultiplier,
                    y = p.y - p.vy * p.trailLengthMultiplier
                )
                val headPos = Offset(p.x, p.y)

                drawLine(
                    color = coreColor.copy(alpha = pAlpha * 0.65f),
                    start = tailStart,
                    end = headPos,
                    strokeWidth = p.size * 0.85f,
                    cap = StrokeCap.Round,
                    blendMode = BlendMode.Plus
                )

                // Outer Glow Halo
                drawCircle(
                    color = p.color.copy(alpha = pAlpha * 0.35f),
                    radius = p.size * 1.9f,
                    center = headPos,
                    blendMode = BlendMode.Plus
                )

                // Hot Spark Core
                drawCircle(
                    color = coreColor,
                    radius = p.size * (0.55f + pAlpha * 0.45f),
                    center = headPos,
                    blendMode = BlendMode.Plus
                )
            }
        }

        // 2. Top HUD Bar: Left Typewriter FPS Telemetry & Right Sound/Haptics Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Top-Left Frosted Typewriter Telemetry Badge (Courier Prime)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF0B101E).copy(alpha = 0.82f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = String.format("FPS: %.0f • %03d SPARKS", liveFps, activeParticleCount),
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }

            // Top-Right Sound & Haptics Toggle Button (🔊 / 🔇)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF0B101E).copy(alpha = 0.82f))
                    .border(
                        1.dp,
                        if (soundAndHapticsEnabled) NeonEmerald.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(50)
                    )
                    .clickable {
                        soundAndHapticsEnabled = !soundAndHapticsEnabled
                        if (soundAndHapticsEnabled) {
                            try {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            } catch (_: Exception) {}
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("pyro_sound_toggle"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (soundAndHapticsEnabled) {
                            Icons.AutoMirrored.Filled.VolumeUp
                        } else {
                            Icons.AutoMirrored.Filled.VolumeOff
                        },
                        contentDescription = "Toggle Sound and Haptics",
                        tint = if (soundAndHapticsEnabled) NeonEmerald else TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (soundAndHapticsEnabled) "SFX ON" else "MUTED",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (soundAndHapticsEnabled) NeonEmerald else TextMuted
                    )
                }
            }
        }

        // 3. Compact Top Floating Pill (When Minimized / Full Sky View)
        AnimatedVisibility(
            visible = isCardMinimized,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 54.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF0F172A).copy(alpha = 0.92f))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(NeonCyan.copy(0.6f), Color.White.copy(0.3f), NeonPurple.copy(0.6f))
                        ),
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🎆 Tap or drag sky",
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(Color.White.copy(alpha = 0.25f))
                )

                Text(
                    text = "Show Card",
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    modifier = Modifier
                        .clickable {
                            triggerHaptic(false)
                            isCardMinimized = false
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(Color.White.copy(alpha = 0.25f))
                )

                Text(
                    text = "Exit",
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier
                        .clickable {
                            triggerHaptic(false)
                            onDismiss()
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        // 4. Center Glass Celebration / Sky Preview Card
        AnimatedVisibility(
            visible = !isCardMinimized,
            enter = fadeIn() + scaleIn(spring(dampingRatio = 0.75f, stiffness = 400f)),
            exit = fadeOut() + scaleOut(spring(dampingRatio = 0.75f, stiffness = 400f)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF0B101E).copy(alpha = 0.85f))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                (if (isGoalArrived) Color(0xFFFFD700) else NeonCyan).copy(alpha = 0.16f),
                                Color.Transparent
                            ),
                            center = Offset(300f, 0f),
                            radius = 450f
                        )
                    )
                    .border(
                        1.2.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(0.35f),
                                if (isGoalArrived) Color(0xFFFFD700).copy(0.45f) else NeonCyan.copy(0.45f),
                                Color.Transparent
                            )
                        ),
                        RoundedCornerShape(32.dp)
                    )
                    .padding(24.dp)
            ) {
                // Top Right Action Buttons: Minimize & Close
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            triggerHaptic(false)
                            isCardMinimized = true
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInFull,
                            contentDescription = "Minimize Card / Full Sky View",
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            triggerHaptic(false)
                            onDismiss()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Top Logo Squircle
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF101A30))
                            .border(
                                1.2.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(0.4f),
                                        if (isGoalArrived) Color(0xFFFFD700).copy(0.6f) else NeonCyan.copy(0.6f)
                                    )
                                ),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "क्षण",
                            fontFamily = KalamFont,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGoalArrived) Color(0xFFFFD700) else NeonCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Badge: SKY PREVIEW vs MILESTONE ARRIVED
                    val badgeLabel = if (isGoalArrived) {
                        "क्षण • ${theme.badgeLabel}"
                    } else {
                        "क्षण • COUNTDOWN SKY PREVIEW"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background((if (isGoalArrived) Color(0xFFFFD700) else NeonCyan).copy(alpha = 0.15f))
                            .border(
                                1.dp,
                                (if (isGoalArrived) Color(0xFFFFD700) else NeonCyan).copy(alpha = 0.45f),
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = badgeLabel.uppercase(),
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = if (isGoalArrived) Color(0xFFFFD700) else NeonCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Event Title
                    Text(
                        text = eventTitle,
                        fontFamily = ComicNeueFontFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live Ticking Typewriter Pill if in Preview vs Status Pill if Arrived
                    if (!isGoalArrived) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.65f))
                                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = remainingTimeFormatted,
                                fontFamily = CourierPrimeFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Tap anywhere on the sky to launch rockets",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = theme.statusPillText,
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFD700),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Action: Deep Cyan-to-Indigo Glass Gradient Button with Press Scale
                    val launchInteraction = remember { MutableInteractionSource() }
                    val isLaunchPressed by launchInteraction.collectIsPressedAsState()
                    val launchScale by animateFloatAsState(
                        targetValue = if (isLaunchPressed) 0.94f else 1f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
                        label = "launch_btn_scale"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .scale(launchScale)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF00F2FE), Color(0xFF6366F1))
                                )
                            )
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.35f),
                                RoundedCornerShape(26.dp)
                            )
                            .clickable(
                                interactionSource = launchInteraction,
                                indication = null
                            ) {
                                launchSalvo(salvoCount = 4, isCardOpen = true)
                                onTriggerFireworks()
                            }
                            .testTag("celebration_burst_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isGoalArrived) theme.burstButtonText else "Launch Rockets",
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary Actions: Minimize Card / Full Sky View & Premium Animated Share Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Minimize Card / Full Sky View Button
                        val skyInteraction = remember { MutableInteractionSource() }
                        val isSkyPressed by skyInteraction.collectIsPressedAsState()
                        val skyScale by animateFloatAsState(
                            targetValue = if (isSkyPressed) 0.95f else 1f,
                            animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
                            label = "sky_btn_scale"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .scale(skyScale)
                                .clip(RoundedCornerShape(23.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(23.dp))
                                .clickable(
                                    interactionSource = skyInteraction,
                                    indication = null
                                ) {
                                    triggerHaptic(false)
                                    isCardMinimized = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Full Sky View",
                                    fontFamily = ComicNeueFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Premium Animated Share Button
                        PremiumShareButton(
                            modifier = Modifier.weight(1f),
                            accentColor = if (isGoalArrived) Color(0xFFFFD700) else NeonCyan,
                            onClick = {
                                triggerHaptic(true)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Celebrating $eventTitle")
                                    putExtra(Intent.EXTRA_TEXT, theme.shareTemplate(eventTitle))
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Celebration"))
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Premium Animated Share Button with continuous specular shimmer, spring press bounce,
 * and icon spin/pulse burst on click.
 */
@Composable
private fun PremiumShareButton(
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var pulseTrigger by remember { mutableStateOf(false) }

    val pressScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.92f
            pulseTrigger -> 1.06f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f),
        label = "share_scale"
    )

    val iconRotation by animateFloatAsState(
        targetValue = if (pulseTrigger) 360f else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 260f),
        label = "share_icon_rotation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "share_shimmer")
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_phase"
    )

    LaunchedEffect(pulseTrigger) {
        if (pulseTrigger) {
            delay(320)
            pulseTrigger = false
        }
    }

    Box(
        modifier = modifier
            .height(46.dp)
            .scale(pressScale)
            .clip(RoundedCornerShape(23.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.16f),
                        NeonPurple.copy(alpha = 0.22f),
                        accentColor.copy(alpha = 0.16f)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.55f),
                        NeonPurple.copy(alpha = 0.65f)
                    ),
                    start = Offset(shimmerPhase * 280f, 0f),
                    end = Offset(shimmerPhase * 280f + 160f, 90f)
                ),
                shape = RoundedCornerShape(23.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                pulseTrigger = true
                onClick()
            }
            .testTag("celebration_share_button"),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share Celebration",
                tint = accentColor,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(iconRotation)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Share",
                fontFamily = ComicNeueFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
