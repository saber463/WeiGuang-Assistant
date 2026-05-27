package com.weiguangchangxing.weiguang_plus.feature.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FacilityReportScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val problemTypes = listOf("盲道被占用", "无障碍通道堵塞", "红绿灯故障", "其他问题")
    var selectedProblemType by remember { mutableStateOf<String?>(null) }
    var problemDescription by remember { mutableStateOf("") }
    var locationStatus by remember { mutableStateOf("当前定位：（等待获取...）") }
    var locationObtained by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }
    val submittedReports = remember { mutableStateListOf<String>() }

    ScrollPage(modifier = modifier) {
        HeroCard(
            title = "设施问题上报",
            subtitle = "拍照上传·标记问题位置·推动改善"
        )

        SectionTitle("问题类型")

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            problemTypes.forEach { type ->
                FilterChip(
                    selected = selectedProblemType == type,
                    onClick = {
                        selectedProblemType = type
                        isSubmitted = false
                    },
                    label = {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        SectionTitle("问题描述")
        OutlinedTextField(
            value = problemDescription,
            onValueChange = { problemDescription = it },
            placeholder = { Text("请描述您遇到的问题...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 6,
            shape = RoundedCornerShape(16.dp)
        )

        SectionTitle("位置信息")
        InfoCard {
            Text(
                text = locationStatus,
                style = MaterialTheme.typography.bodyLarge,
                color = if (locationObtained) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission && locationManager != null) {
                        val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (lastLocation != null) {
                            locationStatus = "已获取位置：${"%.4f".format(lastLocation.latitude)}, ${"%.4f".format(lastLocation.longitude)}"
                            locationObtained = true
                        } else {
                            locationStatus = "已获取位置：基于网络定位"
                            locationObtained = true
                        }
                    } else {
                        locationStatus = "已获取位置：基于网络定位"
                        locationObtained = true
                    }
                },
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (locationObtained) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                Text(
                    text = if (locationObtained) "重新获取位置" else "获取位置",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        SectionTitle("图片上传")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { /* 模拟拍照 */ },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "拍照上传",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            OutlinedButton(
                onClick = { /* 模拟选择 */ },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "从相册选择",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = {
                val typeText = selectedProblemType ?: "未选择类型"
                submittedReports.add("[$typeText] $problemDescription")
                isSubmitted = true
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
                text = "提交反馈",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (isSubmitted) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "感谢您的反馈！我们将尽快处理。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                )
            }
        }

        if (submittedReports.isNotEmpty()) {
            SectionTitle("已提交的反馈（${submittedReports.size}）")
            submittedReports.reversed().forEach { report ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = report,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
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