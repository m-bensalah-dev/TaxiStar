package com.example.taxistar1

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class PreferencesHelper(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "TaxiStarPrefs"
        private const val KEY_DRIVER_INFO = "driver_info"
        private const val KEY_IS_REGISTERED = "is_registered"
    }

    fun isDriverRegistered(): Boolean {
        return prefs.getBoolean(KEY_IS_REGISTERED, false)
    }

    fun saveDriverInfo(driverInfo: DriverInfo) {
        val json = gson.toJson(driverInfo)
        prefs.edit().apply {
            putString(KEY_DRIVER_INFO, json)
            putBoolean(KEY_IS_REGISTERED, true)
            apply()
        }
    }

    fun getDriverInfo(): DriverInfo? {
        val json = prefs.getString(KEY_DRIVER_INFO, null)
        return if (json != null) {
            gson.fromJson(json, DriverInfo::class.java)
        } else {
            null
        }
    }

    fun clearDriverInfo() {
        prefs.edit().apply {
            remove(KEY_DRIVER_INFO)
            putBoolean(KEY_IS_REGISTERED, false)
            apply()
        }
    }
}