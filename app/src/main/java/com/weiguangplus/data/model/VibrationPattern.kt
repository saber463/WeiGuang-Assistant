package com.weiguangplus.data.model

import android.os.VibrationEffect

/**
 * 振动模式数据模型
 *
 * 定义不同事件类型对应的振动波形。
 * 波形基于 Android `VibrationEffect.createWaveform(timings, amplitudes, repeat)`：
 *  - `timings`    : 每个振动/停顿阶段的时间序列（ms），首元素为初始延迟
 *  - `amplitudes` : 与 timings 对应的振动强度（0~255），0 表示停顿
 *
 * 设计意图（WHY）：
 * 对听障用户而言，振动是静音/睡眠场景下唯一可靠的事件感知通道。
 * 不同事件用「强度 + 频率」差异的波形，让用户仅靠触觉就能区分
 * 紧急程度（火灾警报=强连续，门铃=双轻振等），而非只能察觉"有振动"。
 * 该数据模型是 G6 振动模式反馈的核心，供 VibrationController 消费。
 */
data class VibrationPattern(
    /** 事件类型标识（见 VibrationEvent 枚举） */
    val event: VibrationEvent,
    /** 波形时间序列（ms），首元素为初始延迟 */
    val timings: LongArray,
    /** 与 timings 一一对应的强度（0~255），0 表示该段为停顿 */
    val amplitudes: IntArray,
    /** 振动重复索引，-1 表示不循环 */
    val repeat: Int = -1
) {
    /** 便于 Compose 列表展示的事件名称 */
    val displayName: String
        get() = event.label

    /** LongArray 无法被 data class 默认比较，提供便捷的相等判断供 UI 使用 */
    fun isSame(other: VibrationPattern): Boolean {
        return this.event == other.event &&
            this.timings.contentEquals(other.timings) &&
            this.amplitudes.contentEquals(other.amplitudes) &&
            this.repeat == other.repeat
    }
}

/**
 * 振动事件类型枚举
 *
 * 对应生活/安全场景中的关键声音事件。每个事件都预设了一个档案
 * （见 [VibrationPatterns]），用于区分紧急程度与提醒语义。
 */
enum class VibrationEvent(val label: String) {
    /** 火灾/烟雾警报 —— 最紧急，强连续振动 */
    FIRE_ALARM("火灾警报"),

    /** 门铃 —— 中低频双振 */
    DOORBELL("门铃"),

    /** 电话铃声 —— 三短振 */
    PHONE_RING("电话铃声"),

    /** 婴儿哭声 —— 由轻渐重的长+短交替 */
    BABY_CRY("婴儿哭声"),

    /** 自定义/其它 —— 默认双振，可由用户在 G1 中扩展 */
    CUSTOM("自定义");

    companion object {
        /** 事件档案列表：UI 循环展示与控制器索引共用，保证单一数据源 */
        fun all(): List<VibrationPattern> = VibrationPatterns.ALL
    }
}

/**
 * 各事件的默认振动波形档案
 *
 * 振幅取值参考：
 *  - 255 = 设备最大强度（紧急事件用）
 *  - 100~180 = 中低强度（日常事件用，避免过于打扰）
 *
 * 波形节奏语义：
 *  - 火灾：三段"(255,600) 停(200ms)" 强脉冲，提示持续警觉
 *  - 门铃：两段 200ms 轻振 + 300ms 间隔，短促轻快
 *  - 电话：三段 150ms 短振，周期性提醒
 *  - 婴儿：500ms 中振 → 300ms 强振 → 150ms 停顿交替，模拟"哭声渐强"
 *  - 自定义：默认两段 300ms 中振
 */
object VibrationPatterns {
    val ALL: List<VibrationPattern> = listOf(
        VibrationPattern(
            event = VibrationEvent.FIRE_ALARM,
            timings = longArrayOf(0, 600, 200, 600, 200, 600),
            amplitudes = intArrayOf(255, 0, 255, 0, 255, 0)
        ),
        VibrationPattern(
            event = VibrationEvent.DOORBELL,
            timings = longArrayOf(0, 200, 300, 200),
            amplitudes = intArrayOf(120, 0, 120, 0)
        ),
        VibrationPattern(
            event = VibrationEvent.PHONE_RING,
            timings = longArrayOf(0, 150, 150, 150, 150, 150),
            amplitudes = intArrayOf(160, 0, 160, 0, 160, 0)
        ),
        VibrationPattern(
            event = VibrationEvent.BABY_CRY,
            timings = longArrayOf(0, 500, 150, 300, 150),
            amplitudes = intArrayOf(100, 0, 180, 0, 180, 0)
        ),
        VibrationPattern(
            event = VibrationEvent.CUSTOM,
            timings = longArrayOf(0, 300, 200, 300),
            amplitudes = intArrayOf(180, 0, 180, 0)
        )
    )

    /** 按事件类型快速查找档案，找不到时回退到 CUSTOM */
    fun forEvent(event: VibrationEvent): VibrationPattern {
        return ALL.firstOrNull { it.event == event } ?: ALL.last()
    }
}