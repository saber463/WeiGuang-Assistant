package com.weiguangchangxing.weiguang_plus.feature.soundmonitor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.weiguangchangxing.weiguang_plus.core.perception.FusionPerceptionEngine
import com.weiguangchangxing.weiguang_plus.core.perception.PerceptionEvent
import com.weiguangchangxing.weiguang_plus.core.perception.PerceptionEventType
import com.weiguangchangxing.weiguang_plus.core.perception.PerceptionPriority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SoundMonitorState(
    val isMonitoring: Boolean = false,
    val isForegroundService: Boolean = false,
    val wakeLockAcquired: Boolean = false,
    val lastVoiceDetected: Long = 0,
    val totalVoiceDetections: Int = 0,
    val currentSoundType: SoundType = SoundType.SILENCE,
    val monitoringDuration: Long = 0,
    val errorMessage: String? = null
)

class SoundMonitoringService : Service() {

    private val binder = SoundMonitorBinder()
    private lateinit var audioCaptureManager: AudioCaptureManager
    private lateinit var voiceClassifier: VoiceDetectionClassifier
    private lateinit var wakeLock: PowerManager.WakeLock

    private val _state = MutableStateFlow(SoundMonitorState())
    val state: StateFlow<SoundMonitorState> = _state.asStateFlow()

    private var monitoringThread: Thread? = null
    private var isMonitoring = false
    private var startTime: Long = 0

    private var eventListener: SoundEventListener? = null
    private var voiceEventDetector = VoiceEventDetector()

    inner class SoundMonitorBinder : Binder() {
        fun getService(): SoundMonitoringService = this@SoundMonitoringService
    }

    override fun onCreate() {
        super.onCreate()
        initializeComponents()
        createNotificationChannel()
        acquireWakeLock()
    }

