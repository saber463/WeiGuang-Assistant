package com.weiguangplus.ui.screen.caption

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.weiguangplus.core.caption.CaptionOverlayService

private val Blue = Color(0xFF1565C0)
private val Bg = Color(0xFFFAFAFA)
private val T1 = Color(0xFF212121)
private val T2 = Color(0xFF757575)
private val White = Color.White

/**
 * 字幕悬浮窗设置界面（G2）
 *
 * 提供：
 *  - 悬浮窗总开关（开启/关闭 CaptionOverlayService）
 *  - 首次使用引导：授予录音权限 + 悬浮窗权限
 *  - 运行状态提示
 *
 * 设计（WHY）：
 *  - 开关只负责 "启停前台服务 + 状态提示"，不重复实现识别逻辑（服务内自包含）
 *  - 权限缺失时用系统 intent 引导到对应设置页，保证首次使用体验顺滑
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptionSettingsScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    var batteryMonitoring by remember { mutableStateOf(false) }

    // 录音权限请求器
    val recordPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 授权后由用户再点开关即可 */ }

    // 悬浮窗权限请求器：跳系统设置页
    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    fun hasRecordPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("字幕悬浮窗", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        Text("←", color = White, fontSize = 18.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue, titleContentColor = White, navigationIconContentColor = White
                )
            )
        },
        containerColor = Bg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
                .semantics { contentDescription = "字幕悬浮窗设置" },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ===== 功能说明 =====
            Card(
                Modifier.fillMaxWidth(),
                RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("实时语音字幕", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = T1)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "在任意应用上方悬浮显示实时语音转文字字幕，\n覆盖面对面交流、会议、看视频等全场景。",
                        fontSize = 13.sp, color = T2
                    )
                }
            }

            // ===== 开启开关 =====
            Card(
                Modifier.fillMaxWidth(),
                RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("开启字幕悬浮窗", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = T1)
                            Text("开启后所有应用显示字幕", fontSize = 12.sp, color = T2)
                        }
                        Switch(
                            checked = batteryMonitoring,
                            onCheckedChange = { enabled ->
                                when {
                                    // 需要录音权限
                                    enabled && !hasRecordPermission() -> {
                                        recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                    // 需要悬浮窗权限
                                    enabled && !Settings.canDrawOverlays(context) -> {
                                        overlayPermissionLauncher.launch(
                                            Intent(
                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                android.net.Uri.parse("package:${context.packageName}")
                                            )
                                        )
                                        batteryMonitoring = true
                                    }
                                    else -> {
                                        batteryMonitoring = enabled
                                        if (enabled) {
                                            ContextCompat.startForegroundService(
                                                context, Intent(context, CaptionOverlayService::class.java)
                                            )
                                        } else {
                                            context.stopService(Intent(context, CaptionOverlayService::class.java))
                                        }
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = Blue)
                        )
                    }

                    // 权限状态提示
                    Spacer(Modifier.height(8.dp))
                    val recOk = hasRecordPermission()
                    val overlayOk = Settings.canDrawOverlays(context)
                    Text(
                        "录音权限：${if (recOk) "✅ 已授予" else "❌ 未授予"}  悬浮窗权限：${if (overlayOk) "✅ 已授予" else "❌ 未授予"}",
                        fontSize = 12.sp,
                        color = if (recOk && overlayOk) T2 else Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}