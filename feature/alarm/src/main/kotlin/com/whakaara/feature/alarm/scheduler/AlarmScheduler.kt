package com.whakaara.feature.alarm.scheduler

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import com.whakaara.core.PendingIntentUtils
import com.whakaara.core.WidgetUpdater
import com.whakaara.core.constants.GeneralConstants.MAIN_ACTIVITY
import com.whakaara.core.constants.NotificationUtilsConstants
import com.whakaara.data.alarm.AlarmRepository
import com.whakaara.data.preferences.PreferencesRepository
import com.whakaara.feature.alarm.receiver.UpcomingAlarmReceiver
import com.whakaara.feature.alarm.service.AlarmMediaService
import com.whakaara.feature.alarm.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
  private val context: Application,
  private val alarmManager: AlarmManager,
  private val alarmRepository: AlarmRepository,
  private val preferencesRepository: PreferencesRepository,
  private val widgetUpdater: WidgetUpdater
) {
  suspend fun recreateEnabledAlarms() = withContext(Dispatchers.IO) {
    if (!alarmManager.canScheduleExactAlarms()) return@withContext
    val preferences = preferencesRepository.getPreferences()
    val enabledAlarms = alarmRepository.getAllAlarms().filter { it.isEnabled }
    enabledAlarms.forEach { alarm ->
      scheduleAlarm(
        alarmId = alarm.alarmId.toString(),
        autoSilenceTime = preferences.autoSilenceTime.value,
        date = alarm.date,
        upcomingAlarmNotificationEnabled = preferences.upcomingAlarmNotification,
        upcomingAlarmNotificationTime = preferences.upcomingAlarmNotificationTime.value,
        repeatAlarmDaily = alarm.repeatDaily,
        daysOfWeek = alarm.daysOfWeek
      )
    }
    widgetUpdater.updateWidget()
  }

  private fun scheduleAlarm(
    alarmId: String,
    date: Calendar,
    autoSilenceTime: Int,
    upcomingAlarmNotificationEnabled: Boolean,
    upcomingAlarmNotificationTime: Int,
    repeatAlarmDaily: Boolean,
    daysOfWeek: MutableList<Int>
  ) {
    setAlarm(
      alarmId = alarmId,
      autoSilenceTime = autoSilenceTime,
      date = date,
      repeatAlarmDaily = repeatAlarmDaily,
      daysOfWeek = daysOfWeek
    )
    setUpcomingAlarm(
      alarmId = alarmId,
      alarmDate = date,
      upcomingAlarmNotificationEnabled = upcomingAlarmNotificationEnabled,
      upcomingAlarmNotificationTime = upcomingAlarmNotificationTime,
      repeatAlarmDaily = repeatAlarmDaily,
      daysOfWeek = daysOfWeek
    )
  }

  private fun setAlarm(
    alarmId: String,
    autoSilenceTime: Int,
    date: Calendar,
    repeatAlarmDaily: Boolean,
    daysOfWeek: MutableList<Int>
  ) {
    val triggerTime = DateUtils.getTimeAsDate(alarmDate = date)
    val startReceiverIntent = Intent(context, AlarmMediaService::class.java).apply {
      action = alarmId
      putExtra(NotificationUtilsConstants.INTENT_AUTO_SILENCE, autoSilenceTime)
      putExtra(NotificationUtilsConstants.SERVICE_ACTION, NotificationUtilsConstants.PLAY)
      putExtra(NotificationUtilsConstants.NOTIFICATION_TYPE, NotificationUtilsConstants.NOTIFICATION_TYPE_ALARM)
      putExtra(NotificationUtilsConstants.INTENT_ALARM_ID, alarmId)
    }
    val alarmPendingIntent = PendingIntentUtils.getService(
      context,
      NotificationUtilsConstants.INTENT_REQUEST_CODE,
      startReceiverIntent,
      PendingIntent.FLAG_UPDATE_CURRENT
    )
    val deepLinkIntent = Intent(Intent.ACTION_VIEW).apply {
      setClassName(context.packageName, MAIN_ACTIVITY)
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val alarmInfoPendingIntent = PendingIntentUtils.getActivity(
      context,
      NotificationUtilsConstants.INTENT_REQUEST_CODE,
      deepLinkIntent,
      PendingIntent.FLAG_UPDATE_CURRENT
    )
    if (repeatAlarmDaily || daysOfWeek.isNotEmpty()) {
      alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
        triggerTime.timeInMillis,
        AlarmManager.INTERVAL_DAY,
        alarmPendingIntent
      )
    } else {
      alarmManager.setAlarmClock(
        AlarmManager.AlarmClockInfo(triggerTime.timeInMillis, alarmInfoPendingIntent),
        alarmPendingIntent
      )
    }
  }

  private fun setUpcomingAlarm(
    alarmId: String,
    alarmDate: Calendar,
    upcomingAlarmNotificationEnabled: Boolean,
    upcomingAlarmNotificationTime: Int,
    repeatAlarmDaily: Boolean,
    daysOfWeek: MutableList<Int>
  ) {
    val triggerTime = DateUtils.getTimeAsDate(alarmDate = alarmDate)
    val triggerTimeMinusNotification = (triggerTime.clone() as Calendar).apply {
      add(Calendar.MINUTE, -upcomingAlarmNotificationTime)
    }
    val upcomingAlarmIntent = Intent(context, UpcomingAlarmReceiver::class.java).apply {
      action = alarmId
      putExtra(
        NotificationUtilsConstants.UPCOMING_ALARM_INTENT_ACTION,
        NotificationUtilsConstants.UPCOMING_ALARM_RECEIVER_ACTION_START
      )
      putExtra(NotificationUtilsConstants.UPCOMING_ALARM_INTENT_TRIGGER_TIME, triggerTime.timeInMillis)
    }
    val pendingIntent = PendingIntentUtils.getBroadcast(
      context = context,
      id = NotificationUtilsConstants.INTENT_REQUEST_CODE,
      intent = upcomingAlarmIntent,
      flag = PendingIntent.FLAG_UPDATE_CURRENT
    )
    if (
      triggerTimeMinusNotification.timeInMillis > Calendar.getInstance().timeInMillis &&
      upcomingAlarmNotificationEnabled
    ) {
      if (repeatAlarmDaily || daysOfWeek.isNotEmpty()) {
        alarmManager.setRepeating(
          AlarmManager.RTC_WAKEUP,
          triggerTimeMinusNotification.timeInMillis,
          AlarmManager.INTERVAL_DAY,
          pendingIntent
        )
      } else {
        alarmManager.setExactAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          triggerTimeMinusNotification.timeInMillis,
          pendingIntent
        )
      }
    }
  }
}
