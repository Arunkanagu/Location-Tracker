package com.example.locationtracker

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.locationtracker.TrackerApp.Companion.realm
import com.example.locationtracker.Utils.hideSystemUI
import com.example.locationtracker.databinding.ActivityMainBinding
import com.example.locationtracker.realm.UserInfo
import com.example.locationtracker.service.LocationService
import com.example.locationtracker.viewmodal.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.UpdatePolicy
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: AppViewModel by viewModels()
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var navController: NavController
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                0
            )
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel.userLiveData.observe(this) { userList ->
            userList.forEach { userInfo ->
                Log.i(
                    TAG,
                    "User ID: ${userInfo._id}, Name: ${userInfo.userName}, Email: ${userInfo.userEmail}, Password: ${userInfo.password}"
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
        GlobalScope.launch {
            realm.write {
                val userInfo = UserInfo().apply {
                    userEmail = "arun@gamil.com"
                    userName = "arun"
                    password = "12341234"
                }
                copyToRealm(userInfo, updatePolicy = UpdatePolicy.ALL)
            }
        }

    }

    override fun onPause() {
        super.onPause()
        val serviceIntent = Intent(this, LocationService::class.java)
        stopService(serviceIntent)
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    override fun onResume() {
        super.onResume()
        val serviceIntent = Intent(this, LocationService::class.java)
        startService(serviceIntent)
        wakeLock.acquire(60 * 60 * 1000L /*10 minutes*/)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}