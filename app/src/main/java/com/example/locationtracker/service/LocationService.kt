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
import com.example.locationtracker.TrackerApp
import com.example.locationtracker.commen.AppPreferences
import com.example.locationtracker.realm.LocationInfo
import com.example.locationtracker.realm.UserInfo
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.realm.kotlin.UpdatePolicy
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var handler: Handler
    private val interval: Long = 1 * 1 * 10 * 1000 // 15 minutes in milliseconds
    private var isServiceRunning = false

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        isServiceRunning = true
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        handler = android.os.Handler(Looper.getMainLooper())
        handler.postDelayed(locationRunnable, interval)
    }


    private val locationRunnable = object : Runnable {
        override fun run() {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "PERMISSION_NOT_GRANTED")
                return
            }


            Log.d(TAG, "PERMISSION_GRANTED")
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    // Print the location here
                    var loginID = AppPreferences.loginUuid
                    Log.d(
                        TAG,
                        "loginID: $loginID Latitude: ${it.latitude}, Longitude: ${it.longitude}"
                    )
                    GlobalScope.launch() {
                        if (loginID != null) {
                            TrackerApp.realm.write {
                                val locationInfo = LocationInfo().apply {
                                    userId = loginID
                                    latitude = it.latitude
                                    longitude = it.longitude
                                }
                                copyToRealm(locationInfo, updatePolicy = UpdatePolicy.ALL)
                            }
                        }
                    }

                }
            }
            handler.postDelayed(this, interval)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        Log.d(TAG, "Service onDestroy")
        handler.removeCallbacks(locationRunnable)
    }

    companion object {
        private const val TAG = "LocationService"
    }
}