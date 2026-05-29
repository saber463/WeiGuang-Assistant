/**
 * 文件名：LoginScreen.kt
 * 作者：微光同行前端团队
 * 功能描述：用户登录界面（手机号+密码登录），包含完整的表单校验和状态管理
 * 创建日期：2026-05-29
 * 所属模块：ui/screen/auth（认证界面层）
 *
 * 界面组成：
 * 1. 顶部Logo和应用名称区域
 * 2. 手机号输入框（OutlinedTextField + 11位数字校验）
 * 3. 密码输入框（密码可见性切换图标）
 * 4. 登录按钮（Loading状态 + 启用/禁用逻辑）
 * 5. 错误提示Snackbar（网络错误、账号密码错误等）
 * 6. 跳转注册链接（TextButton）
 *
 * 设计规范：
 * - 遵循Material3设计系统（颜色、字体、间距、圆角）
 * - WCAG AA级无障碍对比度（橙色主题#FF6B35）
 * - TalkBack屏幕阅读器兼容（contentDescription语义化标注）
 * - 大触控目标尺寸（最小48x48dp，符合无障碍标准）
 *
 * 交互流程：
 * 1. 用户输入手机号 → 实时格式校验 → 显示错误提示或清除错误
 * 2. 用户输入密码 → 可切换显示/隐藏密码文本
 * 3. 点击登录按钮 → 前端校验 → 显示加载状态 → 发起API请求
 * 4a. 成功 → 导航到主页/MainScreen
 * 4b. 失败 → 显示错误提示（Snackbar）→ 恢复按钮可点击状态
 * 5. 点击"注册"链接 → 导航到RegisterScreen
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weiguangplus.ui.viewmodel.AuthViewModel

/**
 * 登录屏幕Composable函数
 *
 * 这是整个界面的入口函数，
 * 负责协调子组件的布局和ViewModel的状态订阅。
 *
 * @param modifier 容器修饰符（由父组件传入）
 * @param viewModel 认证ViewModel实例（通过Hilt自动注入）
 * @param onLoginSuccess 登录成功后的回调（导航到主页）
 * @param onNavigateToRegister 跳转到注册页面的回调
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    // ==================== 状态收集 ====================

    /**
     * 从ViewModel收集登录UI状态
     *
     * 使用collectAsStateWithLifecycle（推荐方式）：
     * - 自动在Lifecycle处于STARTED状态时收集
     * - Lifecycle进入STOPPED时自动取消收集（节省资源）
     * - 配置变更时自动重建并恢复最新状态
     */
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()

    /** Snackbar宿主状态（用于显示错误提示） */
    val snackbarHostState = remember { SnackbarHostState() }

    // ==================== 事件处理 ====================

    /**
     * 监听一次性事件流（SharedFlow）
     *
     * 使用LaunchedEffect在组合首次执行时启动事件监听协程。
     * 当事件发出时执行对应的回调操作。
     */
    LaunchedEffect(Unit) {
        viewModel.loginEvent.collect { event ->
            when (event) {
                is AuthViewModel.LoginEvent.LoginSuccess -> {
                    // 登录成功：导航到主页
                    onLoginSuccess()
                }
                is AuthViewModel.LoginEvent.ShowError -> {
                    // 显示错误提示Snackbar（自动3秒后消失）
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = androidx.compose.material3.SnackbarDuration.Short
                    )
                }
                is AuthViewModel.LoginEvent.NavigateToRegister -> {
                    // 跳转到注册页面
                    onNavigateToRegister()
                }
            }
        }
    }

    /**
     * 监听错误状态变化并自动显示Snackbar
     *
     * 当loginState.error从null变为非null值时，
     * 自动弹出错误提示。
     */
    LaunchedEffect(loginState.error) {
        if (!loginState.error.isNullOrBlank()) {
            snackbarHostState.showSnackbar(
                message = loginState.error!!,
                duration = androidx.compose.material3.SnackbarDuration.Long
            )
            // 显示后立即清除错误状态（避免重复弹出）
            viewModel.clearLoginError()
        }
    }

    // ==================== UI布局 ====================

    /**
     * 主容器（Scaffold模式简化版）
     *
     * 使用Box作为根容器：
     * - 支持多层叠加布局（背景 + 内容 + Snackbar）
     * - 自动填充父容器的所有可用空间
     */
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),  // 左右内边距24dp（符合Material规范）
        contentAlignment = Alignment.Center  // 内容垂直水平居中
    ) {
        // 垂直排列的Column（主要内容区）
        Column(
            verticalArrangement = Arrangement.spacedBy(  // 子元素间距16dp
                space = 16.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "登录界面" }  // 无障碍描述
        ) {
            // ---------- 1. Logo和应用名称 ----------
            LoginHeader()

            Spacer(modifier = Modifier.height(32.dp))

            // ---------- 2. 手机号输入框 ----------
            PhoneInputField(
                phone = loginState.phone,
                error = loginState.phoneError,
                onPhoneChanged = viewModel::onPhoneChanged
            )

            // ---------- 3. 密码输入框 ----------
            PasswordInputField(
                password = loginState.password,
                error = loginState.passwordError,
                isVisible = loginState.isPasswordVisible,
                onPasswordChanged = viewModel::onPasswordChanged,
                onToggleVisibility = viewModel::togglePasswordVisibility,
                onImeAction = { viewModel.login() }  // 键盘回车键触发登录
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ---------- 4. 登录按钮 ----------
            LoginButton(
                isLoading = loginState.isLoading,
                isEnabled = !loginState.isLoading,  // 加载中禁用按钮
                onClick = viewModel::login
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---------- 5. 注册链接 ----------
            RegisterLink(onClick = onNavigateToRegister)
        }

        // ---------- Snackbar宿主（浮在最上层） ----------
        SnackbarHost(hostState = snackbarHostState)
    }
}

/**
 * 登录页头部（Logo + 标题 + 副标题）
 *
 * 展示应用品牌标识和欢迎语。
 */
@Composable
private fun LoginHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 应用Logo（可替换为实际的图片资源）
        Icon(
            imageVector = Icons.Default.Lock,  // 临时使用锁图标，后续替换为实际Logo
            contentDescription = "微光同行应用图标",
            modifier = Modifier
                .size(80.dp)
                .semantics { contentDescription = "微光同行Logo" },
            tint = MaterialTheme.colorScheme.primary  // 使用主题色着色
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 应用名称
        Text(
            text = "微光同行",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 副标题/欢迎语
        Text(
            text = "无障碍出行助手",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 手机号输入框组件
 *
 * 特性：
 * - OutlinedTextField样式（Material3轮廓风格）
 * - 手机号图标前缀
 * - 11位数字键盘限制
 * - 实时校验错误提示
 * - 无障碍标签和描述
 *
 * @param phone 当前输入的手机号内容
 * @param error 校验错误信息（null表示无错误）
 * @param onPhoneChanged 输入内容变化的回调
 */
@Composable
private fun PhoneInputField(
    phone: String,
    error: String?,
    onPhoneChanged: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = phone,
        onValueChange = onPhoneChanged,
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "手机号输入框"
            },
        label = { Text("手机号") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "手机号图标"
            )
        },
        placeholder = { Text("请输入11位手机号") },
        isError = error != null,  // 有错误时边框变红
        supportingText = {
            // 条件渲染：仅在有错误时显示错误提示文字
            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        singleLine = true,  // 单行输入（禁止换行）
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,  // 数字键盘
            imeAction = ImeAction.Next          // 回车键跳到下一个输入框
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }  // 聚焦移动到密码框
        )
    )
}

