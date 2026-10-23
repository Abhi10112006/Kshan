package com.example

import android.os.Bundle
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
import com.example.ui.theme.ComicNeueFontFamily
import com.example.ui.theme.KalamFont
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
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
                // Top Header Branding: Kalam Devanagari Squircle & Kshan Title
                KshanHeader()

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
                            val springSpec = spring<Float>(dampingRatio = 0.82f, stiffness = 420f)
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
            .padding(horizontal = 22.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // (1) Frosted-Glass Squircle with ambient cyan/purple glow
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
                            width = 1.2.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    NeonCyan.copy(alpha = 0.7f),
                                    NeonPurple.copy(alpha = 0.4f)
                                )
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // (2) Vertical metallic-to-cyan gradient text with upward optical offset
                    val textGradient = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFD8FAFF),
                            NeonCyan
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

                // Translucent 1.dp x 26.dp vertical divider (White at 15% opacity)
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(26.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Brand Title & Meaning
                Column {
                    Text(
                        text = "Kshan",
                        fontFamily = ComicNeueFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Precision Date Companion",
                        fontFamily = ComicNeueFontFamily,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            // Pro Pill Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(NeonCyan.copy(alpha = 0.15f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Pro",
                    fontFamily = ComicNeueFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }
        }
    }
}
