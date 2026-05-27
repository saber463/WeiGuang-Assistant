package com.weiguangchangxing.weiguang_plus.app

import android.app.Application
import com.weiguangchangxing.weiguang_plus.core.tts.TTSManager

class WeiguangPlusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TTSManager.initialize(this)
    }

    override fun onTerminate() {
        TTSManager.shutdown()
        super.onTerminate()
    }
}