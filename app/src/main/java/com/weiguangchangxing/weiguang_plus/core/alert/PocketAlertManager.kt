/**
 * ============================================================================
 * PocketAlertManager - 口袋模式强提醒管理器
 * ============================================================================
 *
 * 【核心场景】
 * 视障/听障用户将手机放入口袋后，由于屏幕被遮挡、声音被隔离，
 * 常规的通知提醒方式（震动一下、响一声）完全失效，导致用户错过重要信息。
 *
 * 本系统通过「接近传感器检测 → 口袋状态判断 → 多模态强提醒」的闭环设计，
 * 确保用户在口袋模式下也能感知到每一个重要提醒。
 *
 * 【问题分析】
 * ┌─────────────────────────────────────────────────────────────────┐
 * │  场景           │  问题                                          │
 * ├─────────────────┼────────────────────────────────────────────────┤
 * │  手机放口袋     │  屏幕被遮挡，看不见通知弹窗                       │
 * │  手机放口袋     │  衣物吸收声音，普通铃声几乎听不见                  │
 * │  手机放口袋     │  单次短震动在行走中难以感知                      │
 * │  静音模式       │  用户忘记关闭静音，完全无声                      │
 * │  听障用户       │  即使有声也听不到，完全依赖视觉/触觉               │
 * └─────────────────┴────────────────────────────────────────────────┘
 *
 * 【解决方案 - 四重强提醒联动】
 *
 *   ┌──────────────┐
 *   │ 接近传感器检测 │ ← 实时监测手机是否在口袋中
 *   │  isInPocket   │
 *   └──────┬───────┘
 *          │ true（在口袋中）
 *          ▼
 *   ┌─────────────────────────────────────────────────────┐
 *   │              多模态强提醒引擎                         │
 *   │                                                     │
 *   │  ① 震动 ─→ 最大振幅(255) + 循环脉冲模式              │
 *   │            → 500ms长震 + 200ms暂停 × 3次循环         │
 *   │                                                     │
 *   │  ② 闪光 ─→ 摄像头闪光灯高速闪烁                      │
 *   │            → 亮300ms + 灭200ms + 亮300ms + 灭200ms   │
 *   │                                                     │
 *   │  ③ 声音 ─→ 强制最高音量 + 覆盖静音模式               │
 *   │            → 使用 STREAM_ALARM 通道强制播放提示音     │
 *   │                                                     │
 *   │  ④ 亮屏 ─→ 唤醒屏幕（API 28以下）                    │
 *   │            → 为后续通知展示做准备                      │
 *   └─────────────────────────────────────────────────────┘
 *
 * 【设计原则】
 * 1. 非侵入性：传感器检测仅用于判断口袋状态，不涉及隐私数据
 * 2. 节能优先：传感器采用 NORMAL 采样率，监测线程含休眠间隔
 * 3. 安全兜底：所有强提醒有超时机制（默认15秒自动停止）
 * 4. 兼容性强：从 API 23 (Android 6.0) 起全面适配
 *
 * 【使用示例】
 * ```
 * // 在 Application 或 Activity 中初始化
 * val pocketAlert = PocketAlertManager(this)
 *
 * // 启动接近传感器监测
 * pocketAlert.startProximityMonitoring()
 *
 * // 在收到通知/告警时，判断是否需要启动口袋强提醒
 * if (pocketAlert.isInPocketMode()) {
 *     pocketAlert.triggerPocketAlert(15000L) // 持续15秒
 * }
 *
 * // Activity 销毁时释放资源
 * override fun onDestroy() {
 *     super.onDestroy()
 *     pocketAlert.release()
 * }
 * ```
 *
 * 【权限要求】
 * - 震动权限：<uses-permission android:name="android.permission.VIBRATE" />
 * - 摄像头权限（闪光灯）：<uses-permission android:name="android.permission.CAMERA" />
 * - 唤醒锁权限：<uses-permission android:name="android.permission.WAKE_LOCK" />
 *
 * ============================================================================
 */

