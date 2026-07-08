package com.sleepy.upscale.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleepy.upscale.UpscaleState
import com.sleepy.upscale.ui.theme.TokyoAccent
import com.sleepy.upscale.ui.theme.TokyoBg
import com.sleepy.upscale.ui.theme.TokyoGradientMid
import com.sleepy.upscale.ui.theme.TokyoPrimary
import com.sleepy.upscale.ui.theme.TokyoSecondary
import com.sleepy.upscale.ui.theme.TokyoSurface
import com.sleepy.upscale.ui.theme.TokyoSurfaceElevated
import com.sleepy.upscale.ui.theme.TokyoTextBright
import com.sleepy.upscale.ui.theme.TokyoTextMuted
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

@Composable
fun ProcessingScreen(state: UpscaleState.Processing) {
    val infiniteTransition = rememberInfiniteTransition(label = "processing")

    val bgProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse),
        label = "bg_progress"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )

    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "sweep"
    )

    val dotCount by remember { mutableIntStateOf(3) }
    var dotIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            dotIndex = (dotIndex + 1) % (dotCount + 1)
        }
    }

    val dots = ".".repeat(dotIndex)

    val animProgress by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(300),
        label = "progress_smooth"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        TokyoBg,
                        if (bgProgress < 0.5f) TokyoGradientMid else TokyoSurface,
                    ),
                    start = Offset.Zero,
                    end = Offset(0f, Float.POSITIVE_INFINITY),
                )
            )
            .padding(horizontal = 48.dp)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.3f))

        Text(
            text = "Upscaling$dots",
            fontSize = 34.sp,
            fontWeight = FontWeight.Light,
            color = TokyoTextBright,
            letterSpacing = (-0.5).sp,
        )

        Spacer(Modifier.height(48.dp))

        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val strokeWidth = 6.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)
                val top = 1f - sweepProgress

                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(TokyoPrimary, TokyoAccent, TokyoSecondary, TokyoPrimary),
                        center = center,
                    ),
                    startAngle = rotation,
                    sweepAngle = 360f * sweepProgress.coerceIn(0.05f, 0.95f),
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )

                drawArc(
                    color = TokyoSurfaceElevated,
                    startAngle = rotation + 360f * sweepProgress.coerceIn(0.05f, 0.95f),
                    sweepAngle = 360f - 360f * sweepProgress.coerceIn(0.05f, 0.95f),
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )

                for (i in 0 until 6) {
                    val angle = rotation + i * 60f
                    val r = radius * 0.7f
                    val px = center.x + r * cos(Math.toRadians(angle.toDouble())).toFloat()
                    val py = center.y + r * sin(Math.toRadians(angle.toDouble())).toFloat()
                    val alpha = (sin(Math.toRadians((angle + rotation).toDouble())).toFloat() + 1f) / 2f
                    drawCircle(
                        color = TokyoPrimary.copy(alpha = alpha * 0.4f),
                        radius = 3.dp.toPx(),
                        center = Offset(px, py),
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = state.stage,
            fontSize = 15.sp,
            color = TokyoTextMuted,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))

        LinearProgressIndicator(
            progress = { animProgress },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            trackColor = TokyoSurfaceElevated,
            indicatorColor = Brush.horizontalGradient(listOf(TokyoPrimary, TokyoAccent)),
            strokeCap = StrokeCap.Round,
        )

        Spacer(Modifier.weight(0.5f))
    }
}
