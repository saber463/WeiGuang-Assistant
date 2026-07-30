package com.weiguangplus.ui.screen.alert

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.weiguangplus.core.FlashlightController

private val Blue = Color(0xFF1565C0)
private val Bg = Color(0xFFFAFAFA)
private val T1 = Color(0xFF212121)
private val T2 = Color(0xFF757575)
private val White = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertSettingsScreen(onNavigate: (String) -> Unit = {}) {
    val context = LocalContext.current
    var vibrateEnabled by remember { mutableStateOf(true) }
    var flashEnabled by remember { mutableStateOf(false) }
    var soundBoostEnabled by remember { mutableStateOf(false) }
    var notifyMonitorEnabled by remember { mutableStateOf(true) }
    var vibrateLevel by remember { mutableStateOf(2) } // 0=off, 1=low, 2=medium, 3=high

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notifyMonitorEnabled = granted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("提醒设置", fontWeight = FontWeight.Bold) },
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
                .semantics { contentDescription = "提醒设置" },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ===== 震动提醒 =====
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
                            Text("震动提醒", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = T1)
                            Text("来电/闹钟/通知时震动", fontSize = 12.sp, color = T2)
                        }
                        Switch(
                            checked = vibrateEnabled,
                            onCheckedChange = { vibrateEnabled = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = Blue)
                        )
                    }

                    if (vibrateEnabled) {
                        Spacer(Modifier.height(12.dp))
                        Text("震动强度", fontSize = 13.sp, color = T2)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for ((idx, label) in listOf("轻", "中", "强", "急").withIndex()) {
                                Button(
                                    onClick = { vibrateLevel = idx + 1 },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (vibrateLevel == idx + 1) Blue else Color(0xFFE0E0E0),
                                        contentColor = if (vibrateLevel == idx + 1) White else T2
                                    )
                                ) {
                                    Text(label, fontSize = 13.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { /* TODO: 测试震动 */ },
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue.copy(alpha = 0.1f), contentColor = Blue)
                        ) {
                            Text("测试震动", fontSize = 13.sp)
                        }
                    }
                }
            }

            // ===== 闪光灯提醒 =====
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
                            Text("闪光灯提醒", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = T1)
                            Text(
                                if (FlashlightController.isFlashAvailable()) "来电时手机闪光灯闪烁"
                                else "您的设备不支持闪光灯",
                                fontSize = 12.sp, color = T2
                            )
                        }
                        Switch(
                            checked = flashEnabled,
                            onCheckedChange = {
                                flashEnabled = it
                                if (it && FlashlightController.isFlashAvailable()) {
                                    FlashlightController.blink(2, 300, 200)
                                }
                            },
                            enabled = FlashlightController.isFlashAvailable(),
                            colors = SwitchDefaults.colors(checkedTrackColor = Blue)
                        )
                    }
                    if (flashEnabled) {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { FlashlightController.blink(3, 300, 200) },
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue.copy(alpha = 0.1f), contentColor = Blue)
                        ) {
                            Text("测试闪光", fontSize = 13.sp)
                        }
                    }
                }
            }

            // ===== 声音增强 =====
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
                            Text("声音增强", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = T1)
                            Text("自动解除静音并拉满音量", fontSize = 12.sp, color = T2)
                        }
                        Switch(
                            checked = soundBoostEnabled,
                            onCheckedChange = { soundBoostEnabled = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = Blue)
                        )
                    }
                }
            }

            // ===== 通知监听 =====
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
                            Text("通知监听", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = T1)
                            Text("监听关键应用通知并提醒", fontSize = 12.sp, color = T2)
                        }
                        Switch(
                            checked = notifyMonitorEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    notifyMonitorEnabled = enabled
                                }
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = Blue)
                        )
                    }
                }
            }

            
            // ===== 紧急联系人 =====
            Card(
                Modifier.fillMaxWidth().clickable(onClick = { onNavigate("emergency_contacts") }),
                RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("紧急联系人", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = T1)
                        Text("SOS 求救短信接收人", fontSize = 12.sp, color = T2)
                    }
                    Text("→", fontSize = 20.sp, color = T2)
                }
            }

            Spacer(Modifier.weight(1f))
            Text(
                "提醒设置实时生效，无需保存",
                fontSize = 11.sp, color = T2,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
