package com.weiguangplus.ui.screen.rehab

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weiguangplus.core.rehab.PronunciationEvaluator
import com.weiguangplus.core.rehab.RehabCourseCatalog
import com.weiguangplus.core.signlanguage.VoiceRecognizer
import com.weiguangplus.core.tts.TtsController
import com.weiguangplus.data.model.RehabLesson
import com.weiguangplus.data.repository.RehabRepository
import kotlinx.coroutines.launch

private val Blue = Color(0xFF1565C0)
private val BlueBg = Color(0xFFE3F2FD)
private val Bg = Color(0xFFFAFAFA)
private val T1 = Color(0xFF212121)
private val T2 = Color(0xFF757575)
private val White = Color.White
private val Green = Color(0xFF2E7D32)
private val Red = Color(0xFFC62828)
private val Orange = Color(0xFFEF6C00)

/** 双 Tab：训练 / 进度 */
private val tabTitles = listOf("发音训练", "训练进度")

/**
 * G3 言语康复 / 构音训练模块主界面。
 *
 * 训练流：选择课程 → TTS 播放标准发音示范 → 语音识别跟读 → 发音评估引擎评分+纠正建议 → 写入进度。
 * 进度流：展示连续打卡 streak + 近 7 天得分柱状 + 训练历史。
 * 复用 VoiceRecognizer（zh-CN 语音识别）与 TtsController（发音示范）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RehabScreen(
    onBack: () -> Unit = {}
) {
    var tabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("言语康复", fontWeight = FontWeight.Bold) },
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
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Tab 切换
            Row(
                modifier = Modifier.fillMaxWidth().background(Blue),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabTitles.forEachIndexed { index, title ->
                    val selected = index == tabIndex
                    Column(
                        modifier = Modifier.clickable { tabIndex = index }.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            title, fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) White else Color(0xFFB3C7E8)
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            Modifier.size(width = 24.dp, height = 3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (selected) White else Color.Transparent)
                        )
                    }
                }
            }

            when (tabIndex) {
                0 -> TrainingTab()
                1 -> ProgressTab()
            }
        }
    }
}

// ───────────────── 训练 Tab ─────────────────

/** 训练 Tab：课程清单 + 选中后的跟读练习面板 */
@Composable
private fun TrainingTab() {
    var selectedId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 语音识别器：用于跟读后评估
    val recognizer = remember {
        VoiceRecognizer(context).also { it.initialize { _ -> } }
    }
    // 离开页面释放语音资源
    DisposableEffect(Unit) {
        onDispose {
            recognizer.release()
            TtsController.stop()
        }
    }

    val partialText by recognizer.partialText.collectAsStateWithLifecycle()
    var spokenText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var startTime by remember { mutableLongStateOf(0L) }
    var evalResult by remember { mutableStateOf<PronunciationEvaluator.EvaluationResult?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // 请选择课程
        Text("选择训练内容（按难度递进）", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = T1)

        // 课程分级列表
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("单音素", "音节", "词语", "短语").forEach { stage ->
                val lessons = RehabCourseCatalog.all.filter { it.stage == stage }
                if (lessons.isNotEmpty()) {
                    item {
                        Text(
                            stage, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                            color = Blue, modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                    }
                    items(lessons, key = { it.id }) { lesson ->
                        LessonCard(
                            lesson = lesson,
                            selected = lesson.id == selectedId,
                            onClick = {
                                selectedId = lesson.id
                                evalResult = null
                            }
                        )
                    }
                }
            }
        }

        // 练习面板（选中课程后显示）
        val lesson = selectedId?.let { RehabCourseCatalog.findById(it) }
        if (lesson != null) {
            PracticePanel(
                lesson = lesson,
                recognizer = recognizer,
                partialText = partialText,
                spokenText = spokenText,
                isListening = isListening,
                evalResult = evalResult,
                playDemo = {
                    TtsController.initialize(context)
                    TtsController.speak(lesson.target)
                },
                toggleListening = {
                    if (isListening) {
                        recognizer.stopListening()
                        spokenText = recognizer.finalText.value.ifBlank { spokenText }
                    } else {
                        spokenText = ""
                        evalResult = null
                        startTime = System.currentTimeMillis()
                        recognizer.startListening()
                    }
                    isListening = !isListening
                },
                onEvaluate = {
                    spokenText = recognizer.finalText.value.ifBlank { spokenText }
                    val res = PronunciationEvaluator.evaluate(lesson.target, spokenText, lesson.breathDesc)
                    evalResult = res
                    if (res != null) {
                        val dur = System.currentTimeMillis() - startTime
                        scope.launch {
                            RehabRepository(context).recordTraining(
                                lessonId = lesson.id,
                                target = lesson.target,
                                score = res.score,
                                durationMs = dur
                            )
                        }
                    }
                }
            )
        }
    }
}

