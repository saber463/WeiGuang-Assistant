package com.weiguangchangxing.weiguang_plus.feature.map

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.weiguangchangxing.weiguang_plus.core.map.AccessibleLocation
import com.weiguangchangxing.weiguang_plus.core.map.AccessibleRoutePlanner

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccessibleMapScreen(modifier: Modifier = Modifier) {
    val allLocations = remember { AccessibleRoutePlanner.getAccessibleDestinations() }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(allLocations) }
    var selectedLocation by remember { mutableStateOf<AccessibleLocation?>(null) }
    var showRouteDialog by remember { mutableStateOf(false) }

    ScrollPage(modifier = modifier) {
        HeroCard(
            title = "无障碍设施地图",
            subtitle = "附近无障碍通道·盲道·辅助设施"
        )

        SectionTitle("目的地搜索")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("目的地") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            Button(
                onClick = {
                    searchResults = if (searchQuery.isBlank()) {
                        allLocations
                    } else {
                        allLocations.filter {
                            it.name.contains(searchQuery, ignoreCase = true)
                        }
                    }
                },
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "搜索",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        SectionTitle("附近设施（${searchResults.size}）")
        if (searchResults.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "未找到匹配的设施",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                )
            }
        } else {
            searchResults.forEach { location ->
                FacilityCard(
                    location = location,
                    onViewRoute = {
                        selectedLocation = location
                        showRouteDialog = true
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showRouteDialog && selectedLocation != null) {
        val loc = selectedLocation!!
        AlertDialog(
            onDismissRequest = { showRouteDialog = false },
            title = {
                Text(
                    text = "前往${loc.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RouteInfoRow("目的地", loc.name)
                    RouteInfoRow(
                        "无障碍通道",
                        if (loc.facilities.contains("无障碍通道")) "有" else "—"
                    )
                    RouteInfoRow(
                        "盲道",
                        if (loc.facilities.contains("盲道") || loc.facilities.contains("盲文图书")) "有" else "—"
                    )
                    RouteInfoRow(
                        "音频信号",
                        if (loc.facilities.contains("音频导览")) "有" else "—"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "温馨提示：出行前建议规划好路线，注意交通安全。",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRouteDialog = false },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "知道了",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRouteDialog = false }) {
                    Text(
                        text = "关闭",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        )
    }
}

@Composable
private fun RouteInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FacilityCard(
    location: AccessibleLocation,
    onViewRoute: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = location.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                location.facilities.forEach { feature ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onViewRoute,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "查看路线",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
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