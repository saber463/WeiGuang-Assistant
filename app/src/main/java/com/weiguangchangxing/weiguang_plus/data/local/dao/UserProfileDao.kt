package com.weiguangchangxing.weiguang_plus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.weiguangchangxing.weiguang_plus.data.local.entity.UserProfileEntity

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity): Long

    @Query("SELECT * FROM user_profile WHERE user_id = :userId LIMIT 1")
    suspend fun getProfileById(userId: Long): UserProfileEntity?

    @Query("SELECT * FROM user_profile ORDER BY user_id ASC LIMIT 1")
    suspend fun getDefaultProfile(): UserProfileEntity?
}
