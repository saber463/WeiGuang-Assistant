package com.weiguangchangxing.weiguang_plus.core.hardware

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager

data class HardwareCapability(
    val hasVibrator: Boolean = false,
    val hasAmplitudeControl: Boolean = false,
    val hasLinearMotor: Boolean = false,
    val hasFlashlight: Boolean = false,
    val hasNotificationLed: Boolean = false,
    val hasScreenFlash: Boolean = false,
    val screenWidth: Int = 0,
    val screenHeight: Int = 0,
    val manufacturer: String = "",
    val model: String = "",
    val sdkVersion: Int = 0
)

object HardwareDetector {
    fun detectHardwareCapabilities(context: Context): HardwareCapability {
        val vibrator = getSystemVibrator(context)
        val hasVibrator = vibrator?.hasVibrator() == true
        val hasAmplitudeControl = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.hasAmplitudeControl() == true
        } else {
            false
        }
        val hasLinearMotor = detectLinearMotor(context)
        val hasFlashlight = detectFlashlight(context)
        val hasNotificationLed = detectNotificationLed()
        val hasScreenFlash = detectScreenFlash(context)

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = android.util.DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)

        return HardwareCapability(
            hasVibrator = hasVibrator,
            hasAmplitudeControl = hasAmplitudeControl,
            hasLinearMotor = hasLinearMotor,
            hasFlashlight = hasFlashlight,
            hasNotificationLed = hasNotificationLed,
            hasScreenFlash = hasScreenFlash,
            screenWidth = displayMetrics.widthPixels,
            screenHeight = displayMetrics.heightPixels,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            sdkVersion = Build.VERSION.SDK_INT
        )
    }

    private fun getSystemVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun detectLinearMotor(context: Context): Boolean {
        val vibrator = getSystemVibrator(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return vibrator?.hasVibrator() == true
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.hasAmplitudeControl() == true
        } else {
            false
        }
    }

    private fun detectFlashlight(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    private fun detectNotificationLed(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    }

    private fun detectScreenFlash(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_SCREEN_PORTRAIT)
    }

    fun adaptVibrationForDevice(
        capability: HardwareCapability,
        baseTimings: LongArray,
        baseAmplitudes: IntArray
    ): Pair<LongArray, IntArray> {
        if (!capability.hasVibrator) {
            return Pair(longArrayOf(0L), intArrayOf(0))
        }

        return when {
            capability.hasAmplitudeControl && capability.hasLinearMotor -> {
                Pair(baseTimings, baseAmplitudes)
            }
            capability.hasAmplitudeControl -> {
                val reducedAmplitudes = baseAmplitudes.map { amplitude ->
                    (amplitude * 0.8).toInt().coerceIn(1, 255)
                }.toIntArray()
                Pair(baseTimings, reducedAmplitudes)
            }
            else -> {
                val simplifiedTimings = simplifyTimings(baseTimings)
                Pair(simplifiedTimings, IntArray(0))
            }
        }
    }

    private fun simplifyTimings(timings: LongArray): LongArray {
        if (timings.size <= 4) return timings

        val simplified = mutableListOf<Long>(0L)
        var i = 1
        while (i < timings.size) {
            val duration = timings[i]
            if (duration > 200) {
                simplified.add(duration)
                if (i + 1 < timings.size) {
                    simplified.add(timings[i + 1].coerceAtMost(100))
                }
            }
            i += 2
        }

        return simplified.toLongArray()
    }

    fun getDeviceAdaptationTips(capability: HardwareCapability): List<String> {
        val tips = mutableListOf<String>()

        if (!capability.hasVibrator) {
            tips.add("当前设备不支持震动，将使用屏幕闪烁作为主要提醒方式")
        } else if (!capability.hasAmplitudeControl) {
            tips.add("当前设备不支持震动强度调节，将使用预设震动模式")
        }

        if (!capability.hasFlashlight && !capability.hasNotificationLed && !capability.hasScreenFlash) {
            tips.add("当前设备不支持灯光提醒，将使用震动和屏幕通知")
        }

        when (capability.manufacturer.lowercase()) {
            "huawei" -> tips.add("华为设备：支持HiTouch和指关节截屏增强提醒")
            "xiaomi" -> tips.add("小米设备：支持MIUI通知增强和闪光灯提醒")
            "oppo" -> tips.add("OPPO设备：支持ColorOS通知增强")
            "vivo" -> tips.add("vivo设备：支持FuntouchOS通知增强")
        }

        return tips
    }
}
