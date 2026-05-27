/*
 * ProductCategory.kt
 *
 * 功能：产品分类枚举
 *
 * 定义微光畅行 App 的三种产品分类：
 * - VISUAL_AID（视障辅助）：面向视障人群，提供环境感知、安全出行相关功能
 * - HEARING_AID（听障辅助）：面向听障人群，提供沟通表达、声音感知相关功能
 * - GENERAL（综合辅助）：面向多重障碍人群，提供全功能无障碍辅助
 *
 * 每个枚举值包含：
 * - displayName：中文显示名称
 * - description：简短描述
 * - sectionNames：该分类下可见的功能页面名称列表（用于动态控制导航栏显示）
 *
 * 持久化方案：
 * - 通过 SharedPreferences 存储/读取用户选择的产品分类
 * - 默认值为 GENERAL
 * - 应用启动时通过 getSavedCategory() 恢复上次选择
 */

package com.weiguangchangxing.weiguang_plus.core

import android.content.Context

enum class ProductCategory(
    val displayName: String,
    val description: String,
    val sectionNames: List<String>
) {
    VISUAL_AID("视障辅助", "环境感知 · 安全出行", listOf("总览", "视觉", "助手", "语音", "提醒")),
    HEARING_AID("听障辅助", "沟通表达 · 声音感知", listOf("总览", "小玉", "药品", "提醒", "语音")),
    GENERAL("综合辅助", "全功能无障碍辅助", listOf("总览", "小玉", "药品", "提醒", "视觉", "助手", "语音"));

    companion object {
        private const val PREFS_NAME = "weiguang_plus_prefs"
        private const val KEY_PRODUCT_CATEGORY = "product_category"

        /**
         * 从 SharedPreferences 读取已保存的产品分类
         *
         * @param context Android Context
         * @return 已保存的 ProductCategory，若无保存则返回 GENERAL
         */
        fun getSavedCategory(context: Context): ProductCategory {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val name = prefs.getString(KEY_PRODUCT_CATEGORY, GENERAL.name) ?: GENERAL.name
            return try { valueOf(name) } catch (e: Exception) { GENERAL }
        }

        /**
         * 将用户选择的产品分类保存到 SharedPreferences
         *
         * @param context Android Context
         * @param category 用户选中的 ProductCategory
         */
        fun saveCategory(context: Context, category: ProductCategory) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PRODUCT_CATEGORY, category.name)
                .apply()
        }
    }
}