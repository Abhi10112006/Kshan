package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChronoEvent
import com.example.engine.CountdownStatus
import com.example.engine.TimeEngine
import com.example.ui.components.CelebrationFireworksOverlay
import com.example.ui.components.CyberDatePickerDialog
import com.example.ui.components.FrostedActionButton
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.HeroOrbitalTimepiece
import com.example.ui.components.ModernCapsuleChip
import com.example.ui.components.ModernPillButton
import com.example.ui.components.resolveCelebrationTheme
import com.example.ui.theme.ComicNeueFontFamily
import com.example.ui.theme.CourierPrimeFontFamily
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.TabularMonoFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ChronoUiState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

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

    val statusText = when (uiState.countdownTelemetry.status) {
        CountdownStatus.ACTIVE -> "Ticking live"
        CountdownStatus.TARGET_REACHED -> activeTheme.statusPillText
        CountdownStatus.EVENT_PASSED -> "Event has passed"
        CountdownStatus.AWAITING -> "Waiting for date..."
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

                // 1. Unified Hero Orbital Timepiece Card (with animated progress arc & pulsing colons)
                HeroOrbitalTimepiece(
                    days = uiState.countdownTelemetry.days,
                    hours = uiState.countdownTelemetry.hours,
                    minutes = uiState.countdownTelemetry.minutes,
                    seconds = uiState.countdownTelemetry.seconds,
                    categoryName = uiState.activeEventCategory,
                    eventTitle = uiState.activeEventTitle,
                    statusText = statusText,
                    accentColor = activeTheme.primaryColor
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons: + Add Countdown & Fireworks
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
                        fontFamily = ComicNeueFontFamily,
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
                    isSelected = event.title == uiState.activeEventTitle,
                    onPin = { onPinEvent(event) },
                    onDelete = { onDeleteEvent(event) },
                    onSelect = { onSelectEvent(event) },
                    onHudInteraction = onHudInteraction
                )
            }

            // 120.dp Bottom Spacer to prevent navigation dock collision
            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }

        // Luxury Ballistic Celebration Fireworks Overlay with Interactive Sky Mode
        val remSecs = (uiState.countdownTelemetry.days * 86400L) +
                (uiState.countdownTelemetry.hours * 3600L) +
                (uiState.countdownTelemetry.minutes * 60L) +
                uiState.countdownTelemetry.seconds
        val remFormatted = "${uiState.countdownTelemetry.days}d ${uiState.countdownTelemetry.hours}h ${uiState.countdownTelemetry.minutes}m remaining"

        CelebrationFireworksOverlay(
            visible = showCelebrationCard || uiState.triggerCelebration,
            eventTitle = uiState.activeEventTitle,
            category = uiState.activeEventCategory,
            remainingSeconds = if (uiState.countdownTelemetry.status == CountdownStatus.TARGET_REACHED) 0L else remSecs,
            remainingTimeFormatted = remFormatted,
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
private fun SavedCountdownCard(
    event: ChronoEvent,
    isSelected: Boolean,
    onPin: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit = {},
    onHudInteraction: (String) -> Unit
) {
    val view = LocalView.current
    val cardTheme = resolveCelebrationTheme(event.category, event.title)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "card_glow_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val cardScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.96f
            isSelected -> 1.02f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 380f),
        label = "saved_card_scale"
    )

    val badgeScale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f),
        label = "badge_scale"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> cardTheme.primaryColor
            event.isPinned -> NeonCyan
            else -> SpaceCardBorder
        },
        animationSpec = tween(250),
        label = "card_border_color"
    )

    val (dateText, daysLeftText) = remember(event.targetIsoString) {
        try {
            val zdt = ZonedDateTime.parse(event.targetIsoString)
            val formatted = zdt.format(TimeEngine.FULL_DATE_FORMATTER)
            val days = ChronoUnit.DAYS.between(LocalDate.now(), zdt.toLocalDate()).coerceAtLeast(0L)
            val badgeStr = if (days == 0L) "Today!" else "${days}d left"
            Pair(formatted, badgeStr)
        } catch (e: Exception) {
            Pair(event.targetIsoString, "0d left")
        }
    }

    FrostedGlassCard(
        borderColor = borderColor,
        glowAlpha = when {
            isSelected -> pulseAlpha
            event.isPinned -> 0.55f
            else -> 0.2f
        },
        cornerRadius = 20.dp,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onHudInteraction("Active countdown set to '${event.title}'")
                onSelect()
            }
            .testTag("event_card_${event.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                cardTheme.primaryColor.copy(alpha = 0.12f * pulseAlpha),
                                Color.Transparent
                            )
                        )
                    } else {
                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    },
                    shape = RoundedCornerShape(14.dp)
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Animated Category Badge with Pulsing Glow & Spring Scale
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = badgeScale
                                scaleY = badgeScale
                            }
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                cardTheme.primaryColor.copy(
                                    alpha = if (isSelected) 0.26f * pulseAlpha + 0.1f else 0.15f
                                )
                            )
                            .border(
                                width = if (isSelected) 1.3.dp else 1.dp,
                                color = cardTheme.primaryColor.copy(
                                    alpha = if (isSelected) pulseAlpha.coerceIn(0.6f, 1f) else 0.5f
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
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
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = cardTheme.primaryColor
                            )
                        }
                    }

                    // Bold Typewriter Remaining Days Badge ("45d left")
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = badgeScale
                                scaleY = badgeScale
                            }
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) cardTheme.primaryColor.copy(alpha = 0.22f)
                                else SpaceDark.copy(alpha = 0.85f)
                            )
                            .border(
                                width = 1.dp,
                                color = cardTheme.primaryColor.copy(alpha = if (isSelected) 0.85f else 0.45f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = daysLeftText,
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else cardTheme.primaryColor
                        )
                    }

                    if (event.isPinned) {
                        Text(
                            text = "• Pinned",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = event.title,
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = dateText,
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // Subtle Frosted Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FrostedActionButton(
                    icon = Icons.Default.PushPin,
                    contentDescription = "Pin Countdown",
                    isActive = event.isPinned,
                    activeColor = NeonCyan,
                    onClick = {
                        onHudInteraction("'${event.title}' pinned to your home screen widget.")
                        onPin()
                    },
                    testTag = "pin_event_${event.id}"
                )

                FrostedActionButton(
                    icon = Icons.Default.Delete,
                    contentDescription = "Delete Countdown",
                    isActive = false,
                    onClick = onDelete,
                    testTag = "delete_event_${event.id}"
                )
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
    val view = LocalView.current
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
        ChronoUnit.DAYS.between(nowZdt.toLocalDate(), selectedDate).coerceAtLeast(0)
    }

    val today = LocalDate.now()
    val presets = remember(today) {
        listOf(
            Pair("Tomorrow", today.plusDays(1)),
            Pair("This Weekend", today.plusDays(((DayOfWeek.SATURDAY.value - today.dayOfWeek.value + 7) % 7).toLong().let { if (it == 0L) 7L else it })),
            Pair("In 1 Week", today.plusWeeks(1)),
            Pair("In 1 Month", today.plusMonths(1)),
            Pair("Year End", LocalDate.of(today.year, 12, 31))
        )
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
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Cancel",
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 14.sp,
                    color = TextMuted,
                    modifier = Modifier.clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onDismiss()
                    }
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
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (title.isBlank()) "Event Title Preview" else title,
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (title.isBlank()) TextMuted else TextPrimary
                        )
                        Text(
                            text = "${selectedDate.format(TimeEngine.SHORT_DATE_FORMATTER)} at ${selectedTime.format(TimeEngine.TIME_FORMATTER)}",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$liveDays",
                            fontFamily = TabularMonoFontFamily,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Text(
                            text = "days left",
                            fontFamily = ComicNeueFontFamily,
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
            Column {
                Text(
                    text = "Quick Date Presets",
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presets) { (label, presetDate) ->
                        ModernCapsuleChip(
                            label = label,
                            isSelected = selectedDate == presetDate,
                            onClick = { selectedDate = presetDate }
                        )
                    }
                    item {
                        ModernCapsuleChip(
                            label = "Custom Date...",
                            isSelected = false,
                            onClick = { showDatePicker = true }
                        )
                    }
                }
            }

            // Quick Time Chips
            Column {
                Text(
                    text = "Time of Day",
                    fontFamily = ComicNeueFontFamily,
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

            // Visual Category Selector (LazyRow) with Pulsing Glow & Spring Scale
            Column {
                Text(
                    text = "Category Icon",
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { (name, icon, color) ->
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
                    val finalTitle = title.ifBlank { "Special $selectedCategory" }
                    onConfirm(finalTitle, targetZdt, selectedCategory)
                },
                isPrimary = true,
                accentColor = NeonCyan,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirm_add_event_button")
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        CyberDatePickerDialog(
            initialDate = selectedDate,
            onDateSelected = {
                selectedDate = it
                showDatePicker = false
            },
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
    val view = LocalView.current
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.04f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "time_chip_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else SpaceCard)
            .border(
                1.dp,
                if (isSelected) NeonCyan else SpaceCardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onClick()
            }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = ComicNeueFontFamily,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
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
    val view = LocalView.current
    val infiniteTransition = rememberInfiniteTransition(label = "category_pill_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cat_pulse_alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.07f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f),
        label = "cat_pill_scale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) color.copy(alpha = pulseAlpha) else SpaceCard)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) color else SpaceCardBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 9.dp)
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
                fontFamily = ComicNeueFontFamily,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) color else TextSecondary
            )
        }
    }
}
