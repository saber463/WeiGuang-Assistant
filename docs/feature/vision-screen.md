# VisionScreen 视觉识别与障碍物检测集成界面

## 1. 文档目标

本文档说明 `VisionScreen.kt` 的功能设计、组件结构、数据流和生命周期管理，帮助后续维护者理解并扩展该界面。

## 2. 功能概述

VisionScreen 提供两个核心功能模块，通过 Material3 TabRow 切换：

| Tab | 功能 | 底层引擎 |
|-----|------|---------|
| **实时物品识别** | CameraX 预览 + ML Kit Image Labeling 识别画面中的物品 | `CameraObjectRecognizer` |
| **障碍物检测** | CameraX ImageAnalysis + ML Kit Object Detection 检测障碍物方位/距离 | `ObstacleDetectionManager` |

两个模块互斥运行——切换 Tab 时自动停止当前模块的检测，释放 CameraX 资源。

## 3. 文件信息

- **文件路径**: `app/src/main/java/com/weiguangchangxing/weiguang_plus/feature/vision/VisionScreen.kt`
- **包名**: `com.weiguangchangxing.weiguang_plus.feature.vision`
- **公开 Composable**: `VisionScreen(modifier: Modifier = Modifier)`

## 4. 组件树

```
VisionScreen (public)
│
├── TabRow
│   ├── Tab: "实时物品识别" (selectedTab = 0)
│   └── Tab: "障碍物检测" (selectedTab = 1)
│
├── ObjectRecognitionContent (private) —— Tab A
│   └── ScrollPage
│       ├── HeroCard: 标题 + 说明
│       ├── SectionTitle: "相机预览"
│       ├── Card[AndroidView(PreviewView)]: 260dp 相机预览
│       ├── SectionTitle: "当前识别结果"
│       ├── HighlightCard: (物品名称 | 置信度 | 时间)
│       ├── SectionTitle: "最近识别历史"
│       ├── InfoCard: 历史列表 (reversed, 最多5条)
│       └── SectionTitle: "控制"
│           └── Button: "开始识别" / "停止识别"
│
└── ObstacleDetectionContent (private) —— Tab B
    └── ScrollPage
        ├── HeroCard: 标题 + 说明
        ├── SectionTitle: "检测状态"
        ├── HighlightCard: (检测器状态 | 障碍物数量 | 错误信息)
        ├── SectionTitle: "当前障碍物（N）"
        ├── ObstacleCard × N: (方向 | 距离 | 标签 | 是否行人 | 置信度)
        └── SectionTitle: "控制"
            └── Button: "开始检测" / "停止检测"
```

## 5. 数据流

### 5.1 Tab A — 实时物品识别

```
CameraObjectRecognizer.state (StateFlow<RecognitionState>)
  ├── isRunning: Boolean
  ├── currentTopObject: RecognizedObject?  →  HighlightCard(title, confidence)
  ├── recentObjects: List<RecognizedObject> →  InfoCard(历史列表)
  └── errorMessage: String?  →  错误提示
```

- `RecognizedObject` 字段: `label`(名称), `confidence`(置信度 0~1), `timestamp`(毫秒时间戳)
- 历史最多保留 5 条，界面按时间倒序展示

### 5.2 Tab B — 障碍物检测

```
ObstacleDetectionManager.state (StateFlow<ObstacleDetectionState>)
  ├── isDetecting: Boolean
  ├── lastDetectedCount: Int
  ├── totalDetections: Int
  └── errorMessage: String?

ObstacleDetectionManager.setDetectedListener → onObstacleDetected(obstacles)
  └── currentObstacles (mutableStateListOf<ObstacleData>)
       └── ObstacleCard × N

TTSManager.speakNow(ttsMessage)  ← 最近的障碍物触发播报
```

- `ObstacleData` 字段: `direction`(方位), `distance`(米), `isHuman`(是否行人), `label`(标签), `confidence`(置信度)
- TTS 播报内容: "注意{方向}约{X}米{，有人}"

## 6. 生命周期管理

### 6.1 Tab 切换逻辑

```
LaunchedEffect(selectedTab) {
    when (selectedTab) {
        0 -> obstacleManager?.stopDetection()   // 停止障碍物检测
        1 -> recognizer.stopRecognition()       // 停止物品识别
    }
}
```

### 6.2 页面销毁

```kotlin
DisposableEffect(Unit) {
    onDispose {
        recognizer.release()
        obstacleManager?.release()
    }
}
```

### 6.3 重要：ObstacleDetectionManager 实例管理

`ObstacleDetectionManager.stopDetection()` 会关闭内部的 ML Kit Object Detector（调用 `close()`），导致同一个实例无法再次启动。因此：

- 每次用户点击"开始检测"时，**创建新的** `ObstacleDetectionManager` 实例
- 每次点击"停止检测"或切换 Tab 时，调用 `stopDetection()` 并将引用置 `null`
- 页面销毁时，若实例仍存在则调用 `release()`

```kotlin
onStartDetection = {
    val newManager = ObstacleDetectionManager(context, lifecycleOwner)
    obstacleManager = newManager
    newManager.startDetection()
}
```

## 7. UI 组件风格

所有私有辅助 Composable 严格遵循 MainActivity / TTSSettingsScreen / VoiceAssistantScreen 已有风格：

| Composable | 样式 |
|-----------|------|
| `ScrollPage` | `fillMaxSize` + `verticalScroll` + 20dp/16dp padding + 12dp spacing |
| `HeroCard` | `primaryContainer` 背景 + 24dp 圆角 + 20dp padding |
| `HighlightCard` | `secondaryContainer` 背景 + 20dp 圆角 + 18dp padding |
| `InfoCard` | `surface` 背景 + 20dp 圆角 + 18dp padding + 10dp spacing |
| `SectionTitle` | `titleLarge` + `Bold` + 6dp top padding |
| `ObstacleCard` | 行人为 `errorContainer` 背景，非行人为 `secondaryContainer` 背景 |

## 8. 依赖清单

| 依赖 | 用途 |
|------|------|
| `androidx.camera:camera-view` | PreviewView（相机预览） |
| `androidx.compose.material3` | TabRow, Tab, Button 等 Material3 组件 |
| `com.google.mlkit:image-labeling` | CameraObjectRecognizer 底层标签识别 |
| `com.google.mlkit:object-detection` | ObstacleDetectionManager 底层物体检测 |
| `TTSManager` (core.tts) | 障碍物检测结果语音播报 |

## 9. 接入方式

在任意 Compose 页面中直接使用：

```kotlin
@Composable
fun SomeScreen() {
    VisionScreen(
        modifier = Modifier.padding(innerPadding)
    )
}
```

如需在 MainActivity 的 NavigationBar 中新增入口，在 `AppSection` 枚举新增条目并在 `when` 分支中添加 `VisionScreen`。

## 10. 后续扩展建议

1. **添加前置摄像头支持** — 在 Tab A 中增加摄像头切换按钮（需修改 `CameraObjectRecognizer`）
2. **障碍物检测添加 Preview** — 目前 ObstacleDetectionManager 不包含 Preview 用例，可考虑添加实时画面叠加检测框
3. **检测间隔控制** — CameraObjectRecognizer 已有 `setDetectionInterval()`（空实现），可补充实际限流逻辑
4. **历史记录持久化** — 将识别历史保存到 DataStore 或 Room 中，支持跨页面查看
5. **障碍物过滤降噪** — 添加时间窗口去重逻辑，避免 TTS 频繁播报相同障碍物