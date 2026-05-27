package com.weiguangchangxing.weiguang_plus

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview as CameraXPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Hearing
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SettingsVoice
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.weiguangchangxing.weiguang_plus.core.ProductCategory
import com.weiguangchangxing.weiguang_plus.core.emergency.EmergencyContactManager
import com.weiguangchangxing.weiguang_plus.core.perception.FusionPerceptionEngine
import com.weiguangchangxing.weiguang_plus.core.tts.TTSManager
import com.weiguangchangxing.weiguang_plus.core.assistant.VoiceAssistantLauncher
import com.weiguangchangxing.weiguang_plus.feature.notification.AlertLevel
import com.weiguangchangxing.weiguang_plus.feature.notification.TripleAlertSystem
import com.weiguangchangxing.weiguang_plus.data.local.DrugDatabaseProvider
import com.weiguangchangxing.weiguang_plus.data.repository.DrugInfo
import com.weiguangchangxing.weiguang_plus.data.repository.DrugRepository
import com.weiguangchangxing.weiguang_plus.feature.assistant.VoiceAssistantScreen
import com.weiguangchangxing.weiguang_plus.feature.category.CategorySelectionScreen
import com.weiguangchangxing.weiguang_plus.feature.emergency.EmergencyHelpScreen
import com.weiguangchangxing.weiguang_plus.feature.learning.LearningCenterScreen
import com.weiguangchangxing.weiguang_plus.feature.map.AccessibleMapScreen
import com.weiguangchangxing.weiguang_plus.feature.map.FacilityReportScreen
import com.weiguangchangxing.weiguang_plus.feature.service.DeviceApplyScreen
import com.weiguangchangxing.weiguang_plus.feature.service.ServiceBookingScreen
import com.weiguangchangxing.weiguang_plus.feature.settings.TTSSettingsScreen
import com.weiguangchangxing.weiguang_plus.feature.signlanguage.DualSignScreen
import com.weiguangchangxing.weiguang_plus.feature.signlanguage.SignScreen
import com.weiguangchangxing.weiguang_plus.feature.transportation.BusMetroScreen
import com.weiguangchangxing.weiguang_plus.feature.transportation.RideHailingHelperScreen
import com.weiguangchangxing.weiguang_plus.feature.vision.VisionScreen
import com.weiguangchangxing.weiguang_plus.ui.theme.WeiguangplusTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TTSManager.initialize(this)
        FusionPerceptionEngine.initialize(this)
        EmergencyContactManager.init(this)
        setContent {
            WeiguangplusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeiguangPlusApp()
                }
            }
        }
    }
}

// 主分类（底部导航栏显示）
private enum class MainCategory(
    val label: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector,
    val subSections: List<AppSection>
) {
    HOME("总览", Icons.Filled.Home, Icons.Outlined.Home, listOf(AppSection.HOME)),
    COMMUNICATION("沟通", Icons.Filled.RecordVoiceOver, Icons.Outlined.RecordVoiceOver,
        listOf(AppSection.SIGN, AppSection.DIALOGUE, AppSection.ASSISTANT)
    ),
    DAILY("日常", Icons.Filled.Medication, Icons.Outlined.Medication,
        listOf(AppSection.MEDICINE, AppSection.VISION, AppSection.LEARNING)
    ),
    TRAVEL("出行", Icons.Filled.DirectionsBus, Icons.Outlined.DirectionsBus,
        listOf(AppSection.TRANSPORT)
    ),
    SAFETY("安全", Icons.Filled.Warning, Icons.Outlined.Warning,
        listOf(AppSection.EMERGENCY, AppSection.ALARM, AppSection.TTS, AppSection.SERVICE)
    )
}

// 子功能页面
private enum class AppSection(
    val label: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector
) {
    HOME("总览", Icons.Filled.Home, Icons.Outlined.Home),
    SIGN("小玉手语", Icons.Filled.RecordVoiceOver, Icons.Outlined.RecordVoiceOver),
    DIALOGUE("双向对话", Icons.Filled.Person, Icons.Outlined.Person),
    ASSISTANT("语音助手", Icons.Filled.SettingsVoice, Icons.Outlined.SettingsVoice),
    MEDICINE("药品识别", Icons.Filled.Medication, Icons.Outlined.Medication),
    VISION("视觉辅助", Icons.Filled.Visibility, Icons.Outlined.Visibility),
    LEARNING("学习中心", Icons.Filled.School, Icons.Outlined.School),
    TRANSPORT("出行辅助", Icons.Filled.DirectionsBus, Icons.Outlined.DirectionsBus),
    EMERGENCY("应急求助", Icons.Filled.Warning, Icons.Outlined.Warning),
    ALARM("强提醒", Icons.Filled.Alarm, Icons.Outlined.Alarm),
    TTS("语音设置", Icons.Filled.Hearing, Icons.Outlined.Hearing),
    SERVICE("服务中心", Icons.Filled.Star, Icons.Outlined.Star)
}

private data class ModuleSummary(
    val title: String,
    val stage: String,
    val target: String,
    val value: String,
    val body: String
)

private data class AlarmPattern(
    val title: String,
    val description: String,
    val timings: LongArray,
    val amplitudes: IntArray
)

private data class CameraOcrAnalysisResult(
    val rawText: String,
    val approvalNo: String?,
    val queryKeyword: String?,
    val strategyLabel: String
)

