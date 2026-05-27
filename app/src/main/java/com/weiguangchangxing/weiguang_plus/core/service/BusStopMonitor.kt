package com.weiguangchangxing.weiguang_plus.core.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.*

data class BusStop(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

data class BusLine(
    val name: String,
    val stops: List<BusStop>
)

class BusStopMonitor(private val context: Context) {

    companion object {
        private const val PREF_NAME = "bus_stop_monitor"
        private const val KEY_CUSTOM_LINES = "custom_lines"
        private const val KEY_LAST_LAT = "last_lat"
        private const val KEY_LAST_LNG = "last_lng"
        private const val KEY_LAST_TIME = "last_time"
    }

    private val locationManager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val customLines: MutableList<BusLine> by lazy { loadCustomLines() }

    private val lines: List<BusLine> = buildBusLines()

    private fun buildBusLines(): List<BusLine> {
        return listOf(
            BusLine("1路", listOf(
                BusStop("火车站", 30.55, 104.06),
                BusStop("人民广场", 30.56, 104.07),
                BusStop("市政府", 30.57, 104.08),
                BusStop("中心医院", 30.58, 104.09),
                BusStop("大学城", 30.59, 104.10),
                BusStop("软件园", 30.60, 104.11),
                BusStop("高新区", 30.61, 104.12)
            )),
            BusLine("2路", listOf(
                BusStop("汽车站", 30.51, 104.01),
                BusStop("批发市场", 30.52, 104.02),
                BusStop("第一中学", 30.53, 104.03),
                BusStop("图书馆", 30.54, 104.04),
                BusStop("体育中心", 30.55, 104.05),
                BusStop("森林公园", 30.56, 104.06)
            )),
            BusLine("3路", listOf(
                BusStop("机场", 30.52, 104.00),
                BusStop("航空港", 30.53, 104.01),
                BusStop("会展中心", 30.54, 104.02),
                BusStop("世纪城", 30.55, 104.03),
                BusStop("金融中心", 30.56, 104.04),
                BusStop("天府广场", 30.57, 104.05)
            )),
            BusLine("4路", listOf(
                BusStop("火车东站", 30.58, 104.12),
                BusStop("万年场", 30.59, 104.11),
                BusStop("双桥子", 30.60, 104.10),
                BusStop("牛王庙", 30.61, 104.09),
                BusStop("东门大桥", 30.62, 104.08),
                BusStop("春熙路", 30.63, 104.07)
            )),
            BusLine("5路", listOf(
                BusStop("动物园", 30.65, 104.06),
                BusStop("昭觉寺", 30.64, 104.07),
                BusStop("驷马桥", 30.63, 104.08),
                BusStop("梁家巷", 30.62, 104.09),
                BusStop("北门大桥", 30.61, 104.10),
                BusStop("文武路", 30.60, 104.11)
            )),
            BusLine("6路", listOf(
                BusStop("金沙车站", 30.66, 104.00),
                BusStop("府南新区", 30.65, 104.01),
                BusStop("战旗小区", 30.64, 104.02),
                BusStop("营门口", 30.63, 104.03),
                BusStop("会展中心", 30.62, 104.04),
                BusStop("九里堤", 30.61, 104.05)
            )),
            BusLine("7路", listOf(
                BusStop("万科魅力之城", 30.57, 104.13),
                BusStop("建材路", 30.58, 104.14),
                BusStop("迎晖路", 30.59, 104.15),
                BusStop("塔子山", 30.60, 104.16),
                BusStop("双桥子", 30.61, 104.17),
                BusStop("建设路", 30.62, 104.18)
            )),
            BusLine("8路", listOf(
                BusStop("跳蹬河", 30.63, 104.18),
                BusStop("万年场", 30.62, 104.17),
                BusStop("新华公园", 30.61, 104.16),
                BusStop("红星桥", 30.60, 104.15),
                BusStop("太升路", 30.59, 104.14),
                BusStop("骡马市", 30.58, 104.13)
            )),
            BusLine("9路", listOf(
                BusStop("高新西区", 30.70, 104.00),
                BusStop("电子科大", 30.69, 104.02),
                BusStop("西芯大道", 30.68, 104.04),
                BusStop("茶店子", 30.67, 104.06),
                BusStop("营门口", 30.66, 104.08),
                BusStop("西门车站", 30.65, 104.10)
            )),
            BusLine("10路", listOf(
                BusStop("石板滩", 30.68, 104.20),
                BusStop("木兰镇", 30.67, 104.18),
                BusStop("三河场", 30.66, 104.16),
                BusStop("锦水河", 30.65, 104.14),
                BusStop("钟楼", 30.64, 104.12),
                BusStop("新都客运站", 30.63, 104.10)
            ))
        )
    }

    private fun loadCustomLines(): MutableList<BusLine> {
        val list = mutableListOf<BusLine>()
        val json = prefs.getString(KEY_CUSTOM_LINES, null) ?: return list
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val name = obj.getString("name")
                val stopsArr = obj.getJSONArray("stops")
                val stops = mutableListOf<BusStop>()
                for (j in 0 until stopsArr.length()) {
                    val s = stopsArr.getJSONObject(j)
                    stops.add(BusStop(s.getString("name"), s.getDouble("lat"), s.getDouble("lng")))
                }
                list.add(BusLine(name, stops))
            }
        } catch (_: Exception) {}
        return list
    }

    private fun saveCustomLines() {
        val arr = JSONArray()
        for (line in customLines) {
            val obj = JSONObject()
            obj.put("name", line.name)
            val stopsArr = JSONArray()
            for (stop in line.stops) {
                val s = JSONObject()
                s.put("name", stop.name)
                s.put("lat", stop.latitude)
                s.put("lng", stop.longitude)
                stopsArr.put(s)
            }
            obj.put("stops", stopsArr)
            arr.put(obj)
        }
        prefs.edit().putString(KEY_CUSTOM_LINES, arr.toString()).apply()
    }

    fun getAvailableLines(): List<BusLine> = lines + customLines

    fun addCustomLine(name: String, stops: List<BusStop>) {
        customLines.removeAll { it.name == name }
        customLines.add(BusLine(name, stops))
        saveCustomLines()
    }

    fun removeCustomLine(name: String) {
        customLines.removeAll { it.name == name }
        saveCustomLines()
    }

    fun getAllCustomLines(): List<BusLine> = customLines.toList()

    fun calculateDistanceFromStop(lat: Double, lng: Double, stop: BusStop): Float {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(stop.latitude - lat)
        val dLng = Math.toRadians(stop.longitude - lng)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat)) * cos(Math.toRadians(stop.latitude)) *
                sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (earthRadius * c).toFloat()
    }

    fun findNearestStop(
        lat: Double,
        lng: Double,
        line: BusLine
    ): Pair<BusStop, Float>? {
        var nearest: Pair<BusStop, Float>? = null
        for (stop in line.stops) {
            val distance = calculateDistanceFromStop(lat, lng, stop)
            if (distance <= 2000f) {
                if (nearest == null || distance < nearest.second) {
                    nearest = Pair(stop, distance)
                }
            }
        }
        return nearest
    }

    fun isApproachingStop(
        lat: Double,
        lng: Double,
        line: BusLine,
        thresholdMeters: Float = 300f
    ): Pair<BusStop, Float>? {
        val nearest = findNearestStop(lat, lng, line) ?: return null
        return if (nearest.second < thresholdMeters) nearest else null
    }

    private fun cacheLocation(lat: Double, lng: Double) {
        prefs.edit()
            .putFloat(KEY_LAST_LAT, lat.toFloat())
            .putFloat(KEY_LAST_LNG, lng.toFloat())
            .putLong(KEY_LAST_TIME, System.currentTimeMillis())
            .apply()
    }

    fun getCachedLocation(): Pair<Double, Double>? {
        val lat = prefs.getFloat(KEY_LAST_LAT, Float.NaN)
        val lng = prefs.getFloat(KEY_LAST_LNG, Float.NaN)
        val time = prefs.getLong(KEY_LAST_TIME, 0L)
        if (lat.isNaN() || lng.isNaN() || time == 0L) return null
        if (System.currentTimeMillis() - time > 24 * 60 * 60 * 1000L) return null
        return Pair(lat.toDouble(), lng.toDouble())
    }

    fun getLocation(): Pair<Double, Double>? {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation && !hasCoarseLocation) {
            return getCachedLocation()
        }

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
        )

        for (provider in providers) {
            val location = locationManager?.getLastKnownLocation(provider) ?: continue
            return Pair(location.latitude, location.longitude).also {
                cacheLocation(it.first, it.second)
            }
        }

        return getCachedLocation()
    }
}