/**
 * 密码输入框组件
 *
 * 特性：
 * - 密码遮罩（默认隐藏字符显示为圆点）
 * - 可见性切换按钮（眼睛图标）
 * - 错误状态高亮显示
 * - 支持键盘回车键直接触发登录
 *
 * @param password 当前密码内容
 * @param error 校验错误信息
 * @param isVisible 是否以明文显示密码
 * @param onPasswordChanged 密码变化回调
 * @param onToggleVisibility 切换可见性回调
 * @param onImeAction 键盘动作回调（通常用于触发登录）
 */
@Composable
private fun PasswordInputField(
    password: String,
    error: String?,
    isVisible: Boolean,
    onPasswordChanged: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    onImeAction: () -> Unit
) {
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChanged,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "密码输入框" },
        label = { Text("密码") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "密码图标"
            )
        },
        trailingIcon = {
            // 密码可见性切换按钮
            IconButton(
                onClick = onToggleVisibility,
                modifier = Modifier.semantics {
                    contentDescription = if (isVisible) "隐藏密码" else "显示密码"
                }
            ) {
                Icon(
                    imageVector = if (isVisible) {
                        Icons.Default.Visibility      // 睁眼图标（明文）
                    } else {
                        Icons.Default.VisibilityOff   // 闭眼图标（密文）
                    },
                    contentDescription = null  // 已在IconButton上设置description
                )
            }
        },
        placeholder = { Text("请输入密码") },
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        singleLine = true,
        visualTransformation = if (isVisible) {
            VisualTransformation.None  // 明文显示（不做任何变换）
        } else {
            PasswordVisualTransformation()  // 密码遮罩（显示为●●●●）
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,  // 密码键盘（不显示建议词）
            imeAction = ImeAction.Done             // 回车键表示完成
        ),
        keyboardActions = KeyboardActions(
            onDone = { onImeAction() }  // 触发登录操作
        )
    )
}

