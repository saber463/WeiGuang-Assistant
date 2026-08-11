# SafeGuard（微光守护）项目BUG完整排查手册

> **项目名称**：微光守护（SafeGuard）- AI多模态安全守护平台  
> **时区标准**：UTC+8 北京时间  
> **使用规范**：出现同类BUG优先查阅历史记录，无匹配方案再新增记录  
> **最后更新**：2026-08-07  
> **BUG总数**：52条  

---

## 一、BUG概览统计

### 1.1 按严重程度分布

| 严重程度 | 数量 | 占比 |
|----------|------|------|
| 🔴 严重 (Critical) | 16 | 30.8% |
| 🟠 高 (High) | 22 | 42.3% |
| 🟡 中 (Medium) | 8 | 15.4% |
| 🟢 低 (Low) | 6 | 11.5% |

### 1.2 按编程语言分布

| 语言 | 数量 | 占比 |
|------|------|------|
| Kotlin (Android) | 45 | 86.5% |
| Python | 7 | 13.5% |

### 1.3 按功能分类分布

| 分类 | 数量 | 涉及BUG编号 |
|------|------|-------------|
| 构建/依赖 | 9 | #011, #017, #022, #024, #025, #026, #028, #038, #048 |
| 稳定性 | 6 | #041, #042, #043, #044, #051, #052 |
| 异常处理 | 2 | #023, #049 |
| 安全检测 | 2 | #003, #037 |
| 闹钟 | 6 | #001, #002, #007, #036, #045, #050 |
| 跌倒检测 | 1 | #005 |
| 健康/安防检测 | 4 | #004, #029, #030, #031 |
| 语音功能 | 4 | #013, #014, #019, #034 |
| 翻译功能 | 4 | #006, #032, #033, #047 |
| 相机 | 2 | #015, #016 |
| 视觉识别 | 1 | #018 |
| UI主题 | 2 | #012, #021 |
| 设备兼容 | 1 | #027 |
| 文档生成 | 4 | #008, #009, #010, #035 |
| 加密测试 | 2 | #039, #040 |
| 功能规划 | 1 | #020 |
| 项目配置 | 1 | #046 |

### 1.4 按编程语言快速索引

#### Kotlin 问题（45条）

- 🟢 [bug_001] 闹钟提前触发 — `闹钟`
- 🟡 [bug_002] 闹钟无声音 — `闹钟`
- 🔴 [bug_003] 火灾检测/燃气泄漏检测闪退 — `安全检测`
- 🟡 [bug_004] 咳嗽检测/敲门检测退出页面即关闭 — `健康/安防检测`
- 🔴 [bug_005] 跌倒检测闪退 — `跌倒检测`
- 🟠 [bug_006] 手语翻译无声音 — `翻译功能`
- 🟠 [bug_007] KnockViewModel关闭所有模型影响其他检测器 — `闹钟`
- 🔴 [bug_011] App启动闪退-MediaPipe native库缺失 — `构建/依赖`
- 🔴 [bug_012] App启动闪退-主题继承错误 — `UI主题`
- 🟡 [bug_013] TTS语音播报 — `语音功能`
- 🟢 [bug_014] 男声切换 — `语音功能`
- 🟡 [bug_015] Camera ImageAnalysis主线程运行 — `相机`
- 🟠 [bug_016] ImageAnalysis未设置分辨率策略 — `相机`
- 🟠 [bug_017] enableNativeYolo默认false — `构建/依赖`
- 🟠 [bug_018] 药品识别率低（999感冒灵等无法识别） — `视觉识别`
- 🟠 [bug_019] 语音功能失效分析 — `语音功能`
- 🟠 [bug_020] 功能缺失分析 — `功能规划`
- 🔴 [bug_021] MaterialAlertDialogBuilder主题崩溃 — `UI主题`
- 🔴 [bug_022] ML Kit native库缺失 libmlkitcommonpipeline.so — `构建/依赖`
- 🔴 [bug_023] UnsatisfiedLinkError未被try-catch捕获 — `异常处理`
- 🔴 [bug_024] AGP 8.2.0 不打包jniLibs的native库（核心BUG） — `构建/依赖`
- 🟠 [bug_025] Android R+ resources.arsc必须不压缩且4字节对齐 — `构建/依赖`
- 🟢 [bug_026] debug_apk.bat 只安装不编译 — `构建/依赖`
- 🟢 [bug_027] 小米HyperOS USB安装权限限制 — `设备兼容`
- 🟠 [bug_028] FireAlarmViewModel缺少combine导入导致编译失败 — `构建/依赖`
- 🟠 [bug_029] CoughDetector模型路径不匹配导致咳嗽检测永久失效 — `健康/安防检测`
- 🟠 [bug_030] KnockDetector模型路径不匹配导致敲门检测永久失效 — `健康/安防检测`
- 🟡 [bug_031] 咳嗽/敲门UI状态与后台检测器不同步 — `健康/安防检测`
- 🟠 [bug_032] 手语翻译/口语翻译功能缺失 — `翻译功能`
- 🟠 [bug_033] 口语翻译-语音引擎初始化卡住 — `翻译功能`
- 🟠 [bug_034] TTS播报-全局无声音 — `语音功能`
- 🟠 [bug_036] 闹钟长按10秒关不掉+无法退出 — `闹钟`
- 🔴 [bug_037] 火灾/燃气/咳嗽/敲门模拟和监控闪退 — `安全检测`
- 🔴 [bug_041] APP闪退-SafeGuardApp无异常捕获 — `稳定性`
- 🔴 [bug_042] 强制解包!!-4处潜在NPE闪退 — `稳定性`
- 🔴 [bug_043] AudioCaptureService-Class.forName反射风险 — `稳定性`
- 🔴 [bug_044] FallDetectorService-updateNotification强制转换风险 — `稳定性`
- 🟠 [bug_045] 闹钟关不掉-缺少通知栏关闭通道 — `闹钟`
- 🟢 [bug_046] 项目名称未统一修改 — `项目配置`
- 🔴 [bug_047] SignLanguageScreen手语翻译第二行快捷短语崩溃 — `翻译功能`
- 🟠 [bug_048] Compose try-catch编译失败 — `构建/依赖`
- 🔴 [bug_049] 全局异常处理器吞噬异常导致连锁崩溃 — `异常处理`
- 🟠 [bug_050] 闹钟强制关闭按钮退出失败 — `闹钟`
- 🟠 [bug_051] AudioCaptureService isRunning标志位提前设置导致误判 — `稳定性`
- 🔴 [bug_052] 依赖注入 — `稳定性`

#### Python 问题（7条）

- 🟡 [bug_008] PDF生成-表格数据行中文乱码 — `文档生成`
- 🟠 [bug_009] PDF生成-中文乱码 — `文档生成`
- 🟡 [bug_010] PDF生成-算法可视化图表乱码 — `文档生成`
- 🟢 [bug_035] Word文档生成-伪代码出现在目录区域 — `文档生成`
- 🟡 [bug_038] Wycheproof测试向量404 — `构建/依赖`
- 🟠 [bug_039] 加密测试-numpy.bool_ JSON序列化失败 — `加密测试`
- 🟠 [bug_040] 加密测试-明文雪崩测试逻辑缺陷 — `加密测试`

---

## 二、BUG详细排查记录

### 构建/依赖（9条）

#### 🔴 严重 [bug_011] App启动闪退-MediaPipe native库缺失

| 字段 | 内容 |
|------|------|
| **时间** | 2026-06-10 15:55 |
| **分类** | 构建/依赖 → App启动闪退-MediaPipe native库缺失 |
| **语言** | Kotlin |
| **严重程度** | 🔴 严重 |
| **错误代码** | `java.lang.UnsatisfiedLinkError, UnsatisfiedLinkError` |

**📋 问题现象**

> 打包APK安装后，点击APP立即闪退。logcat报错：`java.lang.UnsatisfiedLinkError: dlopen failed: library "libmediapipe_tasks_vision_jni.so" not found`，进程在启动后约2.7秒崩溃。

**🔄 复现步骤**

1. 启动APP进入主界面
2. 点击触发'App启动闪退-MediaPipe native库缺失'相关功能按钮
3. 观察APP是否闪退/崩溃
4. 查看logcat日志确认异常堆栈

**❌ 报错代码**

```
java.lang.UnsatisfiedLinkError, UnsatisfiedLinkError
```

**🔍 根因分析**

- `com.google.mediapipe:tasks-vision:0.10.2` AAR 中包含 `jni/` 目录下的 `.so` 文件（arm64-v8a: 12.7MB, armeabi-v7a: 7.6MB, x86: 19.6MB）
- Android Gradle Plugin (AGP) 未将 AAR 中的 native 库自动合并到 APK 的 `lib/` 中
- 代码层面，MediaPipe `HandLandmarker` 类在静态初始化块中调用 `System.loadLibrary("mediapipe_tasks_vision_jni")`，类加载时即崩溃
- `try-catch` 无法捕获 `UnsatisfiedLinkError`（发生在类加载器层面，不在业务代码调用链上）

**✅ 修复方案**

1. 从 Google Maven 直接下载 AAR：`https://dl.google.com/dl/android/maven2/com/google/mediapipe/tasks-vision/0.10.2/tasks-vision-0.10.2.aar`
2. 解压 AAR（它是 ZIP 格式），提取 `jni/` 目录下的 `.so` 文件
3. 将 `libmediapipe_tasks_vision_jni.so` 复制到 `app/src/main/jniLibs/{arm64-v8a,armeabi-v7a}/`
4. AGP 自动将 `jniLibs/` 下的 native 库打包进 APK
5. 代码层添加双重保护：`initialize()` 方法加 try-catch，`LaunchedEffect` 也加 try-catch

**🏷️ 标签**：`native库` `崩溃` `NDK` `打包` `MediaPipe` `闪退` `手部关键点` `APK`

---

#### 🟠 高 [bug_017] enableNativeYolo默认false

| 字段 | 内容 |
|------|------|
| **时间** | 2026-06-03 15:00 |
| **分类** | 构建/依赖 → enableNativeYolo默认false |
| **语言** | Kotlin |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> 药品识别、物体识别功能完全不可用或回退到ML Kit基础识别。

**🔄 复现步骤**

1. 启动APP进入对应的检测/识别模块
2. 开启检测开关
3. 提供对应的输入（音频/图像/传感器）
4. 观察检测结果是否正常返回

**🔍 根因分析**

- `build.gradle`第10行：`def enableNativeYolo = (project.findProperty("ENABLE_NATIVE_YOLO") ?: "false").toString().toBoolean()`
- 默认值为`false`，Native YOLO Pipeline（C++预处理+NMS）不会被编译
- `YoloRuntime.nativeAvailable`为`false`→`YoloDetector`构造抛出异常→`VisionObjectAnalyzer`中yoloDetector为null
- 所有帧都通过ML Kit基础检测（仅识别人、手机、键盘等有限类别），无法识别药品

**✅ 修复方案**

- 将默认值改为`true`：`def enableNativeYolo = (project.findProperty("ENABLE_NATIVE_YOLO") ?: "true").toString().toBoolean()`
- 或者在gradle.properties中添加`ENABLE_NATIVE_YOLO=true`

**📁 涉及文件**

- build.gradle

**🏷️ 标签**：`NDK` `目标检测` `编译错误` `YOLO` `native库`

---

#### 🔴 严重 [bug_022] ML Kit native库缺失 libmlkitcommonpipeline.so

| 字段 | 内容 |
|------|------|
| **时间** | 2026-06-10 17:20 |
| **分类** | 构建/依赖 → ML Kit native库缺失 libmlkitcommonpipeline.so |
| **语言** | Kotlin |
| **严重程度** | 🔴 严重 |
| **错误代码** | `java.lang.UnsatisfiedLinkError, UnsatisfiedLinkError` |

**📋 问题现象**

> MediaPipe闪退修复后，APP启动仍崩溃。logcat报错：
`java.lang.UnsatisfiedLinkError: dlopen failed: library "libmlkitcommonpipeline.so" not found`
崩溃于`com.google.mlkit.vision.vkp.PipelineManager.<clinit>`→`object-detection`初始化

