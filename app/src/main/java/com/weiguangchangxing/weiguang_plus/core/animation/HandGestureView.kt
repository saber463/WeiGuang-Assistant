package com.weiguangchangxing.weiguang_plus.core.animation

import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

data class HandPose(
    val name: String,
    val fingerCurls: FloatArray,
    val thumbAbduction: Float
)

object HandPoseData {
    val poses = listOf(
        HandPose("打开手掌",  floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f), 0.5f),
        HandPose("握拳",      floatArrayOf(0.9f, 0.9f, 0.9f, 0.9f, 0.9f), 0.0f),
        HandPose("竖起大拇指", floatArrayOf(0.0f, 0.9f, 0.9f, 0.9f, 0.9f), 1.0f),
        HandPose("食指指",    floatArrayOf(0.9f, 0.0f, 0.9f, 0.9f, 0.9f), 0.3f),
        HandPose("V字",      floatArrayOf(0.9f, 0.0f, 0.0f, 0.9f, 0.9f), 0.2f),
        HandPose("比心",      floatArrayOf(0.9f, 0.9f, 0.9f, 0.9f, 0.0f), 0.1f),
        HandPose("数字1",     floatArrayOf(0.9f, 0.0f, 0.9f, 0.9f, 0.9f), 0.3f),
        HandPose("数字2",     floatArrayOf(0.9f, 0.0f, 0.0f, 0.9f, 0.9f), 0.2f),
        HandPose("数字3",     floatArrayOf(0.9f, 0.0f, 0.0f, 0.0f, 0.9f), 0.2f),
        HandPose("数字4",     floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f, 0.9f), 0.3f),
        HandPose("数字5",     floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f), 0.5f),
        HandPose("数字6",     floatArrayOf(0.9f, 0.9f, 0.0f, 0.0f, 0.0f), 0.1f),
        HandPose("数字7",     floatArrayOf(0.9f, 0.0f, 0.9f, 0.0f, 0.0f), 0.2f),
        HandPose("数字8",     floatArrayOf(0.9f, 0.0f, 0.0f, 0.9f, 0.0f), 0.2f),
        HandPose("数字9",     floatArrayOf(0.0f, 0.0f, 0.0f, 0.9f, 0.9f), 0.4f),
        HandPose("OK手势",    floatArrayOf(0.5f, 0.9f, 0.9f, 0.9f, 0.9f), 0.8f),
        HandPose("手枪手势",  floatArrayOf(0.9f, 0.0f, 0.9f, 0.9f, 0.7f), 0.6f),
        HandPose("摇滚手势",  floatArrayOf(0.9f, 0.0f, 0.9f, 0.9f, 0.0f), 0.7f),
        HandPose("三指",      floatArrayOf(0.9f, 0.0f, 0.0f, 0.0f, 0.9f), 0.2f),
        HandPose("挥手",      floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f), 0.5f),
        HandPose("爱",        floatArrayOf(0.0f, 0.9f, 0.9f, 0.0f, 0.0f), 0.3f)
    )

    private val nameToPose = poses.associateBy { it.name }

    fun findPose(name: String): HandPose? {
        return nameToPose[name] ?: poses.find { name.contains(it.name) }
    }
}

class HandGestureView(context: Context) : View(context) {
    private var currentCurls = FloatArray(5) { 0f }
    private var targetCurls = FloatArray(5) { 0f }
    private var currentAbduction = 0.5f
    private var targetAbduction = 0.5f
    private var currentWobble = 0f
    private var animator: ValueAnimator? = null
    private var wobbleAnimator: ValueAnimator? = null

