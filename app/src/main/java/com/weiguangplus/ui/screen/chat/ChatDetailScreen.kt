package com.weiguangplus.ui.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weiguangplus.data.model.ChatMessage
import com.weiguangplus.data.model.QuickPhrase
import com.weiguangplus.data.repository.ChatRepository
import com.weiguangplus.data.repository.QuickPhraseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Blue = Color(0xFF1565C0)
private val Bg = Color(0xFFEDEDED)
private val T1 = Color(0xFF212121)
private val T2 = Color(0xFF757575)
private val T3 = Color(0xFFBDBDBD)
private val White = Color.White
private val GreenWeChat = Color(0xFF07C160)
private val GreenLight = Color(0xFFC8E6C9)
private val SentBubble = Color(0xFFD4EDDA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    contactName: String,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { ChatRepository(context) }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var quickPhrases by remember { mutableStateOf<List<QuickPhrase>>(emptyList()) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        repo.initDemoData()
        messages = repo.getMessages(conversationId)
        // 加载统一快捷短语库（G12），供输入框上方短语栏使用
        quickPhrases = QuickPhraseRepository(context).getAllPhrases()
    }

    // Auto scroll to bottom
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage() {
        val text = inputText.trim()
        if (text.isEmpty() || isSending) return
        isSending = true
        scope.launch {
            val msg = repo.sendMessage(conversationId, text)
            messages = messages + msg
            inputText = ""
            isSending = false
            // Simulate auto-reply after 1-2 seconds
            delay((1000..2000).random().toLong())
            val reply = generateAutoReply(contactName)
            val replyMsg = ChatMessage(
                conversationId = conversationId,
                senderId = "auto",
                senderName = contactName,
                content = reply,
                timestamp = System.currentTimeMillis(),
                isSent = false
            )
            repo.sendMessage(conversationId, reply) // just store it
            messages = messages + replyMsg
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👤", fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(contactName, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    }
                },
                navigationIcon = {
                    Text(
                        "← 返回",
                        modifier = Modifier.clickable(onClick = onBack).padding(horizontal = 16.dp),
                        color = White, fontSize = 15.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenWeChat, titleContentColor = White
                )
            )
        },
        containerColor = Bg
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Message list
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Welcome header
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💬 聊天开始", fontSize = 13.sp, color = T3)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                formatDate(System.currentTimeMillis()),
                                fontSize = 11.sp, color = T3
                            )
                        }
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        isSelf = msg.isSent
                    )
                }
            }

            // 快捷短语栏（G12）：点击填入输入框，统一短语库
            if (quickPhrases.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth()
                        .background(White)
                        .border(0.5.dp, Color(0xFFE0E0E0))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickPhrases, key = { it.id }) { phrase ->
                        Text(
                            phrase.text,
                            fontSize = 12.sp,
                            color = GreenWeChat,
                            modifier = Modifier
                                .background(GreenLight, RoundedCornerShape(20.dp))
                                .clickable {
                                    if (inputText.isBlank()) inputText = phrase.text
                                    else inputText = inputText + phrase.text
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .border(0.5.dp, Color(0xFFE0E0E0))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    textStyle = TextStyle(fontSize = 15.sp, color = T1),
                    cursorBrush = SolidColor(GreenWeChat),
                    singleLine = true,
                    maxLines = 4,
                    decorationBox = { innerTextField ->
                        Box {
                            if (inputText.isEmpty()) {
                                Text("输入消息...", color = T3, fontSize = 15.sp)
                            }
                            innerTextField()
                        }
                    }
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) GreenWeChat else Color(0xFFE0E0E0))
                        .clickable(enabled = inputText.isNotBlank()) { sendMessage() }
                        .semantics { contentDescription = "发送消息" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "↑",
                        fontSize = 18.sp,
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isSelf: Boolean
) {
    val bubbleColor = if (isSelf) GreenLight else White
    val alignment = if (isSelf) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        // Sender name (only for received messages)
        if (!isSelf) {
            Text(
                message.senderName,
                fontSize = 12.sp,
                color = T2,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
        ) {
            if (!isSelf) {
                // Avatar placeholder
                Box(
                    Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 16.sp)
                }
                Spacer(Modifier.width(6.dp))
            }
            Card(
                shape = RoundedCornerShape(
                    topStart = if (isSelf) 16.dp else 4.dp,
                    topEnd = if (isSelf) 4.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                colors = CardDefaults.cardColors(containerColor = bubbleColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        message.content,
                        fontSize = 15.sp,
                        color = T1
                    )
                    Text(
                        formatMsgTime(message.timestamp),
                        fontSize = 11.sp,
                        color = T3,
                        modifier = Modifier.align(if (isSelf) Alignment.End else Alignment.Start)
                            .padding(top = 2.dp)
                    )
                }
            }
            if (isSelf) {
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier.size(32.dp).clip(CircleShape).background(GreenWeChat),
                    contentAlignment = Alignment.Center
                ) {
                    Text("我", fontSize = 11.sp, color = White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatMsgTime(timestamp: Long): String {
    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    return fmt.format(Date(timestamp))
}

private fun formatDate(timestamp: Long): String {
    val f = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
    return f.format(Date(timestamp))
}

private fun generateAutoReply(contactName: String): String {
    val replies = when {
        contactName.contains("妈妈") -> listOf("好的宝贝", "知道了", "你也要注意身体", "晚上早点回来", "路上小心")
        contactName.contains("医生") -> listOf("好的,有问题随时联系", "记得按时复查", "注意饮食", "保持良好作息")
        contactName.contains("王姐") -> listOf("加油练习!", "进步很大", "下次课见", "多练习日常用语")
        contactName.contains("志愿者") -> listOf("好的,周六见", "没问题", "到了联系您", "路上注意安全")
        contactName.contains("社区") -> listOf("欢迎加入!", "一起交流", "有什么问题尽管问")
        contactName.contains("邻居") -> listOf("好的", "不客气", "下次帮你拿", "互帮互助")
        contactName.contains("家人") -> listOf("收到👍", "好的", "辛苦啦", "爱你哟❤️")
        else -> listOf("收到", "好的", "谢谢", "没问题")
    }
    return replies.random()
}
