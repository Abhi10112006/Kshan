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
import com.abhi.kshan.ui.theme.CourierPrimeFontFamily
import com.abhi.kshan.ui.theme.IceCyan
import com.abhi.kshan.ui.theme.ObsidianGlass
import com.abhi.kshan.ui.theme.SpaceCard
import com.abhi.kshan.ui.theme.SpaceDark
import com.abhi.kshan.ui.theme.SpaceGroteskFontFamily
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.ui.theme.TextSecondary
import com.abhi.kshan.ui.theme.TitaniumHairline
import com.abhi.kshan.ui.theme.WarmChampagne
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

    val activeAccent = if (uiState.mathIsAdd) IceCyan else WarmChampagne
    val scrubReadout = if (scrubTargetIndex == 0) {
        uiState.mathBase.format(TimeEngine.SHORT_DATE_FORMATTER)
    } else {
        "${if (uiState.mathIsAdd) "+" else "−"}${uiState.mathDays} days"
    }

    fun hapticTick() {
        try {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        } catch (e: Exception) {
            Log.w("DateMathScreen", "Haptic feedback unavailable", e)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Connected Base Date & Days to Shift Cards (Only active scrub target gets subtle Ice Cyan border)
        Box(modifier = Modifier.padding(horizontal = 22.dp)) {
            ConnectedDateCards(
                modifier = Modifier.height(IntrinsicSize.Max),
                connectorLabel = if (uiState.mathIsAdd) "+" else "−",
                accentColor = activeAccent,
                startContent = {
                    val isBaseSelected = scrubTargetIndex == 0
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
                        borderColor = if (isBaseSelected) IceCyan else TitaniumHairline,
                        glowAlpha = if (isBaseSelected) 0.65f else 0.2f,
                        cornerRadius = 22.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isBaseSelected) IceCyan.copy(alpha = 0.14f)
                                            else SpaceCard
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = if (isBaseSelected) IceCyan else TextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Base Date",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isBaseSelected) IceCyan else TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

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
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                },
                endContent = {
                    val isShiftSelected = scrubTargetIndex == 1
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        borderColor = if (isShiftSelected) IceCyan else TitaniumHairline,
                        glowAlpha = if (isShiftSelected) 0.65f else 0.2f,
                        cornerRadius = 22.dp
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
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isShiftSelected) IceCyan else TextSecondary
                                )
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Type custom days",
                                    tint = TextSecondary,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable {
                                            hapticTick()
                                            scrubTargetIndex = 1
                                            showDaysInputDialog = true
                                        }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Tappable Typewriter Number Pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .border(0.8.dp, TitaniumHairline, RoundedCornerShape(10.dp))
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
                                        fontSize = 21.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        modifier = Modifier.testTag("math_days_value")
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.06f))
                                            .border(0.8.dp, TitaniumHairline, CircleShape)
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
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.06f))
                                            .border(0.8.dp, TitaniumHairline, CircleShape)
                                            .clickable {
                                                hapticTick()
                                                if (uiState.mathDays < 9999L) onDaysChanged(uiState.mathDays + 1)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Plus",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Compact 52.dp Frameless Crown Scrubber (with 0..9999 hard boundary clamping & spring snap-back)
        Box(modifier = Modifier.padding(horizontal = 22.dp)) {
            MechanicalScrubRuler(
                targetOptions = listOf("Base", "Shift"),
                selectedTargetIndex = scrubTargetIndex,
                onTargetSelected = { idx ->
                    scrubTargetIndex = idx
                    onHudInteraction(
                        if (idx == 0) "Crown Scrubber focused on Base Date."
                        else "Crown Scrubber focused on Days to Shift."
                    )
                },
                activeValueReadout = scrubReadout,
                accentColor = IceCyan,
                currentNumericValue = if (scrubTargetIndex == 1) uiState.mathDays else null,
                minValue = if (scrubTargetIndex == 1) 0L else null,
                maxValue = if (scrubTargetIndex == 1) 9999L else null,
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

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Preset Chips with Edge-to-Edge scrolling
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 22.dp),
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

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Directional Shift Buttons: Earlier (−) vs Later (+)
        Box(modifier = Modifier.padding(horizontal = 22.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernPillButton(
                    text = "Earlier (−)",
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = { onOperationChanged(false) },
                    isPrimary = !uiState.mathIsAdd,
                    accentColor = WarmChampagne,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("math_btn_sub")
                )

                ModernPillButton(
                    text = "Later (+)",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = { onOperationChanged(true) },
                    isPrimary = uiState.mathIsAdd,
                    accentColor = IceCyan,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("math_btn_add")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

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
                borderColor = if (uiState.mathExcludeWeekends) IceCyan else TitaniumHairline,
                glowAlpha = if (uiState.mathExcludeWeekends) 0.6f else 0.2f,
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
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (uiState.mathExcludeWeekends) IceCyan.copy(alpha = 0.16f) else SpaceCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkHistory,
                                contentDescription = null,
                                tint = if (uiState.mathExcludeWeekends) IceCyan else TextMuted,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Skip Weekends",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Counts working days only",
                                fontFamily = SpaceGroteskFontFamily,
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

        Spacer(modifier = Modifier.height(12.dp))

        // 6. Clean Hero Result Date + Single-Line 3-Metric Strip ("2w 6d • 14 Workdays • 480 Hours")
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
                        .size(190.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    IceCyan.copy(alpha = 0.10f),
                                    Color.White.copy(alpha = 0.03f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "RESULT DATE",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    AnimatedContent(
                        targetState = uiState.mathArrivalDate,
                        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                        label = "arrival_anim"
                    ) { arrivalDate ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = arrivalDate.format(TimeEngine.FULL_DATE_FORMATTER),
                                fontFamily = CourierPrimeFontFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("math_calculated_arrival")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val shiftDesc = if (uiState.mathExcludeWeekends) {
                                "${uiState.mathDays} working days ${if (uiState.mathIsAdd) "later" else "earlier"}"
                            } else {
                                "${uiState.mathDays} days ${if (uiState.mathIsAdd) "later" else "earlier"}"
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(ObsidianGlass)
                                    .border(
                                        0.8.dp,
                                        TitaniumHairline,
                                        RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = shiftDesc,
                                    fontFamily = SpaceGroteskFontFamily,
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

        // 7. Bottom Spacer for Floating Glass Dock
        Spacer(modifier = Modifier.height(110.dp))
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
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Enter Days to Shift",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Type any number of days (0 – 9999):",
                    fontFamily = SpaceGroteskFontFamily,
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
                        unfocusedBorderColor = TitaniumHairline,
                        focusedTextColor = accentColor,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("math_direct_days_input")
                )

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
                                .border(0.8.dp, TitaniumHairline, RoundedCornerShape(10.dp))
                                .clickable {
                                    try {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    } catch (e: Exception) {
                                        Log.w("DateMathScreen", "Haptic feedback unavailable", e)
                                    }
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
                    } catch (e: Exception) {
                        Log.w("DateMathScreen", "Haptic feedback unavailable", e)
                    }
                    val parsed = textValue.toLongOrNull()?.coerceIn(0L, 9999L) ?: initialDays
                    onConfirm(parsed)
                }
            ) {
                Text(
                    text = "Apply",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    } catch (e: Exception) {
                        Log.w("DateMathScreen", "Haptic feedback unavailable", e)
                    }
                    onDismiss()
                }
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = SpaceGroteskFontFamily,
                    color = TextMuted
                )
            }
        }
    )
}
