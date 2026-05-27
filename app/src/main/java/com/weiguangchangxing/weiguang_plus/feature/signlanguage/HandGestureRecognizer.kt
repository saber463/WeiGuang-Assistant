package com.weiguangchangxing.weiguang_plus.feature.signlanguage

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageProxy
import com.weiguangchangxing.weiguang_plus.core.DeviceCapabilityChecker
import com.weiguangchangxing.weiguang_plus.core.DeviceCapabilityChecker.HandTrackingLevel
import com.google.mediapipe.framework.image.BitmapImageBuilder

class HandGestureRecognizer(private val context: Context) {

    companion object {
        private const val TAG = "HandGestureRecognizer"
        private const val MIN_RECOGNITION_INTERVAL_MS = 300L
    }

    private var mediaPipeHandLandmarker: MediaPipeHandLandmarker? = null
    private var fallbackDetector: FallbackGestureDetector? = null
    private val featureExtractor = HandGestureFeatureExtractor()
    private val classifier = HandGestureClassifier()
    private val handTrackingLevel = DeviceCapabilityChecker.handTrackingLevel

    private var lastRecognitionTime = 0L
    private var isInitialized = false
    private var lastRecognizedGesture: String = ""

    private var onGestureRecognizedListener: ((String, Float, HandLandmarkData) -> Unit)? = null
    private var onHandDetectedListener: ((List<HandLandmarkData>) -> Unit)? = null
    private var onModelStatusListener: ((Boolean, String?) -> Unit)? = null
    private var lastHandData: HandLandmarkData? = null

    fun initialize(
        onReady: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        when (handTrackingLevel) {
            HandTrackingLevel.ADVANCED -> {
                Log.d(TAG, "安卓版本 ${DeviceCapabilityChecker.androidVersionName}，启用高级手语识别(MediaPipe)")
                initializeMediaPipe(onReady, onError)
            }
            HandTrackingLevel.FALLBACK -> {
                Log.d(TAG, "安卓版本 ${DeviceCapabilityChecker.androidVersionName}，启用备用手势检测")
                initializeFallback(onReady)
            }
            HandTrackingLevel.NONE -> {
                val msg = "当前安卓版本 ${DeviceCapabilityChecker.androidVersionName} 不支持手势识别"
                Log.w(TAG, msg)
                onModelStatusListener?.invoke(false, msg)
                onError(msg)
            }
        }
    }

    private fun initializeMediaPipe(onReady: () -> Unit, onError: (String) -> Unit) {
        mediaPipeHandLandmarker = MediaPipeHandLandmarker(context)
        mediaPipeHandLandmarker?.initialize(
            onReady = {
                isInitialized = true
                Log.d(TAG, "MediaPipe手语模型就绪")
                onModelStatusListener?.invoke(true, null)
                onReady()
            },
            onError = { errorMsg ->
                Log.e(TAG, "MediaPipe手语模型错误: $errorMsg")
                onModelStatusListener?.invoke(false, errorMsg)
                onError(errorMsg)
            }
        )

        mediaPipeHandLandmarker?.setResultCallback { handsDataList ->
            onHandDetectedListener?.invoke(handsDataList)

            if (handsDataList.isNotEmpty()) {
                val primaryHand = handsDataList.first()
                lastHandData = primaryHand

                val now = System.currentTimeMillis()
                if (now - lastRecognitionTime >= MIN_RECOGNITION_INTERVAL_MS) {
                    lastRecognitionTime = now

                    val result = classifier.classify(primaryHand)
                    if (result.confidence >= HandGestureClassifier.MIN_GESTURE_CONFIDENCE) {
                        if (result.gestureName != lastRecognizedGesture) {
                            lastRecognizedGesture = result.gestureName
                            Log.d(TAG, "识别到手势: ${result.gestureName} (置信度: ${result.confidence})")
                        }
                        onGestureRecognizedListener?.invoke(
                            result.gestureName,
                            result.confidence,
                            primaryHand
                        )
                    }
                }
            }
        }
    }

    private fun initializeFallback(onReady: () -> Unit) {
        fallbackDetector = FallbackGestureDetector()
        fallbackDetector?.setOnGestureDetectedListener { gesture, confidence ->
            isInitialized = true

            val classifierResult = fallbackDetector!!.mapFallbackGestureToClassifierResult(gesture, confidence)

            if (classifierResult.confidence >= HandGestureClassifier.MIN_GESTURE_CONFIDENCE) {
                if (classifierResult.gestureName != lastRecognizedGesture) {
                    lastRecognizedGesture = classifierResult.gestureName
                    Log.d(TAG, "备用检测手势: ${classifierResult.gestureName} (置信度: ${classifierResult.confidence})")
                }

                val handData = HandLandmarkData(
                    landmarks = emptyList(),
                    handedness = "Unknown",
                    timestamp = System.currentTimeMillis()
                )
                lastHandData = handData

                onGestureRecognizedListener?.invoke(
                    classifierResult.gestureName,
                    classifierResult.confidence,
                    handData
                )
            }
        }
        onModelStatusListener?.invoke(true, null)
        onReady()
    }

