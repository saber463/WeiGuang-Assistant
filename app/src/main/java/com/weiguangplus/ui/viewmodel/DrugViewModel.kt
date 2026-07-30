package com.weiguangplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weiguangplus.core.EnhancedOcrEngine
import com.weiguangplus.core.EnhancedDrugResult
import com.weiguangplus.data.model.Drug
import com.weiguangplus.data.model.RecognitionRecord
import com.weiguangplus.data.repository.DrugRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DrugViewModel @Inject constructor(
    private val drugRepository: DrugRepository
) : ViewModel() {

    private val _recognitionState = MutableStateFlow<RecognitionUiState>(RecognitionUiState.Idle)
    val recognitionState: StateFlow<RecognitionUiState> = _recognitionState.asStateFlow()

    private val _recognitionEvent = MutableSharedFlow<RecognitionEvent>(replay = 0)
    val recognitionEvent: SharedFlow<RecognitionEvent> = _recognitionEvent.asSharedFlow()

    private val _historyState = MutableStateFlow(HistoryUiState())
    val historyState: StateFlow<HistoryUiState> = _historyState.asStateFlow()

    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    /** 上传图片并执行药品识别 */
    fun recognizeImage(imageFile: File) {
        viewModelScope.launch {
            _recognitionState.value = RecognitionUiState.Loading
            val result = drugRepository.recognizeDrug(imageFile)
            result.fold(
                onSuccess = { drug ->
                    _recognitionState.value = RecognitionUiState.Success(drug)
                    _recognitionEvent.emit(RecognitionEvent.RecognizeSuccess)
                },
                onFailure = { exception ->
                    _recognitionState.value = RecognitionUiState.Error(
                        exception.message ?: "识别失败"
                    )
                }
            )
        }
    }

    /** 本地OCR识别（离线，不依赖后端） */
    fun recognizeTextLocally(rawText: String) {
        val enhanced = EnhancedOcrEngine.recognize(rawText)
        if (enhanced.confidence > 0.5f) {
            val drug = Drug(
                genericName = enhanced.genericName,
                tradeName = enhanced.drugName,
                riskLevel = enhanced.riskLevel,
                riskPrompts = enhanced.warnings
            )
            _recognitionState.value = RecognitionUiState.Success(drug)
        } else {
            _recognitionState.value = RecognitionUiState.Error("未能识别药品，请重新拍照")
        }
    }

    fun loadMoreHistory() {
        if (_historyState.value.isLoading || !_historyState.value.hasMore) return
        viewModelScope.launch {
            _historyState.value = _historyState.value.copy(isLoading = true)
            val nextPage = _historyState.value.currentPage + 1
            val result = drugRepository.getRecognitionHistory(page = nextPage)
            result.fold(
                onSuccess = { records ->
                    _historyState.value = _historyState.value.copy(
                        records = _historyState.value.records + records,
                        isLoading = false,
                        currentPage = nextPage,
                        hasMore = records.isNotEmpty()
                    )
                },
                onFailure = { exception ->
                    _historyState.value = _historyState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

    fun searchDrugs(keyword: String) {
        if (keyword.isBlank()) {
            clearSearch(); return
        }
        if (_searchState.value.keyword == keyword) return
        viewModelScope.launch {
            _searchState.value = SearchUiState(isLoading = true, keyword = keyword)
            val result = drugRepository.searchDrugs(keyword)
            result.fold(
                onSuccess = { drugs ->
                    _searchState.value = SearchUiState(
                        isLoading = false, keyword = keyword,
                        results = drugs, isEmpty = drugs.isEmpty()
                    )
                },
                onFailure = { exception ->
                    _searchState.value = SearchUiState(
                        isLoading = false, keyword = keyword,
                        error = exception.message ?: "搜索失败"
                    )
                }
            )
        }
    }

    fun clearSearch() {
        _searchState.value = SearchUiState()
    }

    fun submitFeedback(drugId: Long, feedbackType: String, description: String? = null) {
        viewModelScope.launch {
            val result = drugRepository.submitFeedback(drugId, feedbackType, null, description)
            result.fold(
                onSuccess = { _recognitionEvent.emit(RecognitionEvent.FeedbackSubmitted) },
                onFailure = { exception ->
                    _recognitionEvent.emit(
                        RecognitionEvent.ShowError("反馈提交失败: ${exception.message}")
                    )
                }
            )
        }
    }

    fun launchCamera() {
        _recognitionState.value = RecognitionUiState.Loading
    }

    fun launchGallery() {
        _recognitionState.value = RecognitionUiState.Loading
    }

    fun uploadAndRecognize(imageUri: String) {
        viewModelScope.launch {
            _recognitionState.value = RecognitionUiState.Loading
        }
    }

    sealed class RecognitionUiState {
        object Idle : RecognitionUiState()
        object Loading : RecognitionUiState()
        data class Success(val drug: Drug) : RecognitionUiState()
        data class Error(val errorMessage: String) : RecognitionUiState()
    }

    data class HistoryUiState(
        val records: List<RecognitionRecord> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val currentPage: Int = 1,
        val hasMore: Boolean = true
    )

    data class SearchUiState(
        val isLoading: Boolean = false,
        val keyword: String = "",
        val results: List<Drug> = emptyList(),
        val isEmpty: Boolean = false,
        val error: String? = null
    )

    sealed class RecognitionEvent {
        object RecognizeSuccess : RecognitionEvent()
        object FeedbackSubmitted : RecognitionEvent()
        data class ShowError(val message: String) : RecognitionEvent()
        data class NavigateToDetail(val drugId: Long) : RecognitionEvent()
    }
}
