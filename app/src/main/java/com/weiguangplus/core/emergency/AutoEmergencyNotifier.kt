package com.weiguangplus.core.emergency

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.weiguangplus.core.FlashlightController

/**
 * 自动紧急通知协调器（object 单例）
 *
 * 职责（G9）：当 G1 声音事件分类检测到紧急声音事件（火灾/烟雾警报等）时，
 * 自动向预设紧急联系人发送含「事件类型 + GPS 位置」的告警短信，并闪光警示。
 *
 * 与 [SosManager] 的区别（关键设计决策，WHY）：
 *  - [SosManager.trigger] 是**用户主动求救**的全套流程（震动 + TTS 播报 +
 *    SOS 摩斯码 + 短信），警示强度高。
 *  - 本协调器是**系统自动检测告警**，设计为"静默发短信"为主，仅辅以闪光警示，
 *    避免在用户并未主动求救时产生过度惊扰。因此不调用 SosManager.trigger，
 *    而是独立复用联系人数据 + GPS + SmsManager 基础能力。
 *
 * 防误触设计：
 *  - [notifyEmergencyEvent] 内部做**时间级去抖**（两次自动通知间隔 ≥60s），
 *    防止同一波告警在短时间内重复轰炸联系人。
 *  - 连续帧确认由触发方（G1）负责，本协调器接收的已是确认后的事件。
 */
object AutoEmergencyNotifier {

    private const val TAG = "AutoEmergencyNotifier"

    /** 两次自动通知之间的最小间隔（ms），防重复轰炸 */
    private const val COOLDOWN_MS = 60_000L

    /** 上次自动通知的时间戳 */
    private var lastNotifyAt: Long = 0L

    /**
     * 是否已过冷却窗口（供 G1 判断是否值得触发自动通知）
     */
    fun canNotifyNow(): Boolean {
        return System.currentTimeMillis() - lastNotifyAt >= COOLDOWN_MS
    }

    /**
     * 触发一次紧急事件自动通知（带去抖）
     *
     * @param context 上下文（用于访问系统服务与权限检查）
     * @param eventType 紧急事件类型描述，如"火灾警报""烟雾警报"
     * @param onResult 结果回调（success=是否成功发送了短信；smsSent=成功条数）
     * @return 是否实际执行了通知（false 表示处于冷却窗口或未配置联系人）
     */
    fun notifyEmergencyEvent(
        context: Context,
        eventType: String,
        onResult: ((success: Boolean, smsSent: Int) -> Unit)? = null
    ): Boolean {
        if (!canNotifyNow()) {
            Log.d(TAG, "自动通知处于冷却窗口内，忽略 [$eventType]")
            return false
        }

        val contacts = EmergencyContactManager.getContacts()
        if (contacts.isEmpty()) {
            Log.w(TAG, "未配置紧急联系人，无法自动发送紧急短信")
            return false
        }

        lastNotifyAt = System.currentTimeMillis()

        // 视觉警示：闪光灯闪烁（复用现有能力，避免强打扰所以不用 sosBlink 摩斯码）
        if (FlashlightController.isFlashAvailable()) {
            FlashlightController.blink(3, 300, 200)
        }

        // 获取 GPS 位置后发送短信
        getLocation(context) { location ->
            val locStr = if (location != null) {
                "${location.latitude},${location.longitude}"
            } else {
                "位置获取中..."
            }
            val sent = sendSms(context, eventType, locStr, contacts)
            Log.d(TAG, "自动紧急通知完成: type=$eventType, 发送=$sent 条")
            onResult?.invoke(sent > 0, sent)
        }
        return true
    }

    /**
     * 向全部联系人发送紧急告警短信
     *
     * @param eventType 事件类型
     * @param location  位置字符串
     * @param contacts  紧急联系人列表
     * @return 成功发送的短信条数
     */
    private fun sendSms(
        context: Context,
        eventType: String,
        location: String,
        contacts: List<EmergencyContactManager.Contact>
    ): Int {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "无 SEND_SMS 权限，短信未发送")
            return 0
        }

        val message = buildMessage(eventType, location)
        var sent = 0
        val sms = SmsManager.getDefault()
        for (contact in contacts) {
            try {
                sms.sendTextMessage(contact.phone, null, message, null, null)
                sent++
            } catch (e: Exception) {
                Log.e(TAG, "短信发送失败到 ${contact.phone}: ${e.message}")
            }
        }
        return sent
    }

    /** 构造紧急事件短信内容（在 SOS 模板基础上加入事件类型） */
    private fun buildMessage(eventType: String, location: String): String {
        return """【紧急声音警报】检测到${eventType}！
位置坐标：$location
请尽快联系确认安全！
—— 来自微光同行APP"""
    }

    /** 获取最近已知位置（复用 SosManager 相同逻辑，非阻塞回调） */
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
            callback(lm.getLastKnownLocation(provider))
        } catch (_: Exception) {
            callback(null)
        }
    }

    /** 重置冷却窗口（训练新样本/测试时调用） */
    fun resetCooldown() {
        lastNotifyAt = 0L
    }
}