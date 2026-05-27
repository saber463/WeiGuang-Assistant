package com.weiguangchangxing.weiguang_plus.core

import android.os.Build

object DeviceCapabilityChecker {

    enum class ApiLevel(val sdkInt: Int, val androidVersion: String) {
        ANDROID_6(23, "Android 6.0 (Marshmallow)"),
        ANDROID_7(24, "Android 7.0 (Nougat)"),
        ANDROID_7_1(25, "Android 7.1 (Nougat)"),
        ANDROID_8(26, "Android 8.0 (Oreo)"),
        ANDROID_8_1(27, "Android 8.1 (Oreo)"),
        ANDROID_9(28, "Android 9 (Pie)"),
        ANDROID_10(29, "Android 10"),
        ANDROID_11(30, "Android 11"),
        ANDROID_12(31, "Android 12"),
        ANDROID_12L(32, "Android 12L"),
        ANDROID_13(33, "Android 13"),
        ANDROID_14(34, "Android 14")
    }

    enum class HandTrackingLevel {
        ADVANCED,
        FALLBACK,
        NONE
    }

    val currentApiLevel: Int get() = Build.VERSION.SDK_INT

    val androidVersionName: String get() {
        return when (currentApiLevel) {
            23 -> "Android 6.0 (Marshmallow)"
            24 -> "Android 7.0 (Nougat)"
            25 -> "Android 7.1 (Nougat)"
            26 -> "Android 8.0 (Oreo)"
            27 -> "Android 8.1 (Oreo)"
            28 -> "Android 9 (Pie)"
            29 -> "Android 10"
            30 -> "Android 11"
            31 -> "Android 12"
            32 -> "Android 12L"
            33 -> "Android 13"
            34 -> "Android 14"
            else -> "Android ${Build.VERSION.RELEASE}"
        }
    }

    val isAndroid8OrAbove: Boolean get() = currentApiLevel >= 26

    val isAndroid7OrAbove: Boolean get() = currentApiLevel >= 24

    val handTrackingLevel: HandTrackingLevel get() {
        return when {
            currentApiLevel >= 26 -> HandTrackingLevel.ADVANCED
            currentApiLevel >= 24 -> HandTrackingLevel.FALLBACK
            else -> HandTrackingLevel.NONE
        }
    }

    val handTrackingLevelName: String get() {
        return when (handTrackingLevel) {
            HandTrackingLevel.ADVANCED -> "高级模式"
            HandTrackingLevel.FALLBACK -> "备用模式"
            HandTrackingLevel.NONE -> "不支持"
        }
    }

    val handTrackingLevelDescription: String get() {
        return when (handTrackingLevel) {
            HandTrackingLevel.ADVANCED -> "使用 MediaPipe 21点手部关键点追踪，精准识别手语手势"
            HandTrackingLevel.FALLBACK -> "使用摄像头基础运动检测，识别简单手势动作"
            HandTrackingLevel.NONE -> "当前设备不支持摄像头手势识别"
        }
    }

    val isMediaPipeAvailable: Boolean get() = currentApiLevel >= 26

    val isDynamicColorAvailable: Boolean get() = currentApiLevel >= 31

    val isSplitScreenAvailable: Boolean get() = currentApiLevel >= 24

    val isPictureInPictureAvailable: Boolean get() = currentApiLevel >= 26

    val isNotificationListenerAvailable: Boolean get() = currentApiLevel >= 18

    val canUseHapticFeedback: Boolean get() = currentApiLevel >= 11

    val canUseVibratorAmplitude: Boolean get() = currentApiLevel >= 26

    val buildInfo: Map<String, Any> get() = mapOf(
        "apiLevel" to currentApiLevel,
        "androidVersion" to androidVersionName,
        "isAndroid8OrAbove" to isAndroid8OrAbove,
        "handTrackingLevel" to handTrackingLevelName,
        "mediaPipeAvailable" to isMediaPipeAvailable,
        "dynamicColorAvailable" to isDynamicColorAvailable
    )
}