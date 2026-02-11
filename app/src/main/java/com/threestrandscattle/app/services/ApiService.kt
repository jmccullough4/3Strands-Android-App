package com.threestrandscattle.app.services

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.threestrandscattle.app.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class ApiService private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("api_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    val dashboardURL: String
        get() = prefs.getString("api_base_url", "https://dashboard.3strands.co") ?: "https://dashboard.3strands.co"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Flash Sales
    suspend fun fetchFlashSales(): List<FlashSale> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$dashboardURL/api/public/flash-sales")
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw ApiException("Server error: ${response.code}")
        val body = response.body?.string() ?: "[]"
        val type = object : TypeToken<List<ApiFlashSale>>() {}.type
        val apiSales: List<ApiFlashSale> = gson.fromJson(body, type)
        apiSales.map { it.toFlashSale() }
    }

    // Pop-Up Sales
    suspend fun fetchPopUpSales(): List<PopUpSale> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$dashboardURL/api/public/pop-up-sales")
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw ApiException("Server error: ${response.code}")
        val body = response.body?.string() ?: "[]"
        val type = object : TypeToken<List<PopUpSale>>() {}.type
        gson.fromJson(body, type)
    }

    // Announcements
    suspend fun fetchAnnouncements(): List<Announcement> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$dashboardURL/api/public/announcements")
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw ApiException("Server error: ${response.code}")
        val body = response.body?.string() ?: "[]"
        val type = object : TypeToken<List<Announcement>>() {}.type
        gson.fromJson(body, type)
    }

    // Events
    suspend fun fetchEvents(): List<CattleEvent> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$dashboardURL/api/public/events")
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw ApiException("Server error: ${response.code}")
        val body = response.body?.string() ?: "[]"
        val type = object : TypeToken<List<ApiEvent>>() {}.type
        val apiEvents: List<ApiEvent> = gson.fromJson(body, type)
        apiEvents.map { it.toCattleEvent() }
    }

    // Register Device
    suspend fun registerDevice(token: String, deviceId: String, deviceName: String) = withContext(Dispatchers.IO) {
        val json = gson.toJson(mapOf(
            "token" to token,
            "platform" to "android",
            "device_id" to deviceId,
            "device_name" to deviceName
        ))
        val requestBody = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$dashboardURL/api/public/register-device")
            .post(requestBody)
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw ApiException("Registration failed: ${response.code}")
    }

    companion object {
        @Volatile
        private var instance: ApiService? = null

        fun getInstance(context: Context): ApiService {
            return instance ?: synchronized(this) {
                instance ?: ApiService(context.applicationContext).also { instance = it }
            }
        }
    }
}

class ApiException(message: String) : Exception(message)
