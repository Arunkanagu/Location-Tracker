package com.example.locationtracker.viewmodal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.locationtracker.TrackerApp.Companion.realm
import com.example.locationtracker.realm.LocationInfo
import com.example.locationtracker.realm.UserInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    val locations = realm.query<LocationInfo>().asFlow()
        .map { r -> r.list.toList() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )
        .asLiveData()



}