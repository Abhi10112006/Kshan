package com.example.ui.components

import android.content.Intent
import android.view.HapticFeedbackConstants
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KalamFont
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.random.Random

private val GoldMetallic = Color(0xFFFFD700)
private val GoldWarm = Color(0xFFFFA000)
private val DeepIndigo = Color(0xFF0F172A)

/**
 * Category-specific styling, wording, icons, and share texts for celebrations.
 */
data class CategoryCelebrationTheme(
    val badgeLabel: String,
    val statusPillText: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val secondaryColor: Color,
    val burstButtonText: String,
    val shareTemplate: (String) -> String
)

fun resolveCelebrationTheme(category: String, title: String): CategoryCelebrationTheme {
    val lowerCat = category.lowercase()
    val lowerTitle = title.lowercase()

    return when {
        lowerCat.contains("holiday") || lowerCat.contains("vacation") || lowerCat.contains("season") ||
                lowerTitle.contains("holiday") || lowerTitle.contains("vacation") || lowerTitle.contains("trip") || lowerTitle.contains("summer") || lowerTitle.contains("winter") -> {
            CategoryCelebrationTheme(
                badgeLabel = "HOLIDAY HAS ARRIVED",
                statusPillText = "TIME TO UNWIND • OFF TO CELEBRATE",
                icon = Icons.Default.BeachAccess,
                primaryColor = NeonEmerald,
                secondaryColor = GoldMetallic,
                burstButtonText = "Burst Holiday Fireworks",
                shareTemplate = { "🌴 Holiday is here! '$it' has finally arrived! Counted down with क्षण (Kshan)." }
            )
        }
        lowerCat.contains("birthday") || lowerTitle.contains("birthday") || lowerTitle.contains("bday") -> {
            CategoryCelebrationTheme(
                badgeLabel = "HAPPY BIRTHDAY",
                statusPillText = "CHEERS TO ANOTHER YEAR • MAKE A WISH",
                icon = Icons.Default.Cake,
                primaryColor = NeonPink,
                secondaryColor = NeonPurple,
                burstButtonText = "Pop Birthday Fireworks",
                shareTemplate = { "🎂 Happy Birthday! Celebrating '$it' today! Counted down with क्षण (Kshan)." }
            )
        }
        lowerCat.contains("goal") || lowerTitle.contains("goal") || lowerTitle.contains("exam") || lowerTitle.contains("target") -> {
            CategoryCelebrationTheme(
                badgeLabel = "GOAL CONQUERED",
                statusPillText = "100% ACHIEVED • TARGET WON",
                icon = Icons.Default.Flag,
                primaryColor = GoldMetallic,
                secondaryColor = NeonCyan,
                burstButtonText = "Burst Victory Fireworks",
                shareTemplate = { "🏆 Goal conquered! Successfully achieved '$it'! Tracked with क्षण (Kshan)." }
            )
        }
        lowerCat.contains("deadline") || lowerTitle.contains("deadline") || lowerTitle.contains("due") -> {
            CategoryCelebrationTheme(
                badgeLabel = "DEADLINE MET",
                statusPillText = "COUNTDOWN ZEROED • MISSION DELIVERED",
                icon = Icons.Default.Alarm,
                primaryColor = NeonCyan,
                secondaryColor = NeonBlue,
                burstButtonText = "Burst Completion Fireworks",
                shareTemplate = { "⏱️ Target reached for '$it'! Delivered on schedule with क्षण (Kshan)." }
            )
        }
        lowerCat.contains("personal") || lowerCat.contains("anniversary") || lowerTitle.contains("anniversary") || lowerTitle.contains("wedding") -> {
            CategoryCelebrationTheme(
                badgeLabel = "SPECIAL MOMENT",
                statusPillText = "A DAY TO REMEMBER • CHERISH TODAY",
                icon = Icons.Default.Favorite,
                primaryColor = Color(0xFFF43F5E),
                secondaryColor = NeonPink,
                burstButtonText = "Burst Love & Fireworks",
                shareTemplate = { "💖 Special moment arrived: '$it'! Cherished with क्षण (Kshan)." }
            )
        }
        lowerCat.contains("milestone") -> {
            CategoryCelebrationTheme(
                badgeLabel = "MILESTONE REACHED",
                statusPillText = "HISTORIC MOMENT • JOURNEY COMPLETED",
                icon = Icons.Default.Star,
                primaryColor = GoldMetallic,
                secondaryColor = NeonCyan,
                burstButtonText = "Burst Milestone Fireworks",
                shareTemplate = { "✨ Milestone achieved! '$it' has arrived. Tracked precisely with क्षण (Kshan)!" }
            )
        }
        else -> { // Default / Celebration / Party
            CategoryCelebrationTheme(
                badgeLabel = "CELEBRATION TIME",
                statusPillText = "THE MOMENT HAS ARRIVED • TIME TO REJOICE",
                icon = Icons.Default.Celebration,
                primaryColor = NeonPurple,
                secondaryColor = GoldMetallic,
                burstButtonText = "Burst More Fireworks",
                shareTemplate = { "🎉 Celebration time! '$it' is happening right now! Counted down with क्षण (Kshan)." }
            )
        }
    }
}

