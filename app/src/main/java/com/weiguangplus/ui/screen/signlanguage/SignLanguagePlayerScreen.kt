package com.weiguangplus.ui.screen.signlanguage

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weiguangplus.core.signlanguage.SignLanguageGenerator
import com.weiguangplus.core.signlanguage.VoiceToSignController

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
fun SignLanguagePlayerScreen() {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<SignLanguageGenerator.GenerationResult?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var showWordList by remember { mutableStateOf(false) }
    var isVoiceMode by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // ── 语音→手语控制器 ──
    val voiceController = remember { VoiceToSignController(context) }
    val liveText by voiceController.liveText.collectAsStateWithLifecycle()
    val recognizedText by voiceController.recognizedText.collectAsStateWithLifecycle()
    val isListening by voiceController.isListening.collectAsStateWithLifecycle()
    val isModelReady by voiceController.isModelReady.collectAsStateWithLifecycle()
    val initProgress by voiceController.initProgress.collectAsStateWithLifecycle()

    val voiceGeneration by voiceController.currentGeneration.collectAsStateWithLifecycle()

    // 录音权限
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isVoiceMode = true
            voiceController.start { success ->
                if (!success) isVoiceMode = false
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { voiceController.release() }
    }

    // 语音模式下自动更新结果
    if (isVoiceMode && voiceGeneration != null) {
        result = voiceGeneration
        showResult = true
    }
    if (isVoiceMode && recognizedText.isNotBlank() && inputText.isEmpty()) {
        inputText = recognizedText
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics { contentDescription = "手语生成页面" },
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── 标题 ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("手语生成", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = T1)
                Text(
                    if (isVoiceMode) "语音→手语实时翻译中..." else "输入文字或语音，生成手语动画",
                    fontSize = 13.sp, color = T3
                )
            }
            // 麦克风按钮
            MicButton(
                isListening = isListening,
                isModelReady = isModelReady,
                initProgress = initProgress,
                onClick = {
                    if (!isVoiceMode) {
                        if (ContextCompat.checkSelfPermission(
                                context, Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            isVoiceMode = true
                            voiceController.start { success ->
                                if (!success) isVoiceMode = false
                            }
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    } else {
                        isVoiceMode = false
                        voiceController.stop()
                    }
                }
            )
        }

        // ── 语音识别实时文本 ──
        AnimatedVisibility(visible = isVoiceMode && liveText.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 脉冲动画点
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.6f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseScale"
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Red)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("🎤 正在听...", fontSize = 11.sp, color = Green, fontWeight = FontWeight.Medium)
                        Text(
                            liveText,
                            fontSize = 16.sp,
                            color = T1,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ── 文字输入区 ──
        OutlinedTextField(
            value = inputText,
            onValueChange = {
                inputText = it
                if (!isVoiceMode && it.isNotBlank()) {
                    val genResult = voiceController.generateFromText(it)
                    result = genResult
                    showResult = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "输入要翻译的文字" },
            placeholder = {
                Text(
                    if (isVoiceMode) "语音识别中..." else "输入文字，如：你好，我需要帮助",
                    color = T3
                )
            },
            enabled = !isVoiceMode,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Blue,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                disabledContainerColor = Color(0xFFF5F5F5),
                disabledTextColor = T2
            ),
            shape = RoundedCornerShape(12.dp),
            maxLines = 3,
            trailingIcon = {
                IconButton(
                    onClick = {
                        val genResult = voiceController.generateFromText(inputText)
                        result = genResult
                        showResult = true
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier.semantics { contentDescription = "生成手语动画" }
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (inputText.isNotBlank()) Blue else T3
                    )
                }
            }
        )

        // ── 快捷输入 ──
        if (!isVoiceMode) {
            Text("常用短语", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = T3)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("你好", "谢谢", "救命", "好的", "对不起", "我需要帮助").forEach { phrase ->
                    val isSelected = inputText == phrase
                    Button(
                        onClick = {
                            inputText = phrase
                            result = voiceController.generateFromText(phrase)
                            showResult = true
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Blue else White
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 14.dp, vertical = 6.dp
                        ),
                        modifier = Modifier.semantics { contentDescription = "快捷输入：$phrase" }
                    ) {
                        Text(phrase, fontSize = 13.sp, color = if (isSelected) White else T2)
                    }
                }
            }
        }

        // ── 生成结果区 ──
        AnimatedVisibility(
            visible = showResult && result != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val r = result ?: return@AnimatedVisibility

            Column(
                modifier = Modifier.animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)

                Text("生成结果", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = T2)

                if (r.gestures.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⚠️", fontSize = 24.sp)
                            Text(
                                "未找到匹配的手语词汇",
                                fontSize = 14.sp, color = Warn, fontWeight = FontWeight.Medium
                            )
                            if (r.unmatchedWords.isNotEmpty()) {
                                Text(
                                    "未匹配：${r.unmatchedWords.joinToString("、")}",
                                    fontSize = 12.sp, color = T3
                                )
                            }
                        }
                    }
                } else {
                    r.gestures.forEachIndexed { index, gesture ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = White)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (gesture.category) {
                                                    "SOS" -> Red.copy(alpha = 0.1f)
                                                    "情感" -> Warn.copy(alpha = 0.1f)
                                                    else -> Blue.copy(alpha = 0.1f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            when (gesture.category) {
                                                "SOS" -> "🆘"
                                                "问候" -> "👋"
                                                "情感" -> "❤️"
                                                "数字" -> "🔢"
                                                "方向" -> "👉"
                                                "人称" -> "👤"
                                                "时间" -> "⏰"
                                                "疑问" -> "❓"
                                                "医疗" -> "🏥"
                                                else -> "✋"
                                            },
                                            fontSize = 22.sp
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            gesture.word,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = T1
                                        )
                                        Text(gesture.gestureName, fontSize = 12.sp, color = T3)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "${index + 1}/${r.gestures.size}",
                                        fontSize = 11.sp, color = T3
                                    )
                                    Text(
                                        "${gesture.durationMs / 1000.0}s",
                                        fontSize = 11.sp, color = T3
                                    )
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔊", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "可配合 TTS 语音同步播报手语内容",
                                fontSize = 12.sp, color = Green
                            )
                        }
                    }

                    if (r.unmatchedWords.isNotEmpty()) {
                        Text(
                            "未匹配词汇：${r.unmatchedWords.joinToString("、")}",
                            fontSize = 11.sp, color = Warn,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }

        Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)

        // ── 支持词汇库 ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📚 支持词汇库", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = T2)
            IconButton(
                onClick = { showWordList = !showWordList },
                modifier = Modifier.semantics {
                    contentDescription = if (showWordList) "收起词汇列表" else "展开词汇列表"
                }
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = T2)
            }
        }

        AnimatedVisibility(visible = showWordList) {
            val words = remember { SignLanguageGenerator.getSupportedWords() }
            Column {
                Text(
                    "共支持 ${words.size} 个常用词汇",
                    fontSize = 12.sp, color = T3,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.height((words.size / 4 * 36).dp.coerceAtLeast(100.dp)),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(words) { word ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF0F4FF))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(word, fontSize = 12.sp, color = T1, maxLines = 1)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ── 麦克风按钮组件 ──

@Composable
private fun MicButton(
    isListening: Boolean,
    isModelReady: Boolean,
    initProgress: Float,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micPulseScale"
    )

    Box(contentAlignment = Alignment.Center) {
        // 脉冲光圈（录音时）
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(Red.copy(alpha = 0.15f))
            )
        }

        // 初始化进度环
        if (!isModelReady && initProgress > 0f && initProgress < 1f) {
            LinearProgressIndicator(
                progress = initProgress,
                modifier = Modifier.size(56.dp).clip(CircleShape),
                color = Blue,
                trackColor = Color(0xFFE0E0E0)
            )
        }

        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isListening) Red else Blue)
                .semantics {
                    contentDescription = if (isListening) "停止语音输入" else "开始语音输入"
                }
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Refresh else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
