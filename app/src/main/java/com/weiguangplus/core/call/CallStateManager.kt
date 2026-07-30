package com.weiguangplus.core.call

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class CallState { IDLE, RINGING, ANSWERED, DIALING, ENDED }

class CallStateManager(private val context: Context) {
    private val _callState = MutableStateFlow(CallState.IDLE)
    val callState: StateFlow<CallState> = _callState

    private val _incomingNumber = MutableStateFlow<String?>(null)
    val incomingNumber: StateFlow<String?> = _incomingNumber

    private val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    private val listener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, number: String?) {
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    _callState.value = CallState.RINGING
                    _incomingNumber.value = number ?: "未知号码"
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    _callState.value = if (_callState.value == CallState.RINGING)
                        CallState.ANSWERED else CallState.DIALING
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    if (_callState.value != CallState.IDLE)
                        _callState.value = CallState.ENDED
                    _incomingNumber.value = null
                }
            }
        }
    }

    fun hasPermission() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_PHONE_STATE
    ) == PackageManager.PERMISSION_GRANTED

    fun register() {
        if (hasPermission())
            tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    fun unregister() {
        tm.listen(listener, PhoneStateListener.LISTEN_NONE)
    }

    fun resetToIdle() {
        _callState.value = CallState.IDLE
        _incomingNumber.value = null
    }
}
