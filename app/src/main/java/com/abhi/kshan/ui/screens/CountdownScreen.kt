package com.abhi.kshan.ui.screens

import android.util.Log
import android.view.HapticFeedbackConstants
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
import androidx.compose.material.icons.filled.EventRepeat
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
import com.abhi.kshan.data.ChronoEvent
import com.abhi.kshan.engine.CountdownStatus
import com.abhi.kshan.engine.TimeEngine
import com.abhi.kshan.ui.components.CelebrationFireworksOverlay
import com.abhi.kshan.ui.components.CyberDatePickerDialog
import com.abhi.kshan.ui.components.FrostedActionButton
import com.abhi.kshan.ui.components.FrostedGlassCard
import com.abhi.kshan.ui.components.HeroOrbitalTimepiece
import com.abhi.kshan.ui.components.ModernCapsuleChip
import com.abhi.kshan.ui.components.ModernPillButton
import com.abhi.kshan.ui.components.ModernToggle
import com.abhi.kshan.ui.components.resolveCelebrationTheme
import com.abhi.kshan.ui.theme.BrushedSilver
import com.abhi.kshan.ui.theme.CourierPrimeFontFamily
import com.abhi.kshan.ui.theme.IceCyan
import com.abhi.kshan.ui.theme.SpaceCard
import com.abhi.kshan.ui.theme.SpaceDark
import com.abhi.kshan.ui.theme.SpaceGroteskFontFamily
import com.abhi.kshan.ui.theme.TabularMonoFontFamily
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.ui.theme.TextSecondary
import com.abhi.kshan.ui.theme.TitaniumHairline
import com.abhi.kshan.ui.theme.WarmChampagne
import com.abhi.kshan.viewmodel.ChronoUiState
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
    onAddEvent: (String, ZonedDateTime, String, Boolean) -> Unit,
    onTriggerCelebration: () -> Unit,
    onHudInteraction: (String) -> Unit,
    onClearCelebration: () -> Unit = {},
    onSelectEvent: (ChronoEvent) -> Unit = {},
    onToggleCountUpMode: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddSheet by remember { mutableStateOf(false) }
    var showCelebrationCard by remember { mutableStateOf(false) }

    val activeTheme = remember(uiState.activeEventCategory, uiState.activeEventTitle) {
        resolveCelebrationTheme(uiState.activeEventCategory, uiState.activeEventTitle)
    }

    val isTodayCelebration = !uiState.isCountUpMode &&
            uiState.countdownTelemetry.status == CountdownStatus.TODAY_CELEBRATION

    val statusText = when {
        uiState.isCountUpMode -> "Counting up live (+)"
        isTodayCelebration -> "Milestone is Today ✦"
        uiState.isAutoRolled -> "Auto-rolled to ${uiState.effectiveCountdownTarget.year}"
        uiState.countdownTelemetry.status == CountdownStatus.ACTIVE -> "Ticking live"
        uiState.countdownTelemetry.status == CountdownStatus.TARGET_REACHED -> activeTheme.statusPillText
        uiState.countdownTelemetry.status == CountdownStatus.EVENT_PASSED -> "Event has passed"
        else -> "Waiting for date..."
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // 1. Unified Hero Orbital Timepiece Card (with Today ✦ state & past-only Days Since toggle)
                HeroOrbitalTimepiece(
                    days = uiState.countdownTelemetry.days,
                    hours = uiState.countdownTelemetry.hours,
                    minutes = uiState.countdownTelemetry.minutes,
                    seconds = uiState.countdownTelemetry.seconds,
                    categoryName = uiState.activeEventCategory,
                    eventTitle = uiState.activeEventTitle,
                    statusText = statusText,
                    accentColor = IceCyan,
                    isCountUpMode = uiState.isCountUpMode,
                    canUseCountUpMode = uiState.canUseCountUpMode,
                    isAutoRolled = uiState.isAutoRolled,
                    isTodayCelebration = isTodayCelebration,
                    targetYear = uiState.effectiveCountdownTarget.year,
                    onToggleCountUpMode = onToggleCountUpMode
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                        accentColor = IceCyan,
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
                        accentColor = WarmChampagne,
                        modifier = Modifier
                            .weight(0.9f)
                            .testTag("test_celebration_button")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Saved Countdowns Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saved Countdowns (${savedEvents.size})",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
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

            // Bottom Spacer for Floating Glass Dock
            item {
                Spacer(modifier = Modifier.height(110.dp))
            }
        }

        // Luxury Ballistic Celebration Fireworks Overlay with Interactive Sky Mode
        val remSecs = (uiState.countdownTelemetry.days * 86400L) +
                (uiState.countdownTelemetry.hours * 3600L) +
                (uiState.countdownTelemetry.minutes * 60L) +
                uiState.countdownTelemetry.seconds
        val remFormatted = when {
            uiState.isCountUpMode -> "+${uiState.countdownTelemetry.days}d ${uiState.countdownTelemetry.hours}h ${uiState.countdownTelemetry.minutes}m elapsed"
            isTodayCelebration -> "Today ✦"
            else -> "${uiState.countdownTelemetry.days}d ${uiState.countdownTelemetry.hours}h ${uiState.countdownTelemetry.minutes}m remaining"
        }

        CelebrationFireworksOverlay(
            visible = showCelebrationCard || uiState.triggerCelebration,
            eventTitle = uiState.activeEventTitle,
            category = uiState.activeEventCategory,
            remainingSeconds = if (uiState.countdownTelemetry.status == CountdownStatus.TARGET_REACHED || isTodayCelebration) 0L else remSecs,
            remainingTimeFormatted = remFormatted,
            powerSaverEnabled = uiState.powerSaverEnabled,
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
            onConfirm = { title, targetZdt, category, repeatsYearly ->
                onAddEvent(title, targetZdt, category, repeatsYearly)
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

    val cardScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.97f
            isSelected -> 1.01f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.68f, stiffness = 420f),
        label = "saved_card_scale"
    )

    val (dateText, daysLeftText, isTodayMilestone) = remember(
        event.targetIsoString,
        event.category,
        event.repeatsYearly
    ) {
        try {
            val nowZdt = ZonedDateTime.now()
            val rawZdt = ZonedDateTime.parse(event.targetIsoString)
            val isCountUp = TimeEngine.shouldDefaultToCountUp(
                now = nowZdt,
                rawTarget = rawZdt,
                category = event.category,
                repeatsYearly = event.repeatsYearly
            )
            val (effectiveZdt, autoRolled) = TimeEngine.resolveEffectiveTarget(
                now = nowZdt,
                raw = rawZdt,
                repeatsYearly = event.repeatsYearly,
                isCountUpMode = isCountUp
            )
            val formatted = if (autoRolled) {
                "${effectiveZdt.format(TimeEngine.FULL_DATE_FORMATTER)} (↻ Yearly)"
            } else {
                effectiveZdt.format(TimeEngine.FULL_DATE_FORMATTER)
            }
            val today = LocalDate.now()
            val isToday = !isCountUp && effectiveZdt.toLocalDate() == today
            val badgeStr = when {
                isToday -> "Today ✦"
                isCountUp -> {
                    val elapsedDays = ChronoUnit.DAYS.between(rawZdt.toLocalDate(), today).coerceAtLeast(0L)
                    if (elapsedDays == 0L) "+Today" else "+${elapsedDays}d since"
                }
                else -> {
                    val days = ChronoUnit.DAYS.between(today, effectiveZdt.toLocalDate()).coerceAtLeast(0L)
                    "${days}d left"
                }
            }
            Triple(formatted, badgeStr, isToday)
        } catch (e: Exception) {
            Log.w("CountdownScreen", "Failed to parse event targetIsoString: ${event.targetIsoString}", e)
            Triple(event.targetIsoString, "0d left", false)
        }
    }

    val activeBorderColor = if (isTodayMilestone) WarmChampagne else IceCyan

    FrostedGlassCard(
        borderColor = if (isSelected) activeBorderColor else TitaniumHairline,
        glowAlpha = if (isSelected) 0.65f else 0.2f,
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
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Subtle Titanium Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(0.8.dp, TitaniumHairline, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = cardTheme.icon,
                                contentDescription = null,
                                tint = if (isSelected) IceCyan else TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = event.category.uppercase(),
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) IceCyan else TextSecondary
                            )
                        }
                    }

                    // Tabular Remaining Days / Today ✦ Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isTodayMilestone) WarmChampagne.copy(alpha = 0.16f)
                                else if (isSelected) IceCyan.copy(alpha = 0.16f)
                                else SpaceDark
                            )
                            .border(
                                width = 0.8.dp,
                                color = when {
                                    isTodayMilestone -> WarmChampagne.copy(alpha = 0.55f)
                                    isSelected -> IceCyan.copy(alpha = 0.55f)
                                    else -> TitaniumHairline
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = daysLeftText,
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isTodayMilestone -> WarmChampagne
                                isSelected -> IceCyan
                                else -> TextPrimary
                            }
                        )
                    }

                    if (event.repeatsYearly) {
                        Text(
                            text = "↻ Yearly",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextMuted
                        )
                    }

                    if (event.isPinned) {
                        Text(
                            text = "• Pinned",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IceCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = event.title,
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
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
                    activeColor = IceCyan,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SurpriseAddCountdownSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, ZonedDateTime, String, Boolean) -> Unit
) {
    val view = LocalView.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now().plusDays(7)) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var selectedCategory by remember { mutableStateOf("Holiday") }
    var repeatsYearly by remember { mutableStateOf(true) }
    var showDatePicker by remember { mutableStateOf(false) }

    val categories = remember {
        listOf(
            Triple("Holiday", Icons.Default.BeachAccess, IceCyan),
            Triple("Birthday", Icons.Default.Cake, WarmChampagne),
            Triple("Anniversary", Icons.Default.Favorite, WarmChampagne),
            Triple("Celebration", Icons.Default.Celebration, BrushedSilver),
            Triple("Goal", Icons.Default.Flag, IceCyan),
            Triple("Vacation", Icons.Default.BeachAccess, IceCyan),
            Triple("Milestone", Icons.Default.Star, WarmChampagne),
            Triple("Deadline", Icons.Default.Alarm, BrushedSilver),
            Triple("Personal", Icons.Default.Favorite, BrushedSilver)
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
                    .background(TitaniumHairline)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Countdown",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "Cancel",
                    fontFamily = SpaceGroteskFontFamily,
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
                borderColor = IceCyan,
                glowAlpha = 0.6f,
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
                            text = selectedCategory.uppercase() + if (repeatsYearly) " • REPEATS YEARLY" else "",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IceCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (title.isBlank()) "Event Title Preview" else title,
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
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
                            color = IceCyan
                        )
                        Text(
                            text = "days left",
                            fontFamily = SpaceGroteskFontFamily,
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
                    focusedBorderColor = IceCyan,
                    unfocusedBorderColor = TitaniumHairline,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = IceCyan,
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
                    fontFamily = SpaceGroteskFontFamily,
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
                    fontFamily = SpaceGroteskFontFamily,
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
            Column {
                Text(
                    text = "Category",
                    fontFamily = SpaceGroteskFontFamily,
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
                            onClick = {
                                selectedCategory = name
                                repeatsYearly = TimeEngine.defaultRepeatsYearlyForCategory(name)
                            }
                        )
                    }
                }
            }

            // Repeats Yearly Toggle Row (defaults true for Birthday, Holiday, Anniversary)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SpaceCard)
                    .border(0.8.dp, TitaniumHairline, RoundedCornerShape(14.dp))
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        repeatsYearly = !repeatsYearly
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("add_event_repeats_yearly_row"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EventRepeat,
                        contentDescription = null,
                        tint = if (repeatsYearly) IceCyan else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Repeats Yearly",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Auto-rolls to next year after passing",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.5.sp,
                            color = TextMuted
                        )
                    }
                }

                ModernToggle(
                    checked = repeatsYearly,
                    onCheckedChange = { repeatsYearly = it },
                    testTag = "add_event_repeats_yearly_toggle"
                )
            }

            // Confirm Button
            ModernPillButton(
                text = "Save Countdown",
                onClick = {
                    val finalTitle = title.ifBlank { "Special $selectedCategory" }
                    onConfirm(finalTitle, targetZdt, selectedCategory, repeatsYearly)
                },
                isPrimary = true,
                accentColor = IceCyan,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirm_add_event_button")
            )

            Spacer(modifier = Modifier.height(14.dp))
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
        targetValue = if (isSelected) 1.03f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "time_chip_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) IceCyan.copy(alpha = 0.16f) else SpaceCard)
            .border(
                0.8.dp,
                if (isSelected) IceCyan.copy(alpha = 0.65f) else TitaniumHairline,
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
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) IceCyan else TextSecondary,
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
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.04f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "cat_pill_scale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) color.copy(alpha = 0.16f) else SpaceCard)
            .border(
                width = 0.8.dp,
                color = if (isSelected) color.copy(alpha = 0.65f) else TitaniumHairline,
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
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) color else TextSecondary
            )
        }
    }
}
