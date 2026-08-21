/**
 * app/build.gradle.kts — 微光守护 (SafeGuard) 应用模块构建配置
 *
 * 关键决策：
 * - Room 用 KSP 不用 kapt（避免 suspend 函数兼容问题）
 * - Hilt 仍用 kapt（Hilt 2.48 对 KSP 支持不稳定）
 * - jniLibs 手动管理（AGP 8.2.0 BUG：AAR 中 .so 不自动打包）
 */

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")       // Hilt 依赖注入
    id("com.google.devtools.ksp")              // 处理 Room 注解（替代 kapt）
    kotlin("kapt")                             // 处理 Hilt 注解
}

android {
    namespace = "com.weiguangplus"
    compileSdk = 34
    // 指定 NDK 版本（BUG-001 修复）：
    // 本机有两个 NDK：25.1.8937393（缺 llvm-strip）和 25.1.8937393-2（完整）。
    // AGP 默认选 25.1.8937393，strip 任务因 llvm-strip 缺失而硬报错。
    // 此处指定 25.1.8937393-2 触发 AGP 版本解析失败（CXX1103），
    // 使 AGP 回退到 lenient 模式：strip 任务仅复制 .so 不剥离，不报错。
    // 这是让 .so 能进入 APK 的关键配置。
    ndkVersion = "25.1.8937393-2"

    defaultConfig {
        applicationId = "com.weiguangplus"
        minSdk = 24
        targetSdk = 34
        versionCode = 4
        versionName = "1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true            // 兼容旧版 Vector Drawable
        }

        // ================================================================
        // abiFilters：控制 APK 打包的 ABI 列表（BUG-001 修复核心）
        // WHY: 模拟器 ABI 列表为 x86_64,arm64-v8a，native bridge
        //   (libndk_translation.so) 可翻译 arm64-v8a → x86_64。
        //   但 tasks-vision 0.10.2 AAR 不含 x86_64 的 .so，
        //   若 APK 同时有 lib/x86_64/（其他依赖的 .so）和 lib/arm64-v8a/，
        //   系统以 x86_64 为主 ABI 提取 .so，发现缺少
        //   libmediapipe_tasks_vision_jni.so 后不会回退到 arm64-v8a。
        //   排除 x86_64 后，系统无主 ABI 匹配 → 回退到 arm64-v8a，
        //   native bridge 加载全部 arm64-v8a 的 .so 并翻译执行。
        // ================================================================
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86")
        }
    }

    buildTypes {
        // 发布构建配置
        release {
            isMinifyEnabled = false             // 暂不开启代码混淆（开发阶段）
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Java 8：支持 lambda、stream 等特性
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    // 启用 Jetpack Compose
    buildFeatures {
        compose = true
    }

    // Compose 编译器版本（与 Kotlin 1.9.0 兼容）
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    // ================================================================
    // jniLibs 配置：MediaPipe + ML Kit 原生库
    // 背景：AGP 8.2.0 存在 BUG，AAR 中的 .so 文件不会自动打包进 APK
    // 解决：手动从 AAR 提取 .so 文件到 src/main/jniLibs/{abi}/ 目录
    // 涉及的库：
    //   - libmediapipe_tasks_vision_jni.so  (MediaPipe 手部关键点检测)
    //   - libmlkitcommonpipeline.so         (ML Kit 目标检测管线)
    // 覆盖的 ABI：
    //   - arm64-v8a      : 64位 ARM（真机主流）
    //   - armeabi-v7a    : 32位 ARM（旧真机）
    //   - x86            : 32位 x86（x86_64 模拟器兼容运行，BUG-001 修复新增）
    //     WHY: tasks-vision 0.10.2 AAR 不含 x86_64 .so，
    //          x86_64 模拟器可通过 32 位兼容模式加载 x86 .so
    // ================================================================
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }

    // ================================================================
    // aaptOptions：TFLite/MediaPipe 模型文件不压缩
    // 原因：.tflite 和 .task 文件在 APK 中必须以 STORED（不压缩）方式存储，
    //       否则运行时通过 mmap 直接读取模型文件会失败
    // ================================================================
    aaptOptions {
        noCompress("tflite", "task", "lite")
    }

    // ================================================================
    // packaging：APK 打包规则
    // ================================================================
    packaging {
        resources {
            // 排除 META-INF 下的许可证文件，避免重复引用冲突
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            // useLegacyPackaging=true：.so 保留在 APK 内，不在安装时提取
            // WHY: native bridge 提取 arm64-v8a .so 到文件系统时存在兼容问题
            //   (提取目录为空)。保留在 APK 内，运行时由 native bridge 直接从
            //   APK mmap 加载 arm64-v8a 的 .so 并翻译执行（BUG-001 修复）
            useLegacyPackaging = true
        }
    }
}

