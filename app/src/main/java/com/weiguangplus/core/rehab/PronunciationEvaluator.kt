package com.weiguangplus.core.rehab

/**
 * 发音评估引擎（G3 核心）
 *
 * 将用户的跟读识别文本与目标文本比对，输出正确度评分与逐字纠正建议。
 *
 * 实现思路：
 * 1. 文本归一化（去空白与标点，统一大小写）；
 * 2. 计算 Levenshtein 编辑距离（允许插入/删除/替换），归一化为 0~100 相似度评分；
 * 3. 对齐目标与识别文本（回溯编辑路径），定位错误的字符/子串；
 * 4. 针对中文常见易混淆（n/l、zh/z、前后鼻音、送气/不送气）匹配内置纠正建议；
 * 5. 融合课程自带「气息/舌位要点」，生成面向用户的可执行纠正建议。
 *
 * 说明：在缺少 Vosk 音素级输出的前提下，以「识别文本 vs 目标文本」的文本相似度
 * 作为发音准确度的可工程化代理指标；并可随技术演进替换为音素级对比，无需改动 UI。
 */
object PronunciationEvaluator {

    /** 评估结果 */
    data class EvaluationResult(
        val score: Int,                 // 0~100 正确度评分
        val hits: Int,                  // 正确字符数
        val totalChars: Int,            // 目标字符总数
        val errors: List<CharError>,    // 逐字错误明细
        val suggestions: List<String>   // 面向用户的纠正建议
    )

    /** 单处发音错误 */
    data class CharError(
        val expected: Char,  // 期望字符
        val actual: Char?,   // 实际识别到的字符（null = 漏读）
        val index: Int       // 在目标中的位置
    )

    /** 中文常见易混淆发音映射：错误音 → 建议 */
    private val confusionSuggestions = mapOf(
        "n" to "舌尖应抵住上齿龈，让气流从鼻腔通过（n/l 易混）。",
        "l" to "舌尖轻抵上颚两侧，气流从舌头两边送出（l 音舌侧出气）。",
        "zh" to "舌尖上翘抵住硬腭前部，z 音舌尖则平放抵上齿背。",
        "z" to "舌尖平放抵住上齿背，zh 音舌尖需上翘。",
        "ch" to "嘴唇微圆，舌尖由硬腭向前送气，气流较强。",
        "sh" to "舌尖后缩接近硬腭，气流摩擦送出口型收圆。",
        "c" to "舌尖抵上齿背，送气比 z 强，冲出气流。",
        "s" to "舌尖接近上齿背，气流摩擦通过，声音较轻。"
    )

    /**
     * 评估一次跟读结果
     *
     * @param target 目标训练文本
     * @param spoken 用户跟读的语音识别文本（可能含夹杂语气词/识别误差）
     * @param lessonTips 可选的课程气息/舌位要点，评估不理想时附加到建议中
     * @return 评估结果；target 为空时返回 null
     */
    fun evaluate(target: String, spoken: String, lessonTips: String? = null): EvaluationResult? {
        val expected = normalize(target)
        if (expected.isEmpty()) return null
        val actual = normalize(spoken)

        // 逐字编辑路径（替换矩阵），用于定位错误与计算距离
        val dist = matrix(expected, actual)
        val maxLen = maxOf(expected.length, actual.length)
        val editDistance = dist[expected.length][actual.length]
        val score = if (maxLen == 0) 100 else ((1f - editDistance / maxLen.toFloat()) * 100f).toInt()

        // 回溯对齐，收集命中数（按目标字符逐字符比对）
        val errors = collectErrors(expected, actual, dist)

        // 生成建议：基于易混淆音 + 课程要点
        val suggestions = buildSuggestions(errors, score, lessonTips)

        return EvaluationResult(
            score = score.coerceIn(0, 100),
            hits = expected.length - errors.count { it.actual != it.expected },
            totalChars = expected.length,
            errors = errors,
            suggestions = suggestions
        )
    }

    /** 归一化：去空白、标点，转小写 */
    private fun normalize(text: String): String =
        text.filterNot { it.isWhitespace() || it in "，。,、！？.!?， \"'" }.lowercase()

    /** 计算 Levenshtein 编辑距离矩阵 */
    private fun matrix(a: String, b: String): Array<IntArray> {
        val m = a.length
        val n = b.length
        val dp = Array(m + 1) { IntArray(n + 1) }
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,          // 删除 a[i]
                    dp[i][j - 1] + 1,           // 插入 b[j]
                    dp[i - 1][j - 1] + cost     // 匹配或替换
                )
            }
        }
        return dp
    }

    /**
     * 回溯编辑路径，定位目标每个字符的位置状态。
     * 对目标第 i 个字符：若命中最右侧同名匹配视为正确，否则记录为错误。
     */
    private fun collectErrors(a: String, b: String, dp: Array<IntArray>): List<CharError> {
        val errors = mutableListOf<CharError>()
        var i = a.length
        var j = b.length

        while (i > 0 && j > 0) {
            if (a[i - 1] == b[j - 1]) {
                i--
                j--
            } else {
                // 替换或错位，先按「替换」处理并消费一位；插入由得分综合取舍
                errors.add(CharError(a[i - 1], b[j - 1], i - 1))
                // 比较删除/替换/插入上一步成本，选择更小者继续回溯，减少错误错位
                val del = dp[i - 1][j]     // 目标删除该字符（漏读）
                val rep = dp[i - 1][j - 1] // 替换
                val ins = dp[i][j - 1]     // 识别插入字符
                if (rep <= del && rep <= ins) {
                    i--
                    j--
                } else if (del <= ins) {
                    errors.add(CharError(a[i - 1], null, i - 1)) // 漏读
                    i--
                } else {
                    j-- // 识别多出字符，仅占用位置
                }
            }
        }
        // 目标剩余字符 → 全为漏读
        while (i > 0) {
            errors.add(CharError(a[i - 1], null, i - 1))
            i--
        }
        return errors.sortedBy { it.index }
    }

    /**
     * 依据错误字符与评分生成纠正建议；评分达标时不额外提示错误。
     */
    private fun buildSuggestions(
        errors: List<CharError>,
        score: Int,
        lessonTips: String?
    ): List<String> {
        val result = mutableListOf<String>()

        if (score >= 85) {
            result.add("发音控制很好，继续保持！")
            return result
        }

        // 易混淆音纠正（按常见声母映射）
        errors.forEach { e ->
            if (e.actual != null && e.actual != e.expected) {
                val key = e.expected.toString()
                confusionSuggestions[key]?.let(result::add)
            }
        }

        // 未命中映射的，给通用建议
        if (result.isEmpty() && errors.isNotEmpty()) {
            result.add(
                "有 ${errors.count { it.actual == null }} 处漏读、" +
                    "${errors.count { it.actual != null && it.actual != it.expected }} 处读错，" +
                    "请放慢语速逐字跟读。"
            )
        }

        // 评分偏低时附加课程气息/舌位要点
        if (score < 60 && !lessonTips.isNullOrBlank()) {
            result.add(lessonTips)
        }

        return result.distinct().take(4)
    }
}