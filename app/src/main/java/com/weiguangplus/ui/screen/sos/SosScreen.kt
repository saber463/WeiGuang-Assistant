package com.weiguangplus.ui.screen.sos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.weiguangplus.core.emergency.EmergencyContactManager
import com.weiguangplus.core.emergency.SosManager
import com.weiguangplus.core.emergency.SosHistoryManager
import com.weiguangplus.core.emergency.ShakeDetector
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Red = Color(0xFFD32F2F)
private val RedDark = Color(0xFFB71C1C)
private val Bg = Color(0xFFFAFAFA)
private val T1 = Color(0xFF212121)
private val T2 = Color(0xFF757575)
private val White = Color.White
private val Blue = Color(0xFF1565C0)
private val Green = Color(0xFF2E7D32)
private val Orange = Color(0xFFF57C00)

private enum class SosState { READY, COUNTDOWN, RESULT }

@Composable
fun SosScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf(SosState.READY) }
    var countdown by remember { mutableStateOf(3) }
    var result by remember { mutableStateOf<SosManager.SosResult?>(null) }
    var historyCount by remember { mutableStateOf(0) }
    var countdownJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    LaunchedEffect(Unit) {
        SosHistoryManager.init(context)
        historyCount = SosHistoryManager.getRecords(context).size
    }



    val neededPermissions = listOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CAMERA
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.all { it }) {
            state = SosState.COUNTDOWN
            countdown = 3
            countdownJob = startCountdown(scope) { remaining ->
                countdown = remaining
                if (remaining <= 0) {
                    SosManager.trigger(context) { r ->
                        SosHistoryManager.record(context, r)
                        result = r
                        state = SosState.RESULT
                    }
                }
            }
        }
    }

    val allGranted = neededPermissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    val hasContacts = remember { EmergencyContactManager.hasContacts() }

    // 触发 SOS 流程
    val triggerSos: () -> Unit = {
        if (!hasContacts) {
            result = SosManager.SosResult(false, 0, null, "未设置紧急联系人，请先在提醒设置中添加")
            state = SosState.RESULT
        } else if (!allGranted) {
            permissionLauncher.launch(neededPermissions.toTypedArray())
        } else {
            state = SosState.COUNTDOWN
            countdown = 3
            countdownJob = startCountdown(scope) { remaining ->
                countdown = remaining
                if (remaining <= 0) {
                    SosManager.trigger(context) { r ->
                        SosHistoryManager.record(context, r)
                        result = r
                        historyCount = SosHistoryManager.getRecords(context).size
                        state = SosState.RESULT
                    }
                }
            }
        }
    }


    // 摇一摇触发 SOS
    DisposableEffect(Unit) {
        ShakeDetector.start(context) {
            triggerSos()
        }
        onDispose { ShakeDetector.stop() }
    }


    when (state) {
        SosState.READY -> ReadyView(
            onBack = onBack,
            hasContacts = hasContacts,
            allGranted = allGranted,
            historyCount = historyCount,
            onTrigger = triggerSos
        )
        SosState.COUNTDOWN -> CountdownView(
            countdown = countdown,
            onCancel = {
                countdownJob?.cancel()
                countdownJob = null
                countdown = 3
                state = SosState.READY
            }
        )
        SosState.RESULT -> ResultView(
            result = result!!,
            onBack = onBack,
            onReset = {
                countdownJob?.cancel()
                countdownJob = null
                countdown = 3
                result = null
                state = SosState.READY
            },
            onResend = {
                state = SosState.COUNTDOWN
                countdown = 3
                countdownJob = startCountdown(scope) { remaining ->
                    countdown = remaining
                    if (remaining <= 0) {
                        SosManager.trigger(context) { r ->
                            SosHistoryManager.record(context, r)
                            result = r
                            historyCount = SosHistoryManager.getRecords(context).size
                            state = SosState.RESULT
                        }
                    }
                }
            }
        )
    }
}

// 倒计时协程：从 3 倒数到 0，每秒回调一次
private fun startCountdown(
    scope: kotlinx.coroutines.CoroutineScope,
    onTick: (Int) -> Unit
): kotlinx.coroutines.Job {
    return scope.launch {
        for (i in 3 downTo 0) {
            onTick(i)
            if (i > 0) delay(1000)
        }
    }
}