/**
 * In-Modal Fireworks Particle Data Model.
 */
private data class ModalParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var rotation: Float,
    var rotSpeed: Float,
    var alpha: Float,
    var color: Color,
    var isRibbon: Boolean,
    var life: Float = 1.0f
)

/**
 * Premium luxury modal for celebrations in क्षण (Kshan).
 * Adapts wording and icons dynamically by category (Holiday, Birthday, Celebration, Goal, etc.)
 * Features an in-modal foreground fireworks canvas so fireworks are vibrant, explosive,
 * and NEVER hidden behind the popup scrim!
 */
@Composable
fun PremiumMilestoneModal(
    visible: Boolean,
    eventTitle: String,
    category: String = "Celebration",
    onDismiss: () -> Unit,
    onTriggerFireworks: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val theme = remember(category, eventTitle) { resolveCelebrationTheme(category, eventTitle) }

    // In-modal Real-time Fireworks Engine
    val particles = remember { mutableStateListOf<ModalParticle>() }
    var burstTriggerCounter by remember { mutableStateOf(0) }

    val palette = remember(theme) {
        listOf(
            theme.primaryColor,
            theme.secondaryColor,
            GoldMetallic,
            NeonCyan,
            NeonPink,
            Color.White,
            Color(0xFFFFE066)
        )
    }

    // Function to launch explosive multi-particle shells
    fun spawnExplosion(originX: Float, originY: Float, count: Int = 45) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 16f + 3f
            val isRibbon = Random.nextBoolean()
            particles.add(
                ModalParticle(
                    x = originX + (Random.nextFloat() - 0.5f) * 24f,
                    y = originY + (Random.nextFloat() - 0.5f) * 24f,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed - Random.nextFloat() * 5f,
                    size = if (isRibbon) Random.nextFloat() * 8f + 5f else Random.nextFloat() * 5f + 3f,
                    rotation = Random.nextFloat() * 360f,
                    rotSpeed = (Random.nextFloat() - 0.5f) * 18f,
                    alpha = 1.0f,
                    color = palette.random(),
                    isRibbon = isRibbon
                )
            )
        }
    }

    // When modal becomes visible, trigger initial burst and haptics
    LaunchedEffect(visible) {
        if (visible) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            burstTriggerCounter++
        } else {
            particles.clear()
        }
    }

    // Realtime Particle Physics Simulation Loop
    LaunchedEffect(visible) {
        if (!visible) return@LaunchedEffect
        while (true) {
            withFrameNanos {
                if (particles.isNotEmpty()) {
                    val it = particles.iterator()
                    while (it.hasNext()) {
                        val p = it.next()
                        p.x += p.vx
                        p.y += p.vy
                        p.vy += 0.38f // Realistic gravity
                        p.vx *= 0.975f // Air drag
                        p.rotation += p.rotSpeed
                        p.alpha -= 0.014f
                        p.life -= 0.014f

                        if (p.alpha <= 0.02f || p.y > 3000f) {
                            it.remove()
                        }
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(280)) + scaleIn(
            animationSpec = spring(dampingRatio = 0.65f, stiffness = 380f),
            initialScale = 0.88f
        ),
        exit = fadeOut(tween(220)) + scaleOut(
            animationSpec = tween(220),
            targetScale = 0.92f
        ),
        modifier = modifier.fillMaxSize()
    ) {
        // Dimmed backdrop scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.78f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            // Main Modal Dialog Container
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* Intercept tap to prevent dismissing */ }
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                DeepIndigo.copy(alpha = 0.96f),
                                SpaceDark.copy(alpha = 0.98f)
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
                    .padding(26.dp)
            ) {
                // Close icon button at top right
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Jewel Emblem with Orbital Starlight Halo customized for category
                    OrbitalJewelCrest(theme = theme)

                    Spacer(modifier = Modifier.height(20.dp))

                    // Devanagari Brand & Category Tag (e.g. क्षण • HOLIDAY HAS ARRIVED)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "क्षण",
                            fontFamily = KalamFont,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primaryColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "•  ${theme.badgeLabel}",
                            fontFamily = FontFamily.Default,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.4.sp,
                            color = theme.primaryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Event Title
                    Text(
                        text = if (eventTitle.isNotBlank()) "“$eventTitle”" else "“Celebration Moment”",
                        fontFamily = FontFamily.Default,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic Category Status Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(theme.primaryColor.copy(alpha = 0.12f))
                            .border(
                                width = 1.dp,
                                color = theme.primaryColor.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(16.dp)
                            )
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
                            Text(
                                text = theme.statusPillText,
                                fontFamily = FontFamily.Default,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = theme.primaryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary Action: Burst Fireworks Button (Category Wording + Real-time In-Modal Bursts)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(theme.primaryColor, theme.secondaryColor, NeonCyan)
                                )
                            )
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                burstTriggerCounter++
                                onTriggerFireworks()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = DeepIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = theme.burstButtonText,
                                fontFamily = FontFamily.Default,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepIndigo
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary Actions: Share & Dismiss
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Share Button with Category Context
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .background(SpaceDark)
                                .border(
                                    width = 1.dp,
                                    color = NeonCyan.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(23.dp)
                                )
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            theme.shareTemplate(eventTitle.ifBlank { "Event" })
                                        )
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share Celebration")
                                    context.startActivity(shareIntent)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Share",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeonCyan
                                )
                            }
                        }

                        // Done / Dismiss Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .background(SpaceDark)
                                .border(
                                    width = 1.dp,
                                    color = TextMuted.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(23.dp)
                                )
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cherish & Close",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Foreground Realtime Fireworks Canvas Overlay (Rendered directly ON TOP of modal scrim and dialog edges!)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // When trigger counter increments, spawn explosions across screen
                val w = size.width
                val h = size.height

                particles.forEach { p ->
                    val colorWithAlpha = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f))
                    if (p.isRibbon) {
                        rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                            drawRoundRect(
                                color = colorWithAlpha,
                                topLeft = Offset(p.x - p.size, p.y - p.size * 0.4f),
                                size = androidx.compose.ui.geometry.Size(p.size * 2f, p.size * 0.8f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                            )
                        }
                    } else {
                        drawCircle(
                            color = colorWithAlpha,
                            radius = p.size * p.life.coerceIn(0.2f, 1.2f),
                            center = Offset(p.x, p.y)
                        )
                    }
                }
            }

            // Burst trigger watcher: adds 3 burst shells when burstTriggerCounter changes
            LaunchedEffect(burstTriggerCounter) {
                if (burstTriggerCounter > 0) {
                    // Spawn multi-origin explosions
                    spawnExplosion(originX = 540f, originY = 650f, count = 45)
                    spawnExplosion(originX = 260f, originY = 850f, count = 35)
                    spawnExplosion(originX = 820f, originY = 780f, count = 35)
                }
            }
        }
    }
}

