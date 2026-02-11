package com.threestrandscattle.app.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FCMService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")

        serviceScope.launch {
            try {
                val deviceId = android.provider.Settings.Secure.getString(
                    applicationContext.contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                )
                val deviceName = android.os.Build.MODEL
                ApiService.getInstance(applicationContext)
                    .registerDevice(token, deviceId, deviceName)
                Log.d(TAG, "Device registered with backend")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register device: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM message received: ${message.data}")

        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val type = message.data["type"] ?: "general"

        // Route to appropriate notification channel
        val notificationService = NotificationService.getInstance(applicationContext)

        when (type) {
            "flash_sale" -> {
                notificationService.sendNotification(
                    channelId = NotificationService.CHANNEL_ID_SALES,
                    title = title,
                    body = body
                )
            }
            "proximity" -> {
                notificationService.sendNotification(
                    channelId = NotificationService.CHANNEL_ID_PROXIMITY,
                    title = title,
                    body = body
                )
            }
            else -> {
                notificationService.sendNotification(
                    channelId = NotificationService.CHANNEL_ID_GENERAL,
                    title = title,
                    body = body
                )
            }
        }
    }

    companion object {
        private const val TAG = "FCMService"
    }
}