    fun analyzeImageProxy(imageProxy: ImageProxy) {
        when (handTrackingLevel) {
            HandTrackingLevel.ADVANCED -> {
                if (isInitialized && mediaPipeHandLandmarker != null) {
                    try {
                        val bitmap = imageProxyToBitmap(imageProxy) ?: return
                        val mpImage = BitmapImageBuilder(bitmap).build()
                        mediaPipeHandLandmarker?.detectFrame(mpImage)
                    } catch (e: Exception) {
                        Log.e(TAG, "MediaPipe图像分析失败: ${e.message}")
                    }
                }
            }
            HandTrackingLevel.FALLBACK -> {
                fallbackDetector?.analyzeFrame(imageProxy)
            }
            HandTrackingLevel.NONE -> { }
        }
    }

    fun analyzeBitmap(bitmap: Bitmap): HandGestureClassifier.ClassificationResult? {
        if (handTrackingLevel == HandTrackingLevel.ADVANCED && isInitialized) {
            val result = mediaPipeHandLandmarker?.detectBitmap(bitmap) ?: return null
            val handsData = mutableListOf<HandLandmarkData>()

            val landmarkLists = result.landmarks()
            for (i in landmarkLists.indices) {
                val landmarks = landmarkLists[i]
                val landmarkList = landmarks.mapIndexed { index, lm ->
                    HandLandmarkPoint(x = lm.x(), y = lm.y(), z = lm.z(), index = index)
                }
                handsData.add(HandLandmarkData(landmarks = landmarkList, handedness = "Unknown", timestamp = System.currentTimeMillis()))
            }

            if (handsData.isNotEmpty()) {
                return classifier.classify(handsData.first())
            }
        }
        return null
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            val buffer = imageProxy.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            val bitmap = Bitmap.createBitmap(
                imageProxy.width, imageProxy.height,
                Bitmap.Config.ARGB_8888
            )
            val pixels = IntArray(imageProxy.width * imageProxy.height)

            val uPlane = imageProxy.planes[1]
            val vPlane = imageProxy.planes[2]
            val uBuffer = uPlane.buffer
            val vBuffer = vPlane.buffer
            val uBytes = ByteArray(uBuffer.remaining())
            val vBytes = ByteArray(vBuffer.remaining())
            uBuffer.get(uBytes)
            vBuffer.get(vBytes)

            var yIndex = 0
            for (y in 0 until imageProxy.height) {
                for (x in 0 until imageProxy.width) {
                    val yVal = bytes[yIndex++].toInt() and 0xFF
                    val uIndex = (y / 2) * (imageProxy.width / 2) + (x / 2)
                    val uVal = if (uIndex < uBytes.size) (uBytes[uIndex].toInt() and 0xFF) - 128 else 0
                    val vVal = if (uIndex < vBytes.size) (vBytes[uIndex].toInt() and 0xFF) - 128 else 0

                    var r = (yVal + (1.370705 * vVal)).toInt()
                    var g = (yVal - (0.698001 * vVal) - (0.337633 * uVal)).toInt()
                    var b = (yVal + (1.732446 * uVal)).toInt()

                    r = r.coerceIn(0, 255)
                    g = g.coerceIn(0, 255)
                    b = b.coerceIn(0, 255)

                    pixels[y * imageProxy.width + x] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                }
            }
            bitmap.setPixels(pixels, 0, imageProxy.width, 0, 0, imageProxy.width, imageProxy.height)
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "ImageProxy转Bitmap失败: ${e.message}")
            null
        }
    }

    fun setOnGestureRecognizedListener(listener: (String, Float, HandLandmarkData) -> Unit) {
        onGestureRecognizedListener = listener
    }

    fun setOnHandDetectedListener(listener: (List<HandLandmarkData>) -> Unit) {
        onHandDetectedListener = listener
    }

    fun setOnModelStatusListener(listener: (Boolean, String?) -> Unit) {
        onModelStatusListener = listener
    }

    fun getLastRecognizedGesture(): String = lastRecognizedGesture

    fun getClassifier(): HandGestureClassifier = classifier

    fun getFeatureExtractor(): HandGestureFeatureExtractor = featureExtractor

    fun getLastHandData(): HandLandmarkData? = lastHandData

    fun getHandTrackingLevel(): HandTrackingLevel = handTrackingLevel

    fun isReady(): Boolean {
        return when (handTrackingLevel) {
            HandTrackingLevel.ADVANCED -> isInitialized && (mediaPipeHandLandmarker?.isReady() == true)
            HandTrackingLevel.FALLBACK -> isInitialized
            HandTrackingLevel.NONE -> false
        }
    }

    fun isDownloading(): Boolean = mediaPipeHandLandmarker?.isDownloading() == true

    fun release() {
        mediaPipeHandLandmarker?.release()
        mediaPipeHandLandmarker = null
        fallbackDetector = null
        isInitialized = false
        onGestureRecognizedListener = null
        onHandDetectedListener = null
        onModelStatusListener = null
    }
}