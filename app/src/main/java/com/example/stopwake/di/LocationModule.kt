package com.example.stopwake.di

import android.content.Context
import com.example.stopwake.data.location.LocationClientImpl
import com.example.stopwake.domain.location.LocationClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideLocationClient(fusedLocationProviderClient: FusedLocationProviderClient, @ApplicationContext context: Context): LocationClient {
        return LocationClientImpl(context, fusedLocationProviderClient)
    }
}
