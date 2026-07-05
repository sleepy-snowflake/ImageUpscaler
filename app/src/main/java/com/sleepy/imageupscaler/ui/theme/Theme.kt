package com.sleepy.imageupscaler.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BlackAndWhiteScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    secondary = Color(0xFF666666),
    onSecondary = Color.White,
    surface = Color(0xFF1A1A1A),
    onSurface = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surfaceVariant = Color(0xFF242424),
    onSurfaceVariant = Color(0xFF999999),
    outline = Color(0xFF333333),
)

@Composable
fun ImageUpscalerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BlackAndWhiteScheme,
        content = content,
    )
}
