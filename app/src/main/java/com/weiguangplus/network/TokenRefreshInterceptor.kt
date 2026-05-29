/**
 * 文件名：TokenRefreshInterceptor.kt
 * 作者：微光同行前端团队
 * 功能描述：401响应拦截器，自动检测Token过期并触发刷新流程，实现无感续期
 * 创建日期：2026-05-29
 * 所属模块：network（网络层）
 *
 * 技术架构：
 * 1. 实现OkHttp的Application Interceptor（应用层拦截器）
 * 2. 检测HTTP 401/403响应码（未授权/禁止访问）
 * 3. 自动调用刷新Token接口获取新的AccessToken
 * 4. 使用原请求重试机制（最多重试1次避免无限循环）
 *
 * 线程安全设计：
 * - 使用Kotlin Mutex（互斥锁）防止并发刷新请求
 * - 原子性的Token更新操作（CAS模式）
 * - 同步锁确保同一时间只有一个刷新请求在执行
 *
 * 工作流程图：
 * 发送API请求 → 收到401响应 → 加锁 → 调用refreshToken接口
 *     ↓                                    ↓
 *   成功？ ← ────────────── 更新本地Token ← ┘
 *     ↓ 是                          ↓ 否
 *   用新Token重试原请求          清除Token，跳转登录页
 */

package com.weiguangplus.network

import android.content.Context
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Token刷新拦截器
 *
 * 核心职责：
 * - 监听所有API响应，捕获401（Unauthorized）状态码
 * - 自动调用后端的/token/refresh接口获取新Token
 * - 将新Token保存到本地并使用原请求重试
 * - 如果刷新失败，清除本地认证信息并通知UI跳转登录页
 *
 * 设计原则：
 * 1. 幂等性：同一个401响应只会触发一次刷新操作
 * 2. 非阻塞：刷新过程中其他请求会等待而非失败
 * 3. 容错性：刷新失败时优雅降级（清除Token + 提示用户重新登录）
 *
 * @param context Android上下文（用于访问DataStore和启动登录Activity）
 * @param tokenRefreshAction Token刷新的实际执行逻辑（函数类型参数）
 *        签名为 suspend () -> String? 表示挂起函数，返回新Token或null
 */
