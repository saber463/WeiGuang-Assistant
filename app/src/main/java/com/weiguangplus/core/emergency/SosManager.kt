package com.weiguangplus.core.emergency

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.weiguangplus.core.FlashlightController

object SosManager {

    data class SosResult(
        val success: Boolean,
        val smsSent: Int,
        val location: String?,
        val error: String?
    )

    fun trigger(context: Context, onResult: (SosResult) -> Unit) {
        // " 震动反馈 "
        vibrateSos(context)
        // " 语音播报 "
        speakSosWarning(context)

        val contacts = EmergencyContactManager.getContacts()
        if (contacts.isEmpty()) {
            onResult(SosResult(false, 0, null, "未设置紧急联系人，请先在设置中添加"))
            return
        }

        // 1. 闪光灯 SOS 摩斯码
        FlashlightController.sosBlink()

        // 2. 获取 GPS 位置
        getLocation(context) { location ->
            val locStr = if (location != null) {
                "${location.latitude},${location.longitude}"
            } else {
                "位置获取中..."
            }

            // 3. 发送短信
            var sentCount = 0
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                val message = buildMessage(locStr)
                for (contact in contacts) {
                    try {
                        SmsManager.getDefault()
                            .sendTextMessage(contact.phone, null, message, null, null)
                        sentCount++
                    } catch (_: Exception) {}
                }
            }

            onResult(SosResult(
                success = sentCount > 0,
                smsSent = sentCount,
                location = locStr,
                error = when {
                    sentCount == 0 && location == null -> "无法获取位置且无短信权限"
                    sentCount == 0 -> "短信发送失败（可能无权限）"
                    else -> null
                }
            ))
        }
    }

    private fun getLocation(context: Context, callback: (Location?) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            callback(null); return
        }

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = when {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> { callback(null); return }
        }

        try {
            @Suppress("MissingPermission")
            val location = lm.getLastKnownLocation(provider)
            callback(location)
        } catch (_: Exception) { callback(null) }
    }

    private fun buildMessage(location: String): String {
        return """【SOS紧急求助】一位听障人士发出了求救信号！
位置坐标：$location
请尽快联系确认安全！
—— 来自微光同行APP"""
    }

    private fun vibrateSos(context: Context) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            val pattern = longArrayOf(0, 200, 150, 200, 150, 200, 400, 600, 200, 600, 200, 600, 400, 200, 150, 200, 150, 200)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        } catch (_: Exception) {}
    }

    private fun speakSosWarning(context: Context) {
        try {
            com.weiguangplus.core.tts.TtsController.speak("紧急求救已触发，正在发送求救短信")
        } catch (_: Exception) {}
    }
}
