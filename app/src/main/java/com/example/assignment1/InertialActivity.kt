package com.example.assignment1

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.assignment1.ui.theme.Assignment1Theme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.Color
import android.view.ViewGroup
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

data class SensorSample(
    val timestamp: Long,
    val x: Float,
    val y: Float,
    val z: Float
)

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

    private val accelFrequency = mutableStateOf(0f)
    private var lastAccelTimestamp: Long = 0L

    private val isCapturing = mutableStateOf(false)
    private val selectedLabel = mutableStateOf("Sitting")

    private val accelSamples = mutableListOf<SensorSample>()
    private val gyroSamples = mutableListOf<SensorSample>()
    private val magSamples = mutableListOf<SensorSample>()

    private val graphPointsX = mutableStateOf(listOf<Entry>())
    private val graphPointsY = mutableStateOf(listOf<Entry>())
    private val graphPointsZ = mutableStateOf(listOf<Entry>())

    private var graphIndex = 0f
    private val maxGraphPoints = 50

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

                        accelFrequency = accelFrequency.value,
                        graphX = graphPointsX.value,
                        graphY = graphPointsY.value,
                        graphZ = graphPointsZ.value,
                        hasAccelerometer = accelerometer != null,
                        hasGyroscope = gyroscope != null,
                        hasMagnetometer = magnetometer != null,

                        onSelectLabel = { label ->
                            selectedLabel.value = label
                        },
                        onStartClick = {
                            startCapture()
                        },
                        onStopClick = {
                            stopCaptureAndSave()
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

        val currentTime = System.currentTimeMillis()

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelX.value = event.values[0]
                accelY.value = event.values[1]
                accelZ.value = event.values[2]

                if (lastAccelTimestamp != 0L) {
                    val diff = currentTime - lastAccelTimestamp
                    if (diff > 0) {
                        accelFrequency.value = 1000f / diff.toFloat()
                    }
                }
                lastAccelTimestamp = currentTime

                updateGraph(event.values[0], event.values[1], event.values[2])

                if (isCapturing.value) {
                    accelSamples.add(
                        SensorSample(
                            timestamp = currentTime,
                            x = event.values[0],
                            y = event.values[1],
                            z = event.values[2]
                        )
                    )
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                gyroX.value = event.values[0]
                gyroY.value = event.values[1]
                gyroZ.value = event.values[2]

                if (isCapturing.value) {
                    gyroSamples.add(
                        SensorSample(
                            timestamp = currentTime,
                            x = event.values[0],
                            y = event.values[1],
                            z = event.values[2]
                        )
                    )
                }
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                magX.value = event.values[0]
                magY.value = event.values[1]
                magZ.value = event.values[2]

                if (isCapturing.value) {
                    magSamples.add(
                        SensorSample(
                            timestamp = currentTime,
                            x = event.values[0],
                            y = event.values[1],
                            z = event.values[2]
                        )
                    )
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 지금은 비워둬도 됨
    }

    private fun startCapture() {
        accelSamples.clear()
        gyroSamples.clear()
        magSamples.clear()

        graphPointsX.value = emptyList()
        graphPointsY.value = emptyList()
        graphPointsZ.value = emptyList()
        graphIndex = 0f

        isCapturing.value = true

        Toast.makeText(this, "Capture started", Toast.LENGTH_SHORT).show()
    }

    private fun stopCaptureAndSave() {
        if (!isCapturing.value) return

        isCapturing.value = false

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())

        val label = selectedLabel.value
        val outputDir = File(
            getExternalFilesDir(null),
            "output/Inertial/$label"
        )

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        try {
            saveSamplesToCsv(File(outputDir, "accel_$timeStamp.csv"), accelSamples)
            saveSamplesToCsv(File(outputDir, "gyro_$timeStamp.csv"), gyroSamples)
            saveSamplesToCsv(File(outputDir, "mag_$timeStamp.csv"), magSamples)

            Toast.makeText(
                this,
                "Saved to: ${outputDir.absolutePath}",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Save failed: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveSamplesToCsv(file: File, samples: List<SensorSample>) {
        file.bufferedWriter().use { writer ->
            writer.write("timestamp,x,y,z\n")
            for (sample in samples) {
                writer.write("${sample.timestamp},${sample.x},${sample.y},${sample.z}\n")
            }
        }
    }

    private fun updateGraph(x: Float, y: Float, z: Float) {
        graphIndex += 1f

        val newXList = (graphPointsX.value + Entry(graphIndex, x)).takeLast(maxGraphPoints)
        val newYList = (graphPointsY.value + Entry(graphIndex, y)).takeLast(maxGraphPoints)
        val newZList = (graphPointsZ.value + Entry(graphIndex, z)).takeLast(maxGraphPoints)

        graphPointsX.value = newXList
        graphPointsY.value = newYList
        graphPointsZ.value = newZList
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

    accelFrequency: Float,
    graphX: List<Entry>,
    graphY: List<Entry>,
    graphZ: List<Entry>,
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

        Text("Acceleration Table", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Acceleration Graph", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        AccelerationChart(
            xEntries = graphX,
            yEntries = graphY,
            zEntries = graphZ
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Frequency: ${"%.2f".format(accelFrequency)} Hz")

        Spacer(modifier = Modifier.height(8.dp))

        SensorTable(
            x = accelX,
            y = accelY,
            z = accelZ
        )

        Spacer(modifier = Modifier.height(24.dp))

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

@androidx.compose.runtime.Composable
fun SensorTable(
    x: Float,
    y: Float,
    z: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, androidx.compose.ui.graphics.Color.Gray)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            TableCell("Axis", 1f, true)
            TableCell("Value", 1f, true)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            TableCell("X", 1f, false)
            TableCell("%.2f".format(x), 1f, false)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            TableCell("Y", 1f, false)
            TableCell("%.2f".format(y), 1f, false)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            TableCell("Z", 1f, false)
            TableCell("%.2f".format(z), 1f, false)
        }
    }
}

@androidx.compose.runtime.Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean
) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .border(1.dp, androidx.compose.ui.graphics.Color.Gray)
            .padding(12.dp),
        style = if (isHeader) {
            MaterialTheme.typography.titleMedium
        } else {
            MaterialTheme.typography.bodyLarge
        }
    )
}

@androidx.compose.runtime.Composable
fun AccelerationChart(
    xEntries: List<Entry>,
    yEntries: List<Entry>,
    zEntries: List<Entry>
) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            LineChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)

                axisRight.isEnabled = false

                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)

                legend.isEnabled = true
            }
        },
        update = { chart ->
            val dataSetX = LineDataSet(xEntries, "Accel X").apply {
                color = Color.RED
                setDrawCircles(false)
                lineWidth = 2f
            }

            val dataSetY = LineDataSet(yEntries, "Accel Y").apply {
                color = Color.GREEN
                setDrawCircles(false)
                lineWidth = 2f
            }

            val dataSetZ = LineDataSet(zEntries, "Accel Z").apply {
                color = Color.BLUE
                setDrawCircles(false)
                lineWidth = 2f
            }

            chart.data = LineData(dataSetX, dataSetY, dataSetZ)
            chart.invalidate()
        }
    )
}