package com.abhi.kshan.ui.screens

import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhi.kshan.engine.TimeEngine
import com.abhi.kshan.ui.components.ConnectedDateCards
import com.abhi.kshan.ui.components.CyberDatePickerDialog
import com.abhi.kshan.ui.components.FrostedGlassCard
import com.abhi.kshan.ui.components.MechanicalScrubRuler
import com.abhi.kshan.ui.components.ModernCapsuleChip
import com.abhi.kshan.ui.components.ModernPillButton
import com.abhi.kshan.ui.components.ModernToggle
import com.abhi.kshan.ui.components.TemporalConstellationHero
import com.abhi.kshan.ui.theme.ComicNeueFontFamily
import com.abhi.kshan.ui.theme.CourierPrimeFontFamily
import com.abhi.kshan.ui.theme.NeonCyan
import com.abhi.kshan.ui.theme.NeonPink
import com.abhi.kshan.ui.theme.NeonPurple
import com.abhi.kshan.ui.theme.SpaceCard
import com.abhi.kshan.ui.theme.SpaceCardBorder
import com.abhi.kshan.ui.theme.SpaceDark
import com.abhi.kshan.ui.theme.TabularMonoFontFamily
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.ui.theme.TextSecondary
import com.abhi.kshan.viewmodel.ChronoUiState
import java.time.LocalDate

