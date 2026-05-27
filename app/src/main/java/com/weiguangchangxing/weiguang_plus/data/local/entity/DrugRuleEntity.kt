package com.weiguangchangxing.weiguang_plus.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "drug_rule",
    foreignKeys = [
        ForeignKey(
            entity = DrugMasterEntity::class,
            parentColumns = ["drug_id"],
            childColumns = ["drug_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["drug_id"]),
        Index(value = ["match_field"]),
        Index(value = ["match_value"])
    ]
)
data class DrugRuleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rule_id")
    val ruleId: Long = 0L,
    @ColumnInfo(name = "drug_id")
    val drugId: Long? = null,
    @ColumnInfo(name = "match_field")
    val matchField: String,
    @ColumnInfo(name = "match_value")
    val matchValue: String,
    @ColumnInfo(name = "rule_type")
    val ruleType: String,
    @ColumnInfo(name = "risk_level")
    val riskLevel: String,
    @ColumnInfo(name = "display_message")
    val displayMessage: String,
    @ColumnInfo(name = "tts_message")
    val ttsMessage: String? = null,
    @ColumnInfo(name = "enabled")
    val enabled: Boolean = true,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
