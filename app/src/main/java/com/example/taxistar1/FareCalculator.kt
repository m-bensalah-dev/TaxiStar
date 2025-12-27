package com.example.taxistar1

import android.content.Context
import android.location.Location
import com.google.gson.Gson
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class FareCalculator(private val context: Context) {

    private var fareConfig: FareConfig? = null

    init {
        loadFareConfig()
    }

    // Load JSON file from raw resources
    private fun loadFareConfig() {
        try {
            val inputStream = context.resources.openRawResource(R.raw.fare_prices)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            fareConfig = Gson().fromJson(jsonString, FareConfig::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Determine country based on GPS location
    fun getCountryFromLocation(location: Location): Country? {
        fareConfig?.countries?.let { countries ->
            var closestCountry: Country? = null
            var minDistance = Double.MAX_VALUE

            for (country in countries) {
                val distance = calculateDistance(
                    location.latitude,
                    location.longitude,
                    country.coordinates.latitude,
                    country.coordinates.longitude
                )

                if (distance < minDistance) {
                    minDistance = distance
                    closestCountry = country
                }
            }

            return closestCountry
        }
        return null
    }

    // Calculate distance between two points (Haversine formula)
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // km

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    // Check if current time is day or night
    fun isDayTime(country: Country): Boolean {
        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(Calendar.MINUTE)

        val dayStart = parseTime(country.day.start)
        val dayEnd = parseTime(country.day.end)

        val currentTotalMinutes = currentHour * 60 + currentMinute
        val dayStartMinutes = dayStart.first * 60 + dayStart.second
        val dayEndMinutes = dayEnd.first * 60 + dayEnd.second

        return if (dayEndMinutes < dayStartMinutes) {
            // Night crosses midnight (e.g., 22:00 to 06:00)
            currentTotalMinutes >= dayStartMinutes || currentTotalMinutes < dayEndMinutes
        } else {
            // Normal day hours (e.g., 06:00 to 20:00)
            currentTotalMinutes in dayStartMinutes until dayEndMinutes
        }
    }

    // Parse time string "HH:mm" to Pair(hour, minute)
    private fun parseTime(timeString: String): Pair<Int, Int> {
        val parts = timeString.split(":")
        return Pair(parts[0].toInt(), parts[1].toInt())
    }

    // Calculate fare based on distance, time, and country - FIXED VERSION
    fun calculateFare(
        country: Country,
        distanceKm: Double,
        timeMinutes: Long,
        isDay: Boolean
    ): Double {
        val rate = if (isDay) country.day else country.night

        val fare = rate.baseFare +
                (distanceKm * rate.pricePerKm) +
                (timeMinutes * rate.pricePerMinute)

        // Fix: Round to 2 decimal places properly without locale issues
        return kotlin.math.round(fare * 100.0) / 100.0
    }

    // Get current rate (day or night)
    fun getCurrentRate(country: Country): TimeRate {
        return if (isDayTime(country)) country.day else country.night
    }

    // Calculate distance between two locations in kilometers
    fun calculateDistanceBetweenLocations(start: Location, end: Location): Double {
        val results = FloatArray(1)
        Location.distanceBetween(
            start.latitude,
            start.longitude,
            end.latitude,
            end.longitude,
            results
        )
        val distanceKm = results[0] / 1000.0
        return kotlin.math.round(distanceKm * 100.0) / 100.0 // Round to 2 decimals
    }

    // Calculate time difference in minutes
    fun calculateTimeDifference(startTime: Long, endTime: Long): Long {
        return (endTime - startTime) / (1000 * 60) // Convert milliseconds to minutes
    }

    // Format fare with currency - FIXED VERSION
    fun formatFare(fare: Double, currency: String): String {
        return String.format(Locale.US, "%.2f %s", fare, currency)
    }

    // Get all available countries
    fun getAllCountries(): List<Country> {
        return fareConfig?.countries ?: emptyList()
    }
}