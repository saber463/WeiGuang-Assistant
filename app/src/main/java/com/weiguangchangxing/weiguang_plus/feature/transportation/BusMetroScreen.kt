package com.weiguangchangxing.weiguang_plus.feature.transportation

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.weiguangchangxing.weiguang_plus.core.service.BusLine
import com.weiguangchangxing.weiguang_plus.core.service.BusStop
import com.weiguangchangxing.weiguang_plus.core.service.BusStopMonitor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusMetroScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val monitor = remember { BusStopMonitor(context) }
    val busLines = remember { monitor.getAvailableLines() }
    var selectedLineIndex by remember { mutableStateOf(-1) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var nearestStopName by remember { mutableStateOf("") }
    var nearestDistance by remember { mutableStateOf(9999f) }

    ScrollPage(modifier = modifier) {
        HeroCard(
            title = "公交地铁报站",
            subtitle = "GPS定位·到站震动提醒·大字体高对比度"
        )

        SectionTitle("选择线路")
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it }
        ) {
            val selectedName = if (selectedLineIndex >= 0) busLines[selectedLineIndex].name else "请选择线路"
            OutlinedTextField(
                value = selectedName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(16.dp)
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                busLines.forEachIndexed { index, line ->
                    DropdownMenuItem(
                        text = { Text(line.name) },
                        onClick = {
                            selectedLineIndex = index
                            dropdownExpanded = false
                            val pos = monitor.getLocation()
                            if (pos != null) {
                                val result = monitor.findNearestStop(pos.first, pos.second, line)
                                if (result != null) {
                                    nearestStopName = result.first.name
                                    nearestDistance = result.second
                                } else {
                                    nearestStopName = "不在线路上"
                                    nearestDistance = 9999f
                                }
                            } else {
                                nearestStopName = "等待GPS定位"
                                nearestDistance = 9999f
                            }
                        }
                    )
                }
            }
        }

        if (selectedLineIndex >= 0) {
            val currentLine = busLines[selectedLineIndex]
            SectionTitle("站点列表")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    currentLine.stops.forEach { stop ->
                        val isNearest = stop.name == nearestStopName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isNearest && nearestDistance < 1000f) {
                                        Modifier.background(
                                            color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    } else Modifier
                                )
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isNearest && nearestDistance < 1000f) "●" else "○",
                                color = if (isNearest && nearestDistance < 1000f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stop.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isNearest && nearestDistance < 1000f) FontWeight.Bold else FontWeight.Normal,
                                color = if (isNearest && nearestDistance < 1000f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                            )
                            if (isNearest && nearestDistance < 1000f) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "最近",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                }
            }

            SectionTitle("当前状态")
            val statusText = when {
                nearestDistance < 100f -> "即将到站"
                nearestDistance < 300f -> "正在接近"
                nearestDistance < 1000f -> "距离较远"
                else -> "距离较远"
            }
            val isArriving = nearestDistance < 100f

            if (nearestDistance >= 9999f) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = if (nearestStopName == "等待GPS定位") "请开启GPS定位以获取实时站点信息" else "请选择您当前乘坐的线路",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            } else if (isArriving) {
                ArrivingAlertCard(
                    stopName = nearestStopName,
                    distance = nearestDistance,
                    status = statusText
                )
            } else {
                StatusCard(
                    stopName = nearestStopName,
                    distance = nearestDistance,
                    status = statusText
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "请先选择公交或地铁线路，系统将自动定位并显示最近站点信息。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(18.dp)
                )
            }
        }

        SectionTitle("震动测试")
        Button(
            onClick = { triggerVibration(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "到站震动测试",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ArrivingAlertCard(stopName: String, distance: Float, status: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .alpha(blinkAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stopName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "距离 ${distance.toInt()} 米",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun StatusCard(stopName: String, distance: Float, status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stopName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "距离 ${distance.toInt()} 米",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

private fun triggerVibration(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
            ?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(
            VibrationEffect.createWaveform(
                longArrayOf(0, 300, 100, 300, 100, 300),
                intArrayOf(0, 200, 0, 200, 0, 200),
                -1
            )
        )
    } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(longArrayOf(0, 300, 100, 300, 100, 300), -1)
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