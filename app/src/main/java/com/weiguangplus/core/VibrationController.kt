package com.weiguangplus.core

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.weiguangplus.data.model.VibrationEvent
import com.weiguangplus.data.model.VibrationPattern
import com.weiguangplus.data.model.VibrationPatterns

/**
 * 振动控制器（object 单例）
 *
 * 为各提醒场景提供统一的振动能力，与 [FlashlightController] 并行对称设计：
 * FlashlightController 管"看得见的提醒"（闪光灯），VibrationController 管
 * "触得到的提醒"（振动）——在静音/睡眠场景下振动是唯一可靠的感知通道。
 *
 * 兼容性（关键设计决策）：
 *  - minSdk 24，需同时兼容 Android 8.0 (API 26) 前后：
 *    Android 8.0+ 使用 `VibratorManager`（新增系统服务）获取默认 Vibrator；
 *    低版本保留旧的 `Vib.getSystemService(Vibrator::class.java)` 路径。
 *  - 带振幅的 `createWaveform(timings, amplitudes)` 重载在 Android 26+ 才可用，
 *    已按 API 分叉，低版本退化为纯时长波形（无振幅，非致命）。
 *  - VIBRATE 为普通权限，已在 AndroidManifest 声明，无需运行时申请。
 *
 * 使用方式：在 `WeiguangApplication.onCreate()` 调用 `init(context)` 一次，
 * 后续通过 `vibratePattern(event)` 按事件类型触发对应波形。
 */
object VibrationController {

    /** 振动设备是否已初始化且当前设备支持振动 */
    var isVibratorAvailable: Boolean = false
        private set

    private var vibrator: Vibrator? = null

    /** 设备是否支持"强度可调"的远程波形（Android 8.0 引入） */
    private val supportsAmplitude: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    /**
     * 初始化（在 Application.onCreate 中调用一次）
     *
     * 分别探测 Android 8.0 前后的 Vibrator 获取路径：
     *  - API 26+ : 通过 VibratorManager 获取（系统级唯一振动器）
     *  - 旧版本    : 直接取 VIBRATOR_SERVICE
     */
    fun init(context: Context) {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        // hasVibrator() 表示物理振动马达是否存在；无马达设备不做无意义振动
        isVibratorAvailable = vibrator?.hasVibrator() ?: false
    }

    /**
     * 按事件类型触发预设振动波形
     *
     * @param event 事件类型。会解析出其默认波形档案再执行
     */
    fun vibratePattern(event: VibrationEvent) {
        vibratePattern(VibrationPatterns.forEvent(event))
    }

    /**
     * 执行指定振动波形
     *
     * @param pattern 波形参数（timings / amplitudes / repeat）
     */
    fun vibratePattern(pattern: VibrationPattern) {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return

        try {
            if (supportsAmplitude) {
                // Android 8.0+ 支持带振幅的精确波形，能区分"强连续"与"轻短振"
                vib.vibrate(
                    VibrationEffect.createWaveform(
                        pattern.timings,
                        pattern.amplitudes,
                        pattern.repeat
                    )
                )
            } else {
                // 低版本仅支持纯时长波形（无强度控制），退化为均匀振动
                @Suppress("DEPRECATION")
                vib.vibrate(pattern.timings, pattern.repeat)
            }
        } catch (e: Exception) {
            // 波形可能被 OEM 系统拒绝（个别机型优化），静默忽略避免崩溃
        }
    }

    /**
     * 测试/预览某组波形（供设置页"测试震动"按钮调用）
     */
    fun testPattern(pattern: VibrationPattern) {
        vibratePattern(pattern)
    }

    /**
     * 停止当前振动
     */
    fun cancel() {
        vibrator?.cancel()
    }
}