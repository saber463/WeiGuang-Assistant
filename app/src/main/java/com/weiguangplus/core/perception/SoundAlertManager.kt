package com.weiguangplus.core.perception

import android.util.Log
import com.weiguangplus.core.FlashlightController
import com.weiguangplus.core.VibrationController
import com.weiguangplus.data.model.VibrationEvent

/**
 * 声音提醒协调器（多模态提醒去抖）
 *
 * 职责：当 [AmbientSoundMonitor] 检测到已训练声音时，协调触发多通道提醒：
 *  - 振动（VibrationController，触觉通道）
 *  - 闪光灯（FlashlightController，视觉通道）
 *  - 状态回调（可扩展接入弹窗通知）
 *
 * 设计决策（WHY）：
 *  - 通过单例持有"去抖计时"，避免声音在短时间内被重复触发提醒造成打扰
 *    （如门铃响个不停只提醒一次）。去抖窗口默认 8 秒。
 *  - 不直接耦合 Android UI，通过 [onAlert] 回调让上层（UI/服务）决定
 *    是否弹窗通知，保持核心逻辑可独立测试。
 */
object SoundAlertManager {

    private const val TAG = "SoundAlertManager"

    /** 两次提醒之间的最小间隔（ms），防止同一个声音连续触发 */
    private const val DEBOUNCE_MS = 8_000L

    /** 上次触发提醒的时间戳 */
    private var lastAlertAt: Long = 0L

    /** 回调：当某个声音命中时通知上层（例如更新 UI 或发通知） */
    var onAlert: ((soundName: String) -> Unit)? = null

    /** 是否启用振动提醒（可被设置页开关控制） */
    var vibrationEnabled: Boolean = true

    /** 是否启用闪光灯提醒（可被设置页开关控制） */
    var flashEnabled: Boolean = true

    /**
     * 触发一次声音提醒（带去抖）
     *
     * @param soundName 命中的声音名称
     * @return 是否实际触发了提醒（false 表示处于去抖窗口内被抑制）
     */
    fun onSoundDetected(soundName: String): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastAlertAt < DEBOUNCE_MS) {
            Log.d(TAG, "声音 [$soundName] 命中但在去抖窗口内，忽略")
            return false
        }
        lastAlertAt = now

        Log.d(TAG, "检测到自定义声音: $soundName")

        // 振动反馈：使用"自定义"档案的振动波形
        if (vibrationEnabled && VibrationController.isVibratorAvailable) {
            VibrationController.vibratePattern(VibrationEvent.CUSTOM)
        }

        // 闪光灯闪烁：双通道互补（静音场景振动、昏暗场景闪光）
        if (flashEnabled && FlashlightController.isFlashAvailable()) {
            FlashlightController.blink(3, 300, 200)
        }

        // 通知上层（弹窗通知等在 UI 层实现）
        onAlert?.invoke(soundName)
        return true
    }

    /** 重置去抖状态（声音设置页训练新样本后调用） */
    fun resetDebounce() {
        lastAlertAt = 0L
    }
}