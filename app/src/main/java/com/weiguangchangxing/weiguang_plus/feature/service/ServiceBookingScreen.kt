package com.weiguangchangxing.weiguang_plus.feature.service

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceBookingScreen(modifier: Modifier = Modifier) {
    val serviceTypes = listOf(
        ServiceType("手语翻译随行", "专业手语翻译员全程陪同"),
        ServiceType("一对一陪同出行", "志愿者陪您安全出行"),
        ServiceType("应急联系人对接", "对接基地帮扶中心")
    )

    var selectedService by remember { mutableStateOf<ServiceType?>(null) }
    var bookingDate by remember { mutableStateOf("") }
    var bookingTime by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ScrollPage(modifier = modifier) {
        HeroCard(
            title = "基地帮扶服务",
            subtitle = "手语翻译预约·陪同出行·专属服务"
        )

        SectionTitle("选择服务类型")

        serviceTypes.forEach { service ->
            val isSelected = selectedService == service
            Card(
                onClick = {
                    selectedService = service
                    isSubmitted = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = if (isSelected) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                },
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 4.dp else 1.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = service.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (selectedService != null) {
            SectionTitle("预约信息")
            InfoCard {
                OutlinedTextField(
                    value = bookingDate,
                    onValueChange = { bookingDate = it },
                    label = { Text("预约日期") },
                    placeholder = { Text("例如：2026-05-25") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = bookingTime,
                    onValueChange = { bookingTime = it },
                    label = { Text("预约时间") },
                    placeholder = { Text("例如：14:30") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注说明") },
                    placeholder = { Text("如有特殊需求请在此说明...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        isSubmitted = true
                        scope.launch {
                            snackbarHostState.showSnackbar("预约已提交，我们将尽快与您联系确认。")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "提交预约",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (isSubmitted) {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "✅ 预约已成功提交！我们将尽快与您联系确认时间。",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "如需紧急服务，请使用「应急求助」功能",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    SnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(16.dp))
}

private data class ServiceType(val name: String, val description: String)

@Composable
private fun ScrollPage(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}