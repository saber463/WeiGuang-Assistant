package com.weiguangchangxing.weiguang_plus.feature.signlanguage

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class HandGestureFeatureExtractor {

    companion object {
        private const val NUM_LANDMARKS = 21
    }

    data class HandGestureFeatures(
        val fingerAngles: FloatArray,
        val fingerTips: List<HandLandmarkPoint>,
        val fingerMCPs: List<HandLandmarkPoint>,
        val wrist: HandLandmarkPoint,
        val palmCenter: HandLandmarkPoint,
        val palmNormal: HandLandmarkPoint,
        val handedness: String
    )

    fun extractFeatures(handData: HandLandmarkData): HandGestureFeatures? {
        if (handData.landmarks.size < NUM_LANDMARKS) return null

        val lms = handData.landmarks
        val wrist = lms[HandLandmarkData.WRIST]

        val fingerTips = listOf(
            lms[HandLandmarkData.THUMB_TIP],
            lms[HandLandmarkData.INDEX_FINGER_TIP],
            lms[HandLandmarkData.MIDDLE_FINGER_TIP],
            lms[HandLandmarkData.RING_FINGER_TIP],
            lms[HandLandmarkData.PINKY_TIP]
        )

        val fingerMCPs = listOf(
            lms[HandLandmarkData.THUMB_CMC],
            lms[HandLandmarkData.INDEX_FINGER_MCP],
            lms[HandLandmarkData.MIDDLE_FINGER_MCP],
            lms[HandLandmarkData.RING_FINGER_MCP],
            lms[HandLandmarkData.PINKY_MCP]
        )

        val palmCenter = HandLandmarkPoint(
            x = (lms[HandLandmarkData.INDEX_FINGER_MCP].x +
                    lms[HandLandmarkData.MIDDLE_FINGER_MCP].x +
                    lms[HandLandmarkData.RING_FINGER_MCP].x +
                    lms[HandLandmarkData.PINKY_MCP].x) / 4f,
            y = (lms[HandLandmarkData.INDEX_FINGER_MCP].y +
                    lms[HandLandmarkData.MIDDLE_FINGER_MCP].y +
                    lms[HandLandmarkData.RING_FINGER_MCP].y +
                    lms[HandLandmarkData.PINKY_MCP].y) / 4f,
            z = (lms[HandLandmarkData.INDEX_FINGER_MCP].z +
                    lms[HandLandmarkData.MIDDLE_FINGER_MCP].z +
                    lms[HandLandmarkData.RING_FINGER_MCP].z +
                    lms[HandLandmarkData.PINKY_MCP].z) / 4f,
            index = -1
        )

        val fingerAngles = FloatArray(5)
        val fingerIndices = listOf(
            listOf(HandLandmarkData.THUMB_CMC, HandLandmarkData.THUMB_MCP, HandLandmarkData.THUMB_IP, HandLandmarkData.THUMB_TIP),
            listOf(HandLandmarkData.INDEX_FINGER_MCP, HandLandmarkData.INDEX_FINGER_PIP, HandLandmarkData.INDEX_FINGER_DIP, HandLandmarkData.INDEX_FINGER_TIP),
            listOf(HandLandmarkData.MIDDLE_FINGER_MCP, HandLandmarkData.MIDDLE_FINGER_PIP, HandLandmarkData.MIDDLE_FINGER_DIP, HandLandmarkData.MIDDLE_FINGER_TIP),
            listOf(HandLandmarkData.RING_FINGER_MCP, HandLandmarkData.RING_FINGER_PIP, HandLandmarkData.RING_FINGER_DIP, HandLandmarkData.RING_FINGER_TIP),
            listOf(HandLandmarkData.PINKY_MCP, HandLandmarkData.PINKY_PIP, HandLandmarkData.PINKY_DIP, HandLandmarkData.PINKY_TIP)
        )

        for (f in 0 until 5) {
            val indices = fingerIndices[f]
            val base = lms[indices[0]]
            val tip = lms[indices[3]]
            val dx = tip.x - base.x
            val dy = tip.y - base.y
            fingerAngles[f] = atan2(dy.toDouble(), dx.toDouble()).toFloat()
        }

        val x1 = lms[HandLandmarkData.INDEX_FINGER_MCP].x - lms[HandLandmarkData.PINKY_MCP].x
        val y1 = lms[HandLandmarkData.INDEX_FINGER_MCP].y - lms[HandLandmarkData.PINKY_MCP].y
        val z1 = lms[HandLandmarkData.INDEX_FINGER_MCP].z - lms[HandLandmarkData.PINKY_MCP].z
        val x2 = lms[HandLandmarkData.INDEX_FINGER_MCP].x - lms[HandLandmarkData.WRIST].x
        val y2 = lms[HandLandmarkData.INDEX_FINGER_MCP].y - lms[HandLandmarkData.WRIST].y
        val z2 = lms[HandLandmarkData.INDEX_FINGER_MCP].z - lms[HandLandmarkData.WRIST].z

        val nx = y1 * z2 - z1 * y2
        val ny = z1 * x2 - x1 * z2
        val nz = x1 * y2 - y1 * x2
        val nLen = sqrt((nx * nx + ny * ny + nz * nz).toDouble()).toFloat()
        val palmNormal = if (nLen > 0) {
            HandLandmarkPoint(x = nx / nLen, y = ny / nLen, z = nz / nLen, index = -1)
        } else {
            HandLandmarkPoint(x = 0f, y = 0f, z = 1f, index = -1)
        }

        return HandGestureFeatures(
            fingerAngles = fingerAngles,
            fingerTips = fingerTips,
            fingerMCPs = fingerMCPs,
            wrist = wrist,
            palmCenter = palmCenter,
            palmNormal = palmNormal,
            handedness = handData.handedness
        )
    }

    fun calculateExtendedFingers(handData: HandLandmarkData): List<Boolean> {
        if (handData.landmarks.size < NUM_LANDMARKS) return listOf(false, false, false, false, false)

        val lms = handData.landmarks

        val thumbExtended = isThumbExtended(lms)
        val indexExtended = isFingerExtended(lms,
            HandLandmarkData.INDEX_FINGER_TIP,
            HandLandmarkData.INDEX_FINGER_PIP,
            HandLandmarkData.INDEX_FINGER_MCP
        )
        val middleExtended = isFingerExtended(lms,
            HandLandmarkData.MIDDLE_FINGER_TIP,
            HandLandmarkData.MIDDLE_FINGER_PIP,
            HandLandmarkData.MIDDLE_FINGER_MCP
        )
        val ringExtended = isFingerExtended(lms,
            HandLandmarkData.RING_FINGER_TIP,
            HandLandmarkData.RING_FINGER_PIP,
            HandLandmarkData.RING_FINGER_MCP
        )
        val pinkyExtended = isFingerExtended(lms,
            HandLandmarkData.PINKY_TIP,
            HandLandmarkData.PINKY_PIP,
            HandLandmarkData.PINKY_MCP
        )

        return listOf(thumbExtended, indexExtended, middleExtended, ringExtended, pinkyExtended)
    }

    private fun isThumbExtended(lms: List<HandLandmarkPoint>): Boolean {
        val tip = lms[HandLandmarkData.THUMB_TIP]
        val ip = lms[HandLandmarkData.THUMB_IP]
        val mcp = lms[HandLandmarkData.THUMB_MCP]

        val tipToMCP = distance3D(tip, mcp)
        val ipToMCP = distance3D(ip, mcp)

        return tipToMCP > ipToMCP * 0.8f
    }

    private fun isFingerExtended(
        lms: List<HandLandmarkPoint>,
        tipIdx: Int,
        pipIdx: Int,
        mcpIdx: Int
    ): Boolean {
        val tip = lms[tipIdx]
        val pip = lms[pipIdx]
        val mcp = lms[mcpIdx]

        val tipToMCP = distance3D(tip, mcp)
        val pipToMCP = distance3D(pip, mcp)

        return tipToMCP > pipToMCP * 0.85f
    }

    fun calculateFingerTipDistances(handData: HandLandmarkData): FloatArray {
        if (handData.landmarks.size < NUM_LANDMARKS) return FloatArray(10) { 0f }

        val tips = listOf(
            handData.landmarks[HandLandmarkData.THUMB_TIP],
            handData.landmarks[HandLandmarkData.INDEX_FINGER_TIP],
            handData.landmarks[HandLandmarkData.MIDDLE_FINGER_TIP],
            handData.landmarks[HandLandmarkData.RING_FINGER_TIP],
            handData.landmarks[HandLandmarkData.PINKY_TIP]
        )

        val distances = mutableListOf<Float>()
        for (i in tips.indices) {
            for (j in i + 1 until tips.size) {
                distances.add(distance3D(tips[i], tips[j]))
            }
        }

        val wrist = handData.landmarks[HandLandmarkData.WRIST]
        for (tip in tips) {
            distances.add(distance3D(tip, wrist))
        }

        return distances.toFloatArray()
    }

    fun calculateHandOrientation(handData: HandLandmarkData): String {
        if (handData.landmarks.size < NUM_LANDMARKS) return "unknown"

        val wrist = handData.landmarks[HandLandmarkData.WRIST]
        val middleTip = handData.landmarks[HandLandmarkData.MIDDLE_FINGER_TIP]

        val handVectorX = middleTip.x - wrist.x
        val handVectorY = middleTip.y - wrist.y

        return when {
            abs(handVectorY) > abs(handVectorX) && handVectorY < 0 -> "竖直向上"
            abs(handVectorY) > abs(handVectorX) && handVectorY > 0 -> "竖直向下"
            abs(handVectorX) > abs(handVectorY) && handVectorX < 0 -> "水平向左"
            abs(handVectorX) > abs(handVectorY) && handVectorX > 0 -> "水平向右"
            else -> "倾斜"
        }
    }

    private fun distance3D(a: HandLandmarkPoint, b: HandLandmarkPoint): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        val dz = a.z - b.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}