package com.weiguangplus.core

data class EnhancedDrugResult(
    val drugName: String?,
    val genericName: String?,
    val confidence: Float,
    val dosage: String?,
    val manufactureDate: String?,
    val expiryDate: String?,
    val batchNumber: String?,
    val riskLevel: String = "low",
    val rawText: String,
    val warnings: List<String> = emptyList()
)

object EnhancedOcrEngine {

    private val drugLibrary = mapOf(
        "999感冒灵" to "感冒药", "感冒灵" to "感冒药", "感康" to "感冒药",
        "白加黑" to "感冒药", "新康泰克" to "感冒药", "氨酚黄那敏" to "感冒药",
        "连花清瘟" to "感冒药", "藿香正气" to "感冒药", "快克" to "感冒药",
        "泰诺" to "感冒药",
        "布洛芬" to "解热镇痛药", "芬必得" to "解热镇痛药",
        "对乙酰氨基酚" to "解热镇痛药", "散利痛" to "解热镇痛药",
        "阿司匹林" to "解热镇痛药", "萘普生" to "解热镇痛药",
        "扶他林" to "外用镇痛药",
        "阿莫西林" to "抗生素", "头孢" to "抗生素", "阿奇霉素" to "抗生素",
        "罗红霉素" to "抗生素", "左氧氟沙星" to "抗生素", "青霉素" to "抗生素",
        "红霉素" to "抗生素", "克拉霉素" to "抗生素",
        "吗丁啉" to "胃药", "达喜" to "胃药", "奥美拉唑" to "胃药",
        "三九胃泰" to "胃药", "丽珠得乐" to "胃药", "健胃消食片" to "胃药",
        "诺氟沙星" to "肠道用药", "思密达" to "肠道用药", "蒙脱石散" to "肠道用药",
        "黄连素" to "肠道用药",
        "急支糖浆" to "止咳药", "甘草片" to "止咳药", "咳必清" to "止咳药",
        "念慈菴" to "止咳药", "川贝枇杷" to "止咳药", "氨溴索" to "化痰药",
        "维生素C" to "维生素", "维C" to "维生素", "钙片" to "补充剂",
        "葡萄糖酸锌" to "补充剂", "褪黑素" to "补充剂",
        "创可贴" to "外用", "云南白药" to "外用", "红花油" to "外用",
        "风油精" to "外用", "皮炎平" to "外用", "无极膏" to "外用",
        "痔疮膏" to "外用",
        "硝酸甘油" to "心血管药", "速效救心丸" to "心血管药",
        "复方丹参" to "心血管药", "降压药" to "心血管药", "倍他乐克" to "心血管药",
        "胰岛素" to "糖尿病药", "二甲双胍" to "糖尿病药", "格列美脲" to "糖尿病药",
        "氯雷他定" to "抗过敏药", "扑尔敏" to "抗过敏药", "西替利嗪" to "抗过敏药"
    )

    private val highRiskKeywords = setOf(
        "处方药", "抗生素", "头孢", "青霉素", "胰岛素",
        "硝酸甘油", "华法林", "地高辛", "甲氨蝶呤"
    )
    private val liverRiskKeywords = setOf(
        "对乙酰氨基酚", "布洛芬", "他汀", "利福平"
    )
    private val kidneyRiskKeywords = setOf(
        "布洛芬", "阿司匹林", "庆大霉素", "万古霉素"
    )

    fun recognize(rawText: String): EnhancedDrugResult {
        val cleaned = cleanText(rawText)
        val (drugName, genericName, confidence) = fuzzyMatchDrug(cleaned)
        val dosage = extractDosage(cleaned)
        val batch = extractBatchNumber(cleaned)
        val (manuDate, expiryDate) = extractDates(cleaned)
        val riskLevel = assessRisk(drugName, genericName)
        val warnings = generateWarnings(drugName, genericName, riskLevel)
        return EnhancedDrugResult(
            drugName = drugName, genericName = genericName,
            confidence = confidence, dosage = dosage,
            manufactureDate = manuDate, expiryDate = expiryDate,
            batchNumber = batch, riskLevel = riskLevel,
            rawText = cleaned, warnings = warnings
        )
    }

    private fun fuzzyMatchDrug(text: String): Triple<String?, String?, Float> {
        if (text.isBlank()) return Triple(null, null, 0f)
        var bestName: String? = null
        var bestCategory: String? = null
        var bestScore = 0f
        for ((name, category) in drugLibrary) {
            if (text.contains(name)) return Triple(name, category, 0.95f)
            val similarity = textSimilarity(text, name)
            if (similarity > bestScore && similarity > 0.6f) {
                bestScore = similarity
                bestName = name
                bestCategory = category
            }
        }
        return Triple(bestName, bestCategory, bestScore)
    }

    private fun extractDosage(text: String): String? {
        val pattern = Regex("""\d+\.?\d*\s*(mg|g|ml|μg|片|粒|支)""", RegexOption.IGNORE_CASE)
        return pattern.find(text)?.value
    }

    private fun extractBatchNumber(text: String): String? {
        val pattern = Regex("""批号[：:]\s*([A-Za-z0-9]+)""")
        return pattern.find(text)?.groupValues?.getOrNull(1)
    }

    private fun extractDates(text: String): Pair<String?, String?> {
        val prodPattern = Regex("""生产日期[：:]\s*(\d{4}[-/年]\d{1,2}[-/月]\d{1,2})""")
        val expPattern = Regex("""有效期(?:至)?[：:]\s*(\d{4}[-/年]\d{1,2}[-/月]\d{1,2})""")
        val prodMatch = prodPattern.find(text)?.groupValues?.getOrNull(1)
        val expMatch = expPattern.find(text)?.groupValues?.getOrNull(1)
        return Pair(prodMatch, expMatch)
    }

    private fun assessRisk(drugName: String?, genericName: String?): String {
        val combined = "${drugName ?: ""}${genericName ?: ""}"
        return when {
            highRiskKeywords.any { combined.contains(it) } -> "high"
            liverRiskKeywords.any { combined.contains(it) } -> "medium"
            kidneyRiskKeywords.any { combined.contains(it) } -> "medium"
            genericName == "抗生素" -> "medium"
            else -> "low"
        }
    }

    private fun generateWarnings(
        drugName: String?, genericName: String?, riskLevel: String
    ): List<String> {
        val warnings = mutableListOf<String>()
        when (riskLevel) {
            "high" -> warnings.add("高风险药品，请遵医嘱使用")
            "medium" -> warnings.add("请确认无肝肾功能异常后使用")
        }
        if (genericName == "抗生素") {
            warnings.add("抗生素需在医生指导下使用，不可自行停药")
        }
        return warnings
    }

    private fun cleanText(raw: String): String {
        return raw
            .replace("\r\n", "\n")
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\n{2,}"), "\n")
            .trim()
    }

    private fun textSimilarity(text: String, target: String): Float {
        if (target.length >= 3 && text.contains(target)) return 0.9f
        val parts = target.chunked(2).filter { it.length == 2 }
        val matched = parts.count { text.contains(it) }
        if (parts.isNotEmpty()) {
            val ratio = matched.toFloat() / parts.size
            if (ratio > 0.5f) return 0.6f + ratio * 0.3f
        }
        return 0f
    }
}
