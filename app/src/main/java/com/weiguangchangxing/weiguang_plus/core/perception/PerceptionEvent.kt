package com.weiguangchangxing.weiguang_plus.core.perception

/**
 * ============================================================================
 * PerceptionEvent - 感知事件模型
 * ============================================================================
 *
 * 【设计理念】
 * 本文件定义了整个感知模块的核心数据模型。在视障辅助应用中，外部环境信息
 * 必须被转化为结构化的事件对象，才能被上层逻辑消费。PerceptionEvent 就是
 * 这个"万物皆事件"思想的具体体现。
 *
 * 【数据流】
 * 各类传感器/识别模块 → 产生 PerceptionEvent → 注入 FusionPerceptionEngine
 * → 引擎做排序/去重/优先级决策 → UI层展示 + TTS播报 + 震动反馈
 *
 * 【三个核心枚举 + 一个数据类 的设计思路】
 *
 * 1. PerceptionPriority（优先级）
 *    从 LOW 到 EMERGENCY 四级递进。
 *    - LOW: 环境提示性信息（如"前方有垃圾桶"）
 *    - MEDIUM: 一般性提醒（如"识别到门牌号"）
 *    - HIGH: 需要立即关注（如"前方障碍物 2米"）
 *    - EMERGENCY: 危及安全（如"火灾报警"、"检测到摔倒"）
 *    优先级决定了引擎如何处理该事件：是否去重、是否触发TTS/震动、
 *    是否在UI上高亮显示。
 *
 * 2. PerceptionEventType（事件类型）
 *    按感知通道分为四大类：
 *    - 视觉类（OBJECT_DETECTED, OBSTACLE_AHEAD, TEXT_RECOGNIZED 等）
 *      来自摄像头画面分析，包括物体检测、OCR文字识别、行人检测
 *    - 听觉类（CAR_HORN, BABY_CRYING, FIRE_ALARM 等）
 *      来自麦克风声音事件检测，每种声音映射一种危险或场景
 *    - 传感器类（DEVICE_TILTED, USER_STILL 等）
 *      来自陀螺仪/加速度计，用于跌倒检测和异常静止检测
 *    - 综合类（SYSTEM_ALERT, USER_HELP_REQUEST 等）
 *      系统级事件和用户主动发起的求助信号
 *
 * 3. PerceptionEvent（事件数据类）
 *    每个事件携带的信息包括：
 *    - type: 事件类型，用于分类处理和去重
 *    - priority: 优先级，影响处理策略
 *    - sourceModule: 来源模块名，便于追踪和调试
 *    - description: 中文自然语言描述，可直接用于TTS播报
 *    - direction/distance: 空间信息，用于导航和避障场景
 *    - confidence: 置信度，来自AI识别的可靠性指标
 *    - timestamp: 事件产生时间，用于时序排序和防抖
 *    - extraData: 扩展字段，各模块可注入自定义数据
 * ============================================================================
 */

/**
 * 感知事件的优先级枚举
 *
 * 四级优先级的划分依据是"对用户安全和生活的影响程度"：
 *
 * LOW      (0) - 背景信息，用户不需要立即反应
 *                示例："前方20米有一家便利店"
 *                处理方式：存入历史，不触发任何反馈
 *
 * MEDIUM   (1) - 一般提醒，用户可选择性关注
 *                示例："识别到门牌号 301"
 *                处理方式：存入历史，视情况播报
 *
 * HIGH     (2) - 重要事件，需要用户立即知晓
 *                示例："前方2米有障碍物"、"检测到行人靠近"
 *                处理方式：存入历史 + TTS播报 + 震动反馈 + UI闪烁
 *
 * EMERGENCY(3) - 危急事件，需用户立刻采取行动
 *                示例："火灾报警"、"检测到您可能摔倒"
 *                处理方式：存入历史 + 强制TTS播报 + 强震动 + UI闪烁 + 可能自动呼叫求助
 */
enum class PerceptionPriority {
    LOW,
    MEDIUM,
    HIGH,
    EMERGENCY
}

/**
 * 感知事件类型枚举
 *
 * 按照感知通道（视觉/听觉/传感器/综合）分类组织。
 * 每个类型代表一种具体的、可被上层逻辑识别的场景。
 *
 * 设计原则：
 * - 类型粒度要适中：太粗(如只用 SOUND_DETECTED)会丢失语义；
 *   太细(如每个频率一个类型)会导致类型爆炸
 * - 每个类型都对应一种明确的用户场景或危险信号
 * - 新类型可以按需添加，不影响已有逻辑
 */
enum class PerceptionEventType {

    /* ====================================================================
     * 视觉类 - 来自摄像头画面分析
     * ==================================================================== */

    /** 检测到物体（通用），如"前方有椅子""左侧有桌子" */
    OBJECT_DETECTED,

