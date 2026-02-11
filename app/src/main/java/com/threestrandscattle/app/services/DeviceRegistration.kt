package com.threestrandscattle.app.services

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

object DeviceRegistration {

    private const val TAG = "DeviceRegistration"

    suspend fun register(context: Context, fcmToken: String) {
        withContext(Dispatchers.IO) {
            try {
                val prefs = context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE)

                // Get or create persistent device ID
                var deviceId = prefs.getString("device_id", null)
                if (deviceId == null) {
                    deviceId = UUID.randomUUID().toString()
                    prefs.edit().putString("device_id", deviceId).apply()
                }

                // Get app version
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val appVersion = packageInfo.versionName ?: "Unknown"

                // Get device name (user's device name if available)
                val deviceName = Build.MODEL  // e.g., "Pixel 9 Pro"

                // Get detailed device model
                val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"  // e.g., "Google Pixel 9 Pro"

                // Register with backend
                ApiService.getInstance(context).registerDevice(
                    token = fcmToken,
                    deviceId = deviceId,
                    deviceName = deviceName,
                    osVersion = Build.VERSION.RELEASE,
                    appVersion = appVersion,
                    deviceModel = deviceModel,
                    locale = Locale.getDefault().toString(),
                    timezone = TimeZone.getDefault().id
                )

                Log.d(TAG, "Device registered successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Device registration failed", e)
            }
        }
    }
}
