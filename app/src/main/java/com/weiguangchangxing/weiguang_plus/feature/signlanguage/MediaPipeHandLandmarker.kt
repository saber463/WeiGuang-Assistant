package com.weiguangchangxing.weiguang_plus.feature.signlanguage

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

interface DownloadProgressListener {
    fun onProgress(bytesDownloaded: Long, totalBytes: Long)
    fun onComplete()
    fun onError(message: String)
}

class MediaPipeHandLandmarker(private val context: Context) {

    companion object {
        private const val MODEL_FILE = "hand_landmarker.task"
        private const val MODEL_URL = "https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/1/hand_landmarker.task"
        private const val MIN_HAND_DETECTION_CONFIDENCE = 0.5f
        private const val MIN_TRACKING_CONFIDENCE = 0.5f
        private const val RETRY_MAX = 2
        private const val PREF_NAME = "hand_model_download"
        private const val PREF_DOWNLOADED = "model_downloaded"
        private const val PREF_MODEL_VERSION = "model_version"
    }

    private var handLandmarker: HandLandmarker? = null
    private var modelDownloaded = false
    private var modelDownloading = false
    private val downloadExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var onModelReadyListener: (() -> Unit)? = null
    private var onModelErrorListener: ((String) -> Unit)? = null
    private var downloadProgressListener: DownloadProgressListener? = null
    private var retryCount = 0
    private var totalBytes = -1L

    fun initialize(
        onReady: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
            onError?.invoke("手语识别需要 Android 7.0 (API 24) 及以上版本，当前设备不支持")
            return
        }

        onModelReadyListener = onReady
        onModelErrorListener = onError

