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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
import com.abhi.kshan.ui.theme.ComicNeueFontFamily
import com.abhi.kshan.ui.theme.KalamFont
import com.abhi.kshan.ui.theme.MyApplicationTheme
import com.abhi.kshan.ui.theme.NeonCyan
import com.abhi.kshan.ui.theme.NeonPurple
import com.abhi.kshan.ui.theme.SpaceBlack
import com.abhi.kshan.ui.theme.SpaceCard
import com.abhi.kshan.ui.theme.SpaceDark
import com.abhi.kshan.ui.theme.TextMuted
import com.abhi.kshan.ui.theme.TextPrimary
import com.abhi.kshan.viewmodel.ChronoTab
import com.abhi.kshan.viewmodel.ChronoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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
            .background(SpaceBlack)
    ) {
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
                                    Color(0xFF050811).copy(alpha = 0.85f),
                                    Color(0xFF050811)
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
                // Top Header Branding: Interactive Kalam Devanagari Squircle ("वर्तमान क्षण") & "✦ Vision" Pill
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

                Spacer(modifier = Modifier.height(8.dp))

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
                                onExcludeWeekendsChanged = { viewModel.toggleDiffWeekends(it) },
                                onHudInteraction = { viewModel.setHudLog(it) }
                            )

                            ChronoTab.DATE_MATH -> DateMathScreen(
                                uiState = uiState,
                                onBaseChanged = { viewModel.setMathBase(it) },
                                onDaysChanged = { viewModel.setMathDays(it) },
                                onOperationChanged = { viewModel.setMathOperation(it) },
                                onExcludeWeekendsChanged = { viewModel.toggleMathWeekends(it) },
                                onHudInteraction = { viewModel.setHudLog(it) }
                            )

                            ChronoTab.COUNTDOWN -> CountdownScreen(
                                uiState = uiState,
                                savedEvents = savedEvents,
                                onPinEvent = { viewModel.pinEvent(it) },
                                onDeleteEvent = { viewModel.deleteEvent(it) },
                                onAddEvent = { title, date, cat ->
                                    viewModel.addNewEvent(title, date, cat)
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
                                onHudInteraction = { viewModel.setHudLog(it) }
                            )
                        }
                    }
                }
            }
        }

        // 3. Floating "Present Moment (वर्तमान क्षण)" Overlay Card (zIndex = 10f so it never squishes layout)
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

@Composable
fun KshanHeader(
    isPresentKshanOpen: Boolean = false,
    onSquircleClick: () -> Unit = {},
    onVisionClick: () -> Unit = {}
) {
    val view = LocalView.current
    val goldAccent = Color(0xFFFFD700)

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
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        try {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        } catch (e: Exception) {
                            Log.w("KshanHeader", "Haptic feedback unavailable", e)
                        }
                        onSquircleClick()
                    }
                    .testTag("kshan_header_squircle_button")
            ) {
                // (1) Interactive Frosted-Glass Squircle ("वर्तमान क्षण")
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    SpaceCard.copy(alpha = 0.95f),
                                    SpaceDark.copy(alpha = 0.98f)
                                )
                            )
                        )
                        .border(
                            width = 1.3.dp,
                            brush = Brush.verticalGradient(
                                colors = if (isPresentKshanOpen) {
                                    listOf(goldAccent, NeonCyan)
                                } else {
                                    listOf(
                                        NeonCyan.copy(alpha = 0.75f),
                                        goldAccent.copy(alpha = 0.45f),
                                        NeonPurple.copy(alpha = 0.45f)
                                    )
                                }
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val textGradient = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFD8FAFF),
                            if (isPresentKshanOpen) goldAccent else NeonCyan
                        )
                    )

                    Text(
                        text = "क्षण",
                        fontFamily = KalamFont,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(brush = textGradient),
                        modifier = Modifier.offset(y = (-2).dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(26.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Kshan",
                            fontFamily = ComicNeueFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• वर्तमान",
                            fontFamily = KalamFont,
                            fontSize = 11.5.sp,
                            color = goldAccent.copy(alpha = 0.88f)
                        )
                    }
                    Text(
                        text = "Precision Date Companion",
                        fontFamily = ComicNeueFontFamily,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            // (2) Interactive "✦ Vision" Developer Showcase Pill (replaces static "Pro" badge)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                goldAccent.copy(alpha = 0.16f),
                                NeonCyan.copy(alpha = 0.16f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                goldAccent.copy(alpha = 0.7f),
                                NeonCyan.copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable {
                        try {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        } catch (e: Exception) {
                            Log.w("KshanHeader", "Haptic feedback unavailable", e)
                        }
                        onVisionClick()
                    }
                    .padding(horizontal = 12.dp, vertical = 5.dp)
                    .testTag("kshan_vision_pill_button")
            ) {
                Text(
                    text = "✦ Vision",
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            colors = listOf(goldAccent, Color.White, NeonCyan)
                        )
                    )
                )
            }
        }
    }
}
