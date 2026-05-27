package com.weiguangchangxing.weiguang_plus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.weiguangchangxing.weiguang_plus.data.local.entity.DrugRuleEntity

// 风险规则 DAO：
// 负责读写药品风险规则。这里显式按 high / medium / low 做排序，
// 让 UI 层优先拿到最需要人工复核的高风险提示。
@Dao
interface DrugRuleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRules(items: List<DrugRuleEntity>)

    @Query(
        """
        SELECT * FROM drug_rule
        WHERE enabled = 1
          AND (drug_id = :drugId OR drug_id IS NULL)
        ORDER BY CASE risk_level
            WHEN 'high' THEN 3
            WHEN 'medium' THEN 2
            WHEN 'low' THEN 1
            ELSE 0
        END DESC
        """
    )
    suspend fun getRulesForDrug(drugId: Long): List<DrugRuleEntity>

    @Query(
        """
        SELECT * FROM drug_rule
        WHERE enabled = 1
          AND match_field = 'allergy_tag'
          AND match_value LIKE '%' || :keyword || '%'
        ORDER BY CASE risk_level
            WHEN 'high' THEN 3
            WHEN 'medium' THEN 2
            WHEN 'low' THEN 1
            ELSE 0
        END DESC
        """
    )
    suspend fun searchRulesByMatchValue(keyword: String): List<DrugRuleEntity>
}
