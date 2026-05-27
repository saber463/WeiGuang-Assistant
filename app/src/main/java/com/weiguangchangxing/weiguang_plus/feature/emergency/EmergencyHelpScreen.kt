package com.weiguangchangxing.weiguang_plus.feature.emergency

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weiguangchangxing.weiguang_plus.core.emergency.EmergencyContact
import com.weiguangchangxing.weiguang_plus.core.emergency.EmergencyContactManager
import com.weiguangchangxing.weiguang_plus.core.emergency.SosHelper
import com.weiguangchangxing.weiguang_plus.core.tts.TTSManager

private val sceneNames = listOf(
    "身体不适", "迷路走失", "交通事故",
    "发现火警", "遭遇危险", "其他求助"
)

private val sceneIcons = listOf(
    Icons.Filled.Person, Icons.Filled.Warning, Icons.Filled.Warning,
    Icons.Filled.Warning, Icons.Filled.Warning, Icons.Filled.Info
)

@Composable
fun EmergencyHelpScreen(modifier: Modifier = Modifier) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedScene by remember { mutableStateOf("") }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var contacts by remember { mutableStateOf(EmergencyContactManager.getContacts()) }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newRelation by remember { mutableStateOf("") }
    val context = LocalContext.current

    ScrollPage(modifier = modifier) {
        HeroCard(
            title = "一键应急求助",
            subtitle = "6种预设求助场景，一键发送位置+求助信息给紧急联系人"
        )

        SectionTitle("预设求助场景")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            sceneNames.take(3).forEachIndexed { index, scene ->
                SceneButton(
                    name = scene,
                    icon = sceneIcons[index],
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedScene = scene
                        TTSManager.speakNow("已选择$scene")
                        showConfirmDialog = true
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            sceneNames.drop(3).forEachIndexed { index, scene ->
                SceneButton(
                    name = scene,
                    icon = sceneIcons[index + 3],
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedScene = scene
                        TTSManager.speakNow("已选择$scene")
                        showConfirmDialog = true
                    }
                )
            }
        }

        SectionTitle("紧急联系人")
        if (contacts.isEmpty()) {
            InfoCard {
                Text(
                    text = "暂未设置紧急联系人",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            contacts.forEach { contact ->
                ContactCard(contact = contact)
            }
        }
        OutlinedButton(
            onClick = { showAddContactDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("添加联系人")
        }

        SectionTitle("紧急操作")
        Button(
                onClick = {
                    val location = SosHelper.getLocationDescription(context)
                    SosHelper.sendSosAlert(context, contacts, "一键SOS求助", location)
                    TTSManager.speakNow("已发送一键 SOS 求助信息")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "一键发送 SOS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    SosHelper.triggerFlashlight(context)
                    TTSManager.speakNow("已打开闪光灯")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.FlashOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("打开闪光灯")
            }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("确认求助") },
            text = { Text("确认发送【$selectedScene】求助信息？") },
            confirmButton = {
                TextButton(onClick = {
                    val location = SosHelper.getLocationDescription(context)
                    SosHelper.sendSosAlert(context, contacts, selectedScene, location)
                    showConfirmDialog = false
                }) {
                    Text("确认发送")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showAddContactDialog) {
        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = { Text("添加紧急联系人") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("姓名") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("电话") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newRelation,
                        onValueChange = { newRelation = it },
                        label = { Text("关系") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank() && newPhone.isNotBlank()) {
                        EmergencyContactManager.addContact(
                            EmergencyContact(
                                name = newName,
                                phone = newPhone,
                                relation = newRelation
                            )
                        )
                        contacts = EmergencyContactManager.getContacts()
                        newName = ""
                        newPhone = ""
                        newRelation = ""
                        showAddContactDialog = false
                    }
                }) {
                    Text("添加")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun SceneButton(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .width(100.dp)
            .height(64.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.height(20.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ContactCard(contact: EmergencyContact) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Person, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${contact.phone} · ${contact.relation}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}