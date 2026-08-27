package com.whakaara.feature.alarm.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.whakaara.data.location.GeofenceManager
import com.whakaara.data.location.LocationAlarmRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class GeofenceRefreshWorker(
  context: Context,
  params: WorkerParameters
) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result {
    return try {
      val entryPoint = EntryPointAccessors.fromApplication(
        applicationContext,
        GeofenceRefreshWorkerEntryPoint::class.java
      )
      entryPoint.geofenceManager().recreateGeofences(
        entryPoint.locationAlarmRepository().getEnabledLocationAlarms()
      )
      Result.success()
    } catch (exception: Exception) {
      Result.retry()
    }
  }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface GeofenceRefreshWorkerEntryPoint {
  fun geofenceManager(): GeofenceManager
  fun locationAlarmRepository(): LocationAlarmRepository
}