private data class VibrationCapability(
    val hasVibrator: Boolean,
    val hasAmplitudeControl: Boolean,
    val canDirectlyChangeMotorFrequency: Boolean,
    val note: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeiguangPlusApp() {
    val context = LocalContext.current
    var showCategorySelection by remember { mutableStateOf(true) }
    var selectedProductCategory by remember { mutableStateOf<ProductCategory?>(null) }
    var permissionsRequested by remember { mutableStateOf(false) }

    // 底部主分类选中状态
    var selectedMainCategory by rememberSaveable { mutableIntStateOf(0) }
    // 当前子页面选中状态
    var currentSection by rememberSaveable { mutableStateOf(AppSection.HOME) }

    // 定义需要申请的权限
    val requiredPermissions = remember {
        mutableListOf<String>().apply {
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.RECORD_AUDIO)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.READ_PHONE_STATE)
            }
        }
    }

    // 权限申请 Launcher
    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        val saved = ProductCategory.getSavedCategory(context)
        if (saved != ProductCategory.GENERAL || ProductCategory.getSavedCategory(context) != ProductCategory.GENERAL) {
            selectedProductCategory = saved
            showCategorySelection = false
        }
    }

    LaunchedEffect(showCategorySelection) {
        if (!showCategorySelection && !permissionsRequested) {
            permissionsRequested = true
            requestPermissionsLauncher.launch(requiredPermissions.toTypedArray())
        }
    }

    if (showCategorySelection) {
        CategorySelectionScreen(onCategorySelected = { category ->
            selectedProductCategory = category
            showCategorySelection = false
        })
        return
    }

    val productCategory = selectedProductCategory ?: ProductCategory.GENERAL
    val currentMainCategory = MainCategory.entries[selectedMainCategory]
    val subSections = currentMainCategory.subSections

    val appContext = context.applicationContext
    var drugRepository by remember { mutableStateOf<DrugRepository?>(null) }
    var startupError by remember { mutableStateOf<Throwable?>(null) }

    LaunchedEffect(Unit) {
        val result = runCatching { DrugDatabaseProvider.getRepository(appContext) }
        drugRepository = result.getOrNull()
        startupError = result.exceptionOrNull()
        result.getOrNull()?.let { DrugDatabaseProvider.ensureSeedData(appContext) }
    }

    if (startupError != null && drugRepository == null) {
        StartupErrorCard(errorText = startupError!!.message ?: "未知错误")
        return
    }

    val repository = drugRepository ?: return

    // 当主分类切换时，默认选中第一个子页面
    LaunchedEffect(selectedMainCategory) {
        if (subSections.isNotEmpty() && !subSections.contains(currentSection)) {
            currentSection = subSections.first()
        }
    }

    // TTS 自动播报欢迎语 + 状态监测
    val ttsState by TTSManager.state.collectAsState()
    var welcomeSpoken by remember { mutableStateOf(false) }
    var ttsTimeoutExceeded by remember { mutableStateOf(false) }

    LaunchedEffect(ttsState.isReady, ttsState.isEnabled) {
        if (ttsState.isReady && ttsState.isEnabled && !welcomeSpoken) {
            welcomeSpoken = true
            TTSManager.speakNow("欢迎使用微光畅行，无障碍出行助手。")
        }
    }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        if (!ttsState.isReady) {
            ttsTimeoutExceeded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("微光畅行", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            if (currentSection == AppSection.HOME) productCategory.displayName else currentSection.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                MainCategory.entries.forEachIndexed { index, category ->
                    val selected = selectedMainCategory == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = { 
                            selectedMainCategory = index
                            // 切换到新分类的第一个子页面
                            if (category.subSections.isNotEmpty()) {
                                currentSection = category.subSections.first()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) category.iconFilled else category.iconOutlined,
                                contentDescription = category.label
                            )
                        },
                        label = { Text(category.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // TTS 状态提示条：3秒后 TTS 仍未就绪时显示
            if (ttsTimeoutExceeded && currentSection == AppSection.HOME) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "语音引擎未就绪",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "部分功能的语音播报可能无法正常使用",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                ttsTimeoutExceeded = false
                                TTSManager.initialize(context)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("重试", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            // 如果子功能数量 > 1，显示子功能选择器
            if (subSections.size > 1) {
                SubSectionSelector(
                    sections = subSections,
                    selectedSection = currentSection,
                    onSectionSelected = { currentSection = it }
                )
            }
            
            // 页面内容
            AnimatedContent(
                targetState = currentSection,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    fadeIn(animationSpec = spring(dampingRatio = 0.9f)) togetherWith
                        fadeOut(animationSpec = spring(dampingRatio = 0.9f)) using
                        SizeTransform(clip = false)
                },
                label = "ScreenTransition"
            ) { section ->
                when (section) {
                    AppSection.HOME -> HomeScreen(
                        modifier = Modifier,
                        category = productCategory,
                        onNavigateToCategory = { mainCategory ->
                            selectedMainCategory = MainCategory.entries.indexOf(mainCategory)
                            if (mainCategory.subSections.isNotEmpty()) {
                                currentSection = mainCategory.subSections.first()
                            }
                        }
                    )
                    AppSection.SIGN -> SignScreen(Modifier)
                    AppSection.TRANSPORT -> TransportHubScreen(Modifier)
                    AppSection.MEDICINE -> MedicineScreen(Modifier, repository)
                    AppSection.EMERGENCY -> EmergencyHelpScreen(Modifier)
                    AppSection.ALARM -> AlarmScreen(Modifier)
                    AppSection.LEARNING -> LearningCenterScreen(Modifier)
                    AppSection.VISION -> VisionScreen(modifier = Modifier)
                    AppSection.SERVICE -> ServiceHubScreen(Modifier)
                    AppSection.DIALOGUE -> DualSignScreen(Modifier)
                    AppSection.ASSISTANT -> VoiceAssistantScreen(modifier = Modifier)
                    AppSection.TTS -> TTSSettingsScreen(modifier = Modifier)
                }
            }
        }
    }
}

/**
 * 子功能选择器组件
 */
@Composable
private fun SubSectionSelector(
    sections: List<AppSection>,
    selectedSection: AppSection,
    onSectionSelected: (AppSection) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            sections.forEach { section ->
                val isSelected = selectedSection == section
                FilterChip(
                    selected = isSelected,
                    onClick = { onSectionSelected(section) },
                    label = { Text(section.label) },
                    leadingIcon = if (isSelected) {
                        { Icon(section.iconFilled, contentDescription = null) }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun StartupErrorCard(errorText: String) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("启动异常", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(errorText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                DrugDatabaseProvider.recoverDatabase(context.applicationContext)
            }) { Text("重置药品库") }
            OutlinedButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("error", errorText))
            }) { Text("复制错误") }
        }
    }
}

