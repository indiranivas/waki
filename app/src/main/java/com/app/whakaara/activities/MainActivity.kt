package com.app.whakaara.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.app.whakaara.ui.screens.WakiSplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.whakaara.logic.MainViewModel
import com.app.whakaara.state.events.AppViewModels
import com.app.whakaara.state.events.PreferencesEventCallbacks
import com.app.whakaara.ui.screens.MainScreen
import com.whakaara.core.designsystem.theme.WakiTheme
import com.whakaara.feature.alarm.AlarmViewModel
import com.whakaara.feature.alarm.LocationAlarmViewModel
import com.whakaara.feature.stopwatch.StopwatchViewModel
import com.whakaara.feature.timer.TimerViewModel
import com.whakaara.model.preferences.AppTheme
import com.whakaara.model.preferences.Preferences
import com.whakaara.model.preferences.TimeFormat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@ExperimentalLayoutApi
class MainActivity : ComponentActivity(), PreferencesEventCallbacks {
    private val viewModel: MainViewModel by viewModels()
    private val timerViewModel: TimerViewModel by viewModels()
    private val stopwatchViewModel: StopwatchViewModel by viewModels()
    private val alarmViewModel: AlarmViewModel by viewModels()
    private val locationAlarmViewModel: LocationAlarmViewModel by viewModels()

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appViewModels = AppViewModels(
            main = viewModel,
            timer = timerViewModel,
            stopwatch = stopwatchViewModel,
            alarm = alarmViewModel,
            location = locationAlarmViewModel
        )

        installSplashScreen()

        setContent {
            val preferencesState by viewModel.preferencesUiState.collectAsStateWithLifecycle()
            val timerState by timerViewModel.timerState.collectAsStateWithLifecycle()
            var showSplash by remember { mutableStateOf(true) }

            Crossfade(
                targetState = showSplash,
                animationSpec = tween(durationMillis = 550),
                label = "splashCrossfade"
            ) { isSplash ->
                if (isSplash) {
                    WakiSplashScreen(
                        onAnimationFinished = { showSplash = false }
                    )
                } else {
                    val useDarkColours = when (preferencesState.preferences.appTheme) {
                        AppTheme.MODE_DAY -> false
                        AppTheme.MODE_NIGHT -> true
                        AppTheme.MODE_AUTO -> isSystemInDarkTheme()
                    }

                    WakiTheme(
                        darkTheme = useDarkColours,
                        dynamicColor = preferencesState.preferences.dynamicTheme
                    ) {
                        MainScreen(
                            preferencesState = preferencesState,
                            preferencesEventCallbacks = this@MainActivity,
                            viewModels = appViewModels,
                            timerState = timerState,
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        with(stopwatchViewModel) {
            recreateStopwatch()
            cancelStopwatchNotification()
        }

        with(timerViewModel) {
            recreateTimer()
            cancelTimerNotification()
        }
    }

    override fun onPause() {
        super.onPause()
        with(stopwatchViewModel) {
            saveStopwatchStateForRecreation()
            startStopwatchNotification()
        }

        with(timerViewModel) {
            saveTimerStateForRecreation()
            startTimerNotification()
        }
    }

    override fun updatePreferences(preferences: Preferences) {
        viewModel.updatePreferences(preferences = preferences)
    }

    override fun updateAllAlarmSubtitles(format: TimeFormat) {
        alarmViewModel.updateAllAlarmSubtitles(format = format)
    }

    override fun updateCurrentAlarmsToAddOrRemoveUpcomingAlarmNotification(shouldEnableUpcomingAlarmNotification: Boolean) {
        alarmViewModel.updateCurrentAlarmsToAddOrRemoveUpcomingAlarmNotification(shouldEnableUpcomingAlarmNotification)
    }
}
