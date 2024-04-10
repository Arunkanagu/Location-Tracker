package dev.nura.locationtracker.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dev.nura.locationtracker.TrackerApp
import dev.nura.locationtracker.commen.AppPreferences
import dev.nura.locationtracker.realm.LocationInfo
import io.realm.kotlin.UpdatePolicy
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback //15 minutes in milliseconds
    private var isServiceRunning = false

    private lateinit var permissions: Array<String>
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        isServiceRunning = true
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(TrackerApp.instance)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setIntervalMillis(INTERVAL_MILLIS) // Sets the interval for location updates
            .setMinUpdateIntervalMillis(INTERVAL_MILLIS / 2) // Sets the fastest allowed interval of location updates.
            .setWaitForAccurateLocation(true) // Want Accurate location updates make it true or you get approximate updates
            .setMaxUpdateDelayMillis(10) // Sets the longest a location update may be delayed.
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val id = AppPreferences.loginUuid
                Log.d(TAG, "onLocationResult: AppPreferences Id $id")
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
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        }
        Log.w(TAG, "startLocationUpdates: ${permissions.all {
            checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }}" )
        if (permissions.any {
                checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
            }
        ) {
            Log.e(TAG, "startLocationUpdates: PERMISSION NOT GRANTED" )
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )


        Log.e(TAG, "startLocationUpdates: PERMISSION GRANTED" )
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
        private const val INTERVAL_MILLIS :Long= 300000//900000
    }
}