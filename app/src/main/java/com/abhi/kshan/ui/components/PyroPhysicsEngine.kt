package com.abhi.kshan.ui.components

import android.content.Context
import android.view.View
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.abhi.kshan.ui.theme.NeonBlue
import com.abhi.kshan.ui.theme.NeonCyan
import com.abhi.kshan.ui.theme.NeonEmerald
import com.abhi.kshan.ui.theme.NeonPink
import com.abhi.kshan.ui.theme.NeonPurple
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * 6 Pyrotechnic Shell Archetypes including Multi-Layer Peony Crossette and Horsetail Cascades.
 */
enum class PyroBurstShape(val displayName: String) {
    CHRYSANTHEMUM("Chrysanthemum"),
    WILLOW("Golden Willow"),
    RING_SATURN("Saturn Ring"),
    STROBE_CRACKLE("Strobe Crackle"),
    PEONY_CROSSETTE("Peony Crossette"),
    HORSETAIL("Horsetail Cascade")
}

class TwinkleStar(
    val xRatio: Float,
    val yRatio: Float,
    val baseSize: Float,
    val phaseOffset: Float,
    val speed: Float,
    val tint: Color
)

class VolumetricSmokePuff {
    var active: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var radius: Float = 28f
    var alpha: Float = 0f
    var tint: Color = Color.White
}

class BallisticRocket {
    var active: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var targetY: Float = 0f
    var primaryColor: Color = Color(0xFFFFD700)
    var secondaryColor: Color = NeonCyan
    var burstShape: PyroBurstShape = PyroBurstShape.CHRYSANTHEMUM
    var burstScale: Float = 1.0f
    val trailX = FloatArray(6)
    val trailY = FloatArray(6)
    var trailCount: Int = 0

    fun pushTrail(nx: Float, ny: Float) {
        for (i in 5 downTo 1) {
            trailX[i] = trailX[i - 1]
            trailY[i] = trailY[i - 1]
        }
        trailX[0] = nx
        trailY[0] = ny
        if (trailCount < 6) trailCount++
    }
}

class PyroSparkParticle {
    var active: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var drag: Float = 0.975f
    var gravity: Float = 0.45f
    var alpha: Float = 1f
    var decayRate: Float = 0.012f
    var size: Float = 4f
    var color: Color = Color.White
    var secondaryColor: Color = Color.White
    var shape: PyroBurstShape = PyroBurstShape.CHRYSANTHEMUM
    var hasSplit: Boolean = false
    var ageTicks: Int = 0
    val trailX = FloatArray(8)
    val trailY = FloatArray(8)
    var trailCount: Int = 0

    fun pushTrail(nx: Float, ny: Float) {
        for (i in 7 downTo 1) {
            trailX[i] = trailX[i - 1]
            trailY[i] = trailY[i - 1]
        }
        trailX[0] = nx
        trailY[0] = ny
        if (trailCount < 8) trailCount++
    }
}

/**
 * Zero-GC Pooled Pyrotechnic Physics Engine with Power Saver Cap,
 * Atmospheric Sky Flash Lighting, Volumetric Smoke Clouds, Crossette 4-Way Splits, and Horsetail Shells.
 */
class PyroSimulationEngine {
    companion object {
        const val MAX_ROCKETS = 30
        const val MAX_SPARKS_NORMAL = 1200
        const val MAX_SPARKS_POWER_SAVER = 350
        const val MAX_SMOKE_NORMAL = 45
    }

    val stars = List(110) {
        TwinkleStar(
            xRatio = Random.nextFloat(),
            yRatio = Random.nextFloat() * 0.88f,
            baseSize = Random.nextFloat() * 2.1f + 0.8f,
            phaseOffset = Random.nextFloat() * 6.28f,
            speed = Random.nextFloat() * 2.4f + 1.1f,
            tint = when (it % 4) {
                0 -> Color.White
                1 -> Color(0xFFE0F2FE)
                2 -> Color(0xFFFEF08A)
                else -> Color(0xFFF3E8FF)
            }
        )
    }

    val rocketPool = Array(MAX_ROCKETS) { BallisticRocket() }
    val particlePool = Array(MAX_SPARKS_NORMAL) { PyroSparkParticle() }
    val smokePool = Array(MAX_SMOKE_NORMAL) { VolumetricSmokePuff() }

