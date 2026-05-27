package com.weiguangchangxing.weiguang_plus.feature.transportation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val presetPhrases = listOf(
    "我在这里等您",
    "请打开后备箱",
    "请稍等我马上到",
    "我已到达上车点",
    "请确认目的地",
    "我有听障请打字沟通"
)

@Composable
fun RideHailingHelperScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var pickupLocation by remember { mutableStateOf("当前位置") }
    var dropoffLocation by remember { mutableStateOf("") }
    var vehicleInfo by remember { mutableStateOf("") }
    var generatedText by remember { mutableStateOf("") }

    ScrollPage(modifier = modifier) {
        HeroCard(
            title = "网约车沟通助手",
            subtitle = "文字/手语版行程确认·司机对话转文字"
        )

        SectionTitle("行程信息")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = pickupLocation,
                    onValueChange = { pickupLocation = it },
                    label = { Text("上车地点") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = dropoffLocation,
                    onValueChange = { dropoffLocation = it },
                    label = { Text("下车地点") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = vehicleInfo,
                    onValueChange = { vehicleInfo = it },
                    label = { Text("车牌号/车型") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                Button(
                    onClick = {
                        generatedText = buildString {
                            appendLine("【行程确认】")
                            appendLine("上车地点：$pickupLocation")
                            if (dropoffLocation.isNotBlank()) {
                                appendLine("下车地点：$dropoffLocation")
                            }
                            if (vehicleInfo.isNotBlank()) {
                                appendLine("车辆信息：$vehicleInfo")
                            }
                            appendLine("如有问题请与我文字沟通，我有听障不便接听电话，谢谢！")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "生成行程确认",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (generatedText.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = generatedText,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    copyToClipboard(context, generatedText)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("复制行程确认文字")
                            }
                        }
                    }
                }
            }
        }

        SectionTitle("预设沟通话术")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            presetPhrases.take(2).forEach { phrase ->
                PhraseButton(
                    text = phrase,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        copyToClipboard(context, phrase)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            presetPhrases.drop(2).take(2).forEach { phrase ->
                PhraseButton(
                    text = phrase,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        copyToClipboard(context, phrase)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            presetPhrases.drop(4).take(2).forEach { phrase ->
                PhraseButton(
                    text = phrase,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        copyToClipboard(context, phrase)
                    }
                )
            }
        }

        SectionTitle("紧急话术")
        Button(
            onClick = {
                val emergencyText = buildString {
                    appendLine("【紧急求助】")
                    appendLine("我是听障人士，当前乘坐网约车遇到紧急情况，")
                    appendLine("上车地点：$pickupLocation")
                    if (vehicleInfo.isNotBlank()) {
                        appendLine("车辆信息：$vehicleInfo")
                    }
                    appendLine("请帮我报警！")
                }
                copyToClipboard(context, emergencyText)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Warning, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "紧急求助",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PhraseButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(
            Icons.Filled.ContentCopy,
            contentDescription = null,
            modifier = Modifier.height(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("ride_hailing", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
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
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

@Composable
private fun HeroCard(title: String, subtitle: String) {
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