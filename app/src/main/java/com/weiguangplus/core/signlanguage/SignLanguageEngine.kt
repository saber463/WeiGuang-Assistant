package com.weiguangplus.core.signlanguage

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 手语识别引擎（升级版 v2.0）
 *
 * 整合三大核心组件：
 * - SignLanguageRecognizer  : TFLite DNN 模型（99.28% 准确率）
 * - LandmarkBuffer          : 60 帧时序滑动窗口
 * - SignVectorDB            : 质心向量检索 + 交叉验证
 *
 * 识别流水线：
 *   摄像头帧 → MediaPipe 关键点 → LandmarkBuffer 缓冲
 *   → SignLanguageRecognizer 分类 → SignVectorDB 二次确认
 *   → 最终结果（含 Top-K 候选）
 */
object SignLanguageEngine {

    private var recognizer: SignLanguageRecognizer? = null
    private var vectorDB: SignVectorDB? = null
    private var landmarkBuffer: LandmarkBuffer? = null
    private var initialized = false

    /** 当前识别结果 */
    private val _currentResult = MutableStateFlow<SignLanguageResult?>(null)
    val currentResult: StateFlow<SignLanguageResult?> = _currentResult

    /** Top-K 候选列表（用于 UI 展示多选） */
    private val _topKCandidates = MutableStateFlow<List<SignLanguageResult>>(emptyList())
    val topKCandidates: StateFlow<List<SignLanguageResult>> = _topKCandidates

    /** 识别历史 */
    private val _history = MutableStateFlow<List<SignLanguageResult>>(emptyList())
    val history: StateFlow<List<SignLanguageResult>> = _history

    /** 是否正在识别 */
    private val _isRecognizing = MutableStateFlow(false)
    val isRecognizing: StateFlow<Boolean> = _isRecognizing

    /** 模型是否已加载 */
    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: StateFlow<Boolean> = _isModelLoaded

    /** 最近帧的手部运动幅度（用于 UI 运动指示器） */
    private val _motionLevel = MutableStateFlow(0f)
    val motionLevel: StateFlow<Float> = _motionLevel

    /** SOS 紧急手势是否被触发（由外部 SOS 模块监听） */
    private val _sosGestureDetected = MutableStateFlow(false)
    val sosGestureDetected: StateFlow<Boolean> = _sosGestureDetected

    // 去抖：连续 N 帧相同结果才输出
    private var lastStableResult: String? = null
    private var stableFrameCount = 0
    private val stableThreshold = 5 // 连续 5 帧相同才确认

    // SOS 连续检测帧数
    private var sosFrameCount = 0
    private val sosThreshold = 8 // 连续 8 帧 SOS 才触发

    /**
     * 初始化引擎（需在 Application.onCreate 或首次使用时调用）
     */
    fun initialize(context: Context) {
        if (initialized) return

        recognizer = SignLanguageRecognizer(context).also {
            val loaded = it.loadModel()
            _isModelLoaded.value = loaded
        }

        vectorDB = SignVectorDB(context)
        landmarkBuffer = LandmarkBuffer(maxFrames = 60, frameSkip = 2)
        initialized = true
    }

    /**
     * 处理单帧手部检测结果 → 输出手势识别
     *
     * 流水线：
     * 1. 关键点入缓冲
     * 2. 取均值特征减少抖动
     * 3. TFLite 模型分类
     * 4. 向量 DB 交叉验证
     * 5. 去抖确认 → 输出结果
     */
    fun processHandDetection(detection: HandDetectionResult): SignLanguageResult? {
        if (!initialized) return null
        _isRecognizing.value = true

        val buffer = landmarkBuffer ?: return null
        val rec = recognizer ?: return null
        val db = vectorDB ?: return null

        // Step 1: 缓冲关键点（取第一个检测到的手）
        val primaryHand = detection.hands.firstOrNull()
        buffer.addFrame(primaryHand)
        _motionLevel.value = buffer.motionLevel

        // 无动作时跳过推理
        if (!buffer.hasMotion() && buffer.size < 10) {
            _isRecognizing.value = false
            return null
        }

        // Step 2: 取最近 10 帧均值 → 稳定特征向量
        val avgFeatures = buffer.getAverageFeatures(10) ?: run {
            _isRecognizing.value = false
            return null
        }

        // Step 3: TFLite 模型分类（Top-5）
        val topKResults = rec.classifyTopK(primaryHand!!, k = 5)
        val top1 = topKResults.firstOrNull() ?: run {
            _isRecognizing.value = false
            return null
        }

        // Step 4: 向量 DB 交叉验证
        val crossResult = db.crossValidate(top1, avgFeatures)

        // Step 5: 去抖确认
        val finalLabel = crossResult.finalLabel
        val isSos = rec.sosGestureNames.contains(finalLabel)

        if (finalLabel == lastStableResult) {
            stableFrameCount++
        } else {
            lastStableResult = finalLabel
            stableFrameCount = 1
            sosFrameCount = 0
        }

        // SOS 特殊处理：需要更多连续帧确认
        if (isSos) {
            sosFrameCount++
            if (sosFrameCount >= sosThreshold) {
                _sosGestureDetected.value = true
            }
        } else {
            sosFrameCount = 0
        }

        // 去抖未通过时不输出
        if (stableFrameCount < stableThreshold) {
            _isRecognizing.value = false
            return null
        }

        // 构建最终结果
        val result = SignLanguageResult(
            gestureName = crossResult.finalLabelZh,
            confidence = crossResult.confidence,
            gestureType = if (isSos) GestureType.SOS else GestureType.DAILY,
            textTranslation = gestureToText(finalLabel),
            handLandmarks = detection.hands
        )

        _currentResult.value = result
        _history.value = (_history.value + result).takeLast(50)

        // 构建 Top-K 候选列表
        _topKCandidates.value = topKResults.map { g ->
            SignLanguageResult(
                gestureName = db.getLabelZh(g.name),
                confidence = g.confidence,
                gestureType = g.type,
                textTranslation = gestureToText(g.name),
                handLandmarks = emptyList()
            )
        }

        _isRecognizing.value = false
        return result
    }

