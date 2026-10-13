package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ChronoTab

data class DockTabItem(
    val tab: ChronoTab,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun FloatingGlassIslandDock(
    selectedTab: ChronoTab,
    onTabSelected: (ChronoTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    val tabs = remember {
        listOf(
            DockTabItem(ChronoTab.DIFFERENCE, "Difference", Icons.Default.CompareArrows, "tab_difference"),
            DockTabItem(ChronoTab.DATE_MATH, "Shift", Icons.Default.Calculate, "tab_add___subtract"),
            DockTabItem(ChronoTab.COUNTDOWN, "Countdowns", Icons.Default.HourglassTop, "tab_countdowns"),
            DockTabItem(ChronoTab.WIDGETS, "Widgets", Icons.Default.Widgets, "tab_widgets")
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Frosted Glass Capsule
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SpaceCard.copy(alpha = 0.92f),
                            SpaceDark.copy(alpha = 0.95f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.45f),
                            SpaceDark.copy(alpha = 0.2f),
                            NeonCyan.copy(alpha = 0.35f)
                        )
                    ),
                    shape = RoundedCornerShape(34.dp)
                )
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { item ->
                    val isSelected = item.tab == selectedTab

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.72f, stiffness = 450f),
                        label = "dock_icon_scale"
                    )

                    val activeGlowColor by animateColorAsState(
                        targetValue = if (isSelected) NeonCyan else TextMuted,
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
                        label = "dock_color"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                if (isSelected) NeonCyan.copy(alpha = 0.15f)
                                else androidx.compose.ui.graphics.Color.Transparent
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onTabSelected(item.tab)
                            }
                            .testTag(item.testTag),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = activeGlowColor,
                                modifier = Modifier
                                    .size(22.dp)
                                    .scale(iconScale)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = item.label,
                                fontFamily = FontFamily.Default,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) NeonCyan else TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
