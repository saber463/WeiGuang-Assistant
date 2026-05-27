/*
 * VoiceAssistantScreen.kt
 *
 * 功能：语音助手控制面板界面
 * - 自动识别当前设备品牌，显示手机型号和制造商信息
 * - 一键唤醒系统语音助手（自动匹配合适的助手应用）
 * - 列出设备上所有支持的系统助手列表（来自 VoiceAssistantLauncher 的预配置清单）
 * - 每个助手卡片显示：名称、安装状态、唤醒按钮
 * - 所有操作通过 TTSManager 播报操作反馈
 *
 * 数据流：
 * - 设备信息：通过 Build.MODEL / Build.MANUFACTURER 读取
 * - 品牌映射：VoiceAssistantLauncher.getAssistantNameByManufacturer()
 * - 助手列表：VoiceAssistantLauncher.getInstalledAssistants(context)
 * - 唤醒操作：VoiceAssistantLauncher.launchAssistant() / launchBestAssistant()
 * - 反馈播报：TTSManager.speakNow()
 *
 * 风格说明：
 * 遵循 MainActivity 和 TTSSettingsScreen 中建立的 Material3 卡片式布局风格，
 * 复用相同的 ScrollPage / HeroCard / SectionTitle / InfoCard / StatusChip 等布局模式。
 */

package com.weiguangchangxing.weiguang_plus.feature.assistant

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.weiguangchangxing.weiguang_plus.core.assistant.AssistantInfo
import com.weiguangchangxing.weiguang_plus.core.assistant.VoiceAssistantLauncher
import com.weiguangchangxing.weiguang_plus.core.tts.TTSManager

/**
 * 语音助手控制面板
 *
 * 布局结构（从上到下）：
 * 1. HeroCard — 标题区：显示"语音助手"大标题 + 品牌匹配结果 + 功能介绍
 * 2. HighlightCard — 设备信息区：手机型号、制造商、已识别的品牌助手
 * 3. 一键唤醒按钮 — 调用 launchBestAssistant 智能匹配已安装助手
 * 4. SectionTitle + 助手列表 — 遍历展示每个预配置的助手信息卡片
 *
 * @param modifier 外部传入的 Modifier，用于父布局控制边距等
 */
