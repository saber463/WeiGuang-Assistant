package com.weiguangchangxing.weiguang_plus.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    val userId: Long = 0L,
    val nickname: String? = null,
    @ColumnInfo(name = "age_group")
    val ageGroup: String? = null,
    @ColumnInfo(name = "disease_tags")
    val diseaseTags: String = "[]",
    @ColumnInfo(name = "allergy_tags")
    val allergyTags: String = "[]",
    @ColumnInfo(name = "current_drugs")
    val currentDrugs: String = "[]",
    val notes: String? = null,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
