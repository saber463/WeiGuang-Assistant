package com.weiguangplus.data.model

import org.json.JSONObject

/**
 * 自定义声音样本数据模型
 *
 * 对应 G1 自定义声音训练功能：用户录制一段声音（如自家门铃），
 * 程序提取时域特征并保存，供后续实时匹配。
 *
 * 特征向量设计（WHY）：
 * 采用「RMS 能量轮廓 + 过零率」的轻量时域特征，优点是：
 *  - 无需任何外部依赖，单一文件可独立运行
 *  - 对"有节奏/有强度特征"的重复性声音（门铃、拍手、警报）区分度足够
 * 临时不采用 MFCC 谱特征是为了保持基础框架零依赖；后续接通用模型时可扩展。
 *
 * 持久化：以 JSON 形式写入 `context.filesDir/custom_sounds.json`，
 * 不新增 Room 实体，避免数据库版本迁移风险。
 */
data class CustomSound(
    /** 声音名称（用户自定义标识，如"家里门铃"） */
    val name: String,
    /** 特征向量：索引 0..n-1 为能量轮廓，末尾为过零率统计，见特征构造处 */
    val features: FloatArray,
    /** 录音生成该样本的时间戳（epoch ms） */
    val createdAt: Long
) {
    /** 序列化为 JSON（供持久化到文件） */
    fun toJson(): JSONObject = JSONObject().apply {
        put("name", name)
        put("features", features.joinToString(",") { String.format("%.4f", it) })
        put("createdAt", createdAt)
    }

    companion object {
        /** 从 JSON 反序列化 */
        fun fromJson(json: JSONObject): CustomSound {
            val raw = json.getString("features")
            val arr = raw.split(",").filter { it.isNotBlank() }.map { it.toFloat() }
            return CustomSound(
                name = json.getString("name"),
                features = arr.toFloatArray(),
                createdAt = json.getLong("createdAt")
            )
        }
    }
}