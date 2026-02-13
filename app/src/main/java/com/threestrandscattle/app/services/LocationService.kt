package com.threestrandscattle.app.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationService private constructor(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation

    private val _isAuthorized = MutableStateFlow(false)
    val isAuthorized: StateFlow<Boolean> = _isAuthorized

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
}
