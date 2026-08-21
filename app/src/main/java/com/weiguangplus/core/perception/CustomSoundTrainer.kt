package com.weiguangplus.core.perception

import android.util.Log
import com.weiguangplus.data.model.CustomSound
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

/**
 * 自定义声音训练与匹配引擎
 *
 * 职责（G1 基础框架，无需外部模型即可运行）：
 *  1. 从一段 PCM 音频样本中提取轻量时域特征（RMS 能量轮廓 + 零均值过零率）
 *  2. 将特征向量作为"自定义声音样本"保存到本地 JSON 文件（filesDir）
 *  3. 对实时音频帧提取特征，与样本库做余弦相似度匹配（KNN 思想 + 阈值判定）
 *
 * 设计决策（WHY）：
 *  - 采用时域特征（RMS/ZCR）而非 MFCC 谱特征：基础的"强度节奏型"声音
 *    （门铃、拍手、警报、拍桌子）在时域上区分度已足够，且零依赖。
 *    后续接入通用分类模型（YAMNet 等）时，仅替换本类的特征提取方法即可，
 *    训练/匹配/触发的调用方不受影响。
 *  - 使用 filesDir 下的 JSON 文件持久化，避免触碰现有 Room 数据库
 *    （不新增实体、不触发版本迁移），降低本次改动风险。
 */
class CustomSoundTrainer(private val context: android.content.Context) {

    companion object {
        private const val TAG = "CustomSoundTrainer"

        /** 保存样本的文件名（位于 context.filesDir） */
        private const val SOUNDS_FILE = "custom_sounds.json"

        /** 特征长度：RMS 轮廓 12 段 + 过零率 1 个 = 13 维 */
        const val FEATURE_DIMENSION = 13

        /** 余弦相似度命中阈值：0.85 以上认为匹配（经验值，可调） */
        private const val MATCH_THRESHOLD = 0.85f
    }

    // 已训练的样本列表（内存缓存，加载于 init 时）
    private val samples: MutableList<CustomSound> = mutableListOf()

    init {
        loadFromDisk()
    }

    // ==================== 特征提取 ====================

    /**
     * 从 PCM 样本中提取特征向量
     *
     * @param pcm ShortArray 16-bit PCM 音频（单声道）
     * @return FloatArray 特征，长度 [FEATURE_DIMENSION]
     *
     * 特征构成（WHY）：
     *  - `FEATURE_DIMENSION - 1` 个段：将音频均匀切成 12 段，每段计算归一化 RMS 能量，
     *    刻画声音的"强度随时间变化的轮廓"（门铃是震动→停顿→震动，轮廓独特）
     *  - 最后 1 维：整体零均值过零率（描述声音"脆/钝"程度），增强区分度
     */
    fun extractFeatures(pcm: ShortArray): FloatArray {
        if (pcm.isEmpty()) return FloatArray(FEATURE_DIMENSION)

        val segmentCount = FEATURE_DIMENSION - 1
        val segLen = pcm.size / segmentCount
        val features = FloatArray(FEATURE_DIMENSION)

        // 1) 计算整体均方根（RMS）用于归一化，避免音量大小影响特征
        var sumSq = 0.0
        for (s in pcm) sumSq += s.toDouble() * s.toDouble()
        val rms = Math.sqrt(sumSq / pcm.size)

        // 2) 逐段计算 RMS，并用全局 RMS 归一化到 0~1
        for (seg in 0 until segmentCount) {
            val start = seg * segLen
            var segSumSq = 0.0
            var end = start + segLen
            if (end > pcm.size) end = pcm.size
            for (i in start until end) {
                val v = pcm[i].toDouble()
                segSumSq += v * v
            }
            val segRms = Math.sqrt(segSumSq / (end - start))
            features[seg] = if (rms > 0f) (segRms / rms).toFloat() else 0f
        }

        // 3) 最后一位：整体过零率（sign 变化次数占比）
        var zeroCrossings = 0
        for (i in 1 until pcm.size) {
            if ((pcm[i - 1] < 0 && pcm[i] >= 0) || (pcm[i - 1] >= 0 && pcm[i] < 0)) {
                zeroCrossings++
            }
        }
        features[segmentCount] = zeroCrossings.toFloat() / pcm.size
        return features
    }

