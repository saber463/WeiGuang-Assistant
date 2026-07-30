/**
 * 文件名：ApiClient.kt
 * 作者：微光同行前端团队
 * 功能描述：Retrofit HTTP客户端单例配置（OkHttpClient + GsonConverterFactory）
 * 创建日期：2026-05-29
 * 所属模块：network（网络层）
 *
 * 核心职责：
 * 1. 提供全局唯一的Retrofit实例（单例模式）
 * 2. 配置OkHttp客户端参数（超时时间、拦截器、日志等）
 * 3. 配置JSON序列化/反序列化器（Gson）
 * 4. 统一管理BaseUrl和公共请求头
 *
 * 技术架构：
 * - 使用Hilt @Singleton注解保证全局唯一实例
 * - 通过依赖注入提供Context和其他依赖
 * - 支持Debug/Release环境切换日志级别
 * - 完整的拦截器链：Log → Auth → TokenRefresh → Network
 *
 * 性能优化：
 * - 连接池复用（Connection Pool）
 * - HTTP/2协议支持
 * - GZIP压缩自动处理
 * - 响应缓存策略（可选）
 */

package com.weiguangplus.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API客户端提供者类
 *
 * 负责创建和配置Retrofit及OkHttp实例，
 * 作为网络层的入口点和统一配置中心。
 *
 * 设计为Hilt单例（@Singleton），
 * 确保整个应用生命周期内只存在一个Retrofit实例，
 * 避免资源浪费和连接池重复创建。
 *
 * 依赖注入：
 * - Context：Android上下文（用于DataStore访问）
 * - AuthInterceptor：认证拦截器（注入Token到请求头）
 * - TokenRefreshInterceptor：Token刷新拦截器（401自动刷新）
 *
 * 使用方式：
 * ```
 * // 在Repository中通过@Inject注入ApiService接口
 * @Inject lateinit var apiService: WeiguangApiService
 *
 * // Hilt会自动通过此Provider类创建所需的实例
 * ```
 */
