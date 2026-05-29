/**
 * 文件名：AuthRepository.kt
 * 作者：微光同行前端团队
 * 功能描述：用户认证数据仓库，封装登录/注册/Token刷新等认证相关业务逻辑
 * 创建日期：2026-05-29
 * 所属模块：data/repository（仓库层）
 *
 * 架构定位：
 * Repository模式是MVVM架构中的关键组件，
 * 位于ViewModel（UI逻辑层）和DataSource（数据源层）之间。
 *
 * 职责范围：
 * 1. 封装所有与用户认证相关的API调用
 * 2. 管理Token的本地存储和刷新策略
 * 3. 协调多个数据源（远程API + 本地缓存）
 * 4. 提供类型安全的挂起函数接口给ViewModel使用
 *
 * 设计原则：
 * - 单一职责：仅处理认证相关的业务逻辑
 * - 协程友好：所有公开方法都是suspend函数
 * - 错误隔离：内部捕获异常并转换为Result<T>返回
 * - 可测试性：通过接口抽象便于Mock测试
 *
 * 数据流向图：
 * ViewModel → AuthRepository → WeiguangApiService (Retrofit)
 *                    ↓
 *              DataStore (Token本地存储)
 */

package com.weiguangplus.data.repository

import android.content.Context
import com.weiguangplus.data.model.AuthToken
import com.weiguangplus.data.model.User
import com.weiguangplus.network.ApiResponse
import com.weiguangplus.network.AuthInterceptor
import com.weiguangplus.network.WeiguangApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 认证仓库类
 *
 * 使用Hilt @Singleton注解确保全局唯一实例，
 * 通过构造函数注入依赖（WeiguangApiService、Context）。
 *
 * 注入依赖说明：
 * - apiService：Retrofit生成的API接口代理对象，用于发起HTTP请求
 * - context：Android上下文，用于访问DataStore存储Token
 *
 * 线程安全：
 * - 所有方法都在IO调度器上执行（不阻塞主线程）
 * - DataStore操作本身是线程安全的
 * - Token读写通过DataStore的原子事务保证一致性
 */