@Composable
private fun TransportHubScreen(modifier: Modifier = Modifier) {
    var showBusMetro by remember { mutableStateOf(true) }
    ScrollPage(modifier = modifier) {
        HeroCard(
            title = "出行辅助",
            subtitle = "公交报站 · 网约车沟通",
            body = "GPS实时报站+到站震动提醒 | 网约车行程确认+沟通话术"
        )
        SectionTitle("功能选择")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { showBusMetro = true },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = if (showBusMetro) ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ) else ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) { Text("公交报站", fontWeight = FontWeight.Bold) }
            Button(
                onClick = { showBusMetro = false },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = if (!showBusMetro) ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ) else ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) { Text("网约车助手", fontWeight = FontWeight.Bold) }
        }
        if (showBusMetro) {
            BusMetroScreen(Modifier)
        } else {
            RideHailingHelperScreen(Modifier)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ServiceHubScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("基地服务", "设备申领", "设施上报", "无障碍地图")
    ScrollPage(modifier = modifier) {
        HeroCard(
            title = "服务中心",
            subtitle = "基地帮扶 · 设备申领 · 设施反馈",
            body = "手语翻译预约 | 辅助设备申领 | 设施问题上报 | 无障碍地图"
        )
        SectionTitle("服务类别")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, label ->
                OutlinedButton(
                    onClick = { selectedTab = index },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = if (selectedTab == index) ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) else ButtonDefaults.outlinedButtonColors()
                ) { Text(label, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelLarge) }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        when (selectedTab) {
            0 -> ServiceBookingScreen(Modifier)
            1 -> DeviceApplyScreen(Modifier)
            2 -> FacilityReportScreen(Modifier)
            3 -> AccessibleMapScreen(Modifier)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    category: ProductCategory,
    onNavigateToCategory: (MainCategory) -> Unit
) {
    val modules = listOf(
        ModuleSummary("手语沟通", "听障辅助", "语音转手语 / 手语转语音", "117条高频短语+手势识别",
            body = "将语音实时转换为手语动画，同时支持手语动作识别转语音输出，帮助听障人士与健听人无障碍交流。"),
        ModuleSummary("公交报站", "出行辅助", "GPS实时报站+到站震动", "10条公交线路+距离计算",
            body = "通过GPS定位实时感知公交位置，到站前通过振动和语音双重提醒，避免坐过站或下错站。"),
        ModuleSummary("药品识别", "用药安全", "OCR识别+本地103种药品库", "风险预警+TTS播报",
            body = "通过相机扫描药盒或手动输入药名，快速识别药品信息，提供风险分级预警和语音播报。"),
        ModuleSummary("一键应急", "安全守护", "SOS短信+位置+闪光灯", "6种预设求助场景",
            body = "一键触发应急求助，自动发送位置信息和SOS短信至紧急联系人，同时启动闪光灯和振动警示。"),
        ModuleSummary("强提醒", "安全守护", "振动+闪光灯+声音三联动", "四级风险提醒",
            body = "提供四级风险等级的分层提醒机制，通过振动、闪光灯和声音三重联动，确保重要信息不被遗漏。"),
        ModuleSummary("学习中心", "知识培训", "交通规则+应急避险课程", "6类图文/手语课程",
            body = "提供交通规则、应急避险等图文和手语课程，帮助用户系统学习安全出行知识。"),
        ModuleSummary("视觉识别", "视障辅助", "实时物品识别", "ML Kit图像标签",
            body = "利用ML Kit图像识别技术，实时识别周围物品并通过语音播报，帮助视障用户感知环境。"),
        ModuleSummary("障碍物检测", "视障辅助", "前后方向障碍物识别", "传感器+距离估算",
            body = "通过手机传感器检测前后方向的障碍物，提供距离估算和振动反馈，辅助视障用户安全行走。"),
        ModuleSummary("语音转文字", "沟通支持", "全局实时语音转写", "SpeechRecognizer常驻",
            body = "全局常驻的语音识别服务，将实时语音转写为文字，辅助听障用户获取语音信息。"),
        ModuleSummary("基地帮扶", "服务对接", "手语翻译/陪同出行预约", "设备申领+服务预约",
            body = "提供手语翻译、陪同出行等服务预约功能，以及无障碍设备申领服务，连接线下帮扶资源。"),
        ModuleSummary("语音助手", "系统集成", "7大品牌一键唤醒", "小爱/小艺/小布/YOYO等",
            body = "集成主流手机品牌语音助手，一键唤醒小爱、小艺、小布、YOYO等，提供便捷的语音控制入口。"),
        ModuleSummary("TTS播报", "基础能力", "全局离线语音播报", "语速/音调可调",
            body = "提供全局离线语音播报能力，支持语速和音调调节，为所有功能模块提供语音输出支持。")
    )

    var selectedModule by remember { mutableStateOf<ModuleSummary?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    fun getModuleIcon(title: String): ImageVector = when (title) {
        "手语沟通" -> Icons.Filled.Hearing
        "公交报站" -> Icons.Filled.DirectionsBus
        "药品识别" -> Icons.Filled.Medication
        "一键应急" -> Icons.Filled.Warning
        "强提醒" -> Icons.Filled.Alarm
        "学习中心" -> Icons.Filled.School
        "视觉识别" -> Icons.Filled.Visibility
        "障碍物检测" -> Icons.Filled.Visibility
        "语音转文字" -> Icons.Filled.SettingsVoice
        "基地帮扶" -> Icons.Filled.Star
        "语音助手" -> Icons.Filled.RecordVoiceOver
        "TTS播报" -> Icons.Filled.Person
        else -> Icons.Filled.Star
    }

    fun getModuleCategory(title: String): MainCategory = when (title) {
        "手语沟通" -> MainCategory.COMMUNICATION
        "公交报站" -> MainCategory.TRAVEL
        "药品识别" -> MainCategory.DAILY
        "一键应急" -> MainCategory.SAFETY
        "强提醒" -> MainCategory.SAFETY
        "学习中心" -> MainCategory.DAILY
        "视觉识别" -> MainCategory.DAILY
        "障碍物检测" -> MainCategory.DAILY
        "语音转文字" -> MainCategory.COMMUNICATION
        "基地帮扶" -> MainCategory.SAFETY
        "语音助手" -> MainCategory.COMMUNICATION
        "TTS播报" -> MainCategory.SAFETY
        else -> MainCategory.HOME
    }

    ScrollPage(modifier = modifier) {
        HeroCard(
            title = "微光畅行",
            subtitle = category.displayName,
            body = category.description
        )

        SectionTitle("功能模块")
        modules.forEach { module ->
            Card(
                onClick = {
                    selectedModule = module
                    showDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = getModuleIcon(module.title),
                        contentDescription = module.title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(40.dp).height(40.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(module.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            module.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        SectionTitle("快捷操作")
        InfoCard {
            Text(
                "底部分页导航可切换各功能模块。进入「语音」设置开启TTS播报后使用效果更佳。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showDialog && selectedModule != null) {
        val module = selectedModule!!
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = {
                Icon(
                    imageVector = getModuleIcon(module.title),
                    contentDescription = module.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(48.dp).height(48.dp)
                )
            },
            title = {
                Text(
                    text = module.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = module.body,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "使用场景",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = module.value,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        onNavigateToCategory(getModuleCategory(module.title))
                    }
                ) {
                    Text("前往使用")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text("关闭")
                }
            }
        )
    }
}

@Composable
private fun MedicineScreen(
    modifier: Modifier = Modifier,
    repository: DrugRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf("布洛芬") }
    var cameraPermissionGranted by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var cameraPreviewEnabled by rememberSaveable { mutableStateOf(false) }
    var cameraStatus by rememberSaveable { mutableStateOf("") }
    var cameraRecognitionState by rememberSaveable { mutableStateOf("待打开相机") }
    var liveCameraRawText by rememberSaveable { mutableStateOf("") }
    var liveCameraQueryKeyword by rememberSaveable { mutableStateOf("") }
    var liveCameraStrategy by rememberSaveable { mutableStateOf("") }
    var cameraCaptureRequestId by rememberSaveable { mutableIntStateOf(0) }
    var frozenCaptureResult by remember { mutableStateOf<CameraOcrAnalysisResult?>(null) }
    var captureDialogVisible by rememberSaveable { mutableStateOf(false) }
    var ocrPreviewText by rememberSaveable { mutableStateOf("") }
    var announcement by rememberSaveable { mutableStateOf("") }
    var riskFeedback by rememberSaveable { mutableStateOf("") }
    var ttsFeedback by rememberSaveable { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<DrugInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val ttsState by TTSManager.state.collectAsState()

    // 语音搜索状态：是否正在聆听、反馈文字
    var isVoiceSearching by rememberSaveable { mutableStateOf(false) }
    var voiceSearchFeedback by rememberSaveable { mutableStateOf("") }
    // 语音识别器：每次 MedicineScreen 重组时保持同一个实例
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    // 语音识别 Intent：指定中文语音识别、自由格式、返回最佳结果
    val voiceSearchIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
    }

    val ocrSamples = remember {
        listOf(
            OcrPreviewSample("药盒样本A", "芬必得 布洛芬缓释胶囊 国药准字H10900089", "布洛芬"),
            OcrPreviewSample("药盒样本B", "阿莫仙 阿莫西林胶囊 国药准字H44021351", "阿莫西林"),
            OcrPreviewSample("药盒样本C", "格华止 盐酸二甲双胍片 国药准字H20023370", "二甲双胍"),
            OcrPreviewSample("药盒样本D", "泰诺林 对乙酰氨基酚片 国药准字H00001004", "对乙酰氨基酚"),
            OcrPreviewSample("药盒样本E", "开瑞坦 氯雷他定片 国药准字H00001005", "氯雷他定"),
            OcrPreviewSample("药盒样本F", "拜新同 硝苯地平控释片 国药准字H00001008", "硝苯地平")
        )
    }
    val commonMedicineQuickActions = remember {
        listOf("布洛芬", "对乙酰氨基酚", "氯雷他定", "阿莫西林", "二甲双胍", "硝苯地平")
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraPermissionGranted = granted
        cameraPreviewEnabled = granted
        cameraStatus = if (granted) "相机已就绪" else "相机权限未授权"
        cameraRecognitionState = if (granted) "待对准药盒" else "等待相机权限"
    }

    val ttsReady = ttsState.isReady && ttsState.isEnabled
    LaunchedEffect(ttsReady) {
        ttsFeedback = if (ttsReady) "语音播报已就绪" else "TTS引擎未就绪，请在「语音」设置页面检查"
    }

    fun launchMedicineSearch(
        triggerRiskAlert: Boolean,
        incomingKeyword: String? = null,
        sourceLabel: String = "手动输入"
    ) {
        scope.launch {
            isLoading = true
            val effectiveKeyword = incomingKeyword?.trim().orEmpty().ifBlank { query.trim() }
            query = effectiveKeyword
            val results = repository.searchByKeyword(effectiveKeyword)
            val primaryDrug = results.firstOrNull()
            val announcementText = buildDrugAnnouncement(primaryDrug, sourceLabel)
            searchResults = results
            announcement = announcementText
            riskFeedback = buildRiskFeedback(primaryDrug, triggerRiskAlert)
            if (triggerRiskAlert) {
                performRiskLevelAlert(context, primaryDrug?.highestRiskLevel)
                ttsFeedback = speakDrugAnnouncement(text = announcementText, riskLevel = primaryDrug?.highestRiskLevel)
            } else {
                ttsFeedback = if (ttsReady) "语音播报已就绪" else "语音播报当前不可用"
            }
            isLoading = false
        }
    }

    LaunchedEffect(repository) { launchMedicineSearch(triggerRiskAlert = false) }

    // 语音识别回调监听器：
    // 覆盖 SpeechRecognizer 的完整生命周期，把识别结果转成 UI 状态并触发药品搜索
    val speechRecognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() { voiceSearchFeedback = "正在聆听..." }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { voiceSearchFeedback = "识别中..." }
            override fun onError(error: Int) {
                isVoiceSearching = false
                voiceSearchFeedback = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "未识别到语音"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "未检测到语音输入"
                    SpeechRecognizer.ERROR_NETWORK -> "网络错误"
                    else -> "语音识别失败"
                }
            }
            override fun onResults(results: Bundle?) {
                isVoiceSearching = false
                val texts = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (texts.isNullOrEmpty()) {
                    voiceSearchFeedback = "未识别到语音"
                    return
                }
                val recognizedText = texts[0]
                voiceSearchFeedback = "已识别: $recognizedText"
                query = recognizedText

                // 检测是否包含过敏关键词，正则匹配："对XXX过敏"、"XXX不能吃" 等句式
                val allergyMatch = Regex("(?:.*?对)?(.{2,6})(?:过敏|不能吃|不能服用|禁忌|忌用)").find(recognizedText)?.groupValues?.getOrNull(1)
                if (allergyMatch != null) {
                    voiceSearchFeedback = "检测到过敏: $allergyMatch"
                    scope.launch {
                        isLoading = true
                        // 跨数据库搜索所有药品规则表中匹配该过敏关键词的药品
                        val allAffectedDrugs = repository.searchByAllergy(allergyMatch)
                        if (allAffectedDrugs.isNotEmpty()) {
                            val drugNames = allAffectedDrugs.joinToString("、") { it.genericName }
                            announcement = "检测到${allergyMatch}，以下药品存在风险：${drugNames}。请在使用前咨询医生。"
                            riskFeedback = "高风险！${allergyMatch}与以下药品冲突：${drugNames}"
                            TTSManager.speakNow("注意：检测到${allergyMatch}。药品${drugNames}含有${allergyMatch}成分，不可使用。")
                            // 把受影响的药品设为搜索结果，让用户直观看到
                            searchResults = allAffectedDrugs
                        } else {
                            announcement = "已识别语音：$recognizedText。未在本地药品库中找到与${allergyMatch}相关的风险规则。"
                        }
                        isLoading = false
                    }
                } else {
                    // 普通药品搜索：把语音识别文本作为关键词触发搜索和风险分析
                    launchMedicineSearch(triggerRiskAlert = true, incomingKeyword = recognizedText, sourceLabel = "语音搜索")
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    // 把监听器绑定到语音识别器（只在首次创建时执行一次）
    LaunchedEffect(speechRecognitionListener) {
        speechRecognizer.setRecognitionListener(speechRecognitionListener)
    }

    // 语音识别器释放：当 MedicineScreen 退出组合时销毁，防止内存泄漏
    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.destroy()
        }
    }

    ScrollPage(modifier = modifier) {
        HeroCard(
            title = "药品识别与风险提醒",
            subtitle = "56种本地药品库",
            body = "支持药名搜索、药盒OCR识别、风险预警和语音播报"
        )

        SectionTitle("药品搜索")
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("输入药名或关键词") },
            placeholder = { Text("例如：布洛芬、阿莫西林") },
            singleLine = true
        )
        // 搜索按钮和语音搜索按钮并排显示，等高 56dp，间距 12dp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { launchMedicineSearch(triggerRiskAlert = true) },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("搜索", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            // 语音搜索按钮：点击后启动 Android SpeechRecognizer，聆听时显示红色
            Button(
                onClick = {
                    isVoiceSearching = true
                    voiceSearchFeedback = "正在聆听..."
                    speechRecognizer.startListening(voiceSearchIntent)
                },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = if (isVoiceSearching) ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ) else ButtonDefaults.buttonColors()
            ) {
                Text(
                    if (isVoiceSearching) "聆听中..." else "语音搜索",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // 语音搜索反馈区域：当有语音识别状态或结果时显示
        if (voiceSearchFeedback.isNotBlank()) {
            HighlightCard(
                title = "语音输入",
                value = voiceSearchFeedback,
                note = if (isVoiceSearching) "请对手机麦克风说话" else ""
            )
        }

        SectionTitle("快捷检索")
        commonMedicineQuickActions.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { keyword ->
                    OutlinedButton(
                        onClick = {
                            query = keyword
                            launchMedicineSearch(triggerRiskAlert = true, incomingKeyword = keyword, sourceLabel = "快捷入口")
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text(keyword, textAlign = TextAlign.Center) }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (cameraPermissionGranted) {
            SectionTitle("相机识别")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        cameraPreviewEnabled = !cameraPreviewEnabled
                        cameraStatus = if (cameraPreviewEnabled) "相机已开启" else "相机已关闭"
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = if (cameraPreviewEnabled) ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ) else ButtonDefaults.buttonColors()
                ) { Text(if (cameraPreviewEnabled) "关闭相机" else "打开相机", fontWeight = FontWeight.Bold) }
            }

            if (cameraPreviewEnabled) {
                CameraPreviewCard(
                    captureRequestKey = cameraCaptureRequestId,
                    analysisPaused = captureDialogVisible,
                    onAnalysisResult = { result ->
                        liveCameraRawText = result.rawText
                        liveCameraQueryKeyword = result.queryKeyword.orEmpty()
                        liveCameraStrategy = buildCameraStrategyText(result)
                        ocrPreviewText = result.rawText
                        cameraRecognitionState = if (result.queryKeyword.isNullOrBlank()) "识别中" else "已命中"
                        cameraStatus = if (result.queryKeyword.isNullOrBlank()) "未找到药品关键词" else "已识别: ${result.queryKeyword}"
                    },
                    onAnalysisStatus = { status -> cameraStatus = status },
                    onCaptureStarted = {
                        cameraRecognitionState = "拍照冻结识别中"
                        cameraStatus = "正在锁定画面"
                    },
                    onCaptureResult = { result ->
                        frozenCaptureResult = result
                        captureDialogVisible = true
                        ocrPreviewText = result.rawText
                        if (!result.queryKeyword.isNullOrBlank()) {
                            launchMedicineSearch(triggerRiskAlert = true, incomingKeyword = result.queryKeyword, sourceLabel = "拍照OCR")
                        }
                    },
                    onCaptureError = { error -> cameraStatus = error }
                )
            }
        } else {
            SectionTitle("相机权限")
            Button(
                onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text("授权相机权限", fontWeight = FontWeight.Bold) }
        }

        if (isLoading) {
            HighlightCard(title = "搜索中...", value = "正在检索本地药品库", note = "56种药品本地匹配")
        } else if (searchResults.isNotEmpty()) {
            SectionTitle("搜索结果（${searchResults.size}项）")
            searchResults.forEach { drug ->
                DrugResultCard(drug = drug)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        SectionTitle("OCR测试样本")
        ocrSamples.forEach { sample ->
            OutlinedButton(
                onClick = {
                    ocrPreviewText = sample.rawText
                    launchMedicineSearch(triggerRiskAlert = true, incomingKeyword = sample.extractedKeyword, sourceLabel = "OCR样本")
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(sample.title, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(8.dp))
                Text(sample.extractedKeyword, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        if (riskFeedback.isNotBlank()) {
            SectionTitle("风险提示")
            HighlightCard(title = "风险反馈", value = riskFeedback, note = ttsFeedback)
        }

        if (announcement.isNotBlank()) {
            SectionTitle("播报内容")
            InfoCard { Text(announcement, style = MaterialTheme.typography.bodyLarge) }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DrugResultCard(drug: DrugInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (drug.highestRiskLevel) {
                "high" -> MaterialTheme.colorScheme.errorContainer
                "medium" -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(drug.genericName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (drug.tradeName != null) Text("商品名: ${drug.tradeName}", style = MaterialTheme.typography.bodyMedium)
            if (drug.categoryName != null) Text("分类: ${drug.categoryName}", style = MaterialTheme.typography.bodyMedium)
            if (drug.specification != null) Text("规格: ${drug.specification}", style = MaterialTheme.typography.bodyMedium)
            if (drug.approvalNo != null) Text("批准文号: ${drug.approvalNo}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (drug.composition != null || drug.indication != null || drug.usageAndDosage != null || drug.taboo != null || drug.attention != null || drug.adverseReaction != null) {
                Spacer(modifier = Modifier.height(8.dp))
                if (drug.composition != null) InfoLine("组成", drug.composition)
                if (drug.indication != null) InfoLine("适应症", drug.indication)
                if (drug.usageAndDosage != null) InfoLine("用法用量", drug.usageAndDosage)
                if (drug.taboo != null) WarningLine("禁忌", drug.taboo)
                if (drug.attention != null) InfoLine("注意事项", drug.attention)
                if (drug.adverseReaction != null) InfoLine("不良反应", drug.adverseReaction)
            }
            if (drug.riskPrompts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                drug.riskPrompts.forEach { prompt ->
                    Text("⚠ $prompt", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
            }
            if (drug.signKeywords.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("手语关键词: ${drug.signKeywords.joinToString(" / ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun AlarmScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val capability = remember(context) { getVibrationCapability(context) }
    val patterns = listOf(
        AlarmPattern("渐强震动", "适合清晨平缓唤醒，先轻后强", longArrayOf(0, 150, 80, 200, 80, 260), intArrayOf(0, 80, 0, 140, 0, 255)),
        AlarmPattern("紧急爆发", "适合需要快速提醒", longArrayOf(0, 260, 80, 260, 80, 260), intArrayOf(0, 255, 0, 255, 0, 255)),
        AlarmPattern("长循环提醒", "适合连续震动测试", longArrayOf(0, 200, 60, 200, 60, 200, 60, 320), intArrayOf(0, 180, 0, 220, 0, 255, 0, 255))
    )
    var selected by rememberSaveable { mutableStateOf(patterns.first()) }
    var feedback by rememberSaveable { mutableStateOf("选择一个提醒模式进行测试") }
    var customPulseMs by rememberSaveable { mutableStateOf(180f) }
    var customGapMs by rememberSaveable { mutableStateOf(90f) }
    var customRepeatCount by rememberSaveable { mutableStateOf(3f) }
    var customAmplitude by rememberSaveable { mutableStateOf(220f) }

    ScrollPage(modifier = modifier) {
        HeroCard(title = "强提醒闹钟", subtitle = "3套预设波形", body = "振动+闪光灯+声音三重联动提醒")

        SectionTitle("设备信息")
        InfoCard {
            InfoLine("振动马达", if (capability.hasVibrator) "已检测" else "未检测")
            InfoLine("强度调节", if (capability.hasAmplitudeControl) "支持" else "不支持")
        }

        SectionTitle("提醒模式")
        patterns.forEach { pattern ->
            val isSelected = pattern == selected
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(pattern.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(pattern.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { selected = pattern }, modifier = Modifier.height(44.dp)) {
                            Text(if (isSelected) "已选" else "选择")
                        }
                        Button(onClick = {
                            selected = pattern
                            performVibration(context, pattern)
                            feedback = "已触发 ${pattern.title}"
                        }, modifier = Modifier.height(44.dp)) { Text("测试") }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        SectionTitle("自定义震动")
        InfoCard {
            VibrationSliderRow("震动时长", "${customPulseMs.roundToInt()}ms", customPulseMs, 60f..600f, 17) { customPulseMs = it }
            VibrationSliderRow("震动间隔", "${customGapMs.roundToInt()}ms", customGapMs, 40f..400f, 17) { customGapMs = it }
            VibrationSliderRow("重复次数", "${customRepeatCount.roundToInt()}次", customRepeatCount, 1f..6f, 4) { customRepeatCount = it }
            VibrationSliderRow("强度", if (capability.hasAmplitudeControl) "${customAmplitude.roundToInt()}/255" else "不支持", customAmplitude, 40f..255f, 20) { customAmplitude = it }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    performVibration(context, buildCustomAlarmPattern(customPulseMs.roundToInt(), customGapMs.roundToInt(), customRepeatCount.roundToInt(), customAmplitude.roundToInt()))
                    feedback = "已触发自定义震动"
                }, enabled = capability.hasVibrator, modifier = Modifier.weight(1f)) { Text("测试") }
                OutlinedButton(onClick = { cancelVibration(context); feedback = "已停止" }, enabled = capability.hasVibrator, modifier = Modifier.weight(1f)) { Text("停止") }
            }
        }

        HighlightCard(title = "反馈", value = feedback, note = "建议在不同品牌真机上验证")
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ScrollPage(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

@Composable
private fun HeroCard(title: String, subtitle: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.height(12.dp))
            Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun FeatureCard(title: String, subtitle: String, description: String, footer: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                StatusChip(subtitle, "可用")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("✦", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(footer, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HighlightCard(title: String, value: String, note: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Text(note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
private fun WarningCard(title: String, value: String, note: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Text(note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}

@Composable
@Suppress("UNUSED_PARAMETER", "DEPRECATION")
private fun CameraPreviewCard(
    captureRequestKey: Int,
    analysisPaused: Boolean,
    onAnalysisResult: (CameraOcrAnalysisResult) -> Unit,
    onAnalysisStatus: (String) -> Unit,
    onCaptureStarted: () -> Unit,
    onCaptureResult: (CameraOcrAnalysisResult) -> Unit,
    onCaptureError: (String) -> Unit
) {
    val context = LocalContext.current
    val captureExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val previewView = remember(context) { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    val imageCaptureHolder = remember { mutableStateOf<ImageCapture?>(null) }
    val recognizerHolder = remember { mutableStateOf(TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())) }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("相机预览", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    val imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
                    imageCaptureHolder.value = imageCapture
                    onCaptureStarted()
                    imageCapture.takePicture(captureExecutor, object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(imageProxy: ImageProxy) { analyzeCapturedPhoto(imageProxy, recognizerHolder.value, onCaptureResult, onCaptureError) }
                        override fun onError(exception: ImageCaptureException) { onCaptureError("拍照失败: ${exception.message}") }
                    })
                }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp)) { Text("拍照识别") }
            }
        }
    }

    DisposableEffect(previewView) {
        val activity = context.findComponentActivity()
        val providerFuture = if (activity != null) ProcessCameraProvider.getInstance(context) else null
        if (providerFuture != null && activity != null) {
            providerFuture.addListener({
                try {
                    val provider = providerFuture.get()
                    val preview = CameraXPreview.Builder().setTargetResolution(android.util.Size(640, 480)).build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                    val imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
                    imageCaptureHolder.value = imageCapture
                    val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).setTargetResolution(android.util.Size(640, 480)).build()
                    analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                        if (!analysisPaused) analyzeCameraFrame(imageProxy, recognizerHolder.value, { false }, {}, {}, onAnalysisStatus, onAnalysisResult)
                        else imageProxy.close()
                    }
                    provider.unbindAll()
                    provider.bindToLifecycle(activity, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture, analysis)
                } catch (_: Exception) { onAnalysisStatus("相机启动失败") }
            }, ContextCompat.getMainExecutor(context))
        }
        onDispose { providerFuture?.get()?.unbindAll() }
    }
}

@Composable
private fun StatusChip(left: String, right: String) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(left, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(right, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f).padding(start = 12.dp))
    }
}

@Composable
private fun WarningLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f).padding(start = 12.dp))
    }
}

@Composable
private fun TimelineLine(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("●", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Text("$label：$value", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun VibrationSliderRow(label: String, valueText: String, value: Float, valueRange: ClosedFloatingPointRange<Float>, steps: Int, onValueChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(valueText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange, steps = steps)
    }
}

private fun getVibrationCapability(context: Context): VibrationCapability {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java).defaultVibrator
    } else { @Suppress("DEPRECATION") context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    val hasVibrator = vibrator.hasVibrator()
    val hasAmplitude = vibrator.hasAmplitudeControl()
    return VibrationCapability(hasVibrator, hasAmplitude, false, "")
}

private fun performVibration(context: Context, pattern: AlarmPattern) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java).defaultVibrator
    } else { @Suppress("DEPRECATION") context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createWaveform(pattern.timings, pattern.amplitudes, -1))
    } else { @Suppress("DEPRECATION") vibrator.vibrate(pattern.timings, -1) }
}

private fun cancelVibration(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java).defaultVibrator
    } else { @Suppress("DEPRECATION") context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    vibrator.cancel()
    TripleAlertSystem(context).apply { stopAllAlerts() }
}

private fun buildCustomAlarmPattern(pulseMs: Int, gapMs: Int, repeatCount: Int, amplitude: Int): AlarmPattern {
    val timings = mutableListOf<Long>()
    val amplitudes = mutableListOf<Int>()
    timings.add(0); amplitudes.add(0)
    repeat(repeatCount) { timings.add(pulseMs.toLong()); amplitudes.add(amplitude); timings.add(gapMs.toLong()); amplitudes.add(0) }
    return AlarmPattern("自定义", "", timings.toLongArray(), amplitudes.toIntArray())
}

private fun buildDrugAnnouncement(drug: DrugInfo?, sourceLabelInput: String): String {
    if (drug == null) return "未命中本地药品库，当前结果仅供辅助判断。"
    val sourceLabel = if (drug.sourceTag == "seed_demo") "本地演示样本" else drug.sourceTag ?: sourceLabelInput
    return buildString {
        append("已识别${drug.genericName}")
        drug.tradeName?.let { append("（$it）") }
        drug.specification?.let { append(" $it") }
        append("，来源：$sourceLabel。")
        drug.ttsSummary?.let { append(it) }
        if (drug.riskPrompts.isNotEmpty()) { append("风险提示："); drug.riskPrompts.forEach { append("$it；") } }
        append("当前结果仅供辅助判断。")
    }
}

private fun buildRiskFeedback(drug: DrugInfo?, triggerRiskAlert: Boolean): String {
    if (drug == null) return "未命中本地药品库，尚未触发风险联动提醒。"
    if (!triggerRiskAlert) return "已完成风险分级，未主动触发震动提醒。"
    return when (drug.highestRiskLevel) {
        "high" -> "高风险！请立即停止自行判断并转人工复核。"
        "medium" -> "中风险，请结合说明书和用户病史继续核对。"
        "low" -> "低风险，未触发强震动，仅保留页面警示。"
        else -> "未命中风险规则。"
    }
}

private fun performRiskLevelAlert(context: Context, riskLevel: String?) {
    if (riskLevel == "high") {
        TripleAlertSystem(context).triggerTripleAlert(AlertLevel.HIGH)
    }
}

private fun speakDrugAnnouncement(text: String, riskLevel: String?): String {
    val prefix = when (riskLevel) {
        "high" -> "高风险警告："
        "medium" -> "中风险提醒："
        else -> ""
    }
    TTSManager.speakNow("$prefix$text")
    return "语音播报已触发"
}

private fun analyzeCameraFrame(
    imageProxy: ImageProxy,
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    isRecognitionRunning: () -> Boolean,
    onRecognitionStarted: () -> Unit,
    onRecognitionFinished: () -> Unit,
    onAnalysisStatus: (String) -> Unit,
    onAnalysisResult: (CameraOcrAnalysisResult) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null || isRecognitionRunning()) { imageProxy.close(); return }
    onRecognitionStarted()
    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    recognizer.process(inputImage)
        .addOnSuccessListener { visionText ->
            val normalizedText = normalizeOcrText(visionText.text)
            if (normalizedText.isBlank()) onAnalysisStatus("未识别到文字")
            else { val result = extractDrugQueryFromOcrText(normalizedText); onAnalysisResult(result) }
        }
        .addOnFailureListener { onAnalysisStatus("OCR识别失败") }
        .addOnCompleteListener { onRecognitionFinished(); imageProxy.close() }
}

private fun analyzeCapturedPhoto(
    imageProxy: ImageProxy,
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    onCaptureResult: (CameraOcrAnalysisResult) -> Unit,
    onCaptureError: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) { imageProxy.close(); onCaptureError("拍照结果为空"); return }
    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    recognizer.process(inputImage)
        .addOnSuccessListener { visionText ->
            val normalizedText = normalizeOcrText(visionText.text)
            if (normalizedText.isBlank()) onCaptureError("未提取到文字")
            else onCaptureResult(extractDrugQueryFromOcrText(normalizedText))
        }
        .addOnFailureListener { onCaptureError("OCR失败") }
        .addOnCompleteListener { imageProxy.close() }
}

private fun normalizeOcrText(text: String): String = text.replace("\n", " ").replace(Regex("\\s+"), " ").trim()

private fun extractDrugQueryFromOcrText(rawText: String): CameraOcrAnalysisResult {
    val compactText = rawText.replace(" ", "")
    val approvalNo = Regex("国药准字[A-Z]\\d{8}", RegexOption.IGNORE_CASE).find(compactText)?.value?.uppercase(Locale.ROOT)
    if (approvalNo != null) return CameraOcrAnalysisResult(rawText, approvalNo, approvalNo, "批准文号")

    val matchedDrug = commonDrugOcrTargets.firstOrNull { target -> target.matchTokens.any { compactText.contains(it) } }
    if (matchedDrug != null) return CameraOcrAnalysisResult(rawText, null, matchedDrug.canonicalKeyword, "药名词典")

    val ignoredTokens = listOf("有限公司", "说明书", "规格", "用法", "用量", "批准文号", "生产企业", "药品名称", "本品", "适应症")
    val fallbackToken = Regex("[\\u4e00-\\u9fa5]{2,12}").findAll(compactText).map { it.value }.firstOrNull { token -> ignoredTokens.none { token.contains(it) } }
    return CameraOcrAnalysisResult(rawText, null, fallbackToken, if (fallbackToken == null) "未形成关键词" else "候选词回退")
}

private fun buildCameraStrategyText(result: CameraOcrAnalysisResult): String = "策略:${result.strategyLabel} 文号:${result.approvalNo ?: "无"} 关键词:${result.queryKeyword ?: "待提取"}"

private tailrec fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}

private data class OcrPreviewSample(val title: String, val rawText: String, val extractedKeyword: String)

private data class commonDrugOcrTarget(val matchTokens: List<String>, val canonicalKeyword: String)
private val commonDrugOcrTargets = listOf(
    commonDrugOcrTarget(listOf("芬必得", "布洛芬"), "布洛芬"),
    commonDrugOcrTarget(listOf("阿莫仙", "阿莫西林"), "阿莫西林"),
    commonDrugOcrTarget(listOf("格华止", "二甲双胍"), "二甲双胍"),
    commonDrugOcrTarget(listOf("泰诺林", "对乙酰氨基酚"), "对乙酰氨基酚"),
    commonDrugOcrTarget(listOf("开瑞坦", "氯雷他定"), "氯雷他定"),
    commonDrugOcrTarget(listOf("拜新同", "硝苯地平"), "硝苯地平"),
    commonDrugOcrTarget(listOf("立普妥", "阿托伐他汀"), "阿托伐他汀"),
    commonDrugOcrTarget(listOf("洛活喜", "氨氯地平"), "氨氯地平"),
    commonDrugOcrTarget(listOf("代文", "缬沙坦"), "缬沙坦"),
    commonDrugOcrTarget(listOf("波依定", "非洛地平"), "非洛地平"),
    commonDrugOcrTarget(listOf("科素亚", "氯沙坦"), "氯沙坦"),
    commonDrugOcrTarget(listOf("洛汀新", "贝那普利"), "贝那普利"),
    commonDrugOcrTarget(listOf("倍他乐克", "美托洛尔"), "美托洛尔"),
    commonDrugOcrTarget(listOf("舒降之", "辛伐他汀"), "辛伐他汀"),
    commonDrugOcrTarget(listOf("洛赛克", "奥美拉唑"), "奥美拉唑"),
    commonDrugOcrTarget(listOf("吗丁啉", "多潘立酮"), "多潘立酮"),
    commonDrugOcrTarget(listOf("拜阿司匹林", "阿司匹林"), "阿司匹林"),
    commonDrugOcrTarget(listOf("波立维", "氯吡格雷"), "氯吡格雷"),
    commonDrugOcrTarget(listOf("达美康", "格列美脲"), "格列美脲"),
    commonDrugOcrTarget(listOf("速效救心丸", "速效救心丸"), "速效救心丸"),
    commonDrugOcrTarget(listOf("复方丹参滴丸", "复方丹参滴丸"), "复方丹参滴丸"),
    commonDrugOcrTarget(listOf("感康", "感冒灵"), "感冒灵"),
    commonDrugOcrTarget(listOf("白加黑", "氨酚伪麻"), "氨酚伪麻")
)

@ComposePreview(showBackground = true, widthDp = 420, heightDp = 900)
@Composable
private fun WeiguangPlusPreview() {
    WeiguangplusTheme { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { WeiguangPlusApp() } }
}