/**
 * 登录按钮组件
 *
 * 特性：
 * - Loading状态显示圆形进度指示器（替代按钮文字）
 * - Loading时自动禁用点击（防止重复提交）
 * - 橙色主题色（#FF6B35，WCAG AA级对比度）
 * - 圆角矩形形状（Material3标准）
 * - 最小触控目标高度56dp（无障碍标准）
 *
 * @param isLoading 是否正在加载（显示进度条）
 * @param isEnabled 按钮是否可用
 * @param onClick 点击回调
 */
@Composable
private fun LoginButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,  // 控制启用/禁用状态
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)  // 固定高度56dp（大触控目标）
            .semantics {
                contentDescription = if (isLoading) {
                    "正在登录，请稍候"
                } else {
                    "登录按钮"
                }
            },
        shape = MaterialTheme.shapes.large,  // 大圆角（16dp）
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,  // 主题色（橙色）
            contentColor = MaterialTheme.colorScheme.onPrimary,   // 白色文字
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,  // 禁用时灰色
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        if (isLoading) {
            // Loading状态：显示圆形进度指示器
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp  // 进度条粗细
            )
        } else {
            // 正常状态：显示"登录"文字
            Text(
                text = "登 录",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * 注册链接组件
 *
 * 文本样式的按钮，引导未注册用户前往注册页面。
 * 使用TextButton而非Button（视觉权重更低，不抢焦点）。
 *
 * @param onClick 点击回调
 */
@Composable
private fun RegisterLink(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.semantics {
            contentDescription = "跳转到注册页面"
        }
    ) {
        Text(
            text = "还没有账号？立即注册",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Compose预览注解（开发调试用）
 *
 * 在Android Studio的预览面板中实时查看界面效果，
 * 无需运行到模拟器或真机。
 */
@Preview(showBackground = true, name = "登录界面预览")
@Composable
private fun LoginScreenPreview() {
    // 注意：此处无法预览Hilt注入的ViewModel，
    // 实际预览需要提供Mock数据或使用@PreviewParameter
    androidx.compose.material3.MaterialTheme {
        LoginScreen()
    }
}
