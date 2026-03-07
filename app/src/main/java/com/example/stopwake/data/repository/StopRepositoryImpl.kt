package com.example.stopwake.data.repository

import com.example.stopwake.data.local.StopDao
import com.example.stopwake.data.local.entity.StopEntity
import com.example.stopwake.domain.repository.StopRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StopRepositoryImpl @Inject constructor(
    private val dao: StopDao
) : StopRepository {

    override fun getAllStops(): Flow<List<StopEntity>> {
        return dao.getAllStops()
    }

    override fun getActiveStops(): Flow<List<StopEntity>> {
        return dao.getActiveStops()
    }

    override suspend fun insertStop(stop: StopEntity) {
        dao.insertStop(stop)
    }

    override suspend fun insertStop(
        name: String,
        latitude: Double,
        longitude: Double,
        radiusMeters: Float,
        alertType: String,
        userId: String,
        contactNumber: String?,
        alertMessage: String?
    ) {
        val stop = StopEntity(
            name = name,
            latitude = latitude,
            longitude = longitude,
            radiusMeters = radiusMeters,
            alertType = alertType,
            userId = userId,
            contactNumber = contactNumber,
            alertMessage = alertMessage,
            isActive = true
        )
        dao.insertStop(stop)
    }

    override suspend fun updateStop(stop: StopEntity) {
        dao.updateStop(stop)
    }

    override suspend fun deleteStop(stop: StopEntity) {
        dao.deleteStop(stop)
    }

    override suspend fun getStopById(id: Long): StopEntity? {
        return dao.getStopById(id)
    }
}

