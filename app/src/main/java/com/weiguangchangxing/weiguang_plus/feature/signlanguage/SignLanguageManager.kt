package com.weiguangchangxing.weiguang_plus.feature.signlanguage

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import com.weiguangchangxing.weiguang_plus.core.DeviceCapabilityChecker
import com.weiguangchangxing.weiguang_plus.core.tts.TTSManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SignLanguageState(
    val isListening: Boolean = false,
    val isRecognizing: Boolean = false,
    val currentInputMode: InputMode = InputMode.NONE,
    val recognizedText: String = "",
    val matchedSignPhrases: List<SignPhrase> = emptyList(),
    val selectedSignPhrase: SignPhrase? = null,
    val displayText: String = "",
    val ttsReady: Boolean = false,
    val errorMessage: String? = null,
    val modelDownloaded: Boolean = false,
    val modelDownloading: Boolean = false,
    val isHandTrackingSupported: Boolean = false,
    val androidVersion: String = DeviceCapabilityChecker.androidVersionName,
    val apiLevel: Int = DeviceCapabilityChecker.currentApiLevel,
    val handTrackingLevel: String = "NONE",
    val handTrackingDescription: String = "手势识别已禁用"
)

enum class InputMode {
    NONE,
    SPEECH_TO_SIGN,
    SIGN_TO_SPEECH
}

class SignLanguageManager(private val context: Context) {
    private val _state = MutableStateFlow(SignLanguageState())
    val state: StateFlow<SignLanguageState> = _state.asStateFlow()

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var speechRecognizer: SpeechRecognizer? = null
    private val animationPlayer = LottieAnimationPlayer()
    private val signDatabase = SignLanguageDatabase.createDefaultDatabase(context)
    private val signMatcher = SignLanguageMatcher()

    init {
        initializeSpeechRecognizer()
        _state.value = _state.value.copy(
            isHandTrackingSupported = false,
            handTrackingLevel = "NONE"
        )
        startTTSStateObservation()
    }

    private fun startTTSStateObservation() {
        coroutineScope.launch {
            TTSManager.state.collect { ttsState ->
                _state.value = _state.value.copy(
                    ttsReady = ttsState.isReady
                )
            }
        }
    }

    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            setupSpeechRecognitionListener()
        } else {
            _state.value = _state.value.copy(
                errorMessage = "当前设备不支持语音识别功能"
            )
        }
    }

    private fun setupSpeechRecognitionListener() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: android.os.Bundle?) {
                _state.value = _state.value.copy(
                    isRecognizing = true,
                    errorMessage = null
                )
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _state.value = _state.value.copy(isRecognizing = false)
            }

            override fun onError(error: Int) {
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "音频录制错误"
                    SpeechRecognizer.ERROR_CLIENT -> "客户端错误"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "权限不足"
                    SpeechRecognizer.ERROR_NETWORK -> "网络错误"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时"
                    SpeechRecognizer.ERROR_NO_MATCH -> "未识别到语音"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别服务忙"
                    SpeechRecognizer.ERROR_SERVER -> "服务器错误"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "无语音输入"
                    else -> "未知错误"
                }
                _state.value = _state.value.copy(
                    isRecognizing = false,
                    errorMessage = errorMsg
                )
            }

            override fun onResults(results: android.os.Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull() ?: ""
                processRecognizedText(recognizedText)
            }

            override fun onPartialResults(partialResults: android.os.Bundle?) {
                val matches = partialResults?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )
                val partialText = matches?.firstOrNull() ?: ""
                _state.value = _state.value.copy(recognizedText = partialText)
            }

            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        })
    }

    fun startSpeechToSignMode() {
        _state.value = _state.value.copy(
            currentInputMode = InputMode.SPEECH_TO_SIGN,
            isListening = true,
            recognizedText = "",
            matchedSignPhrases = emptyList(),
            displayText = "正在聆听，请说话...",
            errorMessage = null
        )

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isListening = false,
                errorMessage = "启动语音识别失败: ${e.message}"
            )
        }
    }

    fun stopSpeechRecognition() {
        speechRecognizer?.stopListening()
        _state.value = _state.value.copy(
            isListening = false,
            isRecognizing = false
        )
    }

    private fun processRecognizedText(text: String) {
        _state.value = _state.value.copy(recognizedText = text)

        val matchedPhrases = signMatcher.searchText(text, signDatabase)
        val bestMatch = matchedPhrases.firstOrNull()

        _state.value = _state.value.copy(
            matchedSignPhrases = matchedPhrases,
            selectedSignPhrase = bestMatch,
            displayText = bestMatch?.chineseSign ?: text
        )

        if (bestMatch != null) {
            playSignAnimation(bestMatch)
        }
    }

    fun selectSignPhrase(phrase: SignPhrase) {
        _state.value = _state.value.copy(
            selectedSignPhrase = phrase,
            displayText = phrase.chineseSign
        )
        playSignAnimation(phrase)
    }

    fun startSignToSpeechMode() {
        _state.value = _state.value.copy(
            currentInputMode = InputMode.SIGN_TO_SPEECH,
            isRecognizing = true,
            displayText = "当前设备不支持手势识别",
            errorMessage = null
        )
    }

    fun stopSignRecognition() {
        _state.value = _state.value.copy(
            isRecognizing = false,
            isListening = false
        )
    }

    fun analyzeSignFrame(imageProxy: androidx.camera.core.ImageProxy) {
    }

    fun speakNaturalLanguage(text: String) {
        if (TTSManager.isReady()) {
            TTSManager.speakNow(text)
        }
    }

    private fun playSignAnimation(phrase: SignPhrase) {
        try {
            animationPlayer.playAnimation(phrase.lottieFile)
        } catch (_: Exception) {
        }
    }

    fun displayPhrase(phrase: SignPhrase) {
        _state.value = _state.value.copy(
            selectedSignPhrase = phrase,
            displayText = phrase.chineseSign
        )
        playSignAnimation(phrase)
    }

    fun getHighFrequencyPhrases(): List<SignPhrase> {
        return signDatabase.getHighFrequencyPhrases()
    }

    fun getAllPhrases(): List<SignPhrase> {
        return signDatabase.getAllPhrases()
    }

    fun getGestureTemplatesForNextPhrase(): List<Any> {
        return emptyList()
    }

    fun isHandModelReady(): Boolean = false

    fun isHandModelDownloading(): Boolean = false

    fun reset() {
        stopSpeechRecognition()
        stopSignRecognition()
        animationPlayer.stopAnimation()
        _state.value = SignLanguageState(ttsReady = _state.value.ttsReady)
    }

    fun release() {
        speechRecognizer?.destroy()
        animationPlayer.release()
    }
}