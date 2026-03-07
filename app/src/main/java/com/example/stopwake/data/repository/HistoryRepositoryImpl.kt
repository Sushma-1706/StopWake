package com.example.stopwake.data.repository

import com.example.stopwake.data.local.dao.HistoryDao
import com.example.stopwake.data.local.entity.HistoryEntity
import com.example.stopwake.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao
) : HistoryRepository {
    override fun getAllHistory(): Flow<List<HistoryEntity>> = historyDao.getAllHistory()

    override suspend fun insertHistory(history: HistoryEntity) {
        historyDao.insert(history)
    }
}
