package com.abhi.kshan.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.kshan.engine.TimeEngine
import com.abhi.kshan.ui.components.FrostedGlassCard
import com.abhi.kshan.ui.components.ModernPillButton
import com.abhi.kshan.ui.components.ModernToggle
import com.abhi.kshan.ui.theme.ComicNeueFontFamily
import com.abhi.kshan.ui.theme.NeonBlue
import com.abhi.kshan.ui.theme.NeonCyan
import com.abhi.kshan.ui.theme.NeonEmerald
import com.abhi.kshan.ui.theme.NeonPurple
import com.abhi.kshan.ui.theme.SpaceCard
import com.abhi.kshan.ui.theme.SpaceCardBorder
import com.abhi.kshan.ui.theme.SpaceDark
import com.abhi.kshan.ui.theme.TabularMonoFontFamily
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.ui.theme.TextSecondary
import com.abhi.kshan.viewmodel.ChronoUiState
import com.abhi.kshan.widget.ChronoWidgetPinningManager
import com.abhi.kshan.widget.ChronoWidgetPreferences
import com.abhi.kshan.widget.WidgetCustomSettings
import kotlinx.coroutines.delay

@Composable
fun WidgetsDashboardScreen(
    uiState: ChronoUiState,
    onPowerSaverToggled: (Boolean) -> Unit,
    onHudInteraction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    var copiedCheck by remember { mutableStateOf(false) }
    var selectedFormat by remember { mutableStateOf(WidgetPreviewFormat.PANORAMIC_4X2) }
    var showManualGuideDialog by remember { mutableStateOf(false) }

    // Live Customizer State
    val initialSettings = remember { ChronoWidgetPreferences.loadSettings(context) }
    var liveOpacity by remember { mutableFloatStateOf(initialSettings.opacity) }
    var liveColorIndex by remember { mutableIntStateOf(initialSettings.colorIndex) }
    var liveShowBadge by remember { mutableStateOf(initialSettings.showCategoryBadge) }
    var liveShowProgress by remember { mutableStateOf(initialSettings.showProgressArc) }
    var liveShowTarget by remember { mutableStateOf(initialSettings.showTargetDate) }

    val activeColor = remember(liveColorIndex) {
        ChronoWidgetPreferences.COLOR_SWATCHES.getOrElse(liveColorIndex) { ChronoWidgetPreferences.COLOR_SWATCHES[0] }.second
    }

    fun persistSettings(broadcast: Boolean) {
        val newSettings = WidgetCustomSettings(
            opacity = liveOpacity,
            colorIndex = liveColorIndex,
            showCategoryBadge = liveShowBadge,
            showProgressArc = liveShowProgress,
            showTargetDate = liveShowTarget
        )
        ChronoWidgetPreferences.saveSettings(context, newSettings, broadcastUpdate = broadcast)
    }

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
                    text = "OS Widget Studio & Insights",
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // 1. Interactive Phone Mockup Carousel
        item {
            FrostedGlassCard(
                borderColor = activeColor,
                glowAlpha = 0.5f,
                cornerRadius = 28.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Launcher Preview",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = selectedFormat.label,
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeColor
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Format Switcher Tabs (2x2 Orbit | 4x2 Panoramic | 4x1 Minimal Pill)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WidgetPreviewFormat.entries.forEach { fmt ->
                            val isSel = fmt == selectedFormat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) activeColor.copy(alpha = 0.2f) else SpaceCard)
                                    .border(
                                        1.dp,
                                        if (isSel) activeColor else SpaceCardBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        selectedFormat = fmt
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = fmt.label,
                                    fontFamily = ComicNeueFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) activeColor else TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    UpperHalfPhoneMockup(
                        format = selectedFormat,
                        uiState = uiState,
                        opacity = liveOpacity,
                        accentColor = activeColor,
                        showBadge = liveShowBadge,
                        showProgress = liveShowProgress,
                        showTargetDate = liveShowTarget
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ModernPillButton(
                        text = "Add to Home Screen",
                        icon = Icons.Default.Add,
                        onClick = {
                            val pinned = ChronoWidgetPinningManager.requestPinWidget(context)
                            if (pinned) {
                                Toast.makeText(context, "Pinning request sent to your launcher!", Toast.LENGTH_SHORT).show()
                                onHudInteraction("Pinned क्षण widget to your home screen.")
                            } else {
                                showManualGuideDialog = true
                            }
                        },
                        isPrimary = true,
                        accentColor = activeColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_widget_button")
                    )
                }
            }
        }

        // 2. Tactile Widget Customizer Suite
        item {
            FrostedGlassCard(
                borderColor = activeColor,
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
                                    .background(activeColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = activeColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Widget Customizer",
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "Live Sync",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeColor
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Background Opacity",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = String.format("%.0f%%", liveOpacity * 100),
                            fontFamily = TabularMonoFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeColor
                        )
                    }

                    Slider(
                        value = liveOpacity,
                        onValueChange = { liveOpacity = it },
                        onValueChangeFinished = {
                            persistSettings(broadcast = true)
                            Toast.makeText(context, "Widget opacity updated", Toast.LENGTH_SHORT).show()
                        },
                        valueRange = 0.2f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = activeColor,
                            activeTrackColor = activeColor,
                            inactiveTrackColor = SpaceCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Accent Neon Tint",
                        fontFamily = ComicNeueFontFamily,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ChronoWidgetPreferences.COLOR_SWATCHES.forEachIndexed { index, (name, color) ->
                            val isSel = index == liveColorIndex
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(color.copy(alpha = if (isSel) 0.9f else 0.35f))
                                    .border(
                                        width = if (isSel) 2.5.dp else 1.dp,
                                        color = if (isSel) Color.White else SpaceCardBorder,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        liveColorIndex = index
                                        persistSettings(broadcast = true)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = name,
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Visible Elements",
                        fontFamily = ComicNeueFontFamily,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Category Badge",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        ModernToggle(
                            checked = liveShowBadge,
                            onCheckedChange = {
                                liveShowBadge = it
                                persistSettings(broadcast = true)
                            },
                            activeColor = activeColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progress Ring / Bar",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        ModernToggle(
                            checked = liveShowProgress,
                            onCheckedChange = {
                                liveShowProgress = it
                                persistSettings(broadcast = true)
                            },
                            activeColor = activeColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target Date Label",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        ModernToggle(
                            checked = liveShowTarget,
                            onCheckedChange = {
                                liveShowTarget = it
                                persistSettings(broadcast = true)
                            },
                            activeColor = activeColor
                        )
                    }
                }
            }
        }

        // 3. Year & Seasons Progress Card
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
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${uiState.widgetTelemetry.daysUntilNextSeason}d to ${uiState.widgetTelemetry.nextSeasonName}",
                                fontFamily = ComicNeueFontFamily,
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
                        Box(
                            modifier = Modifier.size(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 8.dp.toPx()
                                drawArc(
                                    color = SpaceCardBorder,
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                                )
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
                                fontFamily = TabularMonoFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Day ${uiState.widgetTelemetry.dayOfYear} of ${uiState.widgetTelemetry.totalDaysInYear}",
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${uiState.widgetTelemetry.totalDaysInYear - uiState.widgetTelemetry.dayOfYear} days remaining in this year",
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Exact: ${String.format("%.2f%%", uiState.widgetTelemetry.yearProgressPercent)} completed",
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 12.sp,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }
        }

        // 4. Month & Quarter Progress Dual Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FrostedGlassCard(
                    borderColor = NeonPurple,
                    glowAlpha = 0.4f,
                    cornerRadius = 20.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column {
                        Text(
                            text = "Month Progress",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonPurple
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format("%.1f%%", uiState.widgetTelemetry.monthProgressPercent),
                            fontFamily = TabularMonoFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
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
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                FrostedGlassCard(
                    borderColor = NeonBlue,
                    glowAlpha = 0.4f,
                    cornerRadius = 20.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column {
                        Text(
                            text = "Q${uiState.widgetTelemetry.quarterNumber} Progress",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format("%.1f%%", uiState.widgetTelemetry.quarterProgressPercent),
                            fontFamily = TabularMonoFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
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
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // 5. Quick Date Jumps
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
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "Fast Forward",
                            fontFamily = ComicNeueFontFamily,
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
                                        fontFamily = ComicNeueFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonEmerald
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = jump.targetDate.format(TimeEngine.SHORT_DATE_FORMATTER),
                                        fontFamily = TabularMonoFontFamily,
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

        // 6. World & Precision Time Card
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
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${uiState.widgetTelemetry.epochMillis}",
                            fontFamily = TabularMonoFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurple
                        )

                        Text(
                            text = "Unix seconds: ${uiState.widgetTelemetry.epochSeconds}",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    IconButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
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

        // 7. Battery Saver Card
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
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Caps fireworks particles & pauses ambient starfield",
                                fontFamily = ComicNeueFontFamily,
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
            Spacer(modifier = Modifier.height(120.dp))
        }
    }

    if (showManualGuideDialog) {
        AlertDialog(
            onDismissRequest = { showManualGuideDialog = false },
            title = {
                Text(
                    text = "Add Widget to Home Screen",
                    fontFamily = ComicNeueFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "To place the widget manually on your phone:",
                        fontFamily = ComicNeueFontFamily,
                        color = TextSecondary
                    )
                    Text(
                        text = "1. Go to your phone's Home Screen\n2. Touch and hold an empty space\n3. Tap 'Widgets'\n4. Find 'क्षण (Kshan)' and drag it to your screen",
                        fontFamily = ComicNeueFontFamily,
                        color = TextPrimary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showManualGuideDialog = false }) {
                    Text("Got It", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SpaceDark
        )
    }
}
