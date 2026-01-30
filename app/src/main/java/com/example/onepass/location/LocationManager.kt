package com.example.onepass.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class LocationManager(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)

    fun getCurrentLocation(onResult: (String?) -> Unit) {
        if (!hasLocationPermission()) {
            onResult(null)
            return
        }

        try {
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    val city = getCityFromCoordinates(location.latitude, location.longitude)
                    onResult(city)
                } else {
                    onResult(null)
                }
            }.addOnFailureListener {
                onResult(null)
            }
        } catch (e: Exception) {
            onResult(null)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getCityFromCoordinates(lat: Double, lon: Double): String {
        val geocoder = android.location.Geocoder(context, java.util.Locale.CHINA)
        try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val locality = address.locality
                val adminArea = address.adminArea
                return when {
                    !locality.isNullOrEmpty() -> locality
                    !adminArea.isNullOrEmpty() -> adminArea
                    else -> "Beijing"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "Beijing"
    }
}
