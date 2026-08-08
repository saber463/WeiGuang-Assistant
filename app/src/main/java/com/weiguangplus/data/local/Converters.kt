/**
 * 文件名：Converters.kt
 * 功能描述：Room数据库类型转换器，将复杂Kotlin类型（List、Map等）与SQLite兼容类型互转
 * 所属模块：data/local（本地数据层）
 *
 * 转换策略：
 * - List<String> → JSON字符串（使用Gson序列化）
 * - List<Map<String, String>> → JSON字符串
 * - 转换失败时返回默认值（空列表），确保数据库读取不崩溃
 */
package com.weiguangplus.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room数据库类型转换器
 *
 * Room原生只支持基本类型（Int、Long、String、Float、Double、Boolean、ByteArray），
 * 对于List、Map等复杂类型必须通过TypeConverter桥接。
 */
class Converters {

    private val gson = Gson()

    /**
     * List<String> → JSON字符串
     * 用于存储 riskPrompts、signKeywords 等字段
     */
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }

    /**
     * JSON字符串 → List<String>
     */
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(value, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * List<Map<String, String>> → JSON字符串
     * 用于存储 contraindications 等结构化字段
     */
    @TypeConverter
    fun fromMapList(value: List<Map<String, String>>?): String {
        return gson.toJson(value ?: emptyList<Map<String, String>>())
    }

    /**
     * JSON字符串 → List<Map<String, String>>
     */
    @TypeConverter
    fun toMapList(value: String): List<Map<String, String>> {
        val type = object : TypeToken<List<Map<String, String>>>() {}.type
        return try {
            gson.fromJson(value, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}