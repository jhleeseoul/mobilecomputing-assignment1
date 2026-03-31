package com.example.assignment1

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
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

        val outputDir = File(
            getExternalFilesDir(null),
            "output/Camera/$label"
        )

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
}

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    selectedLabel: String,
    onSelectLabel: (String) -> Unit,
    onSaveClick: () -> Unit,
    onPreviewReady: (PreviewView) -> Unit
) {
    val labels = listOf("Table", "Bookcase", "Bicycle")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Image Collection",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Selected Label: $selectedLabel",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            labels.forEach { label ->
                Button(
                    onClick = { onSelectLabel(label) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(label)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

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

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onSaveClick) {
            Text("Save Image")
        }
    }
}