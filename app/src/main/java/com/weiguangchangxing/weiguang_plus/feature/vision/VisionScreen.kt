/*
 * VisionScreen.kt
 *
 * 功能：视觉识别与障碍物检测集成界面
 * - 两个 Tab 切换：实时物品识别（A）和障碍物检测（B）
 * - Tab A：使用 CameraObjectRecognizer 接入 CameraX 相机预览 + ML Kit 图像标签识别
 * - Tab B：使用 ObstacleDetectionManager 进行实时障碍物检测，联动 TTS 语音播报
 * - 采用 Material3 TabRow + ScrollPage 卡片式布局，与 MainActivity / TTSSettingsScreen 风格一致
 *
 * 数据流：
 * - CameraObjectRecognizer.state (StateFlow) → recognitionState
 * - ObstacleDetectionManager.state (StateFlow) → obstacleState
 * - ObstacleDetectionManager.setDetectedListener → currentObstacles
 * - TTSManager.speakNow() → 障碍物检测结果语音播报
 *
 * 生命周期：
 * - 切换 Tab 时自动停止上一个模块的检测
 * - 每次启动障碍物检测时创建新的 ObstacleDetectionManager 实例，避免 ML Kit 检测器关闭后不可复用
 * - DisposableEffect 在页面销毁时 release 两个管理器
 */

package com.weiguangchangxing.weiguang_plus.feature.vision

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.weiguangchangxing.weiguang_plus.core.tts.TTSManager
import com.weiguangchangxing.weiguang_plus.feature.obstacle.ObstacleData
import com.weiguangchangxing.weiguang_plus.feature.obstacle.ObstacleDetectionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

/**
 * 视觉识别与障碍物检测总面板
 *
 * 布局结构：
 * 1. TabRow — 顶部 Tab 切换栏："实时物品识别" / "障碍物检测"
 * 2. 根据 selectedTab 展示对应的内容区域
 *
 * @param modifier 外部传入的 Modifier，用于父布局控制边距等
 */
@Composable
fun VisionScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("实时物品识别", "障碍物检测")

    val ttsState by TTSManager.state.collectAsState()

    // ==================== 实时物品识别状态 ====================
    val recognizer = remember { CameraObjectRecognizer(context) }
    val recognitionState by recognizer.state.collectAsState()
    val previewView = remember { PreviewView(context) }

    // ==================== 障碍物检测状态 ====================
    var obstacleManager by remember { mutableStateOf<ObstacleDetectionManager?>(null) }
    val obstacleState by (obstacleManager?.state?.collectAsState() ?: remember {
        mutableStateOf(ObstacleDetectionManager.ObstacleDetectionState())
    })
    val currentObstacles = remember { mutableStateListOf<ObstacleData>() }
    var isObstacleDetecting by remember { mutableStateOf(false) }

    // ==================== Tab 切换：停止上一个模块 ====================
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> {
                obstacleManager?.stopDetection()
                obstacleManager = null
                isObstacleDetecting = false
                currentObstacles.clear()
            }
            1 -> {
                recognizer.stopRecognition()
            }
        }
    }

    // ==================== 障碍物检测回调 & TTS 播报 ====================
    LaunchedEffect(obstacleManager) {
        obstacleManager?.setDetectedListener { obstacles ->
            currentObstacles.clear()
            currentObstacles.addAll(obstacles)

            if (ttsState.isEnabled && ttsState.isReady && obstacles.isNotEmpty()) {
                val nearest = obstacles.minByOrNull { it.distance } ?: return@setDetectedListener
                val directionLabel = nearest.direction
                val distanceText = "约${"%.1f".format(nearest.distance)}米"
                val humanText = if (nearest.isHuman) "，有人" else ""
                val ttsMessage = "注意${directionLabel}${distanceText}${humanText}"
                TTSManager.speakNow(ttsMessage)
            }
        }
    }

    // ==================== 页面销毁：释放所有资源 ====================
    DisposableEffect(Unit) {
        onDispose {
            recognizer.release()
            obstacleManager?.release()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = if (selectedTab == index) {
                        Modifier.background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                        )
                    } else {
                        Modifier
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> ObjectRecognitionContent(
                recognizer = recognizer,
                recognitionState = recognitionState,
                previewView = previewView,
                lifecycleOwner = lifecycleOwner
            )
            1 -> ObstacleDetectionContent(
                obstacleState = obstacleState,
                currentObstacles = currentObstacles,
                isObstacleDetecting = isObstacleDetecting,
                onStartDetection = {
                    val newManager = ObstacleDetectionManager(context, lifecycleOwner)
                    obstacleManager = newManager
                    isObstacleDetecting = true
                    currentObstacles.clear()
                    newManager.startDetection()
                },
                onStopDetection = {
                    obstacleManager?.stopDetection()
                    obstacleManager = null
                    isObstacleDetecting = false
                    currentObstacles.clear()
                }
            )
        }
    }
}

