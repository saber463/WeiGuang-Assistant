/**
 * 文件名：DrugViewModel.kt
 * 作者：微光同行前端团队
 * 功能描述：药品识别状态管理ViewModel，处理图片上传、识别结果展示、历史记录等业务逻辑
 * 创建日期：2026-05-29
 * 所属模块：ui/viewmodel（视图模型层）
 *
 * 核心功能：
 * 1. 药品图片上传与AI识别（拍照/相册选择）
 * 2. 识别结果展示（药品详情卡片 + 风险提示）
 * 3. 历史记录分页加载（上拉加载更多）
 * 4. 药品搜索（关键词模糊匹配）
 * 5. 识别反馈提交（纠错功能）
 *
 * 状态管理设计：
 * - 使用sealed class定义不同的UI状态（Idle/Loading/Success/Error）
 * - 通过StateFlow推送状态更新给Composable UI
 * - 支持配置变更恢复（SavedStateHandle可扩展）
 *
 * 性能优化：
 * - 图片压缩后再上传（减少流量和耗时）
 * - 列表使用LazyColumn虚拟滚动（避免一次性渲染所有项）
 * - 搜索防抖（500ms延迟，避免频繁请求）
 */

package com.weiguangplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

/**
 * 药品识别ViewModel类
 *
 * 使用Hilt @HiltViewModel注解支持依赖注入。
 * 通过构造函数注入DrugRepository实例。
 */
