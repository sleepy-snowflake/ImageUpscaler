package com.sleepy.imageupscaler

import android.net.Uri

sealed class UpscaleState {
    data object Idle : UpscaleState()

    data class ImageSelected(
        val uri: Uri,
        val fileName: String,
        val fileSize: Long,
    ) : UpscaleState()

    data class Processing(
        val stage: String,
        val progress: Float = 0f,
    ) : UpscaleState()

    data class Completed(
        val imageBytes: ByteArray,
        val fileName: String,
        val fileSize: Long,
    ) : UpscaleState()

    data class Downloading(
        val progress: Float,
        val bytesDownloaded: Long,
        val totalBytes: Long,
    ) : UpscaleState()

    data class Error(val message: String) : UpscaleState()
}
