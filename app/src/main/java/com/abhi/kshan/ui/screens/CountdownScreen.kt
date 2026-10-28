package com.abhi.kshan.ui.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abhi.kshan.data.ChronoEvent
import com.abhi.kshan.engine.CountdownStatus
import com.abhi.kshan.engine.TimeEngine
import com.abhi.kshan.ui.components.CyberDatePickerDialog
import com.abhi.kshan.ui.components.FrostedGlassCard
import com.abhi.kshan.ui.components.HeroOrbitalTimepiece
import com.abhi.kshan.ui.components.ModernCapsuleChip
import com.abhi.kshan.ui.components.ModernPillButton
import com.abhi.kshan.ui.theme.CardTitleTextStyle
import com.abhi.kshan.ui.theme.CategoryBirthdayDot
import com.abhi.kshan.ui.theme.CategoryGoalDot
import com.abhi.kshan.ui.theme.CategoryHolidayDot
import com.abhi.kshan.ui.theme.CategoryKshanDot
import com.abhi.kshan.ui.theme.JetBrainsMonoFontFamily
import com.abhi.kshan.ui.theme.KshanBorder
import com.abhi.kshan.ui.theme.KshanCardShape
import com.abhi.kshan.ui.theme.KshanCyan
import com.abhi.kshan.ui.theme.KshanPillShape
import com.abhi.kshan.ui.theme.KshanSurface
import com.abhi.kshan.ui.theme.KshanUnpinnedBorder
import com.abhi.kshan.ui.theme.MetricLabelTextStyle
import com.abhi.kshan.ui.theme.SpaceGroteskFontFamily
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.ui.theme.TextSecondary
import com.abhi.kshan.viewmodel.ChronoUiState
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownScreen(
    uiState: ChronoUiState,
    savedEvents: List<ChronoEvent>,
    onPinEvent: (ChronoEvent) -> Unit,
    onDeleteEvent: (ChronoEvent) -> Unit,
    onAddEvent: (String, ZonedDateTime, String, Boolean) -> Unit,
    onTriggerCelebration: () -> Unit = {},
    onClearCelebration: () -> Unit = {},
    onSelectEvent: (ChronoEvent) -> Unit,
    onToggleCountUpMode: (Boolean) -> Unit,
    onHudInteraction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    if (showAddDialog) {
        AddCountdownDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, category, date, repeatsYearly ->
                val targetZdt = date.atTime(LocalTime.of(9, 0)).atZone(ZoneId.systemDefault())
                onAddEvent(title, targetZdt, category, repeatsYearly)
                onHudInteraction("Added Kshan: $title")
                showAddDialog = false
            }
        )
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy") }
    val tele = uiState.countdownTelemetry
    val statusText = remember(uiState.effectiveCountdownTarget, uiState.isCountUpMode) {
        uiState.effectiveCountdownTarget.format(dateFormatter)
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Orbital Timepiece with Real-Life Context Pill
            item {
                Box(modifier = Modifier.widthIn(max = 600.dp)) {
                    HeroOrbitalTimepiece(
                        days = tele.days,
                        hours = tele.hours,
                        minutes = tele.minutes,
                        seconds = tele.seconds,
                        categoryName = uiState.activeEventCategory,
                        eventTitle = uiState.activeEventTitle,
                        statusText = statusText,
                        isCountUpMode = uiState.isCountUpMode,
                        canUseCountUpMode = uiState.canUseCountUpMode,
                        isAutoRolled = uiState.isAutoRolled,
                        isTodayCelebration = tele.status == CountdownStatus.TODAY_CELEBRATION,
                        targetYear = uiState.effectiveCountdownTarget.year,
                        contextLine = uiState.heroContextLine,
                        onToggleCountUpMode = { countUp ->
                            onToggleCountUpMode(countUp)
                        }
                    )
                }
            }

            // 2. Section Header + New Kshan Full-Pill Action
            item {
                Row(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "SAVED KSHANS",
                            style = MetricLabelTextStyle
                        )
                        Text(
                            text = "Swipe left or long-press any row to delete",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    ModernPillButton(
                        text = "New Kshan",
                        icon = Icons.Default.Add,
                        onClick = { showAddDialog = true },
                        isPrimary = true,
                        testTag = "add_countdown_button"
                    )
                }
            }

            // 3. Saved Kshans List (Swipe-to-delete + long-press menu fallback)
            items(savedEvents, key = { it.id }) { event ->
                val isSelected = event.title == uiState.activeEventTitle &&
                        event.category == uiState.activeEventCategory
                Box(modifier = Modifier.widthIn(max = 600.dp)) {
                    SwipeableSavedCountdownCard(
                        event = event,
                        isSelected = isSelected,
                        onClick = {
                            onSelectEvent(event)
                            onHudInteraction("Inspecting ${event.title}")
                        },
                        onPin = {
                            onPinEvent(event)
                            onHudInteraction("Pinned ${event.title} to Hero & Widget")
                        },
                        onDelete = {
                            onDeleteEvent(event)
                            onHudInteraction("Removed ${event.title}")
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun SwipeableSavedCountdownCard(
    event: ChronoEvent,
    isSelected: Boolean,
    onClick: () -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit
) {
    var showContextMenu by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(KshanCardShape)
                    .background(Color(0xFF2B1219))
                    .border(1.dp, Color(0xFFF43F5E).copy(alpha = 0.45f), KshanCardShape)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Kshan",
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "DELETE",
                        style = MetricLabelTextStyle.copy(color = Color(0xFFF43F5E))
                    )
                }
            }
        }
    ) {
        SavedCountdownCard(
            event = event,
            isSelected = isSelected,
            showContextMenu = showContextMenu,
            onDismissContextMenu = { showContextMenu = false },
            onLongClick = { showContextMenu = true },
            onClick = onClick,
            onPin = onPin,
            onDelete = onDelete
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SavedCountdownCard(
    event: ChronoEvent,
    isSelected: Boolean,
    showContextMenu: Boolean,
    onDismissContextMenu: () -> Unit,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit
) {
    val now = ZonedDateTime.now()
    val rawZdt = remember(event.targetIsoString) {
        try {
            ZonedDateTime.parse(event.targetIsoString)
        } catch (e: Exception) {
            Log.w("SavedCountdownCard", "Fallback parse for ${event.targetIsoString}", e)
            ZonedDateTime.now().plusDays(1)
        }
    }
    val rowCountUp = TimeEngine.shouldDefaultToCountUp(
        now = now,
        rawTarget = rawZdt,
        category = event.category,
        repeatsYearly = event.repeatsYearly
    )

    val resolvedPair = TimeEngine.resolveEffectiveTarget(
        now = now,
        raw = rawZdt,
        repeatsYearly = event.repeatsYearly,
        isCountUpMode = rowCountUp
    )
    val effectiveZdt = resolvedPair.first
    val rolledOver = resolvedPair.second

    val tick = if (rowCountUp) {
        TimeEngine.calculateElapsedSince(now, rawZdt)
    } else {
        TimeEngine.calculateCountdown(now, effectiveZdt)
    }
    val shortFormat = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }

    // Colored left category dot per design spec:
    // GOAL = #2ABFFF, BIRTHDAY = #F472B6, HOLIDAY = #E6C87A, KSHAN = #10B981
    val categoryDotColor = when (event.category.lowercase()) {
        "birthday" -> CategoryBirthdayDot
        "holiday", "anniversary" -> CategoryHolidayDot
        "kshan" -> CategoryKshanDot
        else -> CategoryGoalDot
    }

    val isHighlighted = event.isPinned || isSelected

    Box {
        FrostedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .testTag("countdown_card_${event.id}"),
            useCustomBorder = true,
            borderColor = if (isHighlighted) KshanCyan else KshanUnpinnedBorder,
            surfaceAlpha = if (isHighlighted) 1.0f else 0.60f
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Colored Category Status Dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(KshanPillShape)
                                .background(categoryDotColor)
                        )

                        Text(
                            text = event.category.uppercase(),
                            style = MetricLabelTextStyle
                        )

                        if (rolledOver) {
                            Text(
                                text = "• NEXT YEAR",
                                style = MetricLabelTextStyle.copy(color = KshanCyan)
                            )
                        } else if (rowCountUp) {
                            Text(
                                text = "• STREAK (+)",
                                style = MetricLabelTextStyle
                            )
                        }
                    }

                    Text(
                        text = event.title,
                        style = CardTitleTextStyle,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = effectiveZdt.format(shortFormat),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontFeatureSettings = "tnum"
                        ),
                        color = TextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Right-side Day Readout Pill
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .clip(KshanPillShape)
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(1.dp, KshanBorder, KshanPillShape)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        val signPrefix = if (rowCountUp) "+" else ""
                        Text(
                            text = "$signPrefix${tick.days}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontFeatureSettings = "tnum",
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = if (rowCountUp) "DAYS SINCE" else "DAYS LEFT",
                            style = MetricLabelTextStyle
                        )
                    }

                    // Pin Icon Button ONLY (Trash icon removed per strict visual declutter)
                    IconButton(
                        onClick = onPin,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("pin_event_${event.id}")
                    ) {
                        Icon(
                            imageVector = if (event.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Kshan",
                            tint = if (event.isPinned) KshanCyan else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Long-press fallback DropdownMenu for Delete
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = onDismissContextMenu,
            modifier = Modifier.background(KshanSurface)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Delete Kshan",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFFF43F5E)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    onDismissContextMenu()
                    onDelete()
                },
                modifier = Modifier.testTag("delete_event_${event.id}")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCountdownDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, LocalDate, Boolean) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Goal") }
    var repeatsYearly by rememberSaveable { mutableStateOf(false) }
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now().plusDays(30)) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showError by rememberSaveable { mutableStateOf(false) }

    val categories = listOf("Goal", "Birthday", "Holiday", "Milestone", "Kshan")
    val formatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }

    if (showDatePicker) {
        CyberDatePickerDialog(
            initialDate = selectedDate,
            onDateSelected = { picked -> selectedDate = picked },
            onDismiss = { showDatePicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = KshanSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Kshan Milestone",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = SpaceGroteskFontFamily
                    ),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = TextSecondary
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        showError = false
                    },
                    label = { Text("Kshan Title (e.g., Launch Day)") },
                    isError = showError,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KshanCyan,
                        unfocusedBorderColor = KshanBorder,
                        focusedLabelColor = KshanCyan,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_event_title_input")
                )

                Text(
                    text = "CATEGORY",
                    style = MetricLabelTextStyle
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        ModernCapsuleChip(
                            label = cat,
                            isSelected = selectedCategory == cat,
                            onClick = {
                                selectedCategory = cat
                                repeatsYearly = TimeEngine.defaultRepeatsYearlyForCategory(cat)
                            },
                            testTag = "cat_chip_$cat"
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(KshanPillShape)
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(1.dp, KshanBorder, KshanPillShape)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Repeat Yearly",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "Auto-rollover annually",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Switch(
                        checked = repeatsYearly,
                        onCheckedChange = { repeatsYearly = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF070A12),
                            checkedTrackColor = KshanCyan,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = KshanSurface
                        ),
                        modifier = Modifier.testTag("new_event_repeats_yearly_switch")
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(KshanPillShape)
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(1.dp, KshanBorder, KshanPillShape)
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("new_event_date_picker_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TARGET DATE",
                            style = MetricLabelTextStyle
                        )
                        Text(
                            text = selectedDate.format(formatter),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontFeatureSettings = "tnum"
                            ),
                            color = KshanCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            ModernPillButton(
                text = "Save Kshan",
                onClick = {
                    if (title.isBlank()) {
                        showError = true
                    } else {
                        onConfirm(title.trim(), selectedCategory, selectedDate, repeatsYearly)
                    }
                },
                isPrimary = true,
                testTag = "save_new_event_button"
            )
        }
    )
}
