package com.sleepy.upscale.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

        val cardShape = RoundedCornerShape(20.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .aspectRatio(1f)
                .then(
                    if (isSelected) Modifier.shadow(
                        32.dp, cardShape,
                        ambientColor = TokyoGlow, spotColor = TokyoGlow
                    ) else Modifier
                )
                .clip(cardShape)
                .background(TokyoSurfaceGlass)
                .border(
                    1.dp, TokyoBorder.copy(alpha = 0.3f), cardShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onPickImage() },
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                SpinningBorderOverlay(cardShape)
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data((state as UpscaleState.ImageSelected).uri)
                        .crossfade(400).build(),
                    contentDescription = "Selected image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            if (!isSelected) {
                UploadPlaceholder(pulse = pulse)
            }
        }

        Spacer(Modifier.height(10.dp))

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut(),
        ) {
            val selected = state as? UpscaleState.ImageSelected
            if (selected != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        selected.fileName,
                        fontSize = 13.sp,
                        color = TokyoText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        formatSize(selected.fileSize),
                        fontSize = 12.sp,
                        color = TokyoTextMuted,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        ScaleToggle(selectedScale, onScaleChange)
        Spacer(Modifier.height(16.dp))

        PressableButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onUpscaleClick()
            },
            enabled = isSelected,
            text = "Upscale",
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun BoxScope.UploadPlaceholder(pulse: Float) {
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

@Composable
private fun SpinningBorderOverlay(shape: RoundedCornerShape) {
    val transition = rememberInfiniteTransition(label = "spin_border")
    val rotation by transition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "spin"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { rotationZ = rotation }
            .background(
                Brush.sweepGradient(
                    colors = listOf(
                        TokyoPrimary,
                        TokyoAccent,
                        TokyoSecondary,
                        TokyoPrimary,
                    )
                ),
                shape,
            )
            .padding(2.dp)
            .clip(shape)
            .background(TokyoSurfaceGlass)
    )
}

@Composable
private fun PressableButton(
    onClick: () -> Unit,
    enabled: Boolean,
    text: String,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 800f),
        label = "btn_scale"
    )

    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .then(
                if (enabled) Modifier.shadow(16.dp, shape,
                    ambientColor = TokyoGlow, spotColor = TokyoGlow)
                else Modifier
            )
            .clip(shape)
            .then(
                if (enabled) Modifier.background(TokyoPrimary)
                else Modifier.background(TokyoSurfaceElevated)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) TokyoOnPrimary else TokyoTextMuted,
        )
    }
}

@Composable
private fun ScaleToggle(selectedScale: Int, onScaleChange: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val indicatorFraction by animateFloatAsState(
        targetValue = if (selectedScale == 2) 0f else 1f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "indicator_offset"
    )
    var toggleWidth by remember { mutableFloatStateOf(0f) }
    val containerShape = RoundedCornerShape(18.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(containerShape)
            .background(TokyoSurface)
            .border(1.5.dp, TokyoBorder.copy(alpha = 0.6f), containerShape)
            .onSizeChanged { toggleWidth = it.width.toFloat() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(5.dp)
                .offset(
                    x = with(density) {
                        (indicatorFraction * (toggleWidth - 2 * 5.dp.toPx()) / 2f).toDp()
                    }
                )
                .shadow(8.dp, RoundedCornerShape(13.dp),
                    ambientColor = TokyoGlow, spotColor = TokyoGlow)
                .clip(RoundedCornerShape(13.dp))
                .background(TokyoPrimary)
        )

        Row(Modifier.fillMaxSize()) {
            listOf(2, 4).forEachIndexed { _, scale ->
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "${scale}x",
                            fontSize = if (selectedScale == scale) 22.sp else 16.sp,
                            fontWeight = if (selectedScale == scale) FontWeight.Black else FontWeight.Medium,
                            color = if (selectedScale == scale) TokyoOnPrimary else TokyoTextMuted,
                            letterSpacing = 0.5.sp,
                        )
                        if (selectedScale == scale) {
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(TokyoOnPrimary.copy(alpha = 0.7f))
                            )
                        }
                    }
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
