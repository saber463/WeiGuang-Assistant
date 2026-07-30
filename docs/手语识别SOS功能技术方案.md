# 微光同行 — 手语识别 SOS 功能技术方案

> 版本：v1.0
> 日期：2026-06-01
> 状态：方案评审中

---

## 一、需求概述

### 1.1 老板需求
> 手语使用者的动作通常较快，当前需要解决的是摄像头快速准确识别手语动作的问题。

### 1.2 核心场景

| 场景 | 用户行为 | 系统响应 |
|------|----------|----------|
| **SOS紧急触发** | 对准前置摄像头比出"救命"手势 | 自动发送GPS定位+短信给紧急联系人 |
| **实时沟通翻译** | 开启摄像头用手语说话 | 手语实时转为文字显示在屏幕上 |

### 1.3 设计目标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 识别延迟 | < 500ms | 从手势完成到结果显示 |
| 帧率 | ≥ 30fps | 摄像头画面流畅 |
| 识别准确率 | ≥ 85% | SOS手势要求 ≥ 95% |
| SOS触发延迟 | < 2s | 从手势完成到SOS发出 |
| 词汇集 | 30-50个 | 首期覆盖SOS+日常沟通 |
| 离线可用 | ✅ | 100%离线，无需联网 |

---

## 二、技术架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                      用户界面层 (UI Layer)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  手语识别界面  │  │ SOS触发确认  │  │ 实时文字显示面板  │  │
│  └──────┬───────┘  └──────┬───────┘  └────────┬─────────┘  │
│         │                 │                    │             │
├─────────┼─────────────────┼────────────────────┼─────────────┤
│         │           业务逻辑层 (Business Layer)  │             │
│  ┌──────┴───────┐  ┌──────┴───────┐  ┌────────┴─────────┐  │
│  │ SOS触发引擎  │  │ 翻译缓冲管理 │  │ 家属消息推送     │  │
│  └──────┬───────┘  └──────┬───────┘  └────────┬─────────┘  │
│         │                 │                    │             │
├─────────┼─────────────────┼────────────────────┼─────────────┤
│         │          识别引擎层 (Recognition Layer) │             │
│  ┌──────┴──────────────────┴────────────────────┴─────────┐ │
│  │                   手语识别管线                           │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────┐ │ │
│  │  │  Step 1   │  │  Step 2   │  │  Step 3   │  │ Step 4 │ │ │
│  │  │ 手部检测  │→│ 关键点提取 │→│ 序列建模  │→│ 词汇   │ │ │
│  │  │ MediaPipe │  │ 21关键点  │  │ GRU网络   │  │ 分类   │ │ │
│  │  └──────────┘  └──────────┘  └──────────┘  └────────┘ │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
├──────────────────────────────────────────────────────────────┤
│                    传感器层 (Sensor Layer)                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ CameraX  │  │  GPS定位  │  │   TTS    │  │ 振动马达  │   │
│  │ 前置摄像头│  │ 经纬度坐标│  │ 语音合成  │  │ 触觉反馈  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└──────────────────────────────────────────────────────────────┘
```

### 2.2 识别管线详解

```
摄像头帧 (640x480 YUV420)
    │
    ▼
