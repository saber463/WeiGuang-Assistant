package com.weiguangchangxing.weiguang_plus.feature.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.weiguangchangxing.weiguang_plus.core.perception.FusionPerceptionEngine
import com.weiguangchangxing.weiguang_plus.core.perception.PerceptionEvent
import com.weiguangchangxing.weiguang_plus.core.perception.PerceptionEventType
import com.weiguangchangxing.weiguang_plus.core.perception.PerceptionPriority
import java.util.concurrent.Executors

data class RecognizedObject(
    val label: String,
    val confidence: Float,
    val timestamp: Long
)

data class RecognitionState(
    val isRunning: Boolean = false,
    val recentObjects: List<RecognizedObject> = emptyList(),
    val currentTopObject: RecognizedObject? = null,
    val errorMessage: String? = null
)

class CameraObjectRecognizer(private val context: Context) {

    private val _state = MutableStateFlow(RecognitionState())
    val state: StateFlow<RecognitionState> = _state.asStateFlow()

    private var imageAnalysis: ImageAnalysis? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var isRunning = false
    private var previewView: PreviewView? = null

    private val labeler by lazy {
        ImageLabeling.getClient(
            ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.5f)
                .build()
        )
    }

    private val analysisExecutor = Executors.newSingleThreadExecutor()

    private val objectHistory = ArrayDeque<RecognizedObject>(5)

    private var lifecycleOwner: LifecycleOwner? = null

    fun setPreviewView(view: PreviewView) {
        previewView = view
    }

    fun startRecognition(owner: LifecycleOwner) {
        if (isRunning) return

        lifecycleOwner = owner
        isRunning = true
        _state.value = _state.value.copy(isRunning = true)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(owner)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "启动相机识别失败: ${e.message}"
                )
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases(owner: LifecycleOwner) {
        val cameraProvider = cameraProvider ?: return

        @Suppress("DEPRECATION")
        val preview = Preview.Builder()
            .setTargetResolution(android.util.Size(640, 480))
            .build()
            .also {
                previewView?.let { view -> it.setSurfaceProvider(view.surfaceProvider) }
            }

        @Suppress("DEPRECATION")
        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetResolution(android.util.Size(640, 480))
            .build()
            .also { analysis ->
                analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                    processImage(imageProxy)
                }
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(owner, cameraSelector, preview, imageAnalysis)
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                errorMessage = "绑定相机失败: ${e.message}"
            )
        }
    }

    private fun processImage(imageProxy: ImageProxy) {
        if (!isRunning) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        labeler.process(inputImage)
            .addOnSuccessListener { labels ->
                handleLabels(labels, System.currentTimeMillis())
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun handleLabels(labels: List<ImageLabel>, timestamp: Long) {
        if (labels.isEmpty()) return

        val topLabel = labels.maxByOrNull { it.confidence } ?: return

        val recognized = RecognizedObject(
            label = topLabel.text,
            confidence = topLabel.confidence,
            timestamp = timestamp
        )

        val label = topLabel.text
        val confidence = topLabel.confidence
        if (confidence > 0.6f) {
            FusionPerceptionEngine.emitEvent(
                PerceptionEvent(
                    type = PerceptionEventType.OBJECT_DETECTED,
                    priority = if (label == "person" || label == "dog" || label == "cat") PerceptionPriority.HIGH
                               else PerceptionPriority.LOW,
                    sourceModule = "CameraObjectRecognizer",
                    description = "识别到${label}",
                    confidence = confidence,
                    extraData = mapOf("label" to label)
                )
            )
        }

        objectHistory.add(recognized)
        if (objectHistory.size > 5) {
            objectHistory.removeFirst()
        }

        _state.value = _state.value.copy(
            currentTopObject = recognized,
            recentObjects = objectHistory.toList()
        )
    }

    fun stopRecognition() {
        isRunning = false
        try {
            imageAnalysis?.clearAnalyzer()
            cameraProvider?.unbindAll()
        } catch (_: Exception) {
        }
    }

    fun release() {
        stopRecognition()
        analysisExecutor.shutdown()
        labeler.close()
        objectHistory.clear()
        _state.value = RecognitionState()
    }

    @Suppress("UNUSED_PARAMETER")
    fun setDetectionInterval(intervalMs: Long) {
    }

    fun getLastNObjets(n: Int): List<RecognizedObject> {
        return objectHistory.toList().takeLast(n)
    }
}