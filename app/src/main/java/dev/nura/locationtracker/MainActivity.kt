package dev.nura.locationtracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.ServiceCompat.startForeground
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.nura.locationtracker.Utils.hideSystemUI
import dev.nura.locationtracker.databinding.ActivityMainBinding
import dev.nura.locationtracker.service.LocationForegroundService
import dev.nura.locationtracker.service.LocationService
import dev.nura.locationtracker.viewmodal.AppViewModel
import kotlinx.coroutines.DelicateCoroutinesApi


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: AppViewModel by viewModels()
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var navController: NavController

    private lateinit var permissionList: Array<String>

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        window.hideSystemUI()

        navController =
            (supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment).navController

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK, "MainActivity::WakeLock"
        )

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel.userLiveData.observe(this) { userList ->
            userList.forEach { userInfo ->
                Log.i(
                    TAG,
                    "User : ${userInfo.userName}, login : ${userInfo.loginState}, datetime : ${userInfo.lastLoginDate} "
                )
            }
            Log.i(TAG, "__________________________________________________")
        }
        viewModel.locations.observe(this) { loca ->
            loca.forEach { loca ->
                Log.i(
                    TAG,
                    "UserLocation ID: ${loca.userId}, latitude: ${loca.latitude}, longitude: ${loca.longitude}"
                )
            }
            Log.i(TAG, "__________________________________________________")
        }

        checkLocationPermission()

        val serviceIntent = Intent(this, LocationForegroundService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)


    }

    private fun checkLocationPermission() {

        permissionList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION

            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }

        Log.w(TAG, "onCreate: permissionList ${permissionList.size}")
        if (
            permissionList.all {
                ActivityCompat.checkSelfPermission(
                    this,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }

        ) {
            viewModel.haveAllPermissions.postValue(true)
            Log.w(TAG, "checkLocationPermission: Permission granted, perform the action that requires this permission", )
        } else {

            viewModel.haveAllPermissions.postValue(false)
            ActivityCompat.requestPermissions(
                this,
                permissionList,
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }


        viewModel.haveAllPermissions.observe(this){
            if (it){
                restartService()
            }
        }
    }




    override fun onPause() {
        Log.d(TAG, "onPause: ")
        super.onPause()/*val serviceIntent = Intent(this, LocationService::class.java)
        stopService(serviceIntent)*/
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        try {
            wakeLock.acquire(60 * 60 * 1000L)
        } catch (e: Exception) {
            Log.e(TAG, "onResume: ${e.message}")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.any { it!= PackageManager.PERMISSION_GRANTED }) {
                    //
                    viewModel.haveAllPermissions.postValue(false)
                    Log.w(TAG, "onRequestPermissionsResult: Permission denied, handle this scenario (show a message, etc.)" )
                    openAppSettings1()
                } else {
                    //
                    viewModel.haveAllPermissions.postValue(true)
                    Log.w(TAG, "onRequestPermissionsResult: Permission granted, perform the action that requires this permission", )
                }
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent().apply {
            action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun openAppSettings1() {
        val intent = Intent().apply {
            action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
    }



    fun restartService() {
      /*  val serviceIntent = Intent(TrackerApp.instance,LocationService::class.java)
        if (!LocationService().isRunning()) {
            startService(serviceIntent)

        } else {
            stopService(serviceIntent)
            startService(serviceIntent)
        }*/
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 2002
    }
}