package com.weiguangplus.ui.screen.signlanguage

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weiguangplus.core.signlanguage.HandLandmark
import com.weiguangplus.core.signlanguage.SignCourseCatalog
import com.weiguangplus.core.signlanguage.SignLanguageCameraManager
import com.weiguangplus.core.signlanguage.SignLanguageCorrector
import com.weiguangplus.data.model.FingerExpectation
import com.weiguangplus.data.model.GestureTarget
import com.weiguangplus.data.model.SignCourseItem

private val Blue = Color(0xFF1565C0)
private val BlueBg = Color(0xFFE3F2FD)
private val Bg = Color(0xFFFAFAFA)
private val T1 = Color(0xFF212121)
private val T2 = Color(0xFF757575)
private val White = Color.White
private val Green = Color(0xFF2E7D32)
private val Red = Color(0xFFC62828)

/** 三 Tab：词典 / 课程 / 练习 */
private val tabs = listOf("词典查询", "学习课程", "练习纠错")

/**
 * G7 手语学习 / 词典 / 纠错模块主界面。
 * 通过三个 Tab 串联词典查询、按难度分级课程、摄像头实时纠错练习，
 * 复用 SignLanguageEngine / SignVectorDB / SignLanguageCameraManager 现有能力。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignLearningScreen(
    onBack: () -> Unit = {}
) {
    var tabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("手语学习", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Text(
                        "←",
                        fontSize = 20.sp,
                        color = White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onBack() }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Bold
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
        ) {
            // 顶部 Tab 切换条
            Row(
                modifier = Modifier.fillMaxWidth().background(Blue),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEachIndexed { index, title ->
                    val selected = index == tabIndex
                    Column(
                        modifier = Modifier
                            .clickable { tabIndex = index }
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            title,
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) White else Color(0xFFB3C7E8)
                        )
                        // 选中下划线
                        Spacer(Modifier.height(4.dp))
                        Box(
                            Modifier
                                .size(width = 24.dp, height = 3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (selected) White else Color.Transparent)
                        )
                    }
                }
            }

            // Tab 内容
            when (tabIndex) {
                0 -> DictionaryTab()
                1 -> CourseTab()
                2 -> PracticeTab()
            }
        }
    }
}

// ───────────────── 词典查询 Tab ─────────────────

/** 词典查询：输入文字 → 匹配词条 → 展示中文词义、动作要点、标准目标 */
@Composable
private fun DictionaryTab() {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(SignCourseCatalog.all) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                results = SignCourseCatalog.search(it)
            },
            singleLine = true,
            placeholder = { Text("输入文字查询手势，如：谢谢 / 救命") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "共 ${results.size} 个词条 · 字典数据来自内置手语词库",
            fontSize = 12.sp, color = T2
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(results, key = { it.id }) { item ->
                DictionaryCard(item, context)
            }
        }
    }
}

/** 单个词典词条卡片：展示手势示意图文字 + 动作要点 */
@Composable
private fun DictionaryCard(item: SignCourseItem, context: Context) {
    // 手势示意：以文本+难度徽章替代图片资源，避免占用绘图素材，指示该词条的手势名
    val target = describeTarget(item.target)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.chinese, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = T1)
                Spacer(Modifier.width(8.dp))
                // 难度徽章
                Box(
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (item.difficulty <= 1) BlueBg else Color(0xFFFFF3E0))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "难度 ${item.difficulty}",
                        fontSize = 11.sp,
                        color = if (item.difficulty <= 1) Blue else Color(0xFFEF6C00)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text("场景·${item.scene}", fontSize = 12.sp, color = T2)
            }
            Spacer(Modifier.height(6.dp))
            Text("动作示意：[${item.id}]  ${target}", fontSize = 13.sp, color = Green)
            Spacer(Modifier.height(6.dp))
            Text(
                "要点：${item.tips}",
                fontSize = 13.sp, color = T1,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** 把标准手势目标转成人读的描述文本，便于词典页面示意 */
private fun describeTarget(target: GestureTarget): String {
    fun finger(e: FingerExpectation) = when (e) {
        FingerExpectation.BENT -> "弯"
        FingerExpectation.STRAIGHT -> "伸"
        FingerExpectation.ANY -> "自由"
    }
    val contact = when (target.thumbIndexContact) {
        true -> "，拇指碰食指"
        false -> "，拇指食指分开"
        null -> ""
    }
    return "拇[${finger(target.thumb)}] 食[${finger(target.index)}] " +
        "中[${finger(target.middle)}] 无[${finger(target.ring)}] 小[${finger(target.pinky)}]$contact"
}

// ───────────────── 学习课程 Tab ─────────────────

/** 课程 Tab：按难度分级展示课程清单，点击词条查看课程详情 */
@Composable
private fun CourseTab() {
    var selected by remember { mutableStateOf<SignCourseItem?>(null) }

    if (selected != null) {
        CourseDetail(selected!!) { selected = null }
    } else {
        CourseList { selected = it }
    }
}

/** 课程列表：难度 1 基础 → 2 进阶 → 3 场景表达 三个学习级别 */
@Composable
private fun CourseList(onSelect: (SignCourseItem) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { CourseHeader() }
        // 按难度升序分组
        listOf(1, 2, 3).forEach { level ->
            val lessons = SignCourseCatalog.groupedByDifficulty[level].orEmpty()
            if (lessons.isNotEmpty()) {
                item {
                    Text(
                        when (level) {
                            1 -> "基础 · 日常用语"
                            2 -> "进阶 · 礼貌表达"
                            else -> "场景 · 综合表达"
                        },
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Blue
                    )
                }
                items(lessons, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(item) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = White)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                item.chinese,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = T1,
                                modifier = Modifier.width(64.dp)
                            )
                            Column(Modifier.weight(1f)) {
                                Text(item.textTranslation, fontSize = 13.sp, color = T2)
                                Spacer(Modifier.height(2.dp))
                                Text("场景·${item.scene}", fontSize = 11.sp, color = T2)
                            }
                            Text("查看 >", fontSize = 13.sp, color = Blue)
                        }
                    }
                }
                item { Spacer(Modifier.height(4.dp)) }
            }
        }
    }
}

