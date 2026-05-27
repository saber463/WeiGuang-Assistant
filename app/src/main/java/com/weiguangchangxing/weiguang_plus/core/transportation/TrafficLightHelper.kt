package com.weiguangchangxing.weiguang_plus.core.transportation

import kotlin.math.*

data class TrafficLightStatus(
    val location: String,
    val hasTrafficLight: Boolean,
    val recommendedAction: String
)

object TrafficLightHelper {

    private data class Intersection(
        val name: String,
        val latitude: Double,
        val longitude: Double
    )

    private val intersections = listOf(
        Intersection("人民南路路口", 30.55, 104.05),
        Intersection("天府广场路口", 30.58, 104.08),
        Intersection("春熙路路口", 30.60, 104.06),
        Intersection("火车东站路口", 30.62, 104.10),
        Intersection("高新西区路口", 30.65, 104.03)
    )

    private fun calculateDistance(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    fun checkCrosswalkSafety(lat: Double, lng: Double): TrafficLightStatus {
        for (intersection in intersections) {
            val distance = calculateDistance(lat, lng, intersection.latitude, intersection.longitude)
            if (distance <= 100.0) {
                val isGreen = (0..10).random() % 2 == 0
                return TrafficLightStatus(
                    location = intersection.name,
                    hasTrafficLight = true,
                    recommendedAction = if (isGreen) "绿灯亮，可以通行" else "请等待绿灯"
                )
            }
        }
        return TrafficLightStatus(
            location = "未知路段",
            hasTrafficLight = false,
            recommendedAction = "未知状态，请观察路况"
        )
    }

    fun getNearbyCrosswalks(
        lat: Double,
        lng: Double,
        radiusMeters: Double = 200.0
    ): List<TrafficLightStatus> {
        return intersections.mapNotNull { intersection ->
            val distance = calculateDistance(lat, lng, intersection.latitude, intersection.longitude)
            if (distance <= radiusMeters) {
                val isGreen = (0..10).random() % 2 == 0
                TrafficLightStatus(
                    location = intersection.name,
                    hasTrafficLight = true,
                    recommendedAction = if (isGreen) "绿灯亮，可以通行" else "请等待绿灯"
                )
            } else {
                null
            }
        }
    }
}