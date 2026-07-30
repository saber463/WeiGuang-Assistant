/**
 * 文件名：NetworkModule.kt
 * 作者：微光同行前端团队
 * 功能描述：Hilt网络模块，提供OkHttp、Retrofit、API Service等网络相关依赖
 * 创建日期：2026-05-29
 * 所属模块：di（依赖注入层）
 *
 * 核心职责：
 * 1. 配置OkHttp客户端（超时时间、拦截器链、连接池）
 * 2. 配置Retrofit实例（BaseUrl、ConverterFactory）
 * 3. 提供WeiguangApiService接口代理对象
 * 4. 提供认证拦截器和Token刷新拦截器
 *
 * 拦截器链顺序（从外到内）：
 * 1. HttpLoggingInterceptor（日志记录，Debug模式启用详细日志）
 * 2. AuthInterceptor（自动注入Authorization头）
 * 3. TokenRefreshInterceptor（401自动刷新Token并重试）
 *
 * 性能优化配置：
 * - 连接池复用（5个空闲连接，5分钟保活）
 * - HTTP/2协议支持
 * - GZIP压缩自动处理
 */

package com.weiguangplus.di

import android.content.Context
import com.weiguangplus.network.ApiClient
import com.weiguangplus.network.AuthInterceptor
import com.weiguangplus.network.TokenRefreshInterceptor
import com.weiguangplus.network.WeiguangApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 网络层Hilt模块
 *
 * @InstallIn(SingletonComponent::class) 表示所有提供的依赖都是全局单例。
 * 这确保整个应用共享同一个OkHttp连接池和Retrofit实例。
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** 常量定义 */
    /** API基础URL（Android模拟器访问本机使用10.0.2.2） */
    const val BASE_URL = "http://10.0.2.2:8000/"

        /** 连接超时时间（秒） */
    const val CONNECT_TIMEOUT = 30L

        /** 读取超时时间（秒） */
    const val READ_TIMEOUT = 30L

        /** 写入超时时间（秒） */
    const val WRITE_TIMEOUT = 30L

    /**
     * 提供HttpLoggingInterceptor实例
     *
     * 用于在开发阶段记录完整的HTTP请求和响应信息，
     * 方便调试网络问题。
     *
     * 日志级别说明：
     * - NONE：不输出任何日志（Release模式）
     * - BASIC：仅记录请求行和响应码
     * - HEADERS：记录请求/响应的头信息
     * - BODY：记录完整的请求体和响应体（包含Token等敏感信息！仅Debug用）
     *
     * @return 配置好日志级别的HttpLoggingInterceptor
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (isDebugMode()) {
                HttpLoggingInterceptor.Level.BODY  // Debug模式：完整日志
            } else {
                HttpLoggingInterceptor.Level.NONE  // Release模式：禁用日志
            }
        }
    }

    /**
     * 提供AuthInterceptor认证拦截器实例
     *
     * 负责在每个HTTP请求中自动添加Authorization头。
     * 从DataStore读取本地存储的AccessToken。
     *
     * @param context Application Context（用于访问DataStore）
     * @return 配置好的AuthInterceptor实例
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(context: Context): AuthInterceptor {
        return AuthInterceptor(context)
    }

    /**
     * 提供TokenRefreshInterceptor Token刷新拦截器实例
     *
     * 监听HTTP 401/403响应，自动调用刷新接口获取新Token，
     * 并使用原请求重试，实现无感续期体验。
     *
     * @param context Application Context
     * @return 配置好的TokenRefreshInterceptor实例
     */
    @Provides
    @Singleton
    fun provideTokenRefreshInterceptor(context: Context): TokenRefreshInterceptor {
        // 注入Token刷新的实际执行逻辑（通常由AuthRepository提供）
        val tokenRefreshAction: suspend () -> String? = {
            // TODO: 这里应该注入AuthRepository并调用refreshToken方法
            // 由于循环依赖问题，此处简化处理
            null
        }
        return TokenRefreshInterceptor(context, tokenRefreshAction)
    }

    /**
     * 提供OkHttpClient实例
     *
     * 配置HTTP通信的所有底层参数和拦截器链。
     *
     * 关键配置项：
     * - 拦截器链：Log → Auth → TokenRefresh
     * - 超时时间：30s connect/read/write
     * - 连接池：5个空闲连接，5分钟保活
     * - 协议支持：HTTP/2 + HTTP/1.1降级
     *
     * @param loggingInterceptor 日志拦截器
     * @param authInterceptor 认证拦截器
     * @param tokenRefreshInterceptor Token刷新拦截器
     * @return 完全配置好的OkHttpClient单例
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenRefreshInterceptor: TokenRefreshInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .apply {
                // 添加拦截器链（注意顺序很重要！）
                addInterceptor(loggingInterceptor)       // 最外层：记录日志
                addInterceptor(authInterceptor)           // 中间层：添加Token
                addInterceptor(tokenRefreshInterceptor)   // 最内层：处理401刷新

                // 超时时间配置
                connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)

                // 连接池配置（复用TCP连接，提升性能）
                connectionPool(
                    okhttp3.ConnectionPool(
                        maxIdleConnections = 5,
                        keepAliveDuration = 5,
                        timeUnit = TimeUnit.MINUTES
                    )
                )

                // 协议支持（优先HTTP/2，不支持则降级到HTTP/1.1）
                protocols(listOf(okhttp3.Protocol.HTTP_2, okhttp3.Protocol.HTTP_1_1))
            }
            .build()
    }

    /**
     * 提供Retrofit实例
     *
     * 将OkHttp包装成类型安全的声明式HTTP客户端。
     *
     * 核心组件：
     * - baseUrl：API的基础路径前缀
     * - client：底层HTTP引擎（OkHttpClient）
     * - converterFactory：JSON序列化转换器（Gson）
     *
     * @param okHttpClient 已配置的OkHttp客户端
     * @param gson Gson实例（来自AppModule）
     * @return Retrofit单例对象
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: com.google.gson.Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)  // 设置API基础URL
            .client(okHttpClient)  // 注入自定义的OkHttp客户端
            .addConverterFactory(GsonConverterFactory.create(gson))  // JSON转换器
            .build()
    }

    /**
     * 提供WeiguangApiService接口实现
     *
     * 使用Retrofit.create()通过动态代理生成接口的实现类。
     * 所有在WeiguangApiService中声明的方法都可以直接调用了。
     *
     * @param Retrofit实例（已配置好所有参数）
     * @return 可直接使用的API服务接口代理对象
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): WeiguangApiService {
        return retrofit.create(WeiguangApiService::class.java)
    }

    /**
     * 提供ApiClient辅助类（可选）
     *
     * 封装了Retrofit和OkHttp的高级配置逻辑，
     * 如果需要更复杂的自定义可以使用此类。
     *
     * 注意：如果使用了此类的createApiService()方法，
     * 则不需要上面的provideApiService()方法（二选一）。
     *
     * @param context Application Context
     * @param authInterceptor 认证拦截器
     * @param tokenRefreshInterceptor Token刷新拦截器
     * @return ApiClient实例
     */
    @Provides
    @Singleton
    fun provideApiClient(
        context: Context,
        authInterceptor: AuthInterceptor,
        tokenRefreshInterceptor: TokenRefreshInterceptor
    ): ApiClient {
        return ApiClient(context, authInterceptor, tokenRefreshInterceptor)
    }

    /**
     * 判断当前是否为Debug构建模式
     *
     * 用于决定是否启用详细日志等功能。
     *
     * @return true表示Debug版本，false表示Release版本
     */
    private fun isDebugMode(): Boolean {
        // 通过检查ApplicationInfo的DEBUGGABLE标志判断
        // 这种方式不需要BuildConfig字段（更通用）
        try {
            val applicationClass = Class.forName("android.app.ActivityThread")
            val currentActivityThread = applicationClass.getMethod("currentActivityThread")
                .invoke(null)
            val getApplication = currentActivityThread.javaClass.getMethod("application")
            val application = getApplication.invoke(currentActivityThread) as android.app.Application
            return application.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
        } catch (e: Exception) {
            // 异常情况默认返回false（安全起见不输出日志）
            return false
        }
    }
}
