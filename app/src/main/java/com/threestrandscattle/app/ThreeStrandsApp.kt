package com.threestrandscattle.app

import android.app.Application
import android.provider.Settings
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.threestrandscattle.app.services.ApiService
import com.threestrandscattle.app.services.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

                val deviceId = Settings.Secure.getString(
                    contentResolver,
                    Settings.Secure.ANDROID_ID
                )
                val deviceName = android.os.Build.MODEL
                ApiService.getInstance(this@ThreeStrandsApp)
                    .registerDevice(token, deviceId, deviceName)
                Log.d(TAG, "Device registered with backend")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get FCM token or register device: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "ThreeStrandsApp"
    }
}
