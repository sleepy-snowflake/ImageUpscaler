package com.sleepy.imageupscaler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sleepy.imageupscaler.ui.screens.DownloadScreen
import com.sleepy.imageupscaler.ui.screens.ProcessingScreen
import com.sleepy.imageupscaler.ui.screens.UploadScreen
import com.sleepy.imageupscaler.ui.theme.ImageUpscalerTheme
import com.sleepy.imageupscaler.viewmodel.UpscaleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageUpscalerTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                ) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
fun AppContent(viewModel: UpscaleViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val selectedScale by viewModel.selectedScale.collectAsState()
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var imageName by remember { mutableStateOf<String?>(null) }
    var imageSize by remember { mutableStateOf<Long?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            imageUri = it
            viewModel.onImageSelected(context, it) { name, size ->
                imageName = name
                imageSize = size
            }
        }
    }

    val crossfadeTarget by remember { derivedStateOf { state::class } }

    Crossfade(
        targetState = crossfadeTarget,
        animationSpec = tween(400),
    ) {
        when (state) {
            is UpscaleState.Idle, is UpscaleState.ImageSelected -> {
                val s = state as? UpscaleState.ImageSelected
                UploadScreen(
                    imageUri = s?.uri ?: imageUri,
                    selectedScale = selectedScale,
                    imageName = s?.fileName ?: imageName,
                    imageSize = s?.fileSize ?: imageSize,
                    onPickImage = { imagePickerLauncher.launch("image/*") },
                    onSelectScale = { viewModel.setScale(it) },
                    onUpscale = { viewModel.startUpscale(context) },
                )
            }

            is UpscaleState.Processing -> {
                ProcessingScreen(
                    stage = (state as UpscaleState.Processing).stage,
                    progress = (state as UpscaleState.Processing).progress,
                )
            }

            is UpscaleState.Completed -> {
                val s = state as UpscaleState.Completed
                DownloadScreen(
                    imageBytes = s.imageBytes,
                    fileName = s.fileName,
                    fileSize = s.fileSize,
                    downloadProgress = 0f,
                    isDownloading = false,
                    bytesDownloaded = 0L,
                    onDownload = { viewModel.downloadFile(context) },
                    onClear = {
                        imageUri = null
                        imageName = null
                        imageSize = null
                        viewModel.clear()
                    },
                )
            }

            is UpscaleState.Downloading -> {
                val s = state as UpscaleState.Downloading
                val bytes = viewModel.lastImageBytes
                if (bytes == null) {
                    ProcessingScreen(stage = "Downloading...", progress = 0f)
                } else {
                    DownloadScreen(
                        imageBytes = bytes,
                        fileName = viewModel.lastFileName ?: "image.jpg",
                        fileSize = s.totalBytes,
                        downloadProgress = s.progress,
                        isDownloading = true,
                        bytesDownloaded = s.bytesDownloaded,
                        onDownload = {},
                        onClear = {
                            imageUri = null
                            imageName = null
                            imageSize = null
                            viewModel.clear()
                        },
                    )
                }
            }

            is UpscaleState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (state as UpscaleState.Error).message,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                        Button(
                            onClick = {
                                imageUri = null
                                imageName = null
                                imageSize = null
                                viewModel.clear()
                            },
                            modifier = Modifier.padding(top = 24.dp),
                        ) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
    }
}
