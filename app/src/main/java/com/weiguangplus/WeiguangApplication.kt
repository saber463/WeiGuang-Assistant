package com.weiguangplus

import android.app.Application
import com.weiguangplus.core.FlashlightController
import com.weiguangplus.core.call.CallStateManager
import com.weiguangplus.core.perception.AmbientSoundMonitor
import com.weiguangplus.core.tts.TtsController
import com.weiguangplus.core.emergency.EmergencyContactManager
import com.weiguangplus.core.emergency.SosHistoryManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WeiguangApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        TtsController.initialize(this)
        AmbientSoundMonitor.initialize(this)
        FlashlightController.init(this)
        EmergencyContactManager.init(this)
        SosHistoryManager.init(this)
        CallStateManager(this).register()
    }

    override fun onTerminate() {
        super.onTerminate()
        TtsController.shutdown()
    }
}
