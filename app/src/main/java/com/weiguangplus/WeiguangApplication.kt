/**
 * 文件名：WeiguangApplication.kt
 * 作者：微光同行前端团队
 * 功能描述：微光同行Application类，作为Hilt依赖注入的入口点和全局初始化中心
 * 创建日期：2026-05-29
 * 所属模块：根包（Application类）
 *
 * 核心职责：
 * 1. 使用@HiltAndroidApp注解启用Hilt依赖注入框架
 * 2. 在onCreate()中执行应用级别的初始化操作
 * 3. 在onTerminate()中执行资源清理（仅用于测试环境）
 *
 * Hilt工作原理：
 * - @HiltAndroidApp注解触发Hilt代码生成器
 * - 自动生成HiltComponents_Application基类
 * - 创建SingletonComponent（全局单例容器）
 * - 扫描所有@Module和@InstallIn注解的类
 * - 构建依赖关系图（Dependency Graph）
 *
 * Application生命周期：
 * 1. 系统创建进程 → 实例化Application子类
 * 2. 调用attachBaseContext() → Hilt在此处注入依赖
 * 3. 调用onCreate() → 应用初始化（本方法）
 * 4. Activity/Service等组件启动 → 通过@HiltAndroidInject获取依赖
 * 5. 应用终止 → onTerminate()（仅在模拟器上调用）
 *
 * 初始化清单（按顺序）：
 * ✅ Hilt依赖注入框架（自动完成）
 * ✅ TTS语音引擎（TextToSpeechManager.initialize()）
 * ✅ 感知引擎（FusionPerceptionEngine.initialize()）
 * ✅ 紧急联系人管理（EmergencyContactManager.init()）
 */

package com.weiguangplus

import android.app.Application
import com.weiguangchangxing.weiguang_plus.core.tts.TTSManager
import com.weiguangchangxing.weiguang_plus.core.perception.FusionPerceptionEngine
import com.weiguangchangxing.weiguang_plus.core.emergency.EmergencyContactManager
import dagger.hilt.android.HiltAndroidApp

/**
 * 微光同行自定义Application类
 *
 * 使用@HiltAndroidApp注解标记为Hilt的应用入口点。
 * 必须在AndroidManifest.xml中通过android:name属性声明此类。
 *
 * AndroidManifest.xml配置示例：
 * ```xml
 * <application
 *     android:name=".WeiguangApplication"
 *     ... >
 * ```
 *
 * 继承关系：
 * WeiguangApplication → Application（Android SDK基类）
 *
 * 注意事项：
 * - 此类的构造函数必须是无参的（系统反射实例化要求）
 * - 不要在构造函数中做耗时操作（会影响冷启动时间）
 * - onCreate()中的初始化应尽量轻量或异步执行
 */
@HiltAndroidApp  // 关键注解：启用Hilt依赖注入
class WeiguangApplication : Application() {

    /**
     * Application创建时的回调方法
     *
     * 在应用的整个生命周期中只调用一次。
     * 是进行全局初始化的最佳时机。
     *
     * 执行流程：
     * 1. 调用父类的onCreate()（必须首先调用）
     * 2. 初始化TTS语音引擎（无障碍播报核心能力）
     * 3. 初始化感知融合引擎（障碍物检测、手语识别等）
     * 4. 初始化紧急联系人管理器（SOS求助功能支持）
     * 5. （可扩展）其他第三方SDK初始化
     */
    override fun onCreate() {
        super.onCreate()

        // 步骤1：初始化TTS语音引擎
        // TTSManager是全局单例，负责所有文字转语音功能。
        // 包括：药品信息播报、导航提示、紧急警告等。
        // 初始化过程会加载系统TTS服务，可能需要几百毫秒。
        TTSManager.initialize(this)

        // 步骤2：初始化感知融合引擎
        // FusionPerceptionEngine整合多种传感器数据：
        // - 相机视觉识别（ML Kit + CameraX）
        * - 麦克风声音监测（SpeechRecognizer）
        * - 加速度计振动反馈（Vibrator）
        * - GPS定位导航（LocationManager）
        // 用于实时感知用户周围环境和状态。
        FusionPerceptionEngine.initialize(this)

        // 步骤3：初始化紧急联系人管理器
        // EmergencyContactManager负责：
        * - 从本地数据库加载预设的紧急联系人列表
        * - 提供SOS短信发送功能
        * - 管理联系人的增删改查操作
        EmergencyContactManager.init(this)

        // TODO: 可在此处添加更多初始化逻辑：
        // - Crashlytics崩溃上报（Firebase）
        * - 友盟/个推统计SDK
        * - Bugly异常监控
        * - LeakCanary内存泄漏检测（Debug模式）
    }

    /**
     * Application终止时的回调方法
     *
     * ⚠️ 注意：此方法在真机上不会被调用！
     * 仅在模拟器环境下或测试时才会触发。
     * 真机上的应用终止是由系统直接杀进程完成的。
     *
     * 因此，重要的资源清理不应依赖此方法，
     * 应该在各组件的onDestroy()中自行清理。
     *
     * 清理操作（当前实现）：
     * - 关闭TTS引擎释放资源
     * - 停止感知引擎的所有传感器监听
     */
    override fun onTerminate() {
        super.onTerminate()

        // 停止TTS语音引擎（释放TextToSpeech对象）
        TTSManager.shutdown()
        // 感知引擎通常不需要显式停止（跟随进程销毁自动释放）
        // FusionPerceptionEngine.shutdown() // 如需要可取消注释
    }
}
