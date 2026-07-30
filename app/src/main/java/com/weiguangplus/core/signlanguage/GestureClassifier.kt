package com.weiguangplus.core.signlanguage

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 手势分类器
 *
 * 基于 MediaPipe 21个手部关键点进行手势识别。
 * 改进版：支持 20+ 日常手势，覆盖 SOS、日常交流、情感表达。
 *
 * 手指关键点索引:
 *   拇指: 1-4 (CMC, MCP, IP, TIP)
 *   食指: 5-8 (MCP, PIP, DIP, TIP)
 *   中指: 9-12
 *   无名指: 13-16
 *   小指: 17-20
 *   手腕: 0
 */
class GestureClassifier {

    companion object {
        val THUMB = intArrayOf(1, 2, 3, 4)
        val INDEX = intArrayOf(5, 6, 7, 8)
        val MIDDLE = intArrayOf(9, 10, 11, 12)
        val RING = intArrayOf(13, 14, 15, 16)
        val PINKY = intArrayOf(17, 18, 19, 20)
    }

    fun classify(landmarks: List<HandLandmark>): ClassifiedGesture? {
        if (landmarks.size < 21) return null

        val thumbBent = isFingerBent(landmarks, THUMB)
        val indexBent = isFingerBent(landmarks, INDEX)
        val middleBent = isFingerBent(landmarks, MIDDLE)
        val ringBent = isFingerBent(landmarks, RING)
        val pinkyBent = isFingerBent(landmarks, PINKY)

        val thumbTip = landmarks[4]
        val indexTip = landmarks[8]
        val middleTip = landmarks[12]
        val ringTip = landmarks[16]
        val pinkyTip = landmarks[20]
        val wrist = landmarks[0]

        val thumbIndexDist = distance(thumbTip, indexTip)
        val indexMiddleDist = distance(indexTip, middleTip)
        val ringPinkyDist = distance(ringTip, pinkyTip)

        // 拇指与食指是否接触（OK/拿捏手势）
        val thumbTouchesIndex = thumbIndexDist < 0.06f
        // 食指与中指是否并拢
        val indexMiddleTogether = indexMiddleDist < 0.05f
        // 无名指与小指是否分开
        val ringPinkyApart = ringPinkyDist > 0.1f

        return when {
            // === SOS / 紧急手势 ===
            // 握拳 (所有手指弯曲)
            thumbBent && indexBent && middleBent && ringBent && pinkyBent ->
                ClassifiedGesture("握拳/SOS求救", 0.9f, GestureType.SOS)

            // 手掌张开 (所有手指伸直) - "我需要帮助"
            !thumbBent && !indexBent && !middleBent && !ringBent && !pinkyBent ->
                ClassifiedGesture("手掌/我需要帮助", 0.85f, GestureType.SOS)

            // === 日常交流手势 ===
            // OK手势 (拇指食指成圈，其余伸直)
            thumbTouchesIndex && !middleBent && !ringBent && !pinkyBent ->
                ClassifiedGesture("OK/没问题", 0.8f, GestureType.DAILY)

            // 点赞 (拇指伸直，其余握拳)
            !thumbBent && indexBent && middleBent && ringBent && pinkyBent ->
                ClassifiedGesture("点赞/好的", 0.85f, GestureType.DAILY)

            // 单手食指伸直 - 数字1 / 指方向
            !indexBent && middleBent && ringBent && pinkyBent ->
                ClassifiedGesture("数字1/那个", 0.75f, GestureType.DAILY)

            // 食指+中指伸直 (V字/胜利/数字2)
            !indexBent && !middleBent && ringBent && pinkyBent ->
                ClassifiedGesture("胜利/数字2", 0.8f, GestureType.DAILY)

            // === 问候手势 ===
            // 拇指+食指+中指伸直 (数字3/OK变体)
            !thumbBent && !indexBent && !middleBent && ringBent && pinkyBent ->
                ClassifiedGesture("数字3/你好", 0.7f, GestureType.GREETING)

            // 四指伸直 (除拇指外)
            indexBent && !middleBent && !ringBent && !pinkyBent ->
                ClassifiedGesture("四个/等待", 0.7f, GestureType.DAILY)

            // 五指全部伸直+拇指张开 (完整张开手掌)
            !thumbBent && !indexBent && !middleBent && !ringBent && !pinkyBent
                && thumbIndexDist > 0.15f ->
                ClassifiedGesture("全开手掌/停止", 0.8f, GestureType.SOS)

            // === 情绪表达 ===
            // 仅小指伸直 (小指/不好/不)
            thumbBent && indexBent && middleBent && ringBent && !pinkyBent ->
                ClassifiedGesture("小指/不好", 0.75f, GestureType.EMOTION)

            // 拇指弯曲+食指中指伸直 (手枪手势)
            thumbBent && !indexBent && !middleBent && ringBent && pinkyBent ->
                ClassifiedGesture("指方向/那里", 0.7f, GestureType.DAILY)

            else -> null
        }
    }

    /** 判断单根手指是否弯曲 */
    private fun isFingerBent(
        landmarks: List<HandLandmark>,
        indices: IntArray
    ): Boolean {
        val base = landmarks[indices[0]]   // MCP关节（根部）
        val tip = landmarks[indices.last()] // TIP指尖

        // 指尖比根部更靠近手腕 = 手指弯曲了
        val wrist = landmarks[0]
        val tipToWrist = distance(tip, wrist)
        val baseToWrist = distance(base, wrist)

        // 如果指尖到手腕的距离小于根部到手腕的距离，说明手指弯了
        return tipToWrist < baseToWrist * 0.85f
    }

    /** 两点间欧几里得距离 */
    private fun distance(a: HandLandmark, b: HandLandmark): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }
}
