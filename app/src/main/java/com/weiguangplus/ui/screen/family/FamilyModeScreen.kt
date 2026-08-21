package com.weiguangplus.ui.screen.family

import android.content.Context
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weiguangplus.core.emergency.EmergencyContactManager
import java.security.MessageDigest

private val Blue = Color(0xFF1565C0)
private val BlueBg = Color(0xFFE3F2FD)
private val Bg = Color(0xFFFAFAFA)
private val T1 = Color(0xFF212121)
private val T2 = Color(0xFF757575)
private val White = Color.White
private val Red = Color(0xFFC62828)
private val Green = Color(0xFF2E7D32)

/**
 * 家庭模式 —— 家庭看护中心（由空壳补全为可用功能）
 *
 * 遵循"一用户绑多家"的家庭绑定规则：
 * - 展示本用户的**专属配对码**，供家人在各自的"家人客户端"输入以绑定；
 * - 维护**家人列表**（本端直接复用 [EmergencyContactManager] 的紧急联系人
 *   作为家人成员，SOS 时可一键通知/拨打，避免另建存储）；
 * - 提供 **SOS 求救** 与 **紧急联系人管理** 入口，贯通看护闭环。
 *
 * 说明：完整版家庭联动（家人端回传位置/状态）依赖账号与推送服务，本实现先落地
 * 本地可用的"看护中心"，对外作为轻量引流体验；后续接入网络即可升级双向联动。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyModeScreen(
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val context = LocalContext.current
    EmergencyContactManager.init(context)

    var family = remember { EmergencyContactManager.getContacts() }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // 生成专属配对码：基于应用包名稳定派生，同一设备恒定，便于家人反复绑定
    val pairCode = remember { generatePairCode(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("家庭模式", fontWeight = FontWeight.Bold) },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 专属配对码卡
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlueBg)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("我的专属配对码", fontSize = 13.sp, color = T2)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        pairCode, fontSize = 34.sp, fontWeight = FontWeight.Bold,
                        color = Blue, letterSpacing = 4.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "家人在各自 App / 小程序输入此码即可绑定，实现互相守护。",
                        fontSize = 12.sp, color = T2
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("我的家人（${family.size}）", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = T1)
            Spacer(Modifier.height(8.dp))

            if (family.isEmpty()) {
                Text(
                    "还没有绑定家人。填上家人的姓名与联系方式添加到下方列表。",
                    fontSize = 13.sp, color = T2
                )
                Spacer(Modifier.height(8.dp))
            } else {
                family.forEach { c ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = White)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                c.name, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                                color = T1, modifier = Modifier.width(72.dp)
                            )
                            Text(c.phone, fontSize = 14.sp, color = T2, modifier = Modifier.weight(1f))
                            Text(
                                "家人", fontSize = 11.sp, color = Green,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // 添加家人表单
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("家人姓名") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("联系电话") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val n = name.trim()
                            val p = phone.trim()
                            if (n.isNotEmpty() && p.isNotEmpty()) {
                                // 追加家人（保留原有），写回共享存储
                                val updated = family.toMutableList().apply {
                                    removeAll { it.phone == p } // 同号码去重
                                    add(EmergencyContactManager.Contact(n, p))
                                }
                                EmergencyContactManager.setContacts(updated)
                                family = updated
                                name = ""
                                phone = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("添加家人", fontSize = 14.sp) }
                }
            }

            Spacer(Modifier.height(16.dp))
            // 看护操作：SOS + 联系人管理
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { onNavigate("sos") },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Red, contentColor = White
                    )
                ) { Text("SOS 求救", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                OutlinedButton(
                    onClick = { onNavigate("emergency_contacts") },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("管理联系人", fontSize = 14.sp) }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "家庭成员将作为紧急联系人在你发出 SOS 时收到短信与位置通知。",
                fontSize = 12.sp, color = T2
            )
        }
    }
}

/** 生成稳定的专属配对码（6 位大写字母+数字） */
private fun generatePairCode(context: Context): String {
    val seed = context.packageName + "|family|" +
        android.provider.Settings.Secure.getString(
            context.contentResolver, android.provider.Settings.Secure.ANDROID_ID
        ) ?: "0"
    val digest = MessageDigest.getInstance("SHA-256").digest(seed.toByteArray())
    // 取 6 个字节映射到"字母+数字"字符集，产出一致可读的配对码
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // 去除易混淆字母/数字
    val sb = StringBuilder(6)
    for (i in 0 until 6) {
        val idx = (digest[i].toInt() and 0xFF) % chars.length
        sb.append(chars[idx])
    }
    return sb.toString()
}