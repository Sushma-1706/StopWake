package com.example.stopwake.di

import android.content.Context
import androidx.room.Room
import com.example.stopwake.data.local.StopDao
import com.example.stopwake.data.local.StopWakeDatabase
import com.example.stopwake.data.local.dao.HistoryDao
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideStopWakeDatabase(@ApplicationContext context: Context): StopWakeDatabase {
        return Room.databaseBuilder(
            context,
            StopWakeDatabase::class.java,
            "stop_wake_fused_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideStopDao(database: StopWakeDatabase): StopDao {
        return database.stopDao()
    }

    @Provides
    @Singleton
    fun provideHistoryDao(database: StopWakeDatabase): HistoryDao {
        return database.historyDao()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}

