package com.weiguangchangxing.weiguang_plus.core.assistant

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.speech.RecognizerIntent

data class AssistantInfo(
    val name: String,
    val packageName: String,
    val isInstalled: Boolean,
    val launchIntent: Intent?
)

object VoiceAssistantLauncher {
    private val assistantConfigs = listOf(
        AssistantConfig("小米小爱", "com.miui.voiceassist", "com.miui.voiceassist.MainActivity",
            Intent().apply { action = "android.intent.action.VOICE_ASSIST" }),
        AssistantConfig("华为小艺", "com.huawei.vassistant", "com.huawei.vassistant.ui.VAssistantActivity",
            Intent().apply { setClassName("com.huawei.vassistant", "com.huawei.vassistant.ui.VAssistantActivity") }),
        AssistantConfig("OPPO小布", "com.oppo.voiceassist", "com.oppo.voiceassist.VoiceAssistMainActivity",
            Intent().apply { setClassName("com.oppo.voiceassist", "com.oppo.voiceassist.VoiceAssistMainActivity") }),
        AssistantConfig("vivo小V", "com.vivo.voiceassistant", "com.vivo.voiceassistant.VoiceAssistantActivity",
            Intent().apply { setClassName("com.vivo.voiceassistant", "com.vivo.voiceassistant.VoiceAssistantActivity") }),
        AssistantConfig("荣耀YOYO", "com.hihonor.voiceassistant", "com.hihonor.voiceassistant.service.VoiceAssistService",
            Intent().apply { action = "android.intent.action.VOICE_ASSIST" }),
        AssistantConfig("三星Bixby", "com.samsung.android.bixby.wakeup", "com.samsung.android.app.settings.bixby.BixbySamsungAppsActivity",
            Intent().apply { action = "android.intent.action.VOICE_ASSIST" }),
        AssistantConfig("Google助手", "com.google.android.apps.gsa", "com.google.android.apps.gsa.search.core.google.gaia.LoginActivity",
            Intent().apply { action = "android.intent.action.VOICE_ASSIST" })
    )

    data class AssistantConfig(
        val displayName: String,
        val packageName: String,
        val activityName: String,
        val fallbackIntent: Intent
    )

    fun getInstalledAssistants(context: Context): List<AssistantInfo> {
        val pm = context.packageManager
        return assistantConfigs.map { config ->
            val installed = try {
                pm.getPackageInfo(config.packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }

            val launchIntent = if (installed) {
                val intent = Intent().apply {
                    setClassName(config.packageName, config.activityName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent.resolveActivity(pm) != null) {
                    intent
                } else {
                    null
                }
            } else {
                null
            }

            AssistantInfo(
                name = config.displayName,
                packageName = config.packageName,
                isInstalled = installed,
                launchIntent = launchIntent
            )
        }
    }

    fun launchAssistant(context: Context, assistantInfo: AssistantInfo): Boolean {
        val intent = assistantInfo.launchIntent
        if (intent != null) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (e: Exception) {
                return launchSystemVoiceAssistant(context)
            }
        }
        return launchSystemVoiceAssistant(context)
    }

    fun launchSystemVoiceAssistant(context: Context): Boolean {

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "微光畅行 - 语音助手")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
        }

        try {
            val voiceIntent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(voiceIntent)
            return true
        } catch (e: Exception) {
        }

        try {
            val searchIntent = Intent(Intent.ACTION_SEARCH).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(searchIntent)
            return true
        } catch (e: Exception) {
        }

        return false
    }

    fun launchBestAssistant(context: Context): Boolean {
        val installed = getInstalledAssistants(context)
        val best = installed.firstOrNull { it.isInstalled && it.launchIntent != null }
        if (best != null) {
            return launchAssistant(context, best)
        }
        return launchSystemVoiceAssistant(context)
    }

    fun getAssistantNameByManufacturer(): String {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("xiaomi") -> "小爱"
            manufacturer.contains("huawei") -> "小艺"
            manufacturer.contains("oppo") -> "小布"
            manufacturer.contains("vivo") -> "小V"
            manufacturer.contains("honor") -> "YOYO"
            manufacturer.contains("samsung") -> "Bixby"
            manufacturer.contains("google") -> "Google助手"
            else -> "系统语音助手"
        }
    }
}