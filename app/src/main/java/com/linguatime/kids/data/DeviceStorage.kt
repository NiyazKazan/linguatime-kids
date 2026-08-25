package com.linguatime.kids.data

import android.content.Context
import java.util.UUID

class DeviceStorage(context: Context) {

    private val prefs = context.getSharedPreferences("linguatime", Context.MODE_PRIVATE)

    fun deviceId(): String {
        val existing = prefs.getString("device_id", null)
        if (existing != null) return existing
        val id = UUID.randomUUID().toString()
        prefs.edit().putString("device_id", id).apply()
        return id
    }

    fun saveChildId(childId: String?) {
        prefs.edit().putString("child_id", childId).apply()
    }

    fun childId(): String? = prefs.getString("child_id", null)
}