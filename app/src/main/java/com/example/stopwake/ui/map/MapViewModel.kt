package com.example.stopwake.ui.map

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stopwake.domain.location.LocationClient
import com.example.stopwake.domain.repository.StopRepository
import com.example.stopwake.service.StopWakeService
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: StopRepository,
    private val locationClient: LocationClient,
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    init {
        getLocationUpdates()
    }

    private fun getLocationUpdates() {
        viewModelScope.launch {
            locationClient.getLocationUpdates(5000L) // Update every 5s
                .catch { e -> e.printStackTrace() }
                .collect { location ->
                    if (_state.value.currentLocation == null) { // Only update once to user's initial location
                        _state.value = _state.value.copy(
                            currentLocation = LatLng(location.latitude, location.longitude)
                        )
                    }
                }
        }
    }

    fun addStop(
        name: String,
        position: LatLng,
        alertType: String,
        radius: Float,
        contactNumber: String?,
        alertMessage: String?
    ) {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.uid ?: "anonymous"
            
            repository.insertStop(
                name = name,
                latitude = position.latitude,
                longitude = position.longitude,
                radiusMeters = radius,
                alertType = alertType,
                userId = userId,
                contactNumber = contactNumber,
                alertMessage = alertMessage
            )
            startTracking()
        }
    }

    private fun startTracking() {
        Intent(context, StopWakeService::class.java).also {
            it.action = StopWakeService.ACTION_START
            ContextCompat.startForegroundService(context, it)
        }
    }
}

data class MapState(
    val currentLocation: LatLng? = null
)
