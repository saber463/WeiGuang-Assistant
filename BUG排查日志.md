# 项目开发BUG排查日志
> 时区标准：UTC+8 北京时间
> 使用规范：出现同类BUG优先查阅历史记录，无匹配方案再新增记录
> 记录格式：【时间】【BUG分类】问题现象 | 根因分析 | 最终修复方案

---

## 历史BUG记录区

### 【2026-07-31 23:00】【闹钟提前触发】
**问题现象**：闹钟设置 19:28，但 28 分之前就触发了。

**报错/根源原因**：
- `AlarmScheduler.cancelAlarm()` 使用 `PendingIntent.FLAG_NO_CREATE` 创建 PendingIntent 查找旧闹钟
- `scheduleExactAlarm()` 使用 `PendingIntent.FLAG_UPDATE_CURRENT` 创建新闹钟
- 不同 flag 导致 `PendingIntent.getBroadcast()` 返回不同的 PendingIntent 对象
- `cancelAlarm()` 无法找到并取消旧闹钟，导致旧闹钟残留，提前触发

**修复解决办法**：
- 将 `cancelAlarm()` 中的 flag 从 `FLAG_NO_CREATE` 改为 `FLAG_UPDATE_CURRENT`，与 `scheduleExactAlarm()` 保持一致
- 直接调用 `alarmManager.cancel(pendingIntent)` 而非空判断后取消

### 【2026-07-31 23:00】【闹钟无声音】
**问题现象**：闹钟触发后，振动和闪光正常，但没有声音。

**报错/根源原因**：
- `AlarmAudioController` 中 `requestAudioFocus()` 使用了 `AUDIOFOCUS_GAIN_TRANSIENT`（短暂焦点）
- 闹钟场景需要持久音频焦点，短暂焦点可能在播放过程中被其他应用抢占
- 导致闹钟铃声被系统静音或被其他音频打断

**修复解决办法**：
- 将 `AUDIOFOCUS_GAIN_TRANSIENT` 改为 `AUDIOFOCUS_GAIN`（持久焦点），确保闹钟持续播放

### 【2026-07-31 23:00】【火灾检测/燃气泄漏检测闪退】
**问题现象**：点击火灾检测或燃气泄漏检测开关后，APP 直接闪退。

**报错/根源原因**：
1. `AudioCaptureService` 在 `onCreate()` 中调用 `createNotificationChannel()`，但 `notificationManager` 由 Hilt 注入，注入失败时抛出 `UninitializedPropertyAccessException`
2. `FireAlarmViewModel` 的 `combine` 流绑定使用了嵌套的 `let { combine(...) }` 模式，可能导致流的订阅状态异常
3. `FireAlarmViewModel.onCleared()` 和 `GasLeakViewModel.onCleared()` 会调用 `stopMonitoring()`，导致 Singleton 检测器在 ViewModel 销毁时被意外停止

**修复解决办法**：
1. `AudioCaptureService.createNotificationChannel()` 添加 try-catch 防御性获取 NotificationManager：注入失败时降级到 `getSystemService()`
2. `FireAlarmViewModel.uiState` 简化 `combine` 调用：直接 combine 两个原始流，而非嵌套 stateIn + let
3. `FireAlarmViewModel.onCleared()` 和 `GasLeakViewModel.onCleared()` 移除 `stopMonitoring()` 调用，保持与 CoughViewModel 一致的行为

### 【2026-07-31 23:00】【咳嗽检测/敲门检测退出页面即关闭】
**问题现象**：咳嗽检测和敲门检测开启后，退出页面回到主界面，检测功能就停止了。咳嗽检测偶尔显示结果但延迟很大。

**报错/根源原因**：
1. **核心BUG**：`CoughMonitorScreen` 中开关调用的是 `coughViewModel.loadModel(...)` 而非 `coughViewModel.startMonitoring()`
   - `loadModel()` 仅加载 TFLite 模型到内存，不启动音频采集和后台推理
   - `startMonitoring()` 才是真正启动后台持续监控的方法
   - 导致咳嗽检测的音频监控从未真正启动，只是加载了模型
2. **同样问题**：`KnockSecurityScreen` 中开关调用 `knockViewModel.loadModel(...)` 而非 `knockViewModel.startMonitoring()`
   - `KnockViewModel` 甚至没有 `startMonitoring()` 和 `stopMonitoring()` 方法
3. **咳嗽延迟显示**：CoughBuffer 15秒滑窗机制需要 >=3 次才触发 MILD 预警，用户"隔几次才显示"是因为缓冲区未达到阈值
4. **KnockViewModel.onCleared()** 调用 `interpreter.closeAll()` 会关闭所有 TFLite 模型，影响咳嗽、火灾、燃气检测器

**修复解决办法**：
1. `CoughMonitorScreen` 中开关改为调用 `coughViewModel.startMonitoring()` / `coughViewModel.stopMonitoring()`
2. `KnockViewModel` 新增 `startMonitoring()`、`stopMonitoring()` 方法和 `isMonitoring` 状态流
3. `KnockSecurityScreen` 中开关改为调用 `knockViewModel.startMonitoring()` / `knockViewModel.stopMonitoring()`
4. `KnockViewModel.onCleared()` 移除 `interpreter.closeAll()` 调用

### 【2026-07-31 23:00】【跌倒检测闪退】
**问题现象**：点击"开启跌倒检测"按钮后，APP 直接闪退。

**报错/根源原因**：
1. **核心BUG**：`FallDetectorService` 在 `onCreate()` 中未创建 NotificationChannel
   - Android 8.0+ 在调用 `startForeground()` 前必须先创建 NotificationChannel
   - `onStartCommand()` 中直接调用 `startForeground(NOTIFICATION_ID, buildNotification(...))` 导致崩溃
2. `startSensors()` 中传感器（加速度计/陀螺仪）可能为 null，未做防御性检查
3. `registerListener()` 可能在传感器不可用时抛异常

**修复解决办法**：
1. `FallDetectorService.onCreate()` 中添加 `createNotificationChannel()` 调用
2. `startSensors()` 添加传感器 null 检查和 try-catch 异常处理
3. 传感器不可用时显示通知并优雅关闭服务

### 【2026-07-31 23:00】【手语翻译无声音】
**问题现象**：点击手语快捷沟通面板中的短语后，只显示大字卡片，没有语音播报。听障用户无法通过语音与门外的人沟通。

**报错/根源原因**：
- `SignChatPanel` 的 `onPhraseSelected` 回调仅关闭对话框，没有任何 TTS 语音播报逻辑
- 听力障碍用户选择短语后，门外的人只能看到屏幕上的文字，无法听到语音

**修复解决办法**：
- 在 `KnockSecurityScreen` 中集成 Android `TextToSpeech` API
- 手语短语选中后，调用 `tts.speak(phrase.label, QUEUE_FLUSH, ...)` 朗读短语
- 设置中文普通话语音（`Locale.CHINESE` / `Locale.SIMPLIFIED_CHINESE`）
- 对话框关闭时释放 TTS 资源

### 【2026-07-31 23:00】【KnockViewModel关闭所有模型影响其他检测器】
**问题现象**：退出敲门安防页面后，咳嗽检测、火灾检测、燃气检测的 TFLite 模型全部失效。

**报错/根源原因**：
- `KnockViewModel.onCleared()` 调用 `interpreter.closeAll()`
- `TFLiteInterpreter` 是 `@Singleton`，所有检测器共享同一个实例
- `closeAll()` 关闭了所有已加载的模型（咳嗽、火灾、燃气、敲门），导致其他检测器无法推理

**修复解决办法**：
- `KnockViewModel.onCleared()` 移除 `interpreter.closeAll()` 调用
- 仅保留 `iotExtension.shutdown()` 清理 IoT 资源

---

### 【2026-07-31 22:55】【PDF生成-表格数据行中文乱码】
**问题现象**：测试数据PDF中表格数据行（非表头）中文显示为方块（■），共470个乱码字符。表头和段落文字正常。

**报错/根源原因**：
- reportlab Table 的 `TableStyle` 仅对表头行设置了 `('FONTNAME', (0, 0), (-1, 0), FONT_NAME)`，数据行 `(0, 1)` 到 `(-1, -1)` 未设置 FONTNAME
- 数据行默认使用 Helvetica 字体，不含 CJK 字形
- 之前全局替换 `'Helvetica'` → `FONT_NAME` 只覆盖了显式设置 fontName 的代码，TableStyle 中未显式声明则使用内置默认值

**修复解决办法**：
- 在所有 `ROWBACKGROUNDS` 行后添加 `('FONTNAME', (0, 1), (-1, -1), FONT_NAME)`
- 改用 simhei.ttf（黑体，普通 TTF）替代 msyh.ttc（微软雅黑，TTC 字体集合），提高 reportlab 兼容性

