package com.weiguangchangxing.weiguang_plus.core.emergency

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class EmergencyContact(
    val name: String,
    val phone: String,
    val relation: String = ""
)

object EmergencyContactManager {

    private const val PREFS_NAME = "emergency_contacts"
    private const val KEY_CONTACTS = "contacts_json"

    private var prefs: SharedPreferences? = null
    private val contacts = mutableListOf<EmergencyContact>()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadFromPrefs()
    }

    private fun loadFromPrefs() {
        contacts.clear()
        val json = prefs?.getString(KEY_CONTACTS, null) ?: return
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                contacts.add(
                    EmergencyContact(
                        name = obj.optString("name", ""),
                        phone = obj.optString("phone", ""),
                        relation = obj.optString("relation", "")
                    )
                )
            }
        } catch (_: Exception) {
        }
    }

    private fun saveToPrefs() {
        val array = JSONArray()
        for (c in contacts) {
            val obj = JSONObject()
            obj.put("name", c.name)
            obj.put("phone", c.phone)
            obj.put("relation", c.relation)
            array.put(obj)
        }
        prefs?.edit()?.putString(KEY_CONTACTS, array.toString())?.apply()
    }

    fun getContacts(): List<EmergencyContact> = contacts.toList()

    fun addContact(contact: EmergencyContact) {
        contacts.add(contact)
        saveToPrefs()
    }

    fun removeContact(index: Int) {
        if (index in contacts.indices) {
            contacts.removeAt(index)
            saveToPrefs()
        }
    }

    fun updateContact(index: Int, contact: EmergencyContact) {
        if (index in contacts.indices) {
            contacts[index] = contact
            saveToPrefs()
        }
    }
}