package com.weiguangchangxing.weiguang_plus.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "drug_detail",
    foreignKeys = [
        ForeignKey(
            entity = DrugMasterEntity::class,
            parentColumns = ["drug_id"],
            childColumns = ["drug_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DrugDetailEntity(
    @PrimaryKey
    @ColumnInfo(name = "drug_id")
    val drugId: Long,
    val composition: String? = null,
    val indication: String? = null,
    @ColumnInfo(name = "usage_and_dosage")
    val usageAndDosage: String? = null,
    val taboo: String? = null,
    val attention: String? = null,
    @ColumnInfo(name = "adverse_reaction")
    val adverseReaction: String? = null,
    @ColumnInfo(name = "interaction_text")
    val interactionText: String? = null,
    @ColumnInfo(name = "storage_method")
    val storageMethod: String? = null,
    @ColumnInfo(name = "valid_period")
    val validPeriod: String? = null,
    @ColumnInfo(name = "package_info")
    val packageInfo: String? = null,
    @ColumnInfo(name = "tts_summary")
    val ttsSummary: String? = null,
    @ColumnInfo(name = "source_tag")
    val sourceTag: String? = null,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
