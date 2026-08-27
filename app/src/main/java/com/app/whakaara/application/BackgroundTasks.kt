package com.app.whakaara.application

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.whakaara.data.location.GeofenceManager
import com.whakaara.data.location.LocationAlarmRepository
import com.whakaara.feature.alarm.scheduler.AlarmScheduler
import com.whakaara.feature.alarm.worker.GeofenceRefreshWorker
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

object BackgroundTasks {
  private const val GEOFENCE_REFRESH_WORK = "geofence_refresh"

  fun schedule(context: Context) {
    val refreshRequest = PeriodicWorkRequestBuilder<GeofenceRefreshWorker>(12, TimeUnit.HOURS)
      .build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
      GEOFENCE_REFRESH_WORK,
      ExistingPeriodicWorkPolicy.KEEP,
      refreshRequest
    )
  }

  fun refreshOnAppStart(context: Context) {
    val entryPoint = EntryPointAccessors.fromApplication(context, BackgroundTasksEntryPoint::class.java)
    runBlocking(Dispatchers.IO) {
      entryPoint.alarmScheduler().recreateEnabledAlarms()
      entryPoint.geofenceManager().recreateGeofences(
        entryPoint.locationAlarmRepository().getEnabledLocationAlarms()
      )
    }
  }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BackgroundTasksEntryPoint {
  fun alarmScheduler(): AlarmScheduler
  fun geofenceManager(): GeofenceManager
  fun locationAlarmRepository(): LocationAlarmRepository
}
