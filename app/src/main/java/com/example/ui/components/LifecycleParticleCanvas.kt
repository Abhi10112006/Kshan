package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SpaceDark
import kotlin.random.Random

data class CosmicParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var alpha: Float,
    var color: Color
)

data class BurstParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var width: Float,
    var height: Float,
    var rotation: Float,
    var rotSpeed: Float,
    var alpha: Float,
    var color: Color,
    var life: Float = 1.0f,
    var isRibbon: Boolean = false
)

@Composable
fun LifecycleParticleCanvas(
    modifier: Modifier = Modifier,
    powerSaverEnabled: Boolean = false,
    triggerCelebration: Boolean = false,
    onCelebrationFinished: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var touchPos by remember { mutableStateOf(Offset(-1000f, -1000f)) }
    var width by remember { mutableStateOf(1080f) }
    var height by remember { mutableStateOf(2400f) }

    val cosmicParticles = remember { mutableStateListOf<CosmicParticle>() }
    val burstParticles = remember { mutableStateListOf<BurstParticle>() }

    // Shimmering metallic & vibrant celebration colors
    val celebrationPalette = remember {
        listOf(
            Color(0xFFFFD700), // Rich Gold
            Color(0xFFFFE066), // Pale Gold
            NeonCyan,          // Electric Cyan
            NeonPurple,        // Vibrant Violet
            NeonPink,          // Radiant Rose
            NeonEmerald,       // Fresh Emerald
            NeonBlue,          // Sky Blue
            Color(0xFFF8FAFC)  // Shimmer Silver
        )
    }

    // Initialize cosmic dust if empty
    LaunchedEffect(Unit) {
        if (cosmicParticles.isEmpty()) {
            for (i in 0 until 50) {
                cosmicParticles.add(
                    CosmicParticle(
                        x = Random.nextFloat() * 1080f,
                        y = Random.nextFloat() * 2400f,
                        vx = (Random.nextFloat() - 0.5f) * 0.8f,
                        vy = (Random.nextFloat() - 0.5f) * 0.8f,
                        size = Random.nextFloat() * 2.5f + 1.2f,
                        alpha = Random.nextFloat() * 0.45f + 0.15f,
                        color = celebrationPalette.random()
                    )
                )
            }
        }
    }

    // Trigger multi-origin celebration fireworks blast
    LaunchedEffect(triggerCelebration) {
        if (triggerCelebration) {
            burstParticles.clear()
            // 3 launch origins: Center, Left-Up, Right-Up
            val origins = listOf(
                Offset(width * 0.5f, height * 0.38f),
                Offset(width * 0.25f, height * 0.45f),
                Offset(width * 0.75f, height * 0.42f)
            )

            origins.forEach { origin ->
                val particleCount = 55
                for (i in 0 until particleCount) {
                    val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                    val speed = Random.nextFloat() * 14f + 3f
                    val isRibbon = Random.nextBoolean()
                    val pWidth = if (isRibbon) Random.nextFloat() * 10f + 6f else Random.nextFloat() * 6f + 3f
                    val pHeight = if (isRibbon) Random.nextFloat() * 6f + 3f else pWidth

                    burstParticles.add(
                        BurstParticle(
                            x = origin.x + (Random.nextFloat() - 0.5f) * 40f,
                            y = origin.y + (Random.nextFloat() - 0.5f) * 40f,
                            vx = kotlin.math.cos(angle) * speed,
                            vy = kotlin.math.sin(angle) * speed - Random.nextFloat() * 5f,
                            width = pWidth,
                            height = pHeight,
                            rotation = Random.nextFloat() * 360f,
                            rotSpeed = (Random.nextFloat() - 0.5f) * 16f,
                            alpha = 1.0f,
                            color = celebrationPalette.random(),
                            life = 1.0f,
                            isRibbon = isRibbon
                        )
                    )
                }
            }
        }
    }

    // Lifecycle-Aware Frame Loop: Anchored strictly to Lifecycle.State.STARTED
    // Runs during foreground, modal dialogs, and split-screen, but halts on ON_STOP.
    var frameTick by remember { mutableStateOf(0L) }

    LaunchedEffect(lifecycleOwner, powerSaverEnabled) {
        if (powerSaverEnabled) return@LaunchedEffect

        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                withFrameNanos { time ->
                    frameTick = time

                    // Update ambient particles
                    for (p in cosmicParticles) {
                        p.x += p.vx
                        p.y += p.vy

                        // Wrap edges
                        if (p.x < 0) p.x = width
                        if (p.x > width) p.x = 0f
                        if (p.y < 0) p.y = height
                        if (p.y > height) p.y = 0f

                        // Touch gravity / repulsion
                        if (touchPos.x >= 0 && touchPos.y >= 0) {
                            val dx = p.x - touchPos.x
                            val dy = p.y - touchPos.y
                            val distSq = dx * dx + dy * dy
                            if (distSq < 40000f && distSq > 10f) {
                                val dist = kotlin.math.sqrt(distSq)
                                p.x += (dx / dist) * 2.5f
                                p.y += (dy / dist) * 2.5f
                            }
                        }
                    }

                    // Update burst celebration particles
                    if (burstParticles.isNotEmpty()) {
                        val iterator = burstParticles.iterator()
                        while (iterator.hasNext()) {
                            val bp = iterator.next()
                            bp.x += bp.vx
                            bp.y += bp.vy
                            bp.vx *= 0.965f // Aerodynamic drag
                            bp.vy += 0.26f  // Gravity
                            bp.rotation += bp.rotSpeed
                            bp.life -= 0.012f
                            bp.alpha = bp.life.coerceIn(0f, 1f)
                            if (bp.life <= 0f) {
                                iterator.remove()
                            }
                        }
                        if (burstParticles.isEmpty() && triggerCelebration) {
                            onCelebrationFinished()
                        }
                    }
                }
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        touchPos = offset
                        tryAwaitRelease()
                        touchPos = Offset(-1000f, -1000f)
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { touchPos = it },
                    onDragEnd = { touchPos = Offset(-1000f, -1000f) },
                    onDragCancel = { touchPos = Offset(-1000f, -1000f) },
                    onDrag = { change, _ ->
                        touchPos = change.position
                        change.consume()
                    }
                )
            }
    ) {
        width = size.width
        height = size.height

        // 1. OLED Deep Space Background with reactive subtle gradient
        drawRect(color = SpaceBlack)

        // Radial interactive reactive glow
        val glowCenter = if (touchPos.x >= 0 && touchPos.y >= 0) touchPos else Offset(size.width * 0.5f, size.height * 0.3f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    NeonCyan.copy(alpha = if (touchPos.x >= 0) 0.12f else 0.04f),
                    NeonPurple.copy(alpha = 0.02f),
                    Color.Transparent
                ),
                center = glowCenter,
                radius = size.width * 0.7f
            ),
            radius = size.width * 0.7f,
            center = glowCenter
        )

        // Subtle Cyber Matrix Grid
        drawCyberGrid(size.width, size.height)

        // 2. Cosmic Dust Particles
        // We read frameTick to force recomposition per frame when animating
        if (frameTick >= 0L) {
            for (p in cosmicParticles) {
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.size,
                    center = Offset(p.x, p.y)
                )
            }

            // 3. Burst Confetti / Celebration Particles
            for (bp in burstParticles) {
                if (bp.isRibbon) {
                    rotate(degrees = bp.rotation, pivot = Offset(bp.x, bp.y)) {
                        drawRoundRect(
                            color = bp.color.copy(alpha = bp.alpha),
                            topLeft = Offset(bp.x - bp.width / 2f, bp.y - bp.height / 2f),
                            size = Size(bp.width, bp.height),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                } else {
                    drawCircle(
                        color = bp.color.copy(alpha = bp.alpha),
                        radius = bp.width / 2f,
                        center = Offset(bp.x, bp.y)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawCyberGrid(w: Float, h: Float) {
    val step = 72f
    val gridColor = Color.White.copy(alpha = 0.015f)
    var x = step
    while (x < w) {
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 1f
        )
        x += step
    }
    var y = step
    while (y < h) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1f
        )
        y += step
    }
}
