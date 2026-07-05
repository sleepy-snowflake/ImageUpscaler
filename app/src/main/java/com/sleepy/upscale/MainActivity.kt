package com.sleepy.upscale

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ViewFlipper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.sleepy.upscale.viewmodel.UpscaleViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: UpscaleViewModel by viewModels()

    private lateinit var viewFlipper: ViewFlipper

    private lateinit var imageCard: View
    private lateinit var imagePreview: View
    private lateinit var imagePlaceholder: View
    private lateinit var imageNameText: View
    private lateinit var imageSizeText: View
    private lateinit var fileInfoGroup: View
    private lateinit var scaleToggleGroup: MaterialButtonToggleGroup
    private lateinit var scale2x: MaterialButton
    private lateinit var scale4x: MaterialButton
    private lateinit var upscaleButton: MaterialButton

    private lateinit var loadingSpinner: CircularProgressIndicator
    private lateinit var stageText: View
    private lateinit var progressBar: LinearProgressIndicator

    private lateinit var resultPreview: View
    private lateinit var downloadFileName: View
    private lateinit var downloadFileSize: View
    private lateinit var downloadFileInfo: View
    private lateinit var downloadProgressGroup: View
    private lateinit var downloadProgressBar: LinearProgressIndicator
    private lateinit var downloadPercentText: View
    private lateinit var downloadBytesText: View
    private lateinit var downloadTotalText: View
    private lateinit var saveButton: MaterialButton
    private lateinit var clearButton: MaterialButton

    private lateinit var errorMessage: View
    private lateinit var retryButton: MaterialButton

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.onImageSelected(this, it) { _, _ -> }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewFlipper = findViewById(R.id.viewFlipper)

        imageCard = findViewById(R.id.imageCard)
        imagePreview = findViewById(R.id.imagePreview)
        imagePlaceholder = findViewById(R.id.imagePlaceholder)
        imageNameText = findViewById(R.id.imageNameText)
        imageSizeText = findViewById(R.id.imageSizeText)
        fileInfoGroup = findViewById(R.id.fileInfoGroup)
        scaleToggleGroup = findViewById(R.id.scaleToggleGroup)
        scale2x = findViewById(R.id.scale2x)
        scale4x = findViewById(R.id.scale4x)
        upscaleButton = findViewById(R.id.upscaleButton)

        loadingSpinner = findViewById(R.id.loadingSpinner)
        stageText = findViewById(R.id.stageText)
        progressBar = findViewById(R.id.progressBar)

        resultPreview = findViewById(R.id.resultPreview)
        downloadFileName = findViewById(R.id.downloadFileName)
        downloadFileSize = findViewById(R.id.downloadFileSize)
        downloadFileInfo = findViewById(R.id.downloadFileInfo)
        downloadProgressGroup = findViewById(R.id.downloadProgressGroup)
        downloadProgressBar = findViewById(R.id.downloadProgressBar)
        downloadPercentText = findViewById(R.id.downloadPercentText)
        downloadBytesText = findViewById(R.id.downloadBytesText)
        downloadTotalText = findViewById(R.id.downloadTotalText)
        saveButton = findViewById(R.id.saveButton)
        clearButton = findViewById(R.id.clearButton)

        errorMessage = findViewById(R.id.errorMessage)
        retryButton = findViewById(R.id.retryButton)

        imageCard.setOnClickListener { imagePicker.launch("image/*") }
        scaleToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                viewModel.setScale(if (checkedId == R.id.scale2x) 2 else 4)
            }
        }
        upscaleButton.setOnClickListener { viewModel.startUpscale(this) }
        saveButton.setOnClickListener { viewModel.downloadFile(this) }
        clearButton.setOnClickListener { viewModel.clear() }
        retryButton.setOnClickListener { viewModel.clear() }

        observeState()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        when (state) {
                            is UpscaleState.Idle -> showUpload(null)
                            is UpscaleState.ImageSelected -> showUpload(state)
                            is UpscaleState.Processing -> showProcessing(state)
                            is UpscaleState.Completed -> showCompleted(state)
                            is UpscaleState.Downloading -> showDownloading(state)
                            is UpscaleState.Error -> showError(state)
                        }
                    }
                }
                launch {
                    viewModel.selectedScale.collect { scale ->
                        scaleToggleGroup.check(if (scale == 2) R.id.scale2x else R.id.scale4x)
                    }
                }
            }
        }
    }

    private fun showUpload(selected: UpscaleState.ImageSelected?) {
        viewFlipper.displayedChild = 0

        if (selected != null) {
            imagePlaceholder.visibility = View.GONE
            imagePreview.visibility = View.VISIBLE
            Glide.with(this).load(selected.uri).into(imagePreview as ImageView)
            (imageNameText as android.widget.TextView).text = selected.fileName
            (imageSizeText as android.widget.TextView).text = formatFileSize(selected.fileSize)
            fileInfoGroup.visibility = View.VISIBLE
            upscaleButton.isEnabled = true
        } else {
            imagePlaceholder.visibility = View.VISIBLE
            imagePreview.visibility = View.GONE
            fileInfoGroup.visibility = View.GONE
            upscaleButton.isEnabled = false
        }
    }

    private fun showProcessing(state: UpscaleState.Processing) {
        viewFlipper.displayedChild = 1
        (stageText as android.widget.TextView).text = state.stage
        progressBar.progress = (state.progress * 100).toInt()
    }

    private fun showCompleted(state: UpscaleState.Completed) {
        viewFlipper.displayedChild = 2
        Glide.with(this).load(state.imageBytes).into(resultPreview as ImageView)
        (downloadFileName as android.widget.TextView).text = state.fileName
        (downloadFileSize as android.widget.TextView).text = formatFileSize(state.fileSize)
        downloadFileInfo.visibility = View.VISIBLE
        downloadProgressGroup.visibility = View.GONE
        saveButton.visibility = View.VISIBLE
        clearButton.visibility = View.VISIBLE
    }

    private fun showDownloading(state: UpscaleState.Downloading) {
        viewFlipper.displayedChild = 2
        Glide.with(this).load(state.imageBytes).into(resultPreview as ImageView)
        (downloadFileName as android.widget.TextView).text = state.fileName
        (downloadFileSize as android.widget.TextView).text = formatFileSize(state.totalBytes)
        downloadFileInfo.visibility = View.VISIBLE
        downloadProgressGroup.visibility = View.VISIBLE
        saveButton.visibility = View.GONE
        clearButton.visibility = View.GONE

        val percent = (state.progress * 100).toInt()
        downloadProgressBar.progress = percent
        (downloadPercentText as android.widget.TextView).text = "$percent%"
        (downloadBytesText as android.widget.TextView).text = formatFileSize(state.bytesDownloaded)
        (downloadTotalText as android.widget.TextView).text = formatFileSize(state.totalBytes)
    }

    private fun showError(state: UpscaleState.Error) {
        viewFlipper.displayedChild = 3
        (errorMessage as android.widget.TextView).text = state.message
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
            else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
        }
    }
}
