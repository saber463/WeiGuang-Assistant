package com.weiguangplus.core.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * TTS 语音播报控制器
 *
 * 封装 Android TextToSpeech API，提供：
 * - 自动初始化（异步等待引擎就绪）
 * - 队列播报（防止同时播放多条语音）
 * - 紧急播报（打断当前语音，优先播报）
 * - 语速/音调可调
 */
object TtsController {

    private const val TAG = "TtsController"
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var initCallback: ((Boolean) -> Unit)? = null

    /** 语速：0.5~2.0，默认 1.0 */
    var speechRate: Float = 0.9f
    /** 音调：0.5~2.0，默认 1.0 */
    var pitch: Float = 1.0f

    /**
     * 初始化 TTS 引擎
     */
    fun initialize(context: Context, onReady: ((Boolean) -> Unit)? = null) {
        if (isInitialized) {
            onReady?.invoke(true)
            return
        }

        initCallback = onReady
        tts = TextToSpeech(context.applicationContext) { status ->
            val success = status == TextToSpeech.SUCCESS
            if (success) {
                val result = tts?.setLanguage(Locale.CHINESE)
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "中文 TTS 不可用，回退到默认语言")
                    tts?.setLanguage(Locale.getDefault())
                }
                tts?.setSpeechRate(speechRate)
                tts?.setPitch(pitch)
                isInitialized = true
                Log.d(TAG, "TTS 初始化成功 (语言: ${tts?.voice?.name ?: "默认"})")
            } else {
                Log.e(TAG, "TTS 初始化失败, status=$status")
            }
            initCallback?.invoke(success)
            initCallback = null
        }
    }

    /**
     * 普通语音播报（排队模式）
     */
    fun speak(text: String) {
        if (!isInitialized || text.isBlank()) return
        logSpeak(text, false)

        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, "tts_${System.currentTimeMillis()}")
    }

    /**
     * 紧急语音播报（打断当前语音）
     * 用于 SOS 紧急手势等场景
     */
    fun speakWarning(text: String) {
        if (!isInitialized || text.isBlank()) return
        logSpeak(text, true)

        // 提高语速播报紧急信息
        val originalRate = speechRate
        tts?.setSpeechRate(1.2f)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sos_${System.currentTimeMillis()}")
        tts?.setSpeechRate(originalRate)
    }

    /**
     * 静默停止当前播报
     */
    fun stop() {
        tts?.stop()
    }

    /**
     * 设置播报回调
     */
    fun setOnUtteranceListener(listener: UtteranceProgressListener?) {
        tts?.setOnUtteranceProgressListener(listener)
    }

    /**
     * 关闭 TTS 释放资源
     */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        Log.d(TAG, "TTS 已关闭")
    }

    private fun logSpeak(text: String, isWarning: Boolean) {
        val prefix = if (isWarning) "[紧急]" else "[播报]"
        Log.d(TAG, "$prefix $text")
    }
}
