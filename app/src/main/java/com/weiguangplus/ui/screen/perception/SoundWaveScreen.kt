package com.weiguangplus.ui.screen.perception

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weiguangplus.core.perception.SoundWaveformAnalyzer

private val Blue = Color(0xFF1565C0)
private val BlueBg = Color(0xFFE3F2FD)
private val Bg = Color(0xFFFAFAFA)
private val T1 = Color(0xFF212121)
private val T2 = Color(0xFF757575)
private val White = Color.White

/**
 * G10 声音波形可视化界面
 *
 * 调用 [SoundWaveformAnalyzer] 实时采集麦克风，在 Canvas 上把 RMS 幅度缓冲
 * 绘制成随时间推进的波形竖条，并展示当前分贝声级（-60~0 dB）。
 * 作为 G1 环境音监控的附加可视化能力，让用户"看得见"周围的声音强弱。
 *
 * 交互：点击"开始"开启采集实时绘制；离开页面自动停止并释放分析器。
 * 说明：本页为独立可视化，不改变 AmbientSoundMonitor 的监控/提醒逻辑。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundWaveScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    // 分析器生命随本页管理：进入创建，离开释放
    val analyzer = remember { SoundWaveformAnalyzer(context.applicationContext) }
    DisposableEffect(Unit) {
        onDispose { analyzer.release() }
    }

    var running by remember { mutableStateOf(false) }
    val levels by analyzer.levels.collectAsStateWithLifecycle()
    val db by analyzer.dbLevel.collectAsStateWithLifecycle()

    // 是否已有录音权限
    val hasPerm = ContextCompat.checkSelfPermission(
        context, Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("声音波形", fontWeight = FontWeight.Bold) },
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 当前声级卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlueBg)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("当前声级", fontSize = 13.sp, color = T2)
                    Text(
                        if (running) "${db.toInt()} dB" else "—",
                        fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Blue
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // 波形画布
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF10243B))
            ) {
                WaveformCanvas(levels, running)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                if (running) "正在聆听周围的声音…" else "点击下方按钮开始，对着麦克风说话或制造声音",
                fontSize = 13.sp, color = if (running) Blue else T2
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (running) {
                        analyzer.stop()
                        running = false
                    } else if (hasPerm) {
                        running = analyzer.start()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (running) "■ 停止" else "● 开始可视化", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }

            if (!hasPerm) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "未获得录音权限，无法采集声音波形。请到系统设置授予「录音」权限。",
                    fontSize = 12.sp, color = Color(0xFFC62828)
                )
            }
        }
    }
}

/** 波形画布：按缓冲绘制竖条，值越大越高，颜色随高度渐变 */
@Composable
private fun WaveformCanvas(levels: List<Float>, running: Boolean) {
    val bgBrush = Brush.verticalGradient(listOf(Color(0xFF0D1B2A), Color(0xFF10243B)))

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(bgBrush, size = size)
        if (levels.isEmpty()) return@Canvas

        val n = levels.size
        val gap = 3.dp.toPx()
        val barWidth = (size.width - gap * (n - 1)) / n
        val baseY = size.height - 8.dp.toPx()   // 底部基线（留边）
        val maxH = size.height - 16.dp.toPx()   // 最大高度范围

        // 自左向右绘制，最右为最新（实时反馈最靠近右侧）
        levels.forEachIndexed { i, v ->
            val h = (v.coerceIn(0f, 1f) * maxH).coerceAtLeast(if (running) 2f else 0f)
            val x = gap + i * (barWidth + gap)
            drawRoundRect(
                color = if (v > 0.01f) getLevelColor(v) else Color(0xFF26384A),
                topLeft = Offset(x, baseY - h),
                size = Size(barWidth, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f)
            )
        }
    }
}

/** 根据幅度取波形竖条颜色（低位深、高位亮） */
private fun getLevelColor(level: Float): Color {
    return when {
        level < 0.25f -> Color(0xFF4FC3F7)
        level < 0.5f -> Color(0xFF29B6F6)
        level < 0.75f -> Color(0xFF1E88E5)
        else -> Color(0xFF90CAF9)
    }
}