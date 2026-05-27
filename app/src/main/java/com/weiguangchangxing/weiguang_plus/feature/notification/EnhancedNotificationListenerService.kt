package com.weiguangchangxing.weiguang_plus.feature.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EnhancedNotificationListenerService : NotificationListenerService() {

    private val _interceptedNotifications = MutableStateFlow<List<InterceptedNotification>>(emptyList())
    val interceptedNotifications: StateFlow<List<InterceptedNotification>> = _interceptedNotifications.asStateFlow()

    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    private var listener: NotificationInterceptorListener? = null
    private val notificationInterceptor = NotificationInterceptor()

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        val notification = extractNotificationData(sbn)

        if (notificationInterceptor.shouldIntercept(notification)) {
            handleInterceptedNotification(notification)
            listener?.onNotificationIntercepted(notification)
        } else {
            listener?.onNotificationReceived(notification)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn ?: return
        val notification = extractNotificationData(sbn)
        listener?.onNotificationRemoved(notification)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isServiceConnected.value = true
        listener?.onServiceConnected()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isServiceConnected.value = false
        listener?.onServiceDisconnected()
    }

    @Suppress("DEPRECATION")
    private fun extractNotificationData(sbn: StatusBarNotification): InterceptedNotification {
        val extras = sbn.notification.extras

        return InterceptedNotification(
            id = sbn.key,
            packageName = sbn.packageName,
            appName = getAppName(sbn.packageName),
            title = extras.getCharSequence("android.title")?.toString() ?: "",
            content = extras.getCharSequence("android.text")?.toString() ?: "",
            subText = extras.getCharSequence("android.subText")?.toString() ?: "",
            timestamp = sbn.postTime,
            priority = sbn.notification.priority,
            category = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                sbn.notification.category ?: ""
            } else {
                ""
            },
            isOngoing = sbn.isOngoing,
            isClearable = sbn.isClearable,
            hasVibration = hasVibration(sbn),
            hasSound = hasSound(sbn),
            hasLights = hasLights(sbn),
            channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sbn.notification.channelId
            } else {
                ""
            }
        )
    }

    @Suppress("DEPRECATION")
    private fun hasVibration(sbn: StatusBarNotification): Boolean {
        val notification = sbn.notification
        val hasVibratePattern = notification.vibrate?.isNotEmpty() == true
        if (hasVibratePattern) return true
        return notificationHasVibration(notification)
    }

    private fun notificationHasVibration(notification: android.app.Notification): Boolean {
        return try {
            val field = notification.javaClass.getDeclaredField("vibrate")
            field.isAccessible = true
            val vibrate = field.get(notification) as? LongArray
            vibrate?.isNotEmpty() == true
        } catch (e: Exception) {
            false
        }
    }

    @Suppress("DEPRECATION")
    private fun hasSound(sbn: StatusBarNotification): Boolean {
        return sbn.notification.defaults and android.app.Notification.DEFAULT_SOUND != 0 ||
                sbn.notification.sound != null
    }

    @Suppress("DEPRECATION")
    private fun hasLights(sbn: StatusBarNotification): Boolean {
        return sbn.notification.defaults and android.app.Notification.DEFAULT_LIGHTS != 0 ||
                sbn.notification.flags and android.app.Notification.FLAG_SHOW_LIGHTS != 0
    }

    private fun handleInterceptedNotification(notification: InterceptedNotification) {
        val currentList = _interceptedNotifications.value.toMutableList()
        currentList.add(0, notification)
        if (currentList.size > 50) {
            currentList.removeAt(currentList.lastIndex)
        }
        _interceptedNotifications.value = currentList
    }

    fun setInterceptorListener(listener: NotificationInterceptorListener) {
        this.listener = listener
    }

    fun clearInterceptedNotifications() {
        _interceptedNotifications.value = emptyList()
    }

    fun getRecentNotifications(limit: Int = 20): List<InterceptedNotification> {
        return _interceptedNotifications.value.take(limit)
    }

    private fun getAppName(packageName: String): String {
        return try {
            val packageManager = applicationContext.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    companion object {
        const val TAG = "EnhancedNotificationListener"
    }
}

data class InterceptedNotification(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val content: String,
    val subText: String,
    val timestamp: Long,
    val priority: Int,
    val category: String,
    val isOngoing: Boolean,
    val isClearable: Boolean,
    val hasVibration: Boolean,
    val hasSound: Boolean,
    val hasLights: Boolean,
    val channelId: String
)

interface NotificationInterceptorListener {
    fun onNotificationReceived(notification: InterceptedNotification)
    fun onNotificationIntercepted(notification: InterceptedNotification)
    fun onNotificationRemoved(notification: InterceptedNotification)
    fun onServiceConnected()
    fun onServiceDisconnected()
}

class NotificationInterceptor {
    private val interceptedPackages = mutableSetOf<String>()
    private val keywordFilters = mutableListOf<KeywordFilter>()

    fun addInterceptedPackage(packageName: String) {
        interceptedPackages.add(packageName)
    }

    fun removeInterceptedPackage(packageName: String) {
        interceptedPackages.remove(packageName)
    }

    fun addKeywordFilter(filter: KeywordFilter) {
        keywordFilters.add(filter)
    }

    fun shouldIntercept(notification: InterceptedNotification): Boolean {
        if (notification.packageName in interceptedPackages) {
            return true
        }

        for (filter in keywordFilters) {
            if (filter.matches(notification)) {
                return true
            }
        }

        return false
    }

    fun clearFilters() {
        interceptedPackages.clear()
        keywordFilters.clear()
    }
}

data class KeywordFilter(
    val keywords: List<String>,
    val targetField: TargetField,
    val matchType: MatchType = MatchType.CONTAINS
) {
    fun matches(notification: InterceptedNotification): Boolean {
        val fieldValue = when (targetField) {
            TargetField.TITLE -> notification.title
            TargetField.CONTENT -> notification.content
            TargetField.SUBTEXT -> notification.subText
            TargetField.PACKAGE -> notification.packageName
            TargetField.CATEGORY -> notification.category
        }

        return when (matchType) {
            MatchType.CONTAINS -> keywords.any { fieldValue.contains(it, ignoreCase = true) }
            MatchType.EXACT -> keywords.any { fieldValue.equals(it, ignoreCase = true) }
            MatchType.REGEX -> keywords.any { Regex(it, RegexOption.IGNORE_CASE).containsMatchIn(fieldValue) }
        }
    }
}

enum class TargetField {
    TITLE,
    CONTENT,
    SUBTEXT,
    PACKAGE,
    CATEGORY
}

enum class MatchType {
    CONTAINS,
    EXACT,
    REGEX
}
