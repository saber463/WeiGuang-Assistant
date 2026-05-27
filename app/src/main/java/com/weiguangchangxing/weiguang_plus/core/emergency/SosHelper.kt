package com.weiguangchangxing.weiguang_plus.core.emergency

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat

object SosHelper {

    private const val SOS_MESSAGE_TEMPLATE = "【微光畅行紧急求助】%s。当前位置：%s。请尽快联系我！"

    @Suppress("UNUSED_PARAMETER")
    fun sendSosAlert(
        context: Context,
        contacts: List<EmergencyContact>,
        situation: String,
        locationDescription: String
    ): Boolean {
        val message = String.format(SOS_MESSAGE_TEMPLATE, situation, locationDescription)
        @Suppress("DEPRECATION")
        val smsManager = SmsManager.getDefault()
        var anySuccess = false

        if (contacts.isEmpty()) {
            return try {
                smsManager.sendTextMessage("110", null, message, null, null)
                true
            } catch (_: Exception) {
                false
            }
        }

        for (contact in contacts) {
            if (contact.phone.isNotBlank()) {
                try {
                    smsManager.sendTextMessage(contact.phone, null, message, null, null)
                    anySuccess = true
                } catch (_: Exception) {
                }
            }
        }

        return anySuccess
    }

    fun getLocationDescription(context: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasFineLocation = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasCoarseLocation = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasFineLocation && !hasCoarseLocation) {
                return "位置信息获取失败"
            }
        }

        return try {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null) {
                    return "纬度:${location.latitude}, 经度:${location.longitude}"
                }
            }
            "位置信息获取失败"
        } catch (_: Exception) {
            "位置信息获取失败"
        }
    }

    fun triggerFlashlight(context: Context, durationMs: Long = 3000L) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasCameraPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasCameraPermission) return
        }

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        var cameraId: String?

        try {
            cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (_: CameraAccessException) {
            return
        }

        if (cameraId == null) return

        Thread {
            try {
                cameraManager.setTorchMode(cameraId, true)
                Thread.sleep(durationMs)
                cameraManager.setTorchMode(cameraId, false)
            } catch (_: Exception) {
                try {
                    cameraManager.setTorchMode(cameraId, false)
                } catch (_: Exception) {
                }
            }
        }.start()
    }
}