/**
 * 文件名：AppModule.kt
 * 作者：微光同行前端团队
 * 功能描述：Hilt主依赖注入模块，提供Application级别的全局单例实例
 * 创建日期：2026-05-29
 * 所属模块：di（依赖注入层）
 *
 * 模块职责：
 * 1. 提供Context实例（Application Context）
 * 2. 提供DataStore Preferences实例（用户偏好存储）
 * 3. 提供Gson实例（JSON序列化）
 * 4. 协调NetworkModule和DatabaseModule的依赖关系
 *
 * 使用方式：
 * - 通过@Module注解标记为Hilt模块
 * - 通过@InstallIn指定模块的组件作用域（SingletonComponent = 全局单例）
 * - 通过@Provides方法提供依赖对象的创建逻辑
 *
 * 设计原则：
 * - 单例模式：全局共享一个实例（节省内存、保证一致性）
 * - 延迟初始化：首次使用时才创建（加快启动速度）
 * - 线程安全：Hilt自动处理并发访问问题
 */

package com.weiguangplus.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DataStore扩展属性（Context级别单例）
 *
 * 创建一个名为"app_preferences"的Preferences DataStore实例，
 * 用于存储用户偏好设置和轻量级数据。
 *
 * 特点：
 * - 异步API（不阻塞主线程）
 * - 类型安全（强类型的键值对）
 * - 数据一致性（事务性写入）
 * - 自动迁移（版本升级时平滑过渡）
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_preferences"
)

/**
 * 应用级Hilt模块
 *
 * @InstallIn(SingletonComponent::class) 表示此模块提供的依赖是全局单例，
 * 在整个应用生命周期内只创建一次。
 *
 * 与其他模块的关系：
 * - NetworkModule：提供Retrofit/OkHttp等网络相关依赖
 * - DatabaseModule：提供Room数据库等本地存储依赖
 * - 本模块提供基础设施工具（Context/DataStore/Gson）供其他模块使用
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * 提供Application Context实例
     *
     * 使用@ApplicationContext限定符确保注入的是Application Context，
     * 而非Activity或Service的Context（避免内存泄漏）。
     *
     * 为什么需要Application Context？
     * - 生命周期与App一致（不会因Activity销毁而失效）
     * - 不持有UI引用（安全用于后台操作）
     * - 全局唯一（适合作为单例的参数）
     *
     * @param context 由Hilt自动注入的ApplicationContext
     * @return Application Context对象
     */
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context.applicationContext
    }

    /**
     * 提供DataStore<Preferences>实例
     *
     * 用于存储用户的非敏感偏好设置。
     *
     * 典型使用场景：
     * - 用户主题设置（深色/浅色模式）
     * - 字体大小调整
     * - 开关状态（通知开关、振动反馈等）
     * - 缓存的Token等认证信息
     *
     * 注意事项：
     * - 不要存储敏感信息（如密码），应使用EncryptedSharedPreferences
     * - 不要存储大型数据（如图片），应使用Room数据库或文件系统
     * - 读写操作都是异步的（返回Flow<T>）
     *
     * @param context Application Context（用于创建DataStore）
     * @return 配置好的DataStore<Preferences>实例
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    /**
     * 提供Gson实例（JSON序列化工具）
     *
     * 用于Kotlin数据类与JSON字符串之间的相互转换。
     *
     * 当前配置：
     * - setLenient()：宽松解析模式（容错非标准JSON）
     * - setDateFormat()：统一日期格式为"yyyy-MM-dd HH:mm:ss"
     * - setPrettyPrinting()：美化输出格式（调试时便于阅读）
     *
     * 扩展建议：
     * - 可注册自定义TypeAdapter处理特殊类型
     * - 可配置字段命名策略（snake_case ↔ camelCase）
     *
     * @return 配置完成的Gson单例对象
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()  // 宽松模式，提高兼容性
            .setDateFormat("yyyy-MM-dd HH:mm:ss")  // 统一日期格式
            .setPrettyPrinting()  // 格式化输出（方便调试日志查看）
            .create()
    }
}
