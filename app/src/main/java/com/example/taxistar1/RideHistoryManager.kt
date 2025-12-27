package com.example.taxistar1

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RideHistoryManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "RideHistoryPrefs"
        private const val KEY_RIDE_HISTORY = "ride_history"
    }

    // Save a completed ride
    fun saveRide(rideData: RideData) {
        val rides = getAllRides().toMutableList()
        rides.add(0, rideData) // Add at the beginning (newest first)

        val json = gson.toJson(rides)
        prefs.edit().putString(KEY_RIDE_HISTORY, json).apply()
    }

    // Get all rides
    fun getAllRides(): List<RideData> {
        val json = prefs.getString(KEY_RIDE_HISTORY, null)
        return if (json != null) {
            val type = object : TypeToken<List<RideData>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    // Search rides by query (name, country, date, etc.)
    fun searchRides(query: String): List<RideData> {
        if (query.isBlank()) return getAllRides()

        val lowerQuery = query.lowercase()
        return getAllRides().filter { ride ->
            ride.country.lowercase().contains(lowerQuery) ||
                    ride.getFormattedDate().lowercase().contains(lowerQuery) ||
                    ride.getFormattedDay().lowercase().contains(lowerQuery) ||
                    ride.currency.lowercase().contains(lowerQuery) ||
                    ride.fare.toString().contains(lowerQuery) ||
                    ride.distanceKm.toString().contains(lowerQuery)
        }
    }

    // Delete a ride
    fun deleteRide(rideId: String) {
        val rides = getAllRides().toMutableList()
        rides.removeAll { it.id == rideId }

        val json = gson.toJson(rides)
        prefs.edit().putString(KEY_RIDE_HISTORY, json).apply()
    }

    // Clear all history
    fun clearAllHistory() {
        prefs.edit().remove(KEY_RIDE_HISTORY).apply()
    }

    // Get total statistics
    fun getTotalStatistics(): RideStatistics {
        val rides = getAllRides()
        return RideStatistics(
            totalRides = rides.size,
            totalDistance = rides.sumOf { it.distanceKm },
            totalFare = rides.sumOf { it.fare },
            totalTime = rides.sumOf { it.timeMinutes }
        )
    }
}

data class RideStatistics(
    val totalRides: Int,
    val totalDistance: Double,
    val totalFare: Double,
    val totalTime: Long
)