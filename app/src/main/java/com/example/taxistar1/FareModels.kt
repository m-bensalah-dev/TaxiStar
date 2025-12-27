package com.example.taxistar1

import java.text.SimpleDateFormat
import java.util.*

data class FareConfig(
    val countries: List<Country>
)

data class Country(
    val name: String,
    val currency: String,
    val coordinates: Coordinates,
    val day: TimeRate,
    val night: TimeRate
)

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

data class TimeRate(
    val start: String,
    val end: String,
    val pricePerKm: Double,
    val pricePerMinute: Double,
    val baseFare: Double
)

data class RideData(
    var id: String = UUID.randomUUID().toString(),
    var startLocation: android.location.Location? = null,
    var endLocation: android.location.Location? = null,
    var startTime: Long = 0,
    var endTime: Long = 0,
    var distanceKm: Double = 0.0,
    var timeMinutes: Long = 0,
    var fare: Double = 0.0,
    var country: String = "",
    var currency: String = "",
    var isDay: Boolean = true
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(startTime))
    }

    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(startTime))
    }

    fun getFormattedDay(): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date(startTime))
    }

    fun getDuration(): String {
        return if (timeMinutes < 1) {
            "Less than 1 min"
        } else {
            "$timeMinutes min"
        }
    }
}