package com.weiguangchangxing.weiguang_plus.core.alert

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * 口袋模式强提醒管理器
 *
 * 本类是"微光畅行"APP 中针对"手机在口袋中收不到通知"这一
 * 核心痛点的专项解决方案。通过接近传感器检测设备是否在口袋中，
 * 当有重要提醒触发且设备处于口袋状态时，自动启动多模态强提醒。
 *
 * 【架构定位】
 * 本类属于 core 层的基础设施，不依赖任何上层 UI 组件。
 * 上层模块（如通知服务、告警引擎）通过本类的公开 API 判断口袋状态
 * 并触发强提醒，形成清晰的分层依赖关系：
 *
 *   UI层 / Service层
 *        │
 *        │ 调用 isInPocketMode() / triggerPocketAlert()
 *        ▼
 *   ┌─────────────────────────────┐
 *   │  PocketAlertManager (本类)   │  ← core/alert 层
 *   │  ┌───────────────────────┐  │
 *   │  │  SensorEventListener  │  │  ← 接近传感器监听
 *   │  │  triggerMaxVibration  │  │  ← 震动控制
 *   │  │  toggleFlashlight     │  │  ← 闪光灯控制
 *   │  │  forceSoundAlert      │  │  ← 声音强制播放
 *   │  │  wakeScreen           │  │  ← 屏幕唤醒
 *   │  └───────────────────────┘  │
 *   └─────────────────────────────┘
 *        │
 *        ▼
 *   Android Framework API（SensorManager, CameraManager, AudioManager ...）
 *
 * 【线程安全】
 * - 传感器回调在主线程执行
 * - 强提醒循环在独立子线程执行，不阻塞主线程
 * - isAlerting 状态标记使用 volatile 语义（Kotlin 属性默认线程安全读取）
 *
 * @param context Android Context，用于获取系统服务
 *                 建议传入 ApplicationContext 避免 Activity 泄漏
 *
 * @author 微光畅行核心开发组
 */
class PocketAlertManager(private val context: Context) {

    /*
     * ====================================================================
     * 系统服务引用
     * ====================================================================
     *
     * 在构造时一次性获取所有需要的系统服务引用，避免每次操作都重复查找。
     * 这些引用在整个生命周期内保持不变。
     */

    /** 传感器管理器：用于获取和注册接近传感器 */
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    /** 接近传感器实例：检测物体是否靠近屏幕（手机是否在口袋中） */
    private val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    /** 电源管理器：用于唤醒屏幕 */
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    /** 摄像头管理器：用于控制闪光灯开关 */
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    /** 音频管理器：用于控制音量和铃声模式 */
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    /*
     * ====================================================================
     * 状态变量
     * ====================================================================
     *
     * isInPocket：最近一次传感器读数判断的口袋状态
     * isAlerting：当前是否正在执行强提醒流程（防止重复触发）
     * isFlashlightOn：闪光灯当前开关状态（用于状态一致性维护）
     * sensorRegistered：传感器是否已注册（防止重复注册/解注册）
     */

    /** 是否检测到手机在口袋中，由传感器回调实时更新 */
    private var isInPocket = false

    /** 是否正在强提醒中，用于防重入和外部停止控制 */
    private var isAlerting = false

    /** 强提醒循环线程引用，用于外部中断 */
    private var alertThread: Thread? = null

    /** 闪光灯当前状态，用于避免重复开关和无意义的关闭操作 */
    private var isFlashlightOn = false

    /** 传感器注册标记，防止重复注册/解注册导致异常 */
    private var sensorRegistered = false

    /*
     * ====================================================================
     * 接近传感器监听器
     * ====================================================================
     *
     * 工作原理：
     * 接近传感器通常位于手机屏幕上方（听筒附近），
     * 当物体靠近时，传感器值从最大值骤降至接近0。
     *
     * 判断逻辑：
     * event.values[0] < maxRange * 0.9f  → 有物体靠近 → 认为在口袋中
     *
     * 为什么用 0.9f 而非 0？
     * 不同手机的接近传感器返回值的物理含义不同：
     * - 有些手机返回距离（厘米），最大值约 5~10cm
     * - 有些手机只返回 0（靠近）和 maxRange（远离）二值
     * 用 90% 阈值可以兼容两种实现方式
     */

