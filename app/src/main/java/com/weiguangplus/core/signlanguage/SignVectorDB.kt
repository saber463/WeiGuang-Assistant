package com.weiguangplus.core.signlanguage

import android.content.Context
import org.json.JSONObject
import kotlin.math.sqrt

/**
 * 手语向量数据库
 *
 * 基于手势质心向量的余弦相似度检索，提供：
 * - 从 JSON 加载预计算质心向量（与训练脚本 extract_landmarks_from_dataset.py 一致）
 * - 余弦相似度 Top-K 检索
 * - 二次确认：TFLite 识别结果与向量检索交叉验证
 * - 降级方案：纯 Java 暴力搜索（无外部依赖）
 *
 * 数据格式（gesture_centroids.json）：
 * {
 *   "gestures": {
 *     "fist": { "centroid": [...63 floats...], "count": 150, "label_zh": "握拳(SOS)" },
 *     ...
 *   }
 * }
 */
class SignVectorDB(private val context: Context) {

    companion object {
        private const val CENTROIDS_PATH = "gesture_centroids.json"
        private const val FEATURE_DIM = 63 // 21 点 × 3 坐标
        private const val DEFAULT_THRESHOLD = 0.75f // 余弦相似度阈值
    }

    /** 质心向量库：标签 -> 质心向量 */
    private val centroids = mutableMapOf<String, FloatArray>()

    /** 中文标签：英文标签 -> 中文名 */
    private val labelZhMap = mutableMapOf<String, String>()

    /** 每个手势的样本数 */
    private val sampleCounts = mutableMapOf<String, Int>()

    /** 向量库是否加载成功 */
    val isLoaded: Boolean get() = centroids.isNotEmpty()

    /** 已加载的词汇数 */
    val vocabularySize: Int get() = centroids.size

    /** 所有标签列表 */
    val labels: List<String> get() = centroids.keys.toList()

    init {
        loadFromAssets()
    }

    /**
     * 从 assets 加载质心向量 JSON
     */
    private fun loadFromAssets() {
        try {
            val jsonStr = context.assets.open(CENTROIDS_PATH)
                .bufferedReader().use { it.readText() }
            val json = JSONObject(jsonStr)
            val gestures = json.optJSONObject("gestures") ?: return

            val keys = gestures.keys()
            while (keys.hasNext()) {
                val label = keys.next()
                val gesture = gestures.getJSONObject(label)
                val centroidArr = gesture.optJSONArray("centroid") ?: continue

                val vector = FloatArray(FEATURE_DIM)
                for (i in 0 until FEATURE_DIM.coerceAtMost(centroidArr.length())) {
                    vector[i] = centroidArr.optDouble(i, 0.0).toFloat()
                }
                centroids[label] = vector
                labelZhMap[label] = gesture.optString("label_zh", label)
                sampleCounts[label] = gesture.optInt("count", 0)
            }
        } catch (_: Exception) {
            // 降级：使用内置硬编码的质心（基于合成数据）
            loadBuiltinCentroids()
        }
    }

    /**
     * 内置降级质心（当 JSON 文件缺失或加载失败时使用）
     */
    private fun loadBuiltinCentroids() {
        // 简化质心：基于手指伸直/弯曲状态的粗略向量
        centroids["fist"] = FloatArray(FEATURE_DIM) { 0.35f }
        centroids["open_palm"] = FloatArray(FEATURE_DIM) { 0.65f }
        centroids["thumbs_up"] = FloatArray(FEATURE_DIM) { 0.55f }
        centroids["point_index"] = FloatArray(FEATURE_DIM) { 0.50f }
        centroids["peace"] = FloatArray(FEATURE_DIM) { 0.52f }
        centroids["ok_sign"] = FloatArray(FEATURE_DIM) { 0.48f }
        centroids["wave"] = FloatArray(FEATURE_DIM) { 0.58f }
        centroids["heart"] = FloatArray(FEATURE_DIM) { 0.45f }
        centroids["call_me"] = FloatArray(FEATURE_DIM) { 0.42f }
        centroids["neutral"] = FloatArray(FEATURE_DIM) { 0.50f }
        labelZhMap["fist"] = "握拳(SOS)"
        labelZhMap["open_palm"] = "手掌张开(停止)"
        labelZhMap["thumbs_up"] = "竖大拇指(确认)"
        labelZhMap["point_index"] = "食指指向(方向)"
        labelZhMap["peace"] = "剪刀手(胜利)"
        labelZhMap["ok_sign"] = "OK手势"
        labelZhMap["wave"] = "摆手(问候)"
        labelZhMap["heart"] = "比心(谢谢)"
        labelZhMap["call_me"] = "打电话"
        labelZhMap["neutral"] = "无手势"
    }

    /**
     * 余弦相似度检索 — Top-K 匹配
     *
     * @param queryVector 查询向量（63 维关键点特征）
     * @param k 返回 Top-K 结果
     * @param threshold 最低相似度阈值
     * @return 按相似度降序排列的匹配结果列表
     */
    fun search(
        queryVector: FloatArray,
        k: Int = 5,
        threshold: Float = DEFAULT_THRESHOLD
    ): List<VectorMatchResult> {
        if (centroids.isEmpty()) return emptyList()

        return centroids.map { (label, centroid) ->
            val similarity = cosineSimilarity(queryVector, centroid)
            VectorMatchResult(
                label = label,
                labelZh = labelZhMap[label] ?: label,
                similarity = similarity,
                sampleCount = sampleCounts[label] ?: 0
            )
        }
        .filter { it.similarity >= threshold }
        .sortedByDescending { it.similarity }
        .take(k)
    }

