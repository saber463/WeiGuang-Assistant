package com.weiguangchangxing.weiguang_plus.feature.obstacle

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.weiguangchangxing.weiguang_plus.core.perception.FusionPerceptionEngine
import com.weiguangchangxing.weiguang_plus.core.perception.PerceptionEvent
import com.weiguangchangxing.weiguang_plus.core.perception.PerceptionEventType
import com.weiguangchangxing.weiguang_plus.core.perception.PerceptionPriority
import java.util.concurrent.Executors
import kotlin.math.sqrt

/**
 * 障碍物检测结果数据类
 *
 * 功能：
 * - 封装单个被检测物体的完整信息
 * - 包含在画面中的方位（左右中）、估算距离、是否为行人、物体标签及置信度
 *
 * @property direction 物体相对于用户的方位字符串，取值："正前方"、"左前方"、"右前方"、"左侧"、"右侧"、"未知"
 * @property distance 估算距离，单位：米。基于检测框面积占画面比例的经验公式推算
 * @property isHuman 是否为行人。通过 ML Kit 返回的标签是否包含 "person" 判定
 * @property label 物体标签，例如 "person"、"car"、"bottle" 等。若 ML Kit 未返回标签则取 "unknown"
 * @property confidence ML Kit 对该检测结果的置信度，取值范围 0.0 ~ 1.0
 */
data class ObstacleData(
    val direction: String,
    val distance: Float,
    val isHuman: Boolean,
    val label: String,
    val confidence: Float
)

/**
 * 障碍物检测结果回调接口
 *
 * 使用 fun interface（Kotlin 函数式接口），支持以 Lambda 形式传入监听器。
 *
 * 回调时机：
 * - 每帧 CameraX ImageAnalysis 经 ML Kit 检测完成后
 * - 若当前帧检测到至少一个物体且置信度 >= [ObstacleDetectionManager.MIN_CONFIDENCE]
 *
 * @param obstacles 当前帧中所有满足置信度阈值的障碍物列表。列表为空时不回调
 */
fun interface OnObstacleDetectedListener {
    fun onObstacleDetected(obstacles: List<ObstacleData>)
}

/**
 * 障碍物检测管理器
 *
 * 核心职责：
 * 1. 通过 CameraX ImageAnalysis 持续获取相机帧
 * 2. 使用 ML Kit Object Detection（默认流式检测 + 多物体追踪）识别画面中的物体
 * 3. 利用 SensorManager（加速度计 + 陀螺仪）辅助判断设备姿态，稳定检测结果
 * 4. 根据检测框在画面中的位置推算方位和估算距离
 * 5. 通过 [OnObstacleDetectedListener] 回调返回 [ObstacleData] 列表
 *
 * 使用方式：
 * ```kotlin
 * val manager = ObstacleDetectionManager(context, lifecycleOwner)
 * manager.setDetectedListener { obstacles ->
 *     obstacles.forEach { obstacle ->
 *         Log.d("Obstacle", "${obstacle.direction} ${obstacle.distance}m ${obstacle.label}")
 *     }
 * }
 * manager.startDetection()
 * // ...
 * manager.stopDetection()
 * ```
 *
 * 生命周期：
 * - 调用 [startDetection] 绑定 CameraX 并注册传感器监听
 * - 调用 [stopDetection] 解除绑定并注销传感器监听
 * - 内部使用单线程 Executor 处理 ML Kit 检测，避免阻塞主线程
 */
class ObstacleDetectionManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {

    // ==================== 内部状态 ====================

    private val _state = MutableStateFlow(ObstacleDetectionState())
    val state: StateFlow<ObstacleDetectionState> = _state.asStateFlow()

    private var detectedListener: OnObstacleDetectedListener? = null

    // CameraX 相关
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null

    // ML Kit 相关
    private val mlKitExecutor = Executors.newSingleThreadExecutor()

