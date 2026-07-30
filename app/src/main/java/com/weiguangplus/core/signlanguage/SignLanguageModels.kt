package com.weiguangplus.core.signlanguage

/** 手部关键点 */
data class HandLandmark(
    val index: Int,
    val x: Float,
    val y: Float,
    val z: Float = 0f
)

/** 手部检测结果 */
data class HandDetectionResult(
    val hands: List<List<HandLandmark>>,
    val imageWidth: Int,
    val imageHeight: Int,
    val detectedAt: Long = System.currentTimeMillis()
) {
    val hasHands: Boolean get() = hands.isNotEmpty()
    val handCount: Int get() = hands.size
}

/** 手势类型 */
enum class GestureType { SOS, DAILY, GREETING, QUESTION, EMOTION }

/** 手势分类结果 */
data class ClassifiedGesture(
    val name: String,
    val confidence: Float,
    val type: GestureType = GestureType.DAILY
)

/** 手语识别完整结果 */
data class SignLanguageResult(
    val gestureName: String,
    val confidence: Float,
    val gestureType: GestureType,
    val textTranslation: String,      // 对应文字
    val handLandmarks: List<List<HandLandmark>>,
    val detectedAt: Long = System.currentTimeMillis()
)