┌─────────────────────────────────────────────┐
│  MediaPipe Hands (Step 1: 手部检测)          │
│  - 输入: YUV420 图像帧                      │
│  - 输出: 21个手部关键点坐标 (x, y, z)        │
│  - 帧率: 30fps (每33ms处理一帧)              │
│  - 检测置信度阈值: 0.7                       │
│  - 最大检测手数: 2 (双手)                    │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│  关键点预处理 (Step 2: 归一化)               │
│  - 中心化: 以手腕根部(点0)为原点             │
│  - 缩放: 按手掌对角线长度归一化到[0,1]       │
│  - 旋转补偿: 消除手腕旋转影响                │
│  - 输出: 63维特征向量 (21点 × 3坐标)         │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│  滑动窗口缓冲 (Step 3: 时序组装)             │
│  - 窗口大小: 30帧 (约1秒的手势动作)          │
│  - 滑动步长: 5帧 (每5帧做一次推理)           │
│  - 输出: [30, 63] 的时序矩阵                 │
│  - 缓冲区大小: 30帧 × 2手 × 63维 = 3780 floats│
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│  GRU时序分类器 (Step 4: 手势分类)            │
│  - 输入: [30, 63] 时序矩阵                   │
│  - 网络: GRU(128) → GRU(64) → FC(50)        │
│  - 输出: 50个手势类别的概率分布               │
│  - 推理延迟: ~15ms (TFLite, CPU)             │
│  - 模型大小: ~200KB (量化后)                  │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│  后处理 (Step 5: 决策逻辑)                   │
│  - 置信度阈值: 普通手势0.75, SOS手势0.65     │
│  - 连续确认: 同一手势连续3帧确认后才触发      │
│  - 去抖动: 200ms内不重复触发同一手势          │
│  - SOS加急: 检测到SOS手势立即触发，不等确认   │
└─────────────────────────────────────────────┘
```

---

## 三、词汇集设计

### 3.1 SOS紧急手势（优先级最高）

| 序号 | 手势名称 | 手语动作描述 | 触发动作 |
|------|----------|-------------|----------|
| 1 | 救命 | 双手交叉挥动 | 立即发送SOS短信+GPS |
| 2 | 帮我 | 双手握拳上下交替 | 发送求助短信 |
| 3 | 危险 | 双手向前推 | 触发闪光灯警报 |
| 4 | 停 | 双手向前推平 | 发送位置共享 |
| 5 | 快来 | 双手向身体方向拉 | 发送紧急呼叫 |
| 6 | 打电话 | 拇指和小指伸出 | 自动拨打紧急联系人 |
| 7 | 受伤 | 指向身体某部位 | 发送受伤信息 |
| 8 | 迷路 | 双手在头顶画圈 | 发送当前位置 |

### 3.2 日常沟通手势

| 序号 | 手势名称 | 手语动作描述 |
|------|----------|-------------|
| 9 | 你好 | 右手挥手 |
| 10 | 谢谢 | 右手从胸口向前推 |
| 11 | 对不起 | 右手握拳在胸口转 |
| 12 | 请 | 右手向前伸出 |
| 13 | 是 | 右手点头动作 |
| 14 | 不是 | 右手左右摆 |
| 15 | 吃饭 | 右手向嘴边送 |
| 16 | 喝水 | 右手做喝水动作 |
| 17 | 厕所 | 左手握拳右手包住 |
| 18 | 医院 | 右手在左手腕上比十字 |
| 19 | 警察 | 右手敬礼动作 |
| 20 | 回家 | 右手做房子形状 |
| 21-50 | ... | 扩展词汇（根据实际需求） |

### 3.3 词汇集管理策略

```
词汇集分为3个优先级：
├── P0 紧急手势（8个）：SOS核心，置信度阈值降低到0.65
├── P1 基础手势（12个）：日常高频，置信度阈值0.75
└── P2 扩展手势（30个）：按需扩展，置信度阈值0.80
```

---

## 四、快速识别优化策略

### 4.1 问题分析：为什么手语动作快就难识别？

```
正常速度：  [手型A] → [手型A] → [过渡] → [手型B] → [手型B]
            帧1      帧2      帧3      帧4      帧5
            ✅        ✅        ✅        ✅        ✅  全部清晰

快速动作：  [手型A] → [运动模糊] → [运动模糊] → [手型B]
            帧1        帧2          帧3          帧4
            ✅         ❌           ❌           ✅  中间帧模糊
```

**核心矛盾**：手语动作越快 → 运动模糊越严重 → 关键点检测越不准

### 4.2 解决方案：五层加速策略

#### 策略1：CameraX高帧率配置

```kotlin
// 关键配置：使用60fps而不是默认30fps
val cameraProvider = ProcessCameraProvider.getInstance(context).get()
val imageAnalysis = ImageAnalysis.Builder()
    .setTargetResolution(Size(640, 480))
    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
    .build()