/**
 * Animated jewel crest with rotating starlight aura and pulsing ambient glow.
 * Dynamically styled with category icon and accent color.
 */
@Composable
private fun OrbitalJewelCrest(
    theme: CategoryCelebrationTheme,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbital_transition")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_pulse"
    )

    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        // Rotating Canvas Aura
        Canvas(modifier = Modifier.size(98.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            // Radial background bloom in category primary color
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        theme.primaryColor.copy(alpha = 0.32f * pulseScale),
                        theme.secondaryColor.copy(alpha = 0.16f * pulseScale),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = 48.dp.toPx()
                ),
                radius = 48.dp.toPx(),
                center = Offset(cx, cy)
            )

            // Outer rotating dotted ring
            rotate(degrees = rotationAngle, pivot = Offset(cx, cy)) {
                drawCircle(
                    color = theme.primaryColor.copy(alpha = 0.65f),
                    radius = 42.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(6.dp.toPx(), 8.dp.toPx()),
                            0f
                        ),
                        cap = StrokeCap.Round
                    )
                )

                // 4 Satellite Sparkle Dots
                val dist = 42.dp.toPx()
                drawCircle(color = theme.secondaryColor, radius = 2.4.dp.toPx(), center = Offset(cx + dist, cy))
                drawCircle(color = theme.primaryColor, radius = 2.4.dp.toPx(), center = Offset(cx - dist, cy))
                drawCircle(color = NeonPink, radius = 2.4.dp.toPx(), center = Offset(cx, cy + dist))
                drawCircle(color = GoldMetallic, radius = 2.4.dp.toPx(), center = Offset(cx, cy - dist))
            }
        }

        // Inner Frosted Crest Disc displaying the category's specific icon
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            theme.primaryColor.copy(alpha = 0.25f),
                            DeepIndigo.copy(alpha = 0.92f)
                        )
                    )
                )
                .border(
                    width = 1.8.dp,
                    brush = Brush.sweepGradient(
                        listOf(theme.primaryColor, theme.secondaryColor, theme.primaryColor)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = theme.icon,
                contentDescription = null,
                tint = theme.primaryColor,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