    private val skinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 200, 170)
        style = Paint.Style.FILL
        strokeCap = Paint.Cap.ROUND
    }
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(180, 120, 100)
        style = Paint.Style.STROKE
        strokeWidth = 3f
        strokeCap = Paint.Cap.ROUND
    }
    private val jointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(220, 160, 140)
        style = Paint.Style.FILL
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 36f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val subLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }

    private var gestureName = ""
    private var prevCurls = FloatArray(5) { 0f }
    private var prevAbduction = 0.5f

    fun setGesture(name: String, animate: Boolean = true) {
        val pose = HandPoseData.findPose(name)
        if (pose == null) {
            gestureName = ""
            return
        }
        gestureName = pose.name

        prevCurls = currentCurls.copyOf()
        prevAbduction = currentAbduction
        targetCurls = pose.fingerCurls
        targetAbduction = pose.thumbAbduction

        animator?.cancel()
        if (animate) {
            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 400
                interpolator = DecelerateInterpolator()
                addUpdateListener { anim ->
                    val t = anim.animatedFraction
                    currentCurls = FloatArray(5) { i ->
                        prevCurls[i] + (targetCurls[i] - prevCurls[i]) * t
                    }
                    currentAbduction = prevAbduction + (targetAbduction - prevAbduction) * t
                    invalidate()
                }
                start()
            }
        } else {
            currentCurls = targetCurls.copyOf()
            currentAbduction = targetAbduction
            invalidate()
        }

        if (name.contains("挥手")) {
            startWobble()
        } else {
            stopWobble()
        }
    }

    private fun startWobble() {
        wobbleAnimator?.cancel()
        wobbleAnimator = ValueAnimator.ofFloat(-15f, 15f).apply {
            duration = 400
            repeatMode = ValueAnimator.REVERSE
            repeatCount = INFINITE
            addUpdateListener { currentWobble = it.animatedFraction * 30f - 15f; invalidate() }
            start()
        }
    }

    private fun stopWobble() {
        wobbleAnimator?.cancel()
        currentWobble = 0f
    }

    fun stopAnimation() {
        animator?.cancel()
        wobbleAnimator?.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val scale = min(width, height) / 400f
        canvas.save()
        canvas.scale(scale, scale, cx, cy)
        canvas.rotate(currentWobble, cx, cy)

        val palmTop = cy + 20f
        val palmLeft = cx - 55f
        val palmRight = cx + 55f
        val palmBottom = cy + 100f
        val palmRect = RectF(palmLeft, palmTop, palmRight, palmBottom)

        canvas.drawRoundRect(palmRect, 28f, 28f, skinPaint)
        canvas.drawRoundRect(palmRect, 28f, 28f, outlinePaint)

        val fingerBaseX = floatArrayOf(cx - 35f, cx - 12f, cx + 8f, cx + 28f, cx + 45f)
        val fingerBaseY = palmTop
        val fingerAngles = floatArrayOf(-5f, 0f, 2f, 4f, 8f)

        for (i in 0..4) {
            val curl = currentCurls[i].coerceIn(0f, 1f)
            val angle = fingerAngles[i]
            val fullLen = (160f - i * 12f) * (if (i == 0) 0.7f else 1f)
            val segment1Len = fullLen * 0.55f
            val segment2Len = fullLen * 0.45f
            val curl1 = curl.coerceAtMost(0.6f) / 0.6f
            val curl2 = curl

            val bx = fingerBaseX[i]
            val by = fingerBaseY
            val angleRad = Math.toRadians(angle.toDouble())
            val curlAngle1 = curl1 * 60f
            val curlAngle2 = curl2 * 80f

            val s1EndX = bx + (cos(angleRad - Math.toRadians(curlAngle1.toDouble())) * segment1Len).toFloat()
            val s1EndY = by + (sin(angleRad - Math.toRadians(curlAngle1.toDouble())) * segment1Len).toFloat() - segment1Len

            val s1EndAngle = angle - curlAngle1
            val s1EndAngleRad = Math.toRadians(s1EndAngle.toDouble())
            val s2EndY = s1EndY + (sin(s1EndAngleRad - Math.toRadians(curlAngle2.toDouble())) * segment2Len).toFloat() - segment2Len

            val fingerWidth = if (i == 0) 18f else 14f

            skinPaint.color = if (i == 0) Color.rgb(255, 190, 155) else Color.rgb(255, 200, 170)

            val p1 = RectF(bx - fingerWidth / 2, s1EndY - 2f, bx + fingerWidth / 2, by + 5f)
            val tipRadius = fingerWidth / 2
            canvas.drawRoundRect(p1, tipRadius, tipRadius, skinPaint)
            canvas.drawRoundRect(p1, tipRadius, tipRadius, outlinePaint)

            val p2 = RectF(s1EndX - fingerWidth / 2, s2EndY, s1EndX + fingerWidth / 2, s1EndY + 3f)
            canvas.drawRoundRect(p2, tipRadius, tipRadius, skinPaint)
            canvas.drawRoundRect(p2, tipRadius, tipRadius, outlinePaint)

            canvas.drawCircle(bx, (s1EndY + by) / 2, 5f, jointPaint)
            canvas.drawCircle(s1EndX, (s2EndY + s1EndY) / 2, 4f, jointPaint)

            if (i == 0) {
                val abdAngle = currentAbduction * 25f
                val abd = Math.toRadians((-50 - abdAngle).toDouble())
                val thumbLen = fullLen * 0.8f
                val thumbEndX = bx + (cos(abd) * thumbLen).toFloat()
                val thumbEndY = by + (sin(abd) * thumbLen).toFloat() - thumbLen * 0.5f
                val thumbRect = RectF(
                    kotlin.math.min(thumbEndX, bx) - 12f,
                    kotlin.math.min(thumbEndY, by) - 12f,
                    kotlin.math.max(thumbEndX, bx) + 12f,
                    kotlin.math.max(thumbEndY, by) + 12f
                )
                skinPaint.color = Color.rgb(255, 190, 155)
                canvas.drawRoundRect(thumbRect, 12f, 12f, skinPaint)
                canvas.drawRoundRect(thumbRect, 12f, 12f, outlinePaint)
            }
        }

        if (gestureName.isNotEmpty()) {
            canvas.drawText(gestureName, cx, height - 40f, labelPaint)
            canvas.drawText("请向对方展示", cx, height - 10f, subLabelPaint)
        }

        canvas.restore()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
        wobbleAnimator?.cancel()
    }
}