    // Atmospheric Sky Flash Illumination State
    var skyFlashAlpha: Float = 0f
    var skyFlashColor: Color = Color.White
    var skyFlashCenter: Offset = Offset.Zero

    var activeSparksCount: Int = 0
        private set

    fun clearAll() {
        rocketPool.forEach { it.active = false }
        particlePool.forEach { it.active = false }
        smokePool.forEach { it.active = false }
        skyFlashAlpha = 0f
        activeSparksCount = 0
    }

    private fun allocateSpark(powerSaver: Boolean): PyroSparkParticle? {
        val cap = if (powerSaver) MAX_SPARKS_POWER_SAVER else MAX_SPARKS_NORMAL
        for (i in 0 until cap) {
            val p = particlePool[i]
            if (!p.active) return p
        }
        return null
    }

    private fun spawnSmokeCloud(originX: Float, originY: Float, color: Color, powerSaver: Boolean) {
        if (powerSaver) return
        var spawned = 0
        for (puff in smokePool) {
            if (!puff.active) {
                puff.active = true
                puff.x = originX + (Random.nextFloat() - 0.5f) * 38f
                puff.y = originY + (Random.nextFloat() - 0.5f) * 28f
                puff.vx = (Random.nextFloat() - 0.35f) * 0.45f // Gentle prevailing night wind
                puff.vy = -Random.nextFloat() * 0.32f - 0.08f   // Thermal updraft
                puff.radius = Random.nextFloat() * 34f + 26f
                puff.alpha = 0.22f
                puff.tint = color
                spawned++
                if (spawned >= 3) break
            }
        }
    }

    fun spawnRocket(
        startX: Float,
        startY: Float,
        targetX: Float,
        targetY: Float,
        canvasHeight: Float,
        palette: List<Color>,
        soundEnabled: Boolean,
        view: View,
        forcedShape: PyroBurstShape? = null,
        burstScale: Float = 1.0f
    ) {
        val rocket = rocketPool.firstOrNull { !it.active } ?: return
        val clampedTargetY = min(targetY, canvasHeight * 0.72f).coerceAtLeast(canvasHeight * 0.08f)
        val dy = (startY - clampedTargetY).coerceAtLeast(120f)
        val gravity = 0.22f
        val vy0 = -kotlin.math.sqrt(2f * gravity * dy)
        val flightFrames = (-vy0 / gravity).coerceAtLeast(18f)
        val vx0 = (targetX - startX) / flightFrames

        rocket.active = true
        rocket.x = startX
        rocket.y = startY
        rocket.vx = vx0
        rocket.vy = vy0
        rocket.targetY = clampedTargetY
        rocket.primaryColor = palette.random()
        rocket.secondaryColor = palette.random()
        rocket.burstShape = forcedShape ?: PyroBurstShape.values().random()
        rocket.burstScale = burstScale
        rocket.trailCount = 0
        rocket.pushTrail(startX, startY)

        PyroSoundEngine.playLaunchWhoosh(soundEnabled)
        PyroSoundEngine.triggerLaunchHaptic(view, soundEnabled)
    }

