package com.weiguangplus.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 快捷短语实体（G12）
 *
 * 预设/用户自定义的常用语，供听障用户在聊天、通话页面一键输入。
 * 独立存储于 QuickPhraseDatabase（weiguang_quick_phrases.db），不触碰主库。
 *
 * 字段说明（WHY）：
 *  - category：场景分类（就医/出行/购物/日常/求助/自定义），UI 分组展示
 *  - isCustom：区分预设 vs 用户自定义（预设不可删或删除仅隐藏，自定义可自由增删）
 *  - sortOrder：组内排序
 */
@Entity(tableName = "quick_phrases")
data class QuickPhrase(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 场景分类：就医/出行/购物/日常/求助/自定义 */
    val category: String,
    /** 短语文本 */
    val text: String,
    /** 是否用户自定义（false=预设） */
    val isCustom: Boolean = false,
    /** 组内排序 */
    val sortOrder: Int = 0
)