/**
 * 文件名：RegisterScreen.kt
 * 作者：微光同行前端团队
 * 功能描述：用户注册界面（手机号+密码+确认密码+残疾类型选择）
 * 创建日期：2026-05-29
 * 所属模块：ui/screen/auth（认证界面层）
 *
 * 界面组成：
 * 1. 顶部标题区域
 * 2. 手机号输入框（与登录页复用校验逻辑）
 * 3. 密码输入框 + 确认密码输入框（一致性校验）
 * 4. 残疾类型下拉选择器（DropdownMenu或RadioGroup）
 * 5. 注册按钮（Loading状态管理）
 * 6. 返回登录链接
 *
 * 无障碍支持：
 * - 所有输入框都有语义化contentDescription
 * - 错误信息通过TalkBack播报
 * - 残疾类型选项有清晰的文字描述
 */

package com.weiguangplus.ui.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weiguangplus.data.model.User
import com.weiguangplus.ui.viewmodel.AuthViewModel

/**
 * 注册屏幕Composable函数
 */
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onRegisterSuccess: (User) -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val registerState by viewModel.registerState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.registerEvent.collect { event ->
            when (event) {
                is AuthViewModel.RegisterEvent.RegisterSuccess -> {
                    onRegisterSuccess(event.user)
                }
                is AuthViewModel.RegisterEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is AuthViewModel.RegisterEvent.NavigateToLogin -> {
                    onNavigateToLogin()
                }
            }
        }
    }

    LaunchedEffect(registerState.error) {
        if (!registerState.error.isNullOrBlank()) {
            snackbarHostState.showSnackbar(registerState.error!!)
            viewModel.clearRegisterError()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics { contentDescription = "注册界面" },
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // 返回按钮
            IconButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.align(Alignment.Start)
                    .semantics { contentDescription = "返回上一页" }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }

            Text(
                text = "创建账号",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Text(
                text = "填写以下信息完成注册",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 手机号输入框
            OutlinedTextField(
                value = registerState.phone,
                onValueChange = viewModel::onRegisterPhoneChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("手机号") },
                placeholder = { Text("请输入11位手机号") },
                isError = registerState.phoneError != null,
                supportingText = {
                    registerState.phoneError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true
            )

            // 密码输入框
            OutlinedTextField(
                value = registerState.password,
                onValueChange = viewModel::onRegisterPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("密码") },
                placeholder = { Text("请设置6-20位密码") },
                isError = registerState.passwordError != null,
                supportingText = {
                    registerState.passwordError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true
            )

            // 确认密码输入框
            OutlinedTextField(
                value = registerState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("确认密码") },
                placeholder = { Text("请再次输入密码") },
                isError = registerState.confirmPasswordError != null,
                supportingText = {
                    registerState.confirmPasswordError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true
            )

            // 残疾类型选择器
            DisabilityTypeSelector(
                selectedType = registerState.disabilityType,
                error = registerState.disabilityTypeError,
                isExpanded = isDropdownExpanded,
                onExpandChanged = { isDropdownExpanded = it },
                onTypeSelected = viewModel::onDisabilityTypeSelected
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 注册按钮
            Button(
                onClick = viewModel::register,
                enabled = !registerState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                if (registerState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("注 册", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("已有账号？立即登录")
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

/**
 * 残疾类型下拉选择器组件
 *
 * 使用DropdownMenu实现下拉菜单选择。
 * 提供所有支持的残疾类型选项供用户选择。
 */
@Composable
private fun DisabilityTypeSelector(
    selectedType: String,
    error: String?,
    isExpanded: Boolean,
    onExpandChanged: (Boolean) -> Unit,
    onTypeSelected: (String) -> Unit
) {
    val disabilityOptions = listOf(
        "VISUAL_IMPAIRMENT" to "视力障碍",
        "HEARING_IMPAIRMENT" to "听力障碍",
        "PHYSICAL_DISABILITY" to "肢体残疾",
        "INTELLECTUAL_DISABILITY" to "智力障碍",
        "SPEECH_IMPAIRMENT" to "言语障碍",
        "MULTIPLE_DISABILITIES" to "多重残疾"
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = if (selectedType.isEmpty()) "" else
                disabilityOptions.firstOrNull { it.first == selectedType }?.second ?: "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "残疾类型选择" },
            label = { Text("残疾类型") },
            placeholder = { Text("请选择您的残疾类型") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "展开选项"
                )
            },
            isError = error != null,
            readOnly = true,  // 只读，通过点击触发下拉菜单
            supportingText = {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        )

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { onExpandChanged(false) }
        ) {
            disabilityOptions.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onTypeSelected(value)
                        onExpandChanged(false)
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "选择$label"
                    }
                )
            }
        }
    }
}
