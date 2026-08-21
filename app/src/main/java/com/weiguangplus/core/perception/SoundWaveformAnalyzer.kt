package com.weiguangplus.core.perception

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.Executors
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * 声音波形分析器（G10）
 *
 * 实时采集麦克风音频，把每帧的时域能量转换成一条 RMS 幅度（0~1），
 * 维护一个固定长度的滑动缓冲供 UI 逐条绘制，形成"声音波形可视化"。
 *
 * 设计决策（WHY）：
 * - 采样率 16kHz、单声道、PCM 16bit：与 [AmbientSoundMonitor] 采集口径一致，
 *   复用统一的音频采集参数，兼容绝大多数设备。
 * - 分帧（2048 样本 ≈128ms）取 RMS：RMS 反映该帧整体能量，比瞬时采样更能
 *   表达"音量大小"，绘制成竖条即可直观呈现环境声音强弱变化。
 * - 每帧推入一条缓冲值，缓冲上限 [CAPACITY]，UI 用 Canvas 逐条绘制：
 *   视觉上波形随时间从右向左推进，实时反馈清晰。
 * - RMS 归一化(0~1) 再转 dB：dB = 20·log10(幅值)，用于展示分贝声级，
 *   阈值下限 0.001f 对应 -60dB（静音基准）。
 *
 * 注意：与 [AmbientSoundMonitor] 属独立采集实例，同时运行会共用麦克风；
 * 本分析器定位为"演示/辅助可视化"，不参与该监控的匹配去抖逻辑。
 */
class SoundWaveformAnalyzer(private val context: Context) {

    companion object {
        private const val TAG = "SoundWaveform"
        /** 采样率：16kHz（人声/环境音频带足够） */
        private const val SAMPLE_RATE = 16000
        /** 每帧样本数 = 128ms @16kHz */
        private const val FRAME_SAMPLES = 2048
        /** 波形缓冲长度（条数 ≈ 64 × 128ms ≈ 8 秒的幅度轨迹） */
        private const val CAPACITY = 64
    }

    /** 波形缓冲：RMS 幅度 0~1，最新值在末尾（UI 自右向左绘制） */
    private val _levels = MutableStateFlow(MutableList(CAPACITY) { 0f })
    val levels: StateFlow<List<Float>> = _levels

    /** 当前声级 dB（-60 ~ 0） */
    private val _dbLevel = MutableStateFlow(-60f)
    val dbLevel: StateFlow<Float> = _dbLevel

    /** 是否正在采集 */
    @Volatile
    var isRunning: Boolean = false
        private set

    private var audioRecord: AudioRecord? = null
    private val executor = Executors.newSingleThreadExecutor()

    /** 是否已授予录音权限 */
    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * 开始采集可视化
     * @return 是否成功启动（权限缺失 / 初始化失败返回 false，不崩溃）
     */
    fun start(): Boolean {
        if (isRunning) return true
        if (!hasPermission()) return false

        val minBuffer = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuffer <= 0) return false

        val rec = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBuffer * 2
            )
        } catch (e: Exception) {
            Log.e(TAG, "AudioRecord 创建失败: ${e.message}")
            return false
        }

        if (rec.state != AudioRecord.STATE_INITIALIZED) {
            rec.release()
            Log.e(TAG, "AudioRecord 初始化失败（麦克风可能被占用）")
            return false
        }

        audioRecord = rec
        // 清空缓冲，避免复用旧轨迹
        _levels.value = MutableList(CAPACITY) { 0f }
        _dbLevel.value = -60f
        isRunning = true
        executor.execute(::collectLoop)
        return true
    }

    /** 停止采集 */
    fun stop() {
        if (!isRunning) return
        isRunning = false
        try {
            audioRecord?.stop()
        } catch (_: Exception) {
            // 未开始亦可 stop，忽略
        }
        audioRecord?.release()
        audioRecord = null
    }

    /** 释放所有资源（调用后不可再 start） */
    fun release() {
        stop()
        executor.shutdown()
    }

    /** 采集循环：读帧 → 计算 RMS → 入缓冲 */
    private fun collectLoop() {
        val rec = audioRecord ?: return
        val pcm = ShortArray(FRAME_SAMPLES)

        while (isRunning) {
            val read = try {
                rec.read(pcm, 0, pcm.size)
            } catch (e: Exception) {
                Log.e(TAG, "音频读取失败: ${e.message}")
                break
            }
            if (read <= 0) continue

            // 计算本帧 RMS 幅度（0~1），转换成波形条与分贝
            var sumSquares = 0L
            for (i in 0 until read) {
                val v = pcm[i].toInt()
                sumSquares += v.toLong() * v
            }
            val rms = sqrt(sumSquares / read.toDouble()).toFloat() / 32768f
            val clamped = rms.coerceIn(0f, 1f)

            // 入缓冲（移除最旧一条）
            val next = _levels.value.toMutableList()
            next.removeAt(0)
            next.add(clamped)
            _levels.value = next

            // 分贝 = 20·log10(幅值)，0.001f 对应约 -60dB 静音下限
            _dbLevel.value = (20f * log10(clamped.coerceAtLeast(0.001f))).coerceIn(-60f, 0f)
        }
    }
}