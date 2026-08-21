package com.weiguangplus

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.speech.tts.TextToSpeech
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import java.util.Locale

/**
 * 无障碍朗读服务（由空实现补全为可用功能）
 *
 * 面向听障/视障用户的辅助朗读：
 * - 收到系统通知（如来信、短信、闹钟提醒）时，用 TTS 朗读通知内容；
 * - 在可编辑文本中选中文字（TYPE_VIEW_TEXT_SELECTION_CHANGED）时，朗读所选中文字。
 *
 * 设计决策（WHY）：
 * - 使用独立的 TextToSpeech 而非应用内 TtsController 单例：无障碍服务与应用
 *   可能同时需要发声，独立引擎避免互相打断、彼此抢占队列。
 * - 事件过滤：仅处理通知变更与文本选中两类事件，避免 typeAllMask 下海量
 *   事件导致的无效处理和反复朗读。
 * - 通知去抖：同一通知在短时间内不重复朗读，防止系统重复推送造成的重复播报。
 * - 此服务需用户到"系统设置 → 无障碍"手动开启（Android 无障碍服务不允许
 *   应用内静默授予），本应用仅提供跳转引导（见 AccessibilityReadScreen）。
 */
class AccessibilityReadService : AccessibilityService() {

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    /** 上次朗读的通知 key（用于去抖）与时间 */
    private var lastNotifKey: String? = null
    private var lastNotifTime: Long = 0L

    companion object {
        private const val TAG = "AccessibilityRead"
        /** 同一通知去抖间隔：避免重复朗读同一通知 */
        private const val NOTIF_DEBOUNCE_MS = 1500L

        /** 检测无障碍朗读服务是否已开启 */
        fun isServiceEnabled(context: Context): Boolean {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE)
                as android.view.accessibility.AccessibilityManager
            if (!am.isEnabled) return false
            val expected = context.packageName + "/" +
                "com.weiguangplus.AccessibilityReadService"
            return am.getEnabledAccessibilityServiceList(
                android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_SPOKEN
            ).any { it.resolveInfo != null && it.id == expected }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // 初始化独立 TTS，中文播报
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.CHINESE)
                tts?.setSpeechRate(0.9f)
                ttsReady =
                    result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            // 系统通知到达 → 朗读通知内容
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                speakNotification(event)
            }
            // 文本选中变化 → 朗读所选文字
            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> {
                val selected = event.text?.joinToString(" ") { it.toString() }?.trim()
                if (!selected.isNullOrBlank()) {
                    speak(selected)
                }
            }
        }
    }

    override fun onInterrupt() {
        // 系统要求打断（如用户触摸），立即静音
        tts?.stop()
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }

    /** 处理通知事件，提取文本并去抖朗读 */
    private fun speakNotification(event: AccessibilityEvent) {
        val notif = event.parcelableData as? android.app.Notification ?: return
        // 组装可朗读文本：标题 + 内容
        val title = event.text?.joinToString(" ") { it.toString() }?.trim().orEmpty()
        val extraText = notif.extras?.getCharSequence(
            android.app.Notification.EXTRA_TEXT
        )?.toString().orEmpty()

        val content = listOf(title, extraText)
            .filter { it.isNotBlank() }
            .joinToString("，")
        if (content.isBlank()) return

        // 去抖：短时间同一内容不重复朗读
        val now = System.currentTimeMillis()
        if (content == lastNotifKey && now - lastNotifTime < NOTIF_DEBOUNCE_MS) return
        lastNotifKey = content
        lastNotifTime = now

        speak(content)
    }

    /** TTS 朗读文本（引擎未就绪时静默忽略） */
    private fun speak(text: String) {
        if (!ttsReady) {
            // 首次可能尚未就绪，提示一次
            Toast.makeText(this, "无障碍朗读服务 TTS 未就绪", Toast.LENGTH_SHORT).show()
            return
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "read_${System.currentTimeMillis()}")
    }
}