    /**
     * 暴力搜索 — 指定时间预算的最优匹配
     *
     * @param queryVector 查询向量
     * @param maxTimeMs 最大搜索时间（毫秒），用于性能约束
     * @return 最佳匹配或 null（无超过阈值的匹配）
     */
    fun searchBest(
        queryVector: FloatArray,
        maxTimeMs: Long = 2L
    ): VectorMatchResult? {
        if (centroids.isEmpty()) return null

        val startTime = System.currentTimeMillis()
        var bestMatch: VectorMatchResult? = null
        var bestSim = -1f

        for ((label, centroid) in centroids) {
            val sim = cosineSimilarity(queryVector, centroid)
            if (sim > bestSim) {
                bestSim = sim
                bestMatch = VectorMatchResult(
                    label = label,
                    labelZh = labelZhMap[label] ?: label,
                    similarity = sim,
                    sampleCount = sampleCounts[label] ?: 0
                )
            }
            // 超时保护
            if (System.currentTimeMillis() - startTime > maxTimeMs) break
        }

        return if ((bestMatch?.similarity ?: 0f) >= DEFAULT_THRESHOLD) bestMatch else null
    }

    /**
     * 二次确认：TFLite 分类 + 向量检索交叉验证
     *
     * 当 TFLite 模型给出 Top-1 结果时，通过向量检索验证是否匹配。
     * 两者一致 → 高置信度；不一致 → 降低置信度或返回向量检索结果。
     *
     * @param classifierResult TFLite 模型分类结果
     * @param queryVector 查询向量
     * @return 交叉验证后的结果
     */
    fun crossValidate(
        classifierResult: ClassifiedGesture?,
        queryVector: FloatArray
    ): CrossValidationResult {
        if (classifierResult == null) {
            // 无分类结果，仅用向量检索
            val vectorMatch = searchBest(queryVector)
            return CrossValidationResult(
                finalLabel = vectorMatch?.label ?: "unknown",
                finalLabelZh = vectorMatch?.labelZh ?: "未知",
                confidence = vectorMatch?.similarity ?: 0f,
                source = "vector_only"
            )
        }

        // 向量检索 Top-5
        val topK = search(queryVector, k = 5)
        val vectorTop1 = topK.firstOrNull()

        // 交叉验证逻辑
        return when {
            // TFLite Top-1 出现在向量 Top-3 中 → 高置信度，保持一致
            topK.take(3).any { it.label == classifierResult.name } -> {
                CrossValidationResult(
                    finalLabel = classifierResult.name,
                    finalLabelZh = labelZhMap[classifierResult.name] ?: classifierResult.name,
                    confidence = (classifierResult.confidence + (vectorTop1?.similarity ?: 0f)) / 2f,
                    source = "cross_validated"
                )
            }
            // 向量 Top-1 置信度明显更高 → 采用向量结果
            (vectorTop1?.similarity ?: 0f) > classifierResult.confidence + 0.15f -> {
                CrossValidationResult(
                    finalLabel = vectorTop1!!.label,
                    finalLabelZh = vectorTop1.labelZh,
                    confidence = vectorTop1.similarity,
                    source = "vector_override"
                )
            }
            // 否则信任 TFLite 结果但标记为不确定
            else -> {
                CrossValidationResult(
                    finalLabel = classifierResult.name,
                    finalLabelZh = labelZhMap[classifierResult.name] ?: classifierResult.name,
                    confidence = classifierResult.confidence * 0.7f, // 降低置信度
                    source = "classifier_uncertain"
                )
            }
        }
    }

    /** 获取标签的中文名 */
    fun getLabelZh(label: String): String = labelZhMap[label] ?: label

    // === 向量运算 ===

    /**
     * 余弦相似度：cos(θ) = (A·B) / (||A|| * ||B||)
     */
    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        val denominator = sqrt(normA) * sqrt(normB)
        return if (denominator > 1e-8f) dotProduct / denominator else 0f
    }

    /**
     * 向量归一化（L2 范数）
     */
    fun normalize(vector: FloatArray): FloatArray {
        val norm = sqrt(vector.fold(0f) { acc, v -> acc + v * v })
        return if (norm > 1e-8f) FloatArray(vector.size) { vector[it] / norm } else vector
    }
}

/**
 * 向量检索匹配结果
 */
data class VectorMatchResult(
    val label: String,          // 英文标签
    val labelZh: String,        // 中文标签
    val similarity: Float,      // 余弦相似度 (0~1)
    val sampleCount: Int = 0    // 质心样本数
)

/**
 * 交叉验证结果
 */
data class CrossValidationResult(
    val finalLabel: String,     // 最终标签
    val finalLabelZh: String,   // 最终中文标签
    val confidence: Float,      // 置信度
    val source: String          // 来源："cross_validated" | "vector_override" | "classifier_uncertain" | "vector_only"
)
