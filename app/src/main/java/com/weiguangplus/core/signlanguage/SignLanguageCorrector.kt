package com.weiguangplus.core.signlanguage

import com.weiguangplus.data.model.FingerExpectation
import com.weiguangplus.data.model.GestureTarget
import kotlin.math.sqrt

/**
 * 手语手势纠错器
 *
 * 职责：将用户实时手部关键点（21 点）与标准动作目标（GestureTarget）逐项比对，
 * 计算「正确度评分」并产出逐条纠正建议。服务于 G7 的练习纠错场景。
 *
 * 核心算法：
 * - 对每根手指计算「是否弯曲」（复用与 GestureClassifier 一致的距离判据：
 *   指尖到手腕距离 < 根部到手腕距离 × 0.85 视为弯曲），与期望状态比对；
 * - 计算期望为「拇指-食指接触」的手势（如 OK / 比心）时，额外比较两指尖距离；
 * - 综合通过项占比 → 0~100 分评分，并针对未通过项生成对应纠正文案。
 *
 * 纯函数实现，不依赖相机/UI，便于在各练习界面复用与测试。
 */
class SignLanguageCorrector {

    /** 手指关键点索引（与 MediaPipe 21 点约定一致） */
    companion object {
        private const val WRIST = 0
        private const val THUMB_TIP = 4
        private const val INDEX_TIP = 8
        // 各手指骨骼点（自根部 MCP 至指尖 TIP），用于弯曲判据
        private val THUMB = intArrayOf(1, 2, 3, 4)
        private val INDEX = intArrayOf(5, 6, 7, 8)
        private val MIDDLE = intArrayOf(9, 10, 11, 12)
        private val RING = intArrayOf(13, 14, 15, 16)
        private val PINKY = intArrayOf(17, 18, 19, 20)
        // 拇指-食指「接触」判定的指尖距离阈值（归一化坐标系，单位无量纲）
        private const val CONTACT_THRESHOLD = 0.06f
        // 仅用于展示提示文案的距离阈值（换算成近似毫米描述）
        private const val CONTACT_THRESHOLD_MM = 60
    }

    /**
     * 对单个手势执行纠错评估
     *
     * @param target 标准动作目标（由 SignCourseCatalog 词条提供）
     * @param landmarks 用户 21 关键点
     * @return 纠错结果（评分 + 逐条建议），关键点缺失时返回 null
     */
    fun correct(target: GestureTarget, landmarks: List<HandLandmark>): CorrectionResult? {
        // 关键点不足以形成完整手型时无法评估，交由上层提示「未检测到手」
        if (landmarks.size < 21) return null

        val checks = mutableListOf<CorrectionCheck>()

        // 逐手指比对弯曲期望（仅检查被约束到 BENT/STRAIGHT 的手指）
        checkFinger(target.thumb, THUMB, landmarks, "拇指", checks)
        checkFinger(target.index, INDEX, landmarks, "食指", checks)
        checkFinger(target.middle, MIDDLE, landmarks, "中指", checks)
        checkFinger(target.ring, RING, landmarks, "无名指", checks)
        checkFinger(target.pinky, PINKY, landmarks, "小指", checks)

        // 拇指-食指接触期望（OK / 比心等手势）
        target.thumbIndexContact?.let { expectedContact ->
            val actualContact = distance(landmarks[THUMB_TIP], landmarks[INDEX_TIP]) < CONTACT_THRESHOLD
            val passed = actualContact == expectedContact
            checks.add(
                CorrectionCheck(
                    label = "拇指与食指接触",
                    passed = passed,
                    detail = if (expectedContact) {
                        "拇指指尖与食指指尖应相接成环（间距 < ${CONTACT_THRESHOLD_MM}mm）"
                    } else {
                        "拇指与食指应分开，不要贴合"
                    }
                )
            )
        }

        // 综合评分：通过项 / 总约束项
        val score = if (checks.isEmpty()) {
            0
        } else {
            ((checks.count { it.passed } / checks.size.toFloat()) * 100f).toInt()
        }

        // 生成纠正建议（仅收集未通过项）
        val suggestions = checks.filterNot { it.passed }.map { it.detail }

        return CorrectionResult(
            scene = "",
            score = score,
            checks = checks,
            suggestions = suggestions,
            timestamp = System.currentTimeMillis()
        )
    }

    /** 评估单根手指的弯曲期望并写入检查列表 */
    private fun checkFinger(
        expect: FingerExpectation,
        indices: IntArray,
        landmarks: List<HandLandmark>,
        fingerName: String,
        dest: MutableList<CorrectionCheck>
    ) {
        // ANY = 该手指不受约束，跳过
        if (expect == FingerExpectation.ANY) return

        val actualBent = isFingerBent(landmarks, indices)
        val passed = (actualBent == (expect == FingerExpectation.BENT))

        val action = when (expect) {
            FingerExpectation.BENT -> "应弯曲收拢"
            FingerExpectation.STRAIGHT -> "应伸直展开"
            FingerExpectation.ANY -> "" // 不可达，前面已提前 return
        }

        dest.add(
            CorrectionCheck(
                label = fingerName,
                passed = passed,
                detail = if (passed) "$fingerName 状态正确" else "$fingerName 弯曲状态不对，$action"
            )
        )
    }

    /** 判断单根手指是否弯曲（与 GestureClassifier 判据一致） */
    private fun isFingerBent(landmarks: List<HandLandmark>, indices: IntArray): Boolean {
        val base = landmarks[indices[0]]   // MCP 根部
        val tip = landmarks[indices.last()] // TIP 指尖
        val wrist = landmarks[WRIST]

        // 指尖到手腕距离 < 根部到手腕距离 × 0.85 ⇔ 手指弯曲
        return distance(tip, wrist) < distance(base, wrist) * 0.85f
    }

    /** 两点欧氏距离 */
    private fun distance(a: HandLandmark, b: HandLandmark): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }
}

/** 单条纠错检查结果 */
data class CorrectionCheck(
    val label: String,   // 检查项名称（手指/接触）
    val passed: Boolean, // 是否通过
    val detail: String   // 通过时的确认或未通过时的纠正建议
)

/**
 * 手势纠错综合结果
 *
 * @param scene 预留：目标手势场景（当前为空，仅供追溯）
 * @param score 0~100 正确度评分
 * @param checks 各检查项明细
 * @param suggestions 面向用户的纠正建议列表（未通过项拼接）
 */
data class CorrectionResult(
    val scene: String,
    val score: Int,
    val checks: List<CorrectionCheck>,
    val suggestions: List<String>,
    val timestamp: Long
)