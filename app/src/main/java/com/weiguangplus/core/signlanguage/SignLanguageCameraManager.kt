package com.weiguangplus.core.signlanguage

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.weiguangplus.core.tts.TtsController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * 手语识别相机管理器（升级版 v2.0）
 *
 * 集成 CameraX → MediaPipe → TFLite 识别 → 向量验证 → TTS 播报 的完整流水线。
 * 使用 SignLanguageEngine（单例）作为核心识别引擎。
 */
class SignLanguageCameraManager(private val context: Context) {

    companion object {
        private const val TAG = "SignLanguageCamera"
        /** TTS 播报去抖间隔：同一手势至少间隔 3 秒才重复播报 */
        private const val TTS_DEBOUNCE_MS = 3000L
        /** 帧处理间隔：100ms = 10fps */
        private const val FRAME_INTERVAL_MS = 100L
    }

    private val landmarker = MediaPipeHandLandmarker(context)
    private val executor = Executors.newSingleThreadExecutor()

    private val _isCameraReady = MutableStateFlow(false)
    val isCameraReady: StateFlow<Boolean> = _isCameraReady

    private val _recognitionResult = MutableStateFlow<SignLanguageResult?>(null)
    val recognitionResult: StateFlow<SignLanguageResult?> = _recognitionResult

    private val _topKCandidates = MutableStateFlow<List<SignLanguageResult>>(emptyList())
    val topKCandidates: StateFlow<List<SignLanguageResult>> = _topKCandidates

    // TTS 相关
    private var lastTtsText: String? = null
    private var lastTtsTime: Long = 0L
    private var ttsEnabled: Boolean = true

    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var isRecognizing = false
    private var lastFrameTime: Long = 0L

    init {
        landmarker.initialize()
        // 确保引擎已初始化
        SignLanguageEngine.initialize(context)
    }

    /** 启用/禁用 TTS 自动播报 */
    fun setTtsEnabled(enabled: Boolean) {
        ttsEnabled = enabled
    }

    /** 设置 TTS 初始化 */
    fun initTts() {
        TtsController.initialize(context)
    }

    /** 绑定到生命周期 + 预览视图 */
    fun attach(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val provider = future.get()
            cameraProvider = provider
            preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA, // 前置摄像头（自拍角度更自然）
                preview!!
            )
            _isCameraReady.value = true
            Log.d(TAG, "Camera attached & ready")
        }, ContextCompat.getMainExecutor(context))
    }

    /** 开始识别（绑定图像分析器） */
    fun startRecognition() {
        if (isRecognizing) return
        isRecognizing = true

        val provider = cameraProvider ?: return

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(executor) { proxy -> processFrame(proxy) }
            }

        try {
            provider.unbindAll()
            provider.bindToLifecycle(
                context as LifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview!!, imageAnalysis
            )
            Log.d(TAG, "Recognition started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recognition", e)
            isRecognizing = false
        }
    }

    /** 停止识别 */
    fun stopRecognition() {
        isRecognizing = false
        cameraProvider?.unbindAll()
        // 重新绑定仅预览
        val provider = cameraProvider
        if (provider != null) {
            provider.unbindAll()
            provider.bindToLifecycle(
                context as LifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview!!
            )
        }
        Log.d(TAG, "Recognition stopped")
    }

    /** 释放所有资源 */
    fun release() {
        stopRecognition()
        executor.shutdown()
        try { executor.awaitTermination(500, TimeUnit.MILLISECONDS) } catch (_: Exception) {}
        landmarker.release()
        Log.d(TAG, "Camera manager released")
    }

    // ─── 帧处理 ───

    private fun processFrame(imageProxy: ImageProxy) {
        if (!isRecognizing) {
            imageProxy.close()
            return
        }

        // 帧率控制
        val now = System.currentTimeMillis()
        if (now - lastFrameTime < FRAME_INTERVAL_MS) {
            imageProxy.close()
            return
        }
        lastFrameTime = now

        try {
            val rotation = imageProxy.imageInfo.rotationDegrees
            val hands = landmarker.detect(imageProxy.image!!, rotation)

            if (hands.isNotEmpty()) {
                val detection = HandDetectionResult(
                    hands = hands,
                    imageWidth = imageProxy.width,
                    imageHeight = imageProxy.height
                )

                // 送入引擎 → 识别流水线（缓冲 → TFLite → 向量验证 → 去抖）
                val result = SignLanguageEngine.processHandDetection(detection)

                if (result != null) {
                    _recognitionResult.value = result
                    _topKCandidates.value = SignLanguageEngine.topKCandidates.value

                    // TTS 自动播报
                    onRecognitionResult(result)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Frame processing error", e)
        } finally {
            imageProxy.close()
        }
    }

    private fun onRecognitionResult(result: SignLanguageResult) {
        if (!ttsEnabled) return

        val text = result.textTranslation
        val now = System.currentTimeMillis()

        // 去抖：3 秒内相同文本不重复播报
        if (text == lastTtsText && (now - lastTtsTime) < TTS_DEBOUNCE_MS) {
            return
        }

        // SOS 手势强制播报（无论去抖）
        val isSos = result.gestureType == GestureType.SOS
        if (isSos || (now - lastTtsTime) >= TTS_DEBOUNCE_MS) {
            if (result.confidence > 0.6f) {
                if (isSos) {
                    TtsController.speakWarning(text)
                } else {
                    TtsController.speak(text)
                }
                lastTtsText = text
                lastTtsTime = now
            }
        }
    }
}