**🔄 复现步骤**

1. 启动APP进入主界面
2. 点击触发'ML Kit native库缺失 libmlkitcommonpipeline.so'相关功能按钮
3. 观察APP是否闪退/崩溃
4. 查看logcat日志确认异常堆栈

**❌ 报错代码**

```
java.lang.UnsatisfiedLinkError, UnsatisfiedLinkError
```

**🔍 根因分析**

- 与MediaPipe同类型问题：AGP未将AAR中的native库打包进APK
- `com.google.mlkit:object-detection:17.0.0`→依赖`vision-internal-vkp-18.2.2`，其jni目录含`libmlkitcommonpipeline.so`
- 崩溃发生在后台线程pool-10-thread-1，因ML Kit在首次图像分析时触发动态加载

**✅ 修复方案**

- 从Gradle缓存提取：`{GRADLE_CACHE}/transforms-3/7004095af2f2f23926592f165eb3f92c/transformed/vision-internal-vkp-18.2.2/jni/`
- 复制`libmlkitcommonpipeline.so`到`app/src/main/jniLibs/{arm64-v8a,armeabi-v7a}/`

**🏷️ 标签**：`native库` `崩溃` `NDK` `打包` `MediaPipe` `闪退` `主线程` `性能`

---

#### 🔴 严重 [bug_024] AGP 8.2.0 不打包jniLibs的native库（核心BUG）

| 字段 | 内容 |
|------|------|
| **时间** | 2026-06-10 17:40 |
| **分类** | 构建/依赖 → AGP 8.2.0 不打包jniLibs的native库（核心BUG） |
| **语言** | Kotlin |
| **严重程度** | 🔴 严重 |
| **错误代码** | `UnsatisfiedLinkError` |

**📋 问题现象**

> 虽然jniLibs目录下已有MediaPipe和ML Kit的.so文件，gradlew assembleDebug成功，但安装后APP仍因UnsatisfiedLinkError崩溃。验证发现APK的ZIP条目中完全没有`lib/`目录。

**🔄 复现步骤**

1. 启动APP进入主界面
2. 点击触发'AGP 8.2.0 不打包jniLibs的native库（核心BUG）'相关功能按钮
3. 观察APP是否闪退/崩溃
4. 查看logcat日志确认异常堆栈

**❌ 报错代码**

```
UnsatisfiedLinkError
```

**🔍 根因分析**

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

**✅ 修复方案**

