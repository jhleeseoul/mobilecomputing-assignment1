package com.example.assignment1

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.assignment1.ui.theme.Assignment1Theme

class InertialActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var magnetometer: Sensor? = null

    private val accelX = mutableStateOf(0f)
    private val accelY = mutableStateOf(0f)
    private val accelZ = mutableStateOf(0f)

    private val gyroX = mutableStateOf(0f)
    private val gyroY = mutableStateOf(0f)
    private val gyroZ = mutableStateOf(0f)

    private val magX = mutableStateOf(0f)
    private val magY = mutableStateOf(0f)
    private val magZ = mutableStateOf(0f)

    private val isCapturing = mutableStateOf(false)
    private val selectedLabel = mutableStateOf("Sitting")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        setContent {
            Assignment1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    InertialScreen(
                        modifier = Modifier.padding(innerPadding),
                        selectedLabel = selectedLabel.value,
                        isCapturing = isCapturing.value,

                        accelX = accelX.value,
                        accelY = accelY.value,
                        accelZ = accelZ.value,

                        gyroX = gyroX.value,
                        gyroY = gyroY.value,
                        gyroZ = gyroZ.value,

                        magX = magX.value,
                        magY = magY.value,
                        magZ = magZ.value,

                        hasAccelerometer = accelerometer != null,
                        hasGyroscope = gyroscope != null,
                        hasMagnetometer = magnetometer != null,

                        onSelectLabel = { label ->
                            selectedLabel.value = label
                        },
                        onStartClick = {
                            isCapturing.value = true
                        },
                        onStopClick = {
                            isCapturing.value = false
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }

        gyroscope?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }

        magnetometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelX.value = event.values[0]
                accelY.value = event.values[1]
                accelZ.value = event.values[2]
            }

            Sensor.TYPE_GYROSCOPE -> {
                gyroX.value = event.values[0]
                gyroY.value = event.values[1]
                gyroZ.value = event.values[2]
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                magX.value = event.values[0]
                magY.value = event.values[1]
                magZ.value = event.values[2]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 지금은 비워둬도 됨
    }
}

@androidx.compose.runtime.Composable
fun InertialScreen(
    modifier: Modifier = Modifier,
    selectedLabel: String,
    isCapturing: Boolean,

    accelX: Float,
    accelY: Float,
    accelZ: Float,

    gyroX: Float,
    gyroY: Float,
    gyroZ: Float,

    magX: Float,
    magY: Float,
    magZ: Float,

    hasAccelerometer: Boolean,
    hasGyroscope: Boolean,
    hasMagnetometer: Boolean,

    onSelectLabel: (String) -> Unit,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    val labels = listOf("Sitting", "Standing", "Running")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Inertial Sensor Collection",
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

        Row {
            Button(onClick = onStartClick) {
                Text("Start Capture")
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(onClick = onStopClick) {
                Text("Stop Capture")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isCapturing) "Status: Capturing" else "Status: Idle",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Sensor Availability", style = MaterialTheme.typography.titleMedium)
        Text("Accelerometer: ${if (hasAccelerometer) "Available" else "Not Available"}")
        Text("Gyroscope: ${if (hasGyroscope) "Available" else "Not Available"}")
        Text("Magnetometer: ${if (hasMagnetometer) "Available" else "Not Available"}")

        Spacer(modifier = Modifier.height(24.dp))

        Text("Accelerometer", style = MaterialTheme.typography.titleMedium)
        Text("X: ${"%.2f".format(accelX)}")
        Text("Y: ${"%.2f".format(accelY)}")
        Text("Z: ${"%.2f".format(accelZ)}")

        Spacer(modifier = Modifier.height(20.dp))

        Text("Gyroscope", style = MaterialTheme.typography.titleMedium)
        Text("X: ${"%.2f".format(gyroX)}")
        Text("Y: ${"%.2f".format(gyroY)}")
        Text("Z: ${"%.2f".format(gyroZ)}")

        Spacer(modifier = Modifier.height(20.dp))

        Text("Magnetometer", style = MaterialTheme.typography.titleMedium)
        Text("X: ${"%.2f".format(magX)}")
        Text("Y: ${"%.2f".format(magY)}")
        Text("Z: ${"%.2f".format(magZ)}")
    }
}