    fun detonateRocket(
        r: BallisticRocket,
        context: Context,
        view: View,
        soundEnabled: Boolean,
        powerSaver: Boolean,
        canvasHeight: Float,
        palette: List<Color>
    ) {
        val originX = r.x
        val originY = r.y
        val shape = r.burstShape
        val primary = r.primaryColor
        val secondary = r.secondaryColor
        val scale = r.burstScale

        // 1. Trigger Instant Atmospheric Sky Flash Lighting
        skyFlashAlpha = (0.38f * scale).coerceAtMost(0.55f)
        skyFlashColor = primary
        skyFlashCenter = Offset(originX, originY)

        // 2. Spawn lingering volumetric smoke clouds illuminated by the flash
        spawnSmokeCloud(originX, originY, primary, powerSaver)

        // 3. Schedule Speed-of-Sound Delayed Acoustic Boom & Multi-Stage Haptics
        PyroSoundEngine.scheduleDistanceDelayedBoom(
            context = context,
            view = view,
            enabled = soundEnabled,
            burstY = originY,
            canvasHeight = canvasHeight,
            scale = scale,
            isCrackleOrCrossette = (shape == PyroBurstShape.STROBE_CRACKLE || shape == PyroBurstShape.PEONY_CROSSETTE)
        )

        val countMultiplier = if (powerSaver) 0.45f else 1.0f

        when (shape) {
            PyroBurstShape.CHRYSANTHEMUM -> {
                val targetCount = (110 * countMultiplier).toInt()
                for (i in 0 until targetCount) {
                    val p = allocateSpark(powerSaver) ?: break
                    val angle = Random.nextFloat() * 6.2831855f
                    val speed = (Random.nextFloat() * 11.5f + 2.8f) * scale
                    p.initSpark(
                        ox = originX, oy = originY,
                        vx = cos(angle) * speed, vy = sin(angle) * speed,
                        drag = 0.974f, gravity = 0.24f,
                        decay = Random.nextFloat() * 0.007f + 0.009f,
                        size = (Random.nextFloat() * 2.8f + 2.2f) * scale,
                        color = if (i % 3 == 0) secondary else primary,
                        secColor = Color.White,
                        shape = shape
                    )
                }
            }

            PyroBurstShape.WILLOW -> {
                val targetCount = (95 * countMultiplier).toInt()
                val willowGold = Color(0xFFFFD700)
                for (i in 0 until targetCount) {
                    val p = allocateSpark(powerSaver) ?: break
                    val angle = Random.nextFloat() * 6.2831855f
                    val speed = (Random.nextFloat() * 8.2f + 1.8f) * scale
                    p.initSpark(
                        ox = originX, oy = originY,
                        vx = cos(angle) * speed, vy = sin(angle) * speed - 1.2f,
                        drag = 0.952f, gravity = 0.19f,
                        decay = Random.nextFloat() * 0.0045f + 0.0055f,
                        size = (Random.nextFloat() * 2.4f + 2.0f) * scale,
                        color = if (i % 4 == 0) NeonCyan else willowGold,
                        secColor = Color(0xFFFFA000),
                        shape = shape
                    )
                }
            }

            PyroBurstShape.RING_SATURN -> {
                val ringCount = (72 * countMultiplier).toInt()
                for (i in 0 until ringCount) {
                    val p = allocateSpark(powerSaver) ?: break
                    val angle = (i.toFloat() / ringCount) * 6.2831855f
                    val speed = 10.2f * scale
                    p.initSpark(
                        ox = originX, oy = originY,
                        vx = cos(angle) * speed, vy = sin(angle) * speed * 0.42f,
                        drag = 0.972f, gravity = 0.18f,
                        decay = 0.012f,
                        size = 3.4f * scale,
                        color = NeonCyan,
                        secColor = NeonPurple,
                        shape = shape
                    )
                }
                val coreCount = (48 * countMultiplier).toInt()
                for (i in 0 until coreCount) {
                    val p = allocateSpark(powerSaver) ?: break
                    val angle = Random.nextFloat() * 6.2831855f
                    val speed = (Random.nextFloat() * 6.2f + 1.5f) * scale
                    p.initSpark(
                        ox = originX, oy = originY,
                        vx = cos(angle) * speed, vy = sin(angle) * speed,
                        drag = 0.968f, gravity = 0.22f,
                        decay = 0.013f,
                        size = 2.8f * scale,
                        color = Color(0xFFFFD700),
                        secColor = Color.White,
                        shape = shape
                    )
                }
            }

            PyroBurstShape.STROBE_CRACKLE -> {
                val targetCount = (80 * countMultiplier).toInt()
                for (i in 0 until targetCount) {
                    val p = allocateSpark(powerSaver) ?: break
                    val angle = Random.nextFloat() * 6.2831855f
                    val speed = (Random.nextFloat() * 10.5f + 3.0f) * scale
                    p.initSpark(
                        ox = originX, oy = originY,
                        vx = cos(angle) * speed, vy = sin(angle) * speed,
                        drag = 0.968f, gravity = 0.25f,
                        decay = Random.nextFloat() * 0.008f + 0.011f,
                        size = 3.2f * scale,
                        color = primary,
                        secColor = palette.random(),
                        shape = shape
                    )
                }
            }

            PyroBurstShape.PEONY_CROSSETTE -> {
                // Multi-layer Peony Crossette: outer carrier stars split mid-flight into 4 perpendicular cross jets
                val outerCount = (42 * countMultiplier).toInt()
                for (i in 0 until outerCount) {
                    val p = allocateSpark(powerSaver) ?: break
                    val angle = (i.toFloat() / outerCount) * 6.2831855f + (Random.nextFloat() - 0.5f) * 0.08f
                    val speed = (Random.nextFloat() * 3.5f + 7.8f) * scale
                    p.initSpark(
                        ox = originX, oy = originY,
                        vx = cos(angle) * speed, vy = sin(angle) * speed,
                        drag = 0.982f, gravity = 0.20f,
                        decay = 0.009f,
                        size = 4.0f * scale,
                        color = primary,
                        secColor = secondary,
                        shape = PyroBurstShape.PEONY_CROSSETTE
                    )
                }
                // Inner peony core layer
                val innerCount = (36 * countMultiplier).toInt()
                for (i in 0 until innerCount) {
                    val p = allocateSpark(powerSaver) ?: break
                    val angle = Random.nextFloat() * 6.2831855f
                    val speed = (Random.nextFloat() * 4.5f + 2.0f) * scale
                    p.initSpark(
                        ox = originX, oy = originY,
                        vx = cos(angle) * speed, vy = sin(angle) * speed,
                        drag = 0.97f, gravity = 0.22f,
                        decay = 0.014f,
                        size = 2.6f * scale,
                        color = Color.White,
                        secColor = secondary,
                        shape = PyroBurstShape.CHRYSANTHEMUM
                    )
                }
            }

            PyroBurstShape.HORSETAIL -> {
                // Horsetail Cascade: thick brocade/gold streams lofted upward & outward that weep heavily downward
                val targetCount = (88 * countMultiplier).toInt()
                val brocadeGold = Color(0xFFFFDF73)
                for (i in 0 until targetCount) {
                    val p = allocateSpark(powerSaver) ?: break
                    val angle = -Math.PI.toFloat() * Random.nextFloat() + (Random.nextFloat() - 0.5f) * 0.9f
                    val speed = (Random.nextFloat() * 6.8f + 2.2f) * scale
                    p.initSpark(
                        ox = originX, oy = originY,
                        vx = cos(angle) * speed * 0.85f,
                        vy = sin(angle) * speed - 2.4f,
                        drag = 0.962f,
                        gravity = 0.42f, // Heavy cascading weep
                        decay = Random.nextFloat() * 0.0045f + 0.006f,
                        size = (Random.nextFloat() * 2.2f + 2.6f) * scale,
                        color = if (i % 5 == 0) primary else brocadeGold,
                        secColor = Color(0xFFFF8C00),
                        shape = PyroBurstShape.HORSETAIL
                    )
                }
            }
        }
    }