- 在app/build.gradle添加自定义`injectNativeLibs` Gradle任务，作为`assembleDebug`的后处理步骤：
1. 解压原始APK到临时目录
2. 删除旧签名文件（META-INF/*.SF, *.RSA, *.DSA）
3. 复制jniLibs下的.so到lib/{arm64-v8a,armeabi-v7a}/
4. 用Java ZipOutputStream重新打包（.so和resources.arsc设为STORED不压缩）
5. `zipalign -p 4` 4字节对齐
6. `apksigner sign` v2/v3签名（使用debug.keystore）
7. 替换原APK

**📁 涉及文件**

- build.gradle

**🏷️ 标签**：`打包` `NDK` `MediaPipe` `手部关键点` `native库` `APK`

---

#### 🟠 高 [bug_025] Android R+ resources.arsc必须不压缩且4字节对齐

| 字段 | 内容 |
|------|------|
| **时间** | 2026-06-10 18:00 |
| **分类** | 构建/依赖 → Android R+ resources.arsc必须不压缩且4字节对齐 |
| **语言** | Kotlin |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> apksigner签名后的APK安装失败：
`Failure [-124: Failed parse during installPackageLI: Targeting R+ (version 30 and above) requires the resources.arsc of installed APKs to be stored uncompressed and aligned on a 4-byte boundary]`

**🔄 复现步骤**

1. 触发条件：签名或权限问题导致APK安装被拒绝
2. 观察功能是否正常响应
3. 检查日志输出

**🔍 根因分析**

- targetSdk 34 >= 30，Android 11+要求resources.arsc在APK中不压缩存储且4字节边界对齐
- 之前的ant.zip打包方案压缩了resources.arsc
- jarsigner仅支持v1签名（JAR签名），不满足Android R+的v2/v3签名要求

**✅ 修复方案**

- 使用Java ZipOutputStream显式控制：`ZipEntry.STORED`方法，预计算CRC32
- 使用`zipalign -f -p 4`确保4字节对齐
- 使用`apksigner`（build-tools 34.0.0）替代jarsigner，支持v2/v3签名

**🏷️ 标签**：`打包` `APK`

---

#### 🟢 低 [bug_026] debug_apk.bat 只安装不编译

| 字段 | 内容 |
|------|------|
| **时间** | 2026-06-10 18:30 |
| **分类** | 构建/依赖 → debug_apk.bat 只安装不编译 |
| **语言** | Kotlin |
| **严重程度** | 🟢 低 |

**📋 问题现象**

> 运行debug_apk.bat后，手机上的APP仍是旧版本，代码改动未生效。

**🔄 复现步骤**

1. 触发条件：运行debug_apk.bat后，手机上的APP仍是旧版本，代码改动未生效。
2. 观察功能是否正常响应
3. 检查日志输出

**🔍 根因分析**

- debug_apk.bat流程：检查ADB→安装已有APK→清logcat→启动APP→抓取日志
- bat脚本**不会执行gradlew assembleDebug**，只安装build目录下已有的APK
- 用户修改代码后需手动运行`gradlew assembleDebug`才会生成新APK

**✅ 修复方案**

- debug_apk.bat仅用于设备日志诊断，需手动先执行`gradlew assembleDebug`构建
- 或使用Android Studio的Run按钮（自动编译+安装）

**🏷️ 标签**：`打包` `编译错误` `APK`

---

#### 🟠 高 [bug_028] FireAlarmViewModel缺少combine导入导致编译失败

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 23:30 |
| **分类** | 构建/依赖 → FireAlarmViewModel缺少combine导入导致编译失败 |
| **语言** | Kotlin |
| **严重程度** | 🟠 高 |
| **错误代码** | `Unresolved reference: combine` |

**📋 问题现象**

> 编译 APK 时报错 `Unresolved reference: combine`，无法通过编译。

**🔄 复现步骤**

1. 修改代码后执行 `gradlew assembleDebug`
2. 观察编译输出错误信息
3. 检查相关依赖和导入语句

**❌ 报错代码**

```
Unresolved reference: combine
```

**🔍 根因分析**

- `FireAlarmViewModel.kt` 第29行使用 `combine()` 函数合并两个 StateFlow，但 import 区域缺少 `import kotlinx.coroutines.flow.combine`
- 上一轮修复时添加了 `combine` 调用但遗漏了对应的 import 语句
- 导致编译失败，无法生成 APK

**✅ 修复方案**

- 在 `FireAlarmViewModel.kt` 添加 `import kotlinx.coroutines.flow.combine`

**📁 涉及文件**

- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`FireAlarmViewModel.kt

**🏷️ 标签**：`Kotlin` `ViewModel` `MVVM` `打包` `编译错误` `APK`

---

#### 🟡 中 [bug_038] Wycheproof测试向量404

| 字段 | 内容 |
|------|------|
| **时间** | 2026-06-09 16:40 |
| **分类** | 构建/依赖 → Wycheproof测试向量404 |
| **语言** | Python |
| **严重程度** | 🟡 中 |
| **错误代码** | `HTTP 404` |

**📋 问题现象**

> 运行 wycheproof_runner.py 时，下载 Wycheproof 测试向量失败，HTTP 404 Not Found。

**🔄 复现步骤**

1. 触发条件：Google Wycheproof 项目迁移至 C2SP 组织，仓库地址变更导致旧URL失效
2. 观察功能是否正常响应
3. 检查日志输出

**❌ 报错代码**

```
HTTP 404
```

**🔍 根因分析**

- Google Wycheproof 项目已迁移至 C2SP 组织，仓库地址从 google/wycheproof 变为 C2SP/wycheproof
- 测试向量目录从 testvectors/ 变为 testvectors_v1/
- 旧 URL 已失效

**✅ 修复方案**

1. 修改 wycheproof_runner.py 第33行：WYCHEPROOF_BASE = "https://raw.githubusercontent.com/C2SP/wycheproof/main/testvectors_v1/"
2. 清除旧缓存目录 wycheproof_cache/

**📁 涉及文件**

- wycheproof_runner.py

**🏷️ 标签**：`Python` `Wycheproof` `加密测试` `HTTP 404` `URL迁移`

---

#### 🟠 高 [bug_048] Compose try-catch编译失败

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 23:58 |
| **分类** | 构建/依赖 → Compose try-catch编译失败 |
| **语言** | Kotlin |
| **严重程度** | 🟠 高 |
| **错误代码** | `Try catch is not supported` |

**📋 问题现象**

> 编译APK时报错 `Try catch is not supported around composable function invocations`。

**🔄 复现步骤**

1. 修改代码后执行 `gradlew assembleDebug`
2. 观察编译输出错误信息
3. 检查相关依赖和导入语句

**❌ 报错代码**

```
Try catch is not supported
```

**🔍 根因分析**

`SafeGuardApp()` 函数中在 `when` 块外包裹了 try-catch，新版 Compose 编译器不允许在 composable 函数调用周围使用 try-catch。

**✅ 修复方案**

移除 try-catch 包裹，直接使用 `when` 块渲染各 Screen。各 Screen 内部自行处理异常，防止单个模块崩溃导致整个 APP 闪退。
**涉及文件**：
- `MainActivity.kt`

**📁 涉及文件**

- `app/src/main/java/com/weiguangplus/`MainActivity.kt

**🏷️ 标签**：`Jetpack Compose` `打包` `UI` `编译错误` `APK`

---

### 稳定性（6条）

#### 🔴 严重 [bug_041] APP闪退-SafeGuardApp无异常捕获

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 23:50 |
| **分类** | 稳定性 → APP闪退-SafeGuardApp无异常捕获 |
| **语言** | Kotlin |
| **严重程度** | 🔴 严重 |
| **错误代码** | `无直接报错，异常未被捕获导致Compose树崩溃` |

**📋 问题现象**

> APP 任意模块崩溃时，整个应用直接闪退，无法回退到主页面。

**🔄 复现步骤**

1. 启动APP进入主界面
2. 点击触发'APP闪退-SafeGuardApp无异常捕获'相关功能按钮
3. 观察APP是否闪退/崩溃
4. 查看logcat日志确认异常堆栈

**🔍 根因分析**

- SafeGuardApp() 路由函数中 when (currentScreen) 块无 try-catch 包裹
- 任何 Screen 组件在 Compose 渲染期间抛出异常，都会导致整个 Composable 树崩溃
- 用户无法回退到 dashboard，只能看到 APP 闪退

**✅ 修复方案**

- 在 SafeGuardApp() 的 when 块外层包裹 try-catch (e: Exception)
- 异常时记录日志 Log.e("SafeGuardApp", ...) 并回退 currentScreen = "dashboard"

**📁 涉及文件**

- `app/src/main/java/com/weiguangplus/`MainActivity.kt

**🏷️ 标签**：`闪退` `Compose` `异常处理` `SafeGuardApp` `路由` `Kotlin`

---

#### 🔴 严重 [bug_042] 强制解包!!-4处潜在NPE闪退

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 23:50 |
| **分类** | 稳定性 → 强制解包!!-4处潜在NPE闪退 |
| **语言** | Kotlin |
| **严重程度** | 🔴 严重 |

**📋 问题现象**

> 特定条件下（如窗口未附加、预警状态为 null、手语短语未选中）APP 可能因 `!!` 强制解包导致 NPE 闪退。

**🔄 复现步骤**

1. 启动APP进入主界面
2. 点击触发'强制解包!!-4处潜在NPE闪退'相关功能按钮
3. 观察APP是否闪退/崩溃
4. 查看logcat日志确认异常堆栈

**🔍 根因分析**

- `FlashController.kt:68,74`：`attachedWindow!!` 在 `postDelayed` 回调中可能已变为 null
- `AlertManager.kt:53`：`current.activeAlert!!.priority` 若 smart-cast 失效则 NPE
- `SignChatPanel.kt:36`：`selectedPhrase!!` 在 Compose 重组中可能为 null

**✅ 修复方案**

1. `FlashController.kt`：`attachedWindow!!` → `attachedWindow?.let { win -> ... }` 安全调用
2. `AlertManager.kt`：`current.activeAlert!!.priority` → 先提取 `val currentPriority = current.activeAlert?.priority ?: Int.MAX_VALUE`
3. `SignChatPanel.kt`：`selectedPhrase!!` → `selectedPhrase?.let { phrase -> ... }`，配合 `if (selectedPhrase == null)` 处理 else 分支
**涉及文件**：
- `wakeup/FlashController.kt`
- `alert/AlertManager.kt`
- `knock/SignChatPanel.kt`

**📁 涉及文件**

- `app/src/main/java/com/weiguangplus/`FlashController.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`AlertManager.kt
- `app/src/main/java/com/weiguangplus/`SignChatPanel.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`wakeup/FlashController.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`alert/AlertManager.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`knock/SignChatPanel.kt

**🏷️ 标签**：`手语` `Jetpack Compose` `崩溃` `SignLanguage` `UI` `闪退`

---

#### 🔴 严重 [bug_043] AudioCaptureService-Class.forName反射风险

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 23:50 |
| **分类** | 稳定性 → AudioCaptureService-Class.forName反射风险 |
| **语言** | Kotlin |
| **严重程度** | 🔴 严重 |
| **错误代码** | `ClassNotFoundException` |

**📋 问题现象**

> 代码混淆或类名修改后，`AudioCaptureService` 的通知栏点击跳转可能失败，极端情况下导致崩溃。

**🔄 复现步骤**

1. 启动APP进入主界面
2. 点击触发'AudioCaptureService-Class.forName反射风险'相关功能按钮
3. 观察APP是否闪退/崩溃
4. 查看logcat日志确认异常堆栈

**❌ 报错代码**

```
ClassNotFoundException
```

**🔍 根因分析**

- `buildNotification()` 使用 `Class.forName("com.weiguang123.safeguard.MainActivity")` 创建 Intent
- 如果类名因混淆或重构改变，`ClassNotFoundException` 会导致通知创建失败

**✅ 修复方案**

- 添加 try-catch 包裹 `Class.forName()`，异常时降级使用 `packageManager.getLaunchIntentForPackage()` 或安全空 Intent
**涉及文件**：
- `audio/AudioCaptureService.kt`

**📁 涉及文件**

- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`audio/AudioCaptureService.kt

---

#### 🔴 严重 [bug_044] FallDetectorService-updateNotification强制转换风险

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 23:50 |
| **分类** | 稳定性 → FallDetectorService-updateNotification强制转换风险 |
| **语言** | Kotlin |
| **严重程度** | 🔴 严重 |

**📋 问题现象**

> 通知栏更新时，如果 NotificationManager 获取失败，`as` 强制转换抛出异常导致服务崩溃。

**🔄 复现步骤**

1. 启动APP进入主界面
2. 点击触发'FallDetectorService-updateNotification强制转换风险'相关功能按钮
3. 观察APP是否闪退/崩溃
4. 查看logcat日志确认异常堆栈

**🔍 根因分析**

- `updateNotification()` 使用 `getSystemService(NOTIFICATION_SERVICE) as NotificationManager` 强制转换
- 不支持安全转换（`as?`），服务可能因类型转换失败而崩溃

**✅ 修复方案**

- `as NotificationManager` → `as? NotificationManager` + `manager?.notify()`
- 添加 try-catch 包裹整个更新操作
**涉及文件**：
- `fall/FallDetectorService.kt`

**📁 涉及文件**

- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`fall/FallDetectorService.kt

---

#### 🟠 高 [bug_051] AudioCaptureService isRunning标志位提前设置导致误判

| 字段 | 内容 |
|------|------|
| **时间** | 2026-08-01 05:30 |
| **分类** | 稳定性 → AudioCaptureService isRunning标志位提前设置导致误判 |
| **语言** | Kotlin |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> 音频采集服务启动失败（硬件不支持/权限不足）后，`isRunning` 仍为 `true`，导致火灾/燃气/咳嗽/敲门等模块误判服务正在运行，尝试使用音频流时触发连锁异常。

**🔄 复现步骤**

1. 触发条件：音频采集服务启动失败（硬件不支持/权限不足）后，`isRunning` 仍为 `true`，导致火灾/燃气/咳嗽/敲门等模块误判服务正在运行，尝试使用音频流时触发连锁异常。
2. 观察功能是否正常响应
3. 检查日志输出

**🔍 根因分析**

1. `onStartCommand()` 在 `startRecording()` 之前设置 `isRunning = true`
2. `startRecording()` 内部有多个提前返回分支（bufferSize 无效、AudioRecord 构造失败、startRecording 失败），这些分支设置 `isRunning = false` 并 `stopSelf()`，但 `onStartCommand` 仍未感知
3. `startForeground()` 失败时（如通知权限被拒绝）未捕获异常，直接崩溃

**✅ 修复方案**

1. `isRunning = true` 从 `onStartCommand` 移到 `startRecording()` 内部 AudioRecord 成功创建并启动之后
2. `onStartCommand` 中 `startForeground()` 包裹 try-catch，失败时 `isRunning = false` + `stopSelf()` + 返回 `START_NOT_STICKY`
3. 录音循环退出时也设置 `isRunning = false`，确保状态始终准确
**涉及文件**：
- `audio/AudioCaptureService.kt`

**📁 涉及文件**

- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`audio/AudioCaptureService.kt

**🏷️ 标签**：`安全预警` `火灾检测` `音频采集` `燃气检测` `权限` `Android权限` `AudioRecord` `音频分析`

---

#### 🔴 严重 [bug_052] 依赖注入

| 字段 | 内容 |
|------|------|
| **时间** | 2026-08-07 15:24 |
| **分类** | 稳定性 → 依赖注入 |
| **语言** | Kotlin |
| **严重程度** | 🔴 严重 |
| **错误代码** | `RuntimeException: Cannot create an instance of class com.weiguangplus.ui.viewmodel.CallViewModel` |

**📋 问题现象**

> 点击来电助手功能时APP闪退，Hilt无法创建CallViewModel实例

**🔄 复现步骤**

1. 启动APP进入主界面
2. 点击触发'依赖注入'相关功能按钮
3. 观察APP是否闪退/崩溃
4. 查看logcat日志确认异常堆栈

**❌ 报错代码**

```
RuntimeException: Cannot create an instance of class com.weiguangplus.ui.viewmodel.CallViewModel
```

**🔍 根因分析**

CallViewModel继承AndroidViewModel，但缺少@HiltViewModel注解和@Inject constructor，Hilt无法识别该ViewModel的创建方式，导致hiltViewModel()抛出异常

**✅ 修复方案**

在CallViewModel类上添加@HiltViewModel注解，在构造函数上添加@Inject注解：@HiltViewModel class CallViewModel @Inject constructor(application: Application) : AndroidViewModel(application)

**📁 涉及文件**

- `app/src/main/java/com/weiguangplus/`app/src/main/java/com/weiguangplus/ui/viewmodel/CallViewModel.kt

**🏷️ 标签**：`Hilt` `依赖注入` `ViewModel` `闪退` `来电助手`

---

### 异常处理（2条）

#### 🔴 严重 [bug_023] UnsatisfiedLinkError未被try-catch捕获

| 字段 | 内容 |
|------|------|
| **时间** | 2026-06-10 17:30 |
| **分类** | 异常处理 → UnsatisfiedLinkError未被try-catch捕获 |
| **语言** | Kotlin |
| **严重程度** | 🔴 严重 |
| **错误代码** | `java.lang.UnsatisfiedLinkError, UnsatisfiedLinkError, java.lang.Exception, java.lang.Error` |

**📋 问题现象**

> SignLanguageAnalyzer.initialize()中`catch (e: Exception)`未能捕获到UnsatisfiedLinkError，崩溃仍发生。

**🔄 复现步骤**

1. 启动APP进入主界面
2. 点击触发'UnsatisfiedLinkError未被try-catch捕获'相关功能按钮
3. 观察APP是否闪退/崩溃
4. 查看logcat日志确认异常堆栈

**❌ 报错代码**

```
java.lang.UnsatisfiedLinkError, UnsatisfiedLinkError, java.lang.Exception, java.lang.Error
```

**🔍 根因分析**

- `java.lang.UnsatisfiedLinkError`继承自`java.lang.Error`，而非`java.lang.Exception`
- `catch (e: Exception)`只能捕获Exception及其子类，无法捕获Error体系
- 需要`catch (e: Throwable)`才能同时捕获Exception和Error

**✅ 修复方案**

- `SignLanguageAnalyzer.initialize()`中：`catch (e: Exception)`→`catch (e: Throwable)`
- `MediaPipeHandLandmarker.initialize()`中同理修改

---

#### 🔴 严重 [bug_049] 全局异常处理器吞噬异常导致连锁崩溃

| 字段 | 内容 |
|------|------|
| **时间** | 2026-08-01 05:30 |
| **分类** | 异常处理 → 全局异常处理器吞噬异常导致连锁崩溃 |
| **语言** | Kotlin |
| **严重程度** | 🔴 严重 |

**📋 问题现象**

> 点击任何按钮（模拟告警、音频监控、清除预警等）都会闪退，不只限于某一模块。APP 在首次异常后继续运行，但后续任何操作都会触发二次崩溃。

**🔄 复现步骤**

1. 启动APP进入主界面
2. 点击触发'全局异常处理器吞噬异常导致连锁崩溃'相关功能按钮
3. 观察APP是否闪退/崩溃
4. 查看logcat日志确认异常堆栈

**🔍 根因分析**

- `SafeguardApp.kt` 全局异常处理器对非 `VirtualMachineError`/`ThreadDeath` 类型的异常只记录日志后继续运行（Toast 提示"应用出现异常，正在恢复..."）
- 异常发生后 APP 内部状态已损坏（Compose 状态不一致、后台服务/协程处于未知状态、Activity 生命周期异常），继续运行导致后续操作连锁崩溃
- 这是"点击任何按钮都会闪退"的根因，非某个具体模块的问题

**✅ 修复方案**

1. 移除异常吞咽逻辑：所有未捕获异常（包括非致命异常）都交给系统默认处理器 `defaultHandler.uncaughtException(thread, throwable)`
2. 让 APP 干净崩溃后由用户重新启动，获得全新的一致状态
3. 保留详细日志记录（`Log.e`），便于开发者排查根因
4. 移除 `Toast` 相关代码（import 和调用）
**涉及文件**：
- `SafeguardApp.kt`

**📁 涉及文件**

- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`SafeguardApp.kt

**🏷️ 标签**：`闪退` `崩溃` `UI` `Jetpack Compose`

---

### 安全检测（2条）

#### 🔴 严重 [bug_003] 火灾检测/燃气泄漏检测闪退

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 23:00 |
| **分类** | 安全检测 → 火灾检测/燃气泄漏检测闪退 |
| **语言** | Kotlin |
| **严重程度** | 🔴 严重 |
| **错误代码** | `UninitializedPropertyAccessException` |

**📋 问题现象**

> 点击火灾检测或燃气泄漏检测开关后，APP 直接闪退。

**🔄 复现步骤**

1. 启动APP进入主界面
2. 点击触发'火灾检测/燃气泄漏检测闪退'相关功能按钮
3. 观察APP是否闪退/崩溃
4. 查看logcat日志确认异常堆栈

**❌ 报错代码**

```
UninitializedPropertyAccessException
```

**🔍 根因分析**

1. `AudioCaptureService` 在 `onCreate()` 中调用 `createNotificationChannel()`，但 `notificationManager` 由 Hilt 注入，注入失败时抛出 `UninitializedPropertyAccessException`
2. `FireAlarmViewModel` 的 `combine` 流绑定使用了嵌套的 `let { combine(...) }` 模式，可能导致流的订阅状态异常
3. `FireAlarmViewModel.onCleared()` 和 `GasLeakViewModel.onCleared()` 会调用 `stopMonitoring()`，导致 Singleton 检测器在 ViewModel 销毁时被意外停止

**✅ 修复方案**

1. `AudioCaptureService.createNotificationChannel()` 添加 try-catch 防御性获取 NotificationManager：注入失败时降级到 `getSystemService()`
2. `FireAlarmViewModel.uiState` 简化 `combine` 调用：直接 combine 两个原始流，而非嵌套 stateIn + let
3. `FireAlarmViewModel.onCleared()` 和 `GasLeakViewModel.onCleared()` 移除 `stopMonitoring()` 调用，保持与 CoughViewModel 一致的行为

**🏷️ 标签**：`安全预警` `ViewModel` `崩溃` `MVVM` `火灾检测` `燃气检测` `闪退` `依赖注入`

---

#### 🔴 严重 [bug_037] 火灾/燃气/咳嗽/敲门模拟和监控闪退

| 字段 | 内容 |
|------|------|
| **时间** | 2026-08-01 03:00 |
| **分类** | 安全检测 → 火灾/燃气/咳嗽/敲门模拟和监控闪退 |
| **语言** | Kotlin |
| **严重程度** | 🔴 严重 |
| **错误代码** | `IllegalStateException, UnsupportedOperationException, SecurityException, IllegalArgumentException` |

**📋 问题现象**

> 点击模拟告警按钮或音频监控开关时，APP 闪退。任何按钮点击都会触发闪退。

**🔄 复现步骤**

1. 启动APP进入主界面
2. 点击触发'火灾/燃气/咳嗽/敲门模拟和监控闪退'相关功能按钮
3. 观察APP是否闪退/崩溃
4. 查看logcat日志确认异常堆栈

**❌ 报错代码**

```
IllegalStateException, UnsupportedOperationException, SecurityException, IllegalArgumentException
```

**🔍 根因分析**

1. `AudioCaptureService.startRecording()` 中 `AudioRecord` 构造函数可抛出未捕获异常：
- `IllegalArgumentException`：参数无效（buffer size 为负数等）
- `UnsupportedOperationException`：硬件不支持音频参数
- `SecurityException`：RECORD_AUDIO 权限不足
2. `getMinBufferSize()` 返回 `ERROR` 或 `ERROR_BAD_VALUE`（负值）时未校验，直接传给 `AudioRecord` 构造函数
3. `AudioRecord.startRecording()` 可能抛出 `IllegalStateException`
4. 服务崩溃后，`SharedFlow` 状态异常，所有依赖音频采集的模块（火灾/燃气/咳嗽/敲门）连锁闪退
5. `RedAlertOverlay` 中 `startSOSVibration()` 和 `startFlashlight()` 无异常捕获

**✅ 修复方案**

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

**📁 涉及文件**

- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`AudioCaptureService.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`RedAlertOverlay.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`audio/AudioCaptureService.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`alert/RedAlertOverlay.kt

**🏷️ 标签**：`安全预警` `崩溃` `火灾检测` `音频采集` `燃气检测` `闪退` `权限` `Android权限`

---

### 闹钟（6条）

#### 🟢 低 [bug_001] 闹钟提前触发

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 23:00 |
| **分类** | 闹钟 → 闹钟提前触发 |
| **语言** | Kotlin |
| **严重程度** | 🟢 低 |

**📋 问题现象**

> 闹钟设置 19:28，但 28 分之前就触发了。

**🔄 复现步骤**

1. 触发条件：闹钟设置 19:28，但 28 分之前就触发了。
2. 观察功能是否正常响应
3. 检查日志输出

**🔍 根因分析**

- `AlarmScheduler.cancelAlarm()` 使用 `PendingIntent.FLAG_NO_CREATE` 创建 PendingIntent 查找旧闹钟
- `scheduleExactAlarm()` 使用 `PendingIntent.FLAG_UPDATE_CURRENT` 创建新闹钟
- 不同 flag 导致 `PendingIntent.getBroadcast()` 返回不同的 PendingIntent 对象
- `cancelAlarm()` 无法找到并取消旧闹钟，导致旧闹钟残留，提前触发

**✅ 修复方案**

- 将 `cancelAlarm()` 中的 flag 从 `FLAG_NO_CREATE` 改为 `FLAG_UPDATE_CURRENT`，与 `scheduleExactAlarm()` 保持一致
- 直接调用 `alarmManager.cancel(pendingIntent)` 而非空判断后取消

**🏷️ 标签**：`AlarmManager` `PendingIntent` `闹钟`

---

#### 🟡 中 [bug_002] 闹钟无声音

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 23:00 |
| **分类** | 闹钟 → 闹钟无声音 |
| **语言** | Kotlin |
| **严重程度** | 🟡 中 |

**📋 问题现象**

> 闹钟触发后，振动和闪光正常，但没有声音。

**🔄 复现步骤**

1. 启动APP进入相关功能模块
2. 触发'闹钟无声音'相关操作
3. 检查是否有声音输出
4. 确认设备音量、媒体音量正常

**🔍 根因分析**

- `AlarmAudioController` 中 `requestAudioFocus()` 使用了 `AUDIOFOCUS_GAIN_TRANSIENT`（短暂焦点）
- 闹钟场景需要持久音频焦点，短暂焦点可能在播放过程中被其他应用抢占
- 导致闹钟铃声被系统静音或被其他音频打断

**✅ 修复方案**

- 将 `AUDIOFOCUS_GAIN_TRANSIENT` 改为 `AUDIOFOCUS_GAIN`（持久焦点），确保闹钟持续播放

**🏷️ 标签**：`AlarmManager` `闹钟`

---

#### 🟠 高 [bug_007] KnockViewModel关闭所有模型影响其他检测器

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 23:00 |
| **分类** | 闹钟 → KnockViewModel关闭所有模型影响其他检测器 |
| **语言** | Kotlin |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> 退出敲门安防页面后，咳嗽检测、火灾检测、燃气检测的 TFLite 模型全部失效。

**🔄 复现步骤**

1. 启动APP进入对应的检测/识别模块
2. 开启检测开关
3. 提供对应的输入（音频/图像/传感器）
4. 观察检测结果是否正常返回

**🔍 根因分析**

- `KnockViewModel.onCleared()` 调用 `interpreter.closeAll()`
- `TFLiteInterpreter` 是 `@Singleton`，所有检测器共享同一个实例
- `closeAll()` 关闭了所有已加载的模型（咳嗽、火灾、燃气、敲门），导致其他检测器无法推理

**✅ 修复方案**

- `KnockViewModel.onCleared()` 移除 `interpreter.closeAll()` 调用
- 仅保留 `iotExtension.shutdown()` 清理 IoT 资源

**🏷️ 标签**：`安全预警` `ViewModel` `MVVM` `火灾检测` `机器学习` `燃气检测` `TFLite` `音频分析`

---

#### 🟠 高 [bug_036] 闹钟长按10秒关不掉+无法退出

| 字段 | 内容 |
|------|------|
| **时间** | 2026-08-01 03:00 |
| **分类** | 闹钟 → 闹钟长按10秒关不掉+无法退出 |
| **语言** | Kotlin |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> 闹钟有声音了，但长按按钮 10 秒后仍无法关闭闹钟，界面无法退出。

**🔄 复现步骤**

1. 启动APP进入相关功能模块
2. 触发'闹钟长按10秒关不掉+无法退出'相关操作
3. 检查是否有声音输出
4. 确认设备音量、媒体音量正常

**🔍 根因分析**

1. `WakeUpVerifier.LONG_PRESS_DURATION_MS` 为 `3_000L`（3秒），用户以为需要长按 10 秒
2. `tryDismiss()` 调用成功、状态变为 `DISMISSED` 后，`WakeUpActivity` 没有自动关闭
3. 用户看到界面没有变化（闹钟声音停了但界面还在），以为关不掉
4. `forceStop()` 按钮也只停止闹钟不关闭 Activity

**✅ 修复方案**

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

**📁 涉及文件**

- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`WakeUpVerifier.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`WakeUpScreen.kt
- `app/src/main/java/com/weiguangplus/`MainActivity.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`wakeup/WakeUpVerifier.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`wakeup/WakeUpScreen.kt

**🏷️ 标签**：`AlarmManager` `闹钟`

---

#### 🟠 高 [bug_045] 闹钟关不掉-缺少通知栏关闭通道

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 23:50 |
| **分类** | 闹钟 → 闹钟关不掉-缺少通知栏关闭通道 |
| **语言** | Kotlin |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> 闹钟触发后，用户无法从通知栏关闭闹钟，必须进入 APP 找到 WakeUpActivity 界面才能操作。如果 APP 在后台，闹钟振动和声音持续播放，无法停止。

**🔄 复现步骤**

1. 启动APP进入相关功能模块
2. 触发'闹钟关不掉-缺少通知栏关闭通道'相关操作
3. 检查是否有声音输出
4. 确认设备音量、媒体音量正常

**🔍 根因分析**

1. `WakeUpWorker` 启动 `WakeUpActivity` 后，没有创建通知栏通知
2. 用户没有"从通知栏直接关闭闹钟"的通道
3. `AlarmAudioController.stop()` 无重入保护，快速多次调用可能导致异常
4. `WakeUpActivity` 销毁时不会取消通知，通知栏残留

**✅ 修复方案**

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

**📁 涉及文件**

- `app/src/main/java/com/weiguangplus/`AlarmScheduler.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`WakeUpScreen.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`AlarmAudioController.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`wakeup/AlarmScheduler.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`wakeup/WakeUpScreen.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`wakeup/AlarmAudioController.kt
- `weiguang123/safeguard-app/app/src/main/`AndroidManifest.xml

**🏷️ 标签**：`AlarmManager` `闹钟`

---

#### 🟠 高 [bug_050] 闹钟强制关闭按钮退出失败

| 字段 | 内容 |
|------|------|
| **时间** | 2026-08-01 05:30 |
| **分类** | 闹钟 → 闹钟强制关闭按钮退出失败 |
| **语言** | Kotlin |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> 闹钟长按10秒后关不掉，点击"强制关闭并退出"按钮也无法退出界面。

**🔄 复现步骤**

1. 触发条件：闹钟长按10秒后关不掉，点击"强制关闭并退出"按钮也无法退出界面。
2. 观察功能是否正常响应
3. 检查日志输出

**🔍 根因分析**

1. "强制关闭并退出"按钮使用临时创建的 `CoroutineScope(kotlinx.coroutines.Dispatchers.Main)` 延迟300ms后调用 `activity?.finish()`
2. 该 `CoroutineScope` 无父 Job，可能被 GC 回收，导致 `finish()` 永远不会执行
3. `forceStop()` 将状态设为 `IDLE`，`snapshotFlow` 虽能捕获 `IDLE` 但需 `wasActive=true` 前置条件，存在时序风险

**✅ 修复方案**

1. 移除协程延迟逻辑，直接调用 `activity?.finish()`：`forceStop()` 已通过 `finally` 块确保状态重置，无需等待
2. 添加缺失的 `import androidx.compose.material.icons.filled.Close` 导入
**涉及文件**：
- `wakeup/WakeUpScreen.kt`

**📁 涉及文件**

- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`wakeup/WakeUpScreen.kt

**🏷️ 标签**：`AlarmManager` `闹钟` `Kotlin`

---

### 跌倒检测（1条）

#### 🔴 严重 [bug_005] 跌倒检测闪退

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 23:00 |
| **分类** | 跌倒检测 → 跌倒检测闪退 |
| **语言** | Kotlin |
| **严重程度** | 🔴 严重 |

**📋 问题现象**

> 点击"开启跌倒检测"按钮后，APP 直接闪退。

**🔄 复现步骤**

1. 启动APP进入主界面
2. 点击触发'跌倒检测闪退'相关功能按钮
3. 观察APP是否闪退/崩溃
4. 查看logcat日志确认异常堆栈

**🔍 根因分析**

1. **核心BUG**：`FallDetectorService` 在 `onCreate()` 中未创建 NotificationChannel
- Android 8.0+ 在调用 `startForeground()` 前必须先创建 NotificationChannel
- `onStartCommand()` 中直接调用 `startForeground(NOTIFICATION_ID, buildNotification(...))` 导致崩溃
2. `startSensors()` 中传感器（加速度计/陀螺仪）可能为 null，未做防御性检查
3. `registerListener()` 可能在传感器不可用时抛异常

**✅ 修复方案**

1. `FallDetectorService.onCreate()` 中添加 `createNotificationChannel()` 调用
2. `startSensors()` 添加传感器 null 检查和 try-catch 异常处理
3. 传感器不可用时显示通知并优雅关闭服务

**🏷️ 标签**：`闪退` `崩溃` `跌倒检测` `传感器`

---

### 健康/安防检测（4条）

#### 🟡 中 [bug_004] 咳嗽检测/敲门检测退出页面即关闭

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 23:00 |
| **分类** | 健康/安防检测 → 咳嗽检测/敲门检测退出页面即关闭 |
| **语言** | Kotlin |
| **严重程度** | 🟡 中 |

**📋 问题现象**

> 咳嗽检测和敲门检测开启后，退出页面回到主界面，检测功能就停止了。咳嗽检测偶尔显示结果但延迟很大。

**🔄 复现步骤**

1. 启动APP进入对应的检测/识别模块
2. 开启检测开关
3. 提供对应的输入（音频/图像/传感器）
4. 观察检测结果是否正常返回

**🔍 根因分析**

1. **核心BUG**：`CoughMonitorScreen` 中开关调用的是 `coughViewModel.loadModel(...)` 而非 `coughViewModel.startMonitoring()`
- `loadModel()` 仅加载 TFLite 模型到内存，不启动音频采集和后台推理
- `startMonitoring()` 才是真正启动后台持续监控的方法
- 导致咳嗽检测的音频监控从未真正启动，只是加载了模型
2. **同样问题**：`KnockSecurityScreen` 中开关调用 `knockViewModel.loadModel(...)` 而非 `knockViewModel.startMonitoring()`
- `KnockViewModel` 甚至没有 `startMonitoring()` 和 `stopMonitoring()` 方法
3. **咳嗽延迟显示**：CoughBuffer 15秒滑窗机制需要 >=3 次才触发 MILD 预警，用户"隔几次才显示"是因为缓冲区未达到阈值
4. **KnockViewModel.onCleared()** 调用 `interpreter.closeAll()` 会关闭所有 TFLite 模型，影响咳嗽、火灾、燃气检测器

**✅ 修复方案**

1. `CoughMonitorScreen` 中开关改为调用 `coughViewModel.startMonitoring()` / `coughViewModel.stopMonitoring()`
2. `KnockViewModel` 新增 `startMonitoring()`、`stopMonitoring()` 方法和 `isMonitoring` 状态流
3. `KnockSecurityScreen` 中开关改为调用 `knockViewModel.startMonitoring()` / `knockViewModel.stopMonitoring()`
4. `KnockViewModel.onCleared()` 移除 `interpreter.closeAll()` 调用

**🏷️ 标签**：`安全预警` `ViewModel` `MVVM` `火灾检测` `机器学习` `燃气检测` `TFLite` `音频分析`

---

#### 🟠 高 [bug_029] CoughDetector模型路径不匹配导致咳嗽检测永久失效

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 23:30 |
| **分类** | 健康/安防检测 → CoughDetector模型路径不匹配导致咳嗽检测永久失效 |
| **语言** | Kotlin |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> 开启咳嗽检测后，开关显示"监控中"但始终无任何检测结果。偶尔有结果但延迟极大。

**🔄 复现步骤**

1. 启动APP进入对应的检测/识别模块
2. 开启检测开关
3. 提供对应的输入（音频/图像/传感器）
4. 观察检测结果是否正常返回

**🔍 根因分析**

- 代码中模型路径：`models/cough_detector.tflite`
- 实际文件名：`models/cough_detect.tflite`（`cough_detect`，非 `cough_detector`）
- `CoughDetector.startMonitoring()` 中 `interpreter.loadModel()` 因文件不存在抛出异常
- 异常被 catch 后设置 `modelReady = false`，静默失败，所有推理全部跳过
- 用户看到"监控中"但实际上模型从未加载成功，检测完全无效

**✅ 修复方案**

- `CoughDetector.kt` 第36行：`"models/cough_detector.tflite"` → `"models/cough_detect.tflite"`

**📁 涉及文件**

- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`CoughDetector.kt

**🏷️ 标签**：`TFLite` `音频分析` `机器学习` `咳嗽检测`

---

#### 🟠 高 [bug_030] KnockDetector模型路径不匹配导致敲门检测永久失效

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 23:30 |
| **分类** | 健康/安防检测 → KnockDetector模型路径不匹配导致敲门检测永久失效 |
| **语言** | Kotlin |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> 开启敲门检测后，开关显示"监控中"但始终无任何检测结果。

**🔄 复现步骤**

1. 启动APP进入对应的检测/识别模块
2. 开启检测开关
3. 提供对应的输入（音频/图像/传感器）
4. 观察检测结果是否正常返回

**🔍 根因分析**

- 代码中模型路径：`models/knock_detector.tflite`
- 实际文件名：`models/knock_classify.tflite`（`knock_classify`，非 `knock_detector`）
- 与咳嗽检测同类型问题：模型文件名不匹配，加载失败，静默失效

**✅ 修复方案**

- `KnockDetector.kt` 第38行：`"models/knock_detector.tflite"` → `"models/knock_classify.tflite"`

**📁 涉及文件**

- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`KnockDetector.kt

**🏷️ 标签**：`TFLite` `音频分析` `机器学习` `咳嗽检测`

---

#### 🟡 中 [bug_031] 咳嗽/敲门UI状态与后台检测器不同步

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 23:30 |
| **分类** | 健康/安防检测 → 咳嗽/敲门UI状态与后台检测器不同步 |
| **语言** | Kotlin |
| **严重程度** | 🟡 中 |

**📋 问题现象**

> 开启咳嗽/敲门检测后退出页面再返回，开关显示"已停止"但检测器实际仍在后台运行。用户以为检测已关闭，实际上重复开启导致资源浪费。

**🔄 复现步骤**

1. 启动APP进入对应的检测/识别模块
2. 开启检测开关
3. 提供对应的输入（音频/图像/传感器）
4. 观察检测结果是否正常返回

**🔍 根因分析**

- `CoughMonitorScreen` 和 `KnockSecurityScreen` 使用本地 `remember { mutableStateOf(false) }` 存储开关状态
- Compose 导航切换时 `remember` 作用域销毁，`isMonitoring` 重置为 `false`
- 但 `CoughDetector` / `KnockDetector` 是 `@Singleton`，后台持续运行
- UI 状态与检测器真实状态不一致：开关显示"关闭"，检测器实际"运行中"

**✅ 修复方案**

1. `CoughMonitorScreen`：`var isMonitoring by remember` → `val isMonitoring by coughViewModel.isMonitoring.collectAsState()`
2. `KnockSecurityScreen`：`var isMonitoring by remember` → `val isMonitoring by knockViewModel.isMonitoring.collectAsState()`
3. 移除 `onCheckedChange` 中的 `isMonitoring = checked` 手动赋值，由 ViewModel 状态流驱动

**🏷️ 标签**：`Jetpack Compose` `音频分析` `UI` `咳嗽检测`

---

### 语音功能（4条）

#### 🟡 中 [bug_013] TTS语音播报

| 字段 | 内容 |
|------|------|
| **时间** | 2026-06-03 14:30 |
| **分类** | 语音功能 → TTS语音播报 |
| **语言** | Kotlin |
| **严重程度** | 🟡 中 |

**📋 问题现象**

> APP下载到手机后，TTS语音播报无声，点击朗读按钮无任何声音输出。

**🔄 复现步骤**

1. 启动APP进入相关功能模块
2. 触发'TTS语音播报'相关操作
3. 检查是否有声音输出
4. 确认设备音量、媒体音量正常

**🔍 根因分析**

- `OneShotTtsSpeaker` 每次调用`speakChineseNotice()`都创建新的`TtsController`实例
- `TtsController`初始化（`onInit`回调）是异步的，首次调用`speak()`时引擎尚未就绪，返回`false`，播报请求被静默丢弃
- 无请求排队机制，初始化期间的播报请求全部丢失

**✅ 修复方案**

- `OneShotTtsSpeaker` 改为单例模式，复用`TtsController`实例
- `TtsController` 添加`PendingSpeak`请求队列和`flushPendingQueue()`机制
- 初始化未完成时，`speak()`请求存入队列，初始化完成后自动执行
- 队列只保留最新请求（`removeLast()`），避免积压

**🏷️ 标签**：`TTS` `语音播报`

---

#### 🟢 低 [bug_014] 男声切换

| 字段 | 内容 |
|------|------|
| **时间** | 2026-06-03 14:30 |
| **分类** | 语音功能 → 男声切换 |
| **语言** | Kotlin |
| **严重程度** | 🟢 低 |

**📋 问题现象**

> 用户切换到男声后，重启APP又变回女声。

**🔄 复现步骤**

1. 触发条件：用户切换到男声后，重启APP又变回女声。
2. 观察功能是否正常响应
3. 检查日志输出

**🔍 根因分析**

- 男声选择逻辑中使用了`TtsVoiceProfile.MALE`，但配置仅在运行时生效
- `AppPreferences`中没有持久化保存voiceProfile的字段
- `TtsSupport`中的`xiaoyu`关键词虽被标记为男声，但重启后未读取保存的配置

**✅ 修复方案**

- 同一TTS修复中已解决：单例模式复用TTS引擎，切换男声后持续生效
- 确保在`TtsController`中正确设置voiceProfile参数

**🏷️ 标签**：`TTS` `语音播报`

---

#### 🟠 高 [bug_019] 语音功能失效分析

| 字段 | 内容 |
|------|------|
| **时间** | 2026-06-03 16:00 |
| **分类** | 语音功能 → 语音功能失效分析 |
| **语言** | Kotlin |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> 点击语音输入按钮，无反应或提示"语音识别不可用"。

**🔄 复现步骤**

1. 启动APP进入对应的检测/识别模块
2. 开启检测开关
3. 提供对应的输入（音频/图像/传感器）
4. 观察检测结果是否正常返回

**🔍 根因分析**

- 问题1：Vosk离线模型文件缺失，`assets/`目录下无`vosk-model-cn`目录
- 问题2：`VoskModelManager.prepareModel()`检测到assetEntries为空，返回`ready=false`
- 问题3：`SpeechRecognitionSupportDetector.inspect()`中Vosk检测返回false
- 问题4：无网络时，系统SpeechRecognizer不可用，Vosk离线回退也不可用，语音输入完全失效

**✅ 修复方案**

- 下载Vosk中文模型（vosk-model-small-cn-0.22）放入`app/src/main/assets/vosk-model-cn/`
- 增加首次启动时模型下载引导（如网络环境自动下载提示）

---

#### 🟠 高 [bug_034] TTS播报-全局无声音

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 20:55 |
| **分类** | 语音功能 → TTS播报-全局无声音 |
| **语言** | Kotlin |
| **严重程度** | 🟠 高 |
| **错误代码** | `无直接报错，TTS异步初始化时序问题` |

**📋 问题现象**

> 手语翻译、口语翻译、敲门安防三个模块的 TTS 语音播报全部无声。

**🔄 复现步骤**

1. 启动APP进入相关功能模块
2. 触发'TTS播报-全局无声音'相关操作
3. 检查是否有声音输出
4. 确认设备音量、媒体音量正常

**🔍 根因分析**

- 三个模块各自创建独立的 TextToSpeech 实例（remember { TextToSpeech(context) {...} }）
- TTS 初始化是异步的（onInit 回调），但 tts.speak() 在初始化回调完成前就被调用
- 没有请求排队机制，初始化期间的播报请求全部丢失
- 多个 TTS 实例竞争系统资源，导致初始化失败概率增加

**✅ 修复方案**

1. 创建全局 TtsManager 单例（com.weiguang123.safeguard.tts 包）
2. 在 MainActivity.onCreate() 中提前初始化 TTS：TtsManager.init(this)
3. 在 MainActivity.onDestroy() 中释放：TtsManager.shutdown()
4. 三个模块统一使用 TtsManager.speak(text) 替代各自的 tts.speak()
5. TTS 参数：语言中文普通话（Locale.CHINESE）、语速 0.9、音调 1.05
6. 内置等待队列：TTS 未就绪时，speak 请求自动排队，初始化完成后自动播放

**📁 涉及文件**

- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`tts/TtsManager.kt
- `app/src/main/java/com/weiguangplus/`MainActivity.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`ui/signlanguage/SignLanguageScreen.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`ui/signlanguage/SpeechToSignScreen.kt

**🏷️ 标签**：`TTS` `语音播报` `TextToSpeech` `单例模式` `异步初始化` `Kotlin`

---

### 翻译功能（4条）

#### 🟠 高 [bug_006] 手语翻译无声音

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 23:00 |
| **分类** | 翻译功能 → 手语翻译无声音 |
| **语言** | Kotlin |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> 点击手语快捷沟通面板中的短语后，只显示大字卡片，没有语音播报。听障用户无法通过语音与门外的人沟通。

**🔄 复现步骤**

1. 触发条件：音频焦点或初始化问题导致声音无法播放
2. 观察功能是否正常响应
3. 检查日志输出

**🔍 根因分析**

- `SignChatPanel` 的 `onPhraseSelected` 回调仅关闭对话框，没有任何 TTS 语音播报逻辑
- 听力障碍用户选择短语后，门外的人只能看到屏幕上的文字，无法听到语音

**✅ 修复方案**

- 在 `KnockSecurityScreen` 中集成 Android `TextToSpeech` API
- 手语短语选中后，调用 `tts.speak(phrase.label, QUEUE_FLUSH, ...)` 朗读短语
- 设置中文普通话语音（`Locale.CHINESE` / `Locale.SIMPLIFIED_CHINESE`）
- 对话框关闭时释放 TTS 资源

**🏷️ 标签**：`手语` `TTS` `SignLanguage` `语音播报`

---

#### 🟠 高 [bug_032] 手语翻译/口语翻译功能缺失

| 字段 | 内容 |
|------|------|
| **时间** | 2026-08-01 01:30 |
| **分类** | 翻译功能 → 手语翻译/口语翻译功能缺失 |
| **语言** | Kotlin |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> - 手语翻译用不了：safeguard-app 中只有敲门模块的 SignChatPanel（预置短语沟通），没有独立的摄像头手语识别翻译功能
- 口语翻译会弹到另一个界面：safeguard-app 中没有口语翻译模块，用户点击后跳转到错误页面

**🔄 复现步骤**

1. 启动APP进入对应的检测/识别模块
2. 开启检测开关
3. 提供对应的输入（音频/图像/传感器）
4. 观察检测结果是否正常返回

**🔍 根因分析**

- safeguard-app 的模块列表只有 6 个（闹钟/火灾/燃气/咳嗽/敲门/跌倒），没有手语翻译和口语翻译
- 旧版 APP（f:\java\weiguangplus\app\）有完整的 SignLanguageEngine + VoiceToSignController 实现，但未迁移到新版

**✅ 修复方案**

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

**📁 涉及文件**

- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`SignLanguageModels.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`SignLanguageGenerator.kt
- `app/src/main/java/com/weiguangplus/`VoiceRecognizer.kt
- `app/src/main/java/com/weiguangplus/`VoiceToSignController.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`SignLanguageTranslateScreen.kt
- `app/src/main/java/com/weiguangplus/`SpeechToSignScreen.kt
- `app/src/main/java/com/weiguangplus/`MainActivity.kt

**🏷️ 标签**：`手语` `安全预警` `火灾检测` `SignLanguage` `跌倒检测` `燃气检测` `闹钟` `传感器`

---

#### 🟠 高 [bug_033] 口语翻译-语音引擎初始化卡住

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 20:55 |
| **分类** | 翻译功能 → 口语翻译-语音引擎初始化卡住 |
| **语言** | Kotlin |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> 口语翻译界面一直显示"语音引擎初始化中..."，无法进入语音识别状态。

**🔄 复现步骤**

1. 启动APP进入对应的检测/识别模块
2. 开启检测开关
3. 提供对应的输入（音频/图像/传感器）
4. 观察检测结果是否正常返回

**🔍 根因分析**

- `VoiceRecognizer.initialize()` 中调用 `SpeechRecognizer.isRecognitionAvailable()` 在某些设备上返回 false
- 原代码在不可用时 `_isModelReady.value = false`，UI 永久显示"初始化中..."
- `initProgress` 卡在 0.3f，没有超时机制，也没有降级策略

**✅ 修复方案**

1. 即使 SpeechRecognizer 不可用，也标记为降级就绪（`_isModelReady.value = true`）
2. 添加 3 秒超时机制：`Handler.postDelayed(initTimeoutRunnable, 3000)`，超时后自动标记失败并提示用户
3. 正常完成时取消超时定时器 `handler.removeCallbacks(initTimeoutRunnable)`
4. 用户进入界面后如果识别失败，`startListening()` 返回 false，届时再提示

---

#### 🔴 严重 [bug_047] SignLanguageScreen手语翻译第二行快捷短语崩溃

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 23:57 |
| **分类** | 翻译功能 → SignLanguageScreen手语翻译第二行快捷短语崩溃 |
| **语言** | Kotlin |
| **严重程度** | 🔴 严重 |

**📋 问题现象**

> 手语翻译界面第二行快捷短语（"好的"、"我需要帮助"、"请帮我开门"）点击后崩溃。

**🔄 复现步骤**

1. 启动APP进入主界面
2. 点击触发'SignLanguageScreen手语翻译第二行快捷短语崩溃'相关功能按钮
3. 观察APP是否闪退/崩溃
4. 查看logcat日志确认异常堆栈

**🔍 根因分析**

`SignLanguageScreen.kt` 第179行第二行快捷短语的 onClick 中直接使用了 `tts.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "sign_phrase_${phrase}")`，但 `tts` 变量在该作用域中未定义（第一行已正确使用 `TtsManager.speak(phrase)`），导致运行时崩溃。

**✅ 修复方案**

将 `tts.speak(...)` 改为 `TtsManager.speak(phrase)`，与第一行快捷短语保持一致。
**涉及文件**：
- `ui/signlanguage/SignLanguageScreen.kt`

**📁 涉及文件**

- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`ui/signlanguage/SignLanguageScreen.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`SignLanguageScreen.kt

**🏷️ 标签**：`手语` `TTS` `SignLanguage` `语音播报`

---

### 相机（2条）

#### 🟡 中 [bug_015] Camera ImageAnalysis主线程运行

| 字段 | 内容 |
|------|------|
| **时间** | 2026-06-03 15:00 |
| **分类** | 相机 → Camera ImageAnalysis主线程运行 |
| **语言** | Kotlin |
| **严重程度** | 🟡 中 |

**📋 问题现象**

> 相机画面卡顿，识别响应延迟高，界面交互不流畅。

**🔄 复现步骤**

1. 启动APP进入对应的检测/识别模块
2. 开启检测开关
3. 提供对应的输入（音频/图像/传感器）
4. 观察检测结果是否正常返回

**🔍 根因分析**

- `MainActivity.kt`第2676行：`analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx), VisionObjectAnalyzer(...))`
- 图像分析使用主线程执行器，YOLO推理 + ML Kit检测全部在主线程执行
- 主线程被阻塞导致UI渲染、触摸响应、TTS播报全部受影响

**✅ 修复方案**

- 创建专用后台线程的`ExecutorService`
- 将`setAnalyzer`的执行器从`getMainExecutor`改为后台线程执行器
- 确保`VisionObjectAnalyzer.analyze()`中的`onFrameAnalyzed`回调线程安全

**📁 涉及文件**

- `app/src/main/java/com/weiguangplus/`MainActivity.kt

**🏷️ 标签**：`主线程` `TTS` `目标检测` `性能` `YOLO` `语音播报`

---

#### 🟠 高 [bug_016] ImageAnalysis未设置分辨率策略

| 字段 | 内容 |
|------|------|
| **时间** | 2026-06-03 15:00 |
| **分类** | 相机 → ImageAnalysis未设置分辨率策略 |
| **语言** | Kotlin |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> YOLO模型需640x640输入，但ImageAnalysis未指定分辨率，可能输出不同分辨率导致预处理失败。

**🔄 复现步骤**

1. 触发条件：YOLO模型需640x640输入，但ImageAnalysis未指定分辨率，可能输出不同分辨率导致预处理失败。
2. 观察功能是否正常响应
3. 检查日志输出

**🔍 根因分析**

- `YoloDetector.kt`强制要求输入640x640的NHWC格式
- `ImageAnalysis.Builder`未设置`ResolutionSelector`或`setTargetResolution`
- CameraX可能输出1920x1080或其他分辨率，YOLO预处理产生额外的缩放开销

**✅ 修复方案**

- 在`ImageAnalysis.Builder`中添加`setTargetResolution(Size(640, 640))`
- 确保图像分辨率与YOLO模型输入一致，减少预处理失真

**📁 涉及文件**

- `app/src/main/java/com/weiguangplus/`YoloDetector.kt

**🏷️ 标签**：`相机` `目标检测` `CameraX` `YOLO`

---

### 视觉识别（1条）

#### 🟠 高 [bug_018] 药品识别率低（999感冒灵等无法识别）

| 字段 | 内容 |
|------|------|
| **时间** | 2026-06-03 15:30 |
| **分类** | 视觉识别 → 药品识别率低（999感冒灵等无法识别） |
| **语言** | Kotlin |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> 摄像头对准999感冒灵颗粒、阿莫西林等常见药品时，APP无法识别或识别为"瓶子""盒子"。

**🔄 复现步骤**

1. 启动APP进入对应的检测/识别模块
2. 开启检测开关
3. 提供对应的输入（音频/图像/传感器）
4. 观察检测结果是否正常返回

**🔍 根因分析**

- 使用YOLO11n-int8模型，训练数据集为COCO80（80类通用物体）
- COCO80标签中无任何药品相关类别（只有bottle、cup、book等通用类）
- 999感冒灵等药品包装上的关键信息在包装盒表面文字上，需要OCR识别
- OCR虽然存在但在三级管道最后一级，且未针对药品场景优化

**✅ 修复方案**

- 短期：优化OCR管道，确保文字识别结果优先用于药品场景
- 中期：在OCR结果中增加关键词匹配（"感冒灵"、"阿莫西林"、"999"等）
- 长期：收集药品数据集，训练专用药品检测模型（YOLO + 药品SKU分类器）

**🏷️ 标签**：`目标检测` `YOLO`

---

### UI主题（2条）

#### 🔴 严重 [bug_012] App启动闪退-主题继承错误

| 字段 | 内容 |
|------|------|
| **时间** | 2026-06-09 09:52 |
| **分类** | UI主题 → App启动闪退-主题继承错误 |
| **语言** | Kotlin |
| **严重程度** | 🔴 严重 |
| **错误代码** | `IllegalStateException, RuntimeException, java.lang.RuntimeException, java.lang.IllegalStateException` |

**📋 问题现象**

> 点击APP图标闪退，logcat报错：`java.lang.RuntimeException: Unable to start activity ComponentInfo{...MainActivity}: java.lang.IllegalStateException: You need to use a Theme.AppCompat theme (or descendant) with this activity.`

**🔄 复现步骤**

1. 启动APP进入主界面
2. 点击触发'App启动闪退-主题继承错误'相关功能按钮
3. 观察APP是否闪退/崩溃
4. 查看logcat日志确认异常堆栈

**❌ 报错代码**

```
IllegalStateException, RuntimeException, java.lang.RuntimeException, java.lang.IllegalStateException
```

**🔍 根因分析**

- `AndroidManifest.xml`使用`android:theme="@style/Theme.微光同行"`
- `themes.xml`中`Theme.微光同行`的`parent="android:Theme.Material.Light.NoActionBar"` （android:前缀=Android平台原生主题）
- `MainActivity`继承`AppCompatActivity`，强制要求AppCompat主题系列
- 原生主题与AppCompatActivity不兼容，Activity启动时`AppCompatDelegate.createSubDecor`抛出IllegalStateException

**✅ 修复方案**

- `themes.xml`中将`parent="android:Theme.Material.Light.NoActionBar"`改为`parent="Theme.AppCompat.Light.NoActionBar"`
- 移除`android:`前缀，使用AppCompat主题，与AppCompatActivity兼容

**📁 涉及文件**

- `weiguang123/safeguard-app/app/src/main/res/`themes.xml
- `weiguang123/safeguard-app/app/src/main/`AndroidManifest.xml

**🏷️ 标签**：`闪退` `崩溃`

---

#### 🔴 严重 [bug_021] MaterialAlertDialogBuilder主题崩溃

| 字段 | 内容 |
|------|------|
| **时间** | 2026-06-10 17:00 |
| **分类** | UI主题 → MaterialAlertDialogBuilder主题崩溃 |
| **语言** | Kotlin |
| **严重程度** | 🔴 严重 |
| **错误代码** | `IllegalArgumentException, java.lang.IllegalArgumentException` |

**📋 问题现象**

> App启动后，ProjectIntroDialogFragment弹出时崩溃，logcat报错：
`java.lang.IllegalArgumentException: MaterialAlertDialogBuilder requires a value for the colorSurface attribute`

**🔄 复现步骤**

1. 启动APP进入主界面
2. 点击触发'MaterialAlertDialogBuilder主题崩溃'相关功能按钮
3. 观察APP是否闪退/崩溃
4. 查看logcat日志确认异常堆栈

**❌ 报错代码**

```
IllegalArgumentException, java.lang.IllegalArgumentException
```

**🔍 根因分析**

- themes.xml的parent从`android:Theme.Material.Light.NoActionBar`→`Theme.AppCompat.Light.NoActionBar`（为兼容AppCompatActivity）
- AppCompat主题缺少Material Components基础属性`colorSurface`
- MaterialAlertDialogBuilder依赖此属性渲染对话框背景色

**✅ 修复方案**

- `themes.xml`改为`parent="Theme.MaterialComponents.Light.NoActionBar"`（继承AppCompat的同时提供Material属性）
- 确认build.gradle中`com.google.android.material:material:1.11.0`已存在

**📁 涉及文件**

- `weiguang123/safeguard-app/app/src/main/res/`themes.xml
- build.gradle

---

### 设备兼容（1条）

#### 🟢 低 [bug_027] 小米HyperOS USB安装权限限制

| 字段 | 内容 |
|------|------|
| **时间** | 2026-06-10 19:00 |
| **分类** | 设备兼容 → 小米HyperOS USB安装权限限制 |
| **语言** | Kotlin |
| **严重程度** | 🟢 低 |
| **错误代码** | `INSTALL_FAILED_USER_RESTRICTED` |

**📋 问题现象**

> `adb install`提示`INSTALL_FAILED_USER_RESTRICTED: Install canceled by user`，即使手机已解锁。

**🔄 复现步骤**

1. 触发条件：`adb install`提示`INSTALL_FAILED_USER_RESTRICTED: Install canceled by user`，即使手机已解锁。
2. 观察功能是否正常响应
3. 检查日志输出

**❌ 报错代码**

```
INSTALL_FAILED_USER_RESTRICTED
```

**🔍 根因分析**

- 小米HyperOS（Android 14+）对USB安装有额外安全限制，即便开启了开发者选项+USB调试
- `adb install`会触发手机上"USB安装"确认弹窗，但弹窗可能被系统拦截或不可见
- 系统级`pm install`通过`/sdcard/`方式被SELinux拦截（fuse文件系统不可读）

**✅ 修复方案**

- 使用`adb push xxx.apk /data/local/tmp/` + `adb shell pm install -r /data/local/tmp/xxx.apk`
- `/data/local/tmp/`路径绕过了SELinux的fuse文件系统限制
- 手机上需在开发者选项中开启"USB安装"权限

**🏷️ 标签**：`Android权限` `权限`

---

### 文档生成（4条）

#### 🟡 中 [bug_008] PDF生成-表格数据行中文乱码

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 22:55 |
| **分类** | 文档生成 → PDF生成-表格数据行中文乱码 |
| **语言** | Python |
| **严重程度** | 🟡 中 |

**📋 问题现象**

> 测试数据PDF中表格数据行（非表头）中文显示为方块（■），共470个乱码字符。表头和段落文字正常。

**🔄 复现步骤**

1. 运行PDF/文档生成脚本
2. 打开生成的PDF文档
3. 检查中文文本是否正常显示

**🔍 根因分析**

- reportlab Table 的 `TableStyle` 仅对表头行设置了 `('FONTNAME', (0, 0), (-1, 0), FONT_NAME)`，数据行 `(0, 1)` 到 `(-1, -1)` 未设置 FONTNAME
- 数据行默认使用 Helvetica 字体，不含 CJK 字形
- 之前全局替换 `'Helvetica'` → `FONT_NAME` 只覆盖了显式设置 fontName 的代码，TableStyle 中未显式声明则使用内置默认值

**✅ 修复方案**

- 在所有 `ROWBACKGROUNDS` 行后添加 `('FONTNAME', (0, 1), (-1, -1), FONT_NAME)`
- 改用 simhei.ttf（黑体，普通 TTF）替代 msyh.ttc（微软雅黑，TTC 字体集合），提高 reportlab 兼容性

**🏷️ 标签**：`文档生成` `PDF`

---

#### 🟠 高 [bug_009] PDF生成-中文乱码

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 22:30 |
| **分类** | 文档生成 → PDF生成-中文乱码 |
| **语言** | Python |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> 使用 xhtml2pdf 将 Markdown 转换为 PDF 后，所有中文内容显示为方块（■），无法阅读。

**🔄 复现步骤**

1. 运行PDF/文档生成脚本
2. 打开生成的PDF文档
3. 检查中文文本是否正常显示

**🔍 根因分析**

- xhtml2pdf 底层使用 reportlab 渲染 PDF，默认字体为 Helvetica，不含 CJK（中日韩）字形
- 虽然 CSS 中设置了 `font-family: "Microsoft YaHei"`，但 xhtml2pdf 无法自动映射系统字体到 reportlab 字体注册表
- 即使手动注册了 reportlab 字体，xhtml2pdf 的 CSS 解析器也无法正确识别自定义字体名

**✅ 修复方案**

1. 放弃 xhtml2pdf，改用 Edge 浏览器 headless 打印模式：`msedge --headless --print-to-pdf=output.pdf file:///input.html`
2. Edge 原生支持 Windows 系统字体，中文渲染完美，无需额外配置
3. 对于 reportlab 直接生成的图表（测试数据PDF、PPT PDF），通过 `pdfmetrics.registerFont(TTFont(...))` 注册微软雅黑字体，并全局替换所有 `fontName='Helvetica'` 为 `FONT_NAME`
4. 图表中的 `String()` 对象和 `categoryAxis.labels` 必须显式设置 `fontName=FONT_NAME`

**🏷️ 标签**：`文档生成` `PDF`

---

#### 🟡 中 [bug_010] PDF生成-算法可视化图表乱码

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 22:40 |
| **分类** | 文档生成 → PDF生成-算法可视化图表乱码 |
| **语言** | Python |
| **严重程度** | 🟡 中 |

**📋 问题现象**

> 测试数据PDF中 NIST STS 饼图右侧的说明文字（4行中文）显示为空白或乱码。

**🔄 复现步骤**

1. 运行PDF/文档生成脚本
2. 打开生成的PDF文档
3. 检查中文文本是否正常显示

**🔍 根因分析**

- `reportlab.graphics.shapes.String()` 对象默认 `fontName='Helvetica'`，不含中文字形
- 之前的全局替换只覆盖了 `'Helvetica'` 字符串字面量，但未设置 `fontName` 参数的 `String()` 调用使用默认值

**✅ 修复方案**

- 为所有 `String()` 调用显式添加 `fontName=FONT_NAME` 参数
- 为所有 `categoryAxis.labels` 和 `valueAxis.labels` 添加 `fontName=FONT_NAME`
- 为饼图 `pc.slices[n].fontName` 添加 `FONT_NAME`

**🏷️ 标签**：`文档生成` `PDF`

---

#### 🟢 低 [bug_035] Word文档生成-伪代码出现在目录区域

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-24 19:50 |
| **分类** | 文档生成 → Word文档生成-伪代码出现在目录区域 |
| **语言** | Python |
| **严重程度** | 🟢 低 |

**📋 问题现象**

> `gen_docx.py` 生成的 Word 文档（项目计划书_微光同行.docx）中，第2-5页目录区域混入了正文内容：5.3 市场增长驱动力列表、8.1.6 伪代码全部行、15.4 未来展望列表。这些内容本应出现在对应的第八章和第十五章，却被错误地放在了目录页。

**🔄 复现步骤**

1. 触发条件：`gen_docx.py` 生成的 Word 文档（项目计划书_微光同行.docx）中，第2-5页目录区域混入了正文内容：5.3 市场增长驱动力列表、8.1.6 伪代码全部行、15.4 未来展望列表。
2. 观察功能是否正常响应
3. 检查日志输出

**🔍 根因分析**

- 目录生成正则 `r'^\d+\.\s+(.+)$'` 扫描了**整个 MD 文件**，而非仅目录区域
- 正文中多处编号列表行匹配此正则：
- 5.3 市场增长驱动力：`1. **政策驱动**：...`、`2. **技术成熟**：...` 等
- 8.1.6 伪代码：` 1. bestMatch ← null...`（strip后变为 `1. bestMatch`）等
- 15.4 未来展望：`1. **多模态融合**：...`、`2. **SignLLM深度集成**：...` 等
- 这些误匹配的行被当作目录项添加到文档中，导致目录区域出现大量正文碎片

**✅ 修复方案**

- 修改目录生成逻辑：只扫描 MD 文件中 `## 目录` 到下一个 `---` 分隔符之间的内容
- 使用 `in_toc` 状态标记限定扫描范围，遇到 `---` 即停止
- 同时将 `parse_section_body` 中代码块解析优先级提到标题之前，防止代码块内 `###`/`####` 被误判为标题

---

### 加密测试（2条）

#### 🟠 高 [bug_039] 加密测试-numpy.bool_ JSON序列化失败

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 14:09 |
| **分类** | 加密测试 → 加密测试-numpy.bool_ JSON序列化失败 |
| **语言** | Python |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> 运行 `nist_sts_runner.py` 保存结果时报错 `Object of type bool is not JSON serializable`，导致 NIST STS 测试结果无法正确保存到 JSON 文件。

**🔄 复现步骤**

1. 触发条件：运行 `nist_sts_runner.py` 保存结果时报错 `Object of type bool is not JSON serializable`，导致 NIST STS 测试结果无法正确保
2. 观察功能是否正常响应
3. 检查日志输出

**🔍 根因分析**

- NIST STS 测试中，`scipy.stats.chi2` 返回 `numpy.float64` 类型
- `p_value >= 0.01` 比较结果返回 `numpy.bool_` 类型（非 Python 原生 `bool`）
- Python `json.dump` 不支持 `numpy.bool_` 序列化

**✅ 修复方案**

1. 在 `nist_sts_runner.py` 和 `run_all_tests.py` 的 JSON 序列化处添加 `convert_numpy()` 递归转换函数
2. 转换逻辑：`np.bool_` → `bool()`, `np.integer` → `int()`, `np.floating` → `float()`, `np.ndarray` → `.tolist()`

**📁 涉及文件**

- nist_sts_runner.py
- run_all_tests.py

**🏷️ 标签**：`Python`

---

#### 🟠 高 [bug_040] 加密测试-明文雪崩测试逻辑缺陷

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 14:09 |
| **分类** | 加密测试 → 加密测试-明文雪崩测试逻辑缺陷 |
| **语言** | Python |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> 明文雪崩测试显示翻转率仅 11.09%，远低于预期 50%，初始判定为"失败"。但这是 GCM/CTR 模式的正常行为，非安全缺陷。

**🔄 复现步骤**

1. 触发条件：明文雪崩测试显示翻转率仅 11.09%，远低于预期 50%，初始判定为"失败"。但这是 GCM/CTR 模式的正常行为，非安全缺陷。
2. 观察功能是否正常响应
3. 检查日志输出

**🔍 根因分析**

- AES-GCM 使用 CTR 模式加密，属于流密码模式
- 明文 1bit 变化 → 密文仅对应 1bit 变化（CTR 特性）
- 但认证标签（16字节/128bit）因 GHASH 输入变化而翻转约 50%
- 理论翻转率 ≈ (1 + 64) / 640 ≈ 10.16%，而非 50%

**✅ 修复方案**

1. 修正测试逻辑：将明文雪崩的"目标值"从 50% 改为 5%~20% 范围
2. 添加注释说明 GCM/CTR 模式的特性
3. 实测值 10.16%，偏差 0.00%，符合预期

---

### 功能规划（1条）

#### 🟠 高 [bug_020] 功能缺失分析

| 字段 | 内容 |
|------|------|
| **时间** | 2026-06-03 16:30 |
| **分类** | 功能规划 → 功能缺失分析 |
| **语言** | Kotlin |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> 会议记录中提到的以下功能在代码库中完全不存在：
1. 环境音识别（仅存在于规划文档）
2. 语音短文（无任何相关代码）
3. 手语识别（无MediaPipe/手势识别代码）
4. 小雨显示页面（"xiaoyu"仅为TTS语音关键词）
5. 常用语编辑（无相关数据模型或UI）

**🔄 复现步骤**

1. 启动APP进入对应的检测/识别模块
2. 开启检测开关
3. 提供对应的输入（音频/图像/传感器）
4. 观察检测结果是否正常返回

**🔍 根因分析**

- 这些功能属于规划中或已讨论但尚未编码的阶段
- 会议记录中的"失效"指的是用户期望这些功能存在但实际没有找到
- 部分功能入口在UI中占位但后端逻辑未实现

**✅ 修复方案**

- 需要根据产品路线图逐一实现
- 优先级顺序：常用语编辑 > 语音短文 > 环境音识别 > 手语识别 > 小雨页面

**🏷️ 标签**：`手语` `SignLanguage` `MediaPipe` `TTS` `手部关键点` `语音播报`

---

### 项目配置（1条）

#### 🟢 低 [bug_046] 项目名称未统一修改

| 字段 | 内容 |
|------|------|
| **时间** | 2026-07-31 23:55 |
| **分类** | 项目配置 → 项目名称未统一修改 |
| **语言** | Kotlin |
| **严重程度** | 🟢 低 |

**📋 问题现象**

> 用户要求将项目名称从"微光同行"改为"微光守护"，但代码中多处仍在显示"SafeGuard"旧名称。

**🔄 复现步骤**

1. 触发条件：用户要求将项目名称从"微光同行"改为"微光守护"，但代码中多处仍在显示"SafeGuard"旧名称。
2. 观察功能是否正常响应
3. 检查日志输出

**🔍 根因分析**

1. `strings.xml` 中 `app_name` 仍为 `SafeGuard`
2. `AndroidManifest.xml` 中 `label` 仍为 `SafeGuard`
3. `MainActivity.kt` 中 Dashboard 标题仍为 `SafeGuard 安全守护`
4. `AlarmScheduler.kt` 通知标题仍为 `SafeGuard 触觉唤醒`
5. `AudioCaptureService.kt` 通知标题仍为 `SafeGuard Monitoring`
6. `FallAlertDispatcher.kt` 短信内容仍为 `SafeGuard`
7. `FallDetectorService.kt` 通知标题仍为 `SafeGuard 跌倒检测`
8. `FallDetectorModule.kt` 通知渠道描述仍为 `SafeGuard`

**✅ 修复方案**

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

**📁 涉及文件**

- `app/src/main/java/com/weiguangplus/`MainActivity.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`wakeup/AlarmScheduler.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`audio/AudioCaptureService.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`fall/FallAlertDispatcher.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`fall/FallDetectorService.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`fall/FallDetectorModule.kt
- `app/src/main/java/com/weiguangplus/`AlarmScheduler.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`AudioCaptureService.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`FallAlertDispatcher.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`FallDetectorService.kt
- `weiguang123/safeguard-app/app/src/main/java/com/weiguang123/safeguard/`FallDetectorModule.kt
- `weiguang123/safeguard-app/app/src/main/res/`strings.xml
- `weiguang123/safeguard-app/app/src/main/`AndroidManifest.xml

**🏷️ 标签**：`跌倒检测` `传感器`

---

### 宣传网站（1条）

#### 🟠 高 [bug_053] 宣传网站多处404 + 团队学历信息冗余

| 字段 | 内容 |
|------|------|
| **时间** | 2026-08-11 10:00 |
| **分类** | 宣传网站 → 404链接 + 内容优化 |
| **语言** | HTML |
| **严重程度** | 🟠 高 |

**📋 问题现象**

> 宣传网站（index.html）中存在多处404错误：
> 1. 导航栏品牌链接和"探索完整功能"按钮指向不存在的 `app.html`
> 2. 下载区域的 APK、PDF 文件链接指向不存在的本地文件
> 3. 团队介绍表格中展示"学历"列，显示所有成员学历信息

**🔄 复现步骤**

1. 在浏览器中打开 `index.html`
2. 点击导航栏品牌链接（微光同行）→ 跳转 app.html 返回 404
3. 点击"探索完整功能"按钮 → 跳转 app.html 返回 404
4. 点击"下载演示 APK"、"项目计划书 PDF"、"加密算法文档" → 返回 404
5. 查看团队介绍 Tab → 表格包含"学历"列

**🔍 根因分析**

1. `app.html` 文件不存在，但导航栏和按钮均引用了它
2. `微光科技_微光守护_演示APK.apk`、`微光科技_微光守护_项目计划书.pdf`、`微光科技_自研加密算法技术文档.pdf` 三个文件不存在于项目根目录
3. 团队表格中"学历"列为冗余信息，需要删除

**✅ 修复方案**

1. 将所有 `href="app.html"` 改为 `href="index.html"`（共3处）
2. 下载按钮改用 `href="#"` + `onclick="alert('即将上线，敬请期待！');return false"` 占位处理（共6处，两处下载区域）
3. 团队表格删除 `<th>学历</th>` 表头列和每行 `<td>大专</td>` 数据列

**📁 涉及文件**

- `index.html`

**🏷️ 标签**：`404` `HTML` `宣传网站` `团队信息`

---

## 三、常见错误代码速查表

| 错误代码 | 出现次数 | 关联BUG | 快速定位 |
|----------|----------|---------|----------|
| `ClassNotFoundException` | 1 | bug_043 | 见对应BUG详情 |
| `HTTP 404` | 1 | bug_038 | 见对应BUG详情 |
| `INSTALL_FAILED_USER_RESTRICTED` | 1 | bug_027 | 见对应BUG详情 |
| `IllegalArgumentException` | 2 | bug_021, bug_037 | 见对应BUG详情 |
| `IllegalStateException` | 2 | bug_012, bug_037 | 见对应BUG详情 |
| `RuntimeException` | 1 | bug_012 | 见对应BUG详情 |
| `RuntimeException: Cannot create an instance of class com.weiguangplus.ui.viewmodel.CallViewModel` | 1 | bug_052 | 见对应BUG详情 |
| `SecurityException` | 1 | bug_037 | 见对应BUG详情 |
| `Try catch is not supported` | 1 | bug_048 | 见对应BUG详情 |
| `UninitializedPropertyAccessException` | 1 | bug_003 | 见对应BUG详情 |
| `Unresolved reference: combine` | 1 | bug_028 | 见对应BUG详情 |
| `UnsatisfiedLinkError` | 4 | bug_011, bug_022, bug_023, bug_024 | 见对应BUG详情 |
| `UnsupportedOperationException` | 1 | bug_037 | 见对应BUG详情 |
| `java.lang.Error` | 1 | bug_023 | 见对应BUG详情 |
| `java.lang.Exception` | 1 | bug_023 | 见对应BUG详情 |
| `java.lang.IllegalArgumentException` | 1 | bug_021 | 见对应BUG详情 |
| `java.lang.IllegalStateException` | 1 | bug_012 | 见对应BUG详情 |
| `java.lang.RuntimeException` | 1 | bug_012 | 见对应BUG详情 |
| `java.lang.UnsatisfiedLinkError` | 3 | bug_011, bug_022, bug_023 | 见对应BUG详情 |

---

## 四、附录

### 4.1 项目源码结构

```
weiguang123/safeguard-app/
├── app/src/main/java/com/weiguang123/safeguard/
│   ├── MainActivity.kt          # 主Activity，路由控制
│   ├── SafeguardApp.kt          # 全局异常处理器
│   ├── wakeup/                  # 闹钟模块
│   │   ├── AlarmScheduler.kt    # 闹钟调度
│   │   ├── WakeUpScreen.kt      # 闹钟界面
│   │   ├── WakeUpVerifier.kt    # 唤醒验证
│   │   ├── WakeUpEngine.kt      # 唤醒引擎
│   │   ├── AlarmAudioController.kt # 音频控制
│   │   └── FlashController.kt   # 闪光灯控制
│   ├── audio/                   # 音频采集模块
│   │   └── AudioCaptureService.kt # 音频采集服务
│   ├── fall/                    # 跌倒检测模块
│   │   ├── FallDetectorService.kt # 跌倒检测服务
│   │   ├── FallAlertDispatcher.kt # 跌倒告警分发
│   │   └── FallDetectorModule.kt  # 跌倒检测模块
│   ├── alert/                   # 告警模块
│   │   ├── AlertManager.kt      # 告警管理器
│   │   └── RedAlertOverlay.kt   # 红色告警覆盖层
│   ├── cough/                   # 咳嗽检测模块
│   │   └── CoughDetector.kt     # 咳嗽检测器
│   ├── knock/                   # 敲门检测模块
│   │   ├── KnockDetector.kt     # 敲门检测器
│   │   └── SignChatPanel.kt     # 手语对话面板
│   ├── fire/                    # 火灾检测模块
│   │   └── FireAlarmDetector.kt # 火灾检测器
│   ├── gas/                     # 燃气检测模块
│   │   └── GasLeakDetector.kt   # 燃气泄漏检测器
│   ├── signlanguage/            # 手语翻译模块
│   │   ├── SignLanguageScreen.kt      # 手语翻译界面
│   │   ├── SignLanguageGenerator.kt   # 手语手势生成
│   │   ├── SignLanguageModels.kt      # 手语数据模型
│   │   ├── VoiceRecognizer.kt         # 语音识别
│   │   └── VoiceToSignController.kt   # 语音转手语
│   └── tts/                    # TTS模块
│       └── TtsManager.kt       # TTS管理器（单例）
└── app/src/main/res/
    ├── values/strings.xml
    ├── values/themes.xml
    └── AndroidManifest.xml
```

### 4.2 新增BUG填写模板

```markdown
#### 🔴/🟠/🟡/🟢 [bug_NNN] 问题标题

| 字段 | 内容 |
|------|------|
| **时间** | YYYY-MM-DD HH:MM |
| **分类** | 主分类 → 子分类 |
| **语言** | Kotlin / Python |
| **严重程度** | 🔴严重 / 🟠高 / 🟡中 / 🟢低 |
| **错误代码** | 异常类名 |

**📋 问题现象**
> 描述问题具体表现

**🔄 复现步骤**
1. 步骤一
2. 步骤二
3. 步骤三

**❌ 报错代码**
```
异常堆栈或错误信息
```

**🔍 根因分析**
- 根因描述

**✅ 修复方案**
- 修复步骤

**📁 涉及文件**
- 文件路径
```

---

> **文档由 `gen_bug_manual.py` 自动生成** | 数据来源：`bug-bot/knowledge_base_v2.json`
> 如需新增BUG，请编辑 `knowledge_base_v2.json` 后重新运行生成脚本