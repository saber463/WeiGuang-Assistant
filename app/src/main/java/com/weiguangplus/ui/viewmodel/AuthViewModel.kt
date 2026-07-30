/**
 * 文件名：AuthViewModel.kt
 * 作者：微光同行前端团队
 * 功能描述：认证状态管理ViewModel，处理登录/注册的UI状态和业务逻辑
 * 创建日期：2026-05-29
 * 所属模块：ui/viewmodel（视图模型层）
 *
 * 核心职责：
 * 1. 管理登录/注册表单的UI状态（输入内容、校验错误、加载状态）
 * 2. 协调Repository层发起网络请求
 * 3. 将请求结果转换为UI可消费的状态对象（UiState）
 * 4. 暴露给Composable函数的状态流（StateFlow/LiveData）
 *
 * 架构模式：
 * - 遵循Android官方推荐的UI State最佳实践（单一可信源）
 * - 使用data class定义不可变的UI状态快照
 * - 通过StateFlow向UI层推送状态更新
 * - 支持配置变更时自动恢复状态（SavedStateHandle）
 *
 * 状态管理策略：
 * - 登录状态：LoginUiState（phone、password、isLoading、error等）
 * - 注册状态：RegisterUiState（额外包含confirmPassword、disabilityType等）
 * - 统一事件：一次性事件使用SharedFlow（如导航、Toast提示）
 */

