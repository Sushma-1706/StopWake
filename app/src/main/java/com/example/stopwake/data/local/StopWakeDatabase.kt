package com.example.stopwake.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.stopwake.data.local.dao.HistoryDao
import com.example.stopwake.data.local.entity.HistoryEntity
import com.example.stopwake.data.local.entity.StopEntity

@Database(
    entities = [StopEntity::class, HistoryEntity::class],
    version = 3,
    exportSchema = false
)
abstract class StopWakeDatabase : RoomDatabase() {
    abstract fun stopDao(): StopDao
    abstract fun historyDao(): HistoryDao
}

