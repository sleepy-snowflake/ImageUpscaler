package com.sleepy.upscale.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleepy.upscale.UpscaleState
import com.sleepy.upscale.ui.theme.TokyoBg
import com.sleepy.upscale.ui.theme.TokyoError
import com.sleepy.upscale.ui.theme.TokyoGlow
import com.sleepy.upscale.ui.theme.TokyoOnPrimary
import com.sleepy.upscale.ui.theme.TokyoPrimary
import com.sleepy.upscale.ui.theme.TokyoTextBright
import com.sleepy.upscale.ui.theme.TokyoTextMuted
import kotlinx.coroutines.delay

@Composable
fun ErrorScreen(
    state: UpscaleState.Error,
    onRetryClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    var shakeOffset by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        repeat(3) {
            shakeOffset = -8f; delay(50)
            shakeOffset = 8f; delay(50)
            shakeOffset = -5f; delay(50)
            shakeOffset = 5f; delay(50)
            shakeOffset = 0f; delay(200)
        }
    }

    val iconScale by animateFloatAsState(
        targetValue = 1f, animationSpec = tween(400), label = "icon_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TokyoBg)
            .padding(horizontal = 48.dp)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.4f))

        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(iconScale)
                .graphicsLayer { translationX = shakeOffset }
                .background(TokyoError.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("!", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = TokyoError)
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Something went wrong",
            fontSize = 24.sp, fontWeight = FontWeight.Medium,
            color = TokyoTextBright, textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = state.message,
            fontSize = 14.sp, color = TokyoTextMuted,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(40.dp))

        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val btnScale by animateFloatAsState(
            targetValue = if (isPressed) 0.96f else 1f,
            animationSpec = spring(dampingRatio = 0.5f, stiffness = 800f),
            label = "btn_scale"
        )
        val shape = RoundedCornerShape(16.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth().height(56.dp)
                .scale(btnScale)
                .shadow(16.dp, shape,
                    ambientColor = TokyoGlow, spotColor = TokyoGlow)
                .clip(shape)
                .background(TokyoPrimary)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRetryClick()
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Try Again",
                fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                color = TokyoOnPrimary,
            )
        }

        Spacer(Modifier.weight(0.5f))
    }
}
