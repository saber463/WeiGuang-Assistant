package com.weiguangplus.core.signlanguage

/**
 * 手语关键点时序缓冲区
 *
 * 维护一个固定长度的滑动窗口（默认 60 帧 @ 30fps = 2 秒手语动作），
 * 用于收集连续帧的关键点序列，供 LSTM/时序模型进行连续手语识别。
 *
 * 关键设计：
 * - 滑动窗口：新帧入队，最旧帧自动出队，始终保持最新 N 帧
 * - 帧过滤：跳帧机制（默认每 2 帧取 1 帧）降低计算量
 * - 动作检测：通过帧间关键点位移量判断是否有手语动作在进行
 * - 自动重置：长时间无手部检测后自动清空缓冲
 *
 * @param maxFrames 最大缓冲帧数（默认 60）
 * @param frameSkip 跳帧间隔（默认 2，即每 2 帧取 1 帧）
 */
class LandmarkBuffer(
    val maxFrames: Int = 60,
    val frameSkip: Int = 2
) {
    companion object {
        /** 每帧关键点维度：21 点 × 3 坐标 */
        const val FEATURE_DIM = 63
        /** 动作检测阈值：相邻帧平均关键点位移 */
        const val MOTION_THRESHOLD = 0.02f
    }

    /** 存储所有已缓冲的帧特征向量 */
    private val buffer = mutableListOf<FloatArray>()

    /** 上一帧的特征向量（用于动作检测） */
    private var prevFrame: FloatArray? = null

    /** 帧计数器（用于跳帧） */
    private var frameCount = 0

    /** 连续空帧计数（无手检测时递增） */
    private var emptyFrameCount = 0

    /** 连续空帧阈值（超此值自动重置，默认 15 帧 ≈ 0.5 秒） */
    var emptyResetThreshold: Int = 15

    /** 缓冲区的帧序列（只读副本，形状 (N, 225) 的扁平数组仅包含 63 维关键点） */
    val frames: List<FloatArray> get() = buffer.toList()

    /** 当前缓冲帧数 */
    val size: Int get() = buffer.size

    /** 缓冲区是否已满（达到 maxFrames） */
    val isFull: Boolean get() = buffer.size >= maxFrames

    /** 是否为活跃状态（最近有手部检测） */
    val isActive: Boolean get() = emptyFrameCount < emptyResetThreshold

    /**
     * 当前运动幅度（0~1，越大表示手部移动越快）
     * 可用于 UI 可视化（如运动指示器）
     */
    var motionLevel: Float = 0f
        private set

    /**
     * 添加一帧手部关键点到缓冲区
     *
     * @param landmarks 21 个手部关键点，null 表示该帧未检测到手
     */
    fun addFrame(landmarks: List<HandLandmark>?) {
        frameCount++

        // 跳帧逻辑
        if (frameCount % frameSkip != 0) return

        if (landmarks == null || landmarks.size < 21) {
            handleEmptyFrame()
            return
        }

        emptyFrameCount = 0

        // 将关键点展平为 63 维向量
        val features = FloatArray(FEATURE_DIM)
        for (i in 0 until 21) {
            val lm = landmarks.getOrElse(i) { HandLandmark(i, 0f, 0f, 0f) }
            features[i * 3] = lm.x
            features[i * 3 + 1] = lm.y
            features[i * 3 + 2] = lm.z
        }

        // 计算运动幅度
        prevFrame?.let { prev ->
            var totalDelta = 0f
            for (i in 0 until FEATURE_DIM) {
                totalDelta += kotlin.math.abs(features[i] - prev[i])
            }
            motionLevel = (totalDelta / FEATURE_DIM).coerceIn(0f, 1f)
        }

        prevFrame = features

        // 滑动窗口：满时移除最旧帧
        if (buffer.size >= maxFrames) {
            buffer.removeAt(0)
        }
        buffer.add(features)
    }

    private fun handleEmptyFrame() {
        emptyFrameCount++
        if (emptyFrameCount >= emptyResetThreshold) {
            clear()
        }
    }

    /**
     * 获取完整的时序特征矩阵
     * 返回 (bufferSize × FEATURE_DIM) 的二维浮点数组，
     * 若不足 maxFrames 则用零填充。
     */
    fun getSequenceMatrix(): Array<FloatArray> {
        val result = Array(maxFrames) { FloatArray(FEATURE_DIM) }
        val offset = maxFrames - buffer.size
        for (i in buffer.indices) {
            System.arraycopy(buffer[i], 0, result[offset + i], 0, FEATURE_DIM)
        }
        return result
    }

    /**
     * 获取缓冲区的平均特征向量（用于稳定帧分类）
     * 对最近 n 帧取均值，减少单帧抖动。
     */
    fun getAverageFeatures(recentFrames: Int = 10): FloatArray? {
        if (buffer.isEmpty()) return null
        val count = recentFrames.coerceAtMost(buffer.size)
        val start = buffer.size - count
        val avg = FloatArray(FEATURE_DIM)
        for (i in start until buffer.size) {
            val frame = buffer[i]
            for (j in 0 until FEATURE_DIM) {
                avg[j] += frame[j]
            }
        }
        for (j in 0 until FEATURE_DIM) {
            avg[j] = avg[j] / count
        }
        return avg
    }

    /**
     * 检测缓冲区内是否有明显的手语动作
     * 判断依据：相邻帧间位移量超过阈值
     */
    fun hasMotion(): Boolean {
        if (buffer.size < 3) return false
        for (i in 1 until buffer.size) {
            val prev = buffer[i - 1]
            val curr = buffer[i]
            var delta = 0f
            for (j in 0 until FEATURE_DIM) {
                delta += kotlin.math.abs(curr[j] - prev[j])
            }
            if (delta / FEATURE_DIM > MOTION_THRESHOLD) return true
        }
        return false
    }

    /** 清空缓冲区 */
    fun clear() {
        buffer.clear()
        prevFrame = null
        frameCount = 0
        emptyFrameCount = 0
        motionLevel = 0f
    }
}
