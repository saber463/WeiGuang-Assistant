package com.weiguangchangxing.weiguang_plus.data.repository

import com.weiguangchangxing.weiguang_plus.data.local.dao.DrugDao
import com.weiguangchangxing.weiguang_plus.data.local.dao.DrugRuleDao
import com.weiguangchangxing.weiguang_plus.data.local.dao.UserProfileDao
import com.weiguangchangxing.weiguang_plus.data.local.entity.UserProfileEntity

// 本地药品仓库实现：
// 负责把多张 Room 表聚合成页面可直接消费的 DrugInfo，
// 同时在这里统一计算最高风险级别、来源标签和授权备注。
// 这一层还承担一个关键职责：把规则表里的展示文案和 TTS 文案拆开聚合，
// 避免页面层继续直接依赖底层字段名，后续换数据源时也更容易保持稳定。
class LocalDrugRepository(
    private val drugDao: DrugDao,
    private val drugRuleDao: DrugRuleDao,
    private val userProfileDao: UserProfileDao
) : DrugRepository {

    override suspend fun searchByKeyword(keyword: String): List<DrugInfo> {
        if (keyword.isBlank()) return emptyList()
        val drugIds = drugDao.searchDrugIds(keyword.trim())
        return drugIds.mapNotNull { getDrugInfo(it) }
    }

    override suspend fun getDrugInfo(drugId: Long): DrugInfo? {
        val master = drugDao.getDrugMasterById(drugId) ?: return null
        val detail = drugDao.getDrugDetailByDrugId(drugId)
        val aliases = drugDao.getAliasesByDrugId(drugId).map { it.aliasName }
        val rules = drugRuleDao.getRulesForDrug(drugId)
        val ruleMessages = rules.map { it.displayMessage }
        val ruleTtsMessages = rules.mapNotNull { it.ttsMessage ?: it.displayMessage }
        val signMappings = drugDao.getSignMappingsByDrugId(drugId)
        val primarySign = signMappings.firstOrNull()

        return DrugInfo(
            drugId = master.drugId,
            genericName = master.genericName,
            tradeName = master.tradeName,
            approvalNo = master.approvalNo,
            manufacturer = master.manufacturer,
            dosageForm = master.dosageForm,
            specification = master.specification,
            categoryName = master.categoryName,
            composition = detail?.composition,
            indication = detail?.indication,
            usageAndDosage = detail?.usageAndDosage,
            taboo = detail?.taboo,
            attention = detail?.attention,
            adverseReaction = detail?.adverseReaction,
            interactionText = detail?.interactionText,
            storageMethod = detail?.storageMethod,
            validPeriod = detail?.validPeriod,
            packageInfo = detail?.packageInfo,
            ttsSummary = detail?.ttsSummary,
            aliases = aliases,
            riskPrompts = ruleMessages,
            riskTtsPrompts = ruleTtsMessages,
            highestRiskLevel = rules.highestRiskLevel(),
            signKeywords = signMappings.map { it.signKeyword }.distinct(),
            signDisplayText = primarySign?.signDisplayText,
            signVideoPath = primarySign?.videoPath,
            sourceTag = master.sourceTag,
            licenseNote = master.licenseNote
        )
    }

    override suspend fun saveUserProfile(profile: DrugUserProfile): Long {
        return userProfileDao.upsertProfile(
            UserProfileEntity(
                userId = profile.userId,
                nickname = profile.nickname,
                ageGroup = profile.ageGroup,
                diseaseTags = profile.diseaseTagsJson,
                allergyTags = profile.allergyTagsJson,
                currentDrugs = profile.currentDrugsJson,
                notes = profile.notes
            )
        )
    }

    override suspend fun searchByAllergy(allergyKeyword: String): List<DrugInfo> {
        if (allergyKeyword.isBlank()) return emptyList()
        val matchingRules = drugRuleDao.searchRulesByMatchValue(allergyKeyword.trim())
        val drugIds = matchingRules.mapNotNull { it.drugId }.distinct()
        return drugIds.mapNotNull { getDrugInfo(it) }
    }
}

private fun List<com.weiguangchangxing.weiguang_plus.data.local.entity.DrugRuleEntity>.highestRiskLevel(): String? {
    return when {
        any { it.riskLevel == "high" } -> "high"
        any { it.riskLevel == "medium" } -> "medium"
        any { it.riskLevel == "low" } -> "low"
        else -> null
    }
}
