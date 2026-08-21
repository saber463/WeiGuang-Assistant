package com.weiguangplus.ui.screen.transcript

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weiguangplus.data.model.TranscriptRecord
import com.weiguangplus.data.repository.TranscriptRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Blue = Color(0xFF1565C0)
private val Bg = Color(0xFFFAFAFA)
private val T1 = Color(0xFF212121)
private val T2 = Color(0xFF757575)
private val White = Color.White

/**
 * 转录历史列表界面（G4）
 *
 * 展示所有已保存的转录记录（通话/悬浮窗），按时间倒序。
 * 点击记录展开/收起查看全文与摘要。
 *
 * 数据加载：进入时用 rememberCoroutineScope 触发 repository 查询，
 * 状态存于 remember，删除/新增后通过重新进入页面刷新。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranscriptHistoryScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var records by remember { mutableStateOf<List<TranscriptRecord>>(emptyList()) }
    var expandedId by remember { mutableStateOf<Long?>(null) }

    // 进入页面加载一次转录历史
    LaunchedEffect(Unit) {
        val repo = TranscriptRepository(context)
        records = repo.getAllTranscripts()
    }

    fun reload() {
        scope.launch {
            records = TranscriptRepository(context).getAllTranscripts()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("转录历史", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←", color = White, fontSize = 18.sp) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue, titleContentColor = White, navigationIconContentColor = White
                )
            )
        },
        containerColor = Bg
    ) { padding ->
        if (records.isEmpty()) {
            // 空态提示
            Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("暂无转录记录", fontSize = 16.sp, color = T1, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("通话或悬浮窗字幕的转录内容会保存在这里", fontSize = 13.sp, color = T2)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(records) { record ->
                    TranscriptCard(
                        record = record,
                        expanded = expandedId == record.id,
                        onClick = {
                            expandedId = if (expandedId == record.id) null else record.id
                        }
                    )
                }
            }
        }
    }
}

/**
 * 单条转录记录卡片
 *
 * 折叠态：显示类型标签 + 相对时间 + 摘要预览。
 * 展开态：追加显示完整转录文本。
 */
@Composable
private fun TranscriptCard(
    record: TranscriptRecord,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 类型标签
                Text(
                    if (record.type == "call") "📞 通话转写" else "💬 悬浮窗字幕",
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Blue
                )
                // 相对/绝对时间
                Text(formatTime(record.timestamp), fontSize = 12.sp, color = T2)
            }
            Spacer(Modifier.height(8.dp))
            // 摘要预览
            Text(record.summary, fontSize = 14.sp, color = T1, maxLines = if (expanded) Int.MAX_VALUE else 3)
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Text("——— 完整转录 ———", fontSize = 11.sp, color = T2)
                Spacer(Modifier.height(4.dp))
                Text(record.fullText, fontSize = 13.sp, color = T2)
            }
        }
    }
}

/** 时间格式化：今天的显示 HH:mm，否则显示 M月d日 HH:mm */
private fun formatTime(ts: Long): String {
    return try {
        val fmt = SimpleDateFormat("M月d日 HH:mm", Locale.CHINA)
        fmt.format(Date(ts))
    } catch (e: Exception) {
        ts.toString()
    }
}