package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.ModernToggle
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ChronoUiState

@Composable
fun WidgetsDashboardScreen(
    uiState: ChronoUiState,
    onPowerSaverToggled: (Boolean) -> Unit,
    onHudInteraction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // Widget 1: Year Progress Card
        item {
            val progressAnim by animateFloatAsState(
                targetValue = uiState.widgetTelemetry.yearProgressPercent / 100f,
                animationSpec = tween(600),
                label = "year_progress_anim"
            )

            FrostedGlassCard(
                borderColor = NeonCyan,
                glowAlpha = 0.45f,
                cornerRadius = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onHudInteraction("Track how much of the current calendar year has elapsed.")
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
                                text = "Year Progress",
                                fontFamily = FontFamily.Default,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = String.format("%.1f%%", uiState.widgetTelemetry.yearProgressPercent),
                            fontFamily = FontFamily.Default,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { progressAnim },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = NeonCyan,
                        trackColor = SpaceCardBorder
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Day ${uiState.widgetTelemetry.dayOfYear} of ${uiState.widgetTelemetry.totalDaysInYear}",
                            fontFamily = FontFamily.Default,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "${uiState.widgetTelemetry.totalDaysInYear - uiState.widgetTelemetry.dayOfYear} days left",
                            fontFamily = FontFamily.Default,
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Widget 2: Live Millisecond Clock Card
        item {
            FrostedGlassCard(
                borderColor = NeonPurple,
                glowAlpha = 0.45f,
                cornerRadius = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onHudInteraction("Live Unix timestamp counting milliseconds since Jan 1, 1970.")
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
                                text = "Live Timestamp Clock",
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
                            val clip = ClipData.newPlainText("Epoch Timestamp", "${uiState.widgetTelemetry.epochMillis}")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Timestamp copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("copy_epoch_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Timestamp",
                            tint = NeonPurple
                        )
                    }
                }
            }
        }

        // Widget 3: Home Screen Widget Companion Card
        item {
            FrostedGlassCard(
                borderColor = NeonEmerald,
                glowAlpha = 0.45f,
                cornerRadius = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onHudInteraction("Place the Chrono-Scope widget on your home screen for quick glances.")
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

        // Widget 4: Battery Saver Card
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
