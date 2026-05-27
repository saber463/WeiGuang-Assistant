package com.weiguangchangxing.weiguang_plus.core.perception

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * ============================================================================
 * VibrationEncoder - 触觉语言系统
 * ============================================================================
 *
 * 【设计理念：触觉即语言】
 *
 * 对于视障用户而言，视觉信息是缺失的，听觉通道（TTS）是主要的信息获取途径。
 * 但在很多场景下，听觉通道是繁忙甚至不可用的：
 *   - 用户在嘈杂的街道上，TTS 播报被环境音淹没
 *   - 用户在会议/图书馆等需要安静的场合，不便外放语音
 *   - 用户正在与他人交谈，语音播报会造成干扰
 *   - 多事件同时触发时，TTS 队列过长导致信息延迟
 *
 * 触觉语言系统就是为了填补这些空白而设计的。它借鉴了以下理论：
 *
 * 1. 【莫尔斯编码的启示】
 *    莫尔斯码用短音（·）和长音（-）的组合编码字母，人耳可以解码。
 *    我们将同样的思路迁移到触觉通道——用短震和长震的组合来编码事件类型。
 *    人指尖的触觉分辨力足以区分 50ms 和 200ms 的震动差异。
 *
 * 2. 【生态心理学中的"直接知觉"】
 *     Gibson 认为，知觉系统可以直接拾取环境中的"可供性"（affordance）。
 *    触觉语言不追求编码复杂的语义信息，而是让用户通过震动"手感"
 *    直接感知到事件的类别和紧急程度，形成条件反射般的快速响应。
 *
 * 3. 【震动模式的辨识度设计】
 *    每种事件类型的震动模式被设计为具有"节奏指纹"：
 *    - 障碍物类 → 急促短震（模拟"触碰到物体"的感觉）
 *    - 声音类 → 模拟声音本身的节奏（如汽车鸣笛用长-长震）
 *    - 安全类 → 不规则长震（制造紧张感）
 *    - 导航类 → 有规律的引导节奏
 *    - 紧急类 → 持续强震（无法忽视）
 *
 * 【架构定位】
 *
 * VibrationEncoder 位于感知模块的输出层，负责将 FusionPerceptionEngine
 * 处理后的事件最终编码为物理震动信号。它的上游是引擎的决策输出，下游是
 * Android 系统 Vibrator 硬件。
 *
 * 数据流：
 * 传感器/识别模块 → PerceptionEvent → FusionPerceptionEngine →
 * VibrationEncoder.encodeEvent() → Vibrator 硬件
 *
 * 【API 兼容性策略】
 *
 * 项目 minSdk = 23（Android 6.0），震动 API 经历了三次变化：
 * - API 23~25: 使用 Vibrator.vibrate(long[]) 原始震动
 * - API 26~30: 引入 VibrationEffect，支持振幅控制
 * - API 31+:    引入 VibratorManager，废弃 VibratorService 直接获取方式
 *
 * 本类通过 Build.VERSION.SDK_INT 做三路分支，保证在所有目标机型上正常工作。
 * ============================================================================
 */

/**
 * 触觉语言系统的核心类
 *
 * 负责将不同类型的感知事件编码为具有辨识度的震动模式。
 * 每种事件类型对应一个独特的"节奏指纹"，用户通过震动手感即可辨别事件类别。
 *
 * @param context 应用 Context，用于获取系统 Vibrator 服务
 */
class VibrationEncoder(private val context: Context) {