        val modelFile = getModelFile()
        if (!modelFile.exists() && isModelDownloadedBefore()) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().clear().apply()
        }
        if (modelFile.exists()) {
            modelDownloaded = true
            loadModel(modelFile)
            onReady?.invoke()
        } else {
            if (isNetworkAvailable()) {
                downloadModel(modelFile)
            } else {
                onError?.invoke("当前无网络连接，请连接网络后重试，或确认已下载模型文件")
            }
        }
    }

    private fun getModelFile(): File {
        return File(context.filesDir, MODEL_FILE)
    }

    private fun isNetworkAvailable(): Boolean {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return false
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
                return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else {
                @Suppress("DEPRECATION")
                val activeNetwork = connectivityManager.activeNetworkInfo ?: return false
                @Suppress("DEPRECATION")
                return activeNetwork.isConnectedOrConnecting
            }
        } catch (e: Exception) {
            return false
        }
    }

    private fun downloadModel(modelFile: File) {
        if (modelDownloading) return
        modelDownloading = true

        downloadExecutor.execute {
            try {
                val url = URL(MODEL_URL)
                val connection = url.openConnection()
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                totalBytes = connection.contentLengthLong
                val inputStream = connection.getInputStream()
                val outputStream = FileOutputStream(modelFile)
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalRead = 0L
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    if (totalBytes > 0) {
                        downloadProgressListener?.onProgress(totalRead, totalBytes)
                    }
                }
                inputStream.close()
                outputStream.close()

                modelDownloaded = true
                modelDownloading = false
                retryCount = 0
                saveDownloadState()

                loadModel(modelFile)
                downloadProgressListener?.onComplete()
                onModelReadyListener?.invoke()
            } catch (e: Exception) {
                modelDownloading = false
                modelFile.delete()
                retryCount++
                if (retryCount <= RETRY_MAX) {
                    Thread.sleep(1000L * retryCount)
                    downloadModel(modelFile)
                } else {
                    val finalRetryCount = retryCount
                    retryCount = 0
                    val errorMsg = "手语模型下载失败(${finalRetryCount}次): ${e.message}"
                    onModelErrorListener?.invoke(errorMsg)
                    downloadProgressListener?.onError(errorMsg)
                }
            }
        }
    }

    fun setDownloadProgressListener(listener: DownloadProgressListener?) {
        downloadProgressListener = listener
    }

    private fun saveDownloadState() {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(PREF_DOWNLOADED, true)
            .putString(PREF_MODEL_VERSION, "1.0")
            .apply()
    }

    private fun isModelDownloadedBefore(): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(PREF_DOWNLOADED, false)
    }

    private fun loadModel(modelFile: File) {
        try {
            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(
                    com.google.mediapipe.tasks.core.BaseOptions.builder()
                        .setModelAssetPath(modelFile.absolutePath)
                        .build()
                )
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumHands(2)
                .setMinHandDetectionConfidence(MIN_HAND_DETECTION_CONFIDENCE)
                .setMinTrackingConfidence(MIN_TRACKING_CONFIDENCE)
                .setResultListener { result: HandLandmarkerResult, _: com.google.mediapipe.framework.image.MPImage? ->
                    onLandmarkResult(result)
                }
                .setErrorListener { error: RuntimeException ->
                    onModelErrorListener?.invoke("手语模型错误: ${error.message}")
                }
                .build()

            handLandmarker = HandLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            onModelErrorListener?.invoke("手语模型加载失败: ${e.message}")
        }
    }

    private var resultCallback: ((List<HandLandmarkData>) -> Unit)? = null

    fun setResultCallback(callback: (List<HandLandmarkData>) -> Unit) {
        resultCallback = callback
    }

    private fun onLandmarkResult(result: HandLandmarkerResult) {
        val handsData = mutableListOf<HandLandmarkData>()

        val landmarkLists = result.landmarks()

        for (i in landmarkLists.indices) {
            val landmarks = landmarkLists[i]

            val landmarkList = mutableListOf<HandLandmarkPoint>()
            for (j in landmarks.indices) {
                val lm = landmarks[j]
                landmarkList.add(
                    HandLandmarkPoint(
                        x = lm.x(),
                        y = lm.y(),
                        z = lm.z(),
                        index = j
                    )
                )
            }

            handsData.add(
                HandLandmarkData(
                    landmarks = landmarkList,
                    handedness = "Unknown",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        resultCallback?.invoke(handsData)
    }

    fun detectFrame(mpImage: com.google.mediapipe.framework.image.MPImage) {
        val landmarker = handLandmarker ?: return
        try {
            landmarker.detectAsync(mpImage, System.currentTimeMillis())
        } catch (_: Exception) {
        }
    }

    fun detectBitmap(bitmap: Bitmap): HandLandmarkerResult? {
        val landmarker = handLandmarker ?: return null
        return try {
            val mpImage = com.google.mediapipe.framework.image.BitmapImageBuilder(bitmap).build()
            landmarker.detect(mpImage)
        } catch (_: Exception) {
            null
        }
    }

    fun isReady(): Boolean {
        return handLandmarker != null && modelDownloaded && getModelFile().exists()
    }

    fun isDownloading(): Boolean {
        return modelDownloading
    }

    fun release() {
        handLandmarker?.close()
        handLandmarker = null
        downloadExecutor.shutdown()
        resultCallback = null
        onModelReadyListener = null
        onModelErrorListener = null
    }
}

data class HandLandmarkPoint(
    val x: Float,
    val y: Float,
    val z: Float,
    val index: Int
)

data class HandLandmarkData(
    val landmarks: List<HandLandmarkPoint>,
    val handedness: String,
    val timestamp: Long
) {
    companion object {
        const val WRIST = 0
        const val THUMB_CMC = 1
        const val THUMB_MCP = 2
        const val THUMB_IP = 3
        const val THUMB_TIP = 4
        const val INDEX_FINGER_MCP = 5
        const val INDEX_FINGER_PIP = 6
        const val INDEX_FINGER_DIP = 7
        const val INDEX_FINGER_TIP = 8
        const val MIDDLE_FINGER_MCP = 9
        const val MIDDLE_FINGER_PIP = 10
        const val MIDDLE_FINGER_DIP = 11
        const val MIDDLE_FINGER_TIP = 12
        const val RING_FINGER_MCP = 13
        const val RING_FINGER_PIP = 14
        const val RING_FINGER_DIP = 15
        const val RING_FINGER_TIP = 16
        const val PINKY_MCP = 17
        const val PINKY_PIP = 18
        const val PINKY_DIP = 19
        const val PINKY_TIP = 20
    }
}