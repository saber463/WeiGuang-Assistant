package com.weiguangchangxing.weiguang_plus.core.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.weiguangchangxing.weiguang_plus.core.perception.FusionPerceptionEngine
import com.weiguangchangxing.weiguang_plus.core.perception.PerceptionEvent
import com.weiguangchangxing.weiguang_plus.core.perception.PerceptionEventType
import com.weiguangchangxing.weiguang_plus.core.perception.PerceptionPriority

class EmergencyBroadcastListener : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val description = buildDescription(intent) ?: return
        val priority = if (intent.action == Intent.ACTION_BATTERY_LOW) {
            PerceptionPriority.HIGH
        } else {
            PerceptionPriority.MEDIUM
        }

        FusionPerceptionEngine.emitEvent(
            PerceptionEvent(
                type = PerceptionEventType.SYSTEM_ALERT,
                priority = priority,
                sourceModule = "emergency_broadcast",
                description = description
            )
        )
    }

    private fun buildDescription(intent: Intent): String? {
        return when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> "电源已连接，设备正在充电"
            Intent.ACTION_BATTERY_LOW -> "电池电量过低，请及时充电"
            Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                val isEnabled = intent.getBooleanExtra("state", false)
                if (isEnabled) "飞行模式已开启，网络连接已断开"
                else "飞行模式已关闭，网络连接已恢复"
            }
            else -> null
        }
    }
}