package com.whakaara.data.di

import com.whakaara.core.location.GeocodingService
import com.whakaara.data.location.LocationAlarmRepository
import com.whakaara.data.location.LocationAlarmRepositoryImpl
import com.whakaara.data.location.NominatimApi
import com.whakaara.data.location.NominatimGeocodingService
import com.whakaara.database.alarm.LocationAlarmDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LocationModule {

    @Provides
    @Singleton
    fun provideNominatimApi(): NominatimApi {
        return Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGeocodingService(api: NominatimApi): GeocodingService {
        return NominatimGeocodingService(api)
    }

    @Provides
    @Singleton
    fun provideLocationAlarmRepository(
        locationAlarmDao: LocationAlarmDao
    ): LocationAlarmRepository = LocationAlarmRepositoryImpl(locationAlarmDao)
}
