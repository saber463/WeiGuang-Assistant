package com.weiguangchangxing.weiguang_plus.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "drug_sign_mapping",
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
        Index(value = ["sign_keyword"])
    ]
)
data class DrugSignMappingEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "mapping_id")
    val mappingId: Long = 0L,
    @ColumnInfo(name = "drug_id")
    val drugId: Long,
    @ColumnInfo(name = "sign_keyword")
    val signKeyword: String,
    @ColumnInfo(name = "sign_display_text")
    val signDisplayText: String? = null,
    @ColumnInfo(name = "video_path")
    val videoPath: String? = null,
    @ColumnInfo(name = "image_seq_path")
    val imageSeqPath: String? = null,
    @ColumnInfo(name = "spelling_mode")
    val spellingMode: String = "word",
    @ColumnInfo(name = "priority_no")
    val priorityNo: Int = 1,
    @ColumnInfo(name = "enabled")
    val enabled: Boolean = true,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