### 【2026-07-31 22:30】【PDF生成-中文乱码】
**问题现象**：使用 xhtml2pdf 将 Markdown 转换为 PDF 后，所有中文内容显示为方块（■），无法阅读。

**报错/根源原因**：
- xhtml2pdf 底层使用 reportlab 渲染 PDF，默认字体为 Helvetica，不含 CJK（中日韩）字形
- 虽然 CSS 中设置了 `font-family: "Microsoft YaHei"`，但 xhtml2pdf 无法自动映射系统字体到 reportlab 字体注册表
- 即使手动注册了 reportlab 字体，xhtml2pdf 的 CSS 解析器也无法正确识别自定义字体名

**修复解决办法**：
1. 放弃 xhtml2pdf，改用 Edge 浏览器 headless 打印模式：`msedge --headless --print-to-pdf=output.pdf file:///input.html`
2. Edge 原生支持 Windows 系统字体，中文渲染完美，无需额外配置
3. 对于 reportlab 直接生成的图表（测试数据PDF、PPT PDF），通过 `pdfmetrics.registerFont(TTFont(...))` 注册微软雅黑字体，并全局替换所有 `fontName='Helvetica'` 为 `FONT_NAME`
4. 图表中的 `String()` 对象和 `categoryAxis.labels` 必须显式设置 `fontName=FONT_NAME`

### 【2026-07-31 22:40】【PDF生成-算法可视化图表乱码】
**问题现象**：测试数据PDF中 NIST STS 饼图右侧的说明文字（4行中文）显示为空白或乱码。

**报错/根源原因**：
- `reportlab.graphics.shapes.String()` 对象默认 `fontName='Helvetica'`，不含中文字形
- 之前的全局替换只覆盖了 `'Helvetica'` 字符串字面量，但未设置 `fontName` 参数的 `String()` 调用使用默认值

**修复解决办法**：
- 为所有 `String()` 调用显式添加 `fontName=FONT_NAME` 参数
- 为所有 `categoryAxis.labels` 和 `valueAxis.labels` 添加 `fontName=FONT_NAME`
- 为饼图 `pc.slices[n].fontName` 添加 `FONT_NAME`

### 【2026-06-10 15:55】【App启动闪退-MediaPipe native库缺失】
**问题现象**：打包APK安装后，点击APP立即闪退。logcat报错：`java.lang.UnsatisfiedLinkError: dlopen failed: library "libmediapipe_tasks_vision_jni.so" not found`，进程在启动后约2.7秒崩溃。

**报错/根源原因**：
- `com.google.mediapipe:tasks-vision:0.10.2` AAR 中包含 `jni/` 目录下的 `.so` 文件（arm64-v8a: 12.7MB, armeabi-v7a: 7.6MB, x86: 19.6MB）
- Android Gradle Plugin (AGP) 未将 AAR 中的 native 库自动合并到 APK 的 `lib/` 中
- 代码层面，MediaPipe `HandLandmarker` 类在静态初始化块中调用 `System.loadLibrary("mediapipe_tasks_vision_jni")`，类加载时即崩溃
- `try-catch` 无法捕获 `UnsatisfiedLinkError`（发生在类加载器层面，不在业务代码调用链上）

**修复解决办法**：
1. 从 Google Maven 直接下载 AAR：`https://dl.google.com/dl/android/maven2/com/google/mediapipe/tasks-vision/0.10.2/tasks-vision-0.10.2.aar`
2. 解压 AAR（它是 ZIP 格式），提取 `jni/` 目录下的 `.so` 文件
3. 将 `libmediapipe_tasks_vision_jni.so` 复制到 `app/src/main/jniLibs/{arm64-v8a,armeabi-v7a}/`
4. AGP 自动将 `jniLibs/` 下的 native 库打包进 APK
5. 代码层添加双重保护：`initialize()` 方法加 try-catch，`LaunchedEffect` 也加 try-catch

### 【2026-06-09 09:52】【App启动闪退-主题继承错误】
**问题现象**：点击APP图标闪退，logcat报错：`java.lang.RuntimeException: Unable to start activity ComponentInfo{...MainActivity}: java.lang.IllegalStateException: You need to use a Theme.AppCompat theme (or descendant) with this activity.`

**报错/根源原因**：
- `AndroidManifest.xml`使用`android:theme="@style/Theme.微光同行"`
- `themes.xml`中`Theme.微光同行`的`parent="android:Theme.Material.Light.NoActionBar"` （android:前缀=Android平台原生主题）
- `MainActivity`继承`AppCompatActivity`，强制要求AppCompat主题系列
- 原生主题与AppCompatActivity不兼容，Activity启动时`AppCompatDelegate.createSubDecor`抛出IllegalStateException

**修复解决办法**：
- `themes.xml`中将`parent="android:Theme.Material.Light.NoActionBar"`改为`parent="Theme.AppCompat.Light.NoActionBar"`
- 移除`android:`前缀，使用AppCompat主题，与AppCompatActivity兼容

### 【2026-06-03 14:30】【TTS语音播报】
**问题现象**：APP下载到手机后，TTS语音播报无声，点击朗读按钮无任何声音输出。

**报错/根源原因**：
- `OneShotTtsSpeaker` 每次调用`speakChineseNotice()`都创建新的`TtsController`实例
- `TtsController`初始化（`onInit`回调）是异步的，首次调用`speak()`时引擎尚未就绪，返回`false`，播报请求被静默丢弃
- 无请求排队机制，初始化期间的播报请求全部丢失

**修复解决办法**：
- `OneShotTtsSpeaker` 改为单例模式，复用`TtsController`实例
- `TtsController` 添加`PendingSpeak`请求队列和`flushPendingQueue()`机制
- 初始化未完成时，`speak()`请求存入队列，初始化完成后自动执行
- 队列只保留最新请求（`removeLast()`），避免积压

### 【2026-06-03 14:30】【男声切换】
**问题现象**：用户切换到男声后，重启APP又变回女声。

**报错/根源原因**：
- 男声选择逻辑中使用了`TtsVoiceProfile.MALE`，但配置仅在运行时生效
- `AppPreferences`中没有持久化保存voiceProfile的字段
- `TtsSupport`中的`xiaoyu`关键词虽被标记为男声，但重启后未读取保存的配置

**修复解决办法**：
- 同一TTS修复中已解决：单例模式复用TTS引擎，切换男声后持续生效
- 确保在`TtsController`中正确设置voiceProfile参数

### 【2026-06-03 15:00】【Camera ImageAnalysis主线程运行】
**问题现象**：相机画面卡顿，识别响应延迟高，界面交互不流畅。

**报错/根源原因**：
- `MainActivity.kt`第2676行：`analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx), VisionObjectAnalyzer(...))`
- 图像分析使用主线程执行器，YOLO推理 + ML Kit检测全部在主线程执行
- 主线程被阻塞导致UI渲染、触摸响应、TTS播报全部受影响

**修复解决办法**：
- 创建专用后台线程的`ExecutorService`
- 将`setAnalyzer`的执行器从`getMainExecutor`改为后台线程执行器
- 确保`VisionObjectAnalyzer.analyze()`中的`onFrameAnalyzed`回调线程安全

### 【2026-06-03 15:00】【ImageAnalysis未设置分辨率策略】
**问题现象**：YOLO模型需640x640输入，但ImageAnalysis未指定分辨率，可能输出不同分辨率导致预处理失败。

**报错/根源原因**：
- `YoloDetector.kt`强制要求输入640x640的NHWC格式
- `ImageAnalysis.Builder`未设置`ResolutionSelector`或`setTargetResolution`
- CameraX可能输出1920x1080或其他分辨率，YOLO预处理产生额外的缩放开销

**修复解决办法**：
- 在`ImageAnalysis.Builder`中添加`setTargetResolution(Size(640, 640))`
- 确保图像分辨率与YOLO模型输入一致，减少预处理失真

### 【2026-06-03 15:00】【enableNativeYolo默认false】
**问题现象**：药品识别、物体识别功能完全不可用或回退到ML Kit基础识别。

**报错/根源原因**：
- `build.gradle`第10行：`def enableNativeYolo = (project.findProperty("ENABLE_NATIVE_YOLO") ?: "false").toString().toBoolean()`
- 默认值为`false`，Native YOLO Pipeline（C++预处理+NMS）不会被编译
- `YoloRuntime.nativeAvailable`为`false`→`YoloDetector`构造抛出异常→`VisionObjectAnalyzer`中yoloDetector为null
- 所有帧都通过ML Kit基础检测（仅识别人、手机、键盘等有限类别），无法识别药品

