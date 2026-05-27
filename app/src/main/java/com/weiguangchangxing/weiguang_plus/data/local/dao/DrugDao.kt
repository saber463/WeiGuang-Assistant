package com.weiguangchangxing.weiguang_plus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.weiguangchangxing.weiguang_plus.data.local.entity.DrugAliasEntity
import com.weiguangchangxing.weiguang_plus.data.local.entity.DrugDetailEntity
import com.weiguangchangxing.weiguang_plus.data.local.entity.DrugMasterEntity
import com.weiguangchangxing.weiguang_plus.data.local.entity.DrugSignMappingEntity

@Dao
interface DrugDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDrugMasters(items: List<DrugMasterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDrugAliases(items: List<DrugAliasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDrugDetails(items: List<DrugDetailEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDrugSignMappings(items: List<DrugSignMappingEntity>)

    @Query("SELECT COUNT(*) FROM drug_master")
    suspend fun countDrugMasters(): Int

    @Query(
        """
        SELECT * FROM drug_master
        WHERE generic_name LIKE '%' || :keyword || '%'
           OR trade_name LIKE '%' || :keyword || '%'
           OR approval_no = :keyword
           OR pinyin_key LIKE '%' || :keyword || '%'
           OR initials_key LIKE '%' || :keyword || '%'
           OR search_tokens LIKE '%' || :keyword || '%'
        ORDER BY generic_name ASC
        """
    )
    suspend fun searchDrugMasters(keyword: String): List<DrugMasterEntity>

    @Query(
        """
        SELECT dm.* FROM drug_master dm
        INNER JOIN drug_alias da ON dm.drug_id = da.drug_id
        WHERE da.alias_name LIKE '%' || :keyword || '%'
           OR da.normalized_alias LIKE '%' || :keyword || '%'
        ORDER BY dm.generic_name ASC
        """
    )
    suspend fun searchDrugMastersByAlias(keyword: String): List<DrugMasterEntity>

    @Query("SELECT * FROM drug_master WHERE drug_id = :drugId LIMIT 1")
    suspend fun getDrugMasterById(drugId: Long): DrugMasterEntity?

    @Query("SELECT * FROM drug_detail WHERE drug_id = :drugId LIMIT 1")
    suspend fun getDrugDetailByDrugId(drugId: Long): DrugDetailEntity?

    @Query("SELECT * FROM drug_alias WHERE drug_id = :drugId ORDER BY alias_name ASC")
    suspend fun getAliasesByDrugId(drugId: Long): List<DrugAliasEntity>

    @Query(
        """
        SELECT * FROM drug_sign_mapping
        WHERE drug_id = :drugId AND enabled = 1
        ORDER BY priority_no ASC, mapping_id ASC
        """
    )
    suspend fun getSignMappingsByDrugId(drugId: Long): List<DrugSignMappingEntity>

    @Transaction
    suspend fun searchDrugIds(keyword: String): List<Long> {
        val fromMaster = searchDrugMasters(keyword).map { it.drugId }
        val fromAlias = searchDrugMastersByAlias(keyword).map { it.drugId }
        return (fromMaster + fromAlias).distinct()
    }
}
