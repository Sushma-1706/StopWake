package com.example.stopwake.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val stopName: String,
    val latitude: Double,
    val longitude: Double,
    val alertType: String,
    val triggeredAt: Long = System.currentTimeMillis(),
    val userId: String? = null
)
