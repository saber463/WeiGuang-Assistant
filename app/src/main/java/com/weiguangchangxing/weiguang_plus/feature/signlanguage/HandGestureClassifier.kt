package com.weiguangchangxing.weiguang_plus.feature.signlanguage

import kotlin.math.abs
import kotlin.math.sqrt

class HandGestureClassifier {

    data class GestureTemplate(
        val name: String,
        val extendedFingers: List<Boolean>,
        val thumbAngleRange: ClosedFloatingPointRange<Float> = (-180f..180f),
        val indexAngleRange: ClosedFloatingPointRange<Float> = (-180f..180f),
        val middleAngleRange: ClosedFloatingPointRange<Float> = (-180f..180f),
        val ringAngleRange: ClosedFloatingPointRange<Float> = (-180f..180f),
        val pinkyAngleRange: ClosedFloatingPointRange<Float> = (-180f..180f),
        val handOrientation: String = "任意"
    )

    data class ClassificationResult(
        val gestureName: String,
        val confidence: Float,
        val matchedTemplate: GestureTemplate?
    )

    companion object {
        const val MIN_GESTURE_CONFIDENCE = 0.6f

        val FINGER_NAMES = listOf("拇指", "食指", "中指", "无名指", "小指")

        val DEFAULT_GESTURE_TEMPLATES = listOf(
            GestureTemplate(
                name = "握拳",
                extendedFingers = listOf(false, false, false, false, false)
            ),
            GestureTemplate(
                name = "打开手掌",
                extendedFingers = listOf(true, true, true, true, true),
                handOrientation = "竖直向上"
            ),
            GestureTemplate(
                name = "竖起大拇指",
                extendedFingers = listOf(true, false, false, false, false),
                thumbAngleRange = (-90f..90f)
            ),
            GestureTemplate(
                name = "食指指",
                extendedFingers = listOf(false, true, false, false, false)
            ),
            GestureTemplate(
                name = "OK手势",
                extendedFingers = listOf(false, false, false, false, true)
            ),
            GestureTemplate(
                name = "比心",
                extendedFingers = listOf(false, false, false, false, true),
                thumbAngleRange = (45f..135f)
            ),
            GestureTemplate(
                name = "V字",
                extendedFingers = listOf(false, true, true, false, false)
            ),
            GestureTemplate(
                name = "三",
                extendedFingers = listOf(false, true, true, true, false)
            ),
            GestureTemplate(
                name = "四",
                extendedFingers = listOf(false, true, true, true, true)
            ),
            GestureTemplate(
                name = "六",
                extendedFingers = listOf(true, false, false, false, true)
            ),
            GestureTemplate(
                name = "八",
                extendedFingers = listOf(true, false, false, false, true),
                thumbAngleRange = (45f..135f)
            ),
            GestureTemplate(
                name = "数字1",
                extendedFingers = listOf(true, false, false, false, false),
                handOrientation = "竖直向上"
            ),
            GestureTemplate(
                name = "数字2",
                extendedFingers = listOf(false, true, true, false, false),
                handOrientation = "竖直向上"
            ),
            GestureTemplate(
                name = "数字3",
                extendedFingers = listOf(false, true, true, true, false),
                handOrientation = "竖直向上"
            ),
            GestureTemplate(
                name = "数字4",
                extendedFingers = listOf(false, true, true, true, true),
                handOrientation = "竖直向上"
            ),
            GestureTemplate(
                name = "数字5",
                extendedFingers = listOf(true, true, true, true, true),
                handOrientation = "竖直向上"
            ),
            GestureTemplate(
                name = "数字6",
                extendedFingers = listOf(false, false, false, false, true),
                handOrientation = "竖直向上"
            ),
            GestureTemplate(
                name = "数字7",
                extendedFingers = listOf(false, false, false, true, true),
                handOrientation = "竖直向上"
            ),
            GestureTemplate(
                name = "数字8",
                extendedFingers = listOf(true, false, false, false, true),
                handOrientation = "竖直向上"
            ),
            GestureTemplate(
                name = "数字9",
                extendedFingers = listOf(false, true, true, true, true),
                thumbAngleRange = (-135f..-45f)
            ),
            GestureTemplate(
                name = "握拳（拳头）",
                extendedFingers = listOf(false, false, false, false, false),
                handOrientation = "竖直向上"
            )
        )
    }

    private val featureExtractor = HandGestureFeatureExtractor()
    private var gestureTemplates = DEFAULT_GESTURE_TEMPLATES.toMutableList()

    fun setCustomTemplates(templates: List<GestureTemplate>) {
        gestureTemplates.clear()
        gestureTemplates.addAll(templates)
    }

    fun addCustomTemplate(template: GestureTemplate) {
        gestureTemplates.add(template)
    }

    fun classify(handData: HandLandmarkData): ClassificationResult {
        val extendedFingers = featureExtractor.calculateExtendedFingers(handData)
        val features = featureExtractor.extractFeatures(handData)
        val handOrientation = if (features != null) {
            featureExtractor.calculateHandOrientation(handData)
        } else {
            "未知"
        }

        var bestMatch: GestureTemplate? = null
        var bestScore = 0f

        for (template in gestureTemplates) {
            val score = matchGesture(
                extendedFingers = extendedFingers,
                features = features?.fingerAngles,
                handOrientation = handOrientation,
                template = template
            )
            if (score > bestScore) {
                bestScore = score
                bestMatch = template
            }
        }

        return ClassificationResult(
            gestureName = bestMatch?.name ?: "未知手势",
            confidence = bestScore,
            matchedTemplate = bestMatch
        )
    }

    private fun matchGesture(
        extendedFingers: List<Boolean>,
        features: FloatArray?,
        handOrientation: String,
        template: GestureTemplate
    ): Float {
        var score = 0f

        for (i in 0 until 5) {
            if (extendedFingers[i] == template.extendedFingers[i]) {
                score += 15f
            } else {
                score -= 5f
            }
        }

        if (features != null) {
            val angleRanges = listOf(
                template.thumbAngleRange,
                template.indexAngleRange,
                template.middleAngleRange,
                template.ringAngleRange,
                template.pinkyAngleRange
            )
            for (i in 0 until 5) {
                val angle = Math.toDegrees(features[i].toDouble()).toFloat()
                if (angle in angleRanges[i]) {
                    score += 5f
                }
            }
        }

        if (template.handOrientation != "任意") {
            if (handOrientation == template.handOrientation) {
                score += 10f
            } else {
                score -= 3f
            }
        }

        val maxScore = 5 * 15 + 5 * 5 + 10
        return (score / maxScore).coerceIn(0f, 1f)
    }

    fun getExtendedFingersDescription(extendedFingers: List<Boolean>): String {
        val extended = mutableListOf<String>()
        for (i in extendedFingers.indices) {
            if (extendedFingers[i]) {
                extended.add(FINGER_NAMES[i])
            }
        }
        return if (extended.isEmpty()) "握拳" else extended.joinToString("、")
    }
}