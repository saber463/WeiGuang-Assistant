# 项目开发BUG排查日志
> 时区标准：UTC+8 北京时间
> 使用规范：出现同类BUG优先查阅历史记录，无匹配方案再新增记录
> 记录格式：【时间】【BUG分类】问题现象 | 根因分析 | 最终修复方案

---

## 历史BUG记录区

### 【2026-06-09 18:00】【编译错误】MainActivity.kt 跨函数变量引用未通过参数传递

**问题现象**：
Kotlin编译失败，报错：
- `ttsController` 在 MainScreen body 中未解析
- `sosController`、`sosRunning`、`sosPairingCode`、`sosStatusMessage` 在 SosFamilyCard 内联回调中无法访问
- `imageCapture` 在 MainScreen body 中无法赋值
- `cameraExecutor` 在 CameraPreviewCard 的 AndroidView factory 中未定义

**报错/根源原因**：
- `MainScreen(...)` 和 `CameraPreviewCard(...)` 是文件级 `private fun`，**不是** `AppRoot` 的嵌套函数，因此不能通过闭包访问 AppRoot 内的局部变量。
- 原始代码在 AppRoot 的 `else` 分支中以内联 lambda 形式传递回调，内联 lambda 可以访问闭包变量，但将这些回调移到文件级函数后就丢失了闭包。

**修复解决办法**：
1. **AppRoot 中定义回调 lambdas**：在 `if (showSplash)` 之前定义 `onSosStartCallback`、`onSosStopCallback`、`onCopyPairingCodeCallback`、`onImageCaptureReadyCallback`、`onOpenFamilyModeCallback`，这些 lambda 在 AppRoot 闭包内，可以访问 AppRoot 的所有变量。
2. **MainScreen 参数列表添加**：新增 `sosRunning`、`sosPairingCode`、`sosStatusMessage`、`onStartSos`、`onStopSos`、`onCopyPairingCode`、`onOpenFamilyMode`、`onImageCaptureReady`、`cameraExecutor` 共9个参数。
3. **MainScreen call site 更新**：在 AppRoot 的 `else` 分支中，将回调 lambdas 通过命名参数传递给 MainScreen。
4. **MainScreen body 更新**：SosFamilyCard 的内联回调替换为参数引用；CameraPreviewCard 的 `onImageCaptureReady` 和 `cameraExecutor` 改为参数传递。
5. **CameraPreviewCard 参数列表**：末尾添加 `cameraExecutor: java.util.concurrent.ExecutorService`。
6. **注意**：定义回调 lambdas 时不能插在 `if-else if-else` 分支之间（会破坏语法链），必须放在整个 if 链之前。

### 【2026-06-09 18:10】【编译错误】sosController.start() 参数不匹配

**问题现象**：
```
Cannot find a parameter with this name: context
Cannot find a parameter with this name: sosTextProvider
```

**报错/根源原因**：
`SosStreamController.start(imageCapture: ImageCapture, port: Int = SosConstants.defaultPort)` 方法签名只接受 `imageCapture` 和可选的 `port`，不接受 `context` 和 `sosTextProvider` 参数。

**修复解决办法**：
将调用改为 `sosController.start(imageCapture = capture)`，移除多余的 `context` 和 `sosTextProvider` 参数。

### 【2026-06-09 18:10】【编译错误】FeatureCatalog.sos 不存在

**问题现象**：
```
Unresolved reference: sos
```

**报错/根源原因**：
`FeatureCatalog` 中不存在 `sos` 字段（仅有 `cameraPreview`、`tts`、`flashlight`、`vibration`、`quickSettingsTile`、`accelerometer`、`gyroscope`、`stepCounter`）。

**修复解决办法**：
将 `analytics.trackFeatureUsage(FeatureCatalog.sos.key)` 替换为 `analytics.trackFeatureUsage("sos")`，使用硬编码字符串键。

### 【2026-06-09 18:15】【编译错误】String? → String 类型不匹配

**问题现象**：
```
Type mismatch: inferred type is String? but String was expected
```

**报错/根源原因**：
`MainScreen` 参数列表中 `sosStatusMessage` 声明为 `String?` (可空)，但 `SosFamilyCard` 的 `statusMessage` 参数期望 `String` (非空)。

**修复解决办法**：
将 `MainScreen` 参数 `sosStatusMessage: String?` 改为 `sosStatusMessage: String`，与 AppRoot 中的实际初始值（非空字符串）一致。

### 【2026-06-09 18:20】【依赖移除】HandLandmarkDetector.kt 移除 ML Kit Pose Detection

**问题现象**：
编译找不到 `com.google.mlkit.vision.pose.*` 依赖（build.gradle 中未配置该依赖）。

**报错/根源原因**：
`HandLandmarkDetector.kt` 使用了 `com.google.mlkit.vision.pose.PoseDetection`、`PoseDetector`、`PoseDetectorOptions`，但项目 build.gradle 只引入了 `com.google.mlkit:object-detection` 和 `com.google.mlkit:text-recognition-chinese`，没有引入 pose 检测库。

**修复解决办法**：
1. 移除所有 `com.google.mlkit.vision.pose` 导入
2. 删除 `poseDetector` 字段和 `initialize()` 中的初始化代码
3. `detect()` 方法直接返回空的 `HandDetectionResult`（不做实际检测）
4. 移除 `await()` 辅助扩展函数（依赖 `com.google.mlkit.common.Task`）
5. 保留 `classifyGesture()` 方法和 `GestureClassifier`（纯数学分类，无外部依赖）

---

## 新增BUG填写模板
### 【YYYY-MM-DD HH:MM】【BUG分类】
**问题现象**：
描述运行报错、闪退、功能异常、编译失败、接口报错等完整表现

**报错/根源原因**：
源码问题、依赖版本冲突、权限缺失、配置错误、逻辑漏洞、环境变量、数据库字段异常等

**修复解决办法**：
具体代码修改、配置调整、依赖降级/升级、权限开启、逻辑改写、清理缓存等可直接复用步骤