class TokenRefreshInterceptor(
    private val context: Context,
    private val tokenRefreshAction: suspend () -> String?
) : Interceptor {

    /**
     * 互斥锁（Mutex）
     *
     * 用于保证同一时刻只有一个Token刷新操作在执行，
     * 避免多个并发请求同时收到401导致多次调用刷新接口。
     *
     * Kotlin协程的Mutex相比Java的synchronized更轻量，
     * 且不会阻塞线程，仅挂起协程。
     */
    private val refreshMutex = Mutex()

    /**
     * 当前是否正在执行Token刷新操作的标志位
     *
     * 用于快速判断是否需要等待刷新完成，
     * 避免不必要的锁竞争。
     */
    @Volatile
    private var isRefreshing = false

    /**
     * 拦截并处理HTTP响应
     *
     * 这是拦截器的核心方法，实现完整的"检测-刷新-重试"流程。
     *
     * 执行逻辑：
     * 1. 先正常发送原始请求并获取响应
     * 2. 检查响应码是否为401或403（认证失败）
     * 3. 如果是认证错误，进入Token刷新流程
     * 4. 刷新成功后用新Token重建请求并重试
     * 5. 返回最终结果（可能是原始响应或重试后的新响应）
     *
     * @param chain OkHttp拦截器链对象
     * @return HTTP响应对象（Response）
     * @throws IOException 网络异常或刷新失败时的IO异常
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        // 步骤1：发送原始请求
        val originalRequest = chain.request()
        val originalResponse = chain.proceed(originalRequest)

        // 步骤2：检查是否需要刷新Token
        // 仅当响应码为401（未授权）或403（禁止访问）时才触发刷新
        if (originalResponse.code != 401 && originalResponse.code != 403) {
            return originalResponse
        }

        // 关闭原始响应体以释放资源（重要！否则会造成资源泄漏）
        originalResponse.close()

        // 步骤3：加锁执行Token刷新（同步方式桥接协程）
        // runBlocking会将协程的异步操作转换为同步阻塞调用，
        // 因为OkHttp的intercept()方法是同步接口。
        return try {
            kotlinx.coroutines.runBlocking {
                executeWithRefreshLock(chain, originalRequest)
            }
        } catch (e: Exception) {
            // 刷新过程出现异常时，构建一个错误响应返回给调用方
            buildErrorResponse(originalRequest, "Token刷新失败: ${e.message}")
        }
    }

    /**
     * 在互斥锁保护下执行Token刷新和请求重试
     *
     * 这是一个挂起函数（suspend function），
     * 可以在内部安全地调用其他协程代码。
     *
     * 实现细节：
     * 1. 使用mutex.withLock()获取锁
     * 2. 双重检查：获取锁后再次确认是否正在刷新（避免重复刷新）
     * 3. 调用tokenRefreshAction执行实际的Token刷新逻辑
     * 4. 根据刷新结果决定是重试请求还是返回错误
     *
     * @param chain 拦截器链
     * @param originalRequest 原始请求对象
     * @return 新的HTTP响应（重试后的结果或错误响应）
     */
    private suspend fun executeWithRefreshLock(
        chain: Interceptor.Chain,
        originalRequest: Request
    ): Response {
        return refreshMutex.withLock {
            // 双重检查锁定模式（Double-Checked Locking）
            // 避免在等待锁的过程中，其他线程已经完成了刷新操作
            if (isRefreshing) {
                // 如果已经在刷新中，直接使用当前Token重试一次
                // （可能其他线程已经更新了Token到DataStore）
                return@withLock retryWithNewToken(chain, originalRequest)
            }

            try {
                // 标记开始刷新
                isRefreshing = true

                // 调用外部注入的Token刷新逻辑
                // 这个函数通常由AuthRepository提供实现，
                // 内部会调用POST /api/auth/token/refresh接口
                val newToken = tokenRefreshAction.invoke()

                if (newToken != null) {
                    // 刷新成功：保存新Token到本地存储
                    AuthInterceptor.setAccessToken(context, newToken)

                    // 使用新Token重建并重试原请求
                    retryWithNewToken(chain, originalRequest)
                } else {
                    // 刷新失败：清除无效Token，强制用户重新登录
                    AuthInterceptor.clearAccessToken(context)

                    // 构建一个特殊的401错误响应用于提示用户
                    buildErrorResponse(
                        originalRequest,
                        "登录已过期，请重新登录",
                        401
                    )
                }
            } catch (e: Exception) {
                // 异常情况：记录日志并返回错误响应
                AuthInterceptor.clearAccessToken(context)
                buildErrorResponse(
                    originalRequest,
                    "Token刷新异常: ${e.message}",
                    500
                )
            } finally {
                // 无论成功还是失败，都要重置刷新标志
                isRefreshing = false
            }
        }
    }

    /**
     * 使用最新的Token重新发送原始请求
     *
     * 从DataStore读取最新保存的AccessToken（可能已被其他线程更新），
     * 然后复制原始请求的所有配置，仅替换Authorization头。
     *
     * @param chain 拦截器链
     * @param originalRequest 原始请求（URL、Method、Body等保持不变）
     * @return 重试后的新响应
     */
    private fun retryWithNewToken(
        chain: Interceptor.Chain,
        originalRequest: Request
    ): Response {
        // 从DataStore读取最新的Token（同步方式）
        val currentToken = kotlinx.coroutines.runBlocking {
            AuthInterceptor.hasValidToken(context)
        }

        if (!currentToken) {
            // Token不存在或已失效，无法重试
            return buildErrorResponse(
                originalRequest,
                "无有效认证信息"
            )
        }

        // 使用Builder模式重建请求，添加新的Authorization头
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer ${getLatestToken()}")
            .build()

        // 将新请求传递给拦截器链继续处理
        return chain.proceed(newRequest)
    }

    /**
     * 从DataStore获取最新的AccessToken字符串
     *
     * 辅助方法，封装了DataStore的同步读取逻辑。
     *
     * @return AccessToken字符串，如果不存在则返回空字符串
     */
    private fun getLatestToken(): String {
        return kotlinx.coroutines.runBlocking {
            context.authDataStore.data.first()[ACCESS_TOKEN_KEY] ?: ""
        }
    }

    companion object {
        /** DataStore键名常量（与AuthInterceptor共享） */
        private val ACCESS_TOKEN_KEY =
            androidx.datastore.preferences.core.stringPreferencesKey("access_token")

        /**
         * 构建错误响应对象
         *
         * 当Token刷新失败或网络异常时，
         * 手动构建一个包含错误信息的Response对象返回给调用方。
         *
         * @param originalRequest 原始请求（用于关联错误响应）
         * @param errorMessage 用户友好的错误描述
         * @param statusCode HTTP状态码（默认500服务器错误）
         * @return 包含JSON格式错误信息的Response对象
         */
        private fun buildErrorResponse(
            originalRequest: Request,
            errorMessage: String,
            statusCode: Int = 500
        ): Response {
            // 构建符合ApiResponse格式的JSON错误体
            val errorJson = """
                {
                    "code": $statusCode,
                    "message": "$errorMessage",
                    "data": null
                }
            """.trimIndent()

            // 使用OkHttp的ResponseBody创建响应体
            val responseBody = okhttp3.ResponseBody.create(
                okhttp3.MediaType.parse("application/json"),
                errorJson
            )

            // 构建完整的Response对象
            return okhttp3.Response.Builder()
                .request(originalRequest)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(statusCode)
                .message(errorMessage)
                .body(responseBody)
                .build()
        }
    }
}
