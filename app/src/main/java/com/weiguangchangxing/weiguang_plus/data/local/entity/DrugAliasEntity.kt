package com.weiguangchangxing.weiguang_plus.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "drug_alias",
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
        Index(value = ["alias_name"]),
        Index(value = ["normalized_alias"])
    ]
)
data class DrugAliasEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "alias_id")
    val aliasId: Long = 0L,
    @ColumnInfo(name = "drug_id")
    val drugId: Long,
    @ColumnInfo(name = "alias_name")
    val aliasName: String,
    @ColumnInfo(name = "alias_type")
    val aliasType: String = "alias",
    @ColumnInfo(name = "normalized_alias")
    val normalizedAlias: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
