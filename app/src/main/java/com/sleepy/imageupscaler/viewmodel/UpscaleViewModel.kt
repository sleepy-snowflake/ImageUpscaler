package com.sleepy.imageupscaler.viewmodel

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepy.imageupscaler.UpscaleState
import com.sleepy.imageupscaler.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class UpscaleViewModel : ViewModel() {
    private val _state = MutableStateFlow<UpscaleState>(UpscaleState.Idle)
    val state: StateFlow<UpscaleState> = _state.asStateFlow()

    private val _selectedScale = MutableStateFlow(2)
    val selectedScale: StateFlow<Int> = _selectedScale.asStateFlow()

    private var upscaleJob: Job? = null
    private var downloadJob: Job? = null

    fun setScale(scale: Int) {
        _selectedScale.value = scale
    }

    fun onImageSelected(
        context: Context,
        uri: Uri,
        onSizeCalculated: (fileName: String, fileSize: Long) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mimeType = context.contentResolver.getType(uri)
                if (mimeType?.startsWith("image/") != true) {
                    throw Exception("Please select an image file")
                }

                val fileName = getFileName(context, uri) ?: "image.jpg"
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw Exception("Could not read image")

                withContext(Dispatchers.Main) {
                    _state.value = UpscaleState.ImageSelected(
                        uri = uri,
                        fileName = fileName,
                        fileSize = bytes.size.toLong(),
                    )
                    onSizeCalculated(fileName, bytes.size.toLong())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _state.value = UpscaleState.Error("${e.message}")
                }
            }
        }
    }

    fun startUpscale(context: Context) {
        val currentState = _state.value
        if (currentState !is UpscaleState.ImageSelected) return

        upscaleJob?.cancel()
        upscaleJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    _state.value = UpscaleState.Processing("Getting configuration...", 0f)
                }

                val config = ApiService.getConfig()

                withContext(Dispatchers.Main) {
                    _state.value = UpscaleState.Processing("Uploading image...", 0.3f)
                }

                val imageBytes = context.contentResolver.openInputStream(currentState.uri)
                    ?.use { it.readBytes() }
                    ?: throw Exception("Could not read image")

                val serverFilename = ApiService.uploadImage(config, imageBytes, currentState.fileName)

                withContext(Dispatchers.Main) {
                    _state.value = UpscaleState.Processing("Upscaling ${_selectedScale.value}x...", 0.6f)
                }

                val progressStart = 0.6f
                val progressRange = 0.4f
                var lastProgressUpdate = 0L

                val resultBytes = ApiService.upscaleImage(
                    config = config,
                    serverFilename = serverFilename,
                    scale = _selectedScale.value,
                    onProgress = { downloaded, total ->
                        val now = System.currentTimeMillis()
                        if (now - lastProgressUpdate > 100) {
                            lastProgressUpdate = now
                            val p = progressStart + progressRange * (downloaded.toFloat() / total.toFloat())
                            _state.value = UpscaleState.Processing(
                                "Upscaling ${_selectedScale.value}x...",
                                p.coerceIn(progressStart, 1f),
                            )
                        }
                    }
                )

                val outputName = "upscaled_${_selectedScale.value}x_${currentState.fileName}"

                withContext(Dispatchers.Main) {
                    _state.value = UpscaleState.Completed(
                        imageBytes = resultBytes,
                        fileName = outputName,
                        fileSize = resultBytes.size.toLong(),
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _state.value = UpscaleState.Error("Upscale failed: ${e.message}")
                }
            }
        }
    }

    fun downloadFile(context: Context) {
        val currentState = _state.value
        if (currentState !is UpscaleState.Completed) return

        val bytes = currentState.imageBytes
        val name = currentState.fileName

        downloadJob?.cancel()
        downloadJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    _state.value = UpscaleState.Downloading(
                        imageBytes = bytes,
                        fileName = name,
                        progress = 0f,
                        bytesDownloaded = 0L,
                        totalBytes = bytes.size.toLong(),
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, name)
                        put(MediaStore.Downloads.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }

                    val uri = context.contentResolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        contentValues
                    ) ?: throw Exception("Could not create file")

                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        writeWithProgress(bytes, output) { progress, downloaded ->
                            withContext(Dispatchers.Main) {
                                _state.value = UpscaleState.Downloading(
                                    imageBytes = bytes,
                                    fileName = name,
                                    progress = progress,
                                    bytesDownloaded = downloaded,
                                    totalBytes = bytes.size.toLong(),
                                )
                            }
                        }
                    }

                    contentValues.clear()
                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                } else {
                    @Suppress("DEPRECATION")
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    )
                    val file = File(downloadsDir, name)
                    FileOutputStream(file).use { output ->
                        writeWithProgress(bytes, output) { progress, downloaded ->
                            withContext(Dispatchers.Main) {
                                _state.value = UpscaleState.Downloading(
                                    imageBytes = bytes,
                                    fileName = name,
                                    progress = progress,
                                    bytesDownloaded = downloaded,
                                    totalBytes = bytes.size.toLong(),
                                )
                            }
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    _state.value = UpscaleState.Completed(
                        imageBytes = bytes,
                        fileName = name,
                        fileSize = bytes.size.toLong(),
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _state.value = UpscaleState.Error("Download failed: ${e.message}")
                }
            }
        }
    }

    private fun writeWithProgress(
        bytes: ByteArray,
        output: java.io.OutputStream,
        onProgress: (Float, Long) -> Unit,
    ) {
        val chunkSize = 8192
        var offset = 0
        var lastProgressUpdate = 0L
        while (offset < bytes.size) {
            val end = minOf(offset + chunkSize, bytes.size)
            output.write(bytes, offset, end - offset)
            offset = end
            val now = System.currentTimeMillis()
            if (now - lastProgressUpdate > 100) {
                lastProgressUpdate = now
                onProgress(offset.toFloat() / bytes.size.toFloat(), offset.toLong())
            }
        }
    }

    fun clear() {
        upscaleJob?.cancel()
        downloadJob?.cancel()
        _selectedScale.value = 2
        _state.value = UpscaleState.Idle
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) return it.getString(nameIndex)?.takeIf { it.isNotEmpty() }
            }
        }
        return uri.lastPathSegment
    }
}
