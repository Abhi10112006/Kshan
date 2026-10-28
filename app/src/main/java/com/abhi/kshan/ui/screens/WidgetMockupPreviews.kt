package com.abhi.kshan.ui.screens

import android.util.Log
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.kshan.data.ChronoEvent
import com.abhi.kshan.engine.CountdownTelemetry
import com.abhi.kshan.engine.WidgetTelemetry
import com.abhi.kshan.ui.components.FrostedGlassCard
import com.abhi.kshan.ui.theme.JetBrainsMonoFontFamily
import com.abhi.kshan.ui.theme.KshanBackground
import com.abhi.kshan.ui.theme.KshanBorder
import com.abhi.kshan.ui.theme.KshanCardShape
import com.abhi.kshan.ui.theme.KshanCyan
import com.abhi.kshan.ui.theme.KshanPillShape
import com.abhi.kshan.ui.theme.KshanSurface
import com.abhi.kshan.ui.theme.MetricLabelTextStyle
import com.abhi.kshan.ui.theme.SpaceGroteskFontFamily
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.ui.theme.TextSecondary
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun WidgetGalleryMockupsSection(
    heroEvent: ChronoEvent?,
    heroTelemetry: CountdownTelemetry,
    widgetTelemetry: WidgetTelemetry,
    onPinWidget: (String) -> Unit = {}
) {
    val eventTitle = heroEvent?.title ?: "Next 30d Sprint"
    val eventCategory = (heroEvent?.category ?: "MILESTONE").uppercase()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }
    val eventDateStr = remember(heroEvent) {
        if (heroEvent != null) {
            try {
                ZonedDateTime.parse(heroEvent.targetIsoString).format(dateFormatter)
            } catch (e: Exception) {
                Log.w("WidgetGalleryMockups", "Date parse fallback", e)
                "Jan 01, 2027"
            }
        } else {
            "Jan 01, 2027"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("widget_gallery_mockups"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Nothing OS Dot-Matrix Milestone 4x2 Widget Card
        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                WidgetCardHeader(
                    badge = "NOTHING OS 4×2 • MILESTONE MATRIX",
                    onPinClick = { onPinWidget("Milestone Matrix 4×2") },
                    testTag = "pin_widget_4x2"
                )

                // Industrial #070A12 Chassis Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(KshanCardShape)
                        .background(KshanBackground)
                        .border(1.dp, KshanBorder, KshanCardShape)
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(KshanPillShape)
                                            .background(KshanCyan)
                                    )
                                    Text(
                                        text = "KSHAN // $eventCategory",
                                        style = MetricLabelTextStyle.copy(color = KshanCyan)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = eventTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = eventDateStr,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = TextSecondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NothingMiniTile(
                                value = "%02d".format(heroTelemetry.days),
                                label = "DAYS",
                                modifier = Modifier.weight(1.2f)
                            )
                            NothingMiniTile(
                                value = "%02d".format(heroTelemetry.hours),
                                label = "HRS",
                                modifier = Modifier.weight(1f)
                            )
                            NothingMiniTile(
                                value = "%02d".format(heroTelemetry.minutes),
                                label = "MIN",
                                modifier = Modifier.weight(1f)
                            )
                            NothingMiniTile(
                                value = "%02d".format(heroTelemetry.seconds),
                                label = "SEC",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 2. Side-by-side 2x2 Nothing OS Widgets: Orbital Dial & 365-Dot Matrix
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 2A: 2x2 Orbital Dial
            FrostedGlassCard(modifier = Modifier.weight(1f)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ORBITAL 2×2",
                        style = MetricLabelTextStyle
                    )

                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(KshanCardShape)
                            .background(KshanBackground)
                            .border(1.dp, KshanBorder, KshanCardShape)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val ringFraction = ((heroTelemetry.days % 365L).toFloat() / 365f).coerceIn(0.08f, 0.92f)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 5.dp.toPx()
                            drawCircle(
                                color = Color.White.copy(alpha = 0.08f),
                                style = Stroke(width = stroke)
                            )
                            drawArc(
                                color = KshanCyan,
                                startAngle = -90f,
                                sweepAngle = ringFraction * 360f,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${heroTelemetry.days}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontFeatureSettings = "tnum",
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "DAYS",
                                style = MetricLabelTextStyle.copy(fontSize = 8.sp)
                            )
                        }
                    }

                    PinWidgetPillButton(
                        onClick = { onPinWidget("Orbital 2×2") },
                        testTag = "pin_widget_orbital_2x2"
                    )
                }
            }

            // 2B: 2x2 365-Dot Matrix Preview
            FrostedGlassCard(modifier = Modifier.weight(1f)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "DOT MATRIX 2×2",
                        style = MetricLabelTextStyle
                    )

                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(KshanCardShape)
                            .background(KshanBackground)
                            .border(1.dp, KshanBorder, KshanCardShape)
                            .padding(10.dp)
                    ) {
                        val progressRatio = (widgetTelemetry.yearProgressPercent / 100f).coerceIn(0f, 1f)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cols = 8
                            val rows = 6
                            val totalDots = cols * rows
                            val activeDots = (totalDots * progressRatio).toInt()
                            val spacingX = size.width / (cols - 1).coerceAtLeast(1)
                            val spacingY = size.height / (rows - 1).coerceAtLeast(1)
                            var idx = 0
                            for (r in 0 until rows) {
                                for (c in 0 until cols) {
                                    val color = when {
                                        idx < activeDots -> KshanCyan
                                        idx == activeDots -> Color.White
                                        else -> Color.White.copy(alpha = 0.14f)
                                    }
                                    drawCircle(
                                        color = color,
                                        radius = 2.4.dp.toPx(),
                                        center = Offset(c * spacingX, r * spacingY)
                                    )
                                    idx++
                                }
                            }
                        }
                    }

                    PinWidgetPillButton(
                        onClick = { onPinWidget("Dot Matrix 2×2") },
                        testTag = "pin_widget_dot_2x2"
                    )
                }
            }
        }

        // 3. Minimal Horizon Pill 4x1 Widget
        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                WidgetCardHeader(
                    badge = "MINIMAL HORIZON PILL 4×1",
                    onPinClick = { onPinWidget("Horizon Pill 4×1") },
                    testTag = "pin_widget_4x1"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(KshanPillShape)
                        .background(KshanBackground)
                        .border(1.dp, KshanBorder, KshanPillShape)
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(KshanPillShape)
                                .background(KshanCyan)
                        )
                        Text(
                            text = eventTitle,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "${heroTelemetry.days}d ${heroTelemetry.hours}h",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontFeatureSettings = "tnum",
                            fontWeight = FontWeight.Bold
                        ),
                        color = KshanCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun WidgetCardHeader(
    badge: String,
    onPinClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = badge,
            style = MetricLabelTextStyle
        )
        PinWidgetPillButton(
            onClick = onPinClick,
            testTag = testTag
        )
    }
}

@Composable
private fun PinWidgetPillButton(
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(KshanPillShape)
            .background(KshanSurface)
            .border(1.dp, KshanCyan.copy(alpha = 0.45f), KshanPillShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = KshanCyan,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "Pin to Home Screen",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = KshanCyan
            )
        }
    }
}

@Composable
private fun NothingMiniTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(KshanPillShape)
            .background(KshanSurface)
            .border(1.dp, KshanBorder, KshanPillShape)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontFeatureSettings = "tnum",
                fontWeight = FontWeight.Bold
            ),
            color = TextPrimary
        )
        Text(
            text = label,
            style = MetricLabelTextStyle.copy(fontSize = 9.sp),
            color = TextMuted
        )
    }
}