    private fun initializeComponents() {
        audioCaptureManager = AudioCaptureManager(this)
        voiceClassifier = VoiceDetectionClassifier(this)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "微光畅行:环境音监听服务"
        ).apply {
            setReferenceCounted(false)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "环境音监听服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "持续监听环境声音并在检测到人声时提醒您"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun acquireWakeLock() {
        if (!wakeLock.isHeld) {
            wakeLock.acquire(10 * 60 * 1000L)
            _state.value = _state.value.copy(wakeLockAcquired = true)
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock.isHeld) {
            try {
                wakeLock.release()
                _state.value = _state.value.copy(wakeLockAcquired = false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startMonitoring() {
        if (isMonitoring) {
            return
        }

        val cacheDir = cacheDir
        val tempFile = java.io.File(cacheDir, "temp_audio_${System.currentTimeMillis()}.wav")

        if (!audioCaptureManager.startRecording(tempFile.absolutePath)) {
            _state.value = _state.value.copy(
                errorMessage = "无法启动音频采集"
            )
            return
        }

        isMonitoring = true
        startTime = System.currentTimeMillis()

        startForeground(NOTIFICATION_ID, createNotification("正在监听环境声音..."))

        _state.value = _state.value.copy(
            isMonitoring = true,
            isForegroundService = true,
            errorMessage = null
        )

        startMonitoringThread()
    }

    private fun startMonitoringThread() {
        monitoringThread = Thread {
            val bufferSize = AudioCaptureManager.getRecommendedBufferSize(16000)

            while (isMonitoring) {
                val audioLevel = audioCaptureManager.getCurrentAudioLevel()

                if (audioLevel > -40f) {
                    val recordingStatus = audioCaptureManager.getRecordingStatus()
                    if (recordingStatus == AudioCaptureManager.RecordingStatus.RECORDING) {
                        val audioData = captureAudioBuffer(bufferSize)
                        if (audioData.isNotEmpty()) {
                            val result = voiceClassifier.classifyAudio(
                                audioData,
                                16000
                            )

                            processDetectionResult(result)
                        }
                    }
                }

                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }

        monitoringThread?.start()
    }

    private fun captureAudioBuffer(bufferSize: Int): ShortArray {
        return ShortArray(bufferSize / 2)
    }

    private fun processDetectionResult(result: VoiceDetectionResult) {
        val currentTime = System.currentTimeMillis()

        _state.value = _state.value.copy(
            lastVoiceDetected = if (result.isHumanVoice) currentTime else _state.value.lastVoiceDetected,
            totalVoiceDetections = if (result.isHumanVoice) _state.value.totalVoiceDetections + 1 else _state.value.totalVoiceDetections,
            currentSoundType = result.soundType,
            monitoringDuration = currentTime - startTime
        )

        if (result.isHumanVoice) {
            val event = voiceEventDetector.detectEvent(
                result,
                _state.value.totalVoiceDetections
            )

            event?.let { soundEvent ->
                eventListener?.onVoiceDetected(soundEvent)
                updateNotification("检测到${soundEvent.eventName}")

                FusionPerceptionEngine.emitEvent(
                    PerceptionEvent(
                        type = when (result.soundType.name) {
                            "HUMAN_VOICE" -> PerceptionEventType.VOICE_CALLING
                            else -> PerceptionEventType.ENVIRONMENT_SOUND
                        },
                        priority = PerceptionPriority.MEDIUM,
                        sourceModule = "SoundMonitor",
                        description = soundEvent.description,
                        confidence = result.confidence,
                        extraData = mapOf(
                            "soundType" to result.soundType.name,
                            "eventName" to soundEvent.eventName
                        )
                    )
                )
            }
        }
    }

    fun stopMonitoring() {
        isMonitoring = false

        monitoringThread?.interrupt()
        monitoringThread = null

        audioCaptureManager.stopRecording()

        _state.value = _state.value.copy(
            isMonitoring = false,
            isForegroundService = false,
            monitoringDuration = System.currentTimeMillis() - startTime
        )

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun pauseMonitoring() {
        if (isMonitoring) {
            audioCaptureManager.pauseRecording()
            _state.value = _state.value.copy(
                isMonitoring = false
            )
        }
    }

    fun resumeMonitoring() {
        if (!isMonitoring) {
            audioCaptureManager.resumeRecording()
            _state.value = _state.value.copy(
                isMonitoring = true
            )
        }
    }

    fun setEventListener(listener: SoundEventListener) {
        eventListener = listener
    }

    fun removeEventListener() {
        eventListener = null
    }

    private fun createNotification(content: String): Notification {
        val intent = Intent(this, com.weiguangchangxing.weiguang_plus.MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("微光畅行 - 环境音监听")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(content: String) {
        val notification = createNotification(content)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopMonitoring()
            ACTION_PAUSE -> pauseMonitoring()
            ACTION_RESUME -> resumeMonitoring()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopMonitoring()
        releaseWakeLock()
        audioCaptureManager.release()
        voiceClassifier.release()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "sound_monitoring_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_START = "com.weiguangchangxing.action.START_MONITORING"
        const val ACTION_STOP = "com.weiguangchangxing.action.STOP_MONITORING"
        const val ACTION_PAUSE = "com.weiguangchangxing.action.PAUSE_MONITORING"
        const val ACTION_RESUME = "com.weiguangchangxing.action.RESUME_MONITORING"

        fun startService(context: Context) {
            val intent = Intent(context, SoundMonitoringService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, SoundMonitoringService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}

interface SoundEventListener {
    fun onVoiceDetected(event: SoundEvent)
    fun onMonitoringError(error: String)
}

data class SoundEvent(
    val eventId: String,
    val eventName: String,
    val confidence: Float,
    val timestamp: Long,
    val description: String
)

class VoiceEventDetector {
    private var lastEventTime: Long = 0
    private val minEventInterval = 3000L

    fun detectEvent(result: VoiceDetectionResult, detectionCount: Int): SoundEvent? {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastEventTime < minEventInterval) {
            return null
        }

        if (result.confidence < 0.6f) {
            return null
        }

        lastEventTime = currentTime

        return when {
            detectionCount < 3 -> SoundEvent(
                eventId = "first_voice_${System.currentTimeMillis()}",
                eventName = "首次人声",
                confidence = result.confidence,
                timestamp = currentTime,
                description = "检测到附近有人说话"
            )
            detectionCount % 10 == 0 -> SoundEvent(
                eventId = "repeated_voice_${System.currentTimeMillis()}",
                eventName = "持续人声",
                confidence = result.confidence,
                timestamp = currentTime,
                description = "持续检测到人声活动"
            )
            else -> null
        }
    }

    fun reset() {
        lastEventTime = 0
    }
}
