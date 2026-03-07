package com.example.stopwake.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.stopwake.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(history: HistoryEntity)
    
    @Query("SELECT * FROM history ORDER BY triggeredAt DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>
    
    @Query("SELECT * FROM history WHERE userId = :userId ORDER BY triggeredAt DESC")
    fun getHistoryByUser(userId: String): Flow<List<HistoryEntity>>
    
    @Query("DELETE FROM history WHERE id = :id")
    suspend fun delete(id: Long)
    
    @Query("DELETE FROM history")
    suspend fun deleteAll()
}
