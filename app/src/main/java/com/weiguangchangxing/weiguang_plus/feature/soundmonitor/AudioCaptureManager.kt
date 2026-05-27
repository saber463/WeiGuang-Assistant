package com.weiguangchangxing.weiguang_plus.feature.soundmonitor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

data class AudioCaptureState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val sampleRate: Int = 16000,
    val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT,
    val bufferSize: Int = 0,
    val recordedBytes: Long = 0,
    val recordingDuration: Long = 0,
    val audioLevel: Float = 0f,
    val errorMessage: String? = null
)

class AudioCaptureManager(private val context: Context) {

    private val _state = MutableStateFlow(AudioCaptureState())
    val state: StateFlow<AudioCaptureState> = _state.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording = false
    private var isPaused = false

    private val bufferSize: Int by lazy {
        AudioRecord.getMinBufferSize(
            _state.value.sampleRate,
            _state.value.channelConfig,
            _state.value.audioFormat
        ).let {
            if (it > 0) it * 2 else 4096
        }
    }

    private var outputFile: File? = null
    private var fileOutputStream: FileOutputStream? = null
    private var totalBytesWritten: Long = 0
    private var recordingStartTime: Long = 0

    fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startRecording(outputPath: String): Boolean {
        if (!hasRecordPermission()) {
            _state.value = _state.value.copy(
                errorMessage = "缺少麦克风权限"
            )
            return false
        }

        if (isRecording) {
            return false
        }

        try {
            outputFile = File(outputPath)
            fileOutputStream = FileOutputStream(outputFile)

            writeWavHeader(fileOutputStream!!, 0)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                _state.value.sampleRate,
                _state.value.channelConfig,
                _state.value.audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                _state.value = _state.value.copy(
                    errorMessage = "AudioRecord初始化失败"
                )
                return false
            }

            audioRecord?.startRecording()
            isRecording = true
            isPaused = false
            recordingStartTime = System.currentTimeMillis()
            totalBytesWritten = 0

            _state.value = _state.value.copy(
                isRecording = true,
                isPaused = false,
                bufferSize = bufferSize,
                recordedBytes = 0,
                recordingDuration = 0,
                errorMessage = null
            )

            startRecordingThread()

            return true
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                errorMessage = "启动录音失败: ${e.message}"
            )
            e.printStackTrace()
            return false
        }
    }

    private fun startRecordingThread() {
        recordingThread = Thread {
            val buffer = ShortArray(bufferSize / 2)
            val byteBuffer = ByteArray(bufferSize)

            while (isRecording) {
                if (!isPaused) {
                    val readCount = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                    if (readCount > 0) {
                        for (i in 0 until readCount) {
                            byteBuffer[i * 2] = (buffer[i].toInt() and 0xFF).toByte()
                            byteBuffer[i * 2 + 1] = (buffer[i].toInt() shr 8 and 0xFF).toByte()
                        }

                        fileOutputStream?.write(byteBuffer, 0, readCount * 2)
                        totalBytesWritten += readCount * 2

                        val audioLevel = calculateAudioLevel(buffer, readCount)
                        val duration = System.currentTimeMillis() - recordingStartTime

                        _state.value = _state.value.copy(
                            recordedBytes = totalBytesWritten,
                            recordingDuration = duration,
                            audioLevel = audioLevel
                        )
                    }
                } else {
                    Thread.sleep(50)
                }
            }
        }

        recordingThread?.start()
    }

    private fun calculateAudioLevel(buffer: ShortArray, readCount: Int): Float {
        var sum = 0L
        for (i in 0 until readCount) {
            sum += buffer[i] * buffer[i]
        }
        val rms = kotlin.math.sqrt(sum.toDouble() / readCount)
        return (20 * kotlin.math.log10(rms / Short.MAX_VALUE)).toFloat().coerceIn(-60f, 0f)
    }

    fun pauseRecording() {
        isPaused = true
        _state.value = _state.value.copy(isPaused = true)
    }

    fun resumeRecording() {
        isPaused = false
        _state.value = _state.value.copy(isPaused = false)
    }

    fun stopRecording(): String? {
        if (!isRecording) {
            return null
        }

        isRecording = false
        isPaused = false

        try {
            recordingThread?.join(1000)
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            fileOutputStream?.close()

            updateWavHeader(outputFile!!, totalBytesWritten)

            _state.value = _state.value.copy(
                isRecording = false,
                isPaused = false,
                recordedBytes = totalBytesWritten,
                recordingDuration = System.currentTimeMillis() - recordingStartTime
            )

            return outputFile?.absolutePath
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                errorMessage = "停止录音失败: ${e.message}"
            )
            e.printStackTrace()
            return null
        }
    }

    private fun writeWavHeader(outputStream: FileOutputStream, audioLength: Long) {
        val totalDataLen = audioLength + 36
        val byteRate = _state.value.sampleRate * 1 * 16 / 8

        val header = ByteArray(44)

        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()

        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()

        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0

        header[20] = 1
        header[21] = 0

        header[22] = 1
        header[23] = 0

        header[24] = (_state.value.sampleRate and 0xff).toByte()
        header[25] = (_state.value.sampleRate shr 8 and 0xff).toByte()
        header[26] = (_state.value.sampleRate shr 16 and 0xff).toByte()
        header[27] = (_state.value.sampleRate shr 24 and 0xff).toByte()

        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()

        header[32] = (2 * 16 / 8).toByte()
        header[33] = 0

        header[34] = 16
        header[35] = 0

        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()

        header[40] = (audioLength and 0xff).toByte()
        header[41] = (audioLength shr 8 and 0xff).toByte()
        header[42] = (audioLength shr 16 and 0xff).toByte()
        header[43] = (audioLength shr 24 and 0xff).toByte()

        outputStream.write(header)
    }

    private fun updateWavHeader(file: File, audioLength: Long) {
        val raf = RandomAccessFile(file, "rw")

        val totalDataLen = audioLength + 36
        raf.seek(4)
        raf.write((totalDataLen and 0xff).toInt())
        raf.write((totalDataLen shr 8 and 0xff).toInt())
        raf.write((totalDataLen shr 16 and 0xff).toInt())
        raf.write((totalDataLen shr 24 and 0xff).toInt())

        raf.seek(40)
        raf.write((audioLength and 0xff).toInt())
        raf.write((audioLength shr 8 and 0xff).toInt())
        raf.write((audioLength shr 16 and 0xff).toInt())
        raf.write((audioLength shr 24 and 0xff).toInt())

        raf.close()
    }

    fun setSampleRate(sampleRate: Int) {
        if (!isRecording) {
            _state.value = _state.value.copy(sampleRate = sampleRate)
        }
    }

    fun release() {
        stopRecording()
        _state.value = AudioCaptureState()
    }

    fun getCurrentAudioLevel(): Float {
        return _state.value.audioLevel
    }

    fun isCurrentlyRecording(): Boolean {
        return isRecording
    }

    fun getRecordingStatus(): RecordingStatus {
        return when {
            _state.value.errorMessage != null -> RecordingStatus.ERROR
            isRecording && !isPaused -> RecordingStatus.RECORDING
            isRecording && isPaused -> RecordingStatus.PAUSED
            else -> RecordingStatus.IDLE
        }
    }

    enum class RecordingStatus {
        IDLE,
        RECORDING,
        PAUSED,
        ERROR
    }

    companion object {
        const val DEFAULT_SAMPLE_RATE = 16000
        const val DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

        fun getRecommendedBufferSize(sampleRate: Int): Int {
            return AudioRecord.getMinBufferSize(
                sampleRate,
                DEFAULT_CHANNEL_CONFIG,
                DEFAULT_AUDIO_FORMAT
            ).let {
                if (it > 0) it * 2 else 4096
            }
        }
    }
}
