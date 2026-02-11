package com.threestrandscattle.app.services

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.threestrandscattle.app.models.CattleEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationService private constructor(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val geofencingClient: GeofencingClient =
        LocationServices.getGeofencingClient(context)

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation

    private val _isAuthorized = MutableStateFlow(false)
    val isAuthorized: StateFlow<Boolean> = _isAuthorized

    private val alertRadius = 500f // meters

    companion object {
        @Volatile
        private var instance: LocationService? = null

        fun getInstance(context: Context): LocationService {
            return instance ?: synchronized(this) {
                instance ?: LocationService(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        checkPermission()
    }

    fun checkPermission() {
        _isAuthorized.value = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun setAuthorized(authorized: Boolean) {
        _isAuthorized.value = authorized
        if (authorized) {
            requestLastLocation()
        }
    }

    private fun requestLastLocation() {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            _userLocation.value = location
        }
    }

    fun startMonitoringEvents(events: List<CattleEvent>) {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        // Remove existing geofences
        geofencingClient.removeGeofences(getGeofencePendingIntent())

        // Monitor unique locations
        val seen = mutableSetOf<String>()
        val geofences = mutableListOf<Geofence>()

        for (event in events) {
            val key = "${event.latitude},${event.longitude}"
            if (seen.contains(key)) continue
            seen.add(key)

            geofences.add(
                Geofence.Builder()
                    .setRequestId(event.title)
                    .setCircularRegion(event.latitude, event.longitude, alertRadius)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()
            )
        }

        if (geofences.isEmpty()) return

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()

        try {
            geofencingClient.addGeofences(request, getGeofencePendingIntent())
        } catch (_: SecurityException) { }
    }

    private fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
}

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationService = NotificationService.getInstance(context)
        val geofencingEvent = com.google.android.gms.location.GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) return

        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return
        for (geofence in triggeringGeofences) {
            notificationService.sendProximityNotification(geofence.requestId)
        }
    }
}
