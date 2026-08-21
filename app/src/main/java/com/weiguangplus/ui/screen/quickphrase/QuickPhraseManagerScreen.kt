package com.weiguangplus.ui.screen.quickphrase

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import com.weiguangplus.data.model.QuickPhrase
import com.weiguangplus.data.repository.QuickPhraseRepository
import kotlinx.coroutines.launch

private val Blue = Color(0xFF1565C0)
private val Bg = Color(0xFFFAFAFA)
private val T1 = Color(0xFF212121)
private val T2 = Color(0xFF757575)
private val White = Color.White
private val Pink = Color(0xFFD32F2F)

/**
 * 快捷短语管理界面（G12）
 *
 * 允许听障用户：
 *  - 查看按场景分组的预设与自定义短语
 *  - 添加自定义短语（输入文本 + 选择分类）
 *  - 删除短语（自定义可自由删，预设亦可删以精简）
 *
 * 数据流：进入时初始化预设短语库 → 加载全量 → 分组展示；
 * 增删后调用仓库刷新列表。
 *
 * 备注：本页沉淀"统一的短语库"，供聊天/通话页面后续复用（QuickPhraseBar 思维）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickPhraseManagerScreen(
    onBack: () -> Unit = {},
    /** 点击某短语时的回调（供外部页面复用；本管理页内可不传） */
    onPhraseClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var grouped by remember {
        mutableStateOf<Map<String, List<QuickPhrase>>>(emptyMap())
    }
    var newText by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("日常") }

    fun reload() {
        scope.launch {
            grouped = QuickPhraseRepository(context).getGrouped()
        }
    }

    // 进入页面加载（含预设初始化）
    LaunchedEffect(Unit) { reload() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("快捷短语", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ===== 添加自定义短语 =====
            item {
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = White)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("添加自定义短语", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = T1)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newText,
                            onValueChange = { newText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("输入短语内容", fontSize = 14.sp) },
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // 分类选择（简化：日常/就医/搜索简化为几个常用分类）
                            for (cat in listOf("日常", "就医", "求助", "出行")) {
                                Button(
                                    onClick = { newCategory = cat },
                                    modifier = Modifier.height(34.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (newCategory == cat) Blue else Color(0xFFE0E0E0),
                                        contentColor = if (newCategory == cat) White else T2
                                    )
                                ) { Text(cat, fontSize = 12.sp) }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val ok = QuickPhraseRepository(context)
                                            .addCustomPhrase(newText, newCategory)
                                        if (ok) {
                                            newText = ""
                                            reload()
                                        }
                                    }
                                },
                                enabled = newText.isNotBlank()
                            ) { Text("添加", fontSize = 14.sp) }
                        }
                    }
                }
            }

            // ===== 分组展示 ==
            grouped.forEach { (category, phrases) ->
                item {
                    Text(category, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Blue,
                        modifier = Modifier.padding(top = 4.dp))
                }
                items(phrases) { phrase ->
                    Row(
                        Modifier.fillMaxWidth().background(White, RoundedCornerShape(10.dp))
                            .clickable { onPhraseClick?.invoke(phrase.text) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            phrase.text,
                            fontSize = 14.sp, color = T1,
                            modifier = Modifier.weight(1f)
                        )
                        // 删除按钮
                        Text(
                            "删除",
                            fontSize = 12.sp, color = Pink,
                            modifier = Modifier.clickable {
                                scope.launch {
                                    QuickPhraseRepository(context).deletePhrase(phrase.id)
                                    reload()
                                }
                            }.padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}