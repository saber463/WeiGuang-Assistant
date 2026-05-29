/**
 * 文件名：AuthInterceptor.kt
 * 作者：微光同行前端团队
 * 功能描述：HTTP请求认证拦截器，自动为每个API请求添加Authorization头
 * 创建日期：2026-05-29
 * 所属模块：network（网络层）
 *
 * 技术实现：
 * 1. 实现OkHttp的Interceptor接口
 * 2. 从DataStore读取本地存储的AccessToken
 * 3. 自动在请求头中添加"Bearer {token}"格式的Authorization字段
 * 4. 支持协程安全的数据读取（通过runBlocking桥接同步/异步）
 *
 * 安全机制：
 * - 仅当Token存在时才添加请求头（避免未登录状态发送无效token）
 * - Token格式严格遵循OAuth 2.0 Bearer Token规范
 * - 与TokenRefreshInterceptor配合使用，实现自动Token刷新流程
 */

package com.weiguangplus.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * DataStore扩展属性（Context级别单例）
 *
 * 使用私有的DataStore实例存储用户认证信息，
 * 确保数据线程安全且支持异步写入。
 */
private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auth_preferences"
)

/**
 * 认证Token的DataStore键名常量
 */
private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")

/**
 * 请求认证拦截器
 *
 * 职责：在每个HTTP请求发出前，
 * 自动从本地存储读取AccessToken并注入到请求头中。
 *
 * 工作流程：
 * 1. 拦截原始请求（Request）
 * 2. 从DataStore读取本地保存的AccessToken（同步阻塞方式）
 * 3. 如果Token存在，构建新的Request并添加"Authorization: Bearer xxx"头
 * 4. 将修改后的请求传递给下一个拦截器或发送到服务器
 *
 * 使用场景：
 * - 登录后的所有需要认证的API调用
 * - 自动维持用户的登录状态
 * - 配合TokenRefreshInterceptor实现无感Token刷新
 *
 * @param context Android上下文（用于访问DataStore）
 *
 * 构造函数参数说明：
 * @param context Application Context（避免内存泄漏，不要传Activity Context）
 */
class AuthInterceptor(
    private val context: Context
) : Interceptor {

    /**
     * 拦截并处理HTTP请求
     *
     * 这是OkHttp拦截器链的核心方法，
     * 在每个请求发送到服务器之前被调用。
     *
     * 实现逻辑：
     * 1. 获取原始请求对象（chain.request()）
     * 2. 从本地DataStore读取AccessToken
     * 3. 如果Token非空，使用newBuilder()模式复制请求并添加Header
     * 4. 调用chain.proceed()将请求继续传递给后续处理
     *
     * @param chain 拦截器链（包含当前请求和后续处理器）
     * @return 服务器的响应对象（Response）
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        // 获取原始请求（不可变对象，需要通过builder修改）
        val originalRequest = chain.request()

        // 从DataStore同步读取AccessToken
        // 注意：这里使用runBlocking是因为OkHttp拦截器是同步接口，
        // 但DataStore读取是异步操作。在实际生产环境中，
        // 可以考虑在应用启动时预加载Token到内存缓存。
        val accessToken = runBlocking {
            context.authDataStore.data.first()[ACCESS_TOKEN_KEY]
        }

        // 如果Token不存在，直接发送原始请求（未登录状态）
        if (accessToken.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        // 构建带有Authorization头的新的请求对象
        // 使用Builder模式复制原请求的所有配置，仅修改Headers部分
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")  // OAuth 2.0标准格式
            .build()

        // 将携带认证信息的请求传递给下一个拦截器或发送到服务器
        return chain.proceed(authenticatedRequest)
    }

    companion object {
        /**
         * 便捷方法：手动设置AccessToken到本地存储
         *
         * 通常在登录成功后由Repository层调用此方法保存Token。
         *
         * @param context Android上下文
         * @param token 从后端获取的AccessToken字符串
         */
        fun setAccessToken(context: Context, token: String) {
            runBlocking {
                context.authDataStore.edit { preferences ->
                    preferences[ACCESS_TOKEN_KEY] = token
                }
            }
        }

        /**
         * 便捷方法：清除本地存储的AccessToken
         *
         * 通常在登出操作或Token刷新失败时调用。
         *
         * @param context Android上下文
         */
        fun clearAccessToken(context: Context) {
            runBlocking {
                context.authDataStore.edit { preferences ->
                    preferences.remove(ACCESS_TOKEN_KEY)
                }
            }
        }

        /**
         * 便捷方法：检查本地是否存在有效的AccessToken
         *
         * 用于判断用户是否已登录（启动页检查等场景）。
         *
         * @param context Android上下文
         * @return true表示已登录且有有效Token
         */
        fun hasValidToken(context: Context): Boolean {
            return runBlocking {
                val token = context.authDataStore.data.first()[ACCESS_TOKEN_KEY]
                !token.isNullOrBlank()
            }
        }
    }
}
