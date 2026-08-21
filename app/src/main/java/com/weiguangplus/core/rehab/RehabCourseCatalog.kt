package com.weiguangplus.core.rehab

import com.weiguangplus.data.model.RehabLesson
import com.weiguangplus.data.model.RehabStage

/**
 * 言语康复训练课程数据源（内置）
 *
 * 提供面向听障/言语障碍用户的家庭发音训练课程，按「单音素 → 音节 → 词语 → 短语」
 * 四级递进（参考 SLP 构音训练临床流程）。内置课程覆盖中文常见易混淆音与高频日常用语。
 *
 * 后续如需增加课程/支持用户自定义，可在不改变调用方的前提下扩充该数据源，
 * 或升级为 Room 持久化词库。
 */
object RehabCourseCatalog {

    /** 全部课程 */
    val all: List<RehabLesson> by lazy {
        listOf(
            // === 一级：单音素 ===
            RehabLesson(
                id = "phone_zh", target = "zh", stage = RehabStage.PHONE.label,
                category = "声母", difficulty = 1,
                mouthDesc = "舌尖上翘抵住硬腭前部，让气流从缝隙中摩擦通过。",
                breathDesc = "舌尖抵住上颚出气，注意舌面不要贴平。"
            ),
            RehabLesson(
                id = "phone_z", target = "z", stage = RehabStage.PHONE.label,
                category = "声母", difficulty = 1,
                mouthDesc = "舌尖平放抵住上齿背，气流从窄缝挤出。",
                breathDesc = "牙齿咬合，舌尖平放，不要翘起。"
            ),
            RehabLesson(
                id = "phone_n", target = "n", stage = RehabStage.PHONE.label,
                category = "声母", difficulty = 1,
                mouthDesc = "舌尖抵住上齿龈，气流从鼻腔通过。",
                breathDesc = "舌尖顶住上齿龈，让声音从鼻子出来。"
            ),
            RehabLesson(
                id = "phone_l", target = "l", stage = RehabStage.PHONE.label,
                category = "声母", difficulty = 1,
                mouthDesc = "舌尖抵住上齿龈，气流从舌头两侧通过。",
                breathDesc = "舌尖轻抵上颚两侧留缝，气流从两边送出。"
            ),

            // === 二级：音节 ===
            // 针对「n/l 不分」的对比音节
            RehabLesson(
                id = "syl_na", target = "na(那)", stage = RehabStage.SYLLABLE.label,
                category = "对比训练", difficulty = 2,
                mouthDesc = "舌尖顶住上齿龈，先让气流从鼻腔冲出再收。",
                breathDesc = "发 na 时鼻音明显，勿轻带为 da。"
            ),
            RehabLesson(
                id = "syl_la", target = "la(拉)", stage = RehabStage.SYLLABLE.label,
                category = "对比训练", difficulty = 2,
                mouthDesc = "舌尖上翘抵住硬腭前部，气流从舌两侧冲出。",
                breathDesc = "发 la 时气流走舌头两边，勿走鼻腔。"
            ),

            // === 三级：词语 ===
            RehabLesson(
                id = "word_nihao", target = "你好", stage = RehabStage.WORD.label,
                category = "日常用语", difficulty = 2,
                mouthDesc = "先发 ni 上下唇轻拢，再接 hao 张口圆唇。",
                breathDesc = "两个音节连贯，字与字之间气息不要断开。"
            ),
            RehabLesson(
                id = "word_xiexie", target = "谢谢", stage = RehabStage.WORD.label,
                category = "日常用语", difficulty = 2,
                mouthDesc = "发 xie 舌面前部抬近上腭，送气要轻。",
                breathDesc = "第一声与第二声衔接，舌位平稳不偏移。"
            ),
            RehabLesson(
                id = "word_mama", target = "妈妈", stage = RehabStage.WORD.label,
                category = "亲子称谓", difficulty = 1,
                mouthDesc = "双唇轻闭后突然放开，气流从鼻腔冲出。",
                breathDesc = "唇部放松，自然靠拢后弹出。"
            ),

            // === 四级：短语 ===
            RehabLesson(
                id = "phrase_help", target = "请你帮帮我", stage = RehabStage.PHRASE.label,
                category = "求助表达", difficulty = 3,
                mouthDesc = "语句放缓，拉长韵母，让每个字清晰可辨。",
                breathDesc = "完整语句，句尾收束，避免吞音。"
            ),
            RehabLesson(
                id = "phrase_happy", target = "我今天很开心", stage = RehabStage.PHRASE.label,
                category = "情感表达", difficulty = 3,
                mouthDesc = "以自然语速表达，节奏舒缓，字字清楚。",
                breathDesc = "连读不吞字，重点突出'开心'二字。"
            )
        )
    }

    /** 按阶段分组：阶段 -> 课程 */
    val groupedByStage: Map<String, List<RehabLesson>> by lazy { all.groupBy { it.stage } }

    /** 按分类分组：分类 -> 课程 */
    val groupedByCategory: Map<String, List<RehabLesson>> by lazy { all.groupBy { it.category } }

    /** 按课程 id 精确取 */
    fun findById(id: String): RehabLesson? = all.firstOrNull { it.id == id }
}