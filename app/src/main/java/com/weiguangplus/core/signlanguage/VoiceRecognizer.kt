package com.weiguangplus.core.signlanguage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 语音识别器 —— 基于 Android 内置 SpeechRecognizer
 *
 * 使用系统语音识别引擎进行中文语音识别。
 * 识别结果通过 StateFlow 实时输出。
 * 如需离线识别，系统已内置 Vosk 模型（vosk-model-cn），可后续切换。
 */
class VoiceRecognizer(private val context: Context) {

    companion object {
        private const val TAG = "VoiceRecognizer"
    }

    private var speechRecognizer: SpeechRecognizer? = null

    /** 实时部分识别文本 */
    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText

    /** 最终识别结果 */
    private val _finalText = MutableStateFlow("")
    val finalText: StateFlow<String> = _finalText

    /** 是否正在监听 */
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    /** 模型是否就绪 */
    private val _isModelReady = MutableStateFlow(false)
    val isModelReady: StateFlow<Boolean> = _isModelReady

    /** 初始化进度 */
    private val _initProgress = MutableStateFlow(0f)
    val initProgress: StateFlow<Float> = _initProgress

    /** 音量 */
    private val _volumeLevel = MutableStateFlow(0f)
    val volumeLevel: StateFlow<Float> = _volumeLevel

    var onInitComplete: ((Boolean) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    /**
     * 初始化语音识别器
     */
    fun initialize(onReady: ((Boolean) -> Unit)? = null) {
        if (_isModelReady.value) {
            onReady?.invoke(true)
            return
        }
        onInitComplete = onReady

        try {
            _initProgress.value = 0.3f
            val available = SpeechRecognizer.isRecognitionAvailable(context)
            _initProgress.value = 1.0f
            _isModelReady.value = available
            Log.d(TAG, "SpeechRecognizer available=$available")
            onInitComplete?.invoke(available)
            onInitComplete = null
        } catch (e: Exception) {
            Log.e(TAG, "Init failed", e)
            _isModelReady.value = false
            onInitComplete?.invoke(false)
            onInitComplete = null
            onError?.invoke("语音引擎不可用: ${e.message}")
        }
    }

    /**
     * 开始语音监听
     */
    fun startListening(): Boolean {
        if (!_isModelReady.value) return false

        try {
            _partialText.value = ""
            _finalText.value = ""

            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {
                        _volumeLevel.value = (rmsdB / 10f).coerceIn(0f, 1f)
                    }
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {
                        _isListening.value = false
                        // Auto-restart
                        speechRecognizer?.startListening(buildIntent())
                        _isListening.value = true
                    }
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _finalText.value = matches[0]
                            _partialText.value = matches[0]
                        }
                    }
                    override fun onPartialResults(partial: Bundle?) {
                        val matches = partial?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _partialText.value = matches[0]
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
                startListening(buildIntent())
            }

            _isListening.value = true
            Log.d(TAG, "Listening started")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Start failed", e)
            _isListening.value = false
            return false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        _isListening.value = false
    }

    fun pause() { stopListening() }
    fun resume() { startListening() }

    fun release() {
        stopListening()
        _isModelReady.value = false
    }

    private fun buildIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }
}
