package com.weiguangplus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.weiguangplus.data.model.QuickPhrase

/**
 * 快捷短语 DAO（G12）
 *
 * 提供短语的插入（冲突忽略）、全量查询（按分类+排序）、按 id 删除、
 * 按文本查重（用于添加时判定是否已存在）。
 */
@Dao
interface QuickPhraseDao {

    /** 插入单条（若同一条已存在则忽略，用于预设短语幂等初始化） */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(phrase: QuickPhrase): Long

    /** 插入多条（预设短语批量初始化） */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(phrases: List<QuickPhrase>)

    /** 查询全部，按分类 + 排序 */
    @Query("SELECT * FROM quick_phrases ORDER BY category, sortOrder ASC")
    suspend fun getAll(): List<QuickPhrase>

    /** 删除单条 */
    @Query("DELETE FROM quick_phrases WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** 按文本查询（判断是否已存在） */
    @Query("SELECT * FROM quick_phrases WHERE text = :text LIMIT 1")
    suspend fun findByText(text: String): QuickPhrase?
}