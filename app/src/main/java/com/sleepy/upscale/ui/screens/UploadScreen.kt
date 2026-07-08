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
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sleepy.upscale.UpscaleState
import com.sleepy.upscale.ui.theme.TokyoAccent
import com.sleepy.upscale.ui.theme.TokyoBg
import com.sleepy.upscale.ui.theme.TokyoBorder
import com.sleepy.upscale.ui.theme.TokyoGlow
import com.sleepy.upscale.ui.theme.TokyoGradientMid
import com.sleepy.upscale.ui.theme.TokyoOnPrimary
import com.sleepy.upscale.ui.theme.TokyoPrimary
import com.sleepy.upscale.ui.theme.TokyoSecondary
import com.sleepy.upscale.ui.theme.TokyoSurface
import com.sleepy.upscale.ui.theme.TokyoSurfaceElevated
import com.sleepy.upscale.ui.theme.TokyoSurfaceGlass
import com.sleepy.upscale.ui.theme.TokyoText
import com.sleepy.upscale.ui.theme.TokyoTextBright
import com.sleepy.upscale.ui.theme.TokyoTextMuted
import kotlin.math.roundToInt

@Composable
fun UploadScreen(
    state: UpscaleState,
    selectedScale: Int,
    onScaleChange: (Int) -> Unit,
    onUpscaleClick: () -> Unit,
    onPickImage: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val isSelected = state is UpscaleState.ImageSelected

    val infiniteTransition = rememberInfiniteTransition(label = "upload_bg")
    val bgProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Reverse),
        label = "bg_progress"
    )
    val bgColor1 = lerp(TokyoBg, TokyoGradientMid, bgProgress)
    val bgColor2 = lerp(TokyoGradientMid, TokyoSurface, bgProgress)

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(500), label = "glow_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(bgColor1, bgColor2, TokyoSurface),
                    start = Offset(0f, 0f), end = Offset(1000f, 1000f)
                )
            )
            .padding(horizontal = 24.dp)
            .systemBarsPadding()
    ) {
        Spacer(Modifier.height(48.dp))

        Text(
            text = "Upscale",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = TokyoTextBright,
            letterSpacing = (-1).sp,
        )

        Text(
            text = "Upscale your images 2x or 4x",
            fontSize = 14.sp,
            color = TokyoTextMuted,
            modifier = Modifier.padding(top = 4.dp),
        )

        Spacer(Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .aspectRatio(1f)
                .then(
                    if (isSelected) Modifier.shadow(
                        24.dp, RoundedCornerShape(20.dp),
                        ambientColor = TokyoGlow, spotColor = TokyoGlow
                    ) else Modifier
                )
                .clip(RoundedCornerShape(20.dp))
                .background(TokyoSurfaceGlass)
                .then(
                    if (isSelected) Modifier.border(
                        1.5.dp, TokyoGlow.copy(alpha = glowAlpha), RoundedCornerShape(20.dp)
                    ) else Modifier.border(
                        1.dp, TokyoBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp)
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onPickImage() },
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data((state as UpscaleState.ImageSelected).uri)
                        .crossfade(400).build(),
                    contentDescription = "Selected image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            AnimatedVisibility(
                visible = !isSelected,
                enter = fadeIn(), exit = fadeOut(),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .scale(pulse)
                            .clip(CircleShape)
                            .background(TokyoSurfaceElevated),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("+", fontSize = 28.sp, color = TokyoTextMuted)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Tap to select an image", fontSize = 13.sp, color = TokyoTextMuted)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut(),
        ) {
            val selected = state as? UpscaleState.ImageSelected
            if (selected != null) {
                Text(selected.fileName, fontSize = 13.sp, color = TokyoText, maxLines = 1)
                Text(formatSize(selected.fileSize), fontSize = 12.sp, color = TokyoTextMuted,
                    modifier = Modifier.padding(top = 2.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        ScaleToggle(selectedScale, onScaleChange)
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(if (isSelected) 12.dp else 0.dp, RoundedCornerShape(16.dp),
                    ambientColor = TokyoGlow, spotColor = TokyoGlow)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isSelected) Brush.horizontalGradient(
                        listOf(TokyoPrimary, TokyoAccent, TokyoSecondary)
                    ) else TokyoSurfaceElevated
                )
                .clickable(
                    enabled = isSelected,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUpscaleClick()
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Upscale",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) TokyoOnPrimary else TokyoTextMuted,
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ScaleToggle(selectedScale: Int, onScaleChange: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val indicatorOffset by animateFloatAsState(
        targetValue = if (selectedScale == 2) 0f else 1f,
        animationSpec = tween(350), label = "indicator_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(TokyoSurfaceElevated)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(4.dp)
                .offset { IntOffset((indicatorOffset * size.width).roundToInt(), 0) }
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(TokyoPrimary.copy(alpha = 0.2f), TokyoAccent.copy(alpha = 0.2f))
                    )
                )
        )

        Row(Modifier.fillMaxSize()) {
            listOf(2, 4).forEachIndexed { index, scale ->
                Box(
                    modifier = Modifier
                        .weight(1f).fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onScaleChange(scale)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${scale}x",
                        fontSize = 17.sp,
                        fontWeight = if (selectedScale == scale) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedScale == scale) TokyoTextBright else TokyoTextMuted,
                    )
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
    else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
}
