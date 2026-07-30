package com.weiguangplus.core.emergency

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SosHistoryManager {

    private const val PREFS = "sos_history"
    private const val KEY = "records"
    private const val MAX = 20

    data class SosRecord(
        val timestamp: Long,
        val success: Boolean,
        val smsSent: Int,
        val location: String?,
        val error: String?
    ) {
        val timeFormatted: String get() {
            val sdf = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    fun record(context: Context, result: SosManager.SosResult) {
        val p = prefs ?: context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val records = getInternal(p).toMutableList()
        records.add(0, SosRecord(
            timestamp = System.currentTimeMillis(),
            success = result.success,
            smsSent = result.smsSent,
            location = result.location,
            error = result.error
        ))
        if (records.size > MAX) { records.subList(MAX, records.size).clear() }
        saveInternal(p, records)
    }

    fun getRecords(context: Context): List<SosRecord> {
        val p = prefs ?: context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return getInternal(p)
    }

    fun clear(context: Context) {
        val p = prefs ?: context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit { remove(KEY) }
    }

    private fun getInternal(p: SharedPreferences): List<SosRecord> {
        val json = p.getString(KEY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                SosRecord(
                    timestamp = obj.getLong("ts"),
                    success = obj.getBoolean("ok"),
                    smsSent = obj.optInt("sms", 0),
                    location = obj.optString("loc").ifEmpty { null },
                    error = obj.optString("err").ifEmpty { null }
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    private fun saveInternal(p: SharedPreferences, records: List<SosRecord>) {
        val arr = JSONArray()
        for (r in records) {
            arr.put(JSONObject().apply {
                put("ts", r.timestamp)
                put("ok", r.success)
                put("sms", r.smsSent)
                r.location?.let { put("loc", it) }
                r.error?.let { put("err", it) }
            })
        }
        p.edit { putString(KEY, arr.toString()) }
    }
}
