package com.whakaara.feature.alarm.receiver

import android.content.Context
import android.content.Intent
import com.whakaara.core.HiltBroadcastReceiver
import com.whakaara.data.location.GeofenceManager
import com.whakaara.data.location.LocationAlarmRepository
import com.whakaara.feature.alarm.scheduler.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class RecreateAlarmsReceiver : HiltBroadcastReceiver() {
    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var locationAlarmRepository: LocationAlarmRepository

    @Inject
    lateinit var geofenceManager: GeofenceManager

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        super.onReceive(context, intent)
        val actionsList =
            listOf(
                "android.intent.action.DATE_CHANGED",
                "android.intent.action.TIME_SET",
                "android.intent.action.TIMEZONE_CHANGED",
                "android.intent.action.BOOT_COMPLETED",
                "android.intent.action.LOCKED_BOOT_COMPLETED",
                "android.intent.action.QUICKBOOT_POWERON",
                "android.intent.action.MY_PACKAGE_REPLACED",
                "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
            )
        if (!actionsList.contains(intent.action)) return

        runBlocking(Dispatchers.IO) {
            alarmScheduler.recreateEnabledAlarms()
            geofenceManager.recreateGeofences(locationAlarmRepository.getEnabledLocationAlarms())
        }
    }
}
