package com.weiguangplus.ui.screen.call

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weiguangplus.core.call.CallState
import com.weiguangplus.ui.viewmodel.CallViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallAssistantScreen(
    viewModel: CallViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.registerCallListener()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "来电助手",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = when (state.callState) {
                        CallState.RINGING -> Color(0xFF4CAF50)
                        CallState.ANSWERED -> Color(0xFF2196F3)
                        else -> MaterialTheme.colorScheme.surface
                    }
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 通话状态提示
            CallStatusBanner(state.callState, state.incomingNumber)

            // 来电响铃时的动作提示
            if (state.callState == CallState.RINGING) {
                Text(
                    "有来电 - 请接听后将开启实时字幕",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            // 实时转录文字区域
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .semantics { contentDescription = "通话实时字幕区域" },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A2E)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "实时字幕",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.weight(1f))
                        if (state.callState == CallState.ANSWERED) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 4.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                "收听中",
                                color = Color(0xFF4CAF50),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = state.liveText.ifEmpty { "等待通话开始..." },
                        color = Color.White,
                        fontSize = 18.sp,
                        lineHeight = 28.sp
                    )
                }
            }

            // 快捷短语
            Text(
                "快捷回复",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.quickPhrases) { phrase ->
                    FilledTonalButton(
                        onClick = { viewModel.onQuickPhraseClick(phrase) },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(phrase, maxLines = 1, fontSize = 13.sp)
                    }
                }
            }

            // 手动输入回复
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.myDraftText,
                    onValueChange = viewModel::onDraftTextChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入你要说的话...") },
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = viewModel::onSendText,
                    enabled = state.myDraftText.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "发送文字转语音",
                        tint = if (state.myDraftText.isNotBlank())
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun CallStatusBanner(state: CallState, number: String?) {
    val (bgColor, text, icon) = when (state) {
        CallState.IDLE -> Triple(
            Color(0xFFE0E0E0),
            "等待来电...",
            "闲置"
        )
        CallState.RINGING -> Triple(
            Color(0xFFE8F5E9),
            "来电: ${number ?: "未知"}",
            "响铃中"
        )
        CallState.ANSWERED -> Triple(
            Color(0xFFE3F2FD),
            "通话中 - 对方: ${number ?: "未知"}",
            "已接通"
        )
        CallState.DIALING -> Triple(
            Color(0xFFFFF3E0),
            "正在拨出...",
            "拨号中"
        )
        CallState.ENDED -> Triple(
            Color(0xFFFFEBEE),
            "通话已结束",
            "已挂断"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Phone,
                contentDescription = null,
                tint = when (state) {
                    CallState.RINGING -> Color(0xFF4CAF50)
                    CallState.ANSWERED -> Color(0xFF2196F3)
                    CallState.ENDED -> Color(0xFFF44336)
                    else -> Color.Gray
                }
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(text, fontWeight = FontWeight.Bold)
                Text(
                    icon,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
