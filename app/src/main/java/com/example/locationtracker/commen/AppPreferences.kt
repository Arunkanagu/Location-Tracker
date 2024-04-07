package com.example.locationtracker.commen

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.locationtracker.TrackerApp

object AppPreferences {

    private lateinit var preferences: SharedPreferences

    private const val SHARED_PREF_NAME = "tracker_preferences"
    private val masterKeyAlias =
        MasterKey.Builder(TrackerApp.instance, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

    private val LOGIN_UUID = Pair("login_uuid", null)

    fun init(context: Context = TrackerApp.instance) {
        preferences = EncryptedSharedPreferences.create(
            context,
            SHARED_PREF_NAME,
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    var loginUuid: String?
        get() = preferences.getString(LOGIN_UUID.first, null)
        set(value) = preferences.edit().putString(LOGIN_UUID.first, value).apply()

}