package com.weiguangplus.data.model

/**
 * 手语学习词条数据模型
 *
 * 服务于 G7「手语学习 / 词典 / 纠错」模块：
 * - 词典查询：由「文字含义」→ 对应手势（gestureLabel 映射到手势向量库/手势库）
 * - 学习教程：按场景分类 + 难度分级展示词条
 * - 练习纠错：期望手指状态（expected）作为「标准动作」目标，与相机实时关键点对比评分
 *
 * 说明：手势库字符集有限（见 SignCourseCatalog），因此每课词条同时给出
 * 「手势英文标签（程序用）」与「中文动作要点（人读用）」两条辅助信息。
 */

/** 单根手指 / 拇指-食指接触的期望状态 */
enum class FingerExpectation {
    /** 手指应弯曲 */
    BENT,
    /** 手指应伸直 */
    STRAIGHT,
    /** 不确定 / 不检查该手指（灵活手指不受约束） */
    ANY
}

/**
 * 标准手势目标描述 —— 作为练习纠错的标准参照。
 * 每根手指（拇指/食指/中指/无名指/小指）的期望弯曲状态，
 * 以及「拇指是否与食指接触」（用于判断 OK/拿捏类手势）。
 */
data class GestureTarget(
    val thumb: FingerExpectation = FingerExpectation.ANY,
    val index: FingerExpectation = FingerExpectation.ANY,
    val middle: FingerExpectation = FingerExpectation.ANY,
    val ring: FingerExpectation = FingerExpectation.ANY,
    val pinky: FingerExpectation = FingerExpectation.ANY,
    /** 期望拇指与食指接触（如 OK 手势） */
    val thumbIndexContact: Boolean? = null
)

/**
 * 手语学习词条
 *
 * @param id 词条唯一标识（英文，如 "ok_sign"）
 * @param chinese 中文词义（用户查询/展示的语义）
 * @param textTranslation 识别后播报/展示的完整语句
 * @param scene 场景分类（就医/出行/购物/日常/求助）
 * @param difficulty 难度 1~3（1 最基础、3 进阶）
 * @param tips 动作要点说明（面向学习者的人读指导）
 * @param target 标准手指目标状态（供纠错评分）
 */
data class SignCourseItem(
    val id: String,
    val chinese: String,
    val textTranslation: String,
    val scene: String,
    val difficulty: Int,
    val tips: String,
    val target: GestureTarget
)