    /**
     * 震动模式映射表
     *
     * 每种 PerceptionEventType 映射到一套独有的震动模式。
     * 设计原则：
     * - 同类事件使用相似的节奏结构（如障碍物均为3段短震）
     * - 不同类事件节奏差异明显（如声音类模仿声音本身的节奏感）
     * - 紧急事件使用长震动+高重复率，确保用户无法忽视
     *
     * 模式命名使用符号表示法：
     *   · = 短震（pulseMs 参数控制具体时长）
     *   - = 长震（通常 pulseMs >= 200ms）
     */
    private val patternMap: Map<PerceptionEventType, VibrationPattern> = mapOf(
        /* ==================================================================
         * 障碍物类 - 急促短震，模拟"触碰物体"的触感
         * 3段式节奏，不同方向通过长短组合区分
         * ================================================================== */

        /** 前方障碍物：··- （短短长）*/
        PerceptionEventType.OBSTACLE_AHEAD to VibrationPattern("··-", 100, 100, 3),

        /** 左侧障碍物：-·· （长短短）*/
        PerceptionEventType.OBSTACLE_LEFT to VibrationPattern("-··", 100, 100, 3),

        /** 右侧障碍物：··-· （短长短长）*/
        PerceptionEventType.OBSTACLE_RIGHT to VibrationPattern("··-·", 100, 100, 4),

        /* ==================================================================
         * 声音类 - 模拟声音本身的节奏特征
         * 让用户通过震动"手感"联想到对应的声音事件
         * ================================================================== */

        /** 汽车鸣笛：-- （两段长震，模拟汽车喇叭的长鸣）*/
        PerceptionEventType.CAR_HORN to VibrationPattern("--", 200, 100, 2),

        /** 门铃声：··· （三段短快震，模拟清脆的门铃）*/
        PerceptionEventType.DOORBELL to VibrationPattern("···", 80, 80, 3),

        /** 警报声：-··· （一长三短，模拟典型警报节奏）*/
        PerceptionEventType.ALARM_SOUND to VibrationPattern("-···", 300, 100, 4),

        /** 电话铃声：·-· （短长短，模拟电话铃声节奏）*/
        PerceptionEventType.PHONE_RINGING to VibrationPattern("·-·", 150, 100, 3),

        /** 婴儿哭声：·-·- （短长短长，模拟哭声的起伏）*/
        PerceptionEventType.BABY_CRYING to VibrationPattern("·-·-", 120, 80, 4),

        /** 火灾报警：---- （四段长震，最高紧急度的声音事件）*/
        PerceptionEventType.FIRE_ALARM to VibrationPattern("----", 400, 100, 4),

        /* ==================================================================
         * 安全类 - 不规则节奏，制造紧张和警觉感
         * 适用于需要用户立即关注但非紧急的场景
         * ================================================================== */

        /** 设备倾斜/可能摔倒：-·-· （长-短-长-短，不规则节奏引起警觉）*/
        PerceptionEventType.DEVICE_TILTED to VibrationPattern("-·-·", 200, 100, 4),

        /** 用户长时间未移动：·--· （短-长-长-短，异常节奏提示异常状态）*/
        PerceptionEventType.USER_STILL to VibrationPattern("·--·", 150, 200, 4),

        /* ==================================================================
         * 导航类 - 有规律的引导节奏
         * 节奏均匀，让用户感受到"方向感"和"引导感"
         * ================================================================== */

        /** 导航指引：·-· （短长短，均匀引导节奏）*/
        PerceptionEventType.NAVIGATION_GUIDANCE to VibrationPattern("·-·", 200, 200, 3),

        /* ==================================================================
         * 通用高优先级类 - 持续强震
         * 这类事件需要用户无法忽视，使用长时强震动
         * ================================================================== */

        /** 系统告警（电量不足、GPS丢失等）：----- （五段持续长震）*/
        PerceptionEventType.SYSTEM_ALERT to VibrationPattern("-----", 500, 50, 5),

        /** 用户主动求助：-·-·- （长-短交替五次，SOS风格的求救信号）*/
        PerceptionEventType.USER_HELP_REQUEST to VibrationPattern("-·-·-", 300, 150, 5),

        /* ==================================================================
         * 出行类 - 公交/地铁/红绿灯
         * ================================================================== */

        /** 公交到站：·-· （短长短，提醒用户注意下车）*/
        PerceptionEventType.BUS_ARRIVAL to VibrationPattern("·-·", 150, 100, 3),

        /** 后方来车：-- （两段长震，模拟车辆靠近的持续感）*/
        PerceptionEventType.VEHICLE_APPROACHING to VibrationPattern("--", 300, 150, 2),

        /** 红绿灯状态：· （单次短震，轻柔提示）*/
        PerceptionEventType.TRAFFIC_LIGHT_STATUS to VibrationPattern("·", 100, 100, 1),

        /* ==================================================================
         * 应急类 - SOS 求救
         * ================================================================== */

        /** SOS 紧急求助：···---··· （三短三长三短 = 莫尔斯SOS）*/
        PerceptionEventType.EMERGENCY_SOS to VibrationPattern("···---···", 150, 100, 9),

        /* ==================================================================
         * 通用类 - 文字转写/设施上报
         * ================================================================== */

        /** 语音转文字：· （单次轻柔短震）*/
        PerceptionEventType.VOICE_TRANSCRIPTION to VibrationPattern("·", 60, 60, 1),

        /** 设施上报：-·- （长-短-长，确认感）*/
        PerceptionEventType.FACILITY_REPORT to VibrationPattern("-·-", 200, 100, 3)
    )

