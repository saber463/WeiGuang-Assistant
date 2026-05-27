/*
 * CategorySelectionScreen.kt
 *
 * 功能：产品分类选择页面
 *
 * 首次启动时展示，让用户从三种产品分类（视障辅助、听障辅助、综合辅助）中择一进入。
 * 选中后将分类持久化到 SharedPreferences，然后通过回调通知父组件跳转到主界面。
 *
 * 页面布局：
 * - 顶部：应用名称 "微光畅行"（大号、加粗）+ 副标题说明
 * - 中部：三个大卡片按钮，分别对应 ProductCategory 的三个枚举值
 * - 底部："已选分类可随时在设置中更改" 提示文字
 *
 * 交互逻辑：
 * - 点击卡片 → 调用 ProductCategory.saveCategory() 持久化 → 触发 onCategorySelected 回调
 * - 页面不可返回（首次引导流程），必须在三个选项中择一
 */

package com.weiguangchangxing.weiguang_plus.feature.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weiguangchangxing.weiguang_plus.core.ProductCategory

/**
 * 分类选择页面 Composable
 *
 * 全屏页面，背景色使用 MaterialTheme.colorScheme.background。
 * 用户选择产品分类后，通过 [onCategorySelected] 回调通知父组件。
 *
 * @param onCategorySelected 用户选中分类后的回调，参数为所选 ProductCategory
 */
@Composable
fun CategorySelectionScreen(onCategorySelected: (ProductCategory) -> Unit) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // 顶部：应用名称（大号加粗显示）
            Text(
                text = "微光畅行",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 副标题说明
            Text(
                text = "面向视障与听障人群的全面无障碍助手",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 中部：三个分类选择卡片
            ProductCategory.entries.forEach { category ->
                CategoryCard(
                    category = category,
                    onClick = {
                        ProductCategory.saveCategory(context, category)
                        onCategorySelected(category)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 底部：提示文字
            Text(
                text = "已选分类可随时在设置中更改",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 分类卡片组件
 *
 * 一个大圆角、大内边距的卡片按钮，展示分类名称和描述。
 * 点击后触发 [onClick] 回调。
 *
 * @param category 要展示的产品分类
 * @param onClick 点击卡片时的回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryCard(
    category: ProductCategory,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = category.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}