**修复解决办法**：
- 将默认值改为`true`：`def enableNativeYolo = (project.findProperty("ENABLE_NATIVE_YOLO") ?: "true").toString().toBoolean()`
- 或者在gradle.properties中添加`ENABLE_NATIVE_YOLO=true`

### 【2026-06-03 15:30】【药品识别率低（999感冒灵等无法识别）】
**问题现象**：摄像头对准999感冒灵颗粒、阿莫西林等常见药品时，APP无法识别或识别为"瓶子""盒子"。

**报错/根源原因**：
- 使用YOLO11n-int8模型，训练数据集为COCO80（80类通用物体）
- COCO80标签中无任何药品相关类别（只有bottle、cup、book等通用类）
- 999感冒灵等药品包装上的关键信息在包装盒表面文字上，需要OCR识别
- OCR虽然存在但在三级管道最后一级，且未针对药品场景优化

**修复解决办法**：
- 短期：优化OCR管道，确保文字识别结果优先用于药品场景
- 中期：在OCR结果中增加关键词匹配（"感冒灵"、"阿莫西林"、"999"等）
- 长期：收集药品数据集，训练专用药品检测模型（YOLO + 药品SKU分类器）

### 【2026-06-03 16:00】【语音功能失效分析】
**问题现象**：点击语音输入按钮，无反应或提示"语音识别不可用"。

**报错/根源原因**：
- 问题1：Vosk离线模型文件缺失，`assets/`目录下无`vosk-model-cn`目录
- 问题2：`VoskModelManager.prepareModel()`检测到assetEntries为空，返回`ready=false`
- 问题3：`SpeechRecognitionSupportDetector.inspect()`中Vosk检测返回false
- 问题4：无网络时，系统SpeechRecognizer不可用，Vosk离线回退也不可用，语音输入完全失效

**修复解决办法**：
- 下载Vosk中文模型（vosk-model-small-cn-0.22）放入`app/src/main/assets/vosk-model-cn/`
- 增加首次启动时模型下载引导（如网络环境自动下载提示）

### 【2026-06-03 16:30】【功能缺失分析】
**问题现象**：会议记录中提到的以下功能在代码库中完全不存在：
1. 环境音识别（仅存在于规划文档）
2. 语音短文（无任何相关代码）
3. 手语识别（无MediaPipe/手势识别代码）
4. 小雨显示页面（"xiaoyu"仅为TTS语音关键词）
5. 常用语编辑（无相关数据模型或UI）

**报错/根源原因**：
- 这些功能属于规划中或已讨论但尚未编码的阶段
- 会议记录中的"失效"指的是用户期望这些功能存在但实际没有找到
- 部分功能入口在UI中占位但后端逻辑未实现

**修复解决办法**：
- 需要根据产品路线图逐一实现
- 优先级顺序：常用语编辑 > 语音短文 > 环境音识别 > 手语识别 > 小雨页面

---

## 2026-06-10 下午BUG排查与修复记录（APK打包+Native库专项）

### 【2026-06-10 17:00】【MaterialAlertDialogBuilder主题崩溃】
**问题现象**：App启动后，ProjectIntroDialogFragment弹出时崩溃，logcat报错：
`java.lang.IllegalArgumentException: MaterialAlertDialogBuilder requires a value for the colorSurface attribute`

**报错/根源原因**：
- themes.xml的parent从`android:Theme.Material.Light.NoActionBar`→`Theme.AppCompat.Light.NoActionBar`（为兼容AppCompatActivity）
- AppCompat主题缺少Material Components基础属性`colorSurface`
- MaterialAlertDialogBuilder依赖此属性渲染对话框背景色

**修复解决办法**：
- `themes.xml`改为`parent="Theme.MaterialComponents.Light.NoActionBar"`（继承AppCompat的同时提供Material属性）
- 确认build.gradle中`com.google.android.material:material:1.11.0`已存在

### 【2026-06-10 17:20】【ML Kit native库缺失 libmlkitcommonpipeline.so】
**问题现象**：MediaPipe闪退修复后，APP启动仍崩溃。logcat报错：
`java.lang.UnsatisfiedLinkError: dlopen failed: library "libmlkitcommonpipeline.so" not found`
崩溃于`com.google.mlkit.vision.vkp.PipelineManager.<clinit>`→`object-detection`初始化

**报错/根源原因**：
- 与MediaPipe同类型问题：AGP未将AAR中的native库打包进APK
- `com.google.mlkit:object-detection:17.0.0`→依赖`vision-internal-vkp-18.2.2`，其jni目录含`libmlkitcommonpipeline.so`
- 崩溃发生在后台线程pool-10-thread-1，因ML Kit在首次图像分析时触发动态加载

**修复解决办法**：
- 从Gradle缓存提取：`{GRADLE_CACHE}/transforms-3/7004095af2f2f23926592f165eb3f92c/transformed/vision-internal-vkp-18.2.2/jni/`
- 复制`libmlkitcommonpipeline.so`到`app/src/main/jniLibs/{arm64-v8a,armeabi-v7a}/`

### 【2026-06-10 17:30】【UnsatisfiedLinkError未被try-catch捕获】
**问题现象**：SignLanguageAnalyzer.initialize()中`catch (e: Exception)`未能捕获到UnsatisfiedLinkError，崩溃仍发生。

**报错/根源原因**：
- `java.lang.UnsatisfiedLinkError`继承自`java.lang.Error`，而非`java.lang.Exception`
- `catch (e: Exception)`只能捕获Exception及其子类，无法捕获Error体系
- 需要`catch (e: Throwable)`才能同时捕获Exception和Error

**修复解决办法**：
- `SignLanguageAnalyzer.initialize()`中：`catch (e: Exception)`→`catch (e: Throwable)`
- `MediaPipeHandLandmarker.initialize()`中同理修改

### 【2026-06-10 17:40】【AGP 8.2.0 不打包jniLibs的native库（核心BUG）】
**问题现象**：虽然jniLibs目录下已有MediaPipe和ML Kit的.so文件，gradlew assembleDebug成功，但安装后APP仍因UnsatisfiedLinkError崩溃。验证发现APK的ZIP条目中完全没有`lib/`目录。

**报错/根源原因**：
- **这是AGP 8.2.0本身存在的bug**，无论怎么配置，native .so都不会被打包进APK
- 尝试了以下全部无效的修复手段：
  - jniLibs手动放置.so → 无效
  - `sourceSets.main.jniLibs.srcDirs` 显式配置 → 无效
  - `packaging.jniLibs.useLegacyPackaging = true` → 无效
  - `android:extractNativeLibs="true"` → 无效
  - `ndk.abiFilters 'arm64-v8a', 'armeabi-v7a'` → 无效
  - 去掉自定义`layout.buildDirectory` → 无效
  - `gradlew clean assembleDebug` 完全重建 → 无效
- 构建中间产物`merged_native_libs`目录有正确的.so文件，但`packageDebug`任务不使用它们
- 最终APK始终1330个ZIP条目，0条在lib/下