@Singleton
class ApiClient @Inject constructor(
    private val context: Context,
    private val authInterceptor: AuthInterceptor,
    private val tokenRefreshInterceptor: TokenRefreshInterceptor
) {
    /**
     * 后端API基础URL常量
     *
     * 开发环境使用10.0.2.2（Android模拟器访问本机的特殊IP）。
     *
     * 在生产环境中应替换为实际的域名：
     * - 测试环境：https://test-api.weiguangplus.com
     * - 生产环境：https://api.weiguangplus.com
     *
     * 注意事项：
     * - 必须以斜杠(/)结尾（Retrofit规范要求）
     * - 支持HTTPS（生产环境强制使用TLS 1.2+）
     * - 建议使用BuildConfig字段动态配置不同环境
     */
    companion object {
        /** 开发环境BaseUrl（Android模拟器访问本机localhost） */
        const val BASE_URL = "https://api.weiguangplus.com/"

        /**
         * 网络超时时间常量（单位：秒）
         *
         * 根据业务场景和网络环境合理设置：
         * - CONNECT_TIMEOUT：建立TCP连接的超时时间（30秒足够应对慢网络）
         * - READ_TIMEOUT：等待服务器响应数据的超时时间（考虑大数据传输）
         * - WRITE_TIMEOUT：向服务器发送请求数据的超时时间（图片上传等场景）
         */
        const val CONNECT_TIMEOUT = 30L
        const val READ_TIMEOUT = 30L
        const val WRITE_TIMEOUT = 30L
    }

    /**
     * 创建并返回配置好的WeiguangApiService接口实现
     *
     * 这是外部获取API服务实例的唯一入口方法，
     * 内部完成所有OkHttp和Retrofit的初始化工作。
     *
     * 构建流程：
     * 1. 创建OkHttpClient.Builder
     * 2. 添加日志拦截器（Debug模式开启详细日志）
     * 3. 添加认证拦截器（自动注入Token）
     * 4. 添加Token刷新拦截器（401自动刷新）
     * 5. 设置超时时间和连接池参数
     * 6. 构建OkHttpClient实例
     * 7. 创建Retrofit.Builder并设置BaseUrl、Client、Converter
     * 8. 构建Retrofit实例并通过create()生成API接口代理对象
     *
     * @return 可直接调用的WeiguangApiService接口实现对象
     */
    fun createApiService(): WeiguangApiService {
        // 步骤1：构建OkHttp客户端（HTTP通信的底层引擎）
        val okHttpClient = buildOkHttpClient()

        // 步骤2：构建Retrofit实例（类型安全的HTTP客户端封装）
        val retrofit = buildRetrofit(okHttpClient)

        // 步骤3：使用动态代理创建API接口的实现类
        return retrofit.create(WeiguangApiService::class.java)
    }

    /**
     * 构建OkHttpClient实例
     *
     * 配置HTTP客户端的所有参数和拦截器链。
     *
     * 拦截器执行顺序（从外到内）：
     * 1. HttpLoggingInterceptor（日志记录，最外层）
     * 2. AuthInterceptor（添加Authorization头）
     * 3. TokenRefreshInterceptor（检测401并刷新Token，最内层）
     *
     * 这种顺序确保：
     * - 所有请求都会被记录日志
     * - 认证信息在请求发送前被正确添加
     * - 401响应能在最内层被捕获和处理
     *
     * @return 配置完成的OkHttpClient对象
     */
    private fun buildOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .apply {
                // ==================== 拦截器链配置 ====================

                // 1. 日志拦截器（开发调试用）
                // 在Debug模式下输出完整的请求/响应信息（Header、Body等）
                // Release模式下仅记录基本请求信息（保护用户隐私）
                addInterceptor(
                    createLoggingInterceptor()
                )

                // 2. 认证拦截器（自动添加Token）
                // 从本地DataStore读取AccessToken并注入到请求头
                addInterceptor(authInterceptor)

                // 3. Token刷新拦截器（401自动刷新）
                // 当收到401/403响应时，自动调用刷新接口获取新Token
                addInterceptor(tokenRefreshInterceptor)

                // ==================== 超时时间配置 ====================

                // 连接超时：与服务器建立TCP连接的最大等待时间
                // 设置为30秒以适应移动网络的不稳定性
                connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)

                // 读取超时：等待服务器返回数据的最大时间
                // 需要考虑大文件下载或复杂查询的场景
                readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)

                // 写入超时：向服务器发送请求数据的最大时间
                // 图片上传等POST请求需要较长的写入时间
                writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)

                // ==================== 连接池配置 ====================

                // 连接池复用：减少TCP握手开销，提升性能
                // 最大空闲连接数：5个
                // 空闲连接保活时间：5分钟
                connectionPool(
                    okhttp3.ConnectionPool(
                        maxIdleConnections = 5,
                        keepAliveDuration = 5,
                        timeUnit = TimeUnit.MINUTES
                    )
                )

                // ==================== 协议与压缩配置 ====================

                // 启用HTTP/2协议支持（多路复用、头部压缩等性能优势）
                // 如果服务器不支持，会自动降级到HTTP/1.1
                protocols(listOf(okhttp3.Protocol.HTTP_2, okhttp3.Protocol.HTTP_1_1))

                // 自动处理GZIP压缩（减少流量消耗）
                // OkHttp默认已启用，此处显式声明以确保一致性
                // addNetworkInterceptor(GzipRequestInterceptor()) // 如需自定义可取消注释
            }
            .build() // 构建不可变的OkHttpClient实例
    }

    /**
     * 构建Retrofit实例
     *
     * 将OkHttp客户端包装成类型安全的Retrofit接口调用框架。
     *
     * 核心组件：
     * - baseUrl：API的基础地址（所有相对路径的前缀）
     * - client：底层HTTP通信引擎（OkHttpClient）
     * - addConverterFactory：JSON序列化/反序列化转换器（Gson）
     *
     * @param okHttpClient 已配置好的OkHttp客户端实例
     * @return 配置完成的Retrofit对象
     */
    private fun buildRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)  // 设置API基础路径
            .client(okHttpClient)  // 注入自定义的OkHttp客户端
            .addConverterFactory(
                // 使用Gson作为JSON转换器
                // 自动将Kotlin数据类转换为JSON字符串（请求体）
                // 自动将JSON响应转换为Kotlin数据类实例（响应体）
                GsonConverterFactory.create(
                    createGsonInstance()
                )
            )
            .build()  // 构建不可变的Retrofit实例
    }

    /**
     * 创建HTTP日志拦截器
     *
     * 根据构建类型（Debug/Release）设置不同的日志级别：
     * - Debug模式：BODY级别（记录完整的请求头、请求体、响应头、响应体）
     * - Release模式：NONE级别（不输出任何日志，保护隐私和性能）
     *
     * 日志输出目标：Android Logcat（tag="OkHttp"）
     *
     * 注意事项：
     * - BODY级别的日志可能包含敏感信息（Token、密码等），仅在开发环境使用
     * - 生产环境建议使用BASIC级别或完全禁用
     * - 日志量较大时可能影响性能（I/O操作）
     *
     * @return 配置好日志级别的HttpLoggingInterceptor实例
     */
    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            // 判断当前是否为Debug构建版本
            // 可以通过BuildConfig.DEBUG自动判断（需要配置buildConfigField）
            level = if (isDebugMode()) {
                // Debug模式：完整日志（包含请求/响应Body）
                HttpLoggingInterceptor.Level.BODY
            } else {
                // Release模式：禁用日志（保护用户隐私）
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    /**
     * 创建自定义Gson实例
     *
     * 配置Gson的序列化/反序列化行为，
     * 处理特殊的数据格式需求。
     *
     * 当前配置：
     * - setLenient(): 宽松解析模式（容错性更强）
     *   允许JSON中存在多余的逗号、注释等非标准语法
     * - setDateFormat(): 统一日期格式
     *   将所有日期字段按照"yyyy-MM-dd HH:mm:ss"格式解析
     *
     * 扩展建议：
     * - 可注册自定义TypeAdapter处理特殊数据类型
     * - 可配置字段命名策略（如snake_case转camelCase）
     * - 可设置空值处理策略（是否序列化null字段）
     *
     * @return 配置好的Gson实例
     */
    private fun createGsonInstance(): com.google.gson.Gson {
        return com.google.gson.GsonBuilder()
            .setLenient()  // 宽松模式，提高兼容性
            .setDateFormat("yyyy-MM-dd HH:mm:ss")  // 统一日期格式
            .create()  // 构建Gson实例
    }

    /**
     * 判断当前是否为Debug构建模式
     *
     * 用于决定是否启用详细日志等功能。
     *
     * 实现方式：
     * 方式1：使用BuildConfig.DEBUG（推荐，需在build.gradle中配置buildConfig true）
     * 方式2：检查ApplicationInfo.flags中的DEBUGGABLE标志
     * 方式3：读取自定义的配置文件或环境变量
     *
     * 当前采用方式2（不依赖BuildConfig，更通用）
     *
     * @return true表示当前是Debug版本，false表示Release版本
     */
    private fun isDebugMode(): Boolean {
        return context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
    }
}
