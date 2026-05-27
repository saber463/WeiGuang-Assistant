package com.weiguangchangxing.weiguang_plus.feature.signlanguage

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.weiguangchangxing.weiguang_plus.core.tts.TTSManager
import java.util.concurrent.Executors

@Composable
fun SignScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val signManager = remember { SignLanguageManager(context) }
    val state by signManager.state.collectAsState()
    val highFreqPhrases = remember { signManager.getHighFrequencyPhrases() }
    val previewView = remember { PreviewView(context) }
    var cameraBound by remember { mutableStateOf(false) }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    val isSignToSpeech = state.currentInputMode == InputMode.SIGN_TO_SPEECH
    val isSpeechToSign = state.currentInputMode == InputMode.SPEECH_TO_SIGN
    val modeName = when {
        isSignToSpeech -> "手语→语音"
        isSpeechToSign -> "语音→手语"
        else -> "请选择模式"
    }

    DisposableEffect(Unit) {
        onDispose {
            signManager.release()
            analysisExecutor.shutdown()
        }
    }

    LaunchedEffect(isSignToSpeech, lifecycleOwner) {
        if (isSignToSpeech && state.isHandTrackingSupported) {
            bindFrontCamera(signManager, previewView, lifecycleOwner, analysisExecutor)
            cameraBound = true
        } else {
            cameraBound = false
        }
    }

    ScrollPage(modifier = modifier) {
        HeroCard(
            title = "手语沟通",
            subtitle = "当前: $modeName",
            body = when {
                state.handTrackingLevel == "NONE" -> "当前安卓版本不支持手势识别，仅可使用语音→手语模式"
                state.isHandTrackingSupported.not() -> "手势识别需要 Android 7.0+"
                else -> "支持语音转手语展示和手语转语音播报"
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    signManager.stopSignRecognition()
                    signManager.startSpeechToSignMode()
                },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = if (isSpeechToSign) ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ) else ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("语音→手语", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Button(
                onClick = {
                    signManager.stopSpeechRecognition()
                    signManager.startSignToSpeechMode()
                },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = state.isHandTrackingSupported,
                colors = if (isSignToSpeech) ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ) else ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("手语→语音", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        }

        if (isSignToSpeech) {
            SectionTitle("相机预览")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxWidth().height(300.dp)
                )
            }
        }

        SectionTitle("识别内容")
        HighlightCard(
            title = state.displayText.ifBlank { "等待输入..." },
            value = state.recognizedText.ifBlank { "" },
            note = when {
                state.errorMessage != null -> "提示: ${state.errorMessage}"
                isSignToSpeech && state.handTrackingLevel == "ADVANCED" -> "高级模式 - MediaPipe 21点追踪"
                isSignToSpeech && state.handTrackingLevel == "FALLBACK" -> "备用模式 - 简单手势检测"
                isSpeechToSign && state.isListening -> "正在聆听..."
                else -> "选择模式后开始使用"
            }
        )

        if (isSpeechToSign && highFreqPhrases.isNotEmpty()) {
            SectionTitle("高频短语")
            highFreqPhrases.take(12).chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { phrase ->
                        OutlinedButton(
                            onClick = { signManager.selectSignPhrase(phrase) },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(phrase.text, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        SectionTitle("控制")
        if (isSpeechToSign) {
            Button(
                onClick = {
                    if (state.isListening) signManager.stopSpeechRecognition()
                    else signManager.startSpeechToSignMode()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = if (state.isListening) ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ) else ButtonDefaults.buttonColors()
            ) {
                Text(
                    if (state.isListening) "停止聆听" else "开始聆听",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        if (isSignToSpeech) {
            Button(
                onClick = {
                    if (state.isRecognizing) signManager.stopSignRecognition()
                    else signManager.startSignToSpeechMode()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = if (state.isRecognizing) ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ) else ButtonDefaults.buttonColors()
            ) {
                Text(
                    if (state.isRecognizing) "停止识别" else "开始识别",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        if (state.modelDownloading) {
            HighlightCard(
                title = "模型下载中",
                value = "首次使用需下载手语识别模型(~15MB)",
                note = "请保持网络连接"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun bindFrontCamera(
    signManager: SignLanguageManager,
    previewView: PreviewView,
    lifecycleOwner: LifecycleOwner,
    executor: java.util.concurrent.ExecutorService
) {
    val context = previewView.context
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        try {
            val provider = cameraProviderFuture.get()
            @Suppress("DEPRECATION")
            val preview = Preview.Builder()
                .setTargetResolution(android.util.Size(640, 480))
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            @Suppress("DEPRECATION")
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(android.util.Size(640, 480))
                .build()
                .also { analysisUseCase ->
                    analysisUseCase.setAnalyzer(executor) { imageProxy ->
                        signManager.analyzeSignFrame(imageProxy)
                    }
                }

            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis)
        } catch (_: Exception) {
        }
    }, ContextCompat.getMainExecutor(context))
}

@Composable
private fun ScrollPage(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

@Composable
private fun HeroCard(title: String, subtitle: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun HighlightCard(title: String, value: String, note: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (value.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}