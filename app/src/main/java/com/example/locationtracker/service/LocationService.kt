package com.example.locationtracker.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.locationtracker.MainActivity
import com.example.locationtracker.TrackerApp
import com.example.locationtracker.commen.AppPreferences
import com.example.locationtracker.realm.LocationInfo
import com.example.locationtracker.realm.UserInfo
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.realm.kotlin.UpdatePolicy
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback //15 minutes in milliseconds
    private var isServiceRunning = false

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        isServiceRunning = true
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
            .setIntervalMillis(INTERVAL_MILLIS) // Sets the interval for location updates
            .setMinUpdateIntervalMillis(INTERVAL_MILLIS / 2) // Sets the fastest allowed interval of location updates.
            .setWaitForAccurateLocation(false) // Want Accurate location updates make it true or you get approximate updates
            .setMaxUpdateDelayMillis(10) // Sets the longest a location update may be delayed.
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val id = AppPreferences.loginUuid
                if (id != null){
                    locationResult.locations.forEach {
                        Log.d(TAG, "onLocationResult: List Item ${it.latitude},${it.longitude},${it.altitude},")
                        GlobalScope.launch {
                            TrackerApp.realm.write {
                                val locationInfo = LocationInfo().apply {
                                    userId = id
                                    latitude = it.latitude
                                    longitude = it.longitude
                                }
                                copyToRealm(locationInfo, updatePolicy = UpdatePolicy.ALL)
                            }
                        }
                    }
                }
            }
        }
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        Log.d(TAG, "Service onDestroy")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun isRunning(): Boolean {return isServiceRunning}

    companion object {
        private const val TAG = "LocationService"
        private const val INTERVAL_MILLIS :Long= 900000
    }
}