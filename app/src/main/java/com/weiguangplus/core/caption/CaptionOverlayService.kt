package com.weiguangplus.core.caption

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import com.weiguangplus.MainActivity
import com.weiguangplus.R

/**
 * 全场景悬浮窗实时字幕前台服务（G2）
 *
 * 职责：
 * 在任意应用上方显示半透明悬浮窗，将麦克风实时语音识别结果滚动显示，
 * 让听障用户"看得见别人说的话"，覆盖面对面交流/会议/看视频等全场景。
 *
 * 核心流程：
 * ```
 * 用户开启 → 检查悬浮窗权限 → startForeground(通知, microphone类型)
 * → WindowManager.addView(悬浮窗) → SpeechRecognizer 持续识别
 * → onPartialResults/onResults 更新悬浮窗 TextView
 * ```
 *
 * 设计决策（WHY）：
 *  - 用 Android `SpeechRecognizer`（系统识别，zh-CN）而非 Vosk：
 *    与现有通话转写 [CallTranscriber] 完全一致，行为可预期、可复用，降低风险。
 *  - 前台服务 `foregroundServiceType="microphone"`：确保后台/息屏时识别不被打断，
 *    并满足 Android 14 对前台服务类型的强制声明要求。
 *  - 识别引擎在 onError/onEnd 后主动重启，实现"无限续听"（通话转写已验证模式）。
 */
class CaptionOverlayService : Service() {

    companion object {
        private const val TAG = "CaptionOverlayService"
        private const val CHANNEL_ID = "caption_overlay_channel"
        private const val NOTIFICATION_ID = 2001

        /** 悬浮窗距离顶部的高度（作用于副屏幕/多窗口时相对窗口顶部） */
        private const val OVERLAY_TOP_DP = 48

        /** 识别不中断：onError 后重启延迟（ms） */
        private const val RESTART_DELAY_MS = 1000L

        /** 是否正在运行（供 UI 查询状态） */
        var isRunning: Boolean = false
            private set
    }

    private var windowManager: WindowManager? = null
    private var overlayView: FrameLayout? = null
    private var captionText: TextView? = null

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private val mainHandler = Handler(Looper.getMainLooper())

    // ==================== 生命周期 ====================

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 指定"关闭"动作：TODO 简化，本次由 stopService 控制
        startAsForeground()
        if (ensurePermission()) {
            addOverlay()
            startRecognition()
        }
        return START_STICKY // 被系统回收后尝试重建（适合常驻辅助服务）
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        stopListening()
        removeOverlay()
    }

    // ==================== 前台服务 & 通知 ====================

    /** 创建前台服务通知（Android 8.0+ 需要通知渠道） */
    private fun startAsForeground() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 单次创建渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "字幕悬浮窗",
                NotificationManager.IMPORTANCE_LOW // LOW：不打扰但常驻
            )
            nm.createNotificationChannel(channel)
        }

        // 点击通知回到主页（不跳转特定界面）
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        @Suppress("DEPRECATION")
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("字幕悬浮窗运行中")
            .setContentText("正在实时显示语音字幕")
            .setSmallIcon(R.drawable.app_logo)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .build()

        // targetSdk 34：前台服务需带 type 启动
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    // ==================== 悬浮窗 ====================

    /** 悬浮窗权限是否已授予 */
    private fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    /** 检查权限，不足时返回 false（由 UI 层引导授权） */
    private fun ensurePermission(): Boolean = hasOverlayPermission()

    /** 添加悬浮窗 */
    @Suppress("DEPRECATION")
    private fun addOverlay() {
        if (overlayView != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // 构建悬浮窗控件：半透明深底字幕条
        val parent = FrameLayout(this).apply {
            isClickable = true // 消费触摸，防止事件穿透到下层应用
            setBackgroundColor(0x99000000.toInt()) // 近黑色半透明
        }

        captionText = TextView(this).apply {
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(16), dp(12), dp(16), dp(12))
            text = "正在聆听...（等待识别）"
        }
        parent.addView(captionText, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ))

        // 悬浮窗布局参数（顶部居中，焦点不可抢）
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,   // 宽满
            WindowManager.LayoutParams.WRAP_CONTENT,   // 高自适应内容
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = px(OVERLAY_TOP_DP) // 距顶部
        }

        try {
            windowManager?.addView(parent, params)
            overlayView = parent
            Log.d(TAG, "悬浮窗已添加")
        } catch (e: Exception) {
            Log.e(TAG, "添加悬浮窗失败（可能未授权或无系统权限）: ${e.message}")
            overlayView = null
        }
    }

    /** 移除悬浮窗 */
    private fun removeOverlay() {
        overlayView?.let { view ->
            try { windowManager?.removeView(view) } catch (_: Exception) { }
        }
        overlayView = null
        captionText = null
    }

    // ==================== 语音识别 ====================

    /** 启动持续识别（无限续听） */
    private fun startRecognition() {
        if (isListening) return
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            captionText?.text = "设备不支持语音识别"
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val m = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (m != null && m.isNotEmpty()) captionText?.text = m[0]
                    restartRecognition() // 续听
                }

                override fun onPartialResults(partial: Bundle?) {
                    val m = partial?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (m != null && m.isNotEmpty()) captionText?.text = m[0]
                }

                override fun onError(error: Int) {
                    // 常见错误：9=超时等，都重启续听
                    mainHandler.postDelayed(::restartRecognition, RESTART_DELAY_MS)
                }

                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            startListening(buildRecognizerIntent())
        }
        isListening = true
        isRunning = true
    }

    /** 重启识别（onError/onResults 后调用，实现无限续听） */
    private fun restartRecognition() {
        try {
            speechRecognizer?.startListening(buildRecognizerIntent())
        } catch (e: Exception) {
            Log.w(TAG, "重启识别失败: ${e.message}")
        }
    }

    private fun buildRecognizerIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    /** 停止识别 */
    private fun stopListening() {
        isListening = false
        try { speechRecognizer?.destroy() } catch (_: Exception) { }
        speechRecognizer = null
    }

    // ==================== 工具 ====================

    /** dp → px（基于窗口 density） */
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    /** dp 距离 → px */
    private fun px(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    /** 供 UI 调用的启停入口（静态，简化调用） */
    fun startService(context: Context) {
        val intent = Intent(context, CaptionOverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}