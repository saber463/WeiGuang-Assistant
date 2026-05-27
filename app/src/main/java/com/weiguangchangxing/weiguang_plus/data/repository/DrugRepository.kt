package com.weiguangchangxing.weiguang_plus.data.repository

// DrugInfo 是页面层直接消费的聚合模型：
// 它把 Room 多张表的数据整合成一个安全可展示的结构，
// 这里显式把“页面展示文案”和“TTS 播报文案”分开，避免同一段文本同时承担视觉展示和语音播报两种职责。
// 同时把说明书扩展字段、来源标签和授权备注一起透出，保证页面层不需要再反向拼数据库细节。
data class DrugInfo(
    val drugId: Long,
    val genericName: String,
    val tradeName: String?,
    val approvalNo: String?,
    val manufacturer: String?,
    val dosageForm: String?,
    val specification: String?,
    val categoryName: String?,
    val composition: String?,
    val indication: String?,
    val usageAndDosage: String?,
    val taboo: String?,
    val attention: String?,
    val adverseReaction: String?,
    val interactionText: String?,
    val storageMethod: String?,
    val validPeriod: String?,
    val packageInfo: String?,
    val ttsSummary: String?,
    val aliases: List<String>,
    val riskPrompts: List<String>,
    val riskTtsPrompts: List<String>,
    val highestRiskLevel: String?,
    val signKeywords: List<String>,
    val signDisplayText: String?,
    val signVideoPath: String?,
    val sourceTag: String?,
    val licenseNote: String?
)

// Repository 层向页面暴露统一药品查询能力，屏蔽底层数据库与数据源组织细节。
interface DrugRepository {
    suspend fun searchByKeyword(keyword: String): List<DrugInfo>
    suspend fun getDrugInfo(drugId: Long): DrugInfo?
    suspend fun saveUserProfile(profile: DrugUserProfile): Long

    // 语音搜索专用：根据过敏关键词搜索所有药品规则表中 match_field='allergy_tag' 且 match_value 包含该关键词的药品
    suspend fun searchByAllergy(allergyKeyword: String): List<DrugInfo>
}

// 用户健康档案聚合模型，供风险规则引擎和未来的个性化提醒使用。
data class DrugUserProfile(
    val userId: Long = 0L,
    val nickname: String? = null,
    val ageGroup: String? = null,
    val diseaseTagsJson: String = "[]",
    val allergyTagsJson: String = "[]",
    val currentDrugsJson: String = "[]",
    val notes: String? = null
)
