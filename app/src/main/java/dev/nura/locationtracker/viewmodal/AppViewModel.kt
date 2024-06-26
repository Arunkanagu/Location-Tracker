package dev.nura.locationtracker.viewmodal

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nura.locationtracker.TrackerApp.Companion.realm
import dev.nura.locationtracker.commen.AppPreferences
import dev.nura.locationtracker.realm.LocationInfo
import dev.nura.locationtracker.realm.UserInfo
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AppViewModel @Inject constructor() : ViewModel() {

    val userLiveData = realm.query<UserInfo>().asFlow()
        .map { r -> r.list.toList() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )
        .asLiveData()

    val locations = realm.query<LocationInfo>()
        .asFlow()
        .map { r -> r.list.toList() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )
        .asLiveData()

    val locationsWithFilter = realm.query<LocationInfo>("userId == $0" , AppPreferences.loginUuid.toString())
        .asFlow()
        .map { r -> r.list.toList() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )
        .asLiveData()

    fun insertUser(data: UserInfo) {

        viewModelScope.launch {
            realm.write {
                copyToRealm(data, updatePolicy = UpdatePolicy.ALL)
            }
        }
    }

    fun deleteCourse(data: UserInfo) {
        viewModelScope.launch {
            realm.write {
                val user = data ?: return@write
                val latestUser = findLatest(user) ?: return@write
                delete(latestUser)
            }
        }
    }

    fun updateCourse(data: UserInfo) {
        viewModelScope.launch {
            val findData: UserInfo =
                realm.query<UserInfo>("_id == $0", data._id).find().first()
            Log.d(TAG, "updateCourse: $findData")
            realm.write {
                findLatest(findData)?.let { livedata ->
                    livedata.loginState = data.loginState
                    livedata.lastLoginDate = data.lastLoginDate
                }
            }
        }
    }

    var localLocationInfo = MutableLiveData<LocationInfo>()
    var haveAllPermissions = MutableLiveData<Boolean>(false)
    var localUserInfo = UserInfo()


    companion object{
        private val TAG = "AppViewModel"
    }
}