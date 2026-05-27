package com.weiguangchangxing.weiguang_plus.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// 药品主表实体：
// 这是药品离线库的主索引表，负责承载所有检索入口都会用到的核心信息。
// 当前版本同时保留三类检索辅助字段：
// 1. pinyinKey：完整拼音，方便后续做拼音搜索和排序
// 2. initialsKey：拼音首字母，方便做轻量首字母检索
// 3. searchTokens：综合搜索词，给 OCR 容错词、别名词和演示样本召回使用
// 同时保留 sourceTag 和 licenseNote，用于来源追溯、授权说明和答辩口径统一。
@Entity(
    tableName = "drug_master",
    indices = [
        Index(value = ["generic_name"]),
        Index(value = ["trade_name"]),
        Index(value = ["approval_no"], unique = true),
        Index(value = ["pinyin_key"]),
        Index(value = ["initials_key"])
    ]
)
data class DrugMasterEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "drug_id")
    val drugId: Long = 0L,
    @ColumnInfo(name = "generic_name")
    val genericName: String,
    @ColumnInfo(name = "trade_name")
    val tradeName: String? = null,
    @ColumnInfo(name = "approval_no")
    val approvalNo: String? = null,
    val manufacturer: String? = null,
    @ColumnInfo(name = "dosage_form")
    val dosageForm: String? = null,
    val specification: String? = null,
    @ColumnInfo(name = "category_name")
    val categoryName: String? = null,
    @ColumnInfo(name = "pinyin_key")
    val pinyinKey: String? = null,
    @ColumnInfo(name = "initials_key")
    val initialsKey: String? = null,
    @ColumnInfo(name = "search_tokens")
    val searchTokens: String? = null,
    @ColumnInfo(name = "source_tag")
    val sourceTag: String? = null,
    @ColumnInfo(name = "license_note")
    val licenseNote: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
