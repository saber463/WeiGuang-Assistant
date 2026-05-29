/**
 * 文件名：DrugRecognitionScreen.kt
 * 作者：微光同行前端团队
 * 功能描述：药品识别主界面（相机拍照 + 相册选择 + 识别结果展示）
 * 创建日期：2026-05-29
 * 所属模块：ui/screen/drug（药品界面层）
 *
 * 核心功能：
 * 1. 相机拍照按钮 → 调用CameraX拍照 → 自动上传识别
 * 2. 相册选择按钮 → 从图库选择图片 → 上传识别
 * 3. 识别结果卡片展示（药品名称、风险等级、关键信息）
 * 4. 历史记录列表（分页加载，点击可查看详情）
 * 5. TTS语音播报按钮（无障碍支持）
 *
 * 状态管理：
 * - Idle：初始状态，显示操作引导
 * - Loading：正在上传和识别，显示进度动画
 * - Success：识别成功，显示药品详情卡片
 * - Error：识别失败，显示错误提示和重试按钮
 *
 * 无障碍特性：
 * - 大按钮触控目标（最小48dp）
 * - 图片内容描述（Alt Text）
 * - 结果文字TalkBack播报
 * - 振动反馈配合视觉提示
 */

package com.weiguangplus.ui.screen.drug

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.weiguangplus.data.model.Drug
import com.weiguangplus.ui.viewmodel.DrugViewModel

/**
 * 药品识别主屏幕Composable函数
 *
 * 这是药品识别功能的入口界面，
 * 提供拍照/选图入口和结果展示区域。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugRecognitionScreen(
    modifier: Modifier = Modifier,
    viewModel: DrugViewModel = hiltViewModel(),
    onNavigateToDetail: (Long) -> Unit = {}
) {
    val recognitionState by viewModel.recognitionState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.recognitionEvent.collect { event ->
            // 处理导航等一次性事件
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .semantics { contentDescription = "药品识别界面" },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 页面标题
        Text(
            text = "药品识别",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 操作按钮区（相机 + 相册）
        ActionButtonsRow(
            onCameraClick = { /* TODO: 调用CameraX拍照 */ },
            onGalleryClick = { /* TODO: 打开系统相册选择器 */ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 识别结果展示区（根据状态渲染不同UI）
        when (val state = recognitionState) {
            is DrugViewModel.RecognitionUiState.Idle -> {
                IdleStatePlaceholder()
            }
            is DrugViewModel.RecognitionUiState.Loading -> {
                LoadingStateIndicator()
            }
            is DrugViewModel.RecognitionUiState.Success -> {
                DrugResultCard(drug = state.drug, onClick = {
                    onNavigateToDetail(state.drug.id)
                })
            }
            is DrugViewModel.RecognitionUiState.Error -> {
                ErrorStateView(
                    errorMessage = state.errorMessage,
                    onRetry = { viewModel.resetRecognition() }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 历史记录列表（可选，可折叠展开）
        HistorySection(viewModel = viewModel, onNavigateToDetail)
    }
}

/**
 * 操作按钮行（相机 + 相册）
 *
 * 两个并排的大按钮，方便用户快速选择图片来源。
 */
@Composable
private fun ActionButtonsRow(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onCameraClick,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .semantics { contentDescription = "打开相机拍照" },
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("拍照识别")
        }

        OutlinedButton(
            onClick = onGalleryClick,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .semantics { contentDescription = "从相册选择图片" },
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("相册选择")
        }
    }
}

/**
 * 空闲状态占位符（初始状态引导用户操作）
 */
@Composable
private fun IdleStatePlaceholder() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "📷",
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "点击上方按钮拍摄或选择药盒照片",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 加载状态指示器（识别进行中）
 */
@Composable
private fun LoadingStateIndicator() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "正在识别药品信息...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 错误状态视图（识别失败提示）
 */
@Composable
private fun ErrorStateView(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.size(4.dp))
                Text("重新拍照")
            }
        }
    }
}

/**
 * 药品识别结果卡片
 *
 * 展示识别出的药品核心信息，
 * 使用颜色编码表示风险等级。
 */
@Composable
private fun DrugResultCard(
    drug: Drug,
    onClick: () -> Unit
) {
    val riskLevel = drug.getHighestRiskLevel()

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "药品识别结果：${drug.genericName}" },
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = when (riskLevel) {
                "high" -> MaterialTheme.colorScheme.errorContainer
                "medium" -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 药品缩略图
                if (!drug.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = drug.imageUrl,
                        contentDescription = "${drug.genericName}包装盒照片",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(end = 12.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = drug.genericName ?: "未知药品",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    drug.tradeName?.let {
                        Text(
                            text = "商品名：$it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    RiskBadge(level = riskLevel)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (drug.indication != null) {
                Text(
                    text = "适应症：${drug.indication}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }

            if (drug.riskPrompts.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                drug.riskPrompts.forEach { prompt ->
                    Text(
                        text = "⚠ $prompt",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * 风险等级标签组件
 *
 * 使用不同颜色和文字标识风险级别。
 */
@Composable
private fun RiskBadge(level: String) {
    val (text, color) = when (level) {
        "high" -> "高风险" to Color.Red
        "medium" -> "中风险" to Color(0xFFFF9800)  // 橙色
        else -> "低风险" to Color(0xFF4CAF50)       // 绿色
    }

    androidx.compose.material3.Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * 历史记录列表区域
 */
@Composable
private fun HistorySection(
    viewModel: DrugViewModel,
    onNavigateToDetail: (Long) -> Unit
) {
    val historyState by viewModel.historyState.collectAsStateWithLifecycle()

    Column {
        Text(
            text = "识别历史",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),  // 限制高度避免占用过多空间
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(historyState.records) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "历史记录：${record.drugName}"
                        },
                    onClick = { record.drugId?.let { onNavigateToDetail(it) } }
                ) {
                    Text(
                        text = "${record.drugName ?: "未识别"} - ${record.status}",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
