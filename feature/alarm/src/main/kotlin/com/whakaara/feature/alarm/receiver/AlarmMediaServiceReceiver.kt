package com.whakaara.feature.alarm.receiver

import android.content.Context
import android.content.Intent
import com.whakaara.core.HiltBroadcastReceiver
import com.whakaara.core.LogUtils.logD
import com.whakaara.core.constants.NotificationUtilsConstants
import com.whakaara.core.goAsync
import com.whakaara.data.alarm.AlarmRepository
import com.whakaara.feature.alarm.service.AlarmMediaService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmMediaServiceReceiver : HiltBroadcastReceiver() {

    @Inject
    lateinit var repository: AlarmRepository

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val alarmId = intent.getStringExtra(NotificationUtilsConstants.INTENT_ALARM_ID) ?: return
        val alarmType = intent.getIntExtra(NotificationUtilsConstants.NOTIFICATION_TYPE, -1)
        if (alarmType == -1) return

        if (alarmType == NotificationUtilsConstants.NOTIFICATION_TYPE_LOCATION_ALARM) {
            context.stopService(Intent(context, AlarmMediaService::class.java))
            return
        }
        goAsync {
            try {
                repository.triggerDeleteAlarmById(alarmId)
                context.stopService(Intent(context, AlarmMediaService::class.java))
            } catch (exception: Exception) {
                logD(message = "failed to delete alarm: $alarmId", throwable = exception)
            }
        }
    }
}
