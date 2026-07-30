package com.weiguangplus.core

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast

/**
 * 语音助手唤醒器
 *
 * 支持各品牌手机语音助手：
 * - 小爱同学（小米/Redmi）
 * - Google Assistant（原生/Pixel）
 * - Bixby（三星）
 * - Breeno（OPPO/一加）
 * - Celia（华为）
 * - Jovi（vivo/iQOO）
 *
 * 听障用户通过此功能快速唤醒语音助手，用语音控制手机。
 */
object VoiceAssistantLauncher {

    private data class AssistantConfig(
        val brand: String,
        val packageName: String,
        val launchAction: String? = null,
        val launchActivity: String? = null
    )

    private val assistants = listOf(
        AssistantConfig("小爱同学", "com.miui.voiceassist",
            launchActivity = "com.miui.voiceassist.VoiceService"),
        AssistantConfig("Google Assistant", "com.google.android.googlequicksearchbox"),
        AssistantConfig("Bixby", "com.samsung.android.bixby.agent",
            launchAction = "com.samsung.android.bixby.wakeup"),
        AssistantConfig("Breeno", "com.heytap.speechassist"),
        AssistantConfig("Celia", "com.huawei.vassistant",
            launchAction = "com.huawei.vassistant.action.VOICE_SEARCH"),
        AssistantConfig("Jovi", "com.vivo.assist")
    )

    fun launch(context: Context): String? {
        val pm = context.packageManager

        for (assistant in assistants) {
            if (!isInstalled(pm, assistant.packageName)) continue

            // 方式1：专用 Action
            assistant.launchAction?.let { action ->
                try {
                    val i = Intent(action).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    context.startActivity(i)
                    return assistant.brand
                } catch (_: Exception) {}
            }

            // 方式2：直接启动 Activity
            assistant.launchActivity?.let { activity ->
                try {
                    val i = Intent().apply {
                        setClassName(assistant.packageName, activity)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(i)
                    return assistant.brand
                } catch (_: Exception) {}
            }

            // 方式3：包名启动
            try {
                val i = pm.getLaunchIntentForPackage(assistant.packageName)
                if (i != null) {
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(i)
                    return assistant.brand
                }
            } catch (_: Exception) {}
        }

        // 兜底：通用 VOICE_COMMAND
        try {
            val i = Intent(Intent.ACTION_VOICE_COMMAND).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(i)
            return "系统语音助手"
        } catch (_: Exception) {
            Toast.makeText(context, "未找到可用的语音助手", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    fun getAvailable(context: Context): List<String> {
        val pm = context.packageManager
        return assistants.filter { isInstalled(pm, it.packageName) }.map { it.brand }
    }

    fun isAnyAvailable(context: Context): Boolean {
        val pm = context.packageManager
        return assistants.any { isInstalled(pm, it.packageName) }
    }

    private fun isInstalled(pm: PackageManager, pkg: String): Boolean {
        return try {
            pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES); true
        } catch (_: Exception) { false }
    }
}
