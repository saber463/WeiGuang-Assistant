/**
 * 文件名：DrugDao.kt
 * 功能描述：药品数据访问对象，提供Drug实体的CRUD操作接口
 * 所属模块：data/local（本地数据层）
 *
 * 操作说明：
 * - 所有查询方法均为suspend函数，在协程中异步执行
 * - 查询结果自动映射为Drug数据类
 * - 插入操作使用REPLACE策略，相同ID自动覆盖更新
 */
package com.weiguangplus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.weiguangplus.data.model.Drug

/**
 * Drug实体的Room DAO接口
 */
@Dao
interface DrugDao {

    /**
     * 获取所有药品列表
     *
     * @return 按ID排序的完整药品列表
     */
    @Query("SELECT * FROM drugs ORDER BY id ASC")
    suspend fun getAllDrugs(): List<Drug>

    /**
     * 按ID查询药品
     *
     * @param drugId 药品唯一标识
     * @return 匹配的药品，未找到返回null
     */
    @Query("SELECT * FROM drugs WHERE id = :drugId")
    suspend fun getDrugById(drugId: Long): Drug?

    /**
     * 按名称模糊搜索药品（通用名或商品名）
     *
     * @param keyword 搜索关键词
     * @return 匹配的药品列表
     */
    @Query("SELECT * FROM drugs WHERE genericName LIKE '%' || :keyword || '%' OR tradeName LIKE '%' || :keyword || '%'")
    suspend fun searchDrugs(keyword: String): List<Drug>

    /**
     * 按风险等级筛选药品
     *
     * @param level 风险等级（high/medium/low）
     * @return 匹配的药品列表
     */
    @Query("SELECT * FROM drugs WHERE riskLevel = :level")
    suspend fun getDrugsByRiskLevel(level: String): List<Drug>

    /**
     * 插入或更新药品（主键冲突时替换）
     *
     * @param drug 药品实体
     * @return 插入行的ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrug(drug: Drug): Long

    /**
     * 批量插入或更新药品
     *
     * @param drugs 药品列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrugs(drugs: List<Drug>)

    /**
     * 删除指定药品
     *
     * @param drugId 药品ID
     */
    @Query("DELETE FROM drugs WHERE id = :drugId")
    suspend fun deleteDrug(drugId: Long)

    /**
     * 获取药品总数
     *
     * @return 数据库中的药品记录数
     */
    @Query("SELECT COUNT(*) FROM drugs")
    suspend fun getDrugCount(): Int
}