/** 课程介绍头部 */
@Composable
private fun CourseHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BlueBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("手语课程", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = T1)
            Spacer(Modifier.height(4.dp))
            Text(
                "从基础手型入门，循序渐进学习常用手语表达。点击任一课程查看动作详解。",
                fontSize = 13.sp, color = T2
            )
        }
    }
}

/** 课程详情：动作要点 + 标准目标 + 引导进入练习 */
@Composable
private fun CourseDetail(item: SignCourseItem, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("←  返回课程列表", fontSize = 13.sp, color = Blue,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable { onBack() }
                .padding(4.dp))
        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(item.chinese, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = T1)
                Spacer(Modifier.height(4.dp))
                Text(item.textTranslation, fontSize = 15.sp, color = Blue)
                Spacer(Modifier.height(12.dp))
                Text("难度", fontSize = 12.sp, color = T2)
                Text("${item.difficulty} / 3", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = T1)
                Spacer(Modifier.height(12.dp))
                Text("动作要点", fontSize = 13.sp, color = T2)
                Spacer(Modifier.height(4.dp))
                Text(item.tips, fontSize = 14.sp, color = T1, lineHeight = 22.sp)
                Spacer(Modifier.height(12.dp))
                Text("标准手型：${describeTarget(item.target)}", fontSize = 13.sp, color = Green)
            }
        }
    }
}

// ───────────────── 练习纠错 Tab ─────────────────

/** 纠错练习 Tab：选词条 → 开启前置摄像头 → 实时关键点纠错评分 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PracticeTab() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var selectedId by remember { mutableStateOf("open_palm") }
    val target = remember(selectedId) { SignCourseCatalog.findById(selectedId) }

    // 摄像头管理器：进入本 Tab 即初始化，简单用于实时取关键点
    val cameraManager = remember { SignLanguageCameraManager(context) }
    // 离开本 Tab 时释放相机，避免长期占用摄像头与线程资源
    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraManager.release()
            } catch (_: Exception) {
                // 释放失败不影响页面退出
            }
        }
    }
    var practicing by remember { mutableStateOf(false) }
    val result by cameraManager.recognitionResult.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 词条选择横排
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("选择要练习的手势", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = T1)
            }
            items(SignCourseCatalog.all, key = { it.id }) { item ->
                val isSel = item.id == selectedId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedId = item.id }
                        .background(if (isSel) BlueBg else Color.Transparent),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isSel) BlueBg else White)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.chinese, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            color = if (isSel) Blue else T1, modifier = Modifier.width(60.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.tips, fontSize = 12.sp, color = T2, maxLines = 2)
                        }
                    }
                }
            }

            // 相机预览 & 实时评分区
            item {
                Spacer(Modifier.height(8.dp))
                // 预览与评分
                if (practicing) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                scaleX = -1f // 镜像前置摄像头，让用户看到自己的手势
                                cameraManager.attach(lifecycleOwner, this)
                                cameraManager.startRecognition()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    RealTimeScore(cameraManager, target, result?.handLandmarks)
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        practicing = !practicing
                        if (!practicing) cameraManager.stopRecognition()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (practicing) "停止练习" else "开始练习（前置摄像头）", fontSize = 14.sp)
                }
            }
            item {
                if (target != null) {
                    Text(
                        "手势要求：${describeTarget(target.target)}",
                        fontSize = 12.sp, color = T2, modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}

/** 实时纠错评分视图：用识别帧的关键点跑纠错器，展示分数与建议 */
@Composable
private fun RealTimeScore(
    cameraManager: SignLanguageCameraManager,
    target: com.weiguangplus.data.model.SignCourseItem?,
    landmarks: List<List<HandLandmark>>?
) {
    val context = LocalContext.current
    var score by remember { mutableIntStateOf(-1) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var detected by remember { mutableStateOf(false) }

    LaunchedEffect(landmarks, target) {
        if (target == null) return@LaunchedEffect
        // 取识别到的主手关键点，跑纠错评估
        val hand = landmarks?.firstOrNull()
        if (hand == null) {
            detected = false
            score = -1
        } else {
            detected = true
            val r = SignLanguageCorrector().correct(target.target, hand)
            if (r != null) {
                score = r.score
                suggestions = r.suggestions.take(3)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (landmarks == null) {
                Text("未检测到手部，请将手放入画面", fontSize = 14.sp, color = T2)
            } else if (!detected || score < 0) {
                Text("等待手势稳定…", fontSize = 14.sp, color = T2)
            } else {
                // 评分
                Text(
                    "正确度  ${score} 分",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        score >= 85 -> Green
                        score >= 60 -> Color(0xFFEF6C00)
                        else -> Red
                    }
                )
                Spacer(Modifier.height(8.dp))
                // 纠正建议
                if (suggestions.isEmpty()) {
                    Text("动作很标准，继续保持！", fontSize = 14.sp, color = Green)
                } else {
                    suggestions.forEach { s ->
                        Text("• $s", fontSize = 13.sp, color = Red, lineHeight = 20.sp)
                    }
                }
            }
        }
    }
}