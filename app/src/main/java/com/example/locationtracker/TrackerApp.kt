package com.example.locationtracker

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.locationtracker.commen.AppPreferences
import com.example.locationtracker.realm.LocationInfo
import com.example.locationtracker.realm.UserInfo
import dagger.hilt.android.HiltAndroidApp
import io.realm.kotlin.R
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration


@HiltAndroidApp
class TrackerApp : Application() {

    companion object {
        lateinit var instance: TrackerApp
            private set
        lateinit var realm: Realm
        private const val TAG = "TrackerApp"
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        super.onCreate()
        instance = this
        AppPreferences.init(instance)
        realm = Realm.open(
            configuration = RealmConfiguration.create(
                schema = setOf(
                    UserInfo::class,
                    LocationInfo::class
                )
            )
        )
        /* val channel = NotificationChannel (
            "1212","Locater Service",NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)*/
    }
}