// ==================== Ready 视图 ====================
@Composable
private fun ReadyView(
    onBack: () -> Unit,
    hasContacts: Boolean,
    allGranted: Boolean,
    historyCount: Int,
    onTrigger: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Bg).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            TextButton(onClick = onBack) {
                Text("← 返回", fontSize = 14.sp, color = Blue)
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("紧急求救", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = T1)
        Spacer(Modifier.height(8.dp))
        Text(
            "按下按钮后有 3 秒确认时间，可取消",
            fontSize = 14.sp, color = T2, textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        // SOS 主按钮
        Button(
            onClick = onTrigger,
            modifier = Modifier.size(160.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Red)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SOS", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = White)
                Text("长按或点击", fontSize = 11.sp, color = White.copy(alpha = 0.7f))
            }
        }

        Spacer(Modifier.height(24.dp))

        // 状态提示
        if (!hasContacts) {
            WarningCard("⚠️ 请先在提醒设置中添加紧急联系人")
        }
        if (hasContacts && !allGranted) {
            WarningCard("ℹ️ 首次使用需要授予短信、位置和相机权限", Color(0xFFE3F2FD))
        }
        if (hasContacts && historyCount > 0) {
            Card(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Text(
                    "历史求救记录: $historyCount 次",
                    fontSize = 12.sp, color = T2,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "微光同行 · 紧急求助系统",
            fontSize = 11.sp, color = T2, textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WarningCard(message: String, bg: Color = Color(0xFFFFF3E0)) {
    Card(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Text(
            message,
            fontSize = 13.sp, color = T1,
            modifier = Modifier.padding(14.dp),
            textAlign = TextAlign.Center
        )
    }
}

// ==================== 倒计时视图 ====================
@Composable
private fun CountdownView(
    countdown: Int,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (countdown > 0) RedDark else Red,
        animationSpec = tween(300),
        label = "bgColor"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (countdown > 0) {
                Text(
                    "$countdown",
                    fontSize = 96.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = White,
                    modifier = Modifier.scale(pulseScale)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "正在触发紧急求救...",
                    fontSize = 18.sp, color = White.copy(alpha = 0.9f)
                )
                Spacer(Modifier.height(40.dp))
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = White),
                    modifier = Modifier.height(48.dp).width(160.dp)
                ) {
                    Text("取 消", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text("🚨", fontSize = 64.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "求救信号已发送",
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = White
                )
            }
        }
    }
}

// ==================== 结果视图 ====================
@Composable
private fun ResultView(
    result: SosManager.SosResult,
    onBack: () -> Unit,
    onReset: () -> Unit,
    onResend: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().background(Bg).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            TextButton(onClick = onBack) {
                Text("← 返回主页", fontSize = 14.sp, color = Blue)
            }
        }

        Spacer(Modifier.height(24.dp))

        // 结果卡片
        Card(
            Modifier.fillMaxWidth(),
            RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (result.success) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            )
        ) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (result.success) "✅" else "❌",
                    fontSize = 48.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    if (result.success) "已发送 ${result.smsSent} 条求救短信"
                    else "发送失败",
                    fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = if (result.success) Green else Red
                )
                result.error?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, fontSize = 13.sp, color = Red)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 位置信息卡片
        result.location?.let { loc ->
            Card(
                Modifier.fillMaxWidth(),
                RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("📍 位置坐标", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = T1)
                    Spacer(Modifier.height(4.dp))
                    Text(loc, fontSize = 13.sp, color = T2)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val uri = Uri.parse("geo:$loc?q=$loc")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            },
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Blue.copy(alpha = 0.1f),
                                contentColor = Blue
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("🗺️ 打开地图", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                    as android.content.ClipboardManager
                                cm.setPrimaryClip(
                                    android.content.ClipData.newPlainText("SOS坐标", loc)
                                )
                            },
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE0E0E0),
                                contentColor = T1
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("📋 复制坐标", fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // 一键拨号按钮组
        Card(
            Modifier.fillMaxWidth(),
            RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("📞 紧急拨号", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = T1)
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EmergencyCallButton("警察 110", "110", Blue, Modifier.weight(1f))
                    EmergencyCallButton("急救 120", "120", Red, Modifier.weight(1f))
                    EmergencyCallButton("火警 119", "119", Orange, Modifier.weight(1f))
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // 操作按钮
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onResend,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red)
            ) {
                Text("重新发送", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onReset,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0), contentColor = T1)
            ) {
                Text("完成", fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun EmergencyCallButton(
    label: String,
    number: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Button(
        onClick = {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
        },
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.1f),
            contentColor = color
        ),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
