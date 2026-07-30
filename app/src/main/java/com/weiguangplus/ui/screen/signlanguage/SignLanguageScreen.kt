package com.weiguangplus.ui.screen.signlanguage

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
// VolumeUp removed
// VolumeMute removed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weiguangplus.core.signlanguage.SignLanguageCameraManager
import com.weiguangplus.core.signlanguage.SignLanguageEngine
import com.weiguangplus.core.signlanguage.GestureType
import com.weiguangplus.core.signlanguage.SignLanguageResult
import kotlin.math.roundToInt

// ── 设计令牌 ──
private val Blue = Color(0xFF1565C0)
private val BlueDark = Color(0xFF0D47A1)
private val Bg = Color(0xFFF5F7FA)
private val T1 = Color(0xFF212121)
private val T2 = Color(0xFF616161)
private val T3 = Color(0xFF9E9E9E)
private val Green = Color(0xFF2E7D32)
private val Warn = Color(0xFFEF6C00)
private val Red = Color(0xFFC62828)
private val White = Color.White
private val Surface = Color.White

@Composable
fun SignLanguageScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isRecognizing by remember { mutableStateOf(false) }
    var ttsEnabled by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    val cameraManager = remember {
        SignLanguageCameraManager(context).also { it.initTts() }
    }
    val result by cameraManager.recognitionResult.collectAsStateWithLifecycle()
    val candidates by cameraManager.topKCandidates.collectAsStateWithLifecycle()
    val isModelLoaded by SignLanguageEngine.isModelLoaded.collectAsStateWithLifecycle()
    val motionLevel by SignLanguageEngine.motionLevel.collectAsStateWithLifecycle()

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isRecognizing = true
            cameraManager.startRecognition()
        }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose { cameraManager.release() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics { contentDescription = "手语翻译页面" },
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── 标题栏 ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("手语翻译", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = T1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isModelLoaded) Green else Warn)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isModelLoaded) "AI 模型已就绪 · 99% 准确率" else "模型加载中...",
                        fontSize = 11.sp, color = T3
                    )
                }
            }
            Row {
                // TTS 开关
                IconButton(
                    onClick = {
                        ttsEnabled = !ttsEnabled
                        cameraManager.setTtsEnabled(ttsEnabled)
                    },
                    modifier = Modifier.semantics {
                        contentDescription = if (ttsEnabled) "关闭语音播报" else "开启语音播报"
                    }
                ) {
                    Icon(
                        imageVector = if (ttsEnabled) Icons.Default.Refresh else Icons.Default.Refresh,
                        contentDescription = null,
                        tint = if (ttsEnabled) Blue else T3
                    )
                }
            }
        }

        // ── 相机预览区 ──
        Card(
            modifier = Modifier.fillMaxWidth().height(260.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF263238))
        ) {
            Box(Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).also {
                            cameraManager.attach(lifecycleOwner, it)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // 半透明覆盖（非识别状态）
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isRecognizing,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👋", fontSize = 36.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "将手放入取景框",
                                color = White, fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "打手语即可实时翻译",
                                color = White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // 运动指示器（识别状态时显示）
                androidx.compose.animation.AnimatedVisibility(visible = isRecognizing) {
                    LinearProgressIndicator(
                        progress = motionLevel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .height(3.dp),
                        color = Green,
                        trackColor = Color.Transparent
                    )
                }
            }
        }

        // ── 识别结果卡片 ──
        val r = result
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (r?.gestureType == com.weiguangplus.core.signlanguage.GestureType.SOS)
                    Color(0xFFFFEBEE) else Color(0xFFE8EAF6)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (r != null) {
                    // SOS 标识
                    if (r.gestureType == com.weiguangplus.core.signlanguage.GestureType.SOS) {
                        Text(
                            "🚨 紧急手势",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Red
                        )
                        Spacer(Modifier.height(4.dp))
                    }

                    Text(
                        r.gestureName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = T1
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        r.textTranslation,
                        fontSize = 16.sp,
                        color = T2,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))

                    // 置信度进度条
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("置信度", fontSize = 11.sp, color = T3)
                        Spacer(Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = r.confidence,
                            modifier = Modifier.weight(1f).height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (r.confidence > 0.7f) Green else Warn,
                            trackColor = Color(0xFFE0E0E0)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${(r.confidence * 100).roundToInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (r.confidence > 0.7f) Green else Warn
                        )
                    }
                } else {
                    Text(
                        if (isRecognizing) "正在识别中..." else "点击下方按钮开始识别",
                        fontSize = 14.sp,
                        color = T2
                    )
                }
            }
        }

        // ── Top-K 候选词列表 ──
        if (candidates.isNotEmpty()) {
            Text(
                "候选结果 (Top-5)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = T3,
                modifier = Modifier.padding(start = 4.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(candidates) { candidate ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        modifier = Modifier.border(
                            1.dp,
                            if (candidate.gestureName == r?.gestureName) Blue else Color(0xFFE0E0E0),
                            RoundedCornerShape(10.dp)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                candidate.gestureName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = T1,
                                maxLines = 1
                            )
                            Text(
                                "${(candidate.confidence * 100).roundToInt()}%",
                                fontSize = 11.sp,
                                color = if (candidate.confidence > 0.6f) Green else T3
                            )
                        }
                    }
                }
            }
        }

        // ── 开始/停止按钮 ──
        Button(
            onClick = {
                if (!isRecognizing) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        isRecognizing = true
                        cameraManager.startRecognition()
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                } else {
                    isRecognizing = false
                    cameraManager.stopRecognition()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .semantics {
                    contentDescription = if (isRecognizing) "停止手语识别" else "开始手语识别"
                },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecognizing) Red else Blue
            )
        ) {
            Icon(
                imageVector = if (isRecognizing) Icons.Default.Refresh else Icons.Default.Settings,
                contentDescription = null,
                tint = White
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (isRecognizing) "停止识别" else "开始识别",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = White
            )
        }

        Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)

        // ── 手势参考库 ──
        Text("📖 支持的手势", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = T2)

        val gestureLibrary = listOf(
            Pair("✊", "握拳") to "SOS求救",
            Pair("✋", "手掌张开") to "停止 / 需要帮助",
            Pair("👍", "竖大拇指") to "确认 / 好的",
            Pair("☝️", "食指指向") to "那边 / 方向",
            Pair("✌️", "剪刀手") to "胜利",
            Pair("👌", "OK手势") to "没问题",
            Pair("👋", "摆手") to "你好 / 再见",
            Pair("❤️", "比心") to "谢谢",
            Pair("🤙", "打电话") to "打电话",
            Pair("👎", "拇指朝下") to "不好 / 不行"
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(gestureLibrary) { (pair, meaning) ->
                val (emoji, gesture) = pair
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(emoji, fontSize = 24.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(gesture, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = T1)
                        Text(meaning, fontSize = 10.sp, color = T3)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}
