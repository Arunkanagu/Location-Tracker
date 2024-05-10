package dev.nura.locationtracker.service

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.*
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dev.nura.locationtracker.R
import dev.nura.locationtracker.TrackerApp
import dev.nura.locationtracker.commen.AppPreferences
import dev.nura.locationtracker.realm.LocationInfo
import io.realm.kotlin.UpdatePolicy
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class LocationForegroundService : Service() {


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var isServiceRunning = false
    private lateinit var permissions: Array<String>
    override fun onCreate() {
        super.onCreate()

        startForeground(1, createNotification())
        Log.d(TAG, "Service onCreate")
        isServiceRunning = true
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(TrackerApp.instance)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setIntervalMillis(INTERVAL_MILLIS) // Sets the interval for location updates
            .setMinUpdateIntervalMillis(INTERVAL_MILLIS / 2) // Sets the fastest allowed interval of location updates.
            .setWaitForAccurateLocation(false) // Want Accurate location updates make it true or you get approximate updates
            .setMaxUpdateDelayMillis(10) // Sets the longest a location update may be delayed.
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val id = AppPreferences.loginUuid
                Log.d(TAG, "onLocationResult: AppPreferences Id $id")
                if (id != null) {
                    locationResult.locations.forEach {
                        Log.d(
                            TAG,
                            "onLocationResult: List Item ${it.latitude},${it.longitude},${it.altitude},"
                        )
                        CoroutineScope(Dispatchers.Main).launch {
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun requestNotificationPermission() {
        // You can open notification settings screen to let the user grant permission
        val intent = Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }
    private fun isNotificationPermissionGranted(): Boolean {
        // Check if the app has been granted notification permission
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.areNotificationsEnabled()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        Log.d(TAG, "Service onDestroy")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createNotification(): Notification {
        val channelId =
            createNotificationChannel("my_service", "My Background Service")

        val notificationBuilder = Notification.Builder(this, channelId)
            .setContentTitle("My Foreground Service")
            .setContentText("Running")
            .setSmallIcon(R.mipmap.ic_launcher)
        return notificationBuilder.build()
    }

    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        return channelId
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
        Log.w(
            TAG, "startLocationUpdates: ${
                permissions.all {
                    checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
                }
            }"
        )
        if (permissions.any {
                checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
            }
        ) {
            Log.e(TAG, "startLocationUpdates: PERMISSION NOT GRANTED")
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )


        Log.e(TAG, "startLocationUpdates: PERMISSION GRANTED")
    }

    fun isRunning(): Boolean {
        return isServiceRunning
    }


    fun isInternetAvailable(context: Context = TrackerApp.instance): Boolean {
        var result = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
        return result
    }


    companion object {
        private val TAG = "LocationForegroundService"
        private const val INTERVAL_MILLIS: Long = 900000
    }
}
