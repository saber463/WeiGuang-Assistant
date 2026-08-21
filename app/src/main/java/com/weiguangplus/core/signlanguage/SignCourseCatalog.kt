package com.weiguangplus.core.signlanguage

import com.weiguangplus.data.model.FingerExpectation
import com.weiguangplus.data.model.GestureTarget
import com.weiguangplus.data.model.SignCourseItem

/**
 * 手语学习词条目录（内置数据源）
 *
 * 为 G7 模块提供：
 * 1. 手语词典：全部词条，供「文字 → 手势」查询；
 * 2. 学习教程：按场景分类 + 难度分级的课程结构；
 * 3. 练习纠错：每词条内置标准手指目标状态（GestureTarget），
 *    供 SignLanguageCorrector 与用户相机实时关键点比对评分。
 *
 * 词条的手势目标状态与 GestureClassifier 的几何分类规则保持一致，
 * 保证「识别」与「纠错」对同一手势的判读口径一致，避免前后矛盾。
 */
object SignCourseCatalog {

    /** 全部词条（也是词典数据源） */
    val all: List<SignCourseItem> by lazy {
        listOf(
            // === 求助 / 紧急（难度 1）===
            SignCourseItem(
                id = "fist",
                chinese = "救命",
                textTranslation = "救命！需要帮助！",
                scene = "求助",
                difficulty = 1,
                tips = "五指用力握拳，拳心朝向对方，表情紧张以强化求救信号。",
                target = GestureTarget(
                    thumb = FingerExpectation.BENT, index = FingerExpectation.BENT,
                    middle = FingerExpectation.BENT, ring = FingerExpectation.BENT,
                    pinky = FingerExpectation.BENT
                )
            ),
            SignCourseItem(
                id = "open_palm",
                chinese = "帮助 / 停止",
                textTranslation = "请帮帮我 / 停止",
                scene = "求助",
                difficulty = 1,
                tips = "五指自然张开，掌心朝向镜头，手指要完全伸展开。",
                target = GestureTarget(
                    thumb = FingerExpectation.STRAIGHT, index = FingerExpectation.STRAIGHT,
                    middle = FingerExpectation.STRAIGHT, ring = FingerExpectation.STRAIGHT,
                    pinky = FingerExpectation.STRAIGHT
                )
            ),

            // === 日常交流（难度 1）===
            SignCourseItem(
                id = "thumbs_up",
                chinese = "好的 / 确认",
                textTranslation = "好的 / 确认",
                scene = "日常",
                difficulty = 1,
                tips = "拇指竖立并保持固定，其余四指握拳，拇指指尖朝上。",
                target = GestureTarget(
                    thumb = FingerExpectation.STRAIGHT, index = FingerExpectation.BENT,
                    middle = FingerExpectation.BENT, ring = FingerExpectation.BENT,
                    pinky = FingerExpectation.BENT
                )
            ),
            SignCourseItem(
                id = "ok_sign",
                chinese = "没问题",
                textTranslation = "好的，没问题",
                scene = "日常",
                difficulty = 1,
                tips = "拇指与食指指尖相接成圆环，其余三指保持自然伸直。",
                target = GestureTarget(
                    thumb = FingerExpectation.ANY, index = FingerExpectation.ANY,
                    middle = FingerExpectation.STRAIGHT, ring = FingerExpectation.STRAIGHT,
                    pinky = FingerExpectation.STRAIGHT, thumbIndexContact = true
                )
            ),
            SignCourseItem(
                id = "peace",
                chinese = "胜利 / 二",
                textTranslation = "好 / 胜利",
                scene = "日常",
                difficulty = 1,
                tips = "食指与中指并排伸直成 V 形，无名指与小指弯曲收拢。",
                target = GestureTarget(
                    thumb = FingerExpectation.ANY, index = FingerExpectation.STRAIGHT,
                    middle = FingerExpectation.STRAIGHT, ring = FingerExpectation.BENT,
                    pinky = FingerExpectation.BENT
                )
            ),
            SignCourseItem(
                id = "point_index",
                chinese = "指方向 / 那个",
                textTranslation = "那个 / 指方向",
                scene = "日常",
                difficulty = 1,
                tips = "仅食指伸直指向目标，其余手指弯曲握拢。",
                target = GestureTarget(
                    thumb = FingerExpectation.BENT, index = FingerExpectation.STRAIGHT,
                    middle = FingerExpectation.BENT, ring = FingerExpectation.BENT,
                    pinky = FingerExpectation.BENT
                )
            ),

            // === 情感 / 礼貌（难度 2）===
            SignCourseItem(
                id = "heart",
                chinese = "谢谢",
                textTranslation = "谢谢",
                scene = "日常",
                difficulty = 2,
                tips = "拇指与食指指尖相接呈心形，其余手指自然伸直贴合。",
                target = GestureTarget(
                    thumb = FingerExpectation.ANY, index = FingerExpectation.ANY,
                    middle = FingerExpectation.STRAIGHT, ring = FingerExpectation.STRAIGHT,
                    pinky = FingerExpectation.STRAIGHT, thumbIndexContact = true
                )
            ),

            // === 问候（难度 2）===
            SignCourseItem(
                id = "wave",
                chinese = "你好 / 再见",
                textTranslation = "你好 / 再见",
                scene = "日常",
                difficulty = 2,
                tips = "五指并拢自然伸开，手腕左右摆动，如同挥手招呼。",
                target = GestureTarget(
                    thumb = FingerExpectation.STRAIGHT, index = FingerExpectation.STRAIGHT,
                    middle = FingerExpectation.STRAIGHT, ring = FingerExpectation.STRAIGHT,
                    pinky = FingerExpectation.STRAIGHT
                )
            ),

            // === 进阶表达（难度 3）===
            SignCourseItem(
                id = "call_me",
                chinese = "打电话",
                textTranslation = "打电话",
                scene = "日常",
                difficulty = 3,
                tips = "拇指伸直贴向耳旁，其余四指弯曲握拢，模拟手持听筒。",
                target = GestureTarget(
                    thumb = FingerExpectation.STRAIGHT, index = FingerExpectation.BENT,
                    middle = FingerExpectation.BENT, ring = FingerExpectation.BENT,
                    pinky = FingerExpectation.BENT
                )
            )
        )
    }

    /** 所有场景分类（按出现顺序去重） */
    val scenes: List<String> by lazy { all.map { it.scene }.distinct() }

    /** 按场景分组：场景 -> 词条列表 */
    val groupedByScene: Map<String, List<SignCourseItem>> by lazy { all.groupBy { it.scene } }

    /** 按难度分组：难度 -> 词条列表 */
    val groupedByDifficulty: Map<Int, List<SignCourseItem>> by lazy { all.groupBy { it.difficulty } }

    /** 按中文词义/语句关键字做词典精确或包含匹配 */
    fun search(query: String, maxResults: Int = 20): List<SignCourseItem> {
        val kw = query.trim()
        if (kw.isEmpty()) return all
        return all.filter {
            it.chinese.contains(kw) || it.textTranslation.contains(kw) || it.id.contains(kw)
        }.take(maxResults)
    }

    /** 根据词条 id 精确取词条 */
    fun findById(id: String): SignCourseItem? = all.firstOrNull { it.id == id }
}