// ================================================================
// stripDebugDebugSymbols 任务：剥离 .so 文件中的调试符号
//
// 历史：曾在沙箱环境中用 enabled=false 禁用此任务（因 llvm-strip 权限限制），
//       但禁用后该任务不产生输出目录，导致 packageDebug 找不到 .so 文件，
//       所有原生库（MediaPipe、ML Kit、TFLite、Vosk）都无法打包进 APK。
//
// 修复（BUG-001）：移除 enabled=false，让任务正常运行。
//   根因：AGP 默认使用 NDK 25.1.8937393（不完整，缺 llvm-strip），
//         导致 strip 任务失败。现通过 ndkVersion 指定 25.1.8937393-2
//         （完整安装），llvm-strip 可正常执行。
//   同时移除 gradle.properties 中的 android.stripDebugSymbols=false，
//   让 strip 任务正常剥离调试符号（减小 APK 体积）。
// ================================================================

dependencies {
    // ================================================================
    // 第一层：Android 核心库
    // ================================================================
    implementation("androidx.core:core-ktx:1.10.1")                    // Kotlin 扩展库
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")   // 生命周期感知协程
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1") // Compose 生命周期集成
    implementation("androidx.activity:activity-compose:1.7.0")         // Compose Activity
    implementation("androidx.datastore:datastore-preferences:1.0.0")   // 偏好设置持久化（键值对）

    // ================================================================
    // 第二层：Jetpack Compose UI 框架（BOM 统一版本管理）
    // ================================================================
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")                           // Compose UI 基础组件
    implementation("androidx.compose.ui:ui-graphics")                  // 图形绘制
    implementation("androidx.compose.ui:ui-tooling-preview")           // IDE 预览支持
    implementation("androidx.compose.material3:material3")             // Material Design 3 组件
    implementation("androidx.compose.runtime:runtime-livedata")        // LiveData → State 转换

    // ================================================================
    // 第三层：Hilt 依赖注入框架
    //   - hilt-android           : Hilt 核心运行时
    //   - hilt-navigation-compose: Compose 导航中获取 ViewModel 的 hiltViewModel()
    //   - hilt-android-compiler  : 编译时注解处理器（使用 kapt）
    // ================================================================
    implementation("com.google.dagger:hilt-android:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    kapt("com.google.dagger:hilt-android-compiler:2.48")

    // ================================================================
    // 第四层：网络请求（Retrofit + OkHttp）
    //   - Retrofit                : REST API 声明式 HTTP 客户端
    //   - converter-gson          : JSON 解析器（Gson）
    //   - OkHttp                  : 底层 HTTP 引擎
    //   - logging-interceptor     : 网络请求日志（调试用）
    // ================================================================
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ================================================================
    // 第五层：Room 本地数据库
    //   - room-runtime  : Room 运行时库
    //   - room-ktx      : Room 协程扩展（支持 suspend 函数）
    //   - room-compiler : 编译时注解处理器（使用 KSP 而非 kapt）
    //     原因：kapt 与 Kotlin suspend 函数存在兼容性问题，
    //           会导致 "Not sure how to convert a Cursor" 编译错误
    // ================================================================
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    ksp("androidx.room:room-compiler:2.5.2")

    // ================================================================
    // 第六层：图片加载框架 Coil
    // ================================================================
    implementation("io.coil-kt:coil-compose:2.4.0")

    // ================================================================
    // 第七层：CameraX 相机框架
    //   - camera-core      : 相机核心 API
    //   - camera-camera2   : Camera2 硬件抽象层
    //   - camera-lifecycle : 生命周期感知（自动启停相机）
    //   - camera-view      : 预览视图
    // ================================================================
    val cameraxVersion = "1.3.0"
    implementation("androidx.camera:camera-core:${cameraxVersion}")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation("androidx.camera:camera-view:${cameraxVersion}")

    // ================================================================
    // 第八层：ML Kit 机器学习
    //   - text-recognition-chinese : 中文字符 OCR 识别
    //   - object-detection         : 通用物体检测（80类 COCO 标签）
    // ================================================================
    implementation("com.google.mlkit:text-recognition-chinese:16.0.0")
    implementation("com.google.mlkit:object-detection:17.0.0")

    // ================================================================
    // 第九层：MediaPipe 手部关键点检测
    //   - material               : Material Design 组件（MediaPipe 依赖）
    //   - tasks-vision           : MediaPipe 视觉任务（手部21关键点检测）
    //     注意：AAR 中的 .so 文件需手动提取到 jniLibs/
    // ================================================================
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.google.mediapipe:tasks-vision:0.10.2")

    // ================================================================
    // 第十层：TensorFlow Lite 推理引擎
    //   用于咳嗽检测、敲门检测、火灾预警、燃气检测等音频分类模型
    // ================================================================
    implementation("org.tensorflow:tensorflow-lite:2.14.0")

    // ================================================================
    // 第十一层：Vosk 离线语音识别
    //   无需网络即可进行中文语音识别，需配合 assets 中的模型文件
    // ================================================================
    implementation("com.alphacephei:vosk-android:0.3.37")

    // ================================================================
    // 最后：测试依赖（仅开发和测试使用）
    // ================================================================
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}