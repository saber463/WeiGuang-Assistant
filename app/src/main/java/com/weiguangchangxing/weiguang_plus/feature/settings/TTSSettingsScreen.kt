package com.weiguangchangxing.weiguang_plus.feature.settings

import android.speech.tts.Voice
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weiguangchangxing.weiguang_plus.core.tts.TTSManager

/**
 * TTS 语音播报设置界面
 *
 * 功能：
 * - 展示 TTS 全局开关，允许用户启用或禁用语音播报
 * - 提供语速调节滑块（0.25x ~ 2.0x），实时生效
 * - 提供音调调节滑块（0.5x ~ 2.0x），实时生效
 * - 测试播报按钮，点击后播报一段预设文本供用户验证效果
 * - 显示当前 TTS 引擎状态（是否就绪、是否正在播报）
 * - 列出设备上可用的中文语音引擎
 *
 * 数据流：
 * - 通过 TTSManager.state StateFlow 收集实时状态
 * - 所有调节操作直接委托给 TTSManager 单例，不维护本地副本状态
 * - 可用语音列表通过 TTSManager.getAvailableVoices() 获取
 */
@Composable
fun TTSSettingsScreen(modifier: Modifier = Modifier) {
    val ttsState by TTSManager.state.collectAsState()
    val availableVoices = remember { TTSManager.getAvailableVoices() }

    ScrollPage(modifier = modifier) {
        HeroCard(
            title = "TTS 语音播报设置",
            subtitle = "全局开关与参数调节",
            body = "在这里可以统一控制微光畅行的语音播报行为。关闭全局开关后，所有页面将不再主动播报；语速和音调调节会实时生效，建议戴耳机测试以获得最佳体验。"
        )

        SectionTitle("全局开关")
        InfoCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "启用 TTS 语音播报",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (ttsState.isEnabled) "当前已开启" else "当前已关闭",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = ttsState.isEnabled,
                    onCheckedChange = { enabled: Boolean -> TTSManager.setEnabled(enabled) }
                )
            }
        }

        SectionTitle("播报参数调节")
        InfoCard {
            SliderRow(
                label = "语速",
                valueText = "%.2fx".format(ttsState.speed),
                value = ttsState.speed,
                valueRange = 0.25f..2.0f,
                steps = 34,
                onValueChange = { speed: Float -> TTSManager.setSpeed(speed) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            SliderRow(
                label = "音调",
                valueText = "%.2fx".format(ttsState.pitch),
                value = ttsState.pitch,
                valueRange = 0.5f..2.0f,
                steps = 29,
                onValueChange = { pitch: Float -> TTSManager.setPitch(pitch) }
            )
        }

        SectionTitle("测试播报")
        InfoCard {
            Text(
                text = "点击下方按钮可播报测试文本，用于验证语速和音调设置是否达到预期效果。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        TTSManager.speakNow(
                            text = "你好，欢迎使用微光畅行。当前语音播报功能测试正常，语速音调已按你的设置生效。"
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = ttsState.isEnabled && ttsState.isReady,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("播放测试语音")
                }
                OutlinedButton(
                    onClick = { TTSManager.stop() },
                    modifier = Modifier.weight(1f),
                    enabled = ttsState.isSpeaking
                ) {
                    Text("停止播报")
                }
            }
        }

        SectionTitle("当前状态")
        HighlightCard(
            title = "TTS 引擎状态",
            value = buildStatusText(ttsState.isReady, ttsState.isSpeaking, ttsState.isEnabled),
            note = ttsState.errorMessage ?: buildStatusNote(ttsState.isReady, ttsState.isSpeaking, ttsState.isEnabled)
        )

        SectionTitle("可用语音引擎（${availableVoices.size}）")
        if (availableVoices.isEmpty()) {
            InfoCard {
                Text(
                    text = "暂未获取到可用的中文语音引擎，请确认系统 TTS 设置中已下载中文语音数据。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            InfoCard {
                availableVoices.forEach { voice ->
                    VoiceItem(voice = voice)
                }
            }
        }
    }
}

@Composable
private fun buildStatusText(isReady: Boolean, isSpeaking: Boolean, isEnabled: Boolean): String {
    return when {
        !isEnabled -> "语音播报已关闭"
        !isReady -> "引擎未就绪"
        isSpeaking -> "正在播报中"
        else -> "引擎已就绪，等待播报"
    }
}

@Composable
private fun buildStatusNote(isReady: Boolean, isSpeaking: Boolean, isEnabled: Boolean): String {
    return when {
        !isEnabled -> "全局开关已关闭，所有页面将不再主动触发语音播报。"
        !isReady -> "当前系统 TTS 引擎尚未完成初始化，请检查系统设置中是否已下载中文语音数据。"
        isSpeaking -> "当前有语音正在播报，可通过「停止播报」按钮中断。"
        else -> "所有播报参数已就绪，可随时通过测试播报按钮验证效果。"
    }
}

@Composable
private fun VoiceItem(voice: Voice) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = voice.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            StatusChip(
                left = if (voice.isNetworkConnectionRequired) "在线" else "本地",
                right = buildVoiceQualityLabel(voice.quality)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "语种: ${voice.locale.displayName}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun buildVoiceQualityLabel(quality: Int): String {
    return when (quality) {
        Voice.QUALITY_VERY_HIGH -> "极高"
        Voice.QUALITY_HIGH -> "高"
        Voice.QUALITY_NORMAL -> "普通"
        Voice.QUALITY_LOW -> "低"
        else -> "未知"
    }
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
        verticalArrangement = Arrangement.spacedBy(20.dp),
        content = content
    )
}

@Composable
private fun HeroCard(title: String, subtitle: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
private fun HighlightCard(title: String, value: String, note: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
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

@Composable
private fun SliderRow(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}