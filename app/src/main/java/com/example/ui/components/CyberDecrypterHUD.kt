package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CyberDecrypterHUD(
    targetLogText: String,
    isUtc: Boolean,
    onTimezoneToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SpaceDark.copy(alpha = 0.94f),
                            SpaceCard.copy(alpha = 0.94f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = NeonCyan.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Lightbulb Icon and Helpful Hint
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TipsAndUpdates,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    AnimatedContent(
                        targetState = targetLogText,
                        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                        label = "hint_text_anim"
                    ) { hint ->
                        Text(
                            text = hint,
                            fontFamily = com.example.ui.theme.ComicNeueFontFamily,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = TextSecondary,
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right: Timezone LOC/UTC Pill
                val view = androidx.compose.ui.platform.LocalView.current
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(SpaceCard)
                        .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(50))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                            onTimezoneToggled(!isUtc)
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("timezone_toggle"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LOC",
                        fontFamily = com.example.ui.theme.CourierPrimeFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!isUtc) NeonCyan else TextMuted
                    )
                    Text(
                        text = " / ",
                        fontFamily = com.example.ui.theme.CourierPrimeFontFamily,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "UTC",
                        fontFamily = com.example.ui.theme.CourierPrimeFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUtc) NeonCyan else TextMuted
                    )
                }
            }
        }
    }
}
