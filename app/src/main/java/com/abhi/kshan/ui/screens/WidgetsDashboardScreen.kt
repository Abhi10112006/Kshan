package com.abhi.kshan.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatterySaver
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.kshan.data.ChronoEvent
import com.abhi.kshan.engine.QuickJumpItem
import com.abhi.kshan.engine.TimeEngine
import com.abhi.kshan.ui.components.FrostedActionButton
import com.abhi.kshan.ui.components.FrostedGlassCard
import com.abhi.kshan.ui.components.YearDotMatrixGrid
import com.abhi.kshan.ui.theme.JetBrainsMonoFontFamily
import com.abhi.kshan.ui.theme.KshanBorder
import com.abhi.kshan.ui.theme.KshanCyan
import com.abhi.kshan.ui.theme.KshanPillShape
import com.abhi.kshan.ui.theme.KshanSurface
import com.abhi.kshan.ui.theme.MetricLabelTextStyle
import com.abhi.kshan.ui.theme.SpaceGroteskFontFamily
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.ui.theme.TextSecondary
import com.abhi.kshan.viewmodel.ChronoUiState
import com.abhi.kshan.widget.ChronoWidgetProvider
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun WidgetsDashboardScreen(
    uiState: ChronoUiState,
    onPowerSaverToggled: (Boolean) -> Unit,
    onHudInteraction: (String) -> Unit,
    savedEvents: List<ChronoEvent> = emptyList(),
    onOpenJumpInPlanner: (Long, String) -> Unit = { _, _ -> },
    onOpenJumpInRunway: (LocalDate, String) -> Unit = { _, _ -> },
    onSaveJumpToKshans: (String, LocalDate) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showOrbitalRingMode by rememberSaveable { mutableStateOf(false) }

    val heroEvent = savedEvents.firstOrNull { it.isPinned } ?: savedEvents.firstOrNull()
    val wt = uiState.widgetTelemetry
    val telemetry = uiState.presentKshanTelemetry
    val currentYear = remember { LocalDate.now().year }

    val pinWidgetHelper: (String) -> Unit = { widgetStyleName ->
        val pinned = requestPinChronoWidget(context)
        if (pinned) {
            onHudInteraction("Requested $widgetStyleName widget pin")
        } else {
            onHudInteraction("Long-press home screen to add Kshan widget")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.widthIn(max = 600.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // =========================================================================
            // SECTION 1: LIVE LIFE PULSE (Morning Check-In Dashboard)
            // =========================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIVE LIFE PULSE",
                    style = MetricLabelTextStyle
                )

                // Segmented Full-Pill Toggle: [ 365 Dots | Ring ]
                Row(
                    modifier = Modifier
                        .clip(KshanPillShape)
                        .background(KshanSurface)
                        .border(1.dp, KshanBorder, KshanPillShape)
                        .padding(3.dp)
                        .testTag("year_view_mode_toggle"),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(KshanPillShape)
                            .background(if (!showOrbitalRingMode) KshanCyan else Color.Transparent)
                            .clickable {
                                showOrbitalRingMode = false
                                onHudInteraction("365-Dot Year Matrix")
                            }
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "365 Dots",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (!showOrbitalRingMode) Color(0xFF070A12) else TextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(KshanPillShape)
                            .background(if (showOrbitalRingMode) KshanCyan else Color.Transparent)
                            .clickable {
                                showOrbitalRingMode = true
                                onHudInteraction("Orbital Progress Ring")
                            }
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ring",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (showOrbitalRingMode) Color(0xFF070A12) else TextSecondary
                        )
                    }
                }
            }

            // Today's Waking Progress % & Quarter Sprint % side-by-side 24.dp cards
            val remSec = telemetry.secondsRemainingInDay.coerceAtLeast(0)
            val remHours = remSec / 3600
            val remMins = (remSec % 3600) / 60

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FrostedGlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pulse_today_progress_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "TODAY'S PACE",
                            style = MetricLabelTextStyle
                        )
                        Text(
                            text = "${"%.1f".format(telemetry.dayElapsedPercent)}%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontFeatureSettings = "tnum",
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                        LinearProgressIndicator(
                            progress = { (telemetry.dayElapsedPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(KshanPillShape),
                            color = KshanCyan,
                            trackColor = Color.White.copy(alpha = 0.08f)
                        )
                        Text(
                            text = "${remHours}h ${remMins}m left today",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontFeatureSettings = "tnum"
                            ),
                            color = TextSecondary
                        )
                    }
                }

                FrostedGlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pulse_quarter_progress_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Q${wt.quarterNumber} SPRINT",
                            style = MetricLabelTextStyle
                        )
                        Text(
                            text = "${"%.1f".format(wt.quarterProgressPercent)}%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontFeatureSettings = "tnum",
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                        LinearProgressIndicator(
                            progress = { (wt.quarterProgressPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(KshanPillShape),
                            color = KshanCyan,
                            trackColor = Color.White.copy(alpha = 0.08f)
                        )
                        val daysLeftInYear = (wt.totalDaysInYear - wt.dayOfYear).coerceAtLeast(0)
                        Text(
                            text = "${daysLeftInYear}d left in $currentYear",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontFeatureSettings = "tnum"
                            ),
                            color = TextSecondary
                        )
                    }
                }
            }

            // 365-Dot Year Matrix OR Orbital Ring Card (24.dp #121620)
            if (!showOrbitalRingMode) {
                FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                    YearDotMatrixGrid(
                        currentYear = currentYear,
                        dayOfYear = wt.dayOfYear,
                        totalDaysInYear = wt.totalDaysInYear,
                        savedEvents = savedEvents,
                        onDayInspected = onHudInteraction
                    )
                }
            } else {
                val daysRemaining = (wt.totalDaysInYear - wt.dayOfYear).coerceAtLeast(0)
                FrostedGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("widget_year_progress_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "$currentYear ORBITAL TELEMETRY",
                                style = MetricLabelTextStyle
                            )
                            Text(
                                text = "${"%.2f".format(wt.yearProgressPercent)}%",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontFeatureSettings = "tnum",
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "Day ${wt.dayOfYear} of ${wt.totalDaysInYear} • $daysRemaining days left",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = TextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier.size(96.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val progressFraction = (wt.yearProgressPercent / 100f).coerceIn(0f, 1f)
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val stroke = 9.dp.toPx()
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.08f),
                                    style = Stroke(width = stroke)
                                )
                                drawArc(
                                    color = KshanCyan,
                                    startAngle = -90f,
                                    sweepAngle = progressFraction * 360f,
                                    useCenter = false,
                                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                                )
                            }
                            Text(
                                text = "${wt.yearProgressPercent.toInt()}%",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontFeatureSettings = "tnum",
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // SECTION 2: INTERACTIVE QUICK DATE JUMPS (2x2 Bento Grid)
            // =========================================================================
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "QUICK HORIZON JUMPS",
                    style = MetricLabelTextStyle
                )
                Text(
                    text = "Tap a horizon card to open in Planner or bookmark to Kshans",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            val jumps = wt.quickJumps
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pulse_quick_jumps_bento"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                jumps.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { jump ->
                            QuickJumpBentoCard(
                                jump = jump,
                                onCardClick = {
                                    if (jump.label.startsWith("+")) {
                                        onOpenJumpInPlanner(jump.daysAdded.toLong(), jump.label)
                                    } else {
                                        onOpenJumpInRunway(jump.targetDate, jump.label)
                                    }
                                },
                                onBookmarkClick = {
                                    onSaveJumpToKshans(jump.label, jump.targetDate)
                                    onHudInteraction("Saved ${jump.label} to Kshans")
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // SECTION 3: NOTHING OS HOME SCREEN WIDGET STUDIO
            // =========================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Widgets,
                        contentDescription = null,
                        tint = KshanCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "NOTHING OS WIDGET STUDIO",
                            style = MetricLabelTextStyle
                        )
                        Text(
                            text = "Industrial dot-matrix home screen companions",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            WidgetGalleryMockupsSection(
                heroEvent = heroEvent,
                heroTelemetry = uiState.countdownTelemetry,
                widgetTelemetry = wt,
                onPinWidget = pinWidgetHelper
            )

            // =========================================================================
            // SECTION 4: WORLD & PRECISION UNIX TIME + BATTERY SAVER
            // =========================================================================
            val utcNowStr = remember(wt.epochSeconds) {
                ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            }
            val localNowStr = remember(wt.epochSeconds) {
                ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            }

            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Public,
                                contentDescription = null,
                                tint = KshanCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "WORLD & UNIX EPOCH",
                                style = MetricLabelTextStyle
                            )
                        }

                        FrostedActionButton(
                            icon = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy Unix Epoch",
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText("Unix Timestamp", wt.epochSeconds.toString())
                                )
                                onHudInteraction("Copied Unix Epoch to clipboard")
                            },
                            testTag = "copy_epoch_button"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ClockReadoutBox(
                            label = "UTC TIME",
                            value = utcNowStr,
                            modifier = Modifier.weight(1f)
                        )
                        ClockReadoutBox(
                            label = "LOCAL TIME",
                            value = localNowStr,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(KshanPillShape)
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(1.dp, KshanBorder, KshanPillShape)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "UNIX EPOCH (SEC)",
                                style = MetricLabelTextStyle
                            )
                            Text(
                                text = "${wt.epochSeconds}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontFeatureSettings = "tnum",
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary,
                                modifier = Modifier.testTag("widget_unix_epoch")
                            )
                        }
                    }
                }
            }

            // Battery Saver Toggle Card (24.dp #121620)
            FrostedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("widget_power_saver_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BatterySaver,
                            contentDescription = null,
                            tint = KshanCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Battery Saver",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = SpaceGroteskFontFamily
                                ),
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pauses background particle animations",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = uiState.powerSaverEnabled,
                        onCheckedChange = {
                            onPowerSaverToggled(it)
                            onHudInteraction(if (it) "Battery saver active" else "Live 1s refresh active")
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF070A12),
                            checkedTrackColor = KshanCyan,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = KshanSurface
                        ),
                        modifier = Modifier.testTag("power_saver_toggle")
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickJumpBentoCard(
    jump: QuickJumpItem,
    onCardClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDate = remember(jump.targetDate) {
        jump.targetDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    }
    val dayOfWeek = remember(jump.targetDate) {
        jump.targetDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    }

    FrostedGlassCard(
        modifier = modifier
            .clickable { onCardClick() }
            .testTag("quick_jump_card_${jump.label}")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = jump.label.uppercase(),
                    style = MetricLabelTextStyle.copy(color = KshanCyan),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(KshanPillShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, KshanBorder, KshanPillShape)
                        .clickable { onBookmarkClick() }
                        .testTag("quick_jump_save_${jump.label}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkAdd,
                        contentDescription = "Save ${jump.label} to Kshans",
                        tint = KshanCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontFeatureSettings = "tnum",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayOfWeek,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "Plan →",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = KshanCyan
                )
            }
        }
    }
}

@Composable
private fun ClockReadoutBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(KshanPillShape)
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, KshanBorder, KshanPillShape)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label.uppercase(),
            style = MetricLabelTextStyle
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontFeatureSettings = "tnum",
                fontWeight = FontWeight.Bold
            ),
            color = TextPrimary
        )
    }
}

private fun requestPinChronoWidget(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
        if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
            val provider = ComponentName(context, ChronoWidgetProvider::class.java)
            return appWidgetManager.requestPinAppWidget(provider, null, null)
        }
    }
    return false
}
