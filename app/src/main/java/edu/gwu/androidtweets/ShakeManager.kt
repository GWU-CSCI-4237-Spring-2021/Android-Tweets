package edu.gwu.androidtweets

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.pow
import kotlin.math.sqrt

class ShakeManager(context: Context) : SensorEventListener {
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var callback: (() -> Unit)? = null

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        Log.d("ShakeManager", "[X, Y, Z] = [$x, $y, $z]")

        val acceleration = sqrt(
            x.toDouble().pow(2.0) + y.toDouble().pow(2.0) + z.toDouble().pow(2.0)
        ) - SensorManager.GRAVITY_EARTH

        Log.d("ShakeManager", "[X, Y, Z] = [$x, $y, $z], Acceleration = $acceleration")

        if (acceleration > 4) {
            Log.d("ShakeManager", "Shake detected!")
            callback?.invoke()
        }
    }

    fun detectShakes(callback: () -> Unit) {
        this.callback = callback

        if (sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).isNotEmpty()) {
            val sensor: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            

            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
        } else {
            Log.d("ShakeManager", "Device does not have an accelerometer!")
        }
    }

    fun stopDetectingShakes() {
        callback = null
        sensorManager.unregisterListener(this)
    }
}