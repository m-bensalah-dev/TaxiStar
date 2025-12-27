package com.example.taxistar1

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "taxi_ride_channel"
        private const val CHANNEL_NAME = "Taxi Ride Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for completed taxi rides"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendRideCompletedNotification(rideData: RideData, fare: String) {
        // Check notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        // Create notification intent
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🚗 Ride Completed!")
            .setContentText("Total Fare: $fare")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(buildRideDetails(rideData, fare))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        // Show notification
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun buildRideDetails(rideData: RideData, fare: String): String {
        val timeText = if (rideData.timeMinutes < 1) {
            "Less than 1 minute"
        } else {
            "${rideData.timeMinutes} minutes"
        }

        return """
            🚗 Ride Completed!
            
             Total Fare: $fare
             Distance: ${String.format("%.2f", rideData.distanceKm)} km
             Time: $timeText
             Country: ${rideData.country}
            ${if (rideData.isDay) " Day Rate" else " Night Rate"}
            
           
        """.trimIndent()
    }
}