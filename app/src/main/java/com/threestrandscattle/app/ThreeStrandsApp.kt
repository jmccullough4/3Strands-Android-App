package com.threestrandscattle.app

import android.app.Application
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.threestrandscattle.app.services.ApiService
import com.threestrandscattle.app.services.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ThreeStrandsApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Initialize services
        NotificationService.getInstance(this)

        // Get FCM token and register with backend
        appScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                Log.d(TAG, "FCM token: $token")

                val deviceId = getOrCreateDeviceId()
                val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
                ApiService.getInstance(this@ThreeStrandsApp)
                    .registerDevice(token, deviceId, deviceName)
                Log.d(TAG, "Device registered with backend")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get FCM token or register device: ${e.message}")
            }
        }
    }

    private fun getOrCreateDeviceId(): String {
        val prefs = getSharedPreferences("device_prefs", MODE_PRIVATE)
        var deviceId = prefs.getString("device_id", null)
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString("device_id", deviceId).apply()
        }
        return deviceId
    }

    companion object {
        private const val TAG = "ThreeStrandsApp"
    }
}
