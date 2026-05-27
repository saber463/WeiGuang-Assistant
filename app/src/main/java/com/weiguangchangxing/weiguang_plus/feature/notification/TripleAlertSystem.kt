package com.weiguangchangxing.weiguang_plus.feature.notification

import android.content.Context
import android.hardware.Sensor
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.view.Display
import android.view.Gravity
import com.weiguangchangxing.weiguang_plus.core.alert.PocketAlertManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TripleAlertState(
    val vibrationActive: Boolean = false,
    val soundActive: Boolean = false,
    val lightActive: Boolean = false,
    val currentAlertLevel: AlertLevel = AlertLevel.NONE,
    val lastAlertTime: Long = 0
)

enum class AlertLevel {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
    EMERGENCY
}

class TripleAlertSystem(private val context: Context) {

    private val _state = MutableStateFlow(TripleAlertState())
    val state: StateFlow<TripleAlertState> = _state.asStateFlow()

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private val cameraManager: CameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private val audioController = AudioManagerController(context)

    private var windowManager: WindowManager? = null
    private var flashOverlay: FlashOverlayView? = null

    private var isFlashlightOn = false
    private var flashlightThread: Thread? = null

    private val pocketAlertManager = PocketAlertManager(context)
    private var isPocketModeEnabled = true

    fun triggerTripleAlert(level: AlertLevel = AlertLevel.HIGH) {
        _state.value = _state.value.copy(
            currentAlertLevel = level,
            lastAlertTime = System.currentTimeMillis()
        )

        triggerVibration(level)
        triggerSound(level)
        triggerLight(level)

        if (isPocketModeEnabled && level.ordinal >= AlertLevel.HIGH.ordinal) {
            val alertDuration = when (level) {
                AlertLevel.EMERGENCY -> 30000L
                AlertLevel.HIGH -> 15000L
                else -> 8000L
            }
            pocketAlertManager.triggerPocketAlert(alertDuration)
        }
    }

    fun startPocketModeMonitoring() {
        if (isPocketModeEnabled) {
            pocketAlertManager.startProximityMonitoring()
        }
    }

    fun stopPocketModeMonitoring() {
        pocketAlertManager.stopProximityMonitoring()
    }

    fun setPocketModeEnabled(enabled: Boolean) {
        isPocketModeEnabled = enabled
        if (enabled) pocketAlertManager.startProximityMonitoring()
        else pocketAlertManager.stopProximityMonitoring()
    }

