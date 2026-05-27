package com.weiguangchangxing.weiguang_plus.feature.signlanguage

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.weiguangchangxing.weiguang_plus.core.animation.HandGestureView

class LottieAnimationPlayer {
    private var containerView: FrameLayout? = null
    private var handView: HandGestureView? = null
    private var labelView: TextView? = null
    private var currentText: String = ""
    private var isPlaying = false

    private val pathToGesture: Map<String, String> = mapOf(
        "water" to "打开手掌", "drink" to "打开手掌", "thirsty" to "打开手掌",
        "eat" to "打开手掌", "hungry" to "打开手掌", "food" to "打开手掌",
        "help" to "打开手掌", "save" to "打开手掌",
        "thank" to "竖起大拇指", "thanks" to "竖起大拇指",
        "hello" to "挥手", "hi" to "挥手", "goodbye" to "挥手", "bye" to "挥手",
        "sorry" to "握拳",
        "pain" to "握拳", "ache" to "握拳", "hurt" to "握拳", "sick" to "握拳",
        "medicine" to "数字1", "drug" to "数字1", "pill" to "数字1",
        "sleep" to "数字4", "tired" to "数字4", "rest" to "数字4",
        "call" to "数字6", "phone" to "数字6",
        "yes" to "OK手势", "ok" to "OK手势", "okay" to "OK手势", "fine" to "OK手势",
        "love" to "比心", "heart" to "比心", "like" to "比心",
        "peace" to "V字", "victory" to "V字", "two" to "V字",
        "wait" to "数字3", "stop" to "数字3",
        "no" to "食指指", "not" to "食指指",
        "me" to "食指指", "i" to "食指指",
        "you" to "食指指",
        "good" to "竖起大拇指", "great" to "竖起大拇指", "nice" to "竖起大拇指",
        "friend" to "竖起大拇指",
        "school" to "数字3", "class" to "数字3",
        "hospital" to "数字2", "doctor" to "数字2",
        "home" to "打开手掌", "house" to "打开手掌",
        "go" to "打开手掌", "come" to "打开手掌",
        "happy" to "挥手", "smile" to "挥手",
        "sad" to "握拳", "cry" to "握拳",
        "angry" to "握拳", "mad" to "握拳",
        "hot" to "挥手",
        "cold" to "握拳",
        "big" to "数字5", "small" to "数字1",
        "one" to "数字1", "two" to "V字", "three" to "数字3",
        "four" to "数字4", "five" to "数字5",
        "six" to "数字6", "seven" to "数字7",
        "eight" to "数字8", "nine" to "数字9"
    )

    fun createAnimationView(context: Context): View {
        containerView = FrameLayout(context)
        handView = HandGestureView(context)
        labelView = TextView(context).apply {
            textSize = 28f
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setPadding(0, 20, 0, 0)
        }
        labelView?.text = "等待手语展示..."
        containerView?.addView(handView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        containerView?.addView(labelView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL
        })
        return containerView!!
    }

    fun playAnimation(animationPath: String) {
        if (animationPath.isEmpty()) return
        currentText = animationPath
        isPlaying = true
        val gestureName = resolveGesture(animationPath)
        handView?.setGesture(gestureName, true)
        labelView?.text = extractDisplayName(animationPath)
    }

    fun playLoopAnimation(animationPath: String) {
        playAnimation(animationPath)
    }

    fun stopAnimation() {
        isPlaying = false
        handView?.stopAnimation()
        labelView?.text = "已停止"
    }

    fun pauseAnimation() {
        isPlaying = false
        labelView?.text = "已暂停"
    }

    fun resumeAnimation() {
        if (currentText.isNotEmpty()) {
            isPlaying = true
            val gestureName = resolveGesture(currentText)
            handView?.setGesture(gestureName, true)
            labelView?.text = extractDisplayName(currentText)
        }
    }

    fun release() {
        stopAnimation()
        handView = null
        labelView = null
        containerView = null
        currentText = ""
    }

    fun isAnimationPlaying(): Boolean = isPlaying
    fun getCurrentAnimationPath(): String = currentText

    private fun resolveGesture(path: String): String {
        val lower = path.lowercase()
        return pathToGesture.entries
            .firstOrNull { (key, _) -> lower.contains(key) }
            ?.value ?: "打开手掌"
    }

    private fun extractDisplayName(path: String): String {
        return path
            .removeSuffix(".json")
            .replace("_", " ")
            .replace("-", " ")
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }
            }
            .ifBlank { "手语" }
    }

    companion object {
        fun createFallbackTextDisplay(
            context: Context,
            signText: String,
            category: String
        ): View {
            return android.widget.LinearLayout(context).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(48, 48, 48, 48)
                setBackgroundColor(android.graphics.Color.WHITE)
                gravity = android.view.Gravity.CENTER
                addView(TextView(context).apply {
                    text = "[$category]"
                    textSize = 16f
                    setTextColor(android.graphics.Color.GRAY)
                    gravity = android.view.Gravity.CENTER
                })
                addView(TextView(context).apply {
                    text = signText
                    textSize = 72f
                    setTextColor(android.graphics.Color.BLACK)
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    gravity = android.view.Gravity.CENTER
                })
                addView(TextView(context).apply {
                    text = "请向对方展示以上手语"
                    textSize = 14f
                    setTextColor(android.graphics.Color.DKGRAY)
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    gravity = android.view.Gravity.CENTER
                })
            }
        }
    }
}