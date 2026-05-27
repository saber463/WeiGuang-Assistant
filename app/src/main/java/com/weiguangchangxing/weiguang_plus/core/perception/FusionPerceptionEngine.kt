package com.weiguangchangxing.weiguang_plus.core.perception

import android.content.Context
import com.weiguangchangxing.weiguang_plus.core.tts.TTSManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ============================================================================
 * FusionPerceptionEngine - 融合感知引擎
 * ============================================================================
 *
 * 【核心定位】
 * 这是整个 APP 的"感知中枢神经系统"。
 * 所有外部环境信息——无论是摄像头看到的、麦克风听到的、传感器感受到的——
 * 最终都汇聚到这里，被统一处理、排序、去重，然后分发到各个输出通道。
 *
 * 【设计模式】
 * 采用单例模式（object），确保全 APP 只有一个感知事件处理中心。
 * 任何模块都可以通过 FusionPerceptionEngine.emitEvent() 注入事件，
 * 而不需要与其它模块直接耦合。这实现了"发布-订阅"的松耦合架构。
 *
 * 【数据流全景】
 *
 *   摄像头分析 ─┐
 *   声音检测   ─┤
 *   传感器读取 ─┼──→ emitEvent() ──→ FusionPerceptionEngine ──→ StateFlow(UI层)
 *   GPS/导航   ─┤                                        ├──→ TTSManager.speakNow()
 *   用户求助   ─┘                                        └──→ VibrationEncoder
 *
 * 【核心职责】
 * 1. 事件汇聚：接收各模块注入的 PerceptionEvent
 * 2. 防抖去重：同一类型事件在3秒内只保留最新一条（MEDIUM及以下）
 * 3. 优先级排序：事件队列按优先级降序排列，EMERGENCY 始终在最前
 * 4. 自动反馈：HIGH/EMERGENCY 事件自动触发 TTS + 震动 + UI闪烁标记
 * 5. 历史管理：维护最近50条事件记录，支持查询和清空
 *
 * 【使用示例】
 * ```
 * // 在 Application.onCreate() 中初始化
 * FusionPerceptionEngine.initialize(this)
 *
 * // 在物体检测模块中注入事件
 * FusionPerceptionEngine.emitEvent(
 *     PerceptionEvent(
 *         type = PerceptionEventType.OBSTACLE_AHEAD,
 *         priority = PerceptionPriority.HIGH,
 *         sourceModule = "object_detection",
 *         description = "前方2米有障碍物",
 *         direction = "front",
 *         distance = 2.0f,
 *         confidence = 0.92f
 *     )
 * )
 *
 * // 在UI层观察事件流
 * lifecycleScope.launch {
 *     FusionPerceptionEngine.events.collect { eventList ->
 *         adapter.submitList(eventList)
 *     }
 * }
 * ```
 *
 * ============================================================================
 */

/**
 * 融合感知引擎单例
 *
 * 作为全 APP 唯一的感知事件处理中心，负责事件的汇聚、处理、分发。
 * 使用 object 关键字实现线程安全的单例模式。
 */
object FusionPerceptionEngine {

    /*
     * ====================================================================
     * 状态管理 - 使用 StateFlow 实现响应式数据流
     * ====================================================================
     *
     * _events 是可变状态，events 是对外暴露的不可变 StateFlow。
     * UI层或其他模块通过 collect events 来实时获取事件列表更新。
     * 这种设计模式是 Kotlin Flow 的推荐做法：
     * - 内部可变（MutableStateFlow），外部只读（StateFlow）
     * - 修改状态时自动通知所有收集者
     */
    private val _events = MutableStateFlow<List<PerceptionEvent>>(emptyList())
    val events: StateFlow<List<PerceptionEvent>> = _events.asStateFlow()

    /**
     * 防抖记录表
     *
     * 记录每种事件类型最近一次发射的时间戳（毫秒）。
     * 用于实现3秒去重：如果同一类型事件在3秒内重复触发，
     * 且优先级为 MEDIUM 及以下，则丢弃后续重复事件。
     *
     * key: PerceptionEventType（事件类型）
     * value: 最近一次 emit 的时间戳（System.currentTimeMillis()）
     */
    private val lastEmitTime = mutableMapOf<PerceptionEventType, Long>()

    /** 防抖时间窗口：3秒内同一类型事件只保留一次 */
    private const val DEBOUNCE_MS = 3000L

    /** 历史事件队列最大容量：最多保留最近50条 */
    private const val MAX_HISTORY = 50

    /**
     * 震动编码器实例
     *
     * 由 initialize() 方法在 APP 启动时创建。
     * 负责将 HIGH/EMERGENCY 事件编码为对应的震动模式。
     */
    private var vibrationEncoder: VibrationEncoder? = null

    /*
     * ====================================================================
     * 初始化与生命周期
     * ====================================================================
     */

    /**
     * 初始化融合感知引擎
     *
     * 必须在 Application.onCreate() 或第一个 Activity 的
     * onCreate() 中调用。主要完成以下初始化：
     * 1. 创建 VibrationEncoder 实例（依赖 Android Context）
     *
     * TTSManager 的初始化应在外部独立完成，本引擎不依赖 TTSManager.
     * initialize() 的调用时机，TTSManager 可能尚未初始化。
     *
     * @param context Android Context，用于获取系统服务
     */
    fun initialize(context: Context) {
        vibrationEncoder = VibrationEncoder(context)
    }