    @Suppress("DEPRECATION")
    fun triggerVibration(level: AlertLevel) {
        _state.value = _state.value.copy(vibrationActive = true)

        val pattern = getVibrationPattern(level)
        val amplitudes = getVibrationAmplitudes(level, pattern.size)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (vibrator.hasAmplitudeControl()) {
                    val effect = VibrationEffect.createWaveform(pattern, amplitudes, 0)
                    vibrator.vibrate(effect)
                } else {
                    vibrator.vibrate(pattern, 0)
                }
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
        }

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            _state.value = _state.value.copy(vibrationActive = false)
        }, calculateVibrationDuration(pattern))
    }

    private fun getVibrationPattern(level: AlertLevel): LongArray {
        return when (level) {
            AlertLevel.EMERGENCY -> longArrayOf(0, 800, 100, 800, 100, 800, 200, 400, 200, 400, 200)
            AlertLevel.HIGH -> longArrayOf(0, 500, 150, 500, 150, 500)
            AlertLevel.MEDIUM -> longArrayOf(0, 300, 200, 300, 200)
            AlertLevel.LOW -> longArrayOf(0, 200, 300)
            AlertLevel.NONE -> longArrayOf(0)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun getVibrationAmplitudes(level: AlertLevel, patternSize: Int): IntArray {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return IntArray(0)

        return when (level) {
            AlertLevel.EMERGENCY -> intArrayOf(0, 255, 0, 255, 0, 255, 0, 200, 0, 200, 0)
            AlertLevel.HIGH -> intArrayOf(0, 220, 0, 220, 0, 220)
            AlertLevel.MEDIUM -> intArrayOf(0, 180, 0, 180, 0)
            AlertLevel.LOW -> intArrayOf(0, 150, 0)
            AlertLevel.NONE -> intArrayOf(0)
        }
    }

    private fun calculateVibrationDuration(pattern: LongArray): Long {
        return pattern.sum()
    }

    fun triggerSound(level: AlertLevel) {
        _state.value = _state.value.copy(soundActive = true)

        audioController.maximizeAllCriticalVolumes()
        audioController.requestAudioFocus()

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            audioController.abandonAudioFocus()
            _state.value = _state.value.copy(soundActive = false)
        }, getSoundDuration(level))
    }

    private fun getSoundDuration(level: AlertLevel): Long {
        return when (level) {
            AlertLevel.LOW -> 1000
            AlertLevel.MEDIUM -> 2000
            AlertLevel.HIGH -> 3000
            AlertLevel.EMERGENCY -> 5000
            AlertLevel.NONE -> 0
        }
    }

    fun triggerLight(level: AlertLevel) {
        _state.value = _state.value.copy(lightActive = true)

        val flashFrequency = getFlashFrequency(level)
        val flashDuration = getLightDuration(level)

        startFlashlightBlink(flashFrequency, flashDuration)
        startScreenFlash(level, flashDuration)
    }

    private fun getFlashFrequency(level: AlertLevel): Long {
        return when (level) {
            AlertLevel.LOW -> 500
            AlertLevel.MEDIUM -> 300
            AlertLevel.HIGH -> 200
            AlertLevel.EMERGENCY -> 100
            AlertLevel.NONE -> 0
        }
    }

    private fun getLightDuration(level: AlertLevel): Long {
        return when (level) {
            AlertLevel.LOW -> 1000
            AlertLevel.MEDIUM -> 2000
            AlertLevel.HIGH -> 3000
            AlertLevel.EMERGENCY -> 5000
            AlertLevel.NONE -> 0
        }
    }

    private fun startFlashlightBlink(frequency: Long, duration: Long) {
        if (isFlashlightOn) {
            stopFlashlight()
        }

        flashlightThread = Thread {
            val startTime = System.currentTimeMillis()
            isFlashlightOn = true

            while (isFlashlightOn && (System.currentTimeMillis() - startTime) < duration) {
                try {
                    setFlashlight(true)
                    Thread.sleep(frequency / 2)
                    setFlashlight(false)
                    Thread.sleep(frequency / 2)
                } catch (e: InterruptedException) {
                    break
                } catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
            }

            isFlashlightOn = false
            setFlashlight(false)
        }

        flashlightThread?.start()
    }

    private fun setFlashlight(on: Boolean) {
        try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }

            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, on)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun stopFlashlight() {
        isFlashlightOn = false
        flashlightThread?.interrupt()
        setFlashlight(false)
    }

    private fun startScreenFlash(level: AlertLevel, duration: Long) {
        try {
            if (flashOverlay == null) {
                windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                flashOverlay = FlashOverlayView(context)
            }
            // 安全检查是否已经添加
            try {
                val params = flashOverlay?.layoutParams
                if (flashOverlay?.parent == null && windowManager != null) {
                    windowManager?.addView(flashOverlay, params)
                }
            } catch (ignored: Exception) {}
            
            val flashColor = getFlashColor(level)
            flashOverlay?.startFlashing(flashColor, duration)
        } catch (e: SecurityException) {
        } catch (e: android.view.WindowManager.BadTokenException) {
        } catch (e: Exception) {
        }
    }

    private fun getFlashColor(level: AlertLevel): Int {
        return when (level) {
            AlertLevel.LOW -> android.graphics.Color.argb(100, 255, 255, 0)
            AlertLevel.MEDIUM -> android.graphics.Color.argb(150, 255, 165, 0)
            AlertLevel.HIGH -> android.graphics.Color.argb(200, 255, 69, 0)
            AlertLevel.EMERGENCY -> android.graphics.Color.argb(255, 255, 0, 0)
            AlertLevel.NONE -> android.graphics.Color.TRANSPARENT
        }
    }

    fun stopAllAlerts() {
        vibrator.cancel()
        pocketAlertManager.stopAlert()
        stopFlashlight()
        flashOverlay?.stopFlashing()
        audioController.abandonAudioFocus()

        _state.value = TripleAlertState()
    }

    fun setVibrationOnly(level: AlertLevel) {
        _state.value = _state.value.copy(
            soundActive = false,
            lightActive = false
        )
        triggerVibration(level)
    }

    fun setSoundOnly(level: AlertLevel) {
        _state.value = _state.value.copy(
            vibrationActive = false,
            lightActive = false
        )
        triggerSound(level)
    }

    fun setLightOnly(level: AlertLevel) {
        _state.value = _state.value.copy(
            vibrationActive = false,
            soundActive = false
        )
        triggerLight(level)
    }

    fun release() {
        stopAllAlerts()
        flashOverlay = null
    }

    fun isHardwareCapable(): HardwareCapabilities {
        val overlayAvailable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
        return HardwareCapabilities(
            hasVibrator = vibrator.hasVibrator(),
            hasAmplitudeControl = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.hasAmplitudeControl()
            } else {
                false
            },
            hasFlashlight = isFlashlightAvailable(),
            hasScreenFlash = overlayAvailable,
            overlayAvailable = overlayAvailable
        )
    }

    private fun isFlashlightAvailable(): Boolean {
        return try {
            cameraManager.cameraIdList.any { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            false
        }
    }
}

