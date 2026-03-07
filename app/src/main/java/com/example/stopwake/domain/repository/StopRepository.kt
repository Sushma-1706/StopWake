package com.example.stopwake.domain.repository

import com.example.stopwake.data.local.entity.StopEntity
import kotlinx.coroutines.flow.Flow

interface StopRepository {
    fun getAllStops(): Flow<List<StopEntity>>
    fun getActiveStops(): Flow<List<StopEntity>>
    suspend fun insertStop(stop: StopEntity)
    suspend fun insertStop(
        name: String,
        latitude: Double,
        longitude: Double,
        radiusMeters: Float,
        alertType: String,
        userId: String,
        contactNumber: String? = null,
        alertMessage: String? = null
    )
    suspend fun updateStop(stop: StopEntity)
    suspend fun deleteStop(stop: StopEntity)
    suspend fun getStopById(id: Long): StopEntity?
}