    private fun PyroSparkParticle.initSpark(
        ox: Float,
        oy: Float,
        vx: Float,
        vy: Float,
        drag: Float,
        gravity: Float,
        decay: Float,
        size: Float,
        color: Color,
        secColor: Color,
        shape: PyroBurstShape
    ) {
        this.active = true
        this.x = ox
        this.y = oy
        this.vx = vx
        this.vy = vy
        this.drag = drag
        this.gravity = gravity
        this.alpha = 1f
        this.decayRate = decay
        this.size = size
        this.color = color
        this.secondaryColor = secColor
        this.shape = shape
        this.hasSplit = false
        this.ageTicks = 0
        this.trailCount = 0
        this.pushTrail(ox, oy)
    }

    fun stepPhysics(
        dtScale: Float,
        context: Context,
        view: View,
        soundEnabled: Boolean,
        powerSaver: Boolean,
        canvasHeight: Float,
        palette: List<Color>
    ) {
        // Decay atmospheric sky flash
        if (skyFlashAlpha > 0.005f) {
            skyFlashAlpha = (skyFlashAlpha - 0.028f * dtScale).coerceAtLeast(0f)
        } else {
            skyFlashAlpha = 0f
        }

        // Update drifting smoke clouds
        if (!powerSaver) {
            for (puff in smokePool) {
                if (!puff.active) continue
                puff.x += puff.vx * dtScale
                puff.y += puff.vy * dtScale
                puff.radius += 0.22f * dtScale
                puff.alpha -= 0.0018f * dtScale
                if (puff.alpha <= 0.005f) {
                    puff.active = false
                }
            }
        }

        // Stage 1: Ascending Ballistic Rockets
        for (r in rocketPool) {
            if (!r.active) continue
            r.x += r.vx * dtScale
            r.y += r.vy * dtScale
            r.vy += 0.22f * dtScale
            r.pushTrail(r.x, r.y)

            if (r.y <= r.targetY || r.vy >= 0f) {
                r.active = false
                detonateRocket(r, context, view, soundEnabled, powerSaver, canvasHeight, palette)
            }
        }

        // Stage 2: Pyrotechnic Sparks & Crossette/Strobe Secondary Splits
        var liveSparks = 0
        val cap = if (powerSaver) MAX_SPARKS_POWER_SAVER else MAX_SPARKS_NORMAL
        for (idx in 0 until cap) {
            val p = particlePool[idx]
            if (!p.active) continue
            liveSparks++
            p.ageTicks++
            p.x += p.vx * dtScale
            p.y += p.vy * dtScale
            p.vx *= p.drag
            p.vy = (p.vy * p.drag) + (p.gravity * dtScale)
            p.alpha -= p.decayRate * dtScale
            p.pushTrail(p.x, p.y)

            // Peony Crossette 4-Way Perpendicular Split mid-flight
            if (p.shape == PyroBurstShape.PEONY_CROSSETTE && !p.hasSplit && p.ageTicks >= 18 && p.alpha in 0.45f..0.82f) {
                p.hasSplit = true
                val crossAngles = floatArrayOf(0f, 1.5707964f, 3.1415927f, 4.712389f)
                for (cAngle in crossAngles) {
                    val child = allocateSpark(powerSaver) ?: break
                    val splitSpeed = 4.6f
                    child.initSpark(
                        ox = p.x, oy = p.y,
                        vx = p.vx * 0.35f + cos(cAngle) * splitSpeed,
                        vy = p.vy * 0.35f + sin(cAngle) * splitSpeed,
                        drag = 0.965f, gravity = 0.24f,
                        decay = 0.022f,
                        size = p.size * 0.78f,
                        color = p.secondaryColor,
                        secColor = Color.White,
                        shape = PyroBurstShape.CHRYSANTHEMUM
                    )
                    child.hasSplit = true
                }
            }

            // Strobe Crackle 3-way micro-spark split
            if (p.shape == PyroBurstShape.STROBE_CRACKLE && !p.hasSplit && p.alpha in 0.24f..0.38f) {
                p.hasSplit = true
                for (s in 0 until 3) {
                    val micro = allocateSpark(powerSaver) ?: break
                    val ang = Random.nextFloat() * 6.2831855f
                    val spd = Random.nextFloat() * 4.2f + 1.8f
                    micro.initSpark(
                        ox = p.x, oy = p.y,
                        vx = cos(ang) * spd, vy = sin(ang) * spd,
                        drag = 0.94f, gravity = 0.28f,
                        decay = 0.032f,
                        size = p.size * 0.65f,
                        color = Color.White,
                        secColor = p.secondaryColor,
                        shape = PyroBurstShape.CHRYSANTHEMUM
                    )
                    micro.hasSplit = true
                }
            }

            if (p.alpha <= 0.01f || p.y > canvasHeight + 160f) {
                p.active = false
            }
        }
        activeSparksCount = liveSparks
    }

