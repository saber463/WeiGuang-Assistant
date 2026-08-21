package com.weiguangplus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.weiguangplus.data.model.TranscriptRecord

/**
 * 转录记录 DAO（G4）
 *
 * 提供转录记录的插入、查询（按时间倒序/按 id）与数量统计。
 * 全部为 suspend 协程方法，供 Repository 调用。
 */
@Dao
interface TranscriptDao {

    /** 插入一条转录记录，返回自增 id */
    @Insert
    suspend fun insert(record: TranscriptRecord): Long

    /** 查询全部转录记录，按时间倒序（最新在前） */
    @Query("SELECT * FROM transcript_records ORDER BY timestamp DESC")
    suspend fun getAll(): List<TranscriptRecord>

    /** 按 id 查询单条 */
    @Query("SELECT * FROM transcript_records WHERE id = :id")
    suspend fun getById(id: Long): TranscriptRecord?

    /** 统计总条数 */
    @Query("SELECT COUNT(*) FROM transcript_records")
    suspend fun count(): Int
}