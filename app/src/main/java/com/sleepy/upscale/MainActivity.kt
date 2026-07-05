package com.sleepy.upscale

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sleepy.upscale.ui.screens.DownloadScreen
import com.sleepy.upscale.ui.screens.ProcessingScreen
import com.sleepy.upscale.ui.screens.UploadScreen
import com.sleepy.upscale.ui.theme.UpscaleTheme
import com.sleepy.upscale.viewmodel.UpscaleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UpscaleTheme {
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

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            viewModel.onImageSelected(context, it) { _, _ -> }
        }
    }

    Crossfade(
        targetState = state::class,
        animationSpec = tween(400),
    ) {
        when (val s = state) {
            is UpscaleState.Idle, is UpscaleState.ImageSelected -> {
                val selected = s as? UpscaleState.ImageSelected
                UploadScreen(
                    imageUri = selected?.uri,
                    selectedScale = selectedScale,
                    imageName = selected?.fileName,
                    imageSize = selected?.fileSize,
                    onPickImage = { imagePickerLauncher.launch("image/*") },
                    onSelectScale = { viewModel.setScale(it) },
                    onUpscale = { viewModel.startUpscale(context) },
                )
            }

            is UpscaleState.Processing -> {
                ProcessingScreen(stage = s.stage, progress = s.progress)
            }

            is UpscaleState.Completed -> {
                DownloadScreen(
                    imageBytes = s.imageBytes,
                    fileName = s.fileName,
                    fileSize = s.fileSize,
                    downloadProgress = 0f,
                    isDownloading = false,
                    bytesDownloaded = 0L,
                    onDownload = { viewModel.downloadFile(context) },
                    onClear = { viewModel.clear() },
                )
            }

            is UpscaleState.Downloading -> {
                DownloadScreen(
                    imageBytes = s.imageBytes,
                    fileName = s.fileName,
                    fileSize = s.totalBytes,
                    downloadProgress = s.progress,
                    isDownloading = true,
                    bytesDownloaded = s.bytesDownloaded,
                    onDownload = {},
                    onClear = { viewModel.clear() },
                )
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
                            text = s.message,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                        Button(
                            onClick = { viewModel.clear() },
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
