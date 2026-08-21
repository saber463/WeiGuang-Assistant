package com.weiguangplus.core.perception

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.weiguangplus.core.emergency.AutoEmergencyNotifier

/**
 * 环境音监控器（单例）
 *
 * 从 TODO 占位升级为 G1 基础框架的完整实现：
 * 持续采集麦克风音频，对每个音频帧进行实时特征提取与自定义声音匹配，
 * 命中已训练声音后触发多模态提醒（振动 + 闪光灯，经 [SoundAlertManager]）。
 *
 * 核心流程：
 * ```
 * AudioRecord 采集(16kHz) → 分帧(约128ms/帧) 累积窗口 → CustomSoundTrainer.match()
 * → 命中 → SoundAlertManager.onSoundDetected()（带去抖多模态提醒）
 * ```
 *
 * 设计决策（WHY）：
 *  - 采样率 16kHz、单声道、PCM 16bit：对"节奏型声音"识别足够，且兼容绝大多数设备
 *  - 在独立 HandlerThread 上采集，避免阻塞主线程（沿用项目"非主线程做感知"约定）
 *  - 未授予 RECORD_AUDIO 权限时不启动采集，降级为"监控未启用"状态，不崩溃
 *  - 滑动窗口（matchWindow）累计 12 帧再匹配：兼顾实时性与稳定性，
 *    避免单帧瞬时噪声误报；窗口内的连续命中由 SoundAlertManager 去抖收敛
 */
object AmbientSoundMonitor {

    private const val TAG = "AmbientSoundMonitor"

    /** 采样率：16kHz（人声/常见环境音频带足够） */
    private const val SAMPLE_RATE = 16000

    /** 每帧样本数 = 128ms @16kHz */
    private const val FRAME_SAMPLES = 2048

    /** 匹配滑动窗口的帧数（12 帧 ≈ 1.5s 的音频上下文） */
    private const val MATCH_WINDOW_SIZE = 12

    /** 两帧匹配之间的最小时间间隔（防止每帧都跑匹配的 CPU 浪费） */
    private const val MATCH_INTERVAL_MS = 500L

    /** 紧急声音关键词：命中名包含任一即触发自动紧急短信（G9 联动） */
    private val EMERGENCY_KEYWORDS = listOf(
        "火灾", "警报", "烟雾", "警笛", "危险", "sos", "SOS", "救命"
    )

    private var context: Context? = null

    /** 自定义声音训练器（需 context 初始化） */
    private var trainer: CustomSoundTrainer? = null

    private var audioRecord: AudioRecord? = null
    private var recordThread: HandlerThread? = null
    private var recordHandler: Handler? = null
    private var mainHandler: Handler? = null

    /** 是否处于采集状态 */
    @Volatile
    var isMonitoring: Boolean = false
        private set

    /** 是否已授权 RECORD_AUDIO */
    var hasAudioPermission: Boolean = false
        private set

    // 滑动窗缓冲：累积待匹配的 PCM 帧
    private val matchBuffer = ArrayDeque<ShortArray>()

    /**
     * 初始化（在 WeiguangApplication.onCreate 调用）
     * 仅初始化训练器并检查权限，不自动开始采集（交由 UI/服务启动）。
     */
    fun initialize(context: Context) {
        this.context = context.applicationContext
        trainer = CustomSoundTrainer(this.context!!)
        mainHandler = Handler(Looper.getMainLooper())
        hasAudioPermission = ContextCompat.checkSelfPermission(
            this.context!!, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasAudioPermission) {
            Log.w(TAG, "未授予 RECORD_AUDIO 权限，环境音监控将无法启动")
        }
    }

    /** 暴露训练器，供声音设置 UI 添加/测试样本 */
    fun trainerProvider(): CustomSoundTrainer? = trainer

    /** 是否已有训练样本可供匹配 */
    fun hasTrainedSamples(): Boolean = (trainer?.sampleCount() ?: 0) > 0

    /**
     * 开始环境音监控（需已初始化 + 已授权）
     *
     * @return 是否成功启动
     */
    fun start(): Boolean {
        if (isMonitoring) return true
        val ctx = context ?: return false
        if (!hasAudioPermission || !hasAudioPermissionCompat()) return false

        val minBuffer = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuffer <= 0) return false

