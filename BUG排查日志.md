# 项目开发BUG排查日志
> 时区标准：UTC+8 北京时间
> 使用规范：出现同类BUG优先查阅历史记录，无匹配方案再新增记录
> 记录格式：【时间】【BUG分类】问题现象 | 根因分析 | 最终修复方案

---

## 历史BUG记录区

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
- 从Gradle缓存提取：`C:\Users\Fenis\.gradle\caches\transforms-3\7004095af2f2f23926592f165eb3f92c\transformed\vision-internal-vkp-18.2.2\jni\`
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