// 请求60fps帧率（如果设备支持）
val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
val camera = cameraProvider.bindToLifecycle(
    lifecycleOwner, cameraSelector, imageAnalysis, imageCapture
)
camera.cameraControl.setZoomRatio(1.0f)

// 通过Camera2Interop设置高帧率
Camera2Interop.setCaptureRequestOptions(imageAnalysis) { builder ->
    builder.set(
        CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
        Range(60, 60)  // 目标60fps
    )
}
```

#### 策略2：运动模糊检测与帧选择

```kotlin
/**
 * 运动模糊检测器
 * 原理：计算相邻帧之间的像素差异，差异越大说明运动越快
 */
class MotionBlurDetector {
    
    // 运动模糊阈值（超过此值认为帧模糊）
    private val blurThreshold = 30.0f
    
    /**
     * 计算帧的模糊程度
     * @param currentFrame 当前帧的亮度数据
     * @param previousFrame 上一帧的亮度数据
     * @return 模糊分数（0-100，越高越模糊）
     */
    fun calculateBlurScore(
        currentFrame: ByteArray,
        previousFrame: ByteArray
    ): Float {
        if (currentFrame.size != previousFrame.size) return 100f
        
        var diffSum = 0L
        val pixelCount = currentFrame.size
        
        // 计算逐像素差异
        for (i in currentFrame.indices) {
            diffSum += Math.abs(currentFrame[i].toInt() - previousFrame[i].toInt())
        }
        
        // 归一化到0-100
        return (diffSum.toFloat() / pixelCount) * 100f
    }
    
    /**
     * 判断帧是否可用
     * @param blurScore 模糊分数
     * @return true=可用, false=模糊需要跳过
     */
    fun isFrameUsable(blurScore: Float): Boolean {
        return blurScore < blurThreshold
    }
}
```

#### 策略3：关键点平滑滤波

```kotlin
/**
 * 手部关键点平滑器
 * 原理：对快速运动导致的关键点抖动进行卡尔曼滤波平滑
 */
class LandmarkSmoother {
    
    // 卡尔曼滤波器参数
    private val processNoise = 0.03f    // 过程噪声
    private val measurementNoise = 0.1f // 测量噪声
    
    // 历史关键点缓冲（最近5帧）
    private val historyBuffer = ArrayDeque<List<NormalizedLandmark>>(5)
    
    /**
     * 平滑关键点
     * @param rawLandmarks 原始检测到的关键点
     * @return 平滑后的关键点
     */
    fun smooth(rawLandmarks: List<NormalizedLandmark>): List<NormalizedLandmark> {
        // 加入历史缓冲
        historyBuffer.addLast(rawLandmarks)
        if (historyBuffer.size > 5) historyBuffer.removeFirst()
        
        // 如果历史帧不足，直接返回原始值
        if (historyBuffer.size < 3) return rawLandmarks
        
        // 对每个关键点做加权平均
        return rawLandmarks.mapIndexed { index, landmark ->
            val weights = floatArrayOf(0.1f, 0.15f, 0.2f, 0.25f, 0.3f)  // 越新的帧权重越大
            val startIdx = maxOf(0, 5 - historyBuffer.size)
            
            var smoothedX = 0f
            var smoothedY = 0f
            var smoothedZ = 0f
            var weightSum = 0f
            
            for (i in startIdx until 5) {
                val histFrame = historyBuffer[i]
                if (index < histFrame.size) {
                    val w = weights[i]
                    smoothedX += histFrame[index].x * w
                    smoothedY += histFrame[index].y * w
                    smoothedZ += histFrame[index].z * w
                    weightSum += w
                }
            }
            
            NormalizedLandmark(
                x = smoothedX / weightSum,
                y = smoothedY / weightSum,
                z = smoothedZ / weightSum
            )
        }
    }
}
```

#### 策略4：滑动窗口 + 快速推理

```kotlin
/**
 * 手语识别推理管理器
 * 职责：管理滑动窗口、触发推理、后处理决策
 */
