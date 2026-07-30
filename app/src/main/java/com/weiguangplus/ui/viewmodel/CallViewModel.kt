package com.weiguangplus.ui.viewmodel

import android.app.Application
import com.weiguangplus.core.call.CallState
import com.weiguangplus.core.call.CallStateManager
import com.weiguangplus.core.call.CallTranscriber
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CallAssistantState(
    val callState: CallState = CallState.IDLE,
    val incomingNumber: String? = null,
    val liveText: String = "",
    val myDraftText: String = "",
    val quickPhrases: List<String> = listOf(
        "你好,我听不见,请通过文字交流",
        "好的,我知道了",
        "请稍等一下",
        "我打字回复你",
        "请再说一遍",
        "好的,再见"
    )
)

class CallViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CallAssistantState())
    val uiState: StateFlow<CallAssistantState> = _uiState

    val callStateManager = CallStateManager(application)
    val transcriber = CallTranscriber(application)

    init {
        transcriber.initTts()
        viewModelScope.launch {
            callStateManager.callState.collect { state ->
                _uiState.value = _uiState.value.copy(callState = state)
                when (state) {
                    CallState.RINGING -> {
                        _uiState.value = _uiState.value.copy(
                            incomingNumber = callStateManager.incomingNumber.value
                        )
                    }
                    CallState.ANSWERED -> {
                        transcriber.startListening()
                    }
                    CallState.ENDED -> {
                        transcriber.stopListening()
                        callStateManager.resetToIdle()
                    }
                    else -> {}
                }
            }
        }
        viewModelScope.launch {
            transcriber.liveText.collect { text ->
                _uiState.value = _uiState.value.copy(liveText = text)
            }
        }
    }

    fun onDraftTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(myDraftText = text)
    }

    fun onSendText() {
        val text = _uiState.value.myDraftText.trim()
        if (text.isNotEmpty()) {
            transcriber.speakResponse(text)
            _uiState.value = _uiState.value.copy(myDraftText = "")
        }
    }

    fun onQuickPhraseClick(phrase: String) {
        transcriber.speakResponse(phrase)
    }

    fun registerCallListener() {
        callStateManager.register()
    }

    override fun onCleared() {
        super.onCleared()
        callStateManager.unregister()
        transcriber.destroy()
    }
}
