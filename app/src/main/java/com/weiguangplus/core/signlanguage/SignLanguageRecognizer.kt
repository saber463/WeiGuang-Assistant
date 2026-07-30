package com.weiguangplus.core.signlanguage

import android.content.Context
import android.content.res.AssetManager
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.sqrt

/**
 * 手语识别器 — 基于 TFLite DNN 模型
 *
 * 加载手势分类 INT8 量化模型（99.28% 准确率），
 * 对 MediaPipe 提取的 21 点关键点进行实时分类。
 *
 * 模型架构：3层全连接 (128→64→32)，输入 63 维（21点×3坐标），输出 10 类 Softmax。
 * 可选启用 30 维几何特征增强（指尖距离、关节角度等），此时输入为 93 维。
 */
class SignLanguageRecognizer(private val context: Context) {

    companion object {
        private const val MODEL_PATH = "gesture_classifier_int8.tflite"
        private const val LABEL_MAP_PATH = "gesture_label_map.json"
        private const val SCALER_PARAMS_PATH = "gesture_scaler_params.json"
        private const val FEATURE_DIM = 63        // 21 点 × 3 坐标
        private const val NUM_CLASSES = 10        // 分类数
    }

    private var interpreter: Interpreter? = null
    private var nnapiDelegate: NnApiDelegate? = null

    /** 标签映射：索引 → 英文标签名 */
    val labels: List<String>
    /** 标签映射：索引 → 中文标签名 */
    val labelsZh: List<String>
    /** SOS 紧急手势标签名集合 */
    val sosGestureNames: Set<String>

    /** 特征归一化参数 */
    private var mean: FloatArray = FloatArray(FEATURE_DIM)
    private var scale: FloatArray = FloatArray(FEATURE_DIM) { 1f }
    private var useGeometricFeatures: Boolean = false
    private var totalFeatureDim: Int = FEATURE_DIM

    /** 模型是否加载成功 */
    val isLoaded: Boolean get() = interpreter != null

    init {
        // 加载标签映射
        val labelJson = loadJsonFromAssets(LABEL_MAP_PATH)
        labels = labelJson?.optJSONArray("labels")?.let { arr ->
            (0 until arr.length()).map { arr.getString(it) }
        } ?: listOf("fist", "open_palm", "thumbs_up", "point_index", "peace",
                     "ok_sign", "wave", "heart", "call_me", "neutral")

        labelsZh = labelJson?.optJSONArray("labels_zh")?.let { arr ->
            (0 until arr.length()).map { arr.getString(it) }
        } ?: labels

        val sosArr = labelJson?.optJSONArray("sos_gesture_names")
        sosGestureNames = if (sosArr != null) {
            (0 until sosArr.length()).map { sosArr.getString(it) }.toSet()
        } else setOf("fist", "open_palm")

        // 加载归一化参数
        loadScalerParams()
    }