    /**
     * 默认震动模式
     *
     * 当事件类型在 patternMap 中找不到对应模式时使用。
     * 采用最简短的单个短震，避免给用户带来困惑。
     */
    private val defaultPattern = VibrationPattern("·", 200, 100, 1)

    /**
     * 编码并执行一次感知事件的震动反馈
     *
     * 这是本类的主要入口方法。调用方传入一个 PerceptionEvent，
     * 系统根据事件类型查找对应的震动模式，根据事件优先级决定振幅，
     * 然后驱动硬件执行震动。
     *
     * 流程：
     * 1. 根据 event.type 查找 patternMap，获取震动模式
     * 2. 未找到则使用 defaultPattern
     * 3. 获取系统 Vibrator 服务
     * 4. 若设备无震动马达则静默返回
     * 5. 根据优先级映射振幅
     * 6. 执行震动
     *
     * @param event 感知事件对象，包含类型、优先级、描述等信息
     */
    fun encodeEvent(event: PerceptionEvent) {
        val pattern = patternMap[event.type] ?: defaultPattern
        val vibrator = getVibrator()
        if (vibrator == null || !vibrator.hasVibrator()) return

        // 优先级决定震动强度：
        // - EMERGENCY（危急）→ 最大振幅 255，确保用户一定能感知到
        // - HIGH（重要）    → 强振幅 200，足以引起注意但不过度
        // - LOW/MEDIUM      → 中等振幅 150，轻柔提示
        val amplitude = when (event.priority) {
            PerceptionPriority.EMERGENCY -> 255
            PerceptionPriority.HIGH -> 200
            else -> 150
        }

        pattern.play(vibrator, amplitude)
    }

    /**
     * 直接按事件类型播放震动模式
     *
     * 这是一个快捷方法，允许调用方不构造完整的 PerceptionEvent 对象，
     * 直接指定事件类型和振幅来触发震动。
     *
     * 使用场景：
     * - 测试和调试时单独测试某一种震动模式
     * - 不需要携带额外信息的纯震动反馈
     *
     * @param eventType 事件类型，用于查找震动模式
     * @param amplitude 振幅 0~255，默认 150（中等强度）
     */
    fun playPattern(eventType: PerceptionEventType, amplitude: Int = 150) {
        val pattern = patternMap[eventType] ?: defaultPattern
        val vibrator = getVibrator() ?: return
        pattern.play(vibrator, amplitude)
    }

    /**
     * 立即停止所有正在进行的震动
     *
     * 当事件被用户消费、或新的高优先级事件需要抢占时调用。
     * 调用 Vibrator.cancel() 可以立即中断当前的震动波形。
     */
    fun stop() {
        getVibrator()?.cancel()
    }