/** 单个训练课程卡片 */
@Composable
private fun LessonCard(
    lesson: RehabLesson,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (selected) BlueBg else Color.Transparent),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) BlueBg else White)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                lesson.target, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = if (selected) Blue else T1, modifier = Modifier.width(88.dp)
            )
            Column(Modifier.weight(1f)) {
                Text(lesson.mouthDesc, fontSize = 12.sp, color = T2, maxLines = 2)
                Spacer(Modifier.height(2.dp))
                Text("分类·${lesson.category}  难度 ${lesson.difficulty}", fontSize = 11.sp, color = T2)
            }
            Text(if (selected) "练习中" else "选择", fontSize = 12.sp, color = if (selected) Blue else T2)
        }
    }
}

/** 跟读练习面板：示范 + 跟读 + 评分 */
@Composable
private fun PracticePanel(
    lesson: RehabLesson,
    recognizer: VoiceRecognizer,
    partialText: String,
    spokenText: String,
    isListening: Boolean,
    evalResult: PronunciationEvaluator.EvaluationResult?,
    playDemo: () -> Unit,
    toggleListening: () -> Unit,
    onEvaluate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("目标：" + lesson.target, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = T1)
            Spacer(Modifier.height(6.dp))
            Text("口型提示：${lesson.mouthDesc}", fontSize = 13.sp, color = T2)

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = playDemo,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("▶ 听示范", fontSize = 13.sp) }
                Button(
                    onClick = toggleListening,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening) Red else Blue
                    )
                ) { Text(if (isListening) "■ 结束跟读" else "○ 开始跟读", fontSize = 13.sp) }
            }

            Spacer(Modifier.height(12.dp))
            // 实时识别文本
            Text(
                "识别到：" + (if (isListening && partialText.isNotBlank()) partialText else (spokenText.ifBlank { "（点击开始跟读后，对着麦克风朗读）" })),
                fontSize = 14.sp,
                color = if (isListening) Blue else T2
            )

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onEvaluate,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp)
            ) { Text("提交评估", fontSize = 14.sp) }

            // 评估结果
            evalResult?.let { r ->
                Spacer(Modifier.height(14.dp))
                Text(
                    "评分  ${r.score} 分",
                    fontSize = 26.sp, fontWeight = FontWeight.Bold,
                    color = when {
                        r.score >= 85 -> Green
                        r.score >= 60 -> Orange
                        else -> Red
                    }
                )
                r.suggestions.forEach { s ->
                    Spacer(Modifier.height(4.dp))
                    Text("• $s", fontSize = 13.sp, color = if (r.score >= 85) Green else T1)
                }
            }
        }
    }
}

// ───────────────── 进度 Tab ─────────────────

/** 进度 Tab：连续打卡 + 近 7 天得分柱状 + 历史 */
@Composable
private fun ProgressTab() {
    val context = LocalContext.current
    var streak by remember { mutableIntStateOf(0) }
    var series by remember { mutableStateOf<List<RehabRepository.DayStat>>(emptyList()) }
    var records by remember { mutableStateOf<List<com.weiguangplus.data.model.RehabRecord>>(emptyList()) }

    LaunchedEffect(Unit) {
        val repo = RehabRepository(context)
        streak = repo.currentStreak()
        series = repo.recentSeries(7)
        records = repo.getAllRecords()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        // Streak 卡
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BlueBg)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$streak", fontSize = 44.sp, fontWeight = FontWeight.Bold, color = Blue,
                    modifier = Modifier.width(80.dp)
                )
                Column {
                    Text("连续打卡天数", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = T1)
                    Text("坚持每日训练，形成发音习惯", fontSize = 12.sp, color = T2)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("近 7 天得分", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = T1)
        Spacer(Modifier.height(8.dp))
        // 简单柱状图：每根柱代表一天得分
        Row(
            modifier = Modifier.fillMaxWidth().background(White, RoundedCornerShape(14.dp)).padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            series.forEach { day ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier
                            .height((day.score / 100f * 72).dp + 4.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    day.score >= 85 -> Green
                                    day.score >= 60 -> Orange
                                    day.score > 0 -> Blue
                                    else -> Color(0xFFE0E0E0)
                                }
                            )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        day.date.takeLast(5), fontSize = 10.sp, color = T2 // 展示 MM-dd
                    )
                    Text("${day.score}", fontSize = 10.sp, color = T2)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("训练历史", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = T1)
        Spacer(Modifier.height(8.dp))
        if (records.isEmpty()) {
            Text("暂无训练记录，去「发音训练」开始第一次练习吧", fontSize = 13.sp, color = T2, modifier = Modifier.padding(vertical = 8.dp))
        } else {
            records.take(20).forEach { r ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(r.target, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = T1, modifier = Modifier.width(72.dp))
                        Text(r.date, fontSize = 12.sp, color = T2, modifier = Modifier.weight(1f))
                        Text(
                            "${r.score} 分",
                            fontSize = 14.sp, fontWeight = FontWeight.Bold,
                            color = when { r.score >= 85 -> Green; r.score >= 60 -> Orange; else -> Red }
                        )
                    }
                }
            }
        }
    }
}