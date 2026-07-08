package com.sleepy.upscale

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleepy.upscale.ui.MainScreen
import com.sleepy.upscale.ui.theme.UpscaleTheme
import com.sleepy.upscale.viewmodel.UpscaleViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: UpscaleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UpscaleTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                val selectedScale by viewModel.selectedScale.collectAsStateWithLifecycle()

                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        state = state,
                        selectedScale = selectedScale,
                        onScaleChange = { viewModel.setScale(it) },
                        onUpscaleClick = { viewModel.startUpscale(this@MainActivity) },
                        onSaveClick = { viewModel.downloadFile(this@MainActivity) },
                        onClearClick = { viewModel.clear() },
                        onImagePicked = { uri ->
                            viewModel.onImageSelected(this@MainActivity, uri) { _, _ -> }
                        },
                    )
                }
            }
        }
    }
}