class SignLanguageRecognizer(
    private val context: Context
) {
    // 滑动窗口大小（30帧 ≈ 1秒）
    private val windowSize = 30
    
    // 推理步长（每5帧做一次推理）
    private val inferenceStep = 5
    
    // 关键点缓冲区 [帧数, 手数, 关键点数, 坐标数]
    private val landmarkBuffer = Array(windowSize) { 
        Array(2) { FloatArray(63) }  // 2只手，每手63维(21点×3坐标)
    }
    
    // 当前帧计数
    private var frameCount = 0
    
    // TFLite推理器
    private var interpreter: Interpreter? = null
    
    // 词汇表
    private val vocabulary = arrayOf(
        "救命", "帮我", "危险", "停", "快来", "打电话", "受伤", "迷路",
        "你好", "谢谢", "对不起", "请", "是", "不是", "吃饭", "喝水",
        "厕所", "医院", "警察", "回家"
    )
    
    /**
     * 处理新的一帧关键点数据
     * @param leftHand 左手21个关键点（可能为空）
     * @param rightHand 右手21个关键点（可能为空）
     * @return 识别结果（手势名称 + 置信度），null表示未识别
     */
    fun processFrame(
        leftHand: List<NormalizedLandmark>?,
        rightHand: List<NormalizedLandmark>?
    ): RecognitionResult? {
        // 将关键点存入缓冲区
        val bufferIndex = frameCount % windowSize
        landmarkBuffer[bufferIndex][0] = normalizeLandmarks(leftHand)
        landmarkBuffer[bufferIndex][1] = normalizeLandmarks(rightHand)
        
        frameCount++
        
        // 每隔inferenceStep帧做一次推理
        if (frameCount % inferenceStep == 0 && frameCount >= windowSize) {
            return runInference()
        }
        
        return null
    }
    
    /**
     * 执行TFLite推理
     */
    private fun runInference(): RecognitionResult? {
        val interpreter = this.interpreter ?: return null
        
        // 组装输入张量 [1, 30, 126] (30帧 × 2手 × 21点 × 3坐标)
        val inputTensor = Array(1) { 
            Array(windowSize) { frameIdx ->
                val leftHand = landmarkBuffer[frameIdx][0]
                val rightHand = landmarkBuffer[frameIdx][1]
                leftHand + rightHand  // 拼接 [126]
            }
        }
        
        // 输出张量 [1, 20] (20个类别的概率)
        val outputTensor = Array(1) { FloatArray(vocabulary.size) }
        
        // 执行推理（~15ms）
        interpreter.run(inputTensor, outputTensor)
        
        // 获取最高概率
        val probs = outputTensor[0]
        val maxIndex = probs.indices.maxByOrNull { probs[it] } ?: return null
        val maxProb = probs[maxIndex]
        
        // SOS手势使用更低的置信度阈值
        val threshold = if (maxIndex < 8) 0.65f else 0.75f
        
        return if (maxProb >= threshold) {
            RecognitionResult(
                gesture = vocabulary[maxIndex],
                gestureIndex = maxIndex,
                confidence = maxProb,
                isSOS = maxIndex < 8  // 前8个是SOS手势
            )
        } else {
            null
        }
    }
}
```

#### 策略5：SOS紧急加速通道

```kotlin
/**
 * SOS紧急触发引擎
 * 核心逻辑：检测到SOS手势后立即触发，不等连续确认
 */
