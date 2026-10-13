package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.FloatingGlassIslandDock
import com.example.ui.components.LifecycleParticleCanvas
import com.example.ui.screens.CountdownScreen
import com.example.ui.screens.DateMathScreen
import com.example.ui.screens.DifferenceScreen
import com.example.ui.screens.WidgetsDashboardScreen
import com.example.ui.theme.DevanagariDisplayFont
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ChronoTab
import com.example.viewmodel.ChronoViewModel

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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedEvents by viewModel.savedEvents.collectAsStateWithLifecycle()

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
                FloatingGlassIslandDock(
                    selectedTab = uiState.currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .statusBarsPadding()
            ) {
                // Top Header Branding: क्षण (Kshan)
                KshanHeader()

                Spacer(modifier = Modifier.height(10.dp))

                // Active Mode Screen with Fluid Transitions
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = uiState.currentTab,
                        transitionSpec = {
                            fadeIn(tween(240, easing = FastOutSlowInEasing)) togetherWith
                                    fadeOut(tween(180, easing = FastOutSlowInEasing))
                        },
                        label = "mode_switch_anim"
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
                                onHudInteraction = { viewModel.setHudLog(it) }
                            )

                            ChronoTab.WIDGETS -> WidgetsDashboardScreen(
                                uiState = uiState,
                                onPowerSaverToggled = { viewModel.togglePowerSaver(it) },
                                onHudInteraction = { viewModel.setHudLog(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KshanHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                // Prominent Devanagari Script Logo in Rozha One Font
                Text(
                    text = "क्षण",
                    fontFamily = DevanagariDisplayFont,
                    fontSize = 36.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.width(10.dp))

                // English Pronunciation Guide
                Column(modifier = Modifier.padding(bottom = 4.dp)) {
                    Text(
                        text = "KSHAN • /k-sh-uh-n/",
                        fontFamily = FontFamily.Default,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Text(
                        text = "A Moment in Time",
                        fontFamily = FontFamily.Default,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            // Pro Precision Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(NeonCyan.copy(alpha = 0.15f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Pro",
                    fontFamily = FontFamily.Default,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }
        }
    }
}
