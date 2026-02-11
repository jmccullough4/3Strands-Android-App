package com.threestrandscattle.app.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.threestrandscattle.app.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class NotificationService private constructor(private val context: Context) {

    private val _isAuthorized = MutableStateFlow(false)
    val isAuthorized: StateFlow<Boolean> = _isAuthorized

    private val notificationManager = NotificationManagerCompat.from(context)
    private val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)

    companion object {
        const val CHANNEL_ID_SALES = "flash_sales"
        const val CHANNEL_ID_PROXIMITY = "proximity_alerts"
        const val CHANNEL_ID_GENERAL = "general"

        @Volatile
        private var instance: NotificationService? = null

        fun getInstance(context: Context): NotificationService {
            return instance ?: synchronized(this) {
                instance ?: NotificationService(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        createNotificationChannels()
        checkAuthorizationStatus()
    }

    private fun createNotificationChannels() {
        val salesChannel = NotificationChannel(
            CHANNEL_ID_SALES,
            "Flash Sales",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Flash sale alerts and deals"
        }

        val proximityChannel = NotificationChannel(
            CHANNEL_ID_PROXIMITY,
            "Proximity Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Alerts when 3 Strands is nearby"
        }

        val generalChannel = NotificationChannel(
            CHANNEL_ID_GENERAL,
            "General",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "General announcements"
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(salesChannel)
        manager.createNotificationChannel(proximityChannel)
        manager.createNotificationChannel(generalChannel)
    }

    fun checkAuthorizationStatus() {
        _isAuthorized.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            notificationManager.areNotificationsEnabled()
        }
    }

    fun setAuthorized(authorized: Boolean) {
        _isAuthorized.value = authorized
    }

    suspend fun scheduleTestNotification(sale: String, discount: String) {
        delay(5000) // Wait 5 seconds like iOS version
        if (!hasNotificationPermission()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SALES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("3 Strands Flash Sale!")
            .setContentText("$sale — Save $discount! Limited time only!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(UUID.randomUUID().hashCode(), notification)
        } catch (_: SecurityException) { }
    }

    fun sendNotification(channelId: String, title: String, body: String) {
        if (!hasNotificationPermission()) return

        val priority = if (channelId == CHANNEL_ID_SALES) {
            NotificationCompat.PRIORITY_HIGH
        } else {
            NotificationCompat.PRIORITY_DEFAULT
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(priority)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(UUID.randomUUID().hashCode(), notification)
        } catch (_: SecurityException) { }
    }

    fun sendProximityNotification(eventTitle: String) {
        if (!hasNotificationPermission()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PROXIMITY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("3 Strands Is Nearby!")
            .setContentText("We're at $eventTitle right now — stop by and say hello!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(UUID.randomUUID().hashCode(), notification)
        } catch (_: SecurityException) { }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