@Composable
fun DateMathScreen(
    uiState: ChronoUiState,
    onBaseChanged: (LocalDate) -> Unit,
    onDaysChanged: (Long) -> Unit,
    onOperationChanged: (Boolean) -> Unit,
    onExcludeWeekendsChanged: (Boolean) -> Unit,
    onHudInteraction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var showBasePicker by remember { mutableStateOf(false) }
    var showDaysInputDialog by remember { mutableStateOf(false) }
    // 0 = Scrub Base Date, 1 = Scrub Days to Shift (defaults to Days to Shift)
    var scrubTargetIndex by remember { mutableIntStateOf(1) }
    val scrollState = rememberScrollState()

    val presets = remember {
        listOf(
            Pair("+1 Week", 7L),
            Pair("+2 Weeks", 14L),
            Pair("+1 Month (30d)", 30L),
            Pair("+3 Months (90d)", 90L),
            Pair("+1 Year (365d)", 365L)
        )
    }

    val activeAccent = if (uiState.mathIsAdd) NeonCyan else NeonPink
    val scrubAccent = if (scrubTargetIndex == 0) NeonPurple else activeAccent
    val scrubReadout = if (scrubTargetIndex == 0) {
        uiState.mathBase.format(TimeEngine.SHORT_DATE_FORMATTER)
    } else {
        "${if (uiState.mathIsAdd) "+" else "−"}${uiState.mathDays} days"
    }

    fun hapticTick() {
        try {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        } catch (e: Exception) { Log.w("DateMathScreen", "Haptic feedback unavailable", e) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Connected Starting Date & Days to Shift with IntrinsicSize.Max equal heights
        Box(modifier = Modifier.padding(horizontal = 22.dp)) {
            ConnectedDateCards(
                modifier = Modifier.height(IntrinsicSize.Max),
                connectorLabel = if (uiState.mathIsAdd) "+" else "−",
                accentColor = activeAccent,
                startContent = {
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clickable {
                                hapticTick()
                                scrubTargetIndex = 0
                                onHudInteraction("Tap to select your base starting date.")
                                showBasePicker = true
                            }
                            .testTag("math_base_button"),
                        borderColor = NeonPurple,
                        glowAlpha = if (scrubTargetIndex == 0) 0.65f else 0.4f,
                        cornerRadius = 24.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(NeonPurple.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = NeonPurple,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Base Date",
                                    fontFamily = ComicNeueFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeonPurple
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Column {
                                Text(
                                    text = uiState.mathBase.format(TimeEngine.SHORT_DATE_FORMATTER),
                                    fontFamily = CourierPrimeFontFamily,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = uiState.mathBase.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontFamily = ComicNeueFontFamily,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                },
                endContent = {
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        borderColor = activeAccent,
                        glowAlpha = if (scrubTargetIndex == 1) 0.65f else 0.4f,
                        cornerRadius = 24.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Days to Shift",
                                    fontFamily = ComicNeueFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = activeAccent
                                )
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Type custom days",
                                    tint = activeAccent.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable {
                                            hapticTick()
                                            scrubTargetIndex = 1
                                            showDaysInputDialog = true
                                        }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Tappable Typewriter Number Pill to open Direct Number Input Dialog
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(activeAccent.copy(alpha = 0.12f))
                                        .border(1.dp, activeAccent.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                        .clickable {
                                            hapticTick()
                                            scrubTargetIndex = 1
                                            onHudInteraction("Enter custom days to shift.")
                                            showDaysInputDialog = true
                                        }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${uiState.mathDays}",
                                        fontFamily = CourierPrimeFontFamily,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        modifier = Modifier.testTag("math_days_value")
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(SpaceCard)
                                            .clickable {
                                                hapticTick()
                                                if (uiState.mathDays > 0) onDaysChanged(uiState.mathDays - 1)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Minus",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(SpaceCard)
                                            .clickable {
                                                hapticTick()
                                                onDaysChanged(uiState.mathDays + 1)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Plus",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Tactile Mechanical Time Wheel / Scrubbing Ruler
        Box(modifier = Modifier.padding(horizontal = 22.dp)) {
            MechanicalScrubRuler(
                targetOptions = listOf("Base Date", "Days to Shift"),
                selectedTargetIndex = scrubTargetIndex,
                onTargetSelected = { idx ->
                    scrubTargetIndex = idx
                    onHudInteraction(
                        if (idx == 0) "Crown Scrubber focused on Base Date."
                        else "Crown Scrubber focused on Days to Shift."
                    )
                },
                activeValueReadout = scrubReadout,
                accentColor = scrubAccent,
                onStepDelta = { deltaSteps ->
                    if (scrubTargetIndex == 0) {
                        onBaseChanged(uiState.mathBase.plusDays(deltaSteps.toLong()))
                    } else {
                        val nextDays = (uiState.mathDays + deltaSteps).coerceIn(0L, 9999L)
                        onDaysChanged(nextDays)
                    }
                },
                testTag = "math_mechanical_ruler"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Preset Chips with Edge-to-Edge scrolling (PaddingValues horizontal = 24.dp)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presets) { (label, days) ->
                ModernCapsuleChip(
                    label = label,
                    isSelected = uiState.mathDays == days,
                    onClick = { onDaysChanged(days) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Directional Shift Buttons: Earlier (-) vs Later (+)
        Box(modifier = Modifier.padding(horizontal = 22.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ModernPillButton(
                    text = "Earlier (−)",
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = { onOperationChanged(false) },
                    isPrimary = !uiState.mathIsAdd,
                    accentColor = NeonPink,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("math_btn_sub")
                )

                ModernPillButton(
                    text = "Later (+)",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = { onOperationChanged(true) },
                    isPrimary = uiState.mathIsAdd,
                    accentColor = NeonCyan,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("math_btn_add")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Skip Weekends Switch Card
        Box(modifier = Modifier.padding(horizontal = 22.dp)) {
            FrostedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        hapticTick()
                        onExcludeWeekendsChanged(!uiState.mathExcludeWeekends)
                    }
                    .testTag("math_weekends_toggle_row"),
                borderColor = if (uiState.mathExcludeWeekends) NeonCyan else SpaceCardBorder,
                glowAlpha = if (uiState.mathExcludeWeekends) 0.45f else 0.2f,
                cornerRadius = 20.dp
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
                                .background(if (uiState.mathExcludeWeekends) NeonCyan.copy(alpha = 0.2f) else SpaceCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkHistory,
                                contentDescription = null,
                                tint = if (uiState.mathExcludeWeekends) NeonCyan else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Skip Weekends",
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Counts working days only",
                                fontFamily = ComicNeueFontFamily,
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }

                    ModernToggle(
                        checked = uiState.mathExcludeWeekends,
                        onCheckedChange = { onExcludeWeekendsChanged(it) },
                        testTag = "math_weekends_toggle"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 6. Temporal Constellation Hero (Visual Timeline Arc + Result Date + 3-Card Human Rhythm Carousel)
        TemporalConstellationHero(
            startDate = uiState.mathBase,
            endDate = uiState.mathArrivalDate,
            daysCount = uiState.mathDays,
            accentColor = activeAccent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    activeAccent.copy(alpha = 0.16f),
                                    activeAccent.copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "RESULT DATE",
                        fontFamily = ComicNeueFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    AnimatedContent(
                        targetState = uiState.mathArrivalDate,
                        transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                        label = "arrival_anim"
                    ) { arrivalDate ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = arrivalDate.format(TimeEngine.FULL_DATE_FORMATTER),
                                fontFamily = CourierPrimeFontFamily,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("math_calculated_arrival")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            val shiftDesc = if (uiState.mathExcludeWeekends) {
                                "${uiState.mathDays} working days ${if (uiState.mathIsAdd) "later" else "earlier"}"
                            } else {
                                "${uiState.mathDays} days ${if (uiState.mathIsAdd) "later" else "earlier"}"
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFF0F172A).copy(alpha = 0.9f))
                                    .border(
                                        1.dp,
                                        Brush.horizontalGradient(
                                            listOf(activeAccent.copy(0.5f), Color.White.copy(0.2f), activeAccent.copy(0.4f))
                                        ),
                                        RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = shiftDesc,
                                    fontFamily = ComicNeueFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = activeAccent
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. 120.dp Bottom Spacer to prevent navigation dock collision
        Spacer(modifier = Modifier.height(120.dp))
    }

    if (showBasePicker) {
        CyberDatePickerDialog(
            initialDate = uiState.mathBase,
            onDateSelected = onBaseChanged,
            onDismiss = { showBasePicker = false }
        )
    }

    if (showDaysInputDialog) {
        TypewriterNumberInputDialog(
            initialDays = uiState.mathDays,
            accentColor = activeAccent,
            onConfirm = { newDays ->
                onDaysChanged(newDays)
                showDaysInputDialog = false
            },
            onDismiss = { showDaysInputDialog = false }
        )
    }
}

@Composable
private fun TypewriterNumberInputDialog(
    initialDays: Long,
    accentColor: Color,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    var textValue by remember { mutableStateOf(initialDays.toString()) }
    val quickValues = remember { listOf(45L, 100L, 180L, 365L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SpaceDark,
        shape = RoundedCornerShape(26.dp),
        title = {
            Text(
                text = "Enter Days to Shift",
                fontFamily = ComicNeueFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Type any number of days (0 – 9999):",
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = textValue,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }.take(4)
                        textValue = filtered
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val parsed = textValue.toLongOrNull()?.coerceIn(0L, 9999L) ?: initialDays
                            onConfirm(parsed)
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = SpaceCardBorder,
                        focusedTextColor = accentColor,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("math_direct_days_input")
                )

                // Quick Typewriter Jumps inside dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickValues.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SpaceCard)
                                .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .clickable {
                                    try {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    } catch (e: Exception) { Log.w("DateMathScreen", "Haptic feedback unavailable", e) }
                                    textValue = preset.toString()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${preset}d",
                                fontFamily = CourierPrimeFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    } catch (e: Exception) { Log.w("DateMathScreen", "Haptic feedback unavailable", e) }
                    val parsed = textValue.toLongOrNull()?.coerceIn(0L, 9999L) ?: initialDays
                    onConfirm(parsed)
                }
            ) {
                Text(
                    text = "Apply",
                    fontFamily = ComicNeueFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    } catch (e: Exception) { Log.w("DateMathScreen", "Haptic feedback unavailable", e) }
                    onDismiss()
                }
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = ComicNeueFontFamily,
                    color = TextMuted
                )
            }
        }
    )
}
