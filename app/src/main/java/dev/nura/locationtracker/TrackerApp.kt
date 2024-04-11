package dev.nura.locationtracker

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.HiltAndroidApp
import dev.nura.locationtracker.commen.AppPreferences
import dev.nura.locationtracker.realm.LocationInfo
import dev.nura.locationtracker.realm.UserInfo
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