class SosTriggerEngine(
    private val context: Context,
    private val gpsProvider: GpsProvider,
    private val smsSender: SmsSender,
    private val vibrationEngine: VibrationEngine,
    private val flashController: FlashController
) {
    // SOS手势在词汇表中的索引范围
    private val sosGestureIndices = 0..7
    
    // 最近一次SOS触发时间
    private var lastSosTriggerTime = 0L
    
    // SOS冷却时间（5秒内不重复触发）
    private val sosCooldownMs = 5000L
    
    // 连续确认计数器
    private var consecutiveCount = 0
    private val requiredConsecutive = 2  // 连续2帧确认即触发
    
    /**
     * 评估识别结果是否应该触发SOS
     * @param result 手语识别结果
     * @return 是否触发SOS
     */
    fun evaluate(result: RecognitionResult): Boolean {
        val now = System.currentTimeMillis()
        
        // 非SOS手势直接忽略
        if (!result.isSOS) {
            consecutiveCount = 0
            return false
        }
        
        // 冷却期内不重复触发
        if (now - lastSosTriggerTime < sosCooldownMs) {
            return false
        }
        
        // 连续确认（SOS手势需要连续2帧确认）
        consecutiveCount++
        
        if (consecutiveCount >= requiredConsecutive) {
            // 触发SOS！
            triggerSOS(result.gesture)
            lastSosTriggerTime = now
            consecutiveCount = 0
            return true
        }
        
        return false
    }
    
    /**
     * 执行SOS触发
     */
    private fun triggerSOS(gestureName: String) {
        // 1. 立即振动反馈（让用户知道已触发）
        vibrationEngine.vibrateEmergency()
        
        // 2. 闪光灯闪烁（吸引周围人注意）
        flashController.startStrobe()
        
        // 3. 获取GPS定位
        val location = gpsProvider.getCurrentLocation()
        
        // 4. 发送SOS短信给紧急联系人
        val message = "🚨 紧急求助！\n" +
            "触发手势：$gestureName\n" +
            "位置：${location.latitude}, ${location.longitude}\n" +
            "地图：https://maps.google.com/?q=${location.latitude},${location.longitude}\n" +
            "时间：${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.CHINA).format(java.util.Date())}"
        
        smsSender.sendToEmergencyContacts(message)
        
        // 5. 记录日志
        Log.w("SOS", "SOS triggered by gesture: $gestureName at ${location.latitude}, ${location.longitude}")
    }
}
```

---

## 五、MediaPipe Hands 集成方案

### 5.1 依赖配置

```gradle
// app/build.gradle
dependencies {
    // MediaPipe Hands - 手部关键点检测
    implementation 'com.google.mediapipe:tasks-vision:0.10.8'
    
    // TFLite - 手势分类模型推理
    implementation 'org.tensorflow:tensorflow-lite:2.14.0'
    implementation 'org.tensorflow:tensorflow-lite-support:0.4.4'
}
```

### 5.2 MediaPipe Hands 初始化

```kotlin
/**
 * MediaPipe Hands 手部检测器封装
 * 职责：接收CameraX帧，输出21个手部关键点
 */
class HandLandmarkDetector(context: Context) {
    
    private var handLandmarker: HandLandmarker? = null
    
    init {
        // 创建HandLandmarker选项
        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(
                BaseOptions.builder()
                    .setModelAssetPath("hand_landmarker.task")
                    .build()
            )
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumHands(2)                    // 最多检测2只手
            .setMinHandDetectionConfidence(0.7f) // 手部检测置信度
            .setMinTrackingConfidence(0.5f)      // 跟踪置信度
            .setMinHandPresenceConfidence(0.7f)  // 手部存在置信度
            .setResultListener { result, _ ->
                // 回调：将结果传递给下游处理
                onHandResult(result)
            }
            .setErrorListener { error ->
                Log.e("HandDetector", "Error: ${error.message}")
            }
            .build()
        
        handLandmarker = HandLandmarker.createFromOptions(context, options)
    }
    
    /**
     * 处理CameraX帧
     * @param image CameraX提供的图像数据
     * @param timestampMs 时间戳（毫秒）
     */
    fun processFrame(image: Image, timestampMs: Long) {
        handLandmarker?.detectAsync(image, timestampMs)
    }
    
