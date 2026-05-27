package com.weiguangchangxing.weiguang_plus.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat

class SpeechToTextService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        startSpeechRecognition()

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "语音转写服务后台运行通知"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("微光畅行语音转写")
            .setContentText("正在实时转写环境语音")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun startSpeechRecognition() {
        if (isListening) return

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            stopSelf()
            return
        }

        isListening = true

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.setRecognitionListener(object : SpeechRecognitionListener() {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    addToHistory(text)
                    val broadcastIntent = Intent(STT_RESULT_ACTION).apply {
                        putExtra(STT_RESULT_EXTRA, text)
                    }
                    sendBroadcast(broadcastIntent)
                }
                restartRecognition()
            }

            override fun onError(error: Int) {
                isListening = false
                mainHandler.postDelayed({ restartRecognition() }, 2000)
            }
        })

        speechRecognizer?.startListening(intent)
    }

    private fun restartRecognition() {
        isListening = false
        speechRecognizer?.destroy()
        mainHandler.postDelayed({ startSpeechRecognition() }, 100)
    }

    override fun onDestroy() {
        isListening = false
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        mainHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private abstract class SpeechRecognitionListener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    companion object {
        const val CHANNEL_ID = "stt_channel"
        const val CHANNEL_NAME = "语音转写"
        const val NOTIFICATION_ID = 1003
        const val STT_RESULT_ACTION = "com.weiguangchangxing.STT_RESULT"
        const val STT_RESULT_EXTRA = "text"

        private val transcriptHistory = mutableListOf<String>()

        private fun addToHistory(text: String) {
            transcriptHistory.add(text)
            if (transcriptHistory.size > 20) {
                transcriptHistory.removeAt(0)
            }
        }

        @JvmStatic
        fun getTranscriptHistory(): List<String> = transcriptHistory.toList()
    }
}