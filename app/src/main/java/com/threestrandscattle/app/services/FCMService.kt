package com.threestrandscattle.app.services

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.threestrandscattle.app.models.InboxItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FCMService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")

        // Register device with new token
        serviceScope.launch {
            DeviceRegistration.register(applicationContext, token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM message received: ${message.data}")

        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val type = message.data["type"] ?: "general"

        // Add to inbox (persisted in SharedPreferences, picked up by SaleStore)
        addToInbox(applicationContext, title, body)

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
        private val gson = Gson()

        /**
         * Persists an inbox item to SharedPreferences so SaleStore can pick it up.
         * Uses the same key/format as SaleStore for compatibility.
         */
        fun addToInbox(context: Context, title: String, body: String) {
            val prefs = context.getSharedPreferences("sale_store", Context.MODE_PRIVATE)
            val inboxKey = "notification_inbox"

            val existing: List<InboxItem> = try {
                val json = prefs.getString(inboxKey, null)
                if (json != null) {
                    val type = object : TypeToken<List<InboxItem>>() {}.type
                    gson.fromJson(json, type)
                } else emptyList()
            } catch (_: Exception) {
                emptyList()
            }

            val newItem = InboxItem(title = title, body = body)
            val updated = listOf(newItem) + existing
            prefs.edit().putString(inboxKey, gson.toJson(updated)).apply()
        }
    }
}
