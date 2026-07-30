package com.weiguangplus.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weiguangplus.core.VoiceAssistantLauncher

private val Blue = Color(0xFF1565C0)
private val BlueBg = Color(0xFFE3F2FD)
private val Bg = Color(0xFFFAFAFA)
private val T1 = Color(0xFF212121)
private val T2 = Color(0xFF757575)
private val White = Color.White

private data class FeatureCard(
    val emoji: String,
    val title: String,
    val desc: String,
    val route: String
)

private val features = listOf(
    FeatureCard("\uD83D\uDC8A", "药品识别", "拍照识别药物信息", "drug"),
    FeatureCard("\uD83D\uDC4B", "手语识别", "手势翻译成文字", "sign"),
    FeatureCard("\uD83D\uDCDE", "来电助手", "通话实时转文字", "call"),
    FeatureCard("\uD83D\uDD14", "提醒设置", "震动+闪光闹钟", "alert"),
    FeatureCard("\uD83D\uDEA8", "一键求救", "SOS紧急求助", "sos"),
    FeatureCard("", "设置", "个人信息与偏好", "settings")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigate: (String) -> Unit = {}
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("微光同行", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue, titleContentColor = White
                )
            )
        },
        containerColor = Bg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 欢迎区
            Card(
                modifier = Modifier.fillMaxWidth()
                    .semantics { contentDescription = "欢迎使用微光同行" },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlueBg)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("让沟通无界限", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = T1)
                    Spacer(Modifier.height(4.dp))
                    Text("帮助听障人士看得见声音、说得出心意", fontSize = 13.sp, color = T2)
                }
            }

            // 语音助手快速唤醒按钮
            if (VoiceAssistantLauncher.isAnyAvailable(context)) {
                Button(
                    onClick = { VoiceAssistantLauncher.launch(context) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE8EAF6),
                        contentColor = Blue
                    )
                ) {
                    Text(
                        "唤醒语音助手（" +
                            VoiceAssistantLauncher.getAvailable(context).firstOrNull() + "）",
                        fontSize = 14.sp, fontWeight = FontWeight.Medium
                    )
                }
            }

            // 功能卡片网格
            features.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    for (feat in row) {
                        FeatureCardView(feat, { onNavigate(feat.route) }, Modifier.weight(1f))
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "微光同行 v1.3 · 为听障人士设计的无障碍助手",
                fontSize = 11.sp, color = T2,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeatureCardView(
    feat: FeatureCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick)
            .semantics { contentDescription = feat.title },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(feat.emoji, fontSize = 28.sp)
            Spacer(Modifier.height(8.dp))
            Text(feat.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = T1)
            Spacer(Modifier.height(4.dp))
            Text(feat.desc, fontSize = 12.sp, color = T2, textAlign = TextAlign.Center)
        }
    }
}
