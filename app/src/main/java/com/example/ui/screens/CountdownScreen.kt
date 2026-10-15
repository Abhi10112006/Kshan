package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.components.ModernCapsuleChip
import com.example.ui.components.ModernPillButton
import com.example.ui.components.PremiumMilestoneModal
import com.example.ui.components.resolveCelebrationTheme
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ChronoUiState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownScreen(
    uiState: ChronoUiState,
    savedEvents: List<ChronoEvent>,
    onPinEvent: (ChronoEvent) -> Unit,
    onDeleteEvent: (ChronoEvent) -> Unit,
    onAddEvent: (String, ZonedDateTime, String) -> Unit,
    onTriggerCelebration: () -> Unit,
    onHudInteraction: (String) -> Unit,
    onClearCelebration: () -> Unit = {},
    onSelectEvent: (ChronoEvent) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddSheet by remember { mutableStateOf(false) }
    var showCelebrationCard by remember { mutableStateOf(false) }

    val activeTheme = remember(uiState.activeEventCategory, uiState.activeEventTitle) {
        resolveCelebrationTheme(uiState.activeEventCategory, uiState.activeEventTitle)
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Active Event Category & Title Header
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Category Badge Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(activeTheme.primaryColor.copy(alpha = 0.14f))
                            .border(1.dp, activeTheme.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = activeTheme.icon,
                                contentDescription = null,
                                tint = activeTheme.primaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.activeEventCategory.uppercase(),
                                fontFamily = FontFamily.Default,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = activeTheme.primaryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = uiState.activeEventTitle,
                        fontFamily = FontFamily.Default,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val statusText = when (uiState.countdownTelemetry.status) {
                        CountdownStatus.ACTIVE -> "Ticking live"
                        CountdownStatus.TARGET_REACHED -> activeTheme.statusPillText
                        CountdownStatus.EVENT_PASSED -> "Event has passed"
                        CountdownStatus.AWAITING -> "Waiting for date..."
                    }

                    val statusColor = when (uiState.countdownTelemetry.status) {
                        CountdownStatus.TARGET_REACHED -> activeTheme.primaryColor
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

                Spacer(modifier = Modifier.height(18.dp))

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

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons: + Add Countdown & Celebration Fireworks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernPillButton(
                        text = "Add Countdown",
                        icon = Icons.Default.Add,
                        onClick = {
                            onHudInteraction("Create a new countdown.")
                            showAddSheet = true
                        },
                        isPrimary = true,
                        accentColor = NeonCyan,
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("add_event_button")
                    )

                    ModernPillButton(
                        text = "Fireworks",
                        icon = Icons.Default.AutoAwesome,
                        onClick = {
                            onTriggerCelebration()
                            showCelebrationCard = true
                        },
                        isPrimary = false,
                        accentColor = activeTheme.primaryColor,
                        modifier = Modifier
                            .weight(0.9f)
                            .testTag("test_celebration_button")
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

            // List of Saved Events
            items(savedEvents, key = { it.id }) { event ->
                SavedCountdownCard(
                    event = event,
                    onPin = { onPinEvent(event) },
                    onDelete = { onDeleteEvent(event) },
                    onSelect = { onSelectEvent(event) },
                    onHudInteraction = onHudInteraction
                )
            }

            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }

        // Luxury Category-Adaptive Celebration Modal Overlay with In-Modal Fireworks
        PremiumMilestoneModal(
            visible = showCelebrationCard || uiState.triggerCelebration,
            eventTitle = uiState.activeEventTitle,
            category = uiState.activeEventCategory,
            onDismiss = {
                showCelebrationCard = false
                onClearCelebration()
            },
            onTriggerFireworks = onTriggerCelebration
        )
    }

    if (showAddSheet) {
        SurpriseAddCountdownSheet(
            onDismiss = { showAddSheet = false },
            onConfirm = { title, targetZdt, category ->
                onAddEvent(title, targetZdt, category)
                showAddSheet = false
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
    onSelect: () -> Unit = {},
    onHudInteraction: (String) -> Unit
) {
    val cardTheme = resolveCelebrationTheme(event.category, event.title)

    FrostedGlassCard(
        borderColor = if (event.isPinned) NeonCyan else SpaceCardBorder,
        glowAlpha = if (event.isPinned) 0.6f else 0.25f,
        cornerRadius = 20.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onHudInteraction("Active countdown set to '${event.title}'")
                onSelect()
            }
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
                            .clip(RoundedCornerShape(8.dp))
                            .background(cardTheme.primaryColor.copy(alpha = 0.15f))
                            .border(1.dp, cardTheme.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = cardTheme.icon,
                                contentDescription = null,
                                tint = cardTheme.primaryColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = event.category.uppercase(),
                                fontFamily = FontFamily.Default,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = cardTheme.primaryColor
                            )
                        }
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

// "Surprise Me" State-of-the-Art Add Countdown Modal Bottom Sheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SurpriseAddCountdownSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, ZonedDateTime, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now().plusDays(7)) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var selectedCategory by remember { mutableStateOf("Holiday") }
    var showDatePicker by remember { mutableStateOf(false) }

    val categories = remember {
        listOf(
            Triple("Holiday", Icons.Default.BeachAccess, NeonEmerald),
            Triple("Celebration", Icons.Default.Celebration, NeonPurple),
            Triple("Birthday", Icons.Default.Cake, NeonPink),
            Triple("Goal", Icons.Default.Flag, NeonCyan),
            Triple("Vacation", Icons.Default.BeachAccess, Color(0xFF38BDF8)),
            Triple("Milestone", Icons.Default.Star, Color(0xFFFFD700)),
            Triple("Deadline", Icons.Default.Alarm, NeonBlue),
            Triple("Personal", Icons.Default.Favorite, Color(0xFFF472B6))
        )
    }

    val targetZdt = remember(selectedDate, selectedTime) {
        selectedDate.atTime(selectedTime).atZone(ZoneId.systemDefault())
    }
    val nowZdt = ZonedDateTime.now()
    val liveDays = remember(targetZdt, nowZdt) {
        java.time.temporal.ChronoUnit.DAYS.between(nowZdt.toLocalDate(), selectedDate).coerceAtLeast(0)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SpaceDark,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(SpaceCardBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Countdown",
                    fontFamily = FontFamily.Default,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Cancel",
                    fontFamily = FontFamily.Default,
                    fontSize = 14.sp,
                    color = TextMuted,
                    modifier = Modifier.clickable(onClick = onDismiss)
                )
            }

            // Live Interactive Preview Card
            FrostedGlassCard(
                borderColor = NeonCyan,
                glowAlpha = 0.5f,
                cornerRadius = 20.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedCategory.uppercase(),
                            fontFamily = FontFamily.Default,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (title.isBlank()) "Event Title Preview" else title,
                            fontFamily = FontFamily.Default,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (title.isBlank()) TextMuted else TextPrimary
                        )
                        Text(
                            text = "${selectedDate.format(TimeEngine.SHORT_DATE_FORMATTER)} at ${selectedTime.format(TimeEngine.TIME_FORMATTER)}",
                            fontFamily = FontFamily.Default,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$liveDays",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan
                        )
                        Text(
                            text = "days left",
                            fontFamily = FontFamily.Default,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // Event Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("What are you looking forward to?") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = SpaceCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = NeonCyan,
                    unfocusedLabelColor = TextMuted
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_event_title_input")
            )

            // Smart 1-Tap Date Presets
            val presetsScroll = rememberScrollState()
            Column {
                Text(
                    text = "Quick Date Presets",
                    fontFamily = FontFamily.Default,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(presetsScroll),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val today = LocalDate.now()
                    val tomorrow = today.plusDays(1)
                    val nextSat = today.plusDays(((DayOfWeek.SATURDAY.value - today.dayOfWeek.value + 7) % 7).toLong().let { if (it == 0L) 7L else it })
                    val in1Week = today.plusWeeks(1)
                    val in1Month = today.plusMonths(1)
                    val endOfYear = LocalDate.of(today.year, 12, 31)

                    ModernCapsuleChip("Tomorrow", selectedDate == tomorrow, { selectedDate = tomorrow })
                    ModernCapsuleChip("This Weekend", selectedDate == nextSat, { selectedDate = nextSat })
                    ModernCapsuleChip("In 1 Week", selectedDate == in1Week, { selectedDate = in1Week })
                    ModernCapsuleChip("In 1 Month", selectedDate == in1Month, { selectedDate = in1Month })
                    ModernCapsuleChip("Year End", selectedDate == endOfYear, { selectedDate = endOfYear })
                    ModernCapsuleChip("Custom Date...", false, { showDatePicker = true })
                }
            }

            // Quick Time Chips
            Column {
                Text(
                    text = "Time of Day",
                    fontFamily = FontFamily.Default,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeChip("Morning 9 AM", selectedTime.hour == 9, { selectedTime = LocalTime.of(9, 0) }, Modifier.weight(1f))
                    TimeChip("Noon 12 PM", selectedTime.hour == 12, { selectedTime = LocalTime.of(12, 0) }, Modifier.weight(1f))
                    TimeChip("Evening 6 PM", selectedTime.hour == 18, { selectedTime = LocalTime.of(18, 0) }, Modifier.weight(1f))
                    TimeChip("Midnight", selectedTime.hour == 0, { selectedTime = LocalTime.of(0, 0) }, Modifier.weight(1f))
                }
            }

            // Visual Category Selector
            val catScroll = rememberScrollState()
            Column {
                Text(
                    text = "Category Icon",
                    fontFamily = FontFamily.Default,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(catScroll),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { (name, icon, color) ->
                        val isSel = name == selectedCategory
                        CategoryPill(
                            name = name,
                            icon = icon,
                            color = color,
                            isSelected = isSel,
                            onClick = { selectedCategory = name }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Confirm Button
            ModernPillButton(
                text = "Save Countdown",
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, targetZdt, selectedCategory)
                    }
                },
                isPrimary = title.isNotBlank(),
                accentColor = NeonCyan,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        CyberDatePickerDialog(
            initialDate = selectedDate,
            onDateSelected = { selectedDate = it },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun TimeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else SpaceCard)
            .border(1.dp, if (isSelected) NeonCyan else SpaceCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Default,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) NeonCyan else TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CategoryPill(
    name: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) color.copy(alpha = 0.22f) else SpaceCard)
            .border(1.dp, if (isSelected) color else SpaceCardBorder, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) color else TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                fontFamily = FontFamily.Default,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) color else TextSecondary
            )
        }
    }
}
