package com.weiguangchangxing.weiguang_plus.core.map

import kotlin.math.*

data class AccessibleRoute(
    val name: String,
    val description: String,
    val hasElevator: Boolean,
    val hasRamp: Boolean,
    val hasTactilePaving: Boolean,
    val hasAudioSignals: Boolean,
    val riskNotes: List<String> = emptyList()
)

data class AccessibleLocation(
    val name: String,
    val lat: Double,
    val lng: Double,
    val facilities: List<String> = emptyList()
)

object AccessibleRoutePlanner {

    private val accessibleLocations = listOf(
        AccessibleLocation(
            name = "市中心医院",
            lat = 30.57,
            lng = 104.07,
            facilities = listOf("无障碍通道", "轮椅租赁", "手语服务")
        ),
        AccessibleLocation(
            name = "市图书馆",
            lat = 30.59,
            lng = 104.09,
            facilities = listOf("无障碍通道", "盲文图书", "听障阅览室")
        ),
        AccessibleLocation(
            name = "政务服务中心",
            lat = 30.61,
            lng = 104.05,
            facilities = listOf("无障碍通道", "手语翻译", "轮椅租赁")
        ),
        AccessibleLocation(
            name = "体育中心",
            lat = 30.63,
            lng = 104.08,
            facilities = listOf("无障碍通道", "盲道", "音频导览")
        ),
        AccessibleLocation(
            name = "火车站",
            lat = 30.55,
            lng = 104.06,
            facilities = listOf("无障碍通道", "轮椅服务", "手语服务")
        )
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

    fun getAccessibleDestinations(): List<AccessibleLocation> = accessibleLocations

    fun getRouteToDestination(
        fromLat: Double,
        fromLng: Double,
        destinationName: String
    ): AccessibleRoute? {
        val destination = accessibleLocations.find { it.name == destinationName } ?: return null

        val distance = calculateDistance(fromLat, fromLng, destination.lat, destination.lng)
        val distanceStr = when {
            distance < 1000 -> "${distance.toInt()}米"
            else -> String.format("%.1f公里", distance / 1000)
        }

        val facilitiesText = destination.facilities.joinToString("、")

        return AccessibleRoute(
            name = "前往${destination.name}",
            description = "距当前位置约$distanceStr，目的地配备$facilitiesText",
            hasElevator = destination.facilities.contains("无障碍通道"),
            hasRamp = destination.facilities.contains("无障碍通道"),
            hasTactilePaving = destination.facilities.contains("盲道"),
            hasAudioSignals = destination.facilities.contains("音频导览"),
            riskNotes = if (distance > 2000) listOf("距离较远，建议乘坐无障碍交通工具") else emptyList()
        )
    }

    fun getFacilitiesNearby(
        lat: Double,
        lng: Double,
        radiusMeters: Double = 1000.0
    ): List<AccessibleLocation> {
        return accessibleLocations.filter { location ->
            val distance = calculateDistance(lat, lng, location.lat, location.lng)
            distance <= radiusMeters
        }
    }
}