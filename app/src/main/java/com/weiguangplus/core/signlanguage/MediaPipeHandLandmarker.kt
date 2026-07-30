package com.weiguangplus.core.signlanguage

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import com.google.mediapipe.framework.image.MediaImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import java.util.concurrent.atomic.AtomicBoolean

/**
 * MediaPipe 手部关键点检测器封装
 *
 * 加载 hand_landmarker.task 模型，从 CameraX 帧中提取 21 个手部关键点。
 */
class MediaPipeHandLandmarker(private val context: Context) {

    private var handLandmarker: HandLandmarker? = null
    private val initialized = AtomicBoolean(false)
    private var lastDetectionMs: Long = 0L

    companion object {
        private const val MODEL_PATH = "hand_landmarker.task"
        private const val MAX_HANDS = 2
        private const val MIN_CONFIDENCE = 0.5f
        private const val FRAME_INTERVAL_MS = 100L
    }

    fun initialize() {
        if (initialized.get()) return
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_PATH)
                .build()
            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setNumHands(MAX_HANDS)
                .setMinHandDetectionConfidence(MIN_CONFIDENCE)
                .setMinTrackingConfidence(MIN_CONFIDENCE)
                .build()
            handLandmarker = HandLandmarker.createFromOptions(context, options)
            initialized.set(true)
        } catch (e: Exception) {
            initialized.set(false)
        }
    }

    fun detect(image: Image, rotation: Int): List<List<HandLandmark>> {
        if (!initialized.get()) return emptyList()
        val now = System.currentTimeMillis()
        if (now - lastDetectionMs < FRAME_INTERVAL_MS) return emptyList()
        lastDetectionMs = now

        return try {
            val mpImage = MediaImageBuilder(image).build()
            val result: HandLandmarkerResult = handLandmarker!!.detect(mpImage)
            result.landmarks().map { handLandmarks ->
                handLandmarks.map { lm ->
                    HandLandmark(index = 0, x = lm.x(), y = lm.y(), z = lm.z())
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun release() {
        handLandmarker?.close()
        handLandmarker = null
        initialized.set(false)
    }
}
