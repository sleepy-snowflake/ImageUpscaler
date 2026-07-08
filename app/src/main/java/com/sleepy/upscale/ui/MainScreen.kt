package com.sleepy.upscale.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import com.sleepy.upscale.UpscaleState
import com.sleepy.upscale.ui.screens.DownloadScreen
import com.sleepy.upscale.ui.screens.ErrorScreen
import com.sleepy.upscale.ui.screens.ProcessingScreen
import com.sleepy.upscale.ui.screens.UploadScreen

@Composable
fun MainScreen(
    state: UpscaleState,
    selectedScale: Int,
    onScaleChange: (Int) -> Unit,
    onUpscaleClick: () -> Unit,
    onSaveClick: () -> Unit,
    onClearClick: () -> Unit,
    onImagePicked: (Uri) -> Unit,
) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onImagePicked(it) }
    }

    val screenKey = when (state) {
        is UpscaleState.Idle, is UpscaleState.ImageSelected -> "upload"
        is UpscaleState.Processing -> "processing"
        is UpscaleState.Completed, is UpscaleState.Downloading -> "download"
        is UpscaleState.Error -> "error"
    }

    AnimatedContent(
        targetState = screenKey,
        transitionSpec = {
            when (targetState) {
                "upload" -> ContentTransform(
                    fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 4 },
                    fadeOut(tween(200)) + slideOutVertically(tween(200)) { -it / 4 },
                )
                else -> ContentTransform(
                    fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 },
                    fadeOut(tween(200)) + slideOutVertically(tween(200)) { -it / 3 },
                )
            }
        },
        label = "screen_transition",
    ) { key ->
        when (key) {
            "upload" -> UploadScreen(
                state = state,
                selectedScale = selectedScale,
                onScaleChange = onScaleChange,
                onUpscaleClick = onUpscaleClick,
                onPickImage = { picker.launch("image/*") },
            )
            "processing" -> {
                val s = state as? UpscaleState.Processing ?: return@AnimatedContent
                ProcessingScreen(state = s)
            }
            "download" -> DownloadScreen(
                state = state,
                onSaveClick = onSaveClick,
                onClearClick = onClearClick,
            )
            "error" -> {
                val s = state as? UpscaleState.Error ?: return@AnimatedContent
                ErrorScreen(state = s, onRetryClick = onClearClick)
            }
        }
    }
}