@HiltViewModel
class DrugViewModel @Inject constructor(
    private val drugRepository: DrugRepository
) : ViewModel() {

    // ==================== 识别状态管理 ====================

    /** 识别操作的UI状态流（私有可变） */
    private val _recognitionState = MutableStateFlow<RecognitionUiState>(
        RecognitionUiState.Idle
    )

    /** 识别状态的公开只读流（供UI层观察） */
    val recognitionState: StateFlow<RecognitionUiState> =
        _recognitionState.asStateFlow()

    /** 识别相关的一次性事件流（导航、Toast等） */
    private val _recognitionEvent = MutableSharedFlow<RecognitionEvent>(replay = 0)

    /** 识别事件的公开只读流 */
    val recognitionEvent: SharedFlow<RecognitionEvent> =
        _recognitionEvent.asSharedFlow()

    // ==================== 历史记录状态管理 ====================

    /** 历史记录列表的UI状态流 */
    private val _historyState = MutableStateFlow(HistoryUiState())

    /** 历史记录状态的公开只读流 */
    val historyState: StateFlow<HistoryUiState> =
        _historyState.asStateFlow()

    // ==================== 搜索状态管理 ====================

    /** 搜索结果的UI状态流 */
    private val _searchState = MutableStateFlow(SearchUiState())

    /** 搜索状态的公开只读流 */
    val searchState: StateFlow<SearchUiState> =
        _searchState.asStateFlow()

    // ==================== 公开方法（供UI层调用） ====================

    /**
     * 上传图片并执行药品识别
     *
     * 从相机或相册选择的图片文件，
     * 上传到后端进行AI OCR识别。
     *
     * @param imageFile 本地图片文件对象
     */
    fun recognizeImage(imageFile: File) {
        viewModelScope.launch {
            // 设置加载中状态
            _recognitionState.value = RecognitionUiState.Loading

            // 调用Repository执行识别请求
            val result = drugRepository.recognizeDrug(imageFile)

            // 处理结果并更新UI状态
            result.fold(
                onSuccess = { drug ->
                    _recognitionState.value = RecognitionUiState.Success(drug)
                    _recognitionEvent.emit(RecognitionEvent.RecognizeSuccess)
                },
                onFailure = { exception ->
                    _recognitionState.value = RecognitionUiState.Error(
                        errorMessage = exception.message ?: "识别失败，请重试"
                    )
                    _recognitionEvent.emit(
                        RecognitionEvent.ShowError(exception.message!!)
                    )
                }
            )
        }
    }

    /**
     * 重置识别状态为初始状态
     *
     * 用于返回重新拍照或清除当前结果。
     */
    fun resetRecognition() {
        _recognitionState.value = RecognitionUiState.Idle
    }

    /**
     * 加载识别历史记录
     *
     * 分页加载用户的识别历史。
     *
     * @param page 页码（默认1，首次加载或刷新时使用）
     * @param isRefresh 是否为下拉刷新操作（true则清空旧数据）
     */
    fun loadHistory(page: Int = 1, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _historyState.value = HistoryUiState(isLoading = true)
            } else {
                _historyState.value = _historyState.value.copy(isLoading = true)
            }

            val result = drugRepository.getRecognitionHistory(page = page)

            result.fold(
                onSuccess = { records ->
                    val newList = if (isRefresh) {
                        records
                    } else {
                        _historyState.value.records + records
                    }

                    _historyState.value = HistoryUiState(
                        records = newList,
                        isLoading = false,
                        currentPage = page,
                        hasMore = records.size >= 20  // 假设每页20条，如果返回满页则可能还有更多
                    )
                },
                onFailure = { exception ->
                    _historyState.value = _historyState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "加载失败"
                    )
                }
            )
        }
    }

    /**
     * 加载更多历史记录（下一页）
     *
     * 在用户滚动到底部触发上拉加载更多时调用。
     */
    fun loadMoreHistory() {
        if (_historyState.value.isLoading || !_historyState.value.hasMore) return

        loadHistory(
            page = _historyState.value.currentPage + 1,
            isRefresh = false
        )
    }

    /**
     * 搜索药品
     *
     * 根据关键词在药品数据库中进行模糊搜索。
     *
     * @param keyword 搜索关键词
     */
    fun searchDrugs(keyword: String) {
        viewModelScope.launch {
            if (keyword.isBlank()) {
                _searchState.value = SearchUiState()
                return@launch
            }

            _searchState.value = SearchUiState(isLoading = true, keyword = keyword)

            val result = drugRepository.searchDrugs(keyword = keyword)

            result.fold(
                onSuccess = { drugs ->
                    _searchState.value = SearchUiState(
                        isLoading = false,
                        keyword = keyword,
                        results = drugs,
                        isEmpty = drugs.isEmpty()
                    )
                },
                onFailure = { exception ->
                    _searchState.value = _searchState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "搜索失败"
                    )
                }
            )
        }
    }

    /**
     * 清除搜索状态
     */
    fun clearSearch() {
        _searchState.value = SearchUiState()
    }

    /**
     * 提交识别反馈
     *
     * 用户对识别结果进行纠错或确认。
     *
     * @param drugId 药品ID
     * @param feedbackType 反馈类型
     * @param description 详细描述（可选）
     */
    fun submitFeedback(
        drugId: Long,
        feedbackType: String,
        description: String? = null
    ) {
        viewModelScope.launch {
            val result = drugRepository.submitFeedback(
                drugId = drugId,
                feedbackType = feedbackType,
                description = description
            )

            result.fold(
                onSuccess = {
                    _recognitionEvent.emit(RecognitionEvent.FeedbackSubmitted)
                },
                onFailure = { exception ->
                    _recognitionEvent.emit(
                        RecognitionEvent.ShowError("反馈提交失败: ${exception.message}")
                    )
                }
            )
        }
    }

    // ==================== 数据类定义（UI状态） ====================

    /**
     * 识别操作的UI状态密封类
     *
     * 定义识别流程中的不同阶段：
     * - Idle：空闲状态（等待用户操作）
     * - Loading：加载中（正在上传和识别）
     * - Success：识别成功（显示结果）
     * - Error：识别失败（显示错误提示）
     */
    sealed class RecognitionUiState {
        /** 空闲状态（初始状态或已重置） */
        object Idle : RecognitionUiState()

        /** 加载中状态（显示进度指示器） */
        object Loading : RecognitionUiState()

        /**
         * 识别成功状态
         *
         * @property drug 识别出的药品详细信息
         */
        data class Success(val drug: Drug) : RecognitionUiState()

        /**
         * 识别失败状态
         *
         * @property errorMessage 用户友好的错误描述
         */
        data class Error(val errorMessage: String) : RecognitionUiState()
    }

    /**
     * 历史记录列表的UI状态数据类
     *
     * @property records 当前已加载的记录列表
     * @property isLoading 是否正在加载
     * @property error 错误信息（如有）
     * @property currentPage 当前页码
     * @property hasMore 是否还有更多数据（用于控制"加载更多"按钮显示）
     */
    data class HistoryUiState(
        val records: List<RecognitionRecord> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val currentPage: Int = 1,
        val hasMore: Boolean = true
    )

    /**
     * 搜索结果的UI状态数据类
     *
     * @property isLoading 是否正在搜索
     * @property keyword 当前的搜索关键词
     * @property results 搜索结果列表
     * @property isEmpty 结果是否为空（用于显示空状态占位图）
     * @property error 错误信息
     */
    data class SearchUiState(
        val isLoading: Boolean = false,
        val keyword: String = "",
        val results: List<Drug> = emptyList(),
        val isEmpty: Boolean = false,
        val error: String? = null
    )

    /**
     * 药品识别相关的一次性事件密封类
     */
    sealed class RecognitionEvent {
        /** 识别成功事件 */
        object RecognizeSuccess : RecognitionEvent()
        /** 反馈提交成功事件 */
        object FeedbackSubmitted : RecognitionEvent()
        /** 显示错误提示事件 */
        data class ShowError(val message: String) : RecognitionEvent()
        /** 导航到详情页事件 */
        data class NavigateToDetail(val drugId: Long) : RecognitionEvent()
    }
}
