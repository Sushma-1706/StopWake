package com.example.stopwake.ui.home

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stopwake.data.local.entity.StopEntity
import com.example.stopwake.domain.location.LocationClient
import com.example.stopwake.domain.repository.StopRepository
import com.example.stopwake.service.StopWakeService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: StopRepository,
    private val locationClient: LocationClient,
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val stops = repository.getAllStops()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeStopCount = MutableStateFlow(0)
    val activeStopCount = _activeStopCount.asStateFlow()

    init {
        repository.getActiveStops()
            .onEach { _activeStopCount.value = it.size }
            .launchIn(viewModelScope)
    }

    fun toggleStop(stop: StopEntity) {
        viewModelScope.launch {
            repository.updateStop(stop.copy(isActive = !stop.isActive))
        }
    }

    fun updateStop(stop: StopEntity) {
        viewModelScope.launch {
            repository.updateStop(stop)
        }
    }

    fun deleteStop(stop: StopEntity) {
        viewModelScope.launch {
            repository.deleteStop(stop)
        }
    }

    fun startTracking() {
        Intent(context, StopWakeService::class.java).also {
            it.action = StopWakeService.ACTION_START
            context.startService(it)
        }
    }

    fun createAlertFromCurrentLocation() {
        viewModelScope.launch {
            try {
                val userId = firebaseAuth.currentUser?.uid ?: "anonymous"
                // Get current location once
                locationClient.getLocationUpdates(1000L).collect { location ->
                    // Create a stop at current location with default settings
                    repository.insertStop(
                        name = "Current Location",
                        latitude = location.latitude,
                        longitude = location.longitude,
                        radiusMeters = 500f,
                        alertType = "ARRIVAL",
                        userId = userId,
                        contactNumber = null,
                        alertMessage = null
                    )
                    
                    // Start the tracking service
                    startTracking()
                    
                    // Stop collecting after first location
                    return@collect
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopTracking() {
        Intent(context, StopWakeService::class.java).also {
            it.action = StopWakeService.ACTION_STOP
            context.startService(it)
        }
    }
}
