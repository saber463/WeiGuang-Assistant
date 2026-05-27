package com.weiguangchangxing.weiguang_plus.feature.notification

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VolumeState(
    val currentVolume: Int = 0,
    val maxVolume: Int = 0,
    val volumeType: Int = AudioManager.STREAM_NOTIFICATION,
    val isMuted: Boolean = true,
    val isDoNotDisturbEnabled: Boolean = false,
    val isRingerModeNormal: Boolean = false
)

class AudioManagerController(private val context: Context) {

    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private val notificationManager: NotificationManager? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        } else {
            null
        }
    }

    private val _volumeState = MutableStateFlow(VolumeState())
    val volumeState: StateFlow<VolumeState> = _volumeState.asStateFlow()

    private var originalVolume: Int = 0
    private var isVolumeModified: Boolean = false

    fun getCurrentVolume(streamType: Int = AudioManager.STREAM_NOTIFICATION): Int {
        return audioManager.getStreamVolume(streamType)
    }

    fun getMaxVolume(streamType: Int = AudioManager.STREAM_NOTIFICATION): Int {
        return audioManager.getStreamMaxVolume(streamType)
    }

    fun setMaxVolume(streamType: Int = AudioManager.STREAM_NOTIFICATION): Boolean {
        return try {
            val maxVolume = audioManager.getStreamMaxVolume(streamType)
            originalVolume = audioManager.getStreamVolume(streamType)
            isVolumeModified = originalVolume != maxVolume

            audioManager.setStreamVolume(
                streamType,
                maxVolume,
                AudioManager.FLAG_SHOW_UI
            )

            updateVolumeState(streamType)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun restoreOriginalVolume(streamType: Int = AudioManager.STREAM_NOTIFICATION) {
        if (isVolumeModified && originalVolume > 0) {
            try {
                audioManager.setStreamVolume(
                    streamType,
                    originalVolume,
                    0
                )
                isVolumeModified = false
                updateVolumeState(streamType)
            } catch (e: Exception) {
            }
        }
    }

    fun increaseVolume(streamType: Int = AudioManager.STREAM_NOTIFICATION, step: Int = 1): Boolean {
        return try {
            val currentVolume = audioManager.getStreamVolume(streamType)
            val maxVolume = audioManager.getStreamMaxVolume(streamType)
            val newVolume = (currentVolume + step).coerceAtMost(maxVolume)

            audioManager.setStreamVolume(streamType, newVolume, AudioManager.FLAG_PLAY_SOUND)
            updateVolumeState(streamType)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun decreaseVolume(streamType: Int = AudioManager.STREAM_NOTIFICATION, step: Int = 1): Boolean {
        return try {
            val currentVolume = audioManager.getStreamVolume(streamType)
            val newVolume = (currentVolume - step).coerceAtLeast(0)

            audioManager.setStreamVolume(streamType, newVolume, 0)
            updateVolumeState(streamType)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setVolume(volume: Int, streamType: Int = AudioManager.STREAM_NOTIFICATION): Boolean {
        return try {
            val maxVolume = audioManager.getStreamMaxVolume(streamType)
            val safeVolume = volume.coerceIn(0, maxVolume)

            audioManager.setStreamVolume(streamType, safeVolume, 0)
            updateVolumeState(streamType)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isMuted(streamType: Int = AudioManager.STREAM_NOTIFICATION): Boolean {
        return audioManager.getStreamVolume(streamType) == 0
    }

    fun mute(streamType: Int = AudioManager.STREAM_NOTIFICATION) {
        audioManager.setStreamVolume(streamType, 0, 0)
        updateVolumeState(streamType)
    }

    fun unmute(streamType: Int = AudioManager.STREAM_NOTIFICATION) {
        val maxVolume = audioManager.getStreamMaxVolume(streamType)
        audioManager.setStreamVolume(streamType, maxVolume, 0)
        updateVolumeState(streamType)
    }

    fun getRingerMode(): Int {
        return audioManager.ringerMode
    }

    fun setRingerMode(mode: Int): Boolean {
        return try {
            audioManager.ringerMode = mode
            updateVolumeState(AudioManager.STREAM_NOTIFICATION)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setNormalRingerMode(): Boolean {
        return setRingerMode(AudioManager.RINGER_MODE_NORMAL)
    }

    fun disableDoNotDisturb(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val nm = notificationManager ?: return false
                if (nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
                    return true
                }
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                updateVolumeState(AudioManager.STREAM_NOTIFICATION)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } else {
            setRingerMode(AudioManager.RINGER_MODE_NORMAL)
        }
    }

    fun enableDoNotDisturb(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val nm = notificationManager ?: return false
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                updateVolumeState(AudioManager.STREAM_NOTIFICATION)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } else {
            setRingerMode(AudioManager.RINGER_MODE_SILENT)
        }
    }

    fun requestAudioFocus(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { }
                .build()
            val result = audioManager.requestAudioFocus(focusRequest)
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            val result = audioManager.requestAudioFocus(
                { },
                AudioManager.STREAM_NOTIFICATION,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setOnAudioFocusChangeListener { }
                .build()
            audioManager.abandonAudioFocusRequest(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus { }
        }
    }

    private fun updateVolumeState(streamType: Int) {
        val currentVolume = audioManager.getStreamVolume(streamType)
        val maxVolume = audioManager.getStreamMaxVolume(streamType)
        val ringerMode = audioManager.ringerMode

        _volumeState.value = VolumeState(
            currentVolume = currentVolume,
            maxVolume = maxVolume,
            volumeType = streamType,
            isMuted = currentVolume == 0,
            isDoNotDisturbEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val nm = notificationManager
                    if (nm != null) {
                        nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    false
                }
            } else {
                ringerMode == AudioManager.RINGER_MODE_SILENT
            },
            isRingerModeNormal = ringerMode == AudioManager.RINGER_MODE_NORMAL
        )
    }

    fun getAllStreamVolumes(): Map<Int, Pair<Int, Int>> {
        val streamTypes = listOf(
            AudioManager.STREAM_VOICE_CALL,
            AudioManager.STREAM_SYSTEM,
            AudioManager.STREAM_RING,
            AudioManager.STREAM_MUSIC,
            AudioManager.STREAM_ALARM,
            AudioManager.STREAM_NOTIFICATION
        )

        return streamTypes.associateWith { streamType ->
            Pair(
                audioManager.getStreamVolume(streamType),
                audioManager.getStreamMaxVolume(streamType)
            )
        }
    }

    fun maximizeAllCriticalVolumes(): Boolean {
        var allSuccess = true

        allSuccess = setMaxVolume(AudioManager.STREAM_NOTIFICATION) && allSuccess
        allSuccess = setMaxVolume(AudioManager.STREAM_ALARM) && allSuccess
        allSuccess = setMaxVolume(AudioManager.STREAM_RING) && allSuccess
        allSuccess = setMaxVolume(AudioManager.STREAM_MUSIC) && allSuccess

        if (!setNormalRingerMode()) {
            allSuccess = false
        }

        if (!disableDoNotDisturb()) {
            allSuccess = false
        }

        return allSuccess
    }

    fun restoreAllVolumes() {
        restoreOriginalVolume(AudioManager.STREAM_NOTIFICATION)
        restoreOriginalVolume(AudioManager.STREAM_ALARM)
        restoreOriginalVolume(AudioManager.STREAM_RING)
        restoreOriginalVolume(AudioManager.STREAM_MUSIC)
    }
}