    /**
     * 单帧快速识别（不使用缓冲和去抖）
     * 适用于需要即时反馈的场景
     */
    fun classifySingleFrame(detection: HandDetectionResult): ClassifiedGesture? {
        val rec = recognizer ?: return null
        return detection.hands.firstOrNull()?.let { rec.classify(it) }
    }

    /**
     * 将文字转换为手势名称
     */
    fun textToGesture(text: String): String? {
        return textToGestureMap[text]
    }

    /** 手势 → 文字翻译 */
    fun gestureToText(gestureName: String): String {
        return gestureToTextMap[gestureName] ?: vectorDB?.getLabelZh(gestureName) ?: gestureName
    }

    /** 获取 SOS 紧急手势标签集合 */
    fun getSosGestureNames(): Set<String> {
        return recognizer?.sosGestureNames ?: setOf("fist", "open_palm")
    }

    /** 重置 SOS 触发状态 */
    fun resetSosGesture() {
        _sosGestureDetected.value = false
        sosFrameCount = 0
    }

    /** 清除识别历史 */
    fun clearHistory() {
        _history.value = emptyList()
        _currentResult.value = null
        _topKCandidates.value = emptyList()
        lastStableResult = null
        stableFrameCount = 0
        sosFrameCount = 0
    }

    /** 释放资源 */
    fun release() {
        recognizer?.release()
        recognizer = null
        vectorDB = null
        landmarkBuffer?.clear()
        landmarkBuffer = null
        initialized = false
    }

    // ─── 翻译映射表 ───

    private val gestureToTextMap = mapOf(
        "fist" to "救命！需要帮助！",
        "open_palm" to "请帮帮我 / 停止",
        "thumbs_up" to "好的 / 确认",
        "point_index" to "那个 / 指方向",
        "peace" to "好 / 胜利",
        "ok_sign" to "好的，没问题",
        "wave" to "你好 / 再见",
        "heart" to "谢谢",
        "call_me" to "打电话",
        "neutral" to "（无手势）",
        "thumb_down" to "不好 / 不行",
        // 中文兼容
        "握拳(SOS)" to "救命！需要帮助！",
        "手掌张开(停止)" to "请帮帮我 / 停止",
        "竖大拇指(确认)" to "好的 / 确认",
        "食指指向(方向)" to "那个 / 指方向",
        "剪刀手(胜利)" to "好 / 胜利",
        "OK手势" to "好的，没问题",
        "摆手(问候)" to "你好 / 再见",
        "比心(谢谢)" to "谢谢",
        "打电话" to "打电话",
        "无手势" to "（无手势）",
        "拇指朝下(谢谢)" to "不好 / 不行"
    )

    private val textToGestureMap = mapOf(
        "救命" to "握拳/SOS求救",
        "帮助" to "手掌/我需要帮助",
        "好的" to "点赞/好的",
        "你好" to "摆手(问候)",
        "等一下" to "四个/等待",
        "停" to "全开手掌/停止",
        "不好" to "小指/不好",
        "那里" to "指方向/那里",
        "没问题" to "OK/没问题",
        "谢谢" to "比心(谢谢)",
        "再见" to "摆手(问候)",
        "打电话" to "打电话"
    )
}
