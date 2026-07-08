package com.sleepy.upscale.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TokyoColorScheme = darkColorScheme(
    primary = TokyoPrimary,
    onPrimary = TokyoOnPrimary,
    secondary = TokyoSecondary,
    tertiary = TokyoAccent,
    background = TokyoBg,
    surface = TokyoSurface,
    surfaceVariant = TokyoSurfaceElevated,
    error = TokyoError,
    onBackground = TokyoText,
    onSurface = TokyoText,
    onSurfaceVariant = TokyoTextMuted,
    outline = TokyoBorder,
    outlineVariant = TokyoBorder,
)

@Composable
fun UpscaleTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = TokyoColorScheme, content = content)
}