data class HardwareCapabilities(
    val hasVibrator: Boolean,
    val hasAmplitudeControl: Boolean,
    val hasFlashlight: Boolean,
    val hasScreenFlash: Boolean,
    val overlayAvailable: Boolean
)

class FlashOverlayView(context: Context) : android.view.View(context) {
    private var isFlashing = false
    private var flashColor: Int = android.graphics.Color.RED
    private val paint = android.graphics.Paint()
    private val display: Display?
    private var layoutParams: LayoutParams

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            display = (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    LayoutParams.TYPE_SYSTEM_OVERLAY
                },
                LayoutParams.FLAG_NOT_FOCUSABLE or
                        LayoutParams.FLAG_NOT_TOUCHABLE or
                        LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                android.graphics.PixelFormat.TRANSLUCENT
            )
        } else {
            @Suppress("DEPRECATION")
            display = (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay
            @Suppress("DEPRECATION")
            layoutParams = LayoutParams(
                LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT,
                LayoutParams.TYPE_SYSTEM_OVERLAY,
                LayoutParams.FLAG_NOT_FOCUSABLE or
                        LayoutParams.FLAG_NOT_TOUCHABLE or
                        LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                android.graphics.PixelFormat.TRANSLUCENT
            )
        }
        layoutParams.gravity = Gravity.CENTER
    }

    fun startFlashing(color: Int, duration: Long) {
        flashColor = color
        isFlashing = true
        visibility = android.view.View.VISIBLE

        alpha = 0f
        animate()
            .alpha(1f)
            .setDuration(100)
            .withEndAction {
                postDelayed({
                    animate()
                        .alpha(0f)
                        .setDuration(100)
                        .withEndAction {
                            if (isFlashing) {
                                startFlashing(flashColor, duration - 200)
                            } else {
                                visibility = android.view.View.GONE
                            }
                        }
                        .start()
                }, duration - 200)
            }
            .start()
    }

    fun stopFlashing() {
        isFlashing = false
        visibility = android.view.View.GONE
        alpha = 0f
        // 安全移除View
        try {
            val wm = (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)
            if (parent != null && wm != null) {
                wm.removeView(this)
            }
        } catch (ignored: Exception) {}
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)
        paint.color = flashColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }
}
