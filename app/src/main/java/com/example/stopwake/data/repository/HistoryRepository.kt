package com.example.stopwake.domain.repository

import com.example.stopwake.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getAllHistory(): Flow<List<HistoryEntity>>
    suspend fun insertHistory(history: HistoryEntity)
}
