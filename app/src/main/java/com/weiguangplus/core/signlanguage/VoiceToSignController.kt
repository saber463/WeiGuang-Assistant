package com.weiguangplus.core.signlanguage

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 语音→手语控制器
 *
 * 桥接离线语音识别 (VoiceRecognizer) 与手语生成 (SignLanguageGenerator)，
 * 实现"对着手机说话 → 屏幕上显示手语动画"的完整闭环。
 *
 * 流水线：
 *   麦克风 → VoiceRecognizer (Vosk 离线) → 中文文本
 *         → SignLanguageGenerator → 手势序列
 *         → UI 展示 (SignLanguagePlayerScreen)
 *
 * 特性：
 * - 边说边生成（实时部分结果也能匹配常用词）
 * - 自动去抖：停顿 1.5 秒后才触发生成
 * - 支持连续对话模式
 */
class VoiceToSignController(private val context: Context) {

    companion object {
        private const val TAG = "VoiceToSign"
        /** 语音停顿后等待多久触发手语生成（毫秒） */
        private const val GENERATION_DELAY_MS = 1500L
    }

    val voiceRecognizer = VoiceRecognizer(context)

    /** 当前生成的手语序列 */
    private val _currentGeneration = MutableStateFlow<SignLanguageGenerator.GenerationResult?>(
        null
    )
    val currentGeneration: StateFlow<SignLanguageGenerator.GenerationResult?> =
        _currentGeneration

    /** 实时部分识别文本（边说边显示） */
    val liveText: StateFlow<String> = voiceRecognizer.partialText

    /** 最终识别文本 */
    val recognizedText: StateFlow<String> = voiceRecognizer.finalText

    /** 是否正在录音 */
    val isListening: StateFlow<Boolean> = voiceRecognizer.isListening

    /** 模型是否就绪 */
    val isModelReady: StateFlow<Boolean> = voiceRecognizer.isModelReady

    /** 初始化进度 */
    val initProgress: StateFlow<Float> = voiceRecognizer.initProgress

    /** 音量 */
    val volumeLevel: StateFlow<Float> = voiceRecognizer.volumeLevel

    private var generationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /** 手语生成回调（每次有新的识别结果时触发） */
    var onGenerated: ((SignLanguageGenerator.GenerationResult) -> Unit)? = null

    /**
     * 初始化语音识别模型并开始监听
     */
    fun start(onReady: ((Boolean) -> Unit)? = null) {
        voiceRecognizer.initialize { success ->
            if (success) {
                Log.d(TAG, "Vosk 就绪，开始语音→手语闭环")
                startListening()
            }
            onReady?.invoke(success)
        }
    }

    /**
     * 开始语音识别 + 自动手语生成
     */
    fun startListening() {
        voiceRecognizer.startListening()

        // 监听最终识别结果 → 触发手语生成
        scope.launch {
            voiceRecognizer.finalText.collect { text ->
                if (text.isNotBlank()) {
                    // 去抖：等待停顿后生成
                    generationJob?.cancel()
                    generationJob = scope.launch {
                        delay(GENERATION_DELAY_MS)
                        generateSignLanguage(text)
                    }
                }
            }
        }

        // 边听边生成部分结果（快速响应常用短语）
        scope.launch {
            voiceRecognizer.partialText.collect { partial ->
                if (partial.isNotBlank() && partial.length >= 2) {
                    // 对短短语即时生成
                    val quickWords = listOf("救命", "帮助", "好的", "谢谢", "你好", "再见", "停", "不行")
                    if (partial in quickWords) {
                        generateSignLanguage(partial)
                    }
                }
            }
        }
    }

    /**
     * 手动输入文字生成手语（不经过语音）
     */
    fun generateFromText(text: String): SignLanguageGenerator.GenerationResult {
        val result = SignLanguageGenerator.generate(text)
        _currentGeneration.value = result
        onGenerated?.invoke(result)
        return result
    }

    /**
     * 停止语音监听
     */
    fun stop() {
        voiceRecognizer.stopListening()
        generationJob?.cancel()
    }

    /**
     * 释放所有资源
     */
    fun release() {
        stop()
        scope.cancel()
        voiceRecognizer.release()
    }

    // ─── 内部 ───

    private fun generateSignLanguage(text: String) {
        Log.d(TAG, "语音→手语生成: \"$text\"")
        val result = SignLanguageGenerator.generate(text)
        _currentGeneration.value = result
        onGenerated?.invoke(result)

        // 如果生成的手势含 SOS，触发告警
        val hasSos = result.gestures.any { it.category == "SOS" }
        if (hasSos) {
            Log.w(TAG, "⚠️ 语音识别到 SOS 关键词: $text")
        }
    }
}
