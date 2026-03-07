package com.example.stopwake.ui.history

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stopwake.data.local.entity.HistoryEntity
import com.example.stopwake.data.local.entity.StopEntity
import com.example.stopwake.domain.repository.HistoryRepository
import com.example.stopwake.domain.repository.StopRepository
import com.example.stopwake.service.StopWakeService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val stopRepository: StopRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val history = historyRepository.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun reuseHistoryItem(item: HistoryEntity) {
        viewModelScope.launch {
            stopRepository.insertStop(
                StopEntity(
                    name = item.stopName,
                    latitude = item.latitude,
                    longitude = item.longitude,
                    radiusMeters = 500f,
                    alertType = item.alertType,
                    isActive = true,
                    userId = item.userId,
                    createdAt = System.currentTimeMillis()
                )
            )

            Intent(context, StopWakeService::class.java).also {
                it.action = StopWakeService.ACTION_START
                ContextCompat.startForegroundService(context, it)
            }
        }
    }
}
