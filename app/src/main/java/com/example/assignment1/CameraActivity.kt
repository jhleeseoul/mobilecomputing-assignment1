package com.example.assignment1

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.assignment1.ui.theme.Assignment1Theme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraActivity : ComponentActivity() {

    private var imageCapture: ImageCapture? = null
    private lateinit var previewView: PreviewView

    private val selectedLabel = mutableStateOf("Table")
    private val labels = mutableStateListOf("Table", "Bookcase", "Bicycle")
    private var currentCameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private val cameraLensLabel = mutableStateOf("Back Camera")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        loadLabels()

        if (!hasCameraPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                1001
            )
        }

        setContent {
            Assignment1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CameraScreen(
                        modifier = Modifier.padding(innerPadding),
                        selectedLabel = selectedLabel.value,
                        onSelectLabel = { label ->
                            selectedLabel.value = label
                            saveLabels()
                        },
                        labels = labels,
                        cameraLensLabel = cameraLensLabel.value,
                        onAddLabel = { newLabel ->
                            val trimmed = newLabel.trim()
                            if (trimmed.isNotEmpty() && !labels.contains(trimmed)) {
                                labels.add(trimmed)
                                selectedLabel.value = trimmed
                                saveLabels()
                            }
                        },
                        onDeleteCurrentLabel = {
                            if (labels.size > 1) {
                                val current = selectedLabel.value
                                labels.remove(current)
                                selectedLabel.value = labels.first()
                                saveLabels()
                            } else {
                                Toast.makeText(this, "At least one label must remain", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onToggleCamera = {
                            toggleCamera()
                        },
                        onSaveClick = {
                            takePhoto(selectedLabel.value)
                        },
                        onPreviewReady = { pv ->
                            previewView = pv
                            startCamera()
                        }
                    )
                }
            }
        }
    }

    private fun saveLabels() {
        val sharedPref = getSharedPreferences("CameraPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("labels", labels.joinToString(","))
            putString("selectedLabel", selectedLabel.value)
            apply()
        }
    }

    private fun loadLabels() {
        val sharedPref = getSharedPreferences("CameraPrefs", Context.MODE_PRIVATE)
        val savedLabels = sharedPref.getString("labels", null)
        val savedSelected = sharedPref.getString("selectedLabel", null)

        if (savedLabels != null) {
            labels.clear()
            labels.addAll(savedLabels.split(","))
        }

        if (savedSelected != null && labels.contains(savedSelected)) {
            selectedLabel.value = savedSelected
        } else if (labels.isNotEmpty()) {
            selectedLabel.value = labels.first()
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    currentCameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to start camera: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto(label: String) {
        val imageCapture = imageCapture ?: return

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(System.currentTimeMillis())

        // Use externalMediaDirs to align with readme's directory structure example
        val baseDir = externalMediaDirs.firstOrNull() ?: getExternalFilesDir(null)
        val outputDir = File(baseDir, "output/Camera/$label")

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val photoFile = File(outputDir, "image_$timeStamp.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(
                        this@CameraActivity,
                        "Saved: ${photoFile.absolutePath}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity,
                        "Save failed: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    private fun toggleCamera() {
        currentCameraSelector =
            if (currentCameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                cameraLensLabel.value = "Front Camera"
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                cameraLensLabel.value = "Back Camera"
                CameraSelector.DEFAULT_BACK_CAMERA
            }

        startCamera()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    selectedLabel: String,
    onSelectLabel: (String) -> Unit,
    labels: List<String>,
    cameraLensLabel: String,
    onAddLabel: (String) -> Unit,
    onDeleteCurrentLabel: () -> Unit,
    onToggleCamera: () -> Unit,
    onSaveClick: () -> Unit,
    onPreviewReady: (PreviewView) -> Unit
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Image Collection",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Current Category: $selectedLabel",
            style = MaterialTheme.typography.titleMedium
        )

        var newLabelText by remember { mutableStateOf("") }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = newLabelText,
            onValueChange = { newLabelText = it },
            label = { Text("Add Category") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            Button(
                onClick = {
                    onAddLabel(newLabelText)
                    newLabelText = ""
                }
            ) {
                Text("Add")
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onDeleteCurrentLabel,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete Selected")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Select Label for Classification:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // Tailored labeling UI for object classification using FlowRow
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            labels.forEach { label ->
                val isSelected = label == selectedLabel
                Button(
                    onClick = { onSelectLabel(label) },
                    colors = if (isSelected) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors()
                ) {
                    Text(label)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Camera Preview ($cameraLensLabel)",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        AndroidView(
            factory = { context ->
                PreviewView(context).also {
                    onPreviewReady(it)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        Row {
            Button(onClick = onToggleCamera) {
                Text("Flip Camera")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onSaveClick,
                modifier = Modifier.height(56.dp).weight(1f)
            ) {
                Text("Capture & Save Image")
            }
        }
    }
}
