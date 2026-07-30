package com.weiguangplus.core.emergency

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * 摇一摇检测器
 * 检测设备剧烈摇晃，用于触发 SOS 紧急求救
 */
object ShakeDetector : SensorEventListener {

    private const val SHAKE_THRESHOLD = 25f
    private const val SHAKE_INTERVAL_MS = 1000L
    private const val SHAKE_COUNT_TRIGGER = 3

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastShakeTime: Long = 0
    private var shakeCount: Int = 0
    private var onShakeTrigger: (() -> Unit)? = null
    private var isListening: Boolean = false

    fun start(context: Context, onTrigger: () -> Unit) {
        if (isListening) return
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        onShakeTrigger = onTrigger
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            isListening = true
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        isListening = false
        shakeCount = 0
        onShakeTrigger = null
    }

    fun isActive(): Boolean = isListening

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val acceleration = magnitude - SensorManager.GRAVITY_EARTH

        if (acceleration > SHAKE_THRESHOLD) {
            val now = System.currentTimeMillis()
            if (lastShakeTime + SHAKE_INTERVAL_MS < now) {
                shakeCount = 0
            }
            lastShakeTime = now
            shakeCount++

            if (shakeCount >= SHAKE_COUNT_TRIGGER) {
                shakeCount = 0
                onShakeTrigger?.invoke()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
