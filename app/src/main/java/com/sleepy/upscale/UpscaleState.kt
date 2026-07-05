package com.sleepy.upscale

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

    class Completed(
        val imageBytes: ByteArray,
        val fileName: String,
        val fileSize: Long,
    ) : UpscaleState() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Completed) return false
            return fileName == other.fileName &&
                fileSize == other.fileSize &&
                imageBytes.contentEquals(other.imageBytes)
        }
        override fun hashCode(): Int {
            var result = imageBytes.contentHashCode()
            result = 31 * result + fileName.hashCode()
            result = 31 * result + fileSize.hashCode()
            return result
        }
    }

    class Downloading(
        val imageBytes: ByteArray,
        val fileName: String,
        val progress: Float,
        val bytesDownloaded: Long,
        val totalBytes: Long,
    ) : UpscaleState()

    data class Error(val message: String) : UpscaleState()
}