@Composable
fun VoiceAssistantScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val ttsState by TTSManager.state.collectAsState()

    // 获取预配置的所有助手列表（含安装状态），仅在首次 composition 时计算
    val assistants = remember { VoiceAssistantLauncher.getInstalledAssistants(context) }

    // 根据 Build.MANUFACTURER 自动匹配品牌对应的助手名称（如 小米→小爱）
    val manufacturerAssistantName = remember { VoiceAssistantLauncher.getAssistantNameByManufacturer() }

    // 反馈文本状态：当值被设置时，LaunchedEffect 会触发 TTS 播报
    var feedbackText by remember { mutableStateOf<String?>(null) }

    // 监听 feedbackText 变化，一旦有新的反馈内容就通过 TTS 播报
    LaunchedEffect(feedbackText) {
        feedbackText?.let { text ->
            if (ttsState.isEnabled && ttsState.isReady) {
                TTSManager.speakNow(text)
            }
            feedbackText = null
        }
    }

    // 主滚动布局，与 TTSSettingsScreen 的 ScrollPage 风格一致
    ScrollPage(modifier = modifier) {
        // ===== 顶部标题区 =====
        HeroCard(
            title = "语音助手",
            subtitle = "已识别品牌：$manufacturerAssistantName",
            body = "自动检测当前设备型号与制造商，一键唤醒系统语音助手。" +
                    "下方列出设备上常见语音助手的安装状态，方便快速调用。"
        )

        // ===== 设备信息区 =====
        SectionTitle("设备信息")
        HighlightCard(
            title = "当前设备",
            value = Build.MODEL,
            note = "制造商：${Build.MANUFACTURER}　｜　品牌匹配：$manufacturerAssistantName"
        )

        // ===== 一键唤醒按钮区 =====
        SectionTitle("一键唤醒")
        OneClickWakeCard(
            isTtsReady = ttsState.isEnabled && ttsState.isReady,
            onWake = {
                val success = VoiceAssistantLauncher.launchBestAssistant(context)
                val brandName = VoiceAssistantLauncher.getAssistantNameByManufacturer()
                feedbackText = if (success) {
                    "正在唤醒${brandName}，请稍候"
                } else {
                    "唤醒失败，请在系统中检查语音助手设置"
                }
            }
        )

        // ===== 系统助手列表区 =====
        SectionTitle("系统助手列表（${assistants.size}）")
        assistants.forEach { assistant: AssistantInfo ->
            AssistantCard(
                assistant = assistant,
                isTtsReady = ttsState.isEnabled && ttsState.isReady,
                onLaunch = {
                    val success = VoiceAssistantLauncher.launchAssistant(context, assistant)
                    feedbackText = if (success) {
                        "正在打开${assistant.name}"
                    } else {
                        "${assistant.name}启动失败"
                    }
                },
                onFeedback = { text: String ->
                    feedbackText = text
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 底部留白，避免最后一张卡片紧贴边缘
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// =========================================================================
// 以下为私有辅助 Composable，遵循 MainActivity / TTSSettingsScreen 的卡片风格
// =========================================================================

/**
 * 可滚动的页面容器
 * 统一所有页面的内边距和纵向间距，与 TTSSettingsScreen 保持一致
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
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

/**
 * 顶部英雄卡片
 * 使用 primaryContainer 背景色，用于页面标题和核心说明
 */
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

/**
 * 高亮信息卡片
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
 * 普通信息卡片
 * 白色背景圆角卡片，用于承载一般内容
 */
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

/**
 * 章节标题
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
 * 状态标签
 * 水平胶囊形状，左侧文本 + 右侧主题色文本，用于展示安装状态等二元信息
 */
@Composable
private fun StatusChip(left: String, right: String) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = left,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = right,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * 一键唤醒卡片
 * 包含一个全宽的大按钮，点击后自动匹配设备上已安装的最佳助手并唤醒
 *
 * @param isTtsReady TTS 是否就绪（影响按钮启用状态和反馈播报）
 * @param onWake 唤醒回调，由父组件处理实际的 launchBestAssistant 调用和反馈
 */
@Composable
private fun OneClickWakeCard(
    isTtsReady: Boolean,
    onWake: () -> Unit
) {
    InfoCard {
        Text(
            text = "点击下方按钮，系统将自动识别并唤醒设备上已安装的最佳语音助手。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "支持小米小爱、华为小艺、OPPO小布、vivo小V、荣耀YOYO、三星Bixby、Google助手",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onWake,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(24.dp),
            enabled = isTtsReady,
            contentPadding = ButtonDefaults.TextButtonContentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(
                text = "唤醒语音助手",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (!isTtsReady) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "提示：TTS 引擎未就绪，操作仍可执行但无语音反馈",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 单个助手信息卡片
 *
 * 布局：左侧展示助手名称 + 安装状态标签，右侧展示唤醒按钮
 * 逻辑：已安装 → 显示"唤醒"按钮且可点击；未安装 → 按钮不可用并提示
 *
 * @param assistant 助手信息（名称、包名、安装状态、启动 Intent）
 * @param isTtsReady TTS 引擎是否就绪
 * @param onLaunch 唤醒该助手的回调
 * @param onFeedback 播放 TTS 反馈文本的回调
 */
@Composable
@Suppress("UNUSED_PARAMETER")
private fun AssistantCard(
    assistant: AssistantInfo,
    isTtsReady: Boolean,
    onLaunch: () -> Unit,
    onFeedback: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (assistant.isInstalled) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：名称 + 状态标签
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = assistant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                StatusChip(
                    left = assistant.packageName,
                    right = if (assistant.isInstalled) "已安装" else "未安装"
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 右侧：唤醒按钮
            if (assistant.isInstalled) {
                Button(
                    onClick = onLaunch,
                    modifier = Modifier.height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Text(
                        text = "唤醒",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                OutlinedButton(
                    onClick = {
                        onFeedback("${assistant.name}未安装，请在应用商店搜索后安装")
                    },
                    modifier = Modifier.height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Text(
                        text = "未安装",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}