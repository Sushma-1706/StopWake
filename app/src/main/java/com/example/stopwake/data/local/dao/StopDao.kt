package com.example.stopwake.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.stopwake.data.local.entity.StopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StopDao {
    @Query("SELECT * FROM stops ORDER BY createdAt DESC")
    fun getAllStops(): Flow<List<StopEntity>>

    @Query("SELECT * FROM stops WHERE isActive = 1")
    fun getActiveStops(): Flow<List<StopEntity>>

    @Query("SELECT * FROM stops WHERE id = :id")
    suspend fun getStopById(id: Long): StopEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStop(stop: StopEntity)

    @Update
    suspend fun updateStop(stop: StopEntity)

    @Delete
    suspend fun deleteStop(stop: StopEntity)
}