    /**
     * 结果回调
     */
    private fun onHandResult(result: HandLandmarkerResult) {
        val leftHand = result.landmarks().firstOrNull { 
            it.any { landmark -> landmark.x() < 0.5f }  // 简单区分左右手
        }
        val rightHand = result.landmarks().firstOrNull { 
            it.any { landmark -> landmark.x() >= 0.5f }
        }
        
        // 将结果传递给手语识别器
        signLanguageRecognizer.processFrame(leftHand, rightHand)
    }
}
```

---

## 六、模型训练方案

### 6.1 训练数据采集

```
数据采集流程：
├── 1. 请10-20位手语使用者
├── 2. 每人对每个手势做10-20次
├── 3. 从不同角度（正面、左侧、右侧）录制
├── 4. 不同光照条件（室内、室外、夜间）
├── 5. 不同速度（正常速度、快速、极快）
└── 6. 预计总数据量：20人 × 20手势 × 15次 × 3角度 = 18,000条
```

### 6.2 训练脚本

```python
# train_gesture_model.py
# 手语手势分类模型训练脚本

import numpy as np
import tensorflow as tf
from tensorflow.keras import layers, models

def build_gru_classifier(
    num_frames=30,      # 时序窗口大小
    num_landmarks=126,   # 21点 × 2手 × 3坐标
    num_classes=20       # 手势类别数
):
    """
    构建GRU时序分类模型
    
    架构：
    Input [30, 126]
    → GRU(128, return_sequences=True) + Dropout(0.3)
    → GRU(64, return_sequences=False) + Dropout(0.3)
    → Dense(32, activation='relu')
    → Dense(num_classes, activation='softmax')
    
    模型大小：~200KB (量化后~50KB)
    推理延迟：~15ms (TFLite, CPU)
    """
    model = models.Sequential([
        layers.Input(shape=(num_frames, num_landmarks)),
        layers.GRU(128, return_sequences=True),
        layers.Dropout(0.3),
        layers.GRU(64),
        layers.Dropout(0.3),
        layers.Dense(32, activation='relu'),
        layers.Dense(num_classes, activation='softmax')
    ])
    
    model.compile(
        optimizer='adam',
        loss='sparse_categorical_crossentropy',
        metrics=['accuracy']
    )
    
    return model

def prepare_data(landmarks_data, labels):
    """
    预处理关键点数据
    
    步骤：
    1. 归一化：以手腕为原点，按手掌大小缩放
    2. 填充/截断：统一到30帧
    3. 数据增强：随机旋转、缩放、平移
    """
    processed = []
    for sequence in landmarks_data:
        # 归一化
        normalized = normalize_landmarks(sequence)
        # 填充到30帧
        padded = pad_sequence(normalized, target_length=30)
        processed.append(padded)
    
    return np.array(processed), np.array(labels)

def export_to_tflite(model, output_path):
    """
    导出为TFLite格式（INT8量化）
    
    量化后模型大小：~50KB
    推理速度提升：2-3倍
    """
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.target_spec.supported_types = [tf.int8]
    
    tflite_model = converter.convert()
    
    with open(output_path, 'wb') as f:
        f.write(tflite_model)
    
    print(f"Model exported to {output_path}")
    print(f"Model size: {len(tflite_model) / 1024:.1f} KB")

# 主训练流程
if __name__ == "__main__":
    # 加载数据
    X_train, y_train = load_training_data("data/train/")
    X_val, y_val = load_training_data("data/val/")
    
    # 构建模型
    model = build_gru_classifier(num_classes=20)
    
    # 训练
    model.fit(
        X_train, y_train,
        validation_data=(X_val, y_val),
        epochs=100,
        batch_size=32,
        callbacks=[
            tf.keras.callbacks.EarlyStopping(patience=10),
            tf.keras.callbacks.ReduceLROnPlateau()
        ]
    )
    
    # 导出TFLite
    export_to_tflite(model, "models/gesture_classifier.tflite")