    /**
     * 加载 TFLite 模型
     */
    fun loadModel(useNnApi: Boolean = false): Boolean {
        if (interpreter != null) return true

        return try {
            val modelBuffer = loadModelFile(MODEL_PATH)
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                if (useNnApi) {
                    nnapiDelegate = NnApiDelegate()
                    addDelegate(nnapiDelegate)
                }
            }
            interpreter = Interpreter(modelBuffer, options)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 分类单帧手部关键点 → 返回 Top-1 结果
     *
     * @param landmarks 21 个手部关键点列表，每个含 (x, y, z)
     * @param enableGeometric 是否启用 30 维几何特征增强
     * @return 分类结果或 null（置信度过低时）
     */
    fun classify(
        landmarks: List<HandLandmark>,
        enableGeometric: Boolean = true
    ): ClassifiedGesture? {
        val interpreter = interpreter ?: return null
        if (landmarks.size < 21) return null

        // 构建输入特征向量
        val features = buildFeatureVector(landmarks, enableGeometric)

        // TFLite 推理
        val output = Array(1) { FloatArray(NUM_CLASSES) }
        synchronized(interpreter) {
            interpreter.run(features, output)
        }

        val scores = output[0]
        val bestIdx = scores.indices.maxByOrNull { scores[it] } ?: return null
        val confidence = scores[bestIdx]

        // 置信度阈值过滤
        if (confidence < 0.5f) return null

        return ClassifiedGesture(
            name = labels.getOrElse(bestIdx) { "unknown" },
            confidence = confidence,
            type = if (labels.getOrElse(bestIdx) { "" } in sosGestureNames)
                       GestureType.SOS else GestureType.DAILY
        )
    }

    /**
     * 分类单帧 → 返回 Top-K 候选列表（用于向量确认）
     */
    fun classifyTopK(
        landmarks: List<HandLandmark>,
        k: Int = 5,
        enableGeometric: Boolean = true
    ): List<ClassifiedGesture> {
        val interpreter = interpreter ?: return emptyList()
        if (landmarks.size < 21) return emptyList()

        val features = buildFeatureVector(landmarks, enableGeometric)
        val output = Array(1) { FloatArray(NUM_CLASSES) }
        synchronized(interpreter) {
            interpreter.run(features, output)
        }

        val scores = output[0]
        return scores.indices
            .map { idx ->
                ClassifiedGesture(
                    name = labels.getOrElse(idx) { "unknown" },
                    confidence = scores[idx],
                    type = if (labels.getOrElse(idx) { "" } in sosGestureNames)
                               GestureType.SOS else GestureType.DAILY
                )
            }
            .sortedByDescending { it.confidence }
            .take(k)
    }

    /**
     * 构建输入特征向量（含归一化 + 可选几何特征）
     */
    private fun buildFeatureVector(
        landmarks: List<HandLandmark>,
        enableGeometric: Boolean
    ): Array<FloatArray> {
        val raw = FloatArray(FEATURE_DIM)
        for (i in 0 until 21) {
            val lm = landmarks.getOrElse(i) { HandLandmark(i, 0f, 0f, 0f) }
            raw[i * 3] = (lm.x - mean[i * 3]) * scale[i * 3]
            raw[i * 3 + 1] = (lm.y - mean[i * 3 + 1]) * scale[i * 3 + 1]
            raw[i * 3 + 2] = (lm.z - mean[i * 3 + 2]) * scale[i * 3 + 2]
        }

        val actualDim = if (enableGeometric) 93 else 63
        val features = FloatArray(actualDim)
        System.arraycopy(raw, 0, features, 0, 63)

        if (enableGeometric) {
            extractGeometricFeatures(landmarks, features)
        }

        return arrayOf(features)
    }

    /**
     * 提取 30 维几何特征（与训练脚本保持一致）
     */
    private fun extractGeometricFeatures(landmarks: List<HandLandmark>, features: FloatArray) {
        val pts = Array(21) { i ->
            val lm = landmarks.getOrElse(i) { HandLandmark(i, 0f, 0f, 0f) }
            floatArrayOf(lm.x, lm.y, lm.z)
        }

        val fingerTips = intArrayOf(4, 8, 12, 16, 20)
        val fingerMcps = intArrayOf(2, 5, 9, 13, 17)
        var fi = 63

        // 指尖到 MCP 距离 (5)
        for (j in 0..4) {
            features[fi++] = norm(pts[fingerTips[j]], pts[fingerMcps[j]])
        }

        // 关节弯曲角度 (5)
        val triples = arrayOf(
            Triple(4, 3, 2), Triple(8, 7, 6), Triple(12, 11, 10),
            Triple(16, 15, 14), Triple(20, 19, 18)
        )
        for ((tip, dip, pip) in triples) {
            val v1 = sub(pts[dip], pts[tip])
            val v2 = sub(pts[pip], pts[dip])
            features[fi++] = dot(v1, v2) / (norm(v1, v1) * norm(v2, v2) + 1e-8f)
        }

        // 指尖到手腕距离 (5)
        for (tip in fingerTips) {
            features[fi++] = norm(pts[tip], pts[0])
        }

        // 相邻指尖间距 (4)
        for (j in 0..3) {
            features[fi++] = norm(pts[fingerTips[j]], pts[fingerTips[j + 1]])
        }

        // 拇指到各指尖距离 (4)
        for (otherTip in intArrayOf(8, 12, 16, 20)) {
            features[fi++] = norm(pts[4], pts[otherTip])
        }

        // 手掌尺寸 (2)
        features[fi++] = norm(pts[5], pts[17])
        features[fi] = norm(pts[9], pts[0]) / (norm(pts[5], pts[17]) + 1e-8f)
    }

    /** 加载归一化参数 */
    private fun loadScalerParams() {
        try {
            val json = loadJsonFromAssets(SCALER_PARAMS_PATH) ?: return
            val meanArr = json.optJSONArray("mean")
            val scaleArr = json.optJSONArray("scale")
            useGeometricFeatures = json.optBoolean("use_geometric_features", false)
            totalFeatureDim = if (useGeometricFeatures) 93 else 63

            if (meanArr != null && meanArr.length() >= FEATURE_DIM) {
                for (i in 0 until FEATURE_DIM) {
                    mean[i] = meanArr.optDouble(i, 0.0).toFloat()
                }
            }
            if (scaleArr != null && scaleArr.length() >= FEATURE_DIM) {
                for (i in 0 until FEATURE_DIM) {
                    scale[i] = scaleArr.optDouble(i, 1.0).toFloat()
                    if (scale[i] == 0f) scale[i] = 1f
                }
            }
        } catch (_: Exception) {
            // 使用默认参数（无归一化）
        }
    }

    /** 从 assets 加载 JSON */
    private fun loadJsonFromAssets(path: String): org.json.JSONObject? {
        return try {
            val jsonStr = context.assets.open(path).bufferedReader().use { it.readText() }
            org.json.JSONObject(jsonStr)
        } catch (_: Exception) {
            null
        }
    }

    /** 加载模型文件 */
    private fun loadModelFile(path: String): MappedByteBuffer {
        val descriptor = context.assets.openFd(path)
        val inputStream = FileInputStream(descriptor.fileDescriptor)
        return inputStream.channel.map(
            FileChannel.MapMode.READ_ONLY,
            descriptor.startOffset,
            descriptor.declaredLength
        )
    }

    /** 释放资源 */
    fun release() {
        synchronized(this) {
            interpreter?.close()
            interpreter = null
            nnapiDelegate?.close()
            nnapiDelegate = null
        }
    }

    // === 向量运算辅助 (避免创建临时对象) ===
    private fun norm(a: FloatArray, b: FloatArray): Float {
        val dx = a[0] - b[0]; val dy = a[1] - b[1]; val dz = a[2] - b[2]
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
    private fun sub(a: FloatArray, b: FloatArray) =
        floatArrayOf(a[0] - b[0], a[1] - b[1], a[2] - b[2])
    private fun dot(a: FloatArray, b: FloatArray) =
        a[0] * b[0] + a[1] * b[1] + a[2] * b[2]
}
