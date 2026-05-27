package com.weiguangchangxing.weiguang_plus.feature.signlanguage

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageProxy
import kotlin.math.abs
import kotlin.math.sqrt

class FallbackGestureDetector {

    companion object {
        private const val TAG = "FallbackGestureDetector"
        private const val MIN_DETECTION_INTERVAL_MS = 500L
        private const val SKIN_COLOR_MIN = 0
        private const val SKIN_COLOR_MAX = 50

        enum class FallbackGesture(val gestureName: String) {
            OPEN_PALM("打开手掌"),
            FIST("握拳"),
            WAVE("挥手"),
            POINTING("食指指"),
            THUMBS_UP("竖起大拇指"),
            V_SIGN("V字"),
            UNKNOWN("未知手势")
        }
    }

    private var lastDetectionTime = 0L
    private var lastGesture = FallbackGesture.UNKNOWN
    private var consecutiveSameGesture = 0
    private var lastPalmArea = 0f
    private var lastHandCenter = Pair(0f, 0f)

    private var onGestureListener: ((FallbackGesture, Float) -> Unit)? = null

    fun setOnGestureDetectedListener(listener: (FallbackGesture, Float) -> Unit) {
        onGestureListener = listener
    }

    fun analyzeFrame(imageProxy: ImageProxy) {
        val now = System.currentTimeMillis()
        if (now - lastDetectionTime < MIN_DETECTION_INTERVAL_MS) return
        lastDetectionTime = now

        try {
            val bitmap = imageProxyToGrayscale(imageProxy) ?: return
            val skinPixels = detectSkinRegion(bitmap)

            if (skinPixels.size < 50) {
                if (lastGesture != FallbackGesture.UNKNOWN) {
                    lastGesture = FallbackGesture.UNKNOWN
                    consecutiveSameGesture = 0
                }
                return
            }

            val centerX = skinPixels.map { it.first }.average().toFloat()
            val centerY = skinPixels.map { it.second }.average().toFloat()
            val palmArea = skinPixels.size.toFloat()

            val gesture = classifyGesture(
                centerX, centerY, palmArea,
                bitmap.width.toFloat(), bitmap.height.toFloat()
            )

            if (gesture == lastGesture) {
                consecutiveSameGesture++
            } else {
                consecutiveSameGesture = 1
            }

            lastGesture = gesture
            lastPalmArea = palmArea
            lastHandCenter = Pair(centerX, centerY)

            if (consecutiveSameGesture >= 2) {
                val confidence = (consecutiveSameGesture.toFloat() / 5f).coerceAtMost(0.85f)
                onGestureListener?.invoke(gesture, confidence)
                Log.d(TAG, "备用检测: ${gesture.gestureName} 置信度=$confidence")
            }
        } catch (e: Exception) {
            Log.e(TAG, "备用检测帧分析失败: ${e.message}")
        }
    }

    private fun detectSkinRegion(bitmap: Bitmap): List<Pair<Int, Int>> {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val skinPixels = mutableListOf<Pair<Int, Int>>()

        for (y in 0 until height step 2) {
            for (x in 0 until width step 2) {
                val pixel = pixels[y * width + x]
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                if (isSkinColor(r, g, b)) {
                    skinPixels.add(Pair(x, y))
                }
            }
        }

        return skinPixels
    }

    private fun isSkinColor(r: Int, g: Int, b: Int): Boolean {
        if (r < 40 || g < 30 || b < 20) return false
        if (r > 250 && g > 240 && b > 230) return false

        val y = 0.299 * r + 0.587 * g + 0.114 * b
        val cr = r - y
        val cb = b - y

        return cr in 2.0..18.0 && cb in 2.0..18.0
    }

    private fun classifyGesture(
        centerX: Float, centerY: Float,
        palmArea: Float, frameWidth: Float, frameHeight: Float
    ): FallbackGesture {
        val areaRatio = palmArea / (frameWidth * frameHeight)

        val dx = centerX - lastHandCenter.first
        val dy = centerY - lastHandCenter.second
        val movement = sqrt(dx * dx + dy * dy).toDouble()

        val areaChange = abs(palmArea - lastPalmArea) / (lastPalmArea + 1f)

        if (movement > 30 && areaRatio > 0.05f) {
            if (abs(dx) > abs(dy) && abs(dx) > 15) {
                return FallbackGesture.WAVE
            }
            if (areaChange > 0.4f) {
                return if (palmArea > lastPalmArea) FallbackGesture.OPEN_PALM
                else FallbackGesture.FIST
            }
        }

        if (areaRatio > 0.12f) {
            return FallbackGesture.OPEN_PALM
        } else if (areaRatio > 0.04f && areaRatio < 0.08f) {
            return FallbackGesture.FIST
        }

        if (areaRatio > 0.05f && movement < 10) {
            val centerXRatio = centerX / frameWidth
            val centerYRatio = centerY / frameHeight

            if (centerYRatio < 0.35f && centerXRatio in 0.4f..0.6f) {
                return FallbackGesture.V_SIGN
            }
        }

        return FallbackGesture.UNKNOWN
    }

    fun mapFallbackGestureToClassifierResult(
        fallbackGesture: FallbackGesture,
        confidence: Float
    ): HandGestureClassifier.ClassificationResult {
        val matchedTemplate = when (fallbackGesture) {
            FallbackGesture.OPEN_PALM -> HandGestureClassifier.GestureTemplate(
                name = "打开手掌",
                extendedFingers = listOf(true, true, true, true, true)
            )
            FallbackGesture.FIST -> HandGestureClassifier.GestureTemplate(
                name = "握拳",
                extendedFingers = listOf(false, false, false, false, false)
            )
            FallbackGesture.WAVE -> HandGestureClassifier.GestureTemplate(
                name = "挥手",
                extendedFingers = listOf(true, true, true, true, true)
            )
            FallbackGesture.POINTING -> HandGestureClassifier.GestureTemplate(
                name = "食指指",
                extendedFingers = listOf(false, true, false, false, false)
            )
            FallbackGesture.THUMBS_UP -> HandGestureClassifier.GestureTemplate(
                name = "竖起大拇指",
                extendedFingers = listOf(true, false, false, false, false)
            )
            FallbackGesture.V_SIGN -> HandGestureClassifier.GestureTemplate(
                name = "V字",
                extendedFingers = listOf(false, true, true, false, false)
            )
            FallbackGesture.UNKNOWN -> HandGestureClassifier.GestureTemplate(
                name = "未知手势",
                extendedFingers = listOf(false, false, false, false, false)
            )
        }

        return HandGestureClassifier.ClassificationResult(
            gestureName = matchedTemplate.name,
            confidence = confidence,
            matchedTemplate = matchedTemplate
        )
    }

    private fun imageProxyToGrayscale(imageProxy: ImageProxy): Bitmap? {
        return try {
            val buffer = imageProxy.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            val bitmap = Bitmap.createBitmap(
                imageProxy.width, imageProxy.height,
                Bitmap.Config.ARGB_8888
            )
            val pixels = IntArray(imageProxy.width * imageProxy.height)

            for (y in 0 until imageProxy.height) {
                for (x in 0 until imageProxy.width) {
                    val yIndex = y * imageProxy.width + x
                    val yVal = bytes[yIndex].toInt() and 0xFF
                    pixels[yIndex] = (0xFF shl 24) or (yVal shl 16) or (yVal shl 8) or yVal
                }
            }
            bitmap.setPixels(pixels, 0, imageProxy.width, 0, 0, imageProxy.width, imageProxy.height)
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "ImageProxy转Bitmap失败: ${e.message}")
            null
        }
    }
}