package com.example.stopwake.di

import com.example.stopwake.data.repository.AuthRepositoryImpl
import com.example.stopwake.data.repository.StopRepositoryImpl
import com.example.stopwake.domain.repository.AuthRepository
import com.example.stopwake.domain.repository.StopRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStopRepository(
        stopRepositoryImpl: StopRepositoryImpl
    ): StopRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}
