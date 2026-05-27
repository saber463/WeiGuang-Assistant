/**
 * =============================================================================
 * DualSignScreen — 双屏实时手语对话系统
 * =============================================================================
 *
 * 【功能概述】
 * 本文件实现了一个垂直上下分屏的实时双向手语对话界面，让听障人士和健听人士
 * 可以在同一屏幕上完成无障碍的双向沟通。两个区域同时独立运行，互不干扰。
 *
 * 【上半区：手语 → 语音（Sign-to-Speech）】
 *  - 使用 CameraX 调用前置摄像头实时捕捉用户手势
 *  - 通过 MediaPipe Hand Landmarker 识别 21 个手部关键点
 *  - 匹配手语数据库中的预定义手势，转换为自然语言文本
 *  - 通过 TTSManager 将文本播报出来，实现"手语发声"
 *
 * 【下半区：语音 → 手语（Speech-to-Sign）】
 *  - 使用 Android SpeechRecognizer 实时收音并进行语音识别
 *  - 将识别结果转换为文字展示在屏幕上
 *  - 匹配高频手语短语库，提供一键点击播放手语动画
 *  - 帮助听障人士"看懂"对方说的话
 *
 * 【布局设计】
 *  - 使用 Column + weight(1f) 实现上下均分，不使用 ScrollPage
 *  - 中间使用装饰性分割线（带 ⇅ 图标）分隔两个区域
 *  - 大按钮、大字体的适老化/无障碍设计风格
 *  - 上下两个 Card 分别使用 primaryContainer 和 secondaryContainer 背景色
 *
 * 【数据流】
 *  - 通过 SignLanguageManager.state（StateFlow）驱动 UI 更新
 *  - 手势识别帧通过 ImageAnalysis.Analyzer 回调送入 SignLanguageManager
 *  - 语音识别通过 SpeechRecognizer 回调直接更新 state
 *  - TTS 播报委托给 TTSManager 单例处理
 *
 * 【与 SignScreen 的关系】
 *  - SignScreen 是单模式选择页面（用户二选一），而 DualSignScreen 是双模式同时运行
 *  - 本文件不包含 ScrollPage/HeroCard/HighlightCard 等辅助函数
 *  - 本文件内的私有辅助函数以 Dual 前缀命名，避免与 SignScreen/MainActivity 冲突
 *
 * @see SignLanguageManager 手语识别与语音识别的核心管理器
 * @see SignScreen 单模式手语页面（与 DualSignScreen 互补）
 * @see TTSManager 全局 TTS 播报管理器
 * =============================================================================
 */

package com.weiguangchangxing.weiguang_plus.feature.signlanguage

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors

/**
 * =============================================================================
 * DualSignScreen — 双屏实时手语对话系统入口 Composable
 * =============================================================================
 *
 * 【布局结构（从上到下）】
 * 1. 上半区（weight = 1f）：primaryContainer 背景卡
 *    - 标题："手语 → 语音"
 *    - CameraX 前置摄像头预览（手势捕捉）
 *    - 手势识别结果显示（displayText）
 * 2. 中间分割线：4dp 高的半透明 primary 色条 + ⇅ 图标
 * 3. 下半区（weight = 1f）：secondaryContainer 背景卡
 *    - 标题："语音 → 手语"
 *    - 语音识别文字展示（recognizedText）
 *    - 聆听状态提示
 *    - 开始/停止说话控制按钮
 *
 * 【启动行为】
 *  - 进入页面后立即同时启动 sign-to-speech 和 speech-to-sign 模式
 *  - 自动绑定前置摄像头（如果设备支持手势识别）
 *  - 离开页面时释放所有资源（摄像头、语音识别、分析线程池）
 *
 * @param modifier 外部传入的 Modifier，用于父布局控制边距等
 */
