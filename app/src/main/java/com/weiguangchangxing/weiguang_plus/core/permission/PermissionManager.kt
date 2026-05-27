package com.weiguangchangxing.weiguang_plus.core.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionManager {
    const val REQUEST_CODE_NOTIFICATION_LISTENER = 1001
    const val REQUEST_CODE_MICROPHONE = 1002
    const val REQUEST_CODE_CAMERA = 1003
    const val REQUEST_CODE_STORAGE = 1004
    const val REQUEST_CODE_OVERLAY = 1005

    fun hasNotificationListenerPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            val enabledListeners = android.provider.Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            enabledListeners?.contains(context.packageName) == true
        } else {
            true
        }
    }

    fun hasMicrophonePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.provider.Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun requestMicrophonePermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_CODE_MICROPHONE
        )
    }

    fun requestCameraPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CODE_CAMERA
        )
    }

    fun requestNotificationListenerPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            val intent = android.content.Intent(
                android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
            )
            activity.startActivity(intent)
        }
    }

    fun requestOverlayPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = android.content.Intent(
                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:${activity.packageName}")
            )
            activity.startActivity(intent)
        }
    }

    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.FOREGROUND_SERVICE_MICROPHONE,
                Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.SYSTEM_ALERT_WINDOW
            )
        } else {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            )
        }
    }

    fun hasAllRequiredPermissions(context: Context): Boolean {
        return hasMicrophonePermission(context) &&
                hasCameraPermission(context) &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || hasOverlayPermission(context))
    }
}
