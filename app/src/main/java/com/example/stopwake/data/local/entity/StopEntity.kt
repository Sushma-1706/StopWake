package com.example.stopwake.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AlertType {
    TWO_STOPS_BEFORE,  // Triggers at 2x radius
    ONE_STOP_BEFORE,   // Triggers at 1.5x radius
    ARRIVAL            // Triggers at exact radius
}

@Entity(tableName = "stops")
data class StopEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val isActive: Boolean = true,
    val alertType: String = AlertType.ARRIVAL.name, // Store as String for Room
    val contactNumber: String? = null,
    val alertMessage: String? = null,
    val userId: String? = null,
    val triggeredAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getAlertTypeEnum(): AlertType {
        return try {
            AlertType.valueOf(alertType)
        } catch (e: Exception) {
            AlertType.ARRIVAL
        }
    }
    
    fun getEffectiveRadius(): Float {
        return when (getAlertTypeEnum()) {
            AlertType.TWO_STOPS_BEFORE -> radiusMeters * 2f
            AlertType.ONE_STOP_BEFORE -> radiusMeters * 1.5f
            AlertType.ARRIVAL -> radiusMeters
        }
    }
}
