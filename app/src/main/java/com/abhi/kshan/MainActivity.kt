package com.abhi.kshan

import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhi.kshan.ui.components.DeveloperVisionModal
import com.abhi.kshan.ui.components.FloatingGlassIslandDock
import com.abhi.kshan.ui.components.LifecycleParticleCanvas
import com.abhi.kshan.ui.components.PresentKshanDropdownCard
import com.abhi.kshan.ui.screens.CountdownScreen
import com.abhi.kshan.ui.screens.DateMathScreen
import com.abhi.kshan.ui.screens.DifferenceScreen
import com.abhi.kshan.ui.screens.WidgetsDashboardScreen
import com.abhi.kshan.ui.theme.KalamFont
import com.abhi.kshan.ui.theme.KshanBackground
import com.abhi.kshan.ui.theme.KshanBorder
import com.abhi.kshan.ui.theme.KshanPillShape
import com.abhi.kshan.ui.theme.KshanSurface
import com.abhi.kshan.ui.theme.MyApplicationTheme
import com.abhi.kshan.ui.theme.SpaceGroteskFontFamily
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.ui.theme.VisionGold
import com.abhi.kshan.viewmodel.ChronoTab
import com.abhi.kshan.viewmodel.ChronoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                KshanApp()
            }
        }
    }
}

@Composable
fun KshanApp(viewModel: ChronoViewModel = viewModel()) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedEvents by viewModel.savedEvents.collectAsStateWithLifecycle()

    var showPresentKshanOverlay by remember { mutableStateOf(false) }
    var showDeveloperVisionModal by remember { mutableStateOf(false) }

    // Lifecycle-bound 1-second ticker: runs ONLY while Lifecycle is at least STARTED,
    // pausing automatically when the app moves to background to save battery.
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.tickTelemetry()
            while (isActive) {
                delay(1000L)
                viewModel.tickTelemetry()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KshanBackground)
    ) {
        // 0. Subtle 2% Procedural Noise/Grain Overlay + Soft Radial Edge Vignette
        ProceduralGrainAndVignetteOverlay(modifier = Modifier.fillMaxSize())

        // 1. Dynamic Lifecycle-Aware Particle Canvas Background
        LifecycleParticleCanvas(
            modifier = Modifier.fillMaxSize(),
            powerSaverEnabled = uiState.powerSaverEnabled,
            triggerCelebration = uiState.triggerCelebration,
            onCelebrationFinished = { viewModel.clearCelebration() }
        )

        // 2. Main Scaffold Layout with Floating Glass Island Dock
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    KshanBackground.copy(alpha = 0.85f),
                                    KshanBackground
                                )
                            )
                        )
                ) {
                    FloatingGlassIslandDock(
                        selectedTab = uiState.currentTab,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .statusBarsPadding()
            ) {
                // Top Header Branding: Interactive Kalam Devanagari Squircle ("क्षण") & Filled #121620 "✦ Vision" Pill
                KshanHeader(
                    isPresentKshanOpen = showPresentKshanOverlay,
                    onSquircleClick = {
                        showPresentKshanOverlay = !showPresentKshanOverlay
                    },
                    onVisionClick = {
                        showPresentKshanOverlay = false
                        showDeveloperVisionModal = true
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Direction-Aware Fluid Page Transitions
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = uiState.currentTab,
                        transitionSpec = {
                            val forward = targetState.ordinal > initialState.ordinal
                            if (forward) {
                                (slideInHorizontally(animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f)) { it } + fadeIn(tween(260))) togetherWith
                                        (slideOutHorizontally(animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f)) { -it } + fadeOut(tween(200)))
                            } else {
                                (slideInHorizontally(animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f)) { -it } + fadeIn(tween(260))) togetherWith
                                        (slideOutHorizontally(animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f)) { it } + fadeOut(tween(200)))
                            }
                        },
                        label = "direction_aware_page_transition"
                    ) { tab ->
                        when (tab) {
                            ChronoTab.DIFFERENCE -> DifferenceScreen(
                                uiState = uiState,
                                onStartChanged = { viewModel.setDiffStart(it) },
                                onEndChanged = { viewModel.setDiffEnd(it) },
                                onSwapDates = { viewModel.swapDiffDates() },
                                onExcludeWeekendsChanged = { viewModel.toggleDiffWeekends(it) },
                                onFocusPaceChanged = { viewModel.setFocusHoursPerDay(it) },
                                onSaveAsMilestone = { title, date ->
                                    viewModel.saveHorizonOrSprintToKshans(title, date, "Goal")
                                },
                                onHudInteraction = { viewModel.setHudLog(it) }
                            )

                            ChronoTab.DATE_MATH -> DateMathScreen(
                                uiState = uiState,
                                onBaseChanged = { viewModel.setMathBase(it) },
                                onDaysChanged = { viewModel.setMathDays(it) },
                                onOperationChanged = { viewModel.setMathOperation(it) },
                                onExcludeWeekendsChanged = { viewModel.toggleMathWeekends(it) },
                                onPinToKshans = { title, date ->
                                    viewModel.saveHorizonOrSprintToKshans(title, date, "Goal")
                                },
                                onHudInteraction = { viewModel.setHudLog(it) }
                            )

                            ChronoTab.COUNTDOWN -> CountdownScreen(
                                uiState = uiState,
                                savedEvents = savedEvents,
                                onPinEvent = { viewModel.pinEvent(it) },
                                onDeleteEvent = { viewModel.deleteEvent(it) },
                                onAddEvent = { title, date, cat, repeatsYearly ->
                                    viewModel.addNewEvent(title, date, cat, repeatsYearly)
                                },
                                onTriggerCelebration = { viewModel.triggerManualCelebration() },
                                onClearCelebration = { viewModel.clearCelebration() },
                                onSelectEvent = { viewModel.selectActiveEvent(it) },
                                onToggleCountUpMode = { viewModel.toggleCountUpMode(it) },
                                onHudInteraction = { viewModel.setHudLog(it) }
                            )

                            ChronoTab.WIDGETS -> WidgetsDashboardScreen(
                                uiState = uiState,
                                savedEvents = savedEvents,
                                onPowerSaverToggled = { viewModel.togglePowerSaver(it) },
                                onOpenJumpInPlanner = { days, label ->
                                    viewModel.openJumpInPlanner(days, label)
                                },
                                onOpenJumpInRunway = { targetDate, label ->
                                    viewModel.openJumpInRunway(targetDate, label)
                                },
                                onSaveJumpToKshans = { title, targetDate ->
                                    viewModel.saveHorizonOrSprintToKshans(title, targetDate, "Goal")
                                },
                                onHudInteraction = { viewModel.setHudLog(it) }
                            )
                        }
                    }
                }
            }
        }

        // 3. Floating "Present Moment (क्षण)" Overlay Card
        PresentKshanDropdownCard(
            visible = showPresentKshanOverlay,
            telemetry = uiState.presentKshanTelemetry,
            onCaptureKshan = { note ->
                viewModel.capturePresentKshan(note)
            },
            onDismiss = { showPresentKshanOverlay = false }
        )

        // 4. Full-Screen Developer & Vision Showcase Modal ("✦ Vision" • Abhinav Yaduvanshi)
        DeveloperVisionModal(
            visible = showDeveloperVisionModal,
            epochMillis = uiState.widgetTelemetry.epochMillis,
            onDismiss = { showDeveloperVisionModal = false }
        )
    }
}