package com.weiguangplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weiguangplus.data.model.AuthToken
import com.weiguangplus.data.model.User
import com.weiguangplus.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 认证ViewModel类
 *
 * 使用Hilt @HiltViewModel注解支持依赖注入。
 * 通过构造函数注入AuthRepository实例。
 *
 * 生命周期：
 * - 与Activity/Fragment的生命周期绑定
 * - 配置变更（如屏幕旋转）时自动重建但保持状态
 * - 不持有View层的引用（避免内存泄漏）
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // ==================== 登录状态管理 ====================

    /**
     * 登录UI状态的可变流（私有，仅内部修改）
     *
     * 包含登录页面需要的所有UI状态字段。
     * 使用MutableStateFlow实现响应式状态管理。
     */
    private val _loginState = MutableStateFlow(LoginUiState())

    /**
     * 登录UI状态的公开只读流（供UI层观察）
     *
     * Composable函数通过collectAsState()收集此流，
     * 当状态变化时自动触发重组（Recomposition）。
     */
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    /**
     * 登录相关的一次性事件流（私有）
     *
     * 用于发送导航、Toast、Dialog等一次性UI事件。
     * 使用SharedFlow确保事件不会重复消费。
     * replay=0表示不重放旧事件（新订阅者只能收到新事件）。
     */
    private val _loginEvent = MutableSharedFlow<LoginEvent>(replay = 0)

    /**
     * 登录事件的公开只读流
     */
    val loginEvent: SharedFlow<LoginEvent> = _loginEvent.asSharedFlow()

    // ==================== 注册状态管理 ====================

    /** 注册UI状态的可变流（私有） */
    private val _registerState = MutableStateFlow(RegisterUiState())

    /** 注册UI状态的公开只读流 */
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    /** 注册相关的一次性事件流（私有） */
    private val _registerEvent = MutableSharedFlow<RegisterEvent>(replay = 0)

    /** 注册事件的公开只读流 */
    val registerEvent: SharedFlow<RegisterEvent> = _registerEvent.asSharedFlow()

    // ==================== 登录相关方法 ====================

    /**
     * 更新手机号输入框的内容
     *
     * 每次用户输入一个字符时调用此方法更新状态。
     * 同时实时进行格式校验并更新错误提示。
     *
     * @param phone 用户输入的手机号字符串
     */
    fun onPhoneChanged(phone: String) {
        _loginState.value = _loginState.value.copy(
            phone = phone,
            phoneError = validatePhone(phone)  // 实时校验
        )
    }

    /**
     * 更新密码输入框的内容
     *
     * @param password 用户输入的密码字符串
     */
    fun onPasswordChanged(password: String) {
        _loginState.value = _loginState.value.copy(
            password = password,
            passwordError = validatePassword(password)
        )
    }

    /**
     * 切换密码可见性（显示/隐藏密码文本）
     *
     * 点击密码输入框右侧的眼睛图标时调用。
     * 控制OutlinedTextField的visualTransformation属性。
     */
    fun togglePasswordVisibility() {
        _loginState.value = _loginState.value.copy(
            isPasswordVisible = !_loginState.value.isPasswordVisible
        )
    }

    /**
     * 执行登录操作
     *
     * 在用户点击"登录"按钮时调用。
     *
     * 执行流程：
     * 1. 前置校验（手机号和密码格式是否正确）
     * 2. 设置加载状态为true（显示进度条，禁用按钮）
     * 3. 清除之前的错误信息
     * 4. 调用authRepository.login()发起异步请求
     * 5. 处理结果（成功→导航主页，失败→显示错误提示）
     * 6. 无论成功失败都恢复加载状态为false
     *
     * 线程说明：
     * - viewModelScope.launch在主线程启动协程
     * - Repository内部的withContext(Dispatchers.IO)切换到IO线程执行网络请求
     * - 结果回调回到主线程更新UI状态
     */
    fun login() {
        viewModelScope.launch {
            // 步骤1：前端校验（快速反馈，无需等待网络）
            val currentState = _loginState.value
            val phoneError = validatePhone(currentState.phone)
            val passwordError = validatePassword(currentState.password)

            if (phoneError != null || passwordError != null) {
                // 校验不通过，更新错误状态并返回
                _loginState.value = currentState.copy(
                    phoneError = phoneError,
                    passwordError = passwordError
                )
                return@launch
            }

            // 步骤2：设置加载状态
            _loginState.value = currentState.copy(
                isLoading = true,
                error = null  // 清除旧的错误信息
            )

            // 步骤3：调用Repository执行登录请求
            val result = authRepository.login(
                phone = currentState.phone.trim(),
                password = currentState.password
            )

            // 步骤4：处理请求结果
            result.fold(
                onSuccess = { authToken ->
                    // 登录成功！
                    _loginState.value = LoginUiState()  // 重置表单状态
                    _loginEvent.emit(LoginEvent.LoginSuccess(authToken))
                },
                onFailure = { exception ->
                    // 登录失败，显示错误提示
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "登录失败，请检查网络连接"
                    )
                    _loginEvent.emit(LoginEvent.ShowError(exception.message!!))
                }
            )

            // 步骤5：恢复非加载状态（如果还在加载中）
            if (_loginState.value.isLoading) {
                _loginState.value = _loginState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * 清除登录错误信息
     *
     * 通常在用户开始重新输入时调用，
     * 移除之前显示的错误提示（Snackbar或Toast）。
     */
    fun clearLoginError() {
        _loginState.value = _loginState.value.copy(error = null)
    }

    // ==================== 注册相关方法 ====================

    /**
     * 更新注册表单的手机号
     */
    fun onRegisterPhoneChanged(phone: String) {
        _registerState.value = _registerState.value.copy(
            phone = phone,
            phoneError = validatePhone(phone)
        )
    }

    /**
     * 更新注册表单的密码
     */
    fun onRegisterPasswordChanged(password: String) {
        _registerState.value = _registerState.value.copy(
            password = password,
            passwordError = validatePassword(password),
            confirmPasswordError = validateConfirmPassword(
                password,
                _registerState.value.confirmPassword
            )
        )
    }

    /**
     * 更新确认密码输入框
     *
     * 同时校验两次密码是否一致。
     */
    fun onConfirmPasswordChanged(confirmPassword: String) {
        _registerState.value = _registerState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = validateConfirmPassword(
                _registerState.value.password,
                confirmPassword
            )
        )
    }

    /**
     * 选择残疾类型
     *
     * 从下拉菜单或单选列表中选择后调用。
     *
     * @param type 选中的残疾类型枚举值
     */
    fun onDisabilityTypeSelected(type: String) {
        _registerState.value = _registerState.value.copy(
            disabilityType = type
        )
    }

    /**
     * 切换注册页面的密码可见性
     */
    fun toggleRegisterPasswordVisibility() {
        _registerState.value = _registerState.value.copy(
            isPasswordVisible = !_registerState.value.isPasswordVisible
        )
    }

    /**
     * 执行注册操作
     *
     * 与login()方法类似的流程，
     * 但参数更多且需要额外的校验逻辑。
     */
    fun register() {
        viewModelScope.launch {
            val currentState = _registerState.value

            // 前端校验所有字段
            val phoneError = validatePhone(currentState.phone)
            val passwordError = validatePassword(currentState.password)
            val confirmError = validateConfirmPassword(
                currentState.password,
                currentState.confirmPassword
            )
            val typeError = if (currentState.disabilityType.isEmpty()) {
                "请选择残疾类型"
            } else null

            if (phoneError != null || passwordError != null ||
                confirmError != null || typeError != null) {
                _registerState.value = currentState.copy(
                    phoneError = phoneError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError,
                    disabilityTypeError = typeError
                )
                return@launch
            }

            // 设置加载状态
            _registerState.value = currentState.copy(
                isLoading = true,
                error = null
            )

            // 调用注册API
            val result = authRepository.register(
                phone = currentState.phone.trim(),
                password = currentState.password,
                confirmPassword = currentState.confirmPassword,
                disabilityType = currentState.disabilityType
            )

            // 处理结果
            result.fold(
                onSuccess = { user ->
                    _registerState.value = RegisterUiState()
                    _registerEvent.emit(RegisterEvent.RegisterSuccess(user))
                },
                onFailure = { exception ->
                    _registerState.value = _registerState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "注册失败"
                    )
                    _registerEvent.emit(RegisterEvent.ShowError(exception.message!!))
                }
            )

            // 恢复加载状态
            if (_registerState.value.isLoading) {
                _registerState.value = _registerState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * 清除注册错误信息
     */
    fun clearRegisterError() {
        _registerState.value = _registerState.value.copy(error = null)
    }

    // ==================== 辅助校验方法 ====================

    /**
     * 校验手机号格式
     *
     * 规则：
     * - 不能为空
     * - 必须为11位数字
     * - 必须以1开头
     * - 第二位必须是3-9的数字
     *
     * @param phone 待校验的手机号字符串
     * @return 校验通过的错误消息（null表示无错误）
     */
    private fun validatePhone(phone: String): String? {
        return when {
            phone.isBlank() -> "手机号不能为空"
            phone.length != 11 -> "请输入11位手机号"
            !phone.startsWith("1") -> "手机号格式不正确"
            phone[1].digitToIntOrNull()?.let { it in 3..9 } != true -> "手机号格式不正确"
            else -> null  // 校验通过
        }
    }

    /**
     * 校验密码格式
     *
     * 规则：
     * - 不能为空
     * - 长度6-20个字符
     * - 建议包含字母和数字（增强安全性）
     *
     * @param password 待校验的密码字符串
     * @return 错误消息或null
     */
    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "密码不能为空"
            password.length < 6 -> "密码至少6个字符"
            password.length > 20 -> "密码最多20个字符"
            else -> null
        }
    }

    /**
     * 校验确认密码是否与原密码一致
     *
     * @param password 原始密码
     * @param confirmPassword 确认密码
     * @return 错误消息或null
     */
    private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "请再次输入密码"
            confirmPassword != password -> "两次输入的密码不一致"
            else -> null
        }
    }

    // ==================== 数据类定义 ====================

    /**
     * 登录界面UI状态数据类
     *
     * 定义登录页面所需的所有可变状态字段。
     * 使用data class确保不可变性和copy()方法支持。
     *
     * 设计原则：
     * - 所有字段都有默认值（初始状态）
     * - 字段命名清晰表达用途
     * - 区分用户输入值和校验错误值
     */
    data class LoginUiState(
        /** 手机号输入框的当前内容 */
        val phone: String = "",
        /** 密码输入框的当前内容 */
        val password: String = "",
        /** 手机号校验错误信息（null表示无错误） */
        val phoneError: String? = null,
        /** 密码校验错误信息 */
        val passwordError: String? = null,
        /** 是否正在加载（显示进度条） */
        val isLoading: Boolean = false,
        /** 通用错误信息（用于Snackbar展示） */
        val error: String? = null,
        /** 密码是否以明文显示（眼睛图标切换） */
        val isPasswordVisible: Boolean = false
    )

    /**
     * 注册界面UI状态数据类
     *
     * 继承LoginUiState的所有字段，
     * 并增加注册特有的字段。
     */
    data class RegisterUiState(
        /** 手机号 */
        val phone: String = "",
        /** 密码 */
        val password: String = "",
        /** 确认密码 */
        val confirmPassword: String = "",
        /** 选择的残疾类型 */
        val disabilityType: String = "",
        /** 各字段的校验错误信息 */
        val phoneError: String? = null,
        val passwordError: String? = null,
        val confirmPasswordError: String? = null,
        val disabilityTypeError: String? = null,
        /** 加载状态 */
        val isLoading: Boolean = false,
        /** 通用错误信息 */
        val error: String? = null,
        /** 密码可见性 */
        val isPasswordVisible: Boolean = false
    )

    /**
     * 登录相关的一次性事件密封类
     *
     * 用于向UI层发送导航指令、Toast提示等一次性事件。
     * 使用sealed class确保编译时类型安全。
     */
    sealed class LoginEvent {
        /** 登录成功事件（携带Token信息） */
        data class LoginSuccess(val token: AuthToken) : LoginEvent()
        /** 显示错误提示事件（携带错误消息） */
        data class ShowError(val message: String) : LoginEvent()
        /** 导航到注册页面事件 */
        object NavigateToRegister : LoginEvent()
    }

    /**
     * 注册相关的一次性事件密封类
     */
    sealed class RegisterEvent {
        /** 注册成功事件（携带新建的用户信息） */
        data class RegisterSuccess(val user: User) : RegisterEvent()
        /** 显示错误提示事件 */
        data class ShowError(val message: String) : RegisterEvent()
        /** 导航到登录页面事件 */
        object NavigateToLogin : RegisterEvent()
    }
}