    /** 前方有障碍物，用于导航避障场景 */
    OBSTACLE_AHEAD,

    /** 左侧有障碍物 */
    OBSTACLE_LEFT,

    /** 右侧有障碍物 */
    OBSTACLE_RIGHT,

    /** 通过 OCR 识别到文字，如路牌、门牌、菜单 */
    TEXT_RECOGNIZED,

    /** 检测到行人靠近，用于安全提醒 */
    PERSON_DETECTED,

    /* ====================================================================
     * 听觉类 - 来自麦克风声音事件检测
     * ==================================================================== */

    /** 汽车鸣笛声，过马路时的重要警示 */
    CAR_HORN,

    /** 门铃声，居家场景 */
    DOORBELL,

    /** 婴儿哭声，照护场景 */
    BABY_CRYING,

    /** 警报声（非火灾），如防盗报警 */
    ALARM_SOUND,

    /** 电话铃声 */
    PHONE_RINGING,

    /** 有人在呼喊或说话，可能是在求助或打招呼 */
    VOICE_CALLING,

    /** 流水声，可能提示水龙头未关或管道泄漏 */
    WATER_FLOWING,

    /** 火灾报警器声，最高优先级事件之一 */
    FIRE_ALARM,

    /** 通用环境声音，由具体识别模型填充 description */
    ENVIRONMENT_SOUND,

    /* ====================================================================
     * 传感器类 - 来自陀螺仪/加速度计/运动传感器
     * ==================================================================== */

    /** 设备倾斜角度异常，可能意味着用户摔倒 */
    DEVICE_TILTED,

    /** 设备剧烈震动，可能意味着碰撞或跌落 */
    DEVICE_SHAKE,

    /** 检测到设备附近有运动物体 */
    MOTION_DETECTED,

    /** 用户长时间未移动，可能晕倒或睡着 */
    USER_STILL,

    /* ====================================================================
     * 综合类 - 系统级和用户主动触发
     * ==================================================================== */

    /** 系统级告警，如电量不足、网络断开、GPS信号丢失 */
    SYSTEM_ALERT,

    /** 用户主动触发求助（如长按音量键、喊"救命"、双击屏幕等） */
    USER_HELP_REQUEST,

    /** 导航指引，如直行、左转、到达目的地等 */
    NAVIGATION_GUIDANCE,

    /* ====================================================================
     * 出行类 - 公交地铁/网约车场景
     * ==================================================================== */

    /** 公交车/地铁即将到站 */
    BUS_ARRIVAL,

    /** 后方有车辆接近/鸣笛 */
    VEHICLE_APPROACHING,

    /** 红绿灯状态信息 */
    TRAFFIC_LIGHT_STATUS,

    /* ====================================================================
     * 应急类 - 用户主动求助和安全事件
     * ==================================================================== */

    /** 用户发起 SOS 紧急求助 */
    EMERGENCY_SOS,

    /** 语音转文字结果 */
    VOICE_TRANSCRIPTION,

    /** 无障碍设施反馈上报 */
    FACILITY_REPORT
}

/**
 * 感知事件数据类
 *
 * 这是整个感知模块的核心数据载体。所有外部环境信息最终都被转化为
 * PerceptionEvent 的实例，然后通过 FusionPerceptionEngine 分发给
 * 各个消费方（UI/TTS/震动）。
 *
 * @param type         事件类型，用于分类处理和去重判断
 * @param priority     优先级，默认 MEDIUM，影响处理策略
 * @param sourceModule 来源模块标识，如 "object_detection"/"sound_recognition"
 *                     ，用于日志追踪和调试
 * @param description  中文自然语言描述，可直接用于TTS语音播报，
 *                     如"前方2米有障碍物，请向右绕行"
 * @param direction    事件来源方向，枚举值："left"/"right"/"front"/"back"/null
 *                     用于空间感知场景
 * @param distance     事件源与用户的距离，单位米。用于避障和导航
 * @param confidence   识别置信度，范围 0.0~1.0。
 *                     低置信度事件可被引擎抑制或降级处理
 * @param timestamp    事件产生时间戳，System.currentTimeMillis() 自动填充
 *                     用于事件排序和防抖判断
 * @param extraData    扩展数据Map，各模块可注入额外信息。
 *                     例如声音检测可传入 { "decibel": 85.0, "frequency": 440.0 }
 *                     上层按需解析，不强依赖于固定字段
 */
data class PerceptionEvent(
    val type: PerceptionEventType,
    val priority: PerceptionPriority = PerceptionPriority.MEDIUM,
    val sourceModule: String,
    val description: String,
    val direction: String? = null,
    val distance: Float? = null,
    val confidence: Float = 0.5f,
    val timestamp: Long = System.currentTimeMillis(),
    val extraData: Map<String, Any> = emptyMap()
)