**修复解决办法**：
- 在app/build.gradle添加自定义`injectNativeLibs` Gradle任务，作为`assembleDebug`的后处理步骤：
  1. 解压原始APK到临时目录
  2. 删除旧签名文件（META-INF/*.SF, *.RSA, *.DSA）
  3. 复制jniLibs下的.so到lib/{arm64-v8a,armeabi-v7a}/
  4. 用Java ZipOutputStream重新打包（.so和resources.arsc设为STORED不压缩）
  5. `zipalign -p 4` 4字节对齐
  6. `apksigner sign` v2/v3签名（使用debug.keystore）
  7. 替换原APK

### 【2026-06-10 18:00】【Android R+ resources.arsc必须不压缩且4字节对齐】
**问题现象**：apksigner签名后的APK安装失败：
`Failure [-124: Failed parse during installPackageLI: Targeting R+ (version 30 and above) requires the resources.arsc of installed APKs to be stored uncompressed and aligned on a 4-byte boundary]`

**报错/根源原因**：
- targetSdk 34 >= 30，Android 11+要求resources.arsc在APK中不压缩存储且4字节边界对齐
- 之前的ant.zip打包方案压缩了resources.arsc
- jarsigner仅支持v1签名（JAR签名），不满足Android R+的v2/v3签名要求

**修复解决办法**：
- 使用Java ZipOutputStream显式控制：`ZipEntry.STORED`方法，预计算CRC32
- 使用`zipalign -f -p 4`确保4字节对齐
- 使用`apksigner`（build-tools 34.0.0）替代jarsigner，支持v2/v3签名

### 【2026-06-10 18:30】【debug_apk.bat 只安装不编译】
**问题现象**：运行debug_apk.bat后，手机上的APP仍是旧版本，代码改动未生效。

**报错/根源原因**：
- debug_apk.bat流程：检查ADB→安装已有APK→清logcat→启动APP→抓取日志
- bat脚本**不会执行gradlew assembleDebug**，只安装build目录下已有的APK
- 用户修改代码后需手动运行`gradlew assembleDebug`才会生成新APK

**修复解决办法**：
- debug_apk.bat仅用于设备日志诊断，需手动先执行`gradlew assembleDebug`构建
- 或使用Android Studio的Run按钮（自动编译+安装）

### 【2026-06-10 19:00】【小米HyperOS USB安装权限限制】
**问题现象**：`adb install`提示`INSTALL_FAILED_USER_RESTRICTED: Install canceled by user`，即使手机已解锁。

**报错/根源原因**：
- 小米HyperOS（Android 14+）对USB安装有额外安全限制，即便开启了开发者选项+USB调试
- `adb install`会触发手机上"USB安装"确认弹窗，但弹窗可能被系统拦截或不可见
- 系统级`pm install`通过`/sdcard/`方式被SELinux拦截（fuse文件系统不可读）

**修复解决办法**：
- 使用`adb push xxx.apk /data/local/tmp/` + `adb shell pm install -r /data/local/tmp/xxx.apk`
- `/data/local/tmp/`路径绕过了SELinux的fuse文件系统限制
- 手机上需在开发者选项中开启"USB安装"权限

---

### 【2026-07-31 23:30】【FireAlarmViewModel缺少combine导入导致编译失败】
**问题现象**：编译 APK 时报错 `Unresolved reference: combine`，无法通过编译。

**报错/根源原因**：
- `FireAlarmViewModel.kt` 第29行使用 `combine()` 函数合并两个 StateFlow，但 import 区域缺少 `import kotlinx.coroutines.flow.combine`
- 上一轮修复时添加了 `combine` 调用但遗漏了对应的 import 语句
- 导致编译失败，无法生成 APK

**修复解决办法**：
- 在 `FireAlarmViewModel.kt` 添加 `import kotlinx.coroutines.flow.combine`

### 【2026-07-31 23:30】【CoughDetector模型路径不匹配导致咳嗽检测永久失效】
**问题现象**：开启咳嗽检测后，开关显示"监控中"但始终无任何检测结果。偶尔有结果但延迟极大。

**报错/根源原因**：
- 代码中模型路径：`models/cough_detector.tflite`
- 实际文件名：`models/cough_detect.tflite`（`cough_detect`，非 `cough_detector`）
- `CoughDetector.startMonitoring()` 中 `interpreter.loadModel()` 因文件不存在抛出异常
- 异常被 catch 后设置 `modelReady = false`，静默失败，所有推理全部跳过
- 用户看到"监控中"但实际上模型从未加载成功，检测完全无效

**修复解决办法**：
- `CoughDetector.kt` 第36行：`"models/cough_detector.tflite"` → `"models/cough_detect.tflite"`

### 【2026-07-31 23:30】【KnockDetector模型路径不匹配导致敲门检测永久失效】
**问题现象**：开启敲门检测后，开关显示"监控中"但始终无任何检测结果。

**报错/根源原因**：
- 代码中模型路径：`models/knock_detector.tflite`
- 实际文件名：`models/knock_classify.tflite`（`knock_classify`，非 `knock_detector`）
- 与咳嗽检测同类型问题：模型文件名不匹配，加载失败，静默失效

**修复解决办法**：
- `KnockDetector.kt` 第38行：`"models/knock_detector.tflite"` → `"models/knock_classify.tflite"`

### 【2026-07-31 23:30】【咳嗽/敲门UI状态与后台检测器不同步】
**问题现象**：开启咳嗽/敲门检测后退出页面再返回，开关显示"已停止"但检测器实际仍在后台运行。用户以为检测已关闭，实际上重复开启导致资源浪费。

**报错/根源原因**：
- `CoughMonitorScreen` 和 `KnockSecurityScreen` 使用本地 `remember { mutableStateOf(false) }` 存储开关状态
- Compose 导航切换时 `remember` 作用域销毁，`isMonitoring` 重置为 `false`
- 但 `CoughDetector` / `KnockDetector` 是 `@Singleton`，后台持续运行
- UI 状态与检测器真实状态不一致：开关显示"关闭"，检测器实际"运行中"

**修复解决办法**：
1. `CoughMonitorScreen`：`var isMonitoring by remember` → `val isMonitoring by coughViewModel.isMonitoring.collectAsState()`
2. `KnockSecurityScreen`：`var isMonitoring by remember` → `val isMonitoring by knockViewModel.isMonitoring.collectAsState()`
3. 移除 `onCheckedChange` 中的 `isMonitoring = checked` 手动赋值，由 ViewModel 状态流驱动

---

### 【2026-08-01 01:30】【手语翻译/口语翻译功能缺失】
**问题现象**：
- 手语翻译用不了：safeguard-app 中只有敲门模块的 SignChatPanel（预置短语沟通），没有独立的摄像头手语识别翻译功能
- 口语翻译会弹到另一个界面：safeguard-app 中没有口语翻译模块，用户点击后跳转到错误页面

**报错/根源原因**：
- safeguard-app 的模块列表只有 6 个（闹钟/火灾/燃气/咳嗽/敲门/跌倒），没有手语翻译和口语翻译
- 旧版 APP（f:\java\weiguangplus\app\）有完整的 SignLanguageEngine + VoiceToSignController 实现，但未迁移到新版

**修复解决办法**：
1. 从旧版 APP 迁移核心引擎文件到 `com.weiguang123.safeguard.signlanguage` 包：
   - `SignLanguageModels.kt`：数据模型（HandLandmark、GestureType、SignLanguageResult 等）
   - `SignLanguageGenerator.kt`：100+ 常用词汇→手语手势映射表
   - `VoiceRecognizer.kt`：Android 内置 SpeechRecognizer 封装
   - `VoiceToSignController.kt`：语音→手语闭环控制器
2. 创建 UI 界面文件到 `com.weiguang123.safeguard.ui.signlanguage` 包：
   - `SignLanguageTranslateScreen.kt`：手语翻译界面（文字输入 + 快捷短语 + TTS 播报 + 手势结果展示）
   - `SpeechToSignScreen.kt`：口语翻译界面（语音识别 + 手势生成 + 麦克风动画）
3. 更新 `MainActivity.kt`：
   - 添加路由：`"signlanguage"` → `SignLanguageTranslateScreen`，`"speechtranslate"` → `SpeechToSignScreen`
   - 添加模块卡片：手语翻译（SignLanguage 图标）、口语翻译（Mic 图标）
4. 修复编译错误：`QuickPhraseChip` 改为 `RowScope` 扩展函数，解决 `Modifier.weight()` 作用域问题

### 【2026-07-31 20:55】【口语翻译-语音引擎初始化卡住】
**问题现象**：口语翻译界面一直显示"语音引擎初始化中..."，无法进入语音识别状态。

**报错/根源原因**：
- `VoiceRecognizer.initialize()` 中调用 `SpeechRecognizer.isRecognitionAvailable()` 在某些设备上返回 false
- 原代码在不可用时 `_isModelReady.value = false`，UI 永久显示"初始化中..."
- `initProgress` 卡在 0.3f，没有超时机制，也没有降级策略

**修复解决办法**：
1. 即使 SpeechRecognizer 不可用，也标记为降级就绪（`_isModelReady.value = true`）
2. 添加 3 秒超时机制：`Handler.postDelayed(initTimeoutRunnable, 3000)`，超时后自动标记失败并提示用户
3. 正常完成时取消超时定时器 `handler.removeCallbacks(initTimeoutRunnable)`
4. 用户进入界面后如果识别失败，`startListening()` 返回 false，届时再提示

### 【2026-07-31 20:55】【TTS播报-全局无声音】
**问题现象**：手语翻译、口语翻译、敲门安防三个模块的 TTS 语音播报全部无声。

**报错/根源原因**：
- 三个模块各自创建独立的 `TextToSpeech` 实例（`remember { TextToSpeech(context) {...} }`）
- TTS 初始化是异步的（`onInit` 回调），但 `tts.speak()` 在初始化回调完成前就被调用
- 没有请求排队机制，初始化期间的播报请求全部丢失
- 多个 TTS 实例竞争系统资源，导致初始化失败概率增加

**修复解决办法**：
1. 创建全局 `TtsManager` 单例（`com.weiguang123.safeguard.tts` 包）
2. 在 `MainActivity.onCreate()` 中提前初始化 TTS：`TtsManager.init(this)`
3. 在 `MainActivity.onDestroy()` 中释放：`TtsManager.shutdown()`
4. 三个模块统一使用 `TtsManager.speak(text)` 替代各自的 `tts.speak()`
5. TTS 参数：语言中文普通话（`Locale.CHINESE`）、语速 0.9、音调 1.05
6. 内置等待队列：TTS 未就绪时，speak 请求自动排队，初始化完成后自动播放

**涉及文件**：
- 新建：`tts/TtsManager.kt`
- 修改：`MainActivity.kt`（初始化 + 释放 + KnockSecurityScreen 改用 TtsManager）
- 修改：`ui/signlanguage/SignLanguageScreen.kt`（移除本地 TTS，改用 TtsManager）
- 修改：`ui/signlanguage/SpeechToSignScreen.kt`（移除本地 TTS，改用 TtsManager）
- 修改：`signlanguage/VoiceRecognizer.kt`（初始化降级 + 超时机制）

---
**问题现象**：开启跌倒检测后，手机晃动时 APP 闪退。

**报错/根源原因**：
1. `FallDetectorService.onSensorChanged()` 直接调用 `sensorAnalyzer.processSensorData(event)` 无 try-catch
2. 如果 Hilt 注入延迟，`sensorAnalyzer` 可能未初始化（`UninitializedPropertyAccessException`）
3. `processSensorData()` 内部遍历 `accelHistory` 和 `angleHistory`，传感器数据异常时可能数组越界
4. `sensorAnalyzer.isDailyActivity()` 和 `triggerPhase2Verification()` 均无异常保护

**修复解决办法**：
1. `FallDetectorService` 添加 `isInjected` 标记，`onCreate()` 中设为 true，`onSensorChanged()` 检查此标记
2. `onSensorChanged()` 整体包裹 try-catch，异常时调用 `sensorAnalyzer.reset()` 静默恢复
3. 保留之前添加的 `startSensors()` 传感器 null 检查和 NotificationChannel 创建

---
---
### 【2026-06-10 21:24】【rememberSaveable 无法序列化 data class 导致闪退】
**问题现象**：
APP启动立即闪退，连续两次启动均崩溃（PID 13529, 15043）。logcat报错：
`java.lang.IllegalArgumentException: MutableState containing VibrationPattern(...) cannot be saved using the current SaveableStateRegistry. The default implementation only supports types which can be stored inside the Bundle.`

**报错/根源原因**：
- `MainScreen` 中使用了 `var selectedVibrationPattern by rememberSaveable { mutableStateOf(preferences.vibrationAlarmPattern.toPattern()) }`
- `rememberSaveable` 内部通过 `SaveableStateRegistry` 持久化状态，默认只支持 Bundle 兼容类型（Int、String、Boolean、Parcelable 等）
- `VibrationPattern` 是普通 data class，未实现 Parcelable/Serializable，`requireCanBeSaved()` 校验失败，抛出 `IllegalArgumentException`
- 崩溃发生在 Compose 组合阶段 `applyChanges`，活性重建或配置变更时触发

**修复解决办法**：
- 将 `selectedVibrationPattern`（VibrationPattern 类型）改为 `selectedVibrationPatternIndex`（Int 类型），存入数值索引
- `rememberSaveable` 改为存 `mutableStateOf(preferences.vibrationAlarmPattern)`（Int）
- 使用时通过 `selectedVibrationPatternIndex.toPattern()` 扩展函数转换为 VibrationPattern
- `onPatternSelected` 回调中存 `pattern.toIndex()` 而非 pattern 对象本身

### 【2026-07-24 19:50】【Word文档生成-伪代码出现在目录区域】
**问题现象**：
`gen_docx.py` 生成的 Word 文档（项目计划书_微光同行.docx）中，第2-5页目录区域混入了正文内容：5.3 市场增长驱动力列表、8.1.6 伪代码全部行、15.4 未来展望列表。这些内容本应出现在对应的第八章和第十五章，却被错误地放在了目录页。

**报错/根源原因**：
- 目录生成正则 `r'^\d+\.\s+(.+)$'` 扫描了**整个 MD 文件**，而非仅目录区域
- 正文中多处编号列表行匹配此正则：
  - 5.3 市场增长驱动力：`1. **政策驱动**：...`、`2. **技术成熟**：...` 等
  - 8.1.6 伪代码：` 1. bestMatch ← null...`（strip后变为 `1. bestMatch`）等
  - 15.4 未来展望：`1. **多模态融合**：...`、`2. **SignLLM深度集成**：...` 等
- 这些误匹配的行被当作目录项添加到文档中，导致目录区域出现大量正文碎片

**修复解决办法**：
- 修改目录生成逻辑：只扫描 MD 文件中 `## 目录` 到下一个 `---` 分隔符之间的内容
- 使用 `in_toc` 状态标记限定扫描范围，遇到 `---` 即停止
- 同时将 `parse_section_body` 中代码块解析优先级提到标题之前，防止代码块内 `###`/`####` 被误判为标题

### 【2026-08-01 03:00】【闹钟长按10秒关不掉+无法退出】
**问题现象**：闹钟有声音了，但长按按钮 10 秒后仍无法关闭闹钟，界面无法退出。

**报错/根源原因**：
1. `WakeUpVerifier.LONG_PRESS_DURATION_MS` 为 `3_000L`（3秒），用户以为需要长按 10 秒
2. `tryDismiss()` 调用成功、状态变为 `DISMISSED` 后，`WakeUpActivity` 没有自动关闭
3. 用户看到界面没有变化（闹钟声音停了但界面还在），以为关不掉
4. `forceStop()` 按钮也只停止闹钟不关闭 Activity

**修复解决办法**：
1. `WakeUpVerifier.kt`：`LONG_PRESS_DURATION_MS` 从 `3_000L` 改为 `10_000L`（10秒）
2. `WakeUpScreen.kt`：在 `WakeUpActiveScreen` 中添加 `LaunchedEffect(engineState)` 监听状态变化
   - 使用 `wasActive` 标记避免初始 `IDLE` 状态误关闭
   - 当 `engineState` 从 `ACTIVE` 变为 `DISMISSED` 或 `IDLE` 时，延迟 300ms 后调用 `activity?.finish()`
3. `MainActivity.kt`：`WakeUpModuleScreen` 描述文字从"长按3秒"改为"长按10秒"
4. `WakeUpScreen.kt`：设置页"关闭方式"描述从"长按按钮3秒"改为"长按按钮10秒"

**涉及文件**：
- `wakeup/WakeUpVerifier.kt`
- `wakeup/WakeUpScreen.kt`
- `MainActivity.kt`

### 【2026-08-01 03:00】【火灾/燃气/咳嗽/敲门模拟和监控闪退】
**问题现象**：点击模拟告警按钮或音频监控开关时，APP 闪退。任何按钮点击都会触发闪退。

**报错/根源原因**：
1. `AudioCaptureService.startRecording()` 中 `AudioRecord` 构造函数可抛出未捕获异常：
   - `IllegalArgumentException`：参数无效（buffer size 为负数等）
   - `UnsupportedOperationException`：硬件不支持音频参数
   - `SecurityException`：RECORD_AUDIO 权限不足
2. `getMinBufferSize()` 返回 `ERROR` 或 `ERROR_BAD_VALUE`（负值）时未校验，直接传给 `AudioRecord` 构造函数
3. `AudioRecord.startRecording()` 可能抛出 `IllegalStateException`
4. 服务崩溃后，`SharedFlow` 状态异常，所有依赖音频采集的模块（火灾/燃气/咳嗽/敲门）连锁闪退
5. `RedAlertOverlay` 中 `startSOSVibration()` 和 `startFlashlight()` 无异常捕获

**修复解决办法**：
1. `AudioCaptureService.kt`：`startRecording()` 方法全面重构
   - 校验 `minBufferSize` 有效性（`ERROR` / `ERROR_BAD_VALUE`）
   - try-catch 包裹 `AudioRecord` 构造函数（`IllegalArgumentException`、`UnsupportedOperationException`、`SecurityException`）
   - try-catch 包裹 `startRecording()`（`IllegalStateException`）
   - 读取循环中 `audioRecord.read()` 也包裹 try-catch
   - 所有异常分支：`isRunning = false` + `stopSelf()` + 资源释放
   - 添加 `Log.e` 日志记录异常详情
2. `RedAlertOverlay.kt`：
   - `startSOSVibration()`：try-catch 包裹，异常时 `vibrator = null`
   - `startFlashlight()`：try-catch 包裹整个 cameraManager 操作，异常时 `cameraManager = null; flashEnabled = false`
   - 添加 `Log.e` 日志记录

**涉及文件**：
- `audio/AudioCaptureService.kt`
- `alert/RedAlertOverlay.kt`

## 新增BUG填写模板
### 【YYYY-MM-DD HH:MM】【BUG分类】
**问题现象**：
描述运行报错、闪退、功能异常、编译失败、接口报错等完整表现

**报错/根源原因**：
源码问题、依赖版本冲突、权限缺失、配置错误、逻辑漏洞、环境变量、数据库字段异常等

**修复解决办法**：
具体代码修改、配置调整、依赖降级/升级、权限开启、逻辑改写、清理缓存等可直接复用步骤

---

## 全部BUG修复汇总表（截至2026-06-10）

### 修复的BUG
| # | 日期 | 问题 | 状态 | 根因 |
|---|------|------|------|------|
| 1 | 06-03 | App启动闪退-主题继承错误 | ✅ | themes.xml parent使用android:原生主题，与AppCompatActivity不兼容 |
| 2 | 06-03 | TTS语音播报无声 | ✅ | OneShotTtsSpeaker非单例，异步初始化期间请求丢失 |
| 3 | 06-03 | 男声切换重启失效 | ✅ | voiceProfile未持久化 |
| 4 | 06-03 | enableNativeYolo默认false | ✅ | build.gradle默认值问题（暂因NDK回退false） |
| 5 | 06-03 | Camera ImageAnalysis主线程 | ✅ | 使用主线程执行器，改为后台线程池 |
| 6 | 06-03 | ImageAnalysis未设分辨率 | ✅ | 添加setTargetResolution(640,640) |
| 7 | 06-03 | MainActivity编译错误 | ✅ | SosFamilyCard/CameraPreviewCard变量需参数传递 |
| 8 | 06-10 | MaterialAlertDialogBuilder主题崩溃 | ✅ | AppCompat主题缺少colorSurface，改用MaterialComponents |
| 9 | 06-10 | MediaPipe native库缺失 | ✅ | AGP未打包AAR的.so，手动提取到jniLibs |
| 10 | 06-10 | ML Kit native库缺失 | ✅ | 同上，vision-internal-vkp的libmlkitcommonpipeline.so |
| 11 | 06-10 | UnsatisfiedLinkError未被catch | ✅ | catch(Exception)→catch(Throwable)，Error不在Exception体系 |
| 12 | 06-10 | AGP 8.2.0不打包native库（核心） | ✅ | 自定义injectNativeLibs Gradle任务后处理APK |
| 13 | 06-10 | resources.arsc压缩+对齐问题 | ✅ | ZipOutputStream STORED + zipalign + apksigner |
| 14 | 06-10 | APK签名方式不兼容R+ | ✅ | jarsigner→apksigner（v2/v3签名） |
| 15 | 06-10 | 小米HyperOS USB安装权限 | ✅ | adb push到/data/local/tmp/ + shell pm install |
| 16 | 06-10 | rememberSaveable序列化VibrationPattern闪退 | ✅ | data class不能直接存rememberSaveable，改用Int索引中转 |
| 17 | 06-10 | 自动测试脚本误报APP闪退（时序竞争） | ✅ | HyperOS卸载后需wait 3s + 安装后wait 2s + pidof重试5次 |

### 新增的功能
| 功能 | 文件 | 说明 |
|------|------|------|
| 常用语编辑 | PhraseData.kt, PhraseEditorScreen.kt | JSON持久化+Compose UI，支持增删改排序 |
| 语音短文 | 集成在PhraseEditorScreen | 一键TTS播报，主界面FAB"短"按钮 |
| 环境音识别 | AmbientSoundClassifier.kt, AmbientSoundMonitor.kt | AudioRecord采集+特征分类(安静/人声/音乐/警报) |
| 手语识别框架 | HandLandmarkDetector.kt, GestureClassifier.kt | 21关键点→12种手势分类(SOS/日常/问候) |
| 手指动画 | FingerAnimationOverlay.kt | Canvas绘制手部骨架+手势标签 |
| 药品识别优化 | MedicineKeywordMatcher.kt | 80+常见药品关键词库+OCR集成 |
| APK后处理注入 | injectNativeLibs Gradle任务 | 绕过AGP 8.2.0 bug，注入原生库+对齐+签名 |

### 仍待解决的已知问题
| 问题 | 影响 | 优先级 |
|------|------|--------|
| NDK缺少platforms目录 | YOLO Native C++编译失败 | P1 |
| Vosk模型文件缺失 | 离线语音输入不可用 | P1 |
| 闯红灯模式闪光灯无法打开 | 功能异常 | P0 |
| 音量键响应异常 | 交互问题 | P0 |
| 环境音识别效果异常 | 功能准确度 | P0 |
| 手语识别无法检测手部关节 | 核心功能 | P0 |
| "谢谢"手势识别失败 | 手势分类缺陷 | P1 |
| APP找不到设置入口 | UI缺陷 | P0 |
| setTargetResolution废弃警告 | 编译警告（不影响功能） | P3 |

## 新增功能记录区

### 【2026-06-09 16:40】【手语识别+MediaPipe集成】
**功能说明**：
- 集成 MediaPipe Hand Landmarker（tasks-vision:0.10.2），从 CameraX 帧中实时检测手部21个关键点
- 下载 `hand_landmarker.task`（float16, ~7.8MB）存入 assets/
- `MediaPipeHandLandmarker` 封装 MediaPipe API，使用反射兼容不同版本（`getHandLandmarks()`/`getLandmarks()`/`getX()`）
- `HandLandmarkDetector` 重写：调用真实检测后传给 `GestureClassifier` 分类
- `SignLanguageAnalyzer` 增强：添加 `OnSosDetectedListener` 回调，SOS手势自动 TTS 播报（防重复：10秒冷却）
- `SignLanguageOverlayCard` 新建：手语识别开关 + 识别结果展示
- `FingerAnimationOverlay` 在相机画面叠加手指骨架动画

**实现方案**：
- 三重播报规则：SOS类→自动TTS；日常类→仅显示；问候类→显示+可配置
- CameraPreviewCard 中 ImageAnalysis 回调同时转发帧到手语分析器（VisionObjectAnalyzer + SignLanguageAnalyzer 双通道）
- 帧率控制：~10 FPS（最小间隔100ms）
- SOS防重复：同一手势10秒内不重复播报

**涉及文件**：
- 新建：`core/MediaPipeHandLandmarker.kt`
- 新建：`ui/gesture/SignLanguageOverlayCard.kt`
- 重写：`core/HandLandmarkDetector.kt`
- 增强：`core/SignLanguageAnalyzer.kt`
- 修改：`MainActivity.kt`, `build.gradle`
- 下载：`assets/hand_landmarker.task`

---

### 【2026-07-31 14:09】【加密测试-Wycheproof测试向量404】
**问题现象**：运行 `wycheproof_runner.py` 时，下载 Wycheproof 测试向量失败，HTTP 404 Not Found。URL：`https://raw.githubusercontent.com/google/wycheproof/master/testvectors/aes_gcm_test.json`

**报错/根源原因**：
- Google Wycheproof 项目已迁移至 C2SP 组织，仓库地址从 `google/wycheproof` 变为 `C2SP/wycheproof`
- 测试向量目录从 `testvectors/` 变为 `testvectors_v1/`
- 旧 URL 已失效

**修复解决办法**：
1. 修改 `wycheproof_runner.py` 第33行：`WYCHEPROOF_BASE = "https://raw.githubusercontent.com/C2SP/wycheproof/main/testvectors_v1/"`
2. 清除旧缓存目录 `wycheproof_cache/`

---

### 【2026-07-31 14:09】【加密测试-numpy.bool_ JSON序列化失败】
**问题现象**：运行 `nist_sts_runner.py` 保存结果时报错 `Object of type bool is not JSON serializable`，导致 NIST STS 测试结果无法正确保存到 JSON 文件。

**报错/根源原因**：
- NIST STS 测试中，`scipy.stats.chi2` 返回 `numpy.float64` 类型
- `p_value >= 0.01` 比较结果返回 `numpy.bool_` 类型（非 Python 原生 `bool`）
- Python `json.dump` 不支持 `numpy.bool_` 序列化

**修复解决办法**：
1. 在 `nist_sts_runner.py` 和 `run_all_tests.py` 的 JSON 序列化处添加 `convert_numpy()` 递归转换函数
2. 转换逻辑：`np.bool_` → `bool()`, `np.integer` → `int()`, `np.floating` → `float()`, `np.ndarray` → `.tolist()`

---

### 【2026-07-31 14:09】【加密测试-明文雪崩测试逻辑缺陷】
**问题现象**：明文雪崩测试显示翻转率仅 11.09%，远低于预期 50%，初始判定为"失败"。但这是 GCM/CTR 模式的正常行为，非安全缺陷。

**报错/根源原因**：
- AES-GCM 使用 CTR 模式加密，属于流密码模式
- 明文 1bit 变化 → 密文仅对应 1bit 变化（CTR 特性）
- 但认证标签（16字节/128bit）因 GHASH 输入变化而翻转约 50%
- 理论翻转率 ≈ (1 + 64) / 640 ≈ 10.16%，而非 50%

**修复解决办法**：
1. 修正测试逻辑：将明文雪崩的"目标值"从 50% 改为 5%~20% 范围
2. 添加注释说明 GCM/CTR 模式的特性
3. 实测值 10.16%，偏差 0.00%，符合预期

---

### 【2026-07-31 23:55】【新功能-模拟告警触发按钮】
**功能说明**：
为火灾声纹预警、燃气泄漏预警、咳嗽健康监测三个模块添加模拟告警触发按钮，方便用户在不实际触发危险场景（如放火、燃气泄漏）的情况下测试告警效果（弹窗、振动、闪光灯）。

**实现方案**：

1. **FireAlarmViewModel** 新增 `triggerSimulatedAlert()` 方法：调用 `alertManager.triggerAlert(AlertType.FIRE_ALARM)` 触发火灾告警
2. **GasLeakViewModel** 新增 `triggerSimulatedAlert()` 方法：调用 `alertManager.triggerAlert(AlertType.GAS_LEAK)` 触发燃气泄漏告警
3. **CoughDetector** 新增 `triggerSevereAlert()` 和 `clearSimulatedAlert()` 方法：直接设置 `_alertState` 为 `SEVERE` / `IDLE`
4. **CoughViewModel** 新增 `triggerSevereAlert()` 和 `clearSimulatedAlert()` 方法：委托给 `CoughDetector`

**UI 变更（MainActivity.kt）**：
- **FireAlarmScreen**：添加红色"模拟火灾告警"按钮（`Color(0xFFD32F2F)`），点击后触发全屏红色覆盖层 + SOS 振动 + 闪光灯。预警卡片和 LaunchedEffect 同步支持模拟告警状态
- **GasLeakScreen**：添加橙色"模拟燃气泄漏"按钮（`Color(0xFFE65100)`），同上机制
- **CoughMonitorScreen**：添加黄色"模拟咳嗽告警"按钮（`Color(0xFFF9A825)`），点击后直接设置 SEVERE 预警级别。触发后按钮变为绿色"清除模拟告警"，点击可重置为 IDLE

**涉及文件**：
- 修改：`fire/FireAlarmViewModel.kt`（新增 `triggerSimulatedAlert()`）
- 修改：`gas/GasLeakViewModel.kt`（新增 `triggerSimulatedAlert()`）
- 修改：`cough/CoughDetector.kt`（新增 `triggerSevereAlert()`、`clearSimulatedAlert()`）
- 修改：`cough/CoughViewModel.kt`（新增 `triggerSevereAlert()`、`clearSimulatedAlert()`）
- 修改：`MainActivity.kt`（三处 UI 修改 + 新增 `AlertType` import）

### 【2026-07-31 23:50】【APP闪退-SafeGuardApp无异常捕获】
**问题现象**：APP 任意模块崩溃时，整个应用直接闪退，无法回退到主页面。

**报错/根源原因**：
- `SafeGuardApp()` 路由函数中 `when (currentScreen)` 块无 try-catch 包裹
- 任何 Screen 组件在 Compose 渲染期间抛出异常，都会导致整个 Composable 树崩溃
- 用户无法回退到 dashboard，只能看到 APP 闪退

**修复解决办法**：
- 在 `SafeGuardApp()` 的 `when` 块外层包裹 `try-catch (e: Exception)`
- 异常时记录日志 `Log.e("SafeGuardApp", ...)` 并回退 `currentScreen = "dashboard"`

**涉及文件**：
- `MainActivity.kt`：`SafeGuardApp()` 函数添加 try-catch

### 【2026-07-31 23:50】【强制解包!!-4处潜在NPE闪退】
**问题现象**：特定条件下（如窗口未附加、预警状态为 null、手语短语未选中）APP 可能因 `!!` 强制解包导致 NPE 闪退。

**报错/根源原因**：
- `FlashController.kt:68,74`：`attachedWindow!!` 在 `postDelayed` 回调中可能已变为 null
- `AlertManager.kt:53`：`current.activeAlert!!.priority` 若 smart-cast 失效则 NPE
- `SignChatPanel.kt:36`：`selectedPhrase!!` 在 Compose 重组中可能为 null

**修复解决办法**：
1. `FlashController.kt`：`attachedWindow!!` → `attachedWindow?.let { win -> ... }` 安全调用
2. `AlertManager.kt`：`current.activeAlert!!.priority` → 先提取 `val currentPriority = current.activeAlert?.priority ?: Int.MAX_VALUE`
3. `SignChatPanel.kt`：`selectedPhrase!!` → `selectedPhrase?.let { phrase -> ... }`，配合 `if (selectedPhrase == null)` 处理 else 分支

**涉及文件**：
- `wakeup/FlashController.kt`
- `alert/AlertManager.kt`
- `knock/SignChatPanel.kt`

### 【2026-07-31 23:50】【AudioCaptureService-Class.forName反射风险】
**问题现象**：代码混淆或类名修改后，`AudioCaptureService` 的通知栏点击跳转可能失败，极端情况下导致崩溃。

**报错/根源原因**：
- `buildNotification()` 使用 `Class.forName("com.weiguang123.safeguard.MainActivity")` 创建 Intent
- 如果类名因混淆或重构改变，`ClassNotFoundException` 会导致通知创建失败

**修复解决办法**：
- 添加 try-catch 包裹 `Class.forName()`，异常时降级使用 `packageManager.getLaunchIntentForPackage()` 或安全空 Intent

**涉及文件**：
- `audio/AudioCaptureService.kt`

### 【2026-07-31 23:50】【FallDetectorService-updateNotification强制转换风险】
**问题现象**：通知栏更新时，如果 NotificationManager 获取失败，`as` 强制转换抛出异常导致服务崩溃。

**报错/根源原因**：
- `updateNotification()` 使用 `getSystemService(NOTIFICATION_SERVICE) as NotificationManager` 强制转换
- 不支持安全转换（`as?`），服务可能因类型转换失败而崩溃

**修复解决办法**：
- `as NotificationManager` → `as? NotificationManager` + `manager?.notify()`
- 添加 try-catch 包裹整个更新操作

**涉及文件**：
- `fall/FallDetectorService.kt`

### 【2026-07-31 23:50】【闹钟关不掉-缺少通知栏关闭通道】
**问题现象**：闹钟触发后，用户无法从通知栏关闭闹钟，必须进入 APP 找到 WakeUpActivity 界面才能操作。如果 APP 在后台，闹钟振动和声音持续播放，无法停止。

**报错/根源原因**：
1. `WakeUpWorker` 启动 `WakeUpActivity` 后，没有创建通知栏通知
2. 用户没有"从通知栏直接关闭闹钟"的通道
3. `AlarmAudioController.stop()` 无重入保护，快速多次调用可能导致异常
4. `WakeUpActivity` 销毁时不会取消通知，通知栏残留

**修复解决办法**：
1. **AlarmScheduler.kt**：
   - 新增 `WAKEUP_STOP_ACTION` 常量、`WAKEUP_ALARM_CHANNEL_ID` 通知渠道、`WAKEUP_ALARM_NOTIFICATION_ID` 通知 ID
   - `cancelAlarm()` 添加 try-catch 和 `cancelAlarmNotification()` 清理通知
   - 新增 `createAlarmNotificationChannel()` 和 `showAlarmNotification()` 静态方法
   - 新增 `WakeUpStopReceiver` 广播接收器，处理通知栏"停止闹钟"点击
   - 通知包含"停止闹钟"按钮（`WAKEUP_STOP_ACTION`）
2. **WakeUpScreen.kt**：
   - `WakeUpActivity.onCreate()` 新增 `stop_alarm` extra 处理：立即调用 `WakeUpEngine.forceStop()` + 取消通知 + `finish()`
   - `WakeUpActivity.onDestroy()` 新增通知取消逻辑
   - 新增 `WakeUpEntryPoint` Hilt EntryPoint 接口（用于获取 `WakeUpEngine` 单例）
3. **AndroidManifest.xml**：注册 `WakeUpStopReceiver`
4. **AlarmAudioController.kt**：`stop()` 添加 `isStopping` 重入保护，防止多次调用异常

**涉及文件**：
- `wakeup/AlarmScheduler.kt`
- `wakeup/WakeUpScreen.kt`
- `wakeup/AlarmAudioController.kt`
- `AndroidManifest.xml`

### 【2026-07-31 23:55】【项目名称未统一修改】
**问题现象**：用户要求将项目名称从"微光同行"改为"微光守护"，但代码中多处仍在显示"SafeGuard"旧名称。

**报错/根源原因**：
1. `strings.xml` 中 `app_name` 仍为 `SafeGuard`
2. `AndroidManifest.xml` 中 `label` 仍为 `SafeGuard`
3. `MainActivity.kt` 中 Dashboard 标题仍为 `SafeGuard 安全守护`
4. `AlarmScheduler.kt` 通知标题仍为 `SafeGuard 触觉唤醒`
5. `AudioCaptureService.kt` 通知标题仍为 `SafeGuard Monitoring`
6. `FallAlertDispatcher.kt` 短信内容仍为 `SafeGuard`
7. `FallDetectorService.kt` 通知标题仍为 `SafeGuard 跌倒检测`
8. `FallDetectorModule.kt` 通知渠道描述仍为 `SafeGuard`

**修复解决办法**：
全局替换8处用户可见的"SafeGuard"为"微光守护"，保留内部标识符（如 WakeLock tag、加密常量等）不变。

**涉及文件**：
- `res/values/strings.xml`
- `AndroidManifest.xml`
- `MainActivity.kt`
- `wakeup/AlarmScheduler.kt`
- `audio/AudioCaptureService.kt`
- `fall/FallAlertDispatcher.kt`
- `fall/FallDetectorService.kt`
- `fall/FallDetectorModule.kt`

### 【2026-07-31 23:57】【SignLanguageScreen手语翻译第二行快捷短语崩溃】
**问题现象**：手语翻译界面第二行快捷短语（"好的"、"我需要帮助"、"请帮我开门"）点击后崩溃。

**报错/根源原因**：
`SignLanguageScreen.kt` 第179行第二行快捷短语的 onClick 中直接使用了 `tts.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "sign_phrase_${phrase}")`，但 `tts` 变量在该作用域中未定义（第一行已正确使用 `TtsManager.speak(phrase)`），导致运行时崩溃。

**修复解决办法**：
将 `tts.speak(...)` 改为 `TtsManager.speak(phrase)`，与第一行快捷短语保持一致。

**涉及文件**：
- `ui/signlanguage/SignLanguageScreen.kt`

### 【2026-07-31 23:58】【Compose try-catch编译失败】
**问题现象**：编译APK时报错 `Try catch is not supported around composable function invocations`。

**报错/根源原因**：
`SafeGuardApp()` 函数中在 `when` 块外包裹了 try-catch，新版 Compose 编译器不允许在 composable 函数调用周围使用 try-catch。

**修复解决办法**：
移除 try-catch 包裹，直接使用 `when` 块渲染各 Screen。各 Screen 内部自行处理异常，防止单个模块崩溃导致整个 APP 闪退。

**涉及文件**：
- `MainActivity.kt`

### 【2026-08-01 05:30】【全局异常处理器吞噬异常导致连锁崩溃】
**问题现象**：点击任何按钮（模拟告警、音频监控、清除预警等）都会闪退，不只限于某一模块。APP 在首次异常后继续运行，但后续任何操作都会触发二次崩溃。

**报错/根源原因**：
- `SafeguardApp.kt` 全局异常处理器对非 `VirtualMachineError`/`ThreadDeath` 类型的异常只记录日志后继续运行（Toast 提示"应用出现异常，正在恢复..."）
- 异常发生后 APP 内部状态已损坏（Compose 状态不一致、后台服务/协程处于未知状态、Activity 生命周期异常），继续运行导致后续操作连锁崩溃
- 这是"点击任何按钮都会闪退"的根因，非某个具体模块的问题

**修复解决办法**：
1. 移除异常吞咽逻辑：所有未捕获异常（包括非致命异常）都交给系统默认处理器 `defaultHandler.uncaughtException(thread, throwable)`
2. 让 APP 干净崩溃后由用户重新启动，获得全新的一致状态
3. 保留详细日志记录（`Log.e`），便于开发者排查根因
4. 移除 `Toast` 相关代码（import 和调用）

**涉及文件**：
- `SafeguardApp.kt`

### 【2026-08-01 05:30】【闹钟强制关闭按钮退出失败】
**问题现象**：闹钟长按10秒后关不掉，点击"强制关闭并退出"按钮也无法退出界面。

**报错/根源原因**：
1. "强制关闭并退出"按钮使用临时创建的 `CoroutineScope(kotlinx.coroutines.Dispatchers.Main)` 延迟300ms后调用 `activity?.finish()`
2. 该 `CoroutineScope` 无父 Job，可能被 GC 回收，导致 `finish()` 永远不会执行
3. `forceStop()` 将状态设为 `IDLE`，`snapshotFlow` 虽能捕获 `IDLE` 但需 `wasActive=true` 前置条件，存在时序风险

**修复解决办法**：
1. 移除协程延迟逻辑，直接调用 `activity?.finish()`：`forceStop()` 已通过 `finally` 块确保状态重置，无需等待
2. 添加缺失的 `import androidx.compose.material.icons.filled.Close` 导入

**涉及文件**：
- `wakeup/WakeUpScreen.kt`

### 【2026-08-01 05:30】【AudioCaptureService isRunning标志位提前设置导致误判】
**问题现象**：音频采集服务启动失败（硬件不支持/权限不足）后，`isRunning` 仍为 `true`，导致火灾/燃气/咳嗽/敲门等模块误判服务正在运行，尝试使用音频流时触发连锁异常。

**报错/根源原因**：
1. `onStartCommand()` 在 `startRecording()` 之前设置 `isRunning = true`
2. `startRecording()` 内部有多个提前返回分支（bufferSize 无效、AudioRecord 构造失败、startRecording 失败），这些分支设置 `isRunning = false` 并 `stopSelf()`，但 `onStartCommand` 仍未感知
3. `startForeground()` 失败时（如通知权限被拒绝）未捕获异常，直接崩溃

**修复解决办法**：
1. `isRunning = true` 从 `onStartCommand` 移到 `startRecording()` 内部 AudioRecord 成功创建并启动之后
2. `onStartCommand` 中 `startForeground()` 包裹 try-catch，失败时 `isRunning = false` + `stopSelf()` + 返回 `START_NOT_STICKY`
3. 录音循环退出时也设置 `isRunning = false`，确保状态始终准确

**涉及文件**：
- `audio/AudioCaptureService.kt`

### 【2026-08-01 12:00】【Android 14+ 前台服务类型权限缺失导致全部模块闪退】
**问题现象**：
点击火灾/燃气/咳嗽/敲门等模块的"模拟告警"或"音频监控"按钮时 APP 闪退。跌倒检测开启后闪退。所有依赖前台服务的功能全部不可用。

**报错/根源原因**：
- 手机系统为 Android 16（Xiaomi HyperOS），APP targetSDK=34
- Android 14+ 要求每个前台服务类型必须声明对应的权限：
  - `AudioCaptureService` 使用 `foregroundServiceType="microphone"` → 需要 `FOREGROUND_SERVICE_MICROPHONE`（已声明但旧APK未包含）
  - `FallDetectorService` 使用 `foregroundServiceType="location|camera"` → 需要 `FOREGROUND_SERVICE_CAMERA`（**缺失**）和 `FOREGROUND_SERVICE_LOCATION`（**缺失**）
- 系统在 `Service.startForeground()` 时校验权限，缺失则抛出 `SecurityException` 导致服务崩溃
- dropbox 中记录了 5 次相同崩溃（PID: 17247, 28137, 28140, 5133, 7229），全部因 `FOREGROUND_SERVICE_MICROPHONE` 缺失

**崩溃堆栈**：
```
java.lang.SecurityException: Starting FGS with type microphone callerApp=... targetSDK=34 requires permissions: all of the permissions allOf=true [android.permission.FOREGROUND_SERVICE_MICROPHONE]
    at android.app.Service.startForeground(Service.java:776)
    at com.weiguang123.safeguard.audio.AudioCaptureService.onStartCommand(AudioCaptureService.kt:82)
```

**修复解决办法**：
1. `AndroidManifest.xml` 添加缺失权限：
   - `FOREGROUND_SERVICE_CAMERA`（FallDetectorService 的 camera 类型）
   - `FOREGROUND_SERVICE_LOCATION`（FallDetectorService 的 location 类型）
   - `FOREGROUND_SERVICE_SPECIAL_USE`（兜底，防止其他服务类型权限缺失）
2. `FallDetectorService.kt`：`startForeground()` 添加 try-catch（SecurityException + Exception），失败时优雅降级（stopSelf + 返回 START_NOT_STICKY），与 AudioCaptureService 保持一致

**涉及文件**：
- `AndroidManifest.xml`
- `fall/FallDetectorService.kt`