/**
 * Subtle 2% procedural noise/grain overlay + soft radial edge vignette on the root #070A12 Box
 * so the obsidian background feels cinematic rather than flat.
 */
@Composable
private fun ProceduralGrainAndVignetteOverlay(modifier: Modifier = Modifier) {
    val grainPoints = remember {
        val rng = Random(4201)
        FloatArray(360) { rng.nextFloat() }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 2% procedural grain specks
        val count = grainPoints.size / 2
        for (i in 0 until count) {
            val gx = grainPoints[i * 2] * w
            val gy = grainPoints[i * 2 + 1] * h
            val alpha = if (i % 2 == 0) 0.022f else 0.015f
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = 1.1.dp.toPx(),
                center = Offset(gx, gy)
            )
        }

        // Soft radial edge vignette
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.18f),
                    Color.Black.copy(alpha = 0.45f)
                ),
                center = Offset(w / 2f, h * 0.45f),
                radius = maxOf(w, h) * 0.72f
            ),
            size = size
        )
    }
}

@Composable
fun KshanHeader(
    isPresentKshanOpen: Boolean = false,
    onSquircleClick: () -> Unit = {},
    onVisionClick: () -> Unit = {}
) {
    val view = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        try {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        } catch (e: Exception) {
                            Log.w("KshanHeader", "Haptic feedback unavailable", e)
                        }
                        onSquircleClick()
                    }
                    .padding(end = 8.dp)
                    .testTag("kshan_header_squircle_button")
            ) {
                // (1) Interactive #121620 Squircle ("क्षण")
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(KshanSurface)
                        .border(
                            width = 1.dp,
                            color = if (isPresentKshanOpen) VisionGold.copy(alpha = 0.65f) else KshanBorder,
                            shape = RoundedCornerShape(13.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "क्षण",
                        fontFamily = KalamFont,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPresentKshanOpen) VisionGold else TextPrimary,
                        modifier = Modifier.offset(y = (-1).dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(KshanBorder)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Kshan",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "Intentional Time & Runway",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.5.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Visible
                    )
                }
            }

            // (2) Exclusive Vision Gold (#E6C87A) inside Filled Dark #121620 Full Pill with subtle inner highlight
            Box(
                modifier = Modifier
                    .clip(KshanPillShape)
                    .background(KshanSurface)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = KshanPillShape
                    )
                    .clickable {
                        try {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        } catch (e: Exception) {
                            Log.w("KshanHeader", "Haptic feedback unavailable", e)
                        }
                        onVisionClick()
                    }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("kshan_vision_pill_button")
            ) {
                Text(
                    text = "✦ Vision",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VisionGold
                )
            }
        }
    }
}