        // 创建录音实例（捕获可能的初始化失败，例如麦克风被占用）
        val rec = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuffer * 2
            )
        } catch (e: Exception) {
            Log.e(TAG, "AudioRecord 创建失败: ${e.message}")
            return false
        }

        if (rec.state != AudioRecord.STATE_INITIALIZED) {
            rec.release()
            Log.e(TAG, "AudioRecord 初始化失败（可能是麦克风被占用）")
            return false
        }

        audioRecord = rec
        matchBuffer.clear()

        // 独立线程采集音频，主线程回调匹配结果（保持 UI 流畅）
        recordThread = HandlerThread("AmbientSoundMonitor").also { it.start() }
        recordHandler = Handler(recordThread!!.looper)
        recordHandler?.post(::collectLoop)

        isMonitoring = true  // 采集线程启动后标记，确保状态正确
        Log.d(TAG, "环境音监控已启动")
        return true
    }

    /** 停止环境音监控 */
    fun stop() {
        if (!isMonitoring && audioRecord == null) return
        isMonitoring = false
        try {
            audioRecord?.stop()
        } catch (e: Exception) {
            // 未开始亦可 stop，忽略
        }
        audioRecord?.release()
        audioRecord = null
        recordThread?.quitSafely()
        recordThread = null
        recordHandler = null
        Log.d(TAG, "环境音监控已停止")
    }

    /** 采集循环：读取一帧 → 加入滑动窗 → 定时匹配 */
    private fun collectLoop() {
        val rec = audioRecord ?: return
        val pcm = ShortArray(FRAME_SAMPLES)
        var lastMatch = 0L

        while (isMonitoring) {
            val read = try {
                rec.read(pcm, 0, pcm.size)
            } catch (e: Exception) {
                Log.e(TAG, "音频读取失败: ${e.message}")
                break
            }
            if (read <= 0) continue

            // 拷贝实际读取到的样本入窗
            val frame = pcm.copyOf(read)
            matchBuffer.addLast(frame)
            if (matchBuffer.size > MATCH_WINDOW_SIZE) matchBuffer.removeFirst()

            // 有足够上下文且间隔足够时才匹配（节约 CPU）
            val now = System.currentTimeMillis()
            if (matchBuffer.size >= MATCH_WINDOW_SIZE && now - lastMatch >= MATCH_INTERVAL_MS) {
                lastMatch = now
                val windowSamples = concatFrames(matchBuffer)
                val hit = trainer?.match(windowSamples)
                if (hit != null) {
                    // 切回主线程触发提醒，避免在采集线程做 UI 相关回调
                    val hits = hit
                    mainHandler?.post {
                        SoundAlertManager.onSoundDetected(hits)
                        // 紧急声音联动：命中名包含紧急关键词时，自动向紧急联系人发短信（G9）
                        if (isEmergencySoundName(hits)) {
                            val ctx = context
                            if (ctx != null && AutoEmergencyNotifier.canNotifyNow()) {
                                AutoEmergencyNotifier.notifyEmergencyEvent(ctx, hits)
                            }
                        }
                    }
                }
            }
        }
    }

    /** 将多个帧拼接为一个长 PCM 数组供匹配 */
    private fun concatFrames(frames: ArrayDeque<ShortArray>): ShortArray {
        var total = 0
        frames.forEach { total += it.size }
        val out = ShortArray(total)
        var offset = 0
        frames.forEach { f -> f.copyInto(out, offset); offset += f.size }
        return out
    }

    /** 判断命中的声音名是否属于紧急来声音（含紧急关键词即判定） */
    private fun isEmergencySoundName(name: String): Boolean {
        return EMERGENCY_KEYWORDS.any { k -> name.contains(k, ignoreCase = true) }
    }

    /** 兜底检查：重新确认当前仍有 RECORD_AUDIO 权限 */
    private fun hasAudioPermissionCompat(): Boolean {
        val ctx = context ?: return false
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
    }
}