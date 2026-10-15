package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.TimeEngine
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.ModernToggle
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ChronoUiState
import kotlinx.coroutines.delay

@Composable
fun WidgetsDashboardScreen(
    uiState: ChronoUiState,
    onPowerSaverToggled: (Boolean) -> Unit,
    onHudInteraction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var copiedCheck by remember { mutableStateOf(false) }

    LaunchedEffect(copiedCheck) {
        if (copiedCheck) {
            delay(2000)
            copiedCheck = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Widgets & Smart Insights",
                    fontFamily = FontFamily.Default,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // 1. Year & Seasons Progress Card with Circular Arc Dial
        item {
            val progressAnim by animateFloatAsState(
                targetValue = uiState.widgetTelemetry.yearProgressPercent / 100f,
                animationSpec = tween(700),
                label = "year_circle_progress"
            )

            FrostedGlassCard(
                borderColor = NeonCyan,
                glowAlpha = 0.45f,
                cornerRadius = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onHudInteraction("Visual progress through the calendar year and upcoming seasons.")
                    }
                    .testTag("widget_year_progress")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timeline,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Year & Season Progress",
                                fontFamily = FontFamily.Default,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Next Season Countdown Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${uiState.widgetTelemetry.daysUntilNextSeason}d to ${uiState.widgetTelemetry.nextSeasonName}",
                                fontFamily = FontFamily.Default,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circular Radial Dial Arc
                        Box(
                            modifier = Modifier.size(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 8.dp.toPx()
                                // Background ring
                                drawArc(
                                    color = SpaceCardBorder,
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                                )
                                // Progress sweep arc
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        listOf(NeonCyan, NeonPurple, NeonBlue, NeonCyan)
                                    ),
                                    startAngle = -90f,
                                    sweepAngle = 360f * progressAnim,
                                    useCenter = false,
                                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            Text(
                                text = String.format("%.0f%%", uiState.widgetTelemetry.yearProgressPercent),
                                fontFamily = FontFamily.Default,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Day ${uiState.widgetTelemetry.dayOfYear} of ${uiState.widgetTelemetry.totalDaysInYear}",
                                fontFamily = FontFamily.Default,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${uiState.widgetTelemetry.totalDaysInYear - uiState.widgetTelemetry.dayOfYear} days remaining in this year",
                                fontFamily = FontFamily.Default,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Exact progress: ${String.format("%.2f%%", uiState.widgetTelemetry.yearProgressPercent)} completed",
                                fontFamily = FontFamily.Default,
                                fontSize = 12.sp,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }
        }

        // 2. Month & Quarter Progress Dual Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Month Progress
                FrostedGlassCard(
                    borderColor = NeonPurple,
                    glowAlpha = 0.4f,
                    cornerRadius = 20.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column {
                        Text(
                            text = "Month Progress",
                            fontFamily = FontFamily.Default,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonPurple
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format("%.1f%%", uiState.widgetTelemetry.monthProgressPercent),
                            fontFamily = FontFamily.Default,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { uiState.widgetTelemetry.monthProgressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = NeonPurple,
                            trackColor = SpaceCardBorder
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Day ${uiState.widgetTelemetry.dayOfMonth} of ${uiState.widgetTelemetry.totalDaysInMonth}",
                            fontFamily = FontFamily.Default,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                // Quarter Progress
                FrostedGlassCard(
                    borderColor = NeonBlue,
                    glowAlpha = 0.4f,
                    cornerRadius = 20.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column {
                        Text(
                            text = "Q${uiState.widgetTelemetry.quarterNumber} Progress",
                            fontFamily = FontFamily.Default,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format("%.1f%%", uiState.widgetTelemetry.quarterProgressPercent),
                            fontFamily = FontFamily.Default,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { uiState.widgetTelemetry.quarterProgressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = NeonBlue,
                            trackColor = SpaceCardBorder
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Quarter ${uiState.widgetTelemetry.quarterNumber} of 4",
                            fontFamily = FontFamily.Default,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // 3. Quick Date Jumps (Instant Fast Forward Projections)
        item {
            FrostedGlassCard(
                borderColor = NeonEmerald,
                glowAlpha = 0.4f,
                cornerRadius = 24.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(NeonEmerald.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Quick Date Jumps",
                                fontFamily = FontFamily.Default,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "Fast Forward",
                            fontFamily = FontFamily.Default,
                            fontSize = 12.sp,
                            color = NeonEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        uiState.widgetTelemetry.quickJumps.forEach { jump ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SpaceCard)
                                    .border(1.dp, SpaceCardBorder, RoundedCornerShape(14.dp))
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = jump.label,
                                        fontFamily = FontFamily.Default,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonEmerald
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = jump.targetDate.format(TimeEngine.SHORT_DATE_FORMATTER),
                                        fontFamily = FontFamily.Default,
                                        fontSize = 11.sp,
                                        color = TextPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. World & Precision Time Card
        item {
            FrostedGlassCard(
                borderColor = NeonPurple,
                glowAlpha = 0.45f,
                cornerRadius = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onHudInteraction("World & Precision Time: live milliseconds and UTC atomic clock time.")
                    }
                    .testTag("widget_unix_epoch")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(NeonPurple.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = NeonPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "World & Precision Time",
                                fontFamily = FontFamily.Default,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${uiState.widgetTelemetry.epochMillis}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurple
                        )

                        Text(
                            text = "Unix seconds: ${uiState.widgetTelemetry.epochSeconds}",
                            fontFamily = FontFamily.Default,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Timestamp", "${uiState.widgetTelemetry.epochMillis}")
                            clipboard.setPrimaryClip(clip)
                            copiedCheck = true
                            Toast.makeText(context, "Timestamp copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("copy_epoch_button")
                    ) {
                        AnimatedVisibility(visible = copiedCheck) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Copied",
                                tint = NeonEmerald
                            )
                        }
                        AnimatedVisibility(visible = !copiedCheck) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Timestamp",
                                tint = NeonPurple
                            )
                        }
                    }
                }
            }
        }

        // 5. Home Screen Widget Companion Card
        item {
            FrostedGlassCard(
                borderColor = NeonEmerald,
                glowAlpha = 0.45f,
                cornerRadius = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onHudInteraction("Place the क्षण widget on your home screen for instant glanceable countdowns.")
                    }
                    .testTag("widget_home_screen_preview")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Home Screen Widget",
                            fontFamily = FontFamily.Default,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(NeonEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Synced",
                                fontFamily = FontFamily.Default,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonEmerald
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Active Event: \"${uiState.activeEventTitle}\"",
                        fontFamily = FontFamily.Default,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonEmerald
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Designed with frosted glass and high-contrast typography to look gorgeous on any phone wallpaper.",
                        fontFamily = FontFamily.Default,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // 6. Battery Saver Card
        item {
            FrostedGlassCard(
                borderColor = SpaceCardBorder,
                glowAlpha = 0.3f,
                cornerRadius = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("widget_power_saver_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SpaceCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatterySaver,
                                contentDescription = null,
                                tint = if (uiState.powerSaverEnabled) NeonCyan else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Battery Saver",
                                fontFamily = FontFamily.Default,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Pauses background particle animations",
                                fontFamily = FontFamily.Default,
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }

                    ModernToggle(
                        checked = uiState.powerSaverEnabled,
                        onCheckedChange = { onPowerSaverToggled(it) },
                        testTag = "power_saver_toggle"
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}