    /**
     * ML Kit Object Detector 实例
     *
     * 配置策略：
     * - STREAM_MODE：持续输入帧，适合实时检测
     * - 启用多物体追踪：为每个检测到的物体分配 trackingId，跨帧关联
     * - 启用分类：返回物体标签（如 person、car 等）
     * - 不启用二维码 / 条形码检测以提升性能
     */
    private val objectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
    )

    // 传感器相关
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    /**
     * 传感器事件监听器
     *
     * 加速度计：用于判断设备倾斜（pitch / roll），辅助判断物体在垂直方向的位置
     * 陀螺仪：用于检测设备旋转角速度，若旋转过快则跳过检测帧以减少抖动误报
     */
    private val sensorEventListener = object : SensorEventListener {
        private val smoothFactor = 0.2f
        private var smoothedPitch = 0f
        private var smoothedRoll = 0f
        private var currentGyroMagnitude = 0f

        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    // 从加速度计推算设备姿态角（低通滤波平滑）
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val rawPitch = Math.atan2(
                        (-x).toDouble(),
                        sqrt(y * y + z * z).toDouble()
                    ).toFloat()

                    val rawRoll = Math.atan2(
                        y.toDouble(),
                        z.toDouble()
                    ).toFloat()

                    smoothedPitch = smoothFactor * rawPitch + (1 - smoothFactor) * smoothedPitch
                    smoothedRoll = smoothFactor * rawRoll + (1 - smoothFactor) * smoothedRoll

                    _state.value = _state.value.copy(
                        devicePitch = smoothedPitch,
                        deviceRoll = smoothedRoll
                    )
                }

                Sensor.TYPE_GYROSCOPE -> {
                    // 计算陀螺仪角速度幅值，用于判断设备是否在运动中
                    val rx = event.values[0]
                    val ry = event.values[1]
                    val rz = event.values[2]
                    currentGyroMagnitude = sqrt(rx * rx + ry * ry + rz * rz)

                    _state.value = _state.value.copy(
                        gyroMagnitude = currentGyroMagnitude
                    )
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // 传感器精度变化通常无需处理，留空
        }
    }

    // ==================== 公共 API ====================

    /**
     * 设置检测结果回调
     *
     * @param listener 回调接口，每次检测到物体时调用
     */
    fun setDetectedListener(listener: OnObstacleDetectedListener) {
        this.detectedListener = listener
    }

    /**
     * 移除检测结果回调
     */
    fun removeDetectedListener() {
        this.detectedListener = null
    }

    /**
     * 启动障碍物检测
     *
     * 流程：
     * 1. 注册传感器监听（加速度计 + 陀螺仪）
     * 2. 通过 ProcessCameraProvider 绑定 CameraX ImageAnalysis 到生命周期
     * 3. ImageAnalysis 每帧通过 [processImageProxy] 送入 ML Kit 检测
     *
     * 安全：若 CameraX 绑定失败，_state.errorMessage 会记录错误原因
     */
    fun startDetection() {
        registerSensors()
        bindCameraX()
    }

    /**
     * 停止障碍物检测
     *
     * 流程：
     * 1. 关闭 ML Kit 检测器
     * 2. 注销传感器监听
     * 3. 解绑 CameraX（通过将分析器置空实现）
     * 4. 重置内部状态
     */
    fun stopDetection() {
        objectDetector.close()
        unregisterSensors()
        unbindCameraX()
        _state.value = ObstacleDetectionState()
    }

    /**
     * 释放所有资源
     *
     * 调用后当前实例不可再使用，需重新创建实例。
     */
    fun release() {
        stopDetection()
        mlKitExecutor.shutdown()
    }

    // ==================== 传感器管理 ====================

    /**
     * 注册传感器监听
     *
     * 加速度计采用 SENSOR_DELAY_NORMAL 即可满足姿态估算需求
     * 陀螺仪同样采用 NORMAL 延迟，仅用于判断设备是否大幅旋转
     */
    private fun registerSensors() {
        accelerometer?.let {
            sensorManager.registerListener(
                sensorEventListener, it, SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        gyroscope?.let {
            sensorManager.registerListener(
                sensorEventListener, it, SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    /**
     * 注销传感器监听
     */
    private fun unregisterSensors() {
        sensorManager.unregisterListener(sensorEventListener)
    }

    // ==================== CameraX 绑定 ====================

    /**
     * 绑定 CameraX 到当前 LifecycleOwner
     *
     * 使用后置摄像头（CameraSelector.DEFAULT_BACK_CAMERA），
     * ImageAnalysis 分辨率设为 640x480 以平衡检测精度与性能。
     * 图片格式默认为 YUV_420_888，ML Kit 可直接消费。
     */
    @Suppress("DEPRECATION")
    private fun bindCameraX() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(640, 480))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis?.setAnalyzer(mlKitExecutor) { imageProxy ->
                    processImageProxy(imageProxy)
                }

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    imageAnalysis
                )

                _state.value = _state.value.copy(
                    isDetecting = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isDetecting = false,
                    errorMessage = "CameraX 绑定失败: ${e.message}"
                )
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * 解绑 CameraX
     */
    private fun unbindCameraX() {
        try {
            cameraProvider?.unbindAll()
            imageAnalysis?.clearAnalyzer()
            imageAnalysis = null
            cameraProvider = null
            _state.value = _state.value.copy(isDetecting = false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==================== ML Kit 图像分析 ====================

    /**
     * 处理 CameraX 传来的每一帧
     *
     * 流程：
     * 1. 若陀螺仪检测到设备快速旋转（角速度 > 3 rad/s），跳过该帧以减少误报
     * 2. 从 ImageProxy 提取 InputImage
     * 3. 送入 ML Kit Object Detector 异步检测
     * 4. 检测结果通过 [onMlKitResult] 回调处理
     * 5. 无论如何都关闭 ImageProxy 以释放 CameraX 缓冲区
     */
    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        // 若设备正在快速旋转，跳过该帧检测
        if (_state.value.gyroMagnitude > GYRO_MOTION_THRESHOLD) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        objectDetector.process(inputImage)
            .addOnSuccessListener { detectedObjects ->
                onMlKitResult(detectedObjects, imageProxy)
            }
            .addOnFailureListener { exception ->
                _state.value = _state.value.copy(
                    errorMessage = "ML Kit 检测失败: ${exception.message}"
                )
                imageProxy.close()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    /**
     * 处理 ML Kit 检测结果
     *
     * 逻辑：
     * 1. 遍历所有检测到的物体
     * 2. 仅保留置信度 >= [MIN_CONFIDENCE] 的结果
     * 3. 根据检测框在画面中的位置推算方位
     * 4. 根据检测框面积估算距离
     * 5. 通过标签判断是否为行人
     * 6. 将有效结果通过回调返回
     */
    private fun onMlKitResult(
        detectedObjects: List<DetectedObject>,
        imageProxy: ImageProxy
    ) {
        val imageWidth = imageProxy.width
        val imageHeight = imageProxy.height
        val imageArea = (imageWidth * imageHeight).toFloat()

        val obstacles = detectedObjects
            .mapNotNull { obj ->
                val bestLabel = obj.labels.maxByOrNull { it.confidence }
                if (bestLabel == null || bestLabel.confidence < MIN_CONFIDENCE) return@mapNotNull null
                val boundingBox = obj.boundingBox

                // 计算检测框中心点相对于画面的水平位置比例（0=最左，1=最右）
                val centerX = boundingBox.centerX().toFloat() / imageWidth

                // 计算检测框面积占比
                val boxArea = (boundingBox.width() * boundingBox.height()).toFloat()
                val areaRatio = boxArea / imageArea

                // 推算方向
                val direction = calculateDirection(centerX, areaRatio)

                // 估算距离（米）
                val distance = estimateDistance(areaRatio)

                // 判断是否为行人
                val isHuman = obj.labels.any { it.text.lowercase() == "person" }

                val label = bestLabel.text
                val confidence = bestLabel.confidence

                val obstacleData = ObstacleData(
                    direction = direction,
                    distance = distance,
                    isHuman = isHuman,
                    label = label,
                    confidence = confidence
                )

                FusionPerceptionEngine.emitEvent(
                    PerceptionEvent(
                        type = when (direction) {
                            "左侧", "左前方" -> PerceptionEventType.OBSTACLE_LEFT
                            "右侧", "右前方" -> PerceptionEventType.OBSTACLE_RIGHT
                            else -> PerceptionEventType.OBSTACLE_AHEAD
                        },
                        priority = if (distance < 2f) PerceptionPriority.EMERGENCY
                                   else if (distance < 5f) PerceptionPriority.HIGH
                                   else PerceptionPriority.MEDIUM,
                        sourceModule = "ObstacleDetection",
                        description = when (direction) {
                            "左侧", "左前方" -> "左侧${"%.0f".format(distance)}米有障碍物"
                            "右侧", "右前方" -> "右侧${"%.0f".format(distance)}米有障碍物"
                            else -> "前方${"%.0f".format(distance)}米有障碍物"
                        },
                        direction = direction,
                        distance = distance,
                        confidence = confidence,
                        extraData = mapOf("isPerson" to isHuman, "label" to label)
                    )
                )

                obstacleData
            }

        if (obstacles.isNotEmpty()) {
            _state.value = _state.value.copy(
                lastDetectedCount = obstacles.size,
                lastDetectionTimeMs = System.currentTimeMillis()
            )

            val totalDetections = _state.value.totalDetections + obstacles.size
            _state.value = _state.value.copy(totalDetections = totalDetections)

            detectedListener?.onObstacleDetected(obstacles)
        }

        imageProxy.close()
    }

    // ==================== 方位与距离推算 ====================

    /**
     * 根据物体在画面中的水平位置和面积占比推算方位
     *
     * 方位判定逻辑（基于后置摄像头，画面即用户正前方视野）：
     * - centerX 为 0~1，表示物体中心在画面中的水平位置
     *   - 0.33~0.66 → 正前方
     *   - < 0.33 → 左前方
     *   - > 0.66 → 右前方
     * - 若物体面积较大（> FRONT_LARGE_THRESHOLD），说明很近，归为"正前方"
     *
     * 陀螺仪辅助：
     * - 若设备正在旋转（gyroMagnitude > 0.5），降低方向判断的置信度
     * - 当前实现中，仅在大幅度旋转时跳过检测帧（见 processImageProxy），
     *   此处不再额外处理
     *
     * @param centerX 物体中心水平位置比例 [0, 1]
     * @param areaRatio 检测框面积占画面比例
     * @return 中文方向描述字符串
     */
    private fun calculateDirection(centerX: Float, areaRatio: Float): String {
        return when {
            areaRatio > FRONT_LARGE_THRESHOLD -> DIRECTION_FRONT
            centerX < LEFT_RIGHT_BOUNDARY -> DIRECTION_FRONT_LEFT
            centerX > RIGHT_LEFT_BOUNDARY -> DIRECTION_FRONT_RIGHT
            else -> DIRECTION_FRONT
        }
    }

    /**
     * 根据检测框面积占比估算物体距离
     *
     * 估算模型：
     * - 基于"物体在画面中的面积与距离平方成反比"的物理关系
     * - 采用经验公式：distance = calibrationFactor / sqrt(areaRatio)
     * - calibrationFactor 通过对典型物体（行人）在 1m 和 3m 处标定得到
     * - 当前 calibrationFactor 设 0.25，对应：
     *   - areaRatio = 0.0625（约 1/16 画面）→ 1.0m
     *   - areaRatio = 0.0069（约 1/144 画面）→ 3.0m
     *   - areaRatio = 0.0016（约 1/625 画面）→ 6.25m
     *
     * 限制：
     * - 距离估算仅作为参考，实际精度受物体实际大小、镜头参数等因素影响
     * - 建议结合 TTS 播报时使用 "约 X 米" 等模糊表述
     *
     * @param areaRatio 检测框面积占画面比例
     * @return 估算距离（米），最小 0.3m，最大 10.0m
     */
    private fun estimateDistance(areaRatio: Float): Float {
        if (areaRatio <= 0f) return MAX_DISTANCE
        val rawDistance = DISTANCE_CALIBRATION_FACTOR / sqrt(areaRatio)
        return rawDistance.coerceIn(MIN_DISTANCE, MAX_DISTANCE)
    }

    // ==================== 内部状态类 ====================

    /**
     * 障碍物检测管理器内部状态
     *
     * 通过 StateFlow 暴露，外部可观察检测器的工作状态
     *
     * @property isDetecting 是否正在检测
     * @property devicePitch 设备俯仰角（弧度），来自加速度计
     * @property deviceRoll 设备横滚角（弧度），来自加速度计
     * @property gyroMagnitude 陀螺仪角速度幅值（rad/s），用于判断设备是否在运动
     * @property lastDetectedCount 最近一次检测到的物体数量
     * @property lastDetectionTimeMs 最近一次检测到物体的时间戳
     * @property totalDetections 累计检测到的物体总数
     * @property errorMessage 错误信息，无错误时为 null
     */
    data class ObstacleDetectionState(
        val isDetecting: Boolean = false,
        val devicePitch: Float = 0f,
        val deviceRoll: Float = 0f,
        val gyroMagnitude: Float = 0f,
        val lastDetectedCount: Int = 0,
        val lastDetectionTimeMs: Long = 0L,
        val totalDetections: Int = 0,
        val errorMessage: String? = null
    )

    // ==================== 常量 ====================

    companion object {
        /**
         * ML Kit 最低置信度阈值
         *
         * trackingConfidence 取值范围 0~100，此处设 50 为最低要求。
         * 低于此值的结果会被过滤掉，减少误报。
         */
        private const val MIN_CONFIDENCE = 0.5f

        /**
         * 陀螺仪运动阈值（rad/s）
         *
         * 当设备旋转角速度超过此值时，跳过当前帧的检测，
         * 避免设备快速转动时产生大量误报。
         */
        private const val GYRO_MOTION_THRESHOLD = 3.0f

        /**
         * 方向判定左右边界（水平位置比例）
         *
         * - 0.33 左侧边界：物体中心 < 0.33 视为左前方
         * - 0.66 右侧边界：物体中心 > 0.66 视为右前方
         * - 中间区域视为正前方
         */
        private const val LEFT_RIGHT_BOUNDARY = 0.33f
        private const val RIGHT_LEFT_BOUNDARY = 0.66f

        /**
         * 前方大物体阈值（面积占比）
         *
         * 当检测框面积占画面比例超过此值时，无论水平位置如何，
         * 都视为"正前方"——因为物体已经非常近了。
         */
        private const val FRONT_LARGE_THRESHOLD = 0.15f

        /**
         * 距离估算标定因子
         *
         * 基于 distance = calibrationFactor / sqrt(areaRatio) 公式。
         * 标定过程：
         * - 将手机正对一个 1.7m 高的行人
         * - 在 1m 距离处测得 areaRatio ≈ 0.0625
         * - 代入公式 1.0 = F / sqrt(0.0625) → F = 0.25
         */
        private const val DISTANCE_CALIBRATION_FACTOR = 0.25f

        /**
         * 距离估算的最值限制（米）
         *
         * - MIN：避免面积过大时距离趋近于 0
         * - MAX：避免面积过小时距离无限远
         */
        private const val MIN_DISTANCE = 0.3f
        private const val MAX_DISTANCE = 10.0f

        // 方向常量字符串
        private const val DIRECTION_FRONT = "正前方"
        private const val DIRECTION_FRONT_LEFT = "左前方"
        private const val DIRECTION_FRONT_RIGHT = "右前方"
        private const val DIRECTION_LEFT = "左侧"
        private const val DIRECTION_RIGHT = "右侧"
    }
}