@Singleton
class AuthRepository @Inject constructor(
    private val apiService: WeiguangApiService,
    private val context: Context
) {
    /**
     * 用户登录
     *
     * 向后端发送手机号和密码进行身份验证。
     * 登录成功后自动保存Token到本地DataStore。
     *
     * 执行流程：
     * 1. 参数校验（手机号格式、密码非空）
     * 2. 调用apiService.login()发起POST请求
     * 3. 检查响应状态码是否为200
     * 4. 解析响应体获取AuthToken对象
     * 5. 将AccessToken保存到DataStore（供后续请求使用）
     * 6. 返回成功结果（包含User信息或AuthToken）
     *
     * 错误处理：
     * - 网络异常 → Result.failure(IOException)
     * - 401错误 → Result.failure(AuthException("账号或密码错误"))
     * - 429限流 → Result.failure(RateLimitException("操作过于频繁"))
     * - 其他错误 → Result.failure(Exception(message))
     *
     * @param phone 手机号（11位数字）
     * @param password 用户密码
     * @return Result<AuthToken> 成功时包含Token，失败时包含异常信息
     */
    suspend fun login(phone: String, password: String): Result<AuthToken> {
        return withContext(Dispatchers.IO) {
            try {
                // 步骤1：调用Retrofit API接口（挂起函数，自动切换线程）
                val response = apiService.login(phone, password)

                // 步骤2：检查HTTP响应是否成功
                if (!response.isSuccessful) {
                    // HTTP层面失败（如404、500等）
                    return@withContext Result.failure(
                        Exception("服务器错误: ${response.code()}")
                    )
                }

                // 步骤3：获取响应体（可能为null）
                val apiResponse = response.body()
                    ?: return@withContext Result.failure(
                        Exception("服务器返回空响应")
                    )

                // 步骤4：检查业务逻辑是否成功
                if (!apiResponse.isSuccess) {
                    // 业务层面的错误（如账号密码错误）
                    return@withContext Result.failure(
                        Exception(apiResponse.message.ifEmpty { "登录失败" })
                    )
                }

                // 步骤5：提取Token数据
                val authToken = apiResponse.getOrThrow()

                // 步骤6：保存AccessToken到本地DataStore
                AuthInterceptor.setAccessToken(context, authToken.accessToken)

                // 步骤7：返回成功的Result
                Result.success(authToken)
            } catch (e: Exception) {
                // 捕获所有异常并包装为失败的Result
                Result.failure(e)
            }
        }
    }

    /**
     * 用户注册
     *
     * 创建新的用户账户。
     * 注册成功后自动登录（服务端直接返回Token）。
     *
     * 参数说明：
     * - phone：手机号（唯一标识符，不可重复注册）
     * - password：密码（需符合复杂度要求）
     * - confirmPassword：确认密码（必须与password一致）
     * - disabilityType：残疾类型（必填，用于个性化功能推荐）
     * - realName：真实姓名（选填，用于实名认证）
     * - idCard：身份证号（选填，用于政府补贴申领）
     *
     * 前置校验（应在ViewModel层完成，此处做兜底校验）：
     * - 手机号格式：11位数字，1开头，第二位3-9
     * - 密码长度：6-20字符
     * - 确认密码匹配
     * - 残疾类型合法枚举值
     *
     * @param phone 手机号
     * @param password 密码
     * @param confirmPassword 确认密码
     * @param disabilityType 残疾类型
     * @param realName 真实姓名（可选）
     * @param idCard 身份证号（可选）
     * @return Result<User> 成功时包含新建的用户信息
     */
    suspend fun register(
        phone: String,
        password: String,
        confirmPassword: String,
        disabilityType: String,
        realName: String? = null,
        idCard: String? = null
    ): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                // 调用注册API接口
                val response = apiService.register(
                    phone = phone,
                    password = password,
                    confirmPassword = confirmPassword,
                    disabilityType = disabilityType,
                    realName = realName,
                    idCard = idCard
                )

                // 处理响应（与login方法相同的模式）
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("注册请求失败: ${response.code()}")
                    )
                }

                val apiResponse = response.body()
                    ?: return@withContext Result.failure(Exception("服务器返回空响应"))

                if (!apiResponse.isSuccess) {
                    return@withContext Result.failure(
                        Exception(apiResponse.message.ifEmpty { "注册失败" })
                    )
                }

                // 注册成功，提取用户信息
                val newUser = apiResponse.getOrThrow()
                Result.success(newUser)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 刷新AccessToken
     *
     * 当AccessToken过期或即将过期时调用此方法，
     * 使用RefreshToken从后端获取新的Token对。
     *
     * 此方法被TokenRefreshInterceptor在检测到401响应时自动调用。
     * 也可以在ViewModel中手动调用（如应用恢复前台时主动检查）。
     *
     * 刷新流程：
     * 1. 从本地DataStore读取RefreshToken（需要额外存储）
     * 2. 调用apiService.refreshToken()发送刷新请求
     * 3. 验证新Token的有效性
     * 4. 更新本地DataStore中的AccessToken
     * 5. （可选）同时更新RefreshToken（如果服务端返回了新的）
     *
     * 安全机制：
     * - RefreshToken只能使用一次（单次使用后失效）
     * - 如果RefreshToken也过期，清除本地所有Token并跳转登录页
     * - 刷新过程中锁定并发请求（避免多次刷新）
     *
     * @param refreshToken 当前的刷新令牌
     * @return Result<AuthToken> 包含新的Token对，失败时需要重新登录
     */
    suspend fun refreshToken(refreshToken: String): Result<AuthToken> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.refreshToken(refreshToken)

                if (!response.isSuccessful) {
                    // 刷新失败（可能是RefreshToken也过期了）
                    clearAllTokens()
                    return@withContext Result.failure(
                        Exception("Token刷新失败，请重新登录")
                    )
                }

                val apiResponse = response.body()
                    ?: run {
                        clearAllTokens()
                        return@withContext Result.failure(Exception("刷新响应为空"))
                    }

                if (!apiResponse.isSuccess) {
                    clearAllTokens()
                    return@withContext Result.failure(
                        Exception(apiResponse.message.ifEmpty { "刷新失败" })
                    )
                }

                // 刷新成功，保存新Token
                val newToken = apiResponse.getOrThrow()
                AuthInterceptor.setAccessToken(context, newToken.accessToken)

                Result.success(newToken)
            } catch (e: Exception) {
                clearAllTokens()
                Result.failure(e)
            }
        }
    }

    /**
     * 用户登出
     *
     * 清除本地认证信息并通知服务端终止会话。
     *
     * 操作步骤：
     * 1. 调用后端logout接口（使当前Token失效）
     * 2. 清除本地DataStore中的AccessToken
     * 3. 清除本地DataStore中的RefreshToken（如果有）
     * 4. 清除内存中的用户缓存数据
     * 5. 导航回到登录页面（由ViewModel处理）
     *
     * 注意事项：
     * - 即使后端登出请求失败，也要清除本地Token（强制登出）
     * - 建议在finally块中执行清理逻辑，确保一定执行
     *
     * @return Result<Unit> 登出操作的结果（通常忽略失败，强制清理本地）
     */
    suspend fun logout(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // 尝试通知服务端登出（即使失败也不影响本地清理）
                apiService.logout()
            } catch (e: Exception) {
                // 忽略网络异常，继续执行本地清理
            } finally {
                // 无论成功与否，都清除本地Token
                clearAllTokens()
            }
            Result.success(Unit)
        }
    }

    /**
     * 获取当前登录用户信息
     *
     * 从后端拉取最新的用户资料。
     * 用于个人中心页面展示或检查Token有效性。
     *
     * @return Result<User> 用户详细信息
     */
    suspend fun getProfile(): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getProfile()

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("获取用户信息失败: ${response.code()}")
                    )
                }

                val apiResponse = response.body()
                    ?: return@withContext Result.failure(Exception("响应为空"))

                if (!apiResponse.isSuccess) {
                    return@withContext Result.failure(
                        Exception(apiResponse.message)
                    )
                }

                Result.success(apiResponse.getOrThrow())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 清除所有本地存储的认证Token
     *
     * 内部辅助方法，用于统一管理Token清理逻辑。
     * 在登出、Token刷新失败、安全检测异常等场景下调用。
     */
    private fun clearAllTokens() {
        AuthInterceptor.clearAccessToken(context)
        // 如果有单独存储RefreshToken，在此处一并清除
        // RefreshTokenManager.clearRefreshToken(context)
    }

    /**
     * 检查当前是否有有效的登录状态
     *
     * 快速检查本地是否存在未过期的AccessToken。
     * 用于启动页判断是否需要显示登录界面。
     *
     * 注意：此方法仅检查本地Token存在性，
     * 不验证Token是否真的有效（需要联网请求才能确认）。
     *
     * @return true表示本地有Token（可能已过期），false表示未登录
     */
    fun isLoggedIn(): Boolean {
        return AuthInterceptor.hasValidToken(context)
    }
}
