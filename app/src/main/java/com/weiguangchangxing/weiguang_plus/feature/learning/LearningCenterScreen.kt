package com.weiguangchangxing.weiguang_plus.feature.learning

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weiguangchangxing.weiguang_plus.core.tts.TTSManager

private data class CourseCategory(
    val name: String,
    val icon: ImageVector,
    val title: String,
    val description: String,
    val points: List<String>
)

private val courseCategories = listOf(
    CourseCategory(
        name = "交通规则",
        icon = Icons.Filled.Star,
        title = "交通规则",
        description = "了解基本的交通规则和道路安全知识，帮助您在出行时更好地保护自己。",
        points = listOf(
            "过马路时请走斑马线，遵守红绿灯信号",
            "乘坐公交地铁时站稳扶好，注意上下车安全",
            "夜间出行穿浅色或反光衣物，提高可见度",
            "远离大型车辆盲区，保持安全距离"
        )
    ),
    CourseCategory(
        name = "应急避险",
        icon = Icons.Filled.Warning,
        title = "应急避险",
        description = "学习在突发紧急情况下如何快速判断并采取正确的避险措施。",
        points = listOf(
            "地震时寻找坚固掩体，保护头部，远离玻璃窗",
            "火灾时用湿毛巾捂住口鼻，弯腰沿安全通道撤离",
            "遭遇极端天气时尽快进入室内躲避",
            "随身携带紧急联系人信息和必要药品",
            "学会拨打110、119、120等紧急电话"
        )
    ),
    CourseCategory(
        name = "网约车指南",
        icon = Icons.Filled.Info,
        title = "网约车指南",
        description = "安全使用网约车服务的注意事项和实用技巧。",
        points = listOf(
            "上车前核对车牌号、车型和司机信息是否一致",
            "将行程信息分享给亲友，开启自动分享功能",
            "坐在后排座位，系好安全带",
            "遇到异常情况使用APP内紧急求助功能",
            "不要轻易向司机透露个人隐私信息"
        )
    ),
    CourseCategory(
        name = "手语话术",
        icon = Icons.Filled.Favorite,
        title = "手语话术",
        description = "学习常用的手语表达，方便与听障人士沟通或日常使用。",
        points = listOf(
            "你好：右手食指指向对方",
            "谢谢：右手掌心向上，从下巴向外推",
            "对不起：右手握拳，在胸口转动",
            "请帮忙：双手掌心向上，上下摆动",
            "再见：右手五指张开，左右摆动"
        )
    ),
    CourseCategory(
        name = "安全技巧",
        icon = Icons.Filled.School,
        title = "安全技巧",
        description = "提升日常安全意识，掌握实用的自我保护技巧。",
        points = listOf(
            "随身携带哨子或报警器，遇到危险时引起注意",
            "记住常去场所的安全出口位置",
            "手机设置紧急联系人快捷拨打功能",
            "学习基本的自我防卫动作",
            "定期与家人约定安全暗号"
        )
    ),
    CourseCategory(
        name = "设备操作",
        icon = Icons.Filled.Settings,
        title = "设备操作",
        description = "熟悉本应用的各项功能操作，充分发挥辅助工具的作用。",
        points = listOf(
            "TTS语音播报设置：调节语速和音调",
            "手语识别功能：打开前置摄像头进行实时翻译",
            "一键求助：预设场景快速发送求助信息",
            "障碍物检测：使用后置摄像头检测前方障碍物"
        )
    )
)

@Composable
fun LearningCenterScreen(modifier: Modifier = Modifier) {
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    ScrollPage(modifier = modifier) {
        HeroCard(
            title = "安全学习中心",
            subtitle = "图文+手语课程·安全知识随时学"
        )

        SectionTitle("课程分类")

        courseCategories.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { course ->
                    CourseCard(
                        course = course,
                        isExpanded = expandedCategory == course.name,
                        onToggle = {
                            expandedCategory = if (expandedCategory == course.name) null else course.name
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseCard(
    course: CourseCategory,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ttsState by TTSManager.state.collectAsState()
    var isCurrentlySpeaking by remember { mutableStateOf(false) }
    val ttsReady = ttsState.isReady && ttsState.isEnabled

    Card(
        onClick = onToggle,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = course.icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .height(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = course.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = course.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    course.points.forEach { point ->
                        Text(
                            text = "• $point",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (!ttsReady) return@Button
                            isCurrentlySpeaking = true
                            val ttsText = buildString {
                                appendLine(course.title)
                                appendLine(course.description)
                                course.points.forEach { point ->
                                    appendLine(point)
                                }
                            }
                            TTSManager.speakNow(ttsText, onDone = {
                                isCurrentlySpeaking = false
                            })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = ttsReady
                    ) {
                        Text(
                            text = when {
                                isCurrentlySpeaking -> "正在朗读..."
                                !ttsReady -> "语音引擎未就绪"
                                else -> "朗读此课程"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
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