@Composable
fun DualSignScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 创建手语管理器实例，在整个页面生命周期内保持
    val signManager = remember { SignLanguageManager(context) }

    // 通过 StateFlow 收集实时状态，驱动 UI 自动更新
    val state by signManager.state.collectAsState()

    // 获取高频短语列表，供下半区快捷展示使用
    val highFreqPhrases = remember { signManager.getHighFrequencyPhrases() }

    // 创建 CameraX 的 PreviewView，用于展示前置摄像头画面
    val previewView = remember { PreviewView(context) }

    // 创建单线程分析器，用于逐帧分析摄像头图像
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    // =========================================================================
    // 资源释放：当 Composable 离开组合时清理所有资源
    // =========================================================================
    DisposableEffect(Unit) {
        onDispose {
            signManager.release()       // 释放手语管理器（含语音识别器、手势识别器）
            analysisExecutor.shutdown()  // 关闭图像分析线程池
        }
    }

    // =========================================================================
    // 启动双向模式：页面加载后立即同时启动两个方向的识别
    // =========================================================================
    LaunchedEffect(Unit) {
        signManager.startSignToSpeechMode()   // 启动上半区：手语 → 语音
        signManager.startSpeechToSignMode()   // 启动下半区：语音 → 手语
    }

    // =========================================================================
    // 绑定前置摄像头：当设备支持手势追踪时自动开启
    // =========================================================================
    LaunchedEffect(lifecycleOwner) {
        if (state.isHandTrackingSupported) {
            bindFrontCameraDual(signManager, previewView, lifecycleOwner, analysisExecutor)
        }
    }

    // =========================================================================
    // 主布局：垂直上下分屏，各占 50% 高度
    // =========================================================================
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // =====================================================================
        // 上半区：手语 → 语音（Sign-to-Speech）
        // 功能：前置摄像头捕捉手势 → AI 识别 → TTS 播报
        // 背景色：primaryContainer（强调"输出"的语义）
        // =====================================================================
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 区域标题，大字体加粗
                Text(
                    text = "手语 → 语音",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                // 摄像头预览区域：显示前置摄像头画面，让用户看到自己的手势
                if (state.isHandTrackingSupported) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        AndroidView(
                            factory = { previewView },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    // 设备不支持手势识别时的友好提示
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.handTrackingLevel == "NONE") {
                                "当前设备不支持手势识别"
                            } else {
                                "手势识别需要 Android 7.0+"
                            },
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 手势识别结果展示区域
                // displayText 显示匹配到的手语短语文本
                Text(
                    text = state.displayText.ifBlank { "等待手语输入..." },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // =====================================================================
        // 中间分割线：用半透明色条 + ⇅ 双向箭头图标分隔上下两个区域
        // =====================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(4.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(2.dp)
                )
        ) {
            Text(
                text = "⇅",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // =====================================================================
        // 下半区：语音 → 手语（Speech-to-Sign）
        // 功能：麦克风收音 → AI 语音识别 → 文字展示 + 手语短语建议
        // 背景色：secondaryContainer（与上半区形成视觉区分）
        // =====================================================================
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 区域标题，大字体加粗
                Text(
                    text = "语音 → 手语",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                // 语音识别结果展示区：显示识别到的文字内容
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // 识别到的文字，大字体加粗展示
                        Text(
                            text = state.recognizedText.ifBlank { "等待语音输入..." },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        // 聆听状态提示，仅在正在聆听时显示
                        if (state.isListening) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "正在聆听...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 控制按钮：开始/停止语音识别
                // 使用大按钮（48dp 高），方便无障碍操作
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (state.isListening) {
                                signManager.stopSpeechRecognition()  // 停止聆听
                            } else {
                                signManager.startSpeechToSignMode()  // 开始聆听
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = if (state.isListening) {
                            // 聆听中显示红色按钮，表示"点击停止"
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        } else {
                            ButtonDefaults.buttonColors()
                        }
                    ) {
                        Text(
                            text = if (state.isListening) "停止" else "开始说话",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 高频手语短语建议：点击可直接播报对应手语动画
                // 方便听障人士快速回复常见用语（如"谢谢""你好"等）
                if (highFreqPhrases.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 取前 4 个最高频短语展示为快捷按钮
                        highFreqPhrases.take(4).forEach { phrase ->
                            Button(
                                onClick = { signManager.selectSignPhrase(phrase) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            ) {
                                Text(
                                    text = phrase.text,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * =============================================================================
 * bindFrontCameraDual — 绑定前置摄像头到 DualSignScreen
 * =============================================================================
 *
 * 【功能说明】
 * 将 CameraX 的前置摄像头预览流和图像分析流绑定到当前页面的 Lifecycle。
 * 与 SignScreen 中的 bindFrontCamera 功能相同，但在此独立定义以避免命名空间冲突。
 *
 * 【工作流程】
 * 1. 通过 ProcessCameraProvider 获取 CameraX 实例
 * 2. 创建 Preview useCase（640x480），绑定到 previewView 的 SurfaceProvider
 * 3. 创建 ImageAnalysis useCase（640x480），策略为 KEEP_ONLY_LATEST
 * 4. 将图像帧通过分析器送入 SignLanguageManager.analyzeSignFrame() 进行处理
 * 5. 绑定到 lifecycleOwner，使相机生命周期与页面生命周期同步
 *
 * 【参数说明】
 * @param signManager 手语管理器实例，负责分析每一帧图像
 * @param previewView CameraX 的 PreviewView 实例，用于显示相机画面
 * @param lifecycleOwner 生命周期所有者，通常为当前 Composable 的 LifecycleOwner
 * @param executor 图像分析线程池，用于异步处理每一帧
 */
private fun bindFrontCameraDual(
    signManager: SignLanguageManager,
    previewView: PreviewView,
    lifecycleOwner: LifecycleOwner,
    executor: java.util.concurrent.ExecutorService
) {
    val context = previewView.context

    // 获取 CameraX 的 ProcessCameraProvider 实例（异步）
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    // 在主线程上监听 Provider 就绪事件
    cameraProviderFuture.addListener({
        try {
            val provider = cameraProviderFuture.get()

            // ----- 创建预览 UseCase -----
            // 设置 640x480 分辨率，兼顾识别精度和性能
            @Suppress("DEPRECATION")
            val preview = Preview.Builder()
                .setTargetResolution(android.util.Size(640, 480))
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            // ----- 创建图像分析 UseCase -----
            // KEEP_ONLY_LATEST 策略：丢弃积压帧，始终处理最新的一帧
            @Suppress("DEPRECATION")
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(android.util.Size(640, 480))
                .build()
                .also { analysisUseCase ->
                    analysisUseCase.setAnalyzer(executor) { imageProxy ->
                        // 将每一帧送入 SignLanguageManager 进行手势识别分析
                        signManager.analyzeSignFrame(imageProxy)
                    }
                }

            // 解绑所有已有用例，重新绑定前置摄像头 + 预览 + 分析
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                analysis
            )
        } catch (_: Exception) {
            // 相机绑定失败时静默处理，不干扰用户操作
        }
    }, ContextCompat.getMainExecutor(context))
}