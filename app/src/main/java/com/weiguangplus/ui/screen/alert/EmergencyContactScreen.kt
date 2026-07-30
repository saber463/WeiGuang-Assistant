package com.weiguangplus.ui.screen.alert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weiguangplus.core.emergency.EmergencyContactManager
import com.weiguangplus.core.emergency.EmergencyContactManager.Contact

private val Blue = Color(0xFF1565C0)
private val Bg = Color(0xFFFAFAFA)
private val T1 = Color(0xFF212121)
private val T2 = Color(0xFF757575)
private val White = Color.White
private val Red = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyContactScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    EmergencyContactManager.init(context)

    var contacts by remember { mutableStateOf(EmergencyContactManager.getContacts()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var editIndex by remember { mutableStateOf(-1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("紧急联系人", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← 返回", fontSize = 14.sp, color = White)
                    }
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "SOS 紧急求助将向以下联系人发送求救短信",
                fontSize = 13.sp, color = T2, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (contacts.isEmpty()) {
                Card(
                    Modifier.fillMaxWidth(),
                    RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️", fontSize = 32.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("尚未添加紧急联系人", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = T1)
                        Spacer(Modifier.height(4.dp))
                        Text("请至少添加一个联系人以启用 SOS 功能", fontSize = 12.sp, color = T2)
                    }
                }
            } else {
                contacts.forEachIndexed { index, contact ->
                    Card(
                        Modifier.fillMaxWidth(),
                        RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = White)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(contact.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = T1)
                                Text(contact.phone, fontSize = 14.sp, color = T2)
                            }
                            IconButton(onClick = {
                                editIndex = index
                                newName = contact.name
                                newPhone = contact.phone
                                showAddDialog = true
                            }) {
                                Text("✏️", fontSize = 18.sp)
                            }
                            IconButton(onClick = {
                                val updated = contacts.toMutableList()
                                updated.removeAt(index)
                                contacts = updated
                                EmergencyContactManager.setContacts(updated)
                            }) {
                                Text("🗑️", fontSize = 18.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    newName = ""
                    newPhone = ""
                    editIndex = -1
                    showAddDialog = true
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue)
            ) {
                Text("+ 添加紧急联系人", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // 添加/编辑对话框
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    if (editIndex >= 0) "编辑联系人" else "添加紧急联系人",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("姓名") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("手机号") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newPhone.isNotBlank()) {
                            val updated = contacts.toMutableList()
                            if (editIndex >= 0) {
                                updated[editIndex] = Contact(newName.trim(), newPhone.trim())
                            } else {
                                updated.add(Contact(newName.trim(), newPhone.trim()))
                            }
                            contacts = updated
                            EmergencyContactManager.setContacts(updated)
                            showAddDialog = false
                        }
                    },
                    enabled = newName.isNotBlank() && newPhone.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue)
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("取消", color = T2)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

