package com.threestrandscattle.app

import android.app.Application
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.threestrandscattle.app.services.DeviceRegistration
import com.threestrandscattle.app.services.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ThreeStrandsApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Initialize services
        NotificationService.getInstance(this)

        // Get FCM token and register with backend
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Log.d(TAG, "FCM token: $token")
            appScope.launch {
                DeviceRegistration.register(applicationContext, token)
            }
        }
    }

    companion object {
        private const val TAG = "ThreeStrandsApp"
    }
}
