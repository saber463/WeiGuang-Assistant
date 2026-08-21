package com.weiguangplus.ui.screen.accessibility

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weiguangplus.AccessibilityReadService

private val Blue = Color(0xFF1565C0)
private val BlueBg = Color(0xFFE3F2FD)
private val Bg = Color(0xFFFAFAFA)
private val T1 = Color(0xFF212121)
private val T2 = Color(0xFF757575)
private val White = Color.White
private val Green = Color(0xFF2E7D32)

/**
 * 无障碍朗读设置页
 *
 * 说明「无障碍朗读」的功能并引导开启：
 * - 展示服务当前是否已启用（检测已开启的无障碍服务列表中是否包含本服务）；
 * - 未启用时提供按钮跳转到系统「无障碍设置」，由用户手动开启
 *   （Android 无障碍服务禁止应用内静默授予，只能在系统设置开启）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityReadScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(false) }

    // 进入页面时检测一次服务是否已启用；回前台时由用户手动刷新
    LaunchedEffect(Unit) {
        enabled = AccessibilityReadService.isServiceEnabled(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("无障碍朗读", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Text(
                        "←", fontSize = 20.sp, color = White, fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onBack() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                },
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
                .verticalScroll(rememberScrollState())
        ) {
            // 状态卡
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (enabled) Color(0xFFE8F5E9) else BlueBg
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        if (enabled) "● 服务已开启" else "● 服务未开启",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = if (enabled) Green else T1
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (enabled) "正在朗读收到的通知，以及你选中的文字" else "开启后即可自动朗读收到的通知内容",
                        fontSize = 13.sp, color = T2
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("功能说明", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = T1)
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("• 收到系统通知（来信、短信、闹钟）时自动朗读", fontSize = 14.sp, color = T1)
                    Spacer(Modifier.height(8.dp))
                    Text("• 在可编辑文本中选中的文字会立即朗读", fontSize = 14.sp, color = T1)
                    Spacer(Modifier.height(8.dp))
                    Text("• 使用独立的语音引擎，与应用内其他 TTS 互不打扰", fontSize = 14.sp, color = T1)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("如何开启", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = T1)
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. 点击下方按钮进入「无障碍」设置", fontSize = 14.sp, color = T1)
                    Spacer(Modifier.height(6.dp))
                    Text("2. 在「已安装的服务」中找到并开启「WeiguangPlus 无障碍朗读」", fontSize = 14.sp, color = T1)
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    // 跳转系统无障碍设置总列表（该 action 各版本稳定可用）
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (enabled) "服务已开启，点此查看系统无障碍设置" else "前往系统无障碍设置开启", fontSize = 14.sp)
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "开启后回到本页，若状态未刷新请重新进入本页面查看。",
                fontSize = 12.sp, color = T2
            )
        }
    }
}