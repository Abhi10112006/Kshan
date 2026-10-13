package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChronoEvent
import com.example.engine.CountdownStatus
import com.example.engine.TimeEngine
import com.example.ui.components.CyberDatePickerDialog
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.ModernPillButton
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ChronoUiState
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun CountdownScreen(
    uiState: ChronoUiState,
    savedEvents: List<ChronoEvent>,
    onPinEvent: (ChronoEvent) -> Unit,
    onDeleteEvent: (ChronoEvent) -> Unit,
    onAddEvent: (String, ZonedDateTime, String) -> Unit,
    onTriggerCelebration: () -> Unit,
    onHudInteraction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // Active Event Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.activeEventTitle,
                    fontFamily = FontFamily.Default,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                val statusText = when (uiState.countdownTelemetry.status) {
                    CountdownStatus.ACTIVE -> "Ticking live"
                    CountdownStatus.TARGET_REACHED -> "Target reached! Celebration time!"
                    CountdownStatus.EVENT_PASSED -> "Event has passed"
                    CountdownStatus.AWAITING -> "Waiting for date..."
                }

                val statusColor = when (uiState.countdownTelemetry.status) {
                    CountdownStatus.TARGET_REACHED -> NeonPink
                    CountdownStatus.EVENT_PASSED -> TextMuted
                    else -> NeonCyan
                }

                Text(
                    text = statusText,
                    fontFamily = FontFamily.Default,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4 Clean Frosted Dials (Days, Hours, Minutes, Seconds)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("countdown_dials_row"),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModernTimerDial(
                    value = String.format("%02d", uiState.countdownTelemetry.days),
                    label = "Days",
                    modifier = Modifier.weight(1f)
                )
                ModernTimerDial(
                    value = String.format("%02d", uiState.countdownTelemetry.hours),
                    label = "Hours",
                    modifier = Modifier.weight(1f)
                )
                ModernTimerDial(
                    value = String.format("%02d", uiState.countdownTelemetry.minutes),
                    label = "Mins",
                    modifier = Modifier.weight(1f)
                )
                ModernTimerDial(
                    value = String.format("%02d", uiState.countdownTelemetry.seconds),
                    label = "Secs",
                    isSeconds = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Action Buttons: + Add Countdown & Celebration Burst
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernPillButton(
                    text = "Add Countdown",
                    icon = Icons.Default.Add,
                    onClick = {
                        onHudInteraction("Create a new countdown for your milestone.")
                        showAddDialog = true
                    },
                    isPrimary = true,
                    accentColor = NeonCyan,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_event_button")
                )

                ModernPillButton(
                    text = "Celebration",
                    icon = Icons.Default.AutoAwesome,
                    onClick = onTriggerCelebration,
                    isPrimary = false,
                    accentColor = NeonPink,
                    modifier = Modifier.testTag("test_celebration_button")
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Saved Countdowns Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved Countdowns (${savedEvents.size})",
                    fontFamily = FontFamily.Default,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // List of Events
        items(savedEvents, key = { it.id }) { event ->
            SavedCountdownCard(
                event = event,
                onPin = { onPinEvent(event) },
                onDelete = { onDeleteEvent(event) },
                onHudInteraction = onHudInteraction
            )
        }

        item {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }

    if (showAddDialog) {
        AddCountdownDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, date, category ->
                onAddEvent(title, date, category)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ModernTimerDial(
    value: String,
    label: String,
    isSeconds: Boolean = false,
    modifier: Modifier = Modifier
) {
    FrostedGlassCard(
        modifier = modifier,
        borderColor = if (isSeconds) NeonCyan else SpaceCardBorder,
        glowAlpha = if (isSeconds) 0.5f else 0.25f,
        cornerRadius = 20.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = if (isSeconds) NeonCyan else TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontFamily = FontFamily.Default,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSeconds) NeonCyan else TextMuted
            )
        }
    }
}

@Composable
private fun SavedCountdownCard(
    event: ChronoEvent,
    onPin: () -> Unit,
    onDelete: () -> Unit,
    onHudInteraction: (String) -> Unit
) {
    val categoryColor = when (event.category) {
        "Holiday" -> NeonPurple
        "Goal" -> NeonCyan
        "Season" -> NeonBlue
        "Trip" -> NeonEmerald
        else -> NeonPink
    }

    FrostedGlassCard(
        borderColor = if (event.isPinned) NeonCyan else SpaceCardBorder,
        glowAlpha = if (event.isPinned) 0.6f else 0.25f,
        cornerRadius = 20.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}")
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
                            .clip(RoundedCornerShape(6.dp))
                            .background(categoryColor.copy(alpha = 0.15f))
                            .border(1.dp, categoryColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = event.category,
                            fontFamily = FontFamily.Default,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = categoryColor
                        )
                    }

                    if (event.isPinned) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pinned to Widget",
                            fontFamily = FontFamily.Default,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = event.title,
                    fontFamily = FontFamily.Default,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                val dateText = try {
                    val zdt = ZonedDateTime.parse(event.targetIsoString)
                    zdt.format(TimeEngine.FULL_DATE_FORMATTER)
                } catch (e: Exception) {
                    event.targetIsoString
                }

                Text(
                    text = dateText,
                    fontFamily = FontFamily.Default,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Row {
                IconButton(
                    onClick = {
                        onHudInteraction("'${event.title}' pinned to your home screen widget.")
                        onPin()
                    },
                    modifier = Modifier.testTag("pin_event_${event.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pin Countdown",
                        tint = if (event.isPinned) NeonCyan else TextMuted
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_event_${event.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Countdown",
                        tint = NeonPink.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddCountdownDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, ZonedDateTime, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now().plusDays(30)) }
    var selectedCategory by remember { mutableStateOf("Goal") }
    var showDatePicker by remember { mutableStateOf(false) }

    val categories = listOf("Goal", "Holiday", "Trip", "Birthday", "Milestone")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SpaceDark,
        title = {
            Text(
                text = "New Countdown",
                fontFamily = FontFamily.Default,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Name", fontFamily = FontFamily.Default) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = SpaceCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_event_title_input")
                )

                // Date Picker trigger card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SpaceCard)
                        .border(1.dp, NeonPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Target Date",
                            fontFamily = FontFamily.Default,
                            fontSize = 11.sp,
                            color = NeonPurple
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedDate.format(TimeEngine.FULL_DATE_FORMATTER),
                            fontFamily = FontFamily.Default,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                // Category selection
                Column {
                    Text(
                        text = "Category",
                        fontFamily = FontFamily.Default,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.forEach { cat ->
                            val isSel = cat == selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSel) NeonCyan.copy(alpha = 0.2f) else SpaceCard)
                                    .border(1.dp, if (isSel) NeonCyan else SpaceCardBorder, RoundedCornerShape(50))
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontFamily = FontFamily.Default,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) NeonCyan else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        val zdt = selectedDate.atTime(LocalTime.MIDNIGHT).atZone(ZoneId.systemDefault())
                        onConfirm(title, zdt, selectedCategory)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text(
                    text = "Save",
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    fontFamily = FontFamily.Default,
                    color = TextMuted
                )
            }
        }
    )

    if (showDatePicker) {
        CyberDatePickerDialog(
            initialDate = selectedDate,
            onDateSelected = { selectedDate = it },
            onDismiss = { showDatePicker = false }
        )
    }
}