    // ==================== 训练 / 保存 ====================

    /**
     * 添加一个声音样本（训练），并持久化到磁盘
     *
     * @param name 声音名称（如"家里门铃"）
     * @param pcm  声音 PCM 数据
     */
    fun addSample(name: String, pcm: ShortArray) {
        val sound = CustomSound(
            name = name,
            features = extractFeatures(pcm),
            createdAt = System.currentTimeMillis()
        )
        samples.add(sound)
        saveToDisk()
        Log.d(TAG, "已训练自定义声音: $name (样本数=${samples.size})")
    }

    /** 移除指定位置的样本 */
    fun removeSample(index: Int) {
        if (index in samples.indices) {
            samples.removeAt(index)
            saveToDisk()
        }
    }

    /** 清空所有样本 */
    fun clearSamples() {
        samples.clear()
        saveToDisk()
    }

    /** 当前已训练的样本列表（只读拷贝） */
    fun getSamples(): List<CustomSound> = samples.toList()

    /** 已训练样本数量 */
    fun sampleCount(): Int = samples.size

    // ==================== 匹配 ====================

    /**
     * 对一段实时音频做匹配，返回命中的声音名称（无匹配返回 null）
     *
     * 算法：对每个已训练样本计算余弦相似度，取最高且 ≥ [MATCH_THRESHOLD] 的作为命中。
     *
     * @return 命中的声音名称；若无匹配返回 null
     */
    fun match(pcm: ShortArray): String? {
        if (samples.isEmpty()) return null
        val query = extractFeatures(pcm)

        var bestName: String? = null
        var bestScore = 0f
        for (sample in samples) {
            val score = cosineSimilarity(query, sample.features)
            if (score > bestScore) {
                bestScore = score
                bestName = sample.name
            }
        }
        return if (bestScore >= MATCH_THRESHOLD) bestName else null
    }

    /**
     * 计算两个特征向量的余弦相似度
     *
     * 余弦相似度对向量"长度"不敏感，因此匹配不受音量大小影响，
     * 只关注"波形轮廓"是否相似 —— 这正是区分不同类型声音所需的特性。
     */
    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size) return 0f
        var dot = 0f
        var na = 0f
        var nb = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            na += a[i] * a[i]
            nb += b[i] * b[i]
        }
        if (na == 0f || nb == 0f) return 0f
        return dot / (Math.sqrt(na.toDouble()) * Math.sqrt(nb.toDouble())).toFloat()
    }

    // ==================== 持久化 ====================

    private fun getFile(): File = File(context.filesDir, SOUNDS_FILE)

    /** 将样本列表写入 JSON 文件 */
    private fun saveToDisk() {
        try {
            val json = JSONArray()
            samples.forEach { json.put(it.toJson()) }
            getFile().writeText(json.toString(), Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "保存自定义声音失败: ${e.message}")
        }
    }

    /** 启动时从 JSON 文件加载样本 */
    private fun loadFromDisk() {
        try {
            val file = getFile()
            if (!file.exists()) return
            val text = file.readText()
            if (text.isBlank()) return
            val json = JSONArray(text)
            val loaded = mutableListOf<CustomSound>()
            for (i in 0 until json.length()) {
                loaded.add(CustomSound.fromJson(json.getJSONObject(i)))
            }
            samples.addAll(loaded)
            Log.d(TAG, "已从磁盘加载 ${loaded.size} 个自定义声音样本")
        } catch (e: Exception) {
            Log.e(TAG, "加载自定义声音失败: ${e.message}")
        }
    }
}