    fun drawScene(
        scope: DrawScope,
        starPhase: Float,
        powerSaver: Boolean
    ) = with(scope) {
        val w = size.width
        val h = size.height

        // 1. Pure OLED Night Sky (#000000)
        drawRect(color = Color.Black)

        // 2. Atmospheric Sky Flash Lighting on Detonation
        if (skyFlashAlpha > 0.005f) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = (skyFlashAlpha * 0.65f).coerceIn(0f, 1f)),
                        skyFlashColor.copy(alpha = (skyFlashAlpha * 0.45f).coerceIn(0f, 1f)),
                        Color.Transparent
                    ),
                    center = skyFlashCenter,
                    radius = w * 0.95f
                )
            )
        }

        // 3. Twinkling Starfield
        for (star in stars) {
            val osc = (sin((starPhase * star.speed + star.phaseOffset).toDouble()).toFloat() + 1f) * 0.5f
            val alpha = (0.18f + osc * 0.72f).coerceIn(0.1f, 0.95f)
            val rad = star.baseSize * (0.8f + osc * 0.4f)
            drawCircle(
                color = star.tint.copy(alpha = alpha),
                radius = rad,
                center = Offset(star.xRatio * w, star.yRatio * h)
            )
        }

        // 4. Volumetric Smoke Clouds (lit by ambient sky flash)
        if (!powerSaver) {
            for (puff in smokePool) {
                if (!puff.active) continue
                val litBoost = (skyFlashAlpha * 0.45f).coerceIn(0f, 0.25f)
                val smokeColor = if (litBoost > 0.02f) {
                    puff.tint.copy(alpha = (puff.alpha + litBoost).coerceIn(0f, 0.38f))
                } else {
                    Color(0xFF475569).copy(alpha = puff.alpha.coerceIn(0f, 0.28f))
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(smokeColor, Color.Transparent),
                        center = Offset(puff.x, puff.y),
                        radius = puff.radius
                    ),
                    radius = puff.radius,
                    center = Offset(puff.x, puff.y)
                )
            }
        }

        // 5. Ascending Rockets with Golden Comet Tails
        for (r in rocketPool) {
            if (!r.active) continue
            for (i in 0 until r.trailCount - 1) {
                val progress = 1f - (i.toFloat() / r.trailCount)
                drawLine(
                    color = Color(0xFFFF9E00).copy(alpha = progress * 0.78f),
                    start = Offset(r.trailX[i], r.trailY[i]),
                    end = Offset(r.trailX[i + 1], r.trailY[i + 1]),
                    strokeWidth = (4.2f - i * 0.5f).coerceAtLeast(1.2f),
                    cap = StrokeCap.Round,
                    blendMode = BlendMode.Plus
                )
            }
            drawCircle(
                color = Color(0xFFFFD700),
                radius = if (powerSaver) 6f else 10f,
                center = Offset(r.x, r.y),
                blendMode = BlendMode.Plus
            )
            drawCircle(
                color = Color.White,
                radius = 3.5f,
                center = Offset(r.x, r.y),
                blendMode = BlendMode.Plus
            )
        }

        // 6. Pyrotechnic Sparks with Additive Light Blending
        val cap = if (powerSaver) MAX_SPARKS_POWER_SAVER else MAX_SPARKS_NORMAL
        for (idx in 0 until cap) {
            val p = particlePool[idx]
            if (!p.active) continue
            val a = p.alpha.coerceIn(0f, 1f)

            val isStrobeFlash = p.shape == PyroBurstShape.STROBE_CRACKLE && (p.ageTicks % 5 < 2)
            val renderColor = if (isStrobeFlash) Color.White else p.color

            val maxSegments = if (powerSaver) min(3, p.trailCount - 1) else (p.trailCount - 1)
            for (i in 0 until maxSegments) {
                val segAlpha = (1f - i.toFloat() / p.trailCount) * a * 0.68f
                drawLine(
                    color = renderColor.copy(alpha = segAlpha.coerceIn(0f, 1f)),
                    start = Offset(p.trailX[i], p.trailY[i]),
                    end = Offset(p.trailX[i + 1], p.trailY[i + 1]),
                    strokeWidth = (p.size * (1f - i * 0.11f)).coerceAtLeast(1f),
                    cap = StrokeCap.Round,
                    blendMode = BlendMode.Plus
                )
            }

            if (!powerSaver) {
                drawCircle(
                    color = renderColor.copy(alpha = (a * 0.42f).coerceIn(0f, 1f)),
                    radius = p.size * 2.1f,
                    center = Offset(p.x, p.y),
                    blendMode = BlendMode.Plus
                )
            }

            drawCircle(
                color = (if (a > 0.72f || isStrobeFlash) Color.White else renderColor).copy(alpha = a),
                radius = p.size * 0.85f,
                center = Offset(p.x, p.y),
                blendMode = BlendMode.Plus
            )
        }
    }
}