```

---

## 七、SOS场景完整流程

### 7.1 用户操作流程

```
用户操作：
1. 打开APP → 点击"SOS手语"按钮
2. 前置摄像头自动开启
3. 对准摄像头比出"救命"手势
4. 系统识别到SOS手势
5. 手机振动 + 闪光灯闪烁（反馈已触发）
6. 自动发送GPS定位短信给紧急联系人
7. 屏幕显示"✅ SOS已发送"
```

### 7.2 系统处理流程

```
时间线（从手势开始到SOS发出）：

T=0ms     用户开始比"救命"手势
T=0-33ms  CameraX捕获第一帧
T=33ms    MediaPipe Hands检测到手部关键点
T=66ms    滑动窗口填充2帧
T=100ms   滑动窗口填充3帧
T=200ms   滑动窗口填充6帧，触发首次推理
T=215ms   GRU模型推理完成（~15ms）
T=215ms   识别结果："救命" 置信度0.82 > 0.65
T=215ms   consecutiveCount++（第1次确认）
T=250ms   第7帧推理：再次识别为"救命"
T=265ms   consecutiveCount++（第2次确认）→ 触发SOS！
T=265ms   振动马达立即反馈
T=265ms   闪光灯开始闪烁
T=300ms   GPS定位获取完成
T=350ms   短信发送队列

总延迟：从手势开始到SOS发出 ≈ 350ms
```

### 7.3 家属端收到的消息

```
🚨 紧急求助！

触发手势：救命
位置：31.2304, 121.4737
地图：https://maps.google.com/?q=31.2304,121.4737
时间：2026-06-01 14:30:25

请立即联系或前往该位置！
```

---

## 八、实现步骤与排期

### Phase 1：基础能力建设（第1-2周）

| 任务 | 说明 | 产出 |
|------|------|------|
| 集成MediaPipe Hands | 添加依赖，创建HandLandmarkDetector | 能检测手部21个关键点 |
| 关键点预处理 | 中心化、归一化、旋转补偿 | LandmarkSmoother类 |
| 滑动窗口缓冲 | 30帧缓冲区管理 | LandmarkBuffer类 |
| 基础UI | 手语识别界面（摄像头预览+文字显示） | SignLanguageScreen |

### Phase 2：模型训练（第3-4周）

| 任务 | 说明 | 产出 |
|------|------|------|
| 数据采集方案 | 设计采集流程、录制模板 | 数据采集指南 |
| 数据标注工具 | 关键点数据标注脚本 | 标注工具 |
| 模型训练 | GRU分类器训练 | gesture_classifier.tflite |
| 模型评估 | 准确率、延迟、模型大小 | 评估报告 |

### Phase 3：SOS集成（第5周）

| 任务 | 说明 | 产出 |
|------|------|------|
| SOS触发引擎 | 连续确认+冷却时间+紧急加速 | SosTriggerEngine |
| 短信发送 | GPS定位+短信模板 | SmsSender |
| 振动/闪光反馈 | 紧急振动+闪光灯 | VibrationEngine |
| 家属端通知 | HTTP推送SOS信息 | 家属端接收页面 |

### Phase 4：优化与测试（第6周）

| 任务 | 说明 | 产出 |
|------|------|------|
| 快速动作优化 | 运动模糊检测+帧选择 | MotionBlurDetector |
| 多人场景 | 双手同时识别 | 双手处理逻辑 |
| 低光照优化 | 夜间识别增强 | 图像增强预处理 |
| 压力测试 | 不同速度、不同人、不同光照 | 测试报告 |

---

## 九、风险与对策

| 风险 | 概率 | 影响 | 对策 |
|------|------|------|------|
| 快速动作识别率低 | 高 | 高 | 五层加速策略+数据增强（快速样本） |
| 训练数据不足 | 中 | 高 | 众包采集+合成数据增强 |
| 低光照识别差 | 中 | 中 | CameraX自动曝光+图像增强 |
| 不同人手型差异大 | 高 | 中 | 多人数据采集+归一化处理 |
| 模型推理延迟高 | 低 | 高 | INT8量化+XNNPACK加速 |

---

> 文档结束
> 下一步：确认方案后开始Phase 1编码实现
