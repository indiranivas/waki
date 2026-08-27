package com.app.whakaara.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.whakaara.R
import com.app.whakaara.logic.MainViewModel
import com.app.whakaara.ui.screens.AlarmFullScreen
import com.app.whakaara.ui.screens.TimerFullScreen
import com.whakaara.core.GeneralUtils.Companion.showToast
import com.whakaara.core.constants.NotificationUtilsConstants.INTENT_ALARM_ID
import com.whakaara.core.constants.NotificationUtilsConstants.NOTIFICATION_TYPE_LOCATION_ALARM
import com.whakaara.core.constants.NotificationUtilsConstants.INTENT_EXTRA_ALARM
import com.whakaara.feature.alarm.LocationAlarmViewModel
import com.whakaara.feature.alarm.ui.LocationAlarmFullScreen
import com.whakaara.core.constants.NotificationUtilsConstants.NOTIFICATION_TYPE
import com.whakaara.core.constants.NotificationUtilsConstants.NOTIFICATION_TYPE_ALARM
import com.whakaara.core.constants.NotificationUtilsConstants.STOP_FULL_SCREEN_ACTIVITY
import com.whakaara.core.designsystem.theme.WakiTheme
import com.whakaara.feature.alarm.AlarmViewModel
import com.whakaara.feature.alarm.service.AlarmMediaService
import com.whakaara.feature.alarm.utils.GeneralUtils
import com.whakaara.feature.timer.TimerViewModel
import com.whakaara.model.alarm.Alarm
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class FullScreenNotificationActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val timerViewModel: TimerViewModel by viewModels()
    private val alarmViewModel: AlarmViewModel by viewModels()
    private val locationAlarmViewModel: LocationAlarmViewModel by viewModels()
    private var alarm: Alarm? = null
    private var locationAlarmId: String? = null

    private val broadCastReceiverFinishActivity =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                when (intent.action) {
                    STOP_FULL_SCREEN_ACTIVITY -> finishAndRemoveTask()
                    else -> return
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val notificationType = intent.getIntExtra(NOTIFICATION_TYPE, -1)
        if (notificationType == -1) {
            finish()
            return
        }

        hideSystemBars()
        turnScreenOnAndKeyguardOff()
        registerReceiver(broadCastReceiverFinishActivity, IntentFilter(STOP_FULL_SCREEN_ACTIVITY), RECEIVER_NOT_EXPORTED)

        if (notificationType == NOTIFICATION_TYPE_ALARM) {
            alarm = GeneralUtils.convertStringToAlarmObject(string = intent.getStringExtra(INTENT_EXTRA_ALARM))
        } else if (notificationType == NOTIFICATION_TYPE_LOCATION_ALARM) {
            locationAlarmId = intent.getStringExtra(INTENT_ALARM_ID)
        }

        setContent {
            val preferencesState by viewModel.preferencesUiState.collectAsStateWithLifecycle()
            val ringingLocationAlarm by locationAlarmViewModel.ringingLocationAlarm.collectAsStateWithLifecycle()

            LaunchedEffect(locationAlarmId) {
                locationAlarmId?.let { id ->
                    runCatching {
                        locationAlarmViewModel.loadRingingLocationAlarm(UUID.fromString(id))
                    }
                }
            }

            WakiTheme {
                when (notificationType) {
                    NOTIFICATION_TYPE_ALARM -> {
                        alarm?.let {
                            AlarmFullScreen(
                                alarm = it,
                                snooze = alarmViewModel::snooze,
                                disable = alarmViewModel::disable,
                                timeFormat = preferencesState.preferences.timeFormat
                            )
                        }
                    }
                    NOTIFICATION_TYPE_LOCATION_ALARM -> {
                        ringingLocationAlarm?.let {
                            LocationAlarmFullScreen(
                                alarm = it,
                                onStop = { stopLocationAlarmAndFinish(it.name) },
                                onSnooze = { stopLocationAlarmAndFinish(it.name) },
                                timeFormat = preferencesState.preferences.timeFormat
                            )
                        }
                    }
                    else -> {
                        TimerFullScreen(
                            resetTimer = timerViewModel::resetTimer,
                            timeFormat = preferencesState.preferences.timeFormat
                        )
                    }
                }
            }
        }
    }

    private fun stopLocationAlarmAndFinish(alarmName: String) {
        locationAlarmViewModel.dismissLocationAlarm()
        stopAlarmMediaService()
        showToast(message = getString(R.string.notification_action_cancelled, alarmName))
        finishAndRemoveTask()
    }

    private fun stopAlarmMediaService() {
        applicationContext.stopService(Intent(this, AlarmMediaService::class.java))
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
        when {
            alarm != null -> {
                alarmViewModel.disable(alarm = alarm!!)
                showToast(message = getString(R.string.notification_action_cancelled, alarm!!.title))
            }
            locationAlarmId != null -> {
                locationAlarmViewModel.dismissLocationAlarm()
            }
        }
        stopAlarmMediaService()
        finishAndRemoveTask()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmMediaService()
        unregisterReceiver(broadCastReceiverFinishActivity)
    }

    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun Activity.turnScreenOnAndKeyguardOff() {
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        with(getSystemService(KEYGUARD_SERVICE) as KeyguardManager) {
            requestDismissKeyguard(this@turnScreenOnAndKeyguardOff, null)
        }
    }
}