    /**
     * 获取系统 Vibrator 服务实例
     *
     * 【API 兼容性说明】
     *
     * Android 12（API 31）引入了 VibratorManager，废弃了原来直接通过
     * Context.VIBRATOR_SERVICE 获取 Vibrator 的方式。
     *
     * 三路兼容策略：
     * - API 31+（Android 12+）:
     *   通过 Context.VIBRATOR_MANAGER_SERVICE 获取 VibratorManager，
     *   再调用 defaultVibrator 获取 Vibrator 实例。这是官方推荐方式。
     *
     * - API 23~30（Android 6.0~11）:
     *   通过 Context.VIBRATOR_SERVICE 直接获取 Vibrator 实例。
     *   该方法在 API 31 被标记为弃用，但在低版本上仍然可用。
     *
     * @return Vibrator 实例，若设备不支持则可能返回 null
     */
    private fun getVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // API 31+：使用 VibratorManager 获取默认震动器
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            // API 23~30：使用弃用的直接获取方式
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * 震动模式数据类
     *
     * 定义了一次震动反馈的完整参数：
     * - 节奏模式（由 name 的符号表示）
     * - 单次震动时长（pulseMs）
     * - 震动间隔时长（gapMs）
     * - 重复次数（repeatCount）
     *
     * 每个 VibrationPattern 实例都包含了 play() 方法，
     * 可以将自身编码为系统 VibrationEffect 并驱动硬件震动。
     *
     * @param name        模式名称/符号表示，如 "··-" 表示短-短-长
     * @param pulseMs     每次震动持续时长（毫秒）
     * @param gapMs       两次震动之间的间隔时长（毫秒）
     * @param repeatCount 重复次数
     */
    data class VibrationPattern(
        val name: String,
        val pulseMs: Int,
        val gapMs: Int,
        val repeatCount: Int
    ) {
        /**
         * 将当前模式编码为系统震动信号并执行
         *
         * 【API 兼容性说明】
         *
         * VibrationEffect 在 API 26 才引入，因此需要做版本分支：
         *
         * - API 26+：使用 VibrationEffect.createWaveform(timings, amplitudes, -1)
         *   支持精细的时序和振幅控制，-1 表示不循环
         *
         * - API 23~25：使用 Vibrator.vibrate(long[]) 原始方式
         *   仅支持时序控制，不支持振幅控制（振幅参数被忽略）
         *   该方法在 API 26 被标记为弃用，但在低版本上是唯一选择
         *
         * 时序数组结构：
         * [0, pulse, gap, pulse, gap, ..., pulse]
         * 第一个元素 0 表示起始偏移量，后续每对 (pulse, gap) 构成一次震动周期
         *
         * @param vibrator  系统 Vibrator 实例
         * @param amplitude 目标振幅（0~255），仅在 API 26+ 生效
         */
        fun play(vibrator: Vibrator, amplitude: Int) {
            // 构建时序数组：起始偏移0 + 每周期(pulse + gap) × repeatCount
            val timings = mutableListOf<Long>()
            timings.add(0L) // 起始偏移

            repeat(repeatCount) {
                timings.add(pulseMs.toLong())
                timings.add(gapMs.toLong())
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // API 26+：使用 VibrationEffect，支持振幅控制
                val amplitudes = mutableListOf<Int>()
                amplitudes.add(0) // 起始偏移期间的振幅
                repeat(repeatCount) {
                    amplitudes.add(amplitude)  // 震动期间的目标振幅
                    amplitudes.add(0)           // 间隔期间的振幅（0 = 停止）
                }
                vibrator.vibrate(VibrationEffect.createWaveform(
                    timings.toLongArray(),
                    amplitudes.toIntArray(),
                    -1 // -1 表示不循环，只执行一次
                ))
            } else {
                // API 23~25：使用弃用的 vibrate(long[]) 原始方式
                // 不支持振幅控制，但时序控制仍然有效
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings.toLongArray(), -1)
            }
        }

        override fun toString(): String = name
    }

    /**
     * 获取所有震动模式的说明文字列表
     *
     * 用于在 UI 设置页面或帮助页面中展示当前所有震动模式的映射关系，
     * 方便用户学习和记忆不同模式对应的含义。
     *
     * 返回格式示例：
     * "OBSTACLE_AHEAD: ··- (100ms × 3次)"
     *
     * @return 人类可读的模式说明列表
     */
    fun getPatternDescriptions(): List<String> {
        return patternMap.entries.map { (type, pattern) ->
            "${type.name}: ${pattern.name} (${pattern.pulseMs}ms × ${pattern.repeatCount}次)"
        }
    }
}