package com.weiguangplus.core

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.annotation.RequiresPermission

/**
 * 闪光灯控制器
 *
 * 使用 CameraManager.setTorchMode() 控制 LED 闪光灯。
 * 适用于听障人士的视觉提醒场景：来电闪烁、闹钟闪烁、SOS 警示。
 *
 * 兼容性：Android 6.0 (API 23) 及以上
 * 权限要求：android.permission.CAMERA
 */
object FlashlightController {

    /** 闪光灯是否亮起 */
    var isOn: Boolean = false
        private set

    private var cameraManager: CameraManager? = null
    private var torchCameraId: String? = null
    private var flashAvailable: Boolean = false

    /**
     * 初始化（在 Application.onCreate 中调用一次）
     */
    fun init(context: Context) {
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        torchCameraId = findTorchCamera()
        flashAvailable = torchCameraId != null
    }

    /**
     * 查找支持手电筒的摄像头 ID
     */
    private fun findTorchCamera(): String? {
        val manager = cameraManager ?: return null
        return try {
            manager.cameraIdList.firstOrNull { id ->
                val characteristics = manager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                // 优先选择后置摄像头（通常 cameraId = "0"）
                val isBackFacing = characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
                hasFlash && isBackFacing
            } ?: manager.cameraIdList.firstOrNull { id ->
                val characteristics = manager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
            }
        } catch (e: Exception) {
            // 某些设备可能不兼容 Camera2 API
            null
        }
    }

    /**
     * 检查设备是否支持闪光灯
     */
    fun isFlashAvailable(): Boolean = flashAvailable

    /**
     * 打开闪光灯
     * 需要 android.permission.CAMERA 权限
     */
    @RequiresPermission(android.Manifest.permission.CAMERA)
    fun turnOn(): Boolean {
        if (!flashAvailable || isOn) return false
        val manager = cameraManager ?: return false
        val cameraId = torchCameraId ?: return false

        return try {
            manager.setTorchMode(cameraId, true)
            isOn = true
            true
        } catch (e: Exception) {
            // 常见失败原因：
            // 1. 相机正被其他应用占用
            // 2. 权限不足
            // 3. 设备不支持（极少）
            false
        }
    }

    /**
     * 关闭闪光灯
     */
    fun turnOff(): Boolean {
        if (!isOn) return false
        val manager = cameraManager ?: return false
        val cameraId = torchCameraId ?: return false

        return try {
            manager.setTorchMode(cameraId, false)
            isOn = false
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 切换闪光灯状态
     */
    fun toggle(): Boolean {
        return if (isOn) turnOff() else turnOn()
    }

    /**
     * 闪烁（指定次数和间隔）
     *
     * @param times 闪烁次数
     * @param onMs 每次亮起时长（毫秒）
     * @param offMs 每次熄灭时长（毫秒）
     * @param callback 闪烁完成后的回调
     */
    fun blink(
        times: Int = 3,
        onMs: Long = 300,
        offMs: Long = 200,
        callback: (() -> Unit)? = null
    ) {
        Thread {
            repeat(times) {
                turnOn()
                Thread.sleep(onMs)
                turnOff()
                if (it < times - 1) Thread.sleep(offMs)
            }
            callback?.invoke()
        }.start()
    }

    /**
     * SOS 模式：快速高频闪烁
     */
    fun sosBlink(callback: (() -> Unit)? = null) {
        // SOS 摩斯码：··· - - - ···
        Thread {
            // 三个短闪
            repeat(3) { turnOn(); Thread.sleep(200); turnOff(); Thread.sleep(150) }
            Thread.sleep(300)
            // 三个长闪
            repeat(3) { turnOn(); Thread.sleep(600); turnOff(); Thread.sleep(150) }
            Thread.sleep(300)
            // 三个短闪
            repeat(3) { turnOn(); Thread.sleep(200); turnOff(); Thread.sleep(150) }
            callback?.invoke()
        }.start()
    }
}