    /*
     * ====================================================================
     * 核心方法：事件注入
     * ====================================================================
     */

    /**
     * 注入感知事件（核心入口方法）
     *
     * 各模块通过此方法将感知到的事件注入引擎。
     * 引擎会依次执行：防抖检查 → 入队 → 排序 → 反馈触发
     *
     * 【防抖去重逻辑】
     * - HIGH 和 EMERGENCY 事件：跳过防抖，立即通过（安全攸关，不能丢）
     * - MEDIUM 和 LOW 事件：3秒内同一类型重复触发则丢弃
     *   例如：物体检测模块每500ms检测一次"前方有椅子"，
     *   3秒内只有第一条会被处理，后续被丢弃
     *
     * 【自动反馈逻辑】
     * - HIGH 优先级：TTS播报 + 震动反馈
     * - EMERGENCY 优先级：TTS播报 + 强震动反馈
     * - MEDIUM 优先级：仅震动反馈
     * - LOW 优先级：无反馈，仅存入历史
     *
     * @param event 感知事件对象。调用方需确保必填字段（type, sourceModule, description）已填充
     */
    fun emitEvent(event: PerceptionEvent) {
        val now = System.currentTimeMillis()
        val lastTime = lastEmitTime[event.type] ?: 0L

        /*
         * 防抖检查：
         * 只有 MEDIUM 及以下优先级才执行防抖去重。
         * HIGH 和 EMERGENCY 代表安全事故，不允许去重丢失。
         *
         * 判断条件：
         * 1. 距离上次同类型事件 < 3秒
         * 2. 优先级 <= MEDIUM
         * 两个条件同时满足 → 丢弃当前事件，不处理
         */
        if (now - lastTime < DEBOUNCE_MS &&
            event.priority.ordinal <= PerceptionPriority.MEDIUM.ordinal
        ) {
            return
        }

        // 更新该事件类型的最后发射时间
        lastEmitTime[event.type] = now

        /*
         * 将新事件插入队列头部（最新事件在最前）
         * 然后按优先级降序排序，确保 EMERGENCY 在最前
         */
        val current = _events.value.toMutableList()
        current.add(0, event)

        // 超出容量则移除最旧的一条（队列尾部）
        if (current.size > MAX_HISTORY) {
            current.removeAt(current.size - 1)
        }

        /*
         * 按优先级降序排列。
         * ordinal 值越大优先级越高：EMERGENCY(3) > HIGH(2) > MEDIUM(1) > LOW(0)
         * sortedByDescending 保证高优先级事件排在列表前面，
         * UI层展示时用户最先看到最重要的信息
         */
        _events.value = current.sortedByDescending { it.priority.ordinal }

        /*
         * ================================================================
         * 自动反馈触发
         * ================================================================
         *
         * HIGH 及以上级别的事件需要多通道反馈，确保用户一定能感知到：
         * 1. TTS语音播报：用声音告知用户发生了什么
         * 2. 震动反馈：用触觉提醒用户注意（手机静音时仍有效）
         * 3. UI闪烁标记：通过 getAlertState() 暴露，UI层监听后做视觉提醒
         *
         * MEDIUM 级别的震动反馈在 VibrationEncoder 内部处理，
         * 引擎层只触发 HIGH 及以上的 TTS + 震动。
         */
        if (event.priority.ordinal >= PerceptionPriority.HIGH.ordinal) {
            // TTS语音播报：直接朗读事件的文字描述
            TTSManager.speakNow(event.description)

            // 震动反馈：根据事件类型编码为不同的震动节奏
            vibrationEncoder?.encodeEvent(event)
        }
    }

    /*
     * ====================================================================
     * 查询方法
     * ====================================================================
     */

    /**
     * 获取最近 N 条感知事件
     *
     * UI层在初始化时或需要刷新展示时调用此方法。
     * 返回的事件列表已按优先级排序（高优先级在前）。
     *
     * @param count 需要获取的事件数量，默认10条。如果历史记录不足10条则返回全部
     * @return 按优先级降序排列的事件列表
     */
    fun getRecentEvents(count: Int = 10): List<PerceptionEvent> {
        return _events.value.take(count)
    }

    /*
     * ====================================================================
     * 清空操作
     * ====================================================================
     */

    /**
     * 清空所有事件历史
     *
     * 在以下场景使用：
     * - 用户主动"清空通知"
     * - 切换到新的场景/页面时重置感知上下文
     * - 测试用例中重置状态
     *
     * 同时清除防抖记录表，确保清空后重新开始计时
     */
    fun clearEvents() {
        _events.value = emptyList()
        lastEmitTime.clear()
    }

    /*
     * ====================================================================
     * UI状态标记
     * ====================================================================
     */

    /**
     * 检查当前是否存在高优先级告警事件
     *
     * UI层通过此方法判断是否需要显示"闪烁"或"高亮"效果。
     * 当存在 HIGH 或 EMERGENCY 事件时返回 true。
     *
     * 典型使用场景：
     * ```
     * // 在Activity中
     * if (FusionPerceptionEngine.getAlertState()) {
     *     flashingOverlay.visibility = View.VISIBLE
     * } else {
     *     flashingOverlay.visibility = View.GONE
     * }
     * ```
     *
     * @return true 表示存在 HIGH 或 EMERGENCY 级别的事件
     */
    fun getAlertState(): Boolean {
        return _events.value.any { it.priority.ordinal >= PerceptionPriority.HIGH.ordinal }
    }
}