// =========================================================================
// Tab A：实时物品识别
// =========================================================================

/**
 * 实时物品识别内容区域
 *
 * 布局：
 * 1. CameraX 实时预览（PreviewView）
 * 2. 当前识别结果卡片（物品名称 + 置信度）
 * 3. 最近识别历史列表
 * 4. 开始/停止控制按钮
 */
@Composable
private fun ObjectRecognitionContent(
    recognizer: CameraObjectRecognizer,
    recognitionState: RecognitionState,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner
) {
    val isRunning = recognitionState.isRunning
    val currentObject = recognitionState.currentTopObject
    val recentObjects = recognitionState.recentObjects
    val errorMessage = recognitionState.errorMessage

    // 根据识别状态启动或暂停相机
    LaunchedEffect(isRunning) {
        if (isRunning) {
            recognizer.setPreviewView(previewView)
            recognizer.startRecognition(lifecycleOwner)
        }
    }

    ScrollPage {
        HeroCard(
            title = "实时物品识别",
            subtitle = "CameraX + ML Kit 图像标签",
            body = "通过后置摄像头实时识别画面中的物品，支持展示当前识别结果和最近5条识别历史。点击下方按钮开始或停止识别。"
        )

        // ---------- 相机预览 ----------
        SectionTitle("相机预览")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // ---------- 当前识别结果 ----------
        SectionTitle("当前识别结果")
        val lowConfidenceThreshold = 0.7f
        if (currentObject != null) {
            if (currentObject.confidence < lowConfidenceThreshold) {
                HighlightCard(
                    title = currentObject.label,
                    value = "置信度较低",
                    note = "识别置信度较低，建议对准更清晰的物体或增加光照"
                )
            } else {
                HighlightCard(
                    title = currentObject.label,
                    value = "置信度 ${"%.1f".format(currentObject.confidence * 100)}%",
                    note = "识别时间：${timeFormat.format(Date(currentObject.timestamp))}"
                )
            }
        } else {
            if (isRunning) {
                HighlightCard(
                    title = "正在识别中",
                    value = "请将相机对准要识别的物品",
                    note = if (errorMessage != null) "系统错误：$errorMessage" else "识别结果将实时显示在此处"
                )
            } else {
                HighlightCard(
                    title = "等待启动",
                    value = "点击下方「开始识别」按钮",
                    note = if (errorMessage != null) "系统错误：$errorMessage" else "启动后相机将自动识别画面中的物品"
                )
            }
        }

        // ---------- 识别历史 ----------
        SectionTitle("最近识别历史")
        if (recentObjects.isEmpty()) {
            InfoCard {
                Text(
                    text = if (isRunning) "尚未识别到物品，请将相机对准目标物体"
                    else "暂无识别记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            InfoCard {
                recentObjects.reversed().forEachIndexed { index, obj ->
                    if (index > 0) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .padding(horizontal = 4.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = obj.label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            if (obj.confidence > 0.01f) {
                                Text(
                                    text = "${"%.1f".format(obj.confidence * 100)}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Text(
                            text = timeFormat.format(Date(obj.timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ---------- 控制按钮 ----------
        SectionTitle("控制")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (isRunning) {
                        recognizer.stopRecognition()
                    } else {
                        recognizer.setPreviewView(previewView)
                        recognizer.startRecognition(lifecycleOwner)
                    }
                },
                modifier = Modifier.weight(1f),
                colors = if (isRunning) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(if (isRunning) "停止识别" else "开始识别")
            }
        }
    }
}

// =========================================================================
// Tab B：障碍物检测
// =========================================================================

/**
 * 障碍物检测内容区域
 *
 * 布局：
 * 1. 检测状态卡片（是否正在检测、错误信息等）
 * 2. 当前障碍物列表（方向、距离、是否有人、标签、置信度）
 * 3. 开始/停止控制按钮
 */
@Composable
private fun ObstacleDetectionContent(
    obstacleState: ObstacleDetectionManager.ObstacleDetectionState,
    currentObstacles: List<ObstacleData>,
    isObstacleDetecting: Boolean,
    onStartDetection: () -> Unit,
    onStopDetection: () -> Unit
) {
    val errorMessage = obstacleState.errorMessage

    ScrollPage {
        HeroCard(
            title = "障碍物检测",
            subtitle = "ML Kit Object Detection + 传感器辅助",
            body = "通过后置摄像头实时检测前方障碍物，识别物体方向、距离和类别（是否有人），检测结果将通过语音播报提示。"
        )

        // ---------- 检测状态 ----------
        SectionTitle("检测状态")
        val statusText = when {
            errorMessage != null -> "检测异常"
            isObstacleDetecting -> "正在检测"
            else -> "未启动"
        }
        val statusNote = when {
            errorMessage != null -> errorMessage
            isObstacleDetecting -> "已检测到 ${currentObstacles.size} 个障碍物"
            else -> "点击下方按钮开始障碍物检测"
        }
        HighlightCard(
            title = "检测器状态",
            value = statusText,
            note = statusNote
        )

        // ---------- 当前障碍物列表 ----------
        SectionTitle("当前障碍物（${currentObstacles.size}）")
        if (currentObstacles.isEmpty()) {
            InfoCard {
                Text(
                    text = if (isObstacleDetecting) "尚未检测到障碍物"
                    else "暂无检测数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            currentObstacles.forEachIndexed { index, obstacle ->
                ObstacleCard(obstacle = obstacle)
                if (index < currentObstacles.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // ---------- 控制按钮 ----------
        SectionTitle("控制")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (isObstacleDetecting) {
                        onStopDetection()
                    } else {
                        onStartDetection()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = if (isObstacleDetecting) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(if (isObstacleDetecting) "停止检测" else "开始检测")
            }
        }
    }
}

/**
 * 单个障碍物信息卡片
 *
 * 显示：方向、距离、标签、是否行人、置信度
 */
@Composable
private fun ObstacleCard(obstacle: ObstacleData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (obstacle.isHuman) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = obstacle.label.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (obstacle.isHuman) {
                    Text(
                        text = "行人",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "方向：${obstacle.direction}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "距离：${"%.1f".format(obstacle.distance)}m",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "置信度：${"%.0f".format(obstacle.confidence * 100)}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =========================================================================
// 以下为私有辅助 Composable，遵循 MainActivity / TTSSettingsScreen 的卡片风格
// =========================================================================

/**
 * 可滚动的页面容器
 *
 * 统一所有页面的内边距和纵向间距，与 TTSSettingsScreen / VoiceAssistantScreen 保持一致
 */
@Composable
private fun ScrollPage(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

/**
 * 顶部英雄卡片
 *
 * 使用 primaryContainer 背景色，用于页面标题和核心说明
 */
@Composable
private fun HeroCard(title: String, subtitle: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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

/**
 * 高亮信息卡片
 *
 * 使用 secondaryContainer 背景色，适合展示状态或关键数据
 */
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

/**
 * 章节标题
 *
 * 在每个功能区块上方使用，保持页面层级清晰
 */
@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 6.dp)
    )
}

/**
 * 普通信息卡片
 *
 * 白色圆角容器，用于包裹列表、说明文字等常规内容
 */
@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}