package com.sleepy.upscale.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sleepy.upscale.UpscaleState
import com.sleepy.upscale.ui.theme.TokyoAccent
import com.sleepy.upscale.ui.theme.TokyoGlow
import com.sleepy.upscale.ui.theme.TokyoOnPrimary
import com.sleepy.upscale.ui.theme.TokyoPrimary
import com.sleepy.upscale.ui.theme.TokyoSecondary
import com.sleepy.upscale.ui.theme.TokyoSurface
import com.sleepy.upscale.ui.theme.TokyoSurfaceElevated
import com.sleepy.upscale.ui.theme.TokyoSurfaceGlass
import com.sleepy.upscale.ui.theme.TokyoText
import com.sleepy.upscale.ui.theme.TokyoTextBright
import com.sleepy.upscale.ui.theme.TokyoTextMuted

@Composable
fun DownloadScreen(
    state: UpscaleState,
    onSaveClick: () -> Unit,
    onClearClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "download_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow_pulse"
    )

    val isCompleted = state is UpscaleState.Completed
    val isDownloading = state is UpscaleState.Downloading

    val completed = state as? UpscaleState.Completed
    val downloading = state as? UpscaleState.Downloading

    val checkScale by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        animationSpec = tween(600, delayMillis = 300), label = "check_scale"
    )

    val cardScale by animateFloatAsState(
        targetValue = if (isCompleted && checkScale > 0.5f) 1f else 0.95f,
        animationSpec = tween(400), label = "card_scale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isCompleted || isDownloading) 1f else 0f,
        animationSpec = tween(400), label = "content_alpha"
    )

    val currentName = completed?.fileName ?: downloading?.fileName ?: ""
    val currentSize = completed?.fileSize ?: downloading?.totalBytes ?: 0L
    val currentBytes = downloading?.imageBytes ?: completed?.imageBytes ?: byteArrayOf()
    val downloadProgress = downloading?.progress ?: 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TokyoSurface)
            .padding(horizontal = 24.dp)
            .systemBarsPadding()
    ) {
        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(checkScale)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(listOf(TokyoPrimary, TokyoAccent))
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("✓", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TokyoOnPrimary)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = if (isDownloading) "Saving..." else "Done",
                    fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    color = TokyoTextBright, letterSpacing = (-0.5).sp,
                )
                Text(
                    text = if (isDownloading) "Downloading your image" else "Your upscaled image is ready",
                    fontSize = 14.sp, color = TokyoTextMuted,
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .weight(1f).fillMaxWidth()
                .graphicsLayer(scaleX = cardScale, scaleY = cardScale, alpha = contentAlpha)
                .shadow(32.dp, RoundedCornerShape(20.dp),
                    ambientColor = TokyoGlow.copy(alpha = glowPulse),
                    spotColor = TokyoGlow.copy(alpha = glowPulse * 0.5f))
                .clip(RoundedCornerShape(20.dp))
                .background(TokyoSurfaceGlass)
                .border(1.5.dp, TokyoGlow.copy(alpha = glowPulse), RoundedCornerShape(20.dp))
        ) {
            AsyncImage(
                model = currentBytes,
                contentDescription = "Upscaled image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth().alpha(contentAlpha)
                .graphicsLayer { translationY = (contentAlpha * 20f - 20f) },
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(currentName, fontSize = 13.sp, color = TokyoText, maxLines = 1,
                modifier = Modifier.weight(1f))
            Text(formatSize(currentSize), fontSize = 12.sp, color = TokyoTextMuted)
        }

        AnimatedVisibility(
            visible = isDownloading,
            enter = fadeIn() + slideInVertically { it / 2 },
        ) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Downloading...", fontSize = 13.sp, color = TokyoTextMuted)
                    Text("${(downloadProgress * 100).toInt()}%", fontSize = 13.sp, color = TokyoTextBright)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { downloadProgress },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    trackColor = TokyoSurfaceElevated,
                    indicatorColor = Brush.horizontalGradient(listOf(TokyoPrimary, TokyoAccent)),
                    strokeCap = StrokeCap.Round,
                )
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatSize(downloading?.bytesDownloaded ?: 0L), fontSize = 11.sp, color = TokyoTextMuted)
                    Text(formatSize(downloading?.totalBytes ?: 0L), fontSize = 11.sp, color = TokyoTextMuted)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth().height(56.dp)
                .alpha(if (isCompleted) 1f else 0.5f)
                .shadow(if (isCompleted) 12.dp else 0.dp, RoundedCornerShape(16.dp),
                    ambientColor = TokyoGlow, spotColor = TokyoGlow)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isCompleted) Brush.horizontalGradient(listOf(TokyoPrimary, TokyoAccent))
                    else TokyoSurfaceElevated
                )
                .clickable(
                    enabled = isCompleted,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSaveClick()
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (isDownloading) "Saving..." else "Save to Downloads",
                fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                color = if (isCompleted) TokyoOnPrimary else TokyoTextMuted,
            )
        }

        TextButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClearClick()
            },
            modifier = Modifier.fillMaxWidth().height(52.dp).alpha(contentAlpha),
        )

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text("Clear", fontSize = 15.sp, color = TokyoTextMuted)
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
    else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
}
