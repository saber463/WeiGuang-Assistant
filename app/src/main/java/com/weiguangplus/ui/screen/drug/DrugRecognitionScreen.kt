package com.weiguangplus.ui.screen.drug

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weiguangplus.ui.viewmodel.DrugViewModel

private val Blue = Color(0xFF1565C0)
private val LightBlue = Color(0xFFE3F2FD)
private val Bg = Color(0xFFFAFAFA)
private val T1 = Color(0xFF212121)
private val T2 = Color(0xFF757575)
private val Warn = Color(0xFFEF6C00)

@Composable
fun DrugRecognitionScreen(
    modifier: Modifier = Modifier,
    viewModel: DrugViewModel = hiltViewModel()
) {
    val s by viewModel.recognitionState.collectAsStateWithLifecycle()
    val h by viewModel.historyState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize().background(Bg).padding(24.dp)
            .semantics { contentDescription = "药品识别" },
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("药品识别", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = T1)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { viewModel.launchCamera() },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue)
            ) {
                Text("拍照识别", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            Button(
                onClick = { viewModel.launchGallery() },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White, contentColor = Blue
                )
            ) {
                Text("从相册选", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }

        when {
            s is DrugViewModel.RecognitionUiState.Loading -> Box(
                Modifier.fillMaxWidth().height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Blue)
                    Spacer(Modifier.height(12.dp))
                    Text("正在识别...", color = T2, fontSize = 14.sp)
                }
            }
            s is DrugViewModel.RecognitionUiState.Success -> Card(
                Modifier.fillMaxWidth(),
                RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = LightBlue)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        text = (s as DrugViewModel.RecognitionUiState.Success).drug.genericName ?: "识别结果",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = T1
                    )
                    Spacer(Modifier.height(8.dp))
                    (s as DrugViewModel.RecognitionUiState.Success).drug.tradeName?.let {
                        Text("商品名：" + it, fontSize = 14.sp, color = T2)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when ((s as DrugViewModel.RecognitionUiState.Success).drug.riskLevel) {
                                "high" -> "高风险"
                                "medium" -> "注意"
                                else -> "安全"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when ((s as DrugViewModel.RecognitionUiState.Success).drug.riskLevel) {
                                "high" -> Warn
                                "medium" -> Warn
                                else -> Color(0xFF2E7D32)
                            }
                        )
                    }
                }
            }
            s is DrugViewModel.RecognitionUiState.Error -> Text(
                "识别失败，请重试",
                color = Warn,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        if (h.records.isNotEmpty()) {
            Text("历史记录", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = T2)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(h.records.takeLast(10)) { r ->
                    Card(
                        Modifier.fillMaxWidth(),
                        RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            (r.drugName ?: "未识别") + " - " + r.status,
                            Modifier.padding(14.dp),
                            fontSize = 14.sp,
                            color = T1
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Text(
            "对准药品包装盒拍照即可识别",
            fontSize = 12.sp,
            color = T2,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}
