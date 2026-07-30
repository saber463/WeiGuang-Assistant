package com.weiguangplus.core.emergency

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object EmergencyContactManager {

    private const val PREFS_NAME = "sos_contacts"
    private const val KEY_COUNT = "contact_count"
    private const val KEY_NAME = "contact_name_"
    private const val KEY_PHONE = "contact_phone_"

    data class Contact(val name: String, val phone: String)

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getContacts(): List<Contact> {
        val p = prefs ?: return emptyList()
        val count = p.getInt(KEY_COUNT, 0)
        return (0 until count).mapNotNull { i ->
            val name = p.getString(KEY_NAME + i, null)
            val phone = p.getString(KEY_PHONE + i, null)
            if (name != null && phone != null) Contact(name, phone) else null
        }
    }

    fun setContacts(contacts: List<Contact>) {
        val p = prefs ?: return
        p.edit {
            putInt(KEY_COUNT, contacts.size)
            contacts.forEachIndexed { i, c ->
                putString(KEY_NAME + i, c.name)
                putString(KEY_PHONE + i, c.phone)
            }
        }
    }

    fun hasContacts(): Boolean = getContacts().isNotEmpty()
}
