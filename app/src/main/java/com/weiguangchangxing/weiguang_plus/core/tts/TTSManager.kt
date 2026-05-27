package com.weiguangchangxing.weiguang_plus.core.tts

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class TTSState(
    val isEnabled: Boolean = true,
    val isSpeaking: Boolean = false,
    val isReady: Boolean = false,
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val currentLanguage: String = "zh-CN",
    val errorMessage: String? = null
)

object TTSManager {
    private const val maxRetryAttempts = 3

    private var tts: TextToSpeech? = null
    private var initialized = false
    private var appContext: Context? = null
    private var initializationAttempts = 0
    private val mainHandler = Handler(Looper.getMainLooper())
    private var retryRunnable: Runnable? = null

    private val _state = MutableStateFlow(TTSState())
    val state: StateFlow<TTSState> = _state.asStateFlow()

    private val utteranceQueue = ArrayDeque<UtteranceItem>(8)
    private var currentUtteranceId: String? = null

    private data class UtteranceItem(
        val text: String,
        val utteranceId: String,
        val priority: Int = 0,
        val onStart: (() -> Unit)? = null,
        val onDone: (() -> Unit)? = null
    )

    val initializationStatusText: String
        get() {
            if (initialized) return "TTS引擎已就绪"
            val currentAttempt = initializationAttempts + 1
            return when {
                currentAttempt == 1 -> "初始化中..."
                currentAttempt <= maxRetryAttempts -> "第${currentAttempt}次重试..."
                else -> "初始化失败，请检查系统语音引擎"
            }
        }

    fun initialize(context: Context) {
        if (initialized) return
        appContext = context.applicationContext
        initializationAttempts = 0
        createAndInitTts()
    }

    private fun createAndInitTts() {
        if (appContext == null) return

        retryRunnable?.let { mainHandler.removeCallbacks(it) }
        retryRunnable = null

        tts?.shutdown()
        tts = TextToSpeech(appContext!!) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val languageResult = tts?.setLanguage(Locale.CHINA)
                _state.value = _state.value.copy(
                    isReady = languageResult != TextToSpeech.LANG_MISSING_DATA &&
                            languageResult != TextToSpeech.LANG_NOT_SUPPORTED,
                    isEnabled = true,
                    errorMessage = null
                )
                applySpeedAndPitch()
                setupUtteranceListener()
                initialized = true
                initializationAttempts = 0
            } else {
                initializationAttempts++
                if (initializationAttempts < maxRetryAttempts) {
                    _state.value = _state.value.copy(
                        isReady = false,
                        errorMessage = null
                    )
                    retryRunnable = Runnable { createAndInitTts() }
                    val runnable = retryRunnable
                    if (runnable != null) {
                        mainHandler.postDelayed(runnable, 2000)
                    }
                } else {
                    _state.value = _state.value.copy(
                        isReady = false,
                        errorMessage = "TTS引擎初始化失败，请在系统设置中检查语音引擎"
                    )
                }
            }
        }
    }

    private fun setupUtteranceListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _state.value = _state.value.copy(isSpeaking = true)
                    utteranceQueue.firstOrNull()?.onStart?.invoke()
                }

                override fun onDone(utteranceId: String?) {
                    processNextInQueue()
                }

                @Suppress("OVERRIDE_DEPRECATION")
                override fun onError(utteranceId: String?) {
                    processNextInQueue()
                }
            })
        }
    }

    private fun processNextInQueue() {
        _state.value = _state.value.copy(isSpeaking = false)
        utteranceQueue.removeFirstOrNull()
        currentUtteranceId = null

        val next = utteranceQueue.firstOrNull()
        if (next != null) {
            speakInternal(next)
        }
    }

    fun speak(
        text: String,
        priority: Int = 0,
        onStart: (() -> Unit)? = null,
        onDone: (() -> Unit)? = null
    ) {
        if (!_state.value.isEnabled || !_state.value.isReady || text.isBlank()) {
            onDone?.invoke()
            return
        }

        val utteranceId = "tts_${System.currentTimeMillis()}_${text.hashCode()}"
        val item = UtteranceItem(text, utteranceId, priority, onStart, onDone)

        if (priority > 0 && utteranceQueue.isNotEmpty()) {
            val insertIndex = utteranceQueue.indexOfFirst { it.priority < priority }
            if (insertIndex >= 0) {
                utteranceQueue.add(insertIndex, item)
            } else {
                utteranceQueue.add(item)
            }
        } else {
            utteranceQueue.add(item)
        }

        if (currentUtteranceId == null) {
            processNextInQueue()
        }
    }

    private fun speakInternal(item: UtteranceItem) {
        currentUtteranceId = item.utteranceId
        applySpeedAndPitch()

        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts?.speak(item.text, TextToSpeech.QUEUE_FLUSH, null, item.utteranceId)
        } else {
            @Suppress("DEPRECATION")
            tts?.speak(item.text, TextToSpeech.QUEUE_FLUSH, null)
        }

        if (result != TextToSpeech.SUCCESS) {
            processNextInQueue()
        }
    }

    fun speakNow(text: String, onDone: (() -> Unit)? = null) {
        speak(text, priority = Int.MAX_VALUE, onStart = {
            _state.value = _state.value.copy(isSpeaking = true)
        }, onDone = onDone)
    }

    fun speakPageText(
        pageTitle: String,
        pageContent: String,
        buttonLabels: List<String> = emptyList()
    ) {
        val text = buildString {
            appendLine("当前页面：$pageTitle")
            appendLine(pageContent)
            if (buttonLabels.isNotEmpty()) {
                appendLine("可操作按钮：")
                buttonLabels.forEach { label -> appendLine("  $label") }
            }
        }
        speak(text, priority = 0)
    }

    fun stop() {
        tts?.stop()
        utteranceQueue.clear()
        currentUtteranceId = null
        _state.value = _state.value.copy(isSpeaking = false)
    }

    fun setEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(isEnabled = enabled)
        if (!enabled) {
            stop()
        }
    }

    fun setSpeed(speed: Float) {
        val safeSpeed = speed.coerceIn(0.25f, 2.0f)
        _state.value = _state.value.copy(speed = safeSpeed)
        applySpeedAndPitch()
    }

    fun setPitch(pitch: Float) {
        val safePitch = pitch.coerceIn(0.5f, 2.0f)
        _state.value = _state.value.copy(pitch = safePitch)
        applySpeedAndPitch()
    }

    private fun applySpeedAndPitch() {
        val state = _state.value
        if (state.isReady) {
            tts?.setSpeechRate(state.speed)
            tts?.setPitch(state.pitch)
        }
    }

    fun setLanguage(locale: Locale): Boolean {
        val result = tts?.setLanguage(locale)
        val success = result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED
        if (success) {
            _state.value = _state.value.copy(currentLanguage = locale.displayName)
        }
        return success
    }

    fun isSpeaking(): Boolean = _state.value.isSpeaking

    fun isReady(): Boolean = _state.value.isReady

    fun getAvailableVoices(): List<Voice> {
        return tts?.voices?.toList()?.filter { voice ->
            voice.locale.language == Locale.CHINA.language
        } ?: emptyList()
    }

    fun shutdown() {
        stop()
        retryRunnable?.let { mainHandler.removeCallbacks(it) }
        retryRunnable = null
        tts?.shutdown()
        tts = null
        initialized = false
        initializationAttempts = 0
    }
}