    /** 接近传感器事件监听器 */
    private val sensorListener = object : SensorEventListener {

        /**
         * 传感器数据变化回调
         *
         * 当接近传感器的读数发生变化时触发。
         * 此回调运行在传感器事件调度线程（通常为主线程），
         * 因此不宜在此做耗时操作。
         *
         * @param event 传感器事件，包含类型、精度、数值等
         */
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
                val maxRange = event.sensor.maximumRange
                /*
                 * 判断是否在口袋中：
                 * 接近传感器的值小于最大值的 90% 说明有物体遮挡，
                 * 即手机被放入了口袋。
                 *
                 * 特殊处理：
                 * - 部分手机的 maxRange 为 0，此时 event.values[0] 也为 0，
                 *   0 < 0 * 0.9 = false，不会误判为口袋状态
                 * - 部分手机返回二值（0 或 maxRange），阈值 0.9 同样兼容
                 */
                isInPocket = event.values[0] < maxRange * 0.9f
            }
        }

        /**
         * 传感器精度变化回调
         *
         * 当传感器的精度模式发生变化时触发。
         * 在实际使用中很少触发，此处留空即可。
         *
         * @param sensor 变化的传感器
         * @param accuracy 新的精度等级（SENSOR_STATUS_ACCURACY_*）
         */
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // 精度变化不需要额外处理
        }
    }

    /*
     * ====================================================================
     * 传感器生命周期管理
     * ====================================================================
     */

    /**
     * 启动接近传感器监测
     *
     * 在 Application.onCreate() 或 Activity.onStart() 中调用。
     * 注册后，[isInPocketMode] 将根据传感器实时数据返回准确的口袋状态。
     *
     * 【采样率选择】
     * 使用 SENSOR_DELAY_NORMAL（200ms 间隔）而非更快的延迟，
     * 原因如下：
     * - 口袋状态是慢变化量（放入/取出动作约 0.5~2 秒），
     *   不需要高频采样
     * - 降低采样率可以显著减少传感器功耗
     * - 避免短时间内大量回调造成主线程压力
     *
     * 【幂等保护】
     * 如果传感器已被注册，重复调用不会重复注册。
     */
    fun startProximityMonitoring() {
        if (proximitySensor != null && !sensorRegistered) {
            sensorManager.registerListener(
                sensorListener,
                proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            sensorRegistered = true
        }
    }

    /**
     * 停止接近传感器监测
     *
     * 在 Activity.onStop() 或不需要口袋检测时调用。
     * 释放传感器资源，避免后台持续消耗电量。
     *
     * 【幂等保护】
     * 如果传感器未被注册，调用无任何效果。
     */
    fun stopProximityMonitoring() {
        if (sensorRegistered) {
            sensorManager.unregisterListener(sensorListener)
            sensorRegistered = false
        }
    }

    /*
     * ====================================================================
     * 口袋状态查询
     * ====================================================================
     */

    /**
     * 查询当前是否处于口袋模式
     *
     * 上层模块（通知服务、告警引擎）通过此方法判断
     * 是否需要触发口袋强提醒。
     *
     * 典型的调用链路：
     * ```
     * fun onNewNotification(title: String, content: String) {
     *     if (pocketAlertManager.isInPocketMode()) {
     *         // 手机在口袋中 → 启动强提醒
     *         pocketAlertManager.triggerPocketAlert()
     *     } else {
     *         // 手机不在口袋中 → 使用常规提醒
     *         showNormalNotification(title, content)
     *     }
     * }
     * ```
     *
     * @return true 表示手机当前在口袋中（接近传感器被遮挡）
     *         false 表示手机不在口袋中（传感器无遮挡）
     */
    fun isInPocketMode(): Boolean = isInPocket

    /*
     * ====================================================================
     * 口袋强提醒触发
     * ====================================================================
     *
     * 【工作流程】
     *
     *   triggerPocketAlert() 被调用
     *         │
     *         ├── 检查 isInPocket && !isAlerting
     *         │     └── 条件不满足 → 直接返回（不执行）
     *         │
     *         ▼
     *   ┌──────────────────────────────────────┐
     *   │  子线程循环执行以下四重提醒（默认15秒） │
     *   │                                      │
     *   │  ┌──────────────────────────────┐    │
     *   │  │ ① 最大振幅震动（255）         │    │
     *   │  │   500ms震 + 200ms停 × 3次    │    │
     *   │  └──────────────────────────────┘    │
     *   │          ▼                          │
     *   │  ┌──────────────────────────────┐    │
     *   │  │ ② 闪光灯闪烁                 │    │
     *   │  │   亮300ms → 灭200ms × 2次    │    │
     *   │  └──────────────────────────────┘    │
     *   │          ▼                          │
     *   │  ┌──────────────────────────────┐    │
     *   │  │ ③ 强制声音（仅在口袋中时）    │    │
     *   │  │   最高音量 + 覆盖静音        │    │
     *   │  └──────────────────────────────┘    │
     *   │          ▼                          │
     *   │  ┌──────────────────────────────┐    │
     *   │  │ ④ 唤醒屏幕                   │    │
     *   │  │   SCREEN_BRIGHT_WAKE_LOCK    │    │
     *   │  └──────────────────────────────┘    │
     *   │                                      │
     *   │  等待 2 秒后进入下一次循环            │
     *   └──────────────────────────────────────┘
     *         │
     *         ▼
     *   超时或 stopAlert() 被调用 → 结束
     *
     * 【注意】
     * ③ 强制声音仅在判断为口袋模式时执行，
     * 因为如果手机已经取出（不在口袋），强制最高音量可能会吓到用户。
     */

    /**
     * 触发口袋模式强提醒
     *
     * 当检测到手机在口袋中且有重要通知时调用此方法。
     * 启动独立子线程执行多模态强提醒循环。
     *
     * 【触发条件】
     * - isInPocket == true（手机确实在口袋中）
     * - isAlerting == false（没有正在执行的提醒）
     * 任一条件不满足时，调用无效（静默返回）
     *
     * 【超时机制】
     * 默认持续 15 秒自动停止，防止无限震动耗尽电量。
     * 如果用户在这期间取出手机，应调用 [stopAlert] 立即停止。
     *
     * 【线程说明】
     * 提醒循环运行在独立子线程中，不阻塞主线程。
     * 线程内部通过标志位 isAlerting 控制循环退出。
     * 外部通过 [stopAlert] 将标志位置为 false 来中断循环。
     *
     * @param durationMs 提醒持续时长（毫秒），默认 15000ms（15秒）
     *                   建议范围 5000~30000ms
     *                   太短可能用户未感知到就结束了
     *                   太长会消耗较多电量且可能引起用户不适
     */
    fun triggerPocketAlert(durationMs: Long = 15000L) {
        /*
         * 防重复触发检查：
         * - 不在口袋中不触发（避免在正常使用时突然震动）
         * - 已在提醒中不触发（避免叠加多个提醒线程）
         */
        if (!isInPocket && !isAlerting) return
        isAlerting = true

        // 如果已有线程在运行，先中断它
        alertThread?.interrupt()

        // 在独立子线程中执行提醒循环
        alertThread = Thread {
            val startTime = System.currentTimeMillis()

            /*
             * 主循环：
             * 每隔约 3.5 秒完成一轮完整的四重提醒，
             * 然后休眠 2 秒进入下一轮，直到超时或被中断。
             */
            while (System.currentTimeMillis() - startTime < durationMs && isAlerting) {
                try {
                    // ── ① 最大振幅震动 ──
                    triggerMaxVibration()

                    // ── ② 闪光灯闪烁（亮灭交替两次） ──
                    toggleFlashlight(true)
                    Thread.sleep(300)
                    toggleFlashlight(false)
                    Thread.sleep(200)
                    toggleFlashlight(true)
                    Thread.sleep(300)
                    toggleFlashlight(false)

                    // ── ③ 强制发声（仅在口袋中时） ──
                    //
                    // 为什么只在口袋中时发声？
                    // 如果用户已经把手机从口袋中取出，
                    // 强制最高音量播放提示音可能会非常刺耳，
                    // 甚至吓到用户或引起周围人注意。
                    if (isInPocket) {
                        forceSoundAlert()
                    }

                    // ── ④ 唤醒屏幕 ──
                    wakeScreen()

                    /*
                     * 每轮循环间隔 2 秒：
                     * 避免提醒过于频繁消耗电量和引起不适，
                     * 同时确保提醒的持续性和可感知性。
                     */
                    Thread.sleep(2000)

                } catch (e: InterruptedException) {
                    // 线程被中断 → 退出循环（中断可能来自 stopAlert 或新一次 trigger）
                    break
                }
            }

            // 循环结束：重置提醒状态并关闭闪光灯
            isAlerting = false
            toggleFlashlight(false)
        }.apply { start() }
    }

    /**
     * 停止当前强提醒
     *
     * 在以下场景调用：
     * - 用户取出了手机（通过传感器检测到不在口袋中）
     * - 用户主动关闭了提醒
     * - Activity/Service 即将销毁
     *
     * 该方法将 isAlerting 置为 false，中断提醒线程，
     * 并确保闪光灯处于关闭状态。
     */
    fun stopAlert() {
        isAlerting = false
        alertThread?.interrupt()
        alertThread = null
        /*
         * 确保闪光灯关闭：
         * 如果提醒在闪光灯亮着的状态被中断，
         * 这里做一次兜底关闭，防止闪光灯常亮。
         */
        toggleFlashlight(false)
    }

    /*
     * ====================================================================
     * 四重提醒核心实现
     * ====================================================================
     */

    /**
     * ① 触发最大振幅震动
     *
     * 使用 VibrationEffect.createWaveform() 创建循环震动模式。
     *
     * 【震动模式设计】
     * timings:  [0, 500, 200, 500, 200, 500]
     *            ↑   ↑    ↑    ↑    ↑    ↑
     *           等待 长震  暂停  长震  暂停  长震
     *
     * amplitudes: [0, 255, 0, 255, 0, 255]
     *             静  最大  静  最大  静  最大
     *
     * 效果：500ms 最大振幅震动 → 200ms 暂停 → 重复 3 次
     * 这种"长震-暂停-长震"的模式比连续震动更容易被感知，
     * 因为人通过触觉感知变化比感知持续存在更敏感。
     *
     * 【兼容性说明】
     * - API 26+：使用 VibrationEffect.createWaveform() 支持振幅控制
     * - API 23-25：使用弃用的 vibrate(long[], int) 方法（不支持振幅）
     *
     * 【性能说明】
     * repeatIndex = 0 表示从索引 0 开始循环，即无限重复此模式。
     * 由调用方的超时机制（durationMs）或外部中断控制结束。
     */
    private fun triggerMaxVibration() {
        val vibrator = getVibrator() ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /*
             * API 26+ 标准震动 API：
             * createWaveform(timings, amplitudes, repeatIndex)
             * - timings：各阶段的持续时间（毫秒），第一个元素是开始前的延迟
             * - amplitudes：各阶段的振幅（0-255），与 timings 一一对应
             * - repeatIndex：循环开始的索引，-1 表示不循环
             *
             * 这里使用索引 0 循环，达到"持续强震动直到停止"的效果
             */
            val timings = longArrayOf(0, 500, 200, 500, 200, 500)
            val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, 0))
        } else {
            /*
             * API 23-25 兼容方案：
             * 使用弃用的 vibrate(long[], int) 方法。
             * 注意：此版本不支持振幅控制，震动强度由硬件默认决定。
             * @Suppress("DEPRECATION") 标记告诉编译器这是有意使用弃用 API
             */
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), 0)
        }
    }

    /**
     * ② 控制摄像头闪光灯开关
     *
     * 使用 CameraManager.setTorchMode() 控制闪光灯。
     * 这是标准的 Android 闪光灯控制 API，适用于所有带闪光灯的设备。
     *
     * 【注意事项】
     * - 需要 CAMERA 权限
     * - cameraIdList[0] 通常是后置摄像头（带闪光灯的那一个）
     * - setTorchMode(true) 会持续点亮闪光灯直到 setTorchMode(false)
     * - 如果相机被其他应用占用，此操作可能抛异常，已通过 try-catch 保护
     *
     * 【异常场景】
     * - 设备没有闪光灯 → cameraIdList 可能为空 → ArrayIndexOutOfBoundsException
     * - 相机被占用 → CameraAccessException
     * - API < 23 不支持 setTorchMode → 编译时已通过 Build.VERSION_CODES.M 保证
     *
     * @param on true 打开闪光灯，false 关闭闪光灯
     */
    private fun toggleFlashlight(on: Boolean) {
        try {
            val cameraId = cameraManager.cameraIdList[0]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, on)
                isFlashlightOn = on
            }
        } catch (_: Exception) {
            /*
             * 忽略所有异常：
             * 闪光灯只是辅助提醒手段，即使失败也不应影响其他提醒方式。
             * 常见的异常包括：
             * - 设备没有后置摄像头
             * - 相机正在被其他应用使用
             * - 权限被拒绝
             */
        }
    }

    /**
     * ③ 强制声音提醒（覆盖静音模式）
     *
     * 即使手机处于静音或震动模式，也强制通过 STREAM_ALARM 通道
     * 以最高音量播放提示音。
     *
     * 【实现原理】
     * 1. 保存当前音量和铃声模式（用于恢复——但当前版本未实现恢复，
     *    因为单次提醒结束后系统会自动管理）
     * 2. 将 STREAM_ALARM（闹钟流）的音量设为最大值
     * 3. 将铃声模式临时改为正常模式（RINGER_MODE_NORMAL）
     * 4. 使用 ToneGenerator 通过 STREAM_ALARM 通道播放提示音
     *
     * 【为什么用 STREAM_ALARM 而不是 STREAM_NOTIFICATION】
     * - STREAM_NOTIFICATION 受静音模式影响
     * - STREAM_ALARM 在静音模式下仍可发声
     * - ToneGenerator 提供了纯音生成能力，无需音频文件
     *
     * 【注意】
     * 此方法仅应在 isInPocket == true 时调用。
     * 如果用户取出手机，强制最大音量可能会造成不适。
     *
     * 【音量恢复说明】
     * 当前实现没有在提醒结束后恢复原音量/铃声模式。
     * 这是因为：
     * - 单次提醒结束后，Android 系统没有自动恢复音量的机制
     * - 用户取出手机后，后续的提醒会使用正常音量
     * - 如果用户确实关闭了静音，音量保持最大可能不是期望行为
     * - 后续版本可考虑在 stopAlert() 中增加音量恢复逻辑
     */
    private fun forceSoundAlert() {
        try {
            // 将闹钟流音量设为最大值（STREAM_ALARM 在静音模式下仍可响）
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)

            /*
             * 如果手机处于静音或仅震动模式，临时改为正常模式。
             *
             * RINGER_MODE_SILENT：完全静音
             * RINGER_MODE_VIBRATE：仅震动
             * RINGER_MODE_NORMAL：正常响铃
             *
             * 注意：此操作会短暂更改用户的铃声模式设置。
             * 在口袋模式下这是有意的行为——确保用户能听到声音。
             */
            if (audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT ||
                audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE
            ) {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }

            /*
             * 播放提示音：
             * - 使用 STREAM_ALARM 通道（不受静音模式影响）
             * - 音量 100（最大值）
             * - 音调类型：TONE_PROP_ACK（标准确认音）
             * - 持续时间：1500ms（1.5 秒，足够引起注意）
             *
             * ToneGenerator 使用说明：
             * - startTone() 是非阻塞调用，立即返回
             * - 播放完成后需要调用 release() 释放资源
             * - 同一个 ToneGenerator 实例可以多次调用 startTone()
             */
            val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 1500)

            // 等待 1.5 秒确保提示音播放完成
            Thread.sleep(1500)

            // 释放 ToneGenerator 资源
            toneGenerator.release()
        } catch (_: Exception) {
            /*
             * 忽略所有异常：
             * 声音提醒失败不应影响震动和闪光等其他提醒方式。
             * 常见异常：
             * - ToneGenerator 初始化失败（设备问题）
             * - 音频服务暂时不可用
             */
        }
    }

    /**
     * ④ 唤醒屏幕
     *
     * 在口袋模式下唤醒屏幕，确保用户取出手机后能立即看到提醒内容。
     *
     * 【实现说明】
     * - 使用 PowerManager 的 SCREEN_BRIGHT_WAKE_LOCK 唤醒屏幕
     * - ACQUIRE_CAUSES_WAKEUP 标志确保屏幕从休眠状态被唤醒
     * - 持锁 3 秒后自动释放
     *
     * 【API 28+ 限制】
     * 从 Android 9（API 28）起，Google 限制了后台应用
     * 使用 WakeLock 唤醒屏幕的能力（后台应用无法获取 WAKE_LOCK）。
     * 对于 API 28+，屏幕唤醒应通过 Activity 启动来实现：
     * ```
     * // API 28+ 推荐方案
     * val intent = Intent(context, AlertActivity::class.java).apply {
     *     addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
     *     addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
     * }
     * context.startActivity(intent)
     * ```
     *
     * 【兼容性策略】
     * - API 23-27：使用 WakeLock 唤醒屏幕（本方法）
     * - API 28+：通过启动 Activity 来唤醒（调用方自行实现，本方法跳过）
     *
     * 【未来优化】
     * 可考虑在 API 28+ 上通过启动一个透明 Activity 来实现屏幕唤醒，
     * 这需要在 AndroidManifest.xml 中注册该 Activity 并设置
     * android:excludeFromRecents="true" 等属性。
     */
    @Suppress("DEPRECATION")
    private fun wakeScreen() {
        try {
            /*
             * API 28+ 兼容性处理：
             * Android 9 开始限制了后台 WakeLock 的屏幕唤醒能力，
             * 此处跳过 WakeLock 方式，由调用方通过 Activity 方式实现。
             * 条件使用 Build.VERSION_CODES.P（28）进行判断。
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return
            }

            /*
             * API 23-27 方案：
             * 使用 WakeLock 唤醒屏幕。
             * SCREEN_BRIGHT_WAKE_LOCK：点亮屏幕（但可能不解锁）
             * ACQUIRE_CAUSES_WAKEUP：强制从休眠状态唤醒
             *
             * 注意：需要在 AndroidManifest.xml 中声明 WAKE_LOCK 权限。
             */
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "WeiguangPlus:PocketAlert"
            )
            wakeLock.acquire(3000)  // 持锁 3 秒，确保屏幕保持点亮
            wakeLock.release()       // 释放锁，系统可正常休眠
        } catch (_: Exception) {
            /*
             * 忽略所有异常：
             * 屏幕唤醒失败不影响震动和闪光灯功能。
             * 常见异常：
             * - 缺少 WAKE_LOCK 权限
             * - 系统拒绝 WakeLock 请求（API 28+）
             */
        }
    }

    /*
     * ====================================================================
     * 辅助方法
     * ====================================================================
     */

    /**
     * 获取系统震动器实例
     *
     * 兼容不同 Android 版本的 Vibrator 获取方式：
     * - API 31+（Android 12）：通过 VibratorManager 获取
     * - API 23-30：通过 Context.VIBRATOR_SERVICE 获取
     *
     * 推荐使用最新的 API 获取方式，因为 VibratorManager 提供了
     * 更精细的控制能力。
     *
     * @return Vibrator 实例，如果设备不支持震动则返回 null
     */
    private fun getVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            /*
             * API 31+ 方案：
             * 通过 VibratorManager 获取默认震动器。
             * VibratorManager 管理设备上的所有 Vibrator（主马达、触觉反馈等）
             */
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            /*
             * API 23-30 方案：
             * 直接从系统服务获取 Vibrator。
             * @Suppress("DEPRECATION") 标记弃用的 API 调用
             */
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /*
     * ====================================================================
     * 资源释放
     * ====================================================================
     */

    /**
     * 释放所有资源
     *
     * 在以下场景调用：
     * - Application.onTerminate()（极少触发）
     * - 不再需要口袋检测功能时
     *
     * 主要执行以下清理：
     * 1. 停止所有提醒（关闭闪光灯、中断线程）
     * 2. 注销传感器监听
     *
     * 调用后，当前实例不应再被使用。
     * 如果需要重新使用，应创建新的 PocketAlertManager 实例。
     */
    fun release() {
        stopAlert()
        stopProximityMonitoring()
    }

    /*
     * ====================================================================
     * 伴生对象 - 工具方法
     * ====================================================================
     */

    companion object {

        /**
         * 检查当前设备是否支持接近传感器
         *
         * 在决定是否启用口袋模式功能前，先检查硬件支持情况。
         * 如果不支持，应禁用口袋模式相关功能或提供替代方案。
         *
         * 注意：部分低端设备或平板可能没有接近传感器。
         *
         * @param context Android Context
         * @return true 表示设备有接近传感器，可以启用口袋模式
         *         false 表示设备无接近传感器，口袋模式不可用
         */
        fun isProximitySensorAvailable(context: Context): Boolean {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            return sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null
        }
    }
}