package com.whakaara.feature.alarm.receiver

import android.content.Context
import android.content.Intent
import com.google.android.gms.location.GeofencingEvent
import com.whakaara.core.HiltBroadcastReceiver
import com.whakaara.core.constants.NotificationUtilsConstants
import com.whakaara.feature.alarm.service.AlarmMediaService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationAlarmReceiver : HiltBroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            return
        }

        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return

        triggeringGeofences.forEach { geofence ->
            val alarmId = geofence.requestId
            val serviceIntent = Intent(context, AlarmMediaService::class.java).apply {
                putExtra(NotificationUtilsConstants.INTENT_ALARM_ID, alarmId)
                putExtra(NotificationUtilsConstants.SERVICE_ACTION, NotificationUtilsConstants.PLAY)
                putExtra(
                    NotificationUtilsConstants.NOTIFICATION_TYPE,
                    NotificationUtilsConstants.NOTIFICATION_TYPE_LOCATION_ALARM
                )
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}
