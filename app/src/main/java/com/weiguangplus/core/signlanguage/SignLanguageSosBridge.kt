package com.weiguangplus.core.signlanguage

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.weiguangplus.core.emergency.SosManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

/**
 * 手语 SOS 紧急联动桥接
 *
 * 监听 SignLanguageEngine.sosGestureDetected，
 * 当连续检测到 SOS 紧急手势（握拳/手掌张开）时，
 * 自动触发 SOS 家属联动（短信 + 位置 + 闪光灯）。
 *
 * 安全机制：
 * - 冷却期：触发后 30 秒内不再重复触发
 * - 防误触：需连续检测到 SOS 手势达到阈值
 * - 用户可取消：弹窗倒计时 3 秒可取消
 */
object SignLanguageSosBridge {

    private const val TAG = "SignLanguageSosBridge"
    private const val COOLDOWN_MS = 30_000L // 30 秒冷却
    private const val CANCEL_WINDOW_MS = 3_000L // 3 秒取消窗口

    private var lastTriggerTime: Long = 0L
    private var isEnabled: Boolean = true
    private var scope: CoroutineScope? = null
    private var context: Context? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    /** SOS 触发回调（UI 层监听以显示倒计时/取消） */
    var onSosTriggered: ((countdownSeconds: Int) -> Unit)? = null
    var onSosCanceled: (() -> Unit)? = null
    var onSosCompleted: ((SosManager.SosResult) -> Unit)? = null

    /**
     * 启动 SOS 手语监听
     */
    fun start(context: Context) {
        this.context = context.applicationContext
        scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        scope?.launch {
            SignLanguageEngine.sosGestureDetected.collectLatest { detected ->
                if (detected && isEnabled) {
                    handleSosDetection(context)
                }
            }
        }
        Log.d(TAG, "SOS 手语监听已启动")
    }

    /**
     * 停止监听
     */
    fun stop() {
        scope?.cancel()
        scope = null
        Log.d(TAG, "SOS 手语监听已停止")
    }

    /**
     * 启用/禁用 SOS 联动
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    /**
     * 手动触发 SOS（跳过冷却）
     */
    fun triggerNow(context: Context) {
        executeSos(context, bypassCooldown = true)
    }

    /**
     * 取消当前 SOS 倒计时
     */
    fun cancel() {
        mainHandler.removeCallbacksAndMessages(null)
        onSosCanceled?.invoke()
        Log.d(TAG, "SOS 已取消")
    }

    // ─── 内部逻辑 ───

    private fun handleSosDetection(context: Context) {
        val now = System.currentTimeMillis()

        // 冷却检查
        if (now - lastTriggerTime < COOLDOWN_MS) {
            Log.d(TAG, "SOS 冷却中，距上次 ${(now - lastTriggerTime) / 1000}s")
            return
        }

        lastTriggerTime = now

        // 3 秒倒计时窗口
        onSosTriggered?.invoke(3)

        mainHandler.postDelayed({
            onSosTriggered?.invoke(2)
        }, 1000)

        mainHandler.postDelayed({
            onSosTriggered?.invoke(1)
        }, 2000)

        // 3 秒后执行 SOS
        mainHandler.postDelayed({
            executeSos(context, bypassCooldown = false)
        }, CANCEL_WINDOW_MS)
    }

    private fun executeSos(context: Context, bypassCooldown: Boolean) {
        if (!bypassCooldown) {
            val now = System.currentTimeMillis()
            if (now - lastTriggerTime < COOLDOWN_MS - CANCEL_WINDOW_MS) {
                return // 已被取消或冷却
            }
        }

        Log.w(TAG, "🚨 手语 SOS 触发！执行紧急联动...")

        SosManager.trigger(context) { result ->
            mainHandler.post {
                onSosCompleted?.invoke(result)
                SignLanguageEngine.resetSosGesture()
            }
        }
    }
}
