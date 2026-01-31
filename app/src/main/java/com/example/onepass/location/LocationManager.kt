package com.example.onepass.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat

class LocationManager(private val context: Context) {
    private val locationManager: LocationManager = 
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun getCurrentLocation(onResult: (String?) -> Unit) {
        if (!hasLocationPermission()) {
            onResult(null)
            return
        }

        try {
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    Log.i("WeatherAPI", "主动定位成功: ${location.latitude}, ${location.longitude}")
                    val city = getCityFromCoordinates(location.latitude, location.longitude)
                    onResult(city)
                    
                    locationManager.removeUpdates(this)
                }

                override fun onProviderEnabled(provider: String) {
                    Log.d("WeatherAPI", "位置提供者已启用: $provider")
                }

                override fun onProviderDisabled(provider: String) {
                    Log.d("WeatherAPI", "位置提供者已禁用: $provider")
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                    Log.d("WeatherAPI", "位置提供者状态改变: $provider, status: $status")
                }
            }

            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                0f,
                locationListener
            )
            
            Log.d("WeatherAPI", "开始请求位置更新")
        } catch (e: SecurityException) {
            Log.e("WeatherAPI", "位置权限不足", e)
            onResult(null)
        } catch (e: Exception) {
            Log.e("WeatherAPI", "获取位置失败", e)
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
