package com.whakaara.feature.timer

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.whakaara.core.CountDownTimerUtil
import com.whakaara.core.LogUtils.logD
import com.whakaara.core.LogUtils.logE
import com.whakaara.core.PendingIntentUtils
import com.whakaara.core.constants.DateUtilsConstants
import com.whakaara.core.constants.GeneralConstants
import com.whakaara.core.constants.NotificationUtilsConstants
import com.whakaara.core.hyperisland.WakiHyperIsland
import com.whakaara.core.di.ApplicationScope
import com.whakaara.core.di.IoDispatcher
import com.whakaara.core.di.MainDispatcher
import com.whakaara.data.datastore.PreferencesDataStoreRepository
import com.whakaara.data.preferences.PreferencesRepository
import com.whakaara.data.timer.TimerRepository
import com.whakaara.feature.timer.reciever.TimerReceiver
import com.whakaara.feature.timer.service.TimerMediaService
import com.whakaara.feature.timer.util.DateUtils
import com.whakaara.model.datastore.TimerStateDataStore
import com.whakaara.model.preferences.PreferencesState
import com.whakaara.model.timer.TimerState
import com.whakaara.model.timer.TimerStateReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val app: Application,
    private val alarmManager: AlarmManager,
    private val notificationManager: NotificationManager,
    @Named("timer")
    private val timerNotificationBuilder: NotificationCompat.Builder,
    private val countDownTimerUtil: CountDownTimerUtil,
    private val preferencesDatastore: PreferencesDataStoreRepository,
    private val timerRepository: TimerRepository,
    private val preferencesRepository: PreferencesRepository,
    @ApplicationScope private val coroutineScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : AndroidViewModel(application = app) {

    private val _timerState: MutableStateFlow<TimerState> = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _preferences: MutableStateFlow<PreferencesState> = MutableStateFlow(PreferencesState())
    val preferences: StateFlow<PreferencesState> = _preferences.asStateFlow()

    init {
        collectPreferencesState()
        collectTimerStateFromReceiver()
    }

    private fun collectPreferencesState() = viewModelScope.launch {
        preferencesRepository.getPreferencesFlow().flowOn(ioDispatcher).collect { preferences ->
            _preferences.value = PreferencesState(preferences = preferences, isReady = true)
        }
    }

    private fun collectTimerStateFromReceiver() = viewModelScope.launch {
        timerRepository.timerState.collectLatest { state ->
            when (state) {
                is TimerStateReceiver.Idle -> {
                    logD(message = "TimerStateReceiver.Idle")
                }

                is TimerStateReceiver.Started -> {
                    startTimer()
                    startTimerNotificationCountdown(milliseconds = state.currentTime + Calendar.getInstance().timeInMillis)

                    logD(message = "TimerStateReceiver.Started")
                }

                is TimerStateReceiver.Paused -> {
                    pauseTimer()
                    pauseTimerNotificationCountdown()

                    logD(message = "TimerStateReceiver.Paused")
                }

                is TimerStateReceiver.Stopped -> {
                    resetTimer()

                    logD(message = "TimerStateReceiver.Stopped")
                }
            }
        }
    }

    //region mvm timer
    fun updateInputHours(newValue: String) {
        _timerState.update {
            it.copy(
                inputHours = newValue
            )
        }
    }

    fun updateInputMinutes(newValue: String) {
        _timerState.update {
            it.copy(
                inputMinutes = newValue
            )
        }
    }

    fun updateInputSeconds(newValue: String) {
        _timerState.update {
            it.copy(
                inputSeconds = newValue
            )
        }
    }

    fun startTimer() {
        val currentTimeInMillis = Calendar.getInstance().timeInMillis
        if (timerState.value.isTimerPaused) {
            createTimerNotification(milliseconds = currentTimeInMillis + timerState.value.currentTime)
            startCountDownTimer(timeToCountDown = timerState.value.currentTime)
            updateTimerStateToStarted(millisecondsToAdd = timerState.value.currentTime)
        } else if (checkIfOneInputValueGreaterThanZero()) {
            val millisecondsFromTimerInput =
                DateUtils.generateMillisecondsFromTimerInputValues(
                    hours = timerState.value.inputHours,
                    minutes = timerState.value.inputMinutes,
                    seconds = timerState.value.inputSeconds
                )
            createTimerNotification(milliseconds = currentTimeInMillis + millisecondsFromTimerInput)
            startCountDownTimer(timeToCountDown = millisecondsFromTimerInput)
            updateTimerStateToStarted(millisecondsToAdd = millisecondsFromTimerInput)
        }
    }

    fun pauseTimer() {
        if (!_timerState.value.isTimerPaused) {
            cancelTimerAlarm()
            countDownTimerUtil.cancel()
            _timerState.update {
                it.copy(
                    isTimerPaused = true,
                    isTimerActive = false
                )
            }
        }
    }

    fun resetTimer() = viewModelScope.launch {
        cancelNotification()
        cancelTimerAlarm()
        countDownTimerUtil.cancel()
        _timerState.update {
            it.copy(
                isTimerPaused = false,
                isTimerActive = false,
                currentTime = GeneralConstants.ZERO_MILLIS,
                inputHours = DateUtilsConstants.TIMER_INPUT_INITIAL_VALUE,
                inputMinutes = "25",
                inputSeconds = DateUtilsConstants.TIMER_INPUT_INITIAL_VALUE,
                isStart = true,
                progress = GeneralConstants.STARTING_CIRCULAR_PROGRESS,
                time = "00:25:00",
                millisecondsFromTimerInput = GeneralConstants.ZERO_MILLIS
            )
        }
        preferencesDatastore.saveTimerData(
            state = TimerStateDataStore(
                inputHours = DateUtilsConstants.TIMER_INPUT_INITIAL_VALUE,
                inputMinutes = "25",
                inputSeconds = DateUtilsConstants.TIMER_INPUT_INITIAL_VALUE,
            )
        )
    }

    fun restartTimer() {
        cancelNotification()
        cancelTimerAlarm()
        countDownTimerUtil.cancel()
        _timerState.update {
            it.copy(
                isTimerPaused = false,
                isTimerActive = false,
                isStart = true,
                currentTime = GeneralConstants.ZERO_MILLIS,
                progress = GeneralConstants.STARTING_CIRCULAR_PROGRESS,
                time = DateUtilsConstants.TIMER_STARTING_FORMAT
            )
        }

        if (_preferences.value.preferences.autoRestartTimer) {
            _timerState.update {
                it.copy(
                    millisecondsFromTimerInput = GeneralConstants.ZERO_MILLIS
                )
            }
            startTimer()
        }
    }

    /** Adds minutes to a running or paused timer (mockup "Add time"). */
    fun addTime(minutes: Int = 1) {
        val state = timerState.value
        if (!state.isTimerActive && !state.isTimerPaused) return

        val extraMs = minutes.coerceAtLeast(1) * 60_000L
        val newCurrent = state.currentTime + extraMs
        val baseTotal = state.millisecondsFromTimerInput.coerceAtLeast(state.currentTime)
        val newTotal = baseTotal + extraMs

        countDownTimerUtil.cancel()
        cancelTimerAlarm()

        val hours = (newCurrent / 3_600_000).toInt()
        val mins = ((newCurrent % 3_600_000) / 60_000).toInt()
        val secs = ((newCurrent % 60_000) / 1000).toInt()

        _timerState.update {
            it.copy(
                currentTime = newCurrent,
                millisecondsFromTimerInput = newTotal,
                progress = (newCurrent.toFloat() / newTotal.toFloat()).coerceIn(0f, 1f),
                time = DateUtils.formatTimeForTimer(millis = newCurrent),
                inputHours = String.format(java.util.Locale.ROOT, "%02d", hours),
                inputMinutes = String.format(java.util.Locale.ROOT, "%02d", mins),
                inputSeconds = String.format(java.util.Locale.ROOT, "%02d", secs),
            )
        }

        if (state.isTimerActive) {
            val endAt = Calendar.getInstance().timeInMillis + newCurrent
            createTimerNotification(milliseconds = endAt)
            startCountDownTimer(timeToCountDown = newCurrent, totalForProgress = newTotal)
            startTimerNotificationCountdown(milliseconds = endAt)
        }
    }

    fun recreateTimer() = viewModelScope.launch(mainDispatcher) {
        if (timerState.value != TimerState()) return@launch

        val status = preferencesDatastore.readTimerStatus().first()
        if (status == TimerStateDataStore()) return@launch

        val difference = System.currentTimeMillis() - status.timeStamp
        val remaining = status.remainingTimeInMillis - difference

        if (remaining <= 0) return@launch

        if (status.isActive) {
            recreateActiveTimer(
                milliseconds = remaining,
                inputHours = status.inputHours,
                inputMinutes = status.inputMinutes,
                inputSeconds = status.inputSeconds
            )
        } else if (status.isPaused) {
            recreatePausedTimer(
                milliseconds = remaining,
                inputHours = status.inputHours,
                inputMinutes = status.inputMinutes,
                inputSeconds = status.inputSeconds
            )
        }

        preferencesDatastore.saveTimerData(
            state = TimerStateDataStore()
        )
    }

    fun saveTimerStateForRecreation() = viewModelScope.launch(ioDispatcher) {
        if (!timerState.value.isStart) {
            preferencesDatastore.saveTimerData(
                TimerStateDataStore(
                    remainingTimeInMillis = timerState.value.currentTime,
                    isActive = timerState.value.isTimerActive,
                    isPaused = timerState.value.isTimerPaused,
                    timeStamp = System.currentTimeMillis(),
                    inputHours = timerState.value.inputHours,
                    inputMinutes = timerState.value.inputMinutes,
                    inputSeconds = timerState.value.inputSeconds
                )
            )
        }
    }

    fun startTimerNotification() {
        if (timerState.value.isTimerPaused) {
            pauseTimerNotificationCountdown()
        } else if (timerState.value.isTimerActive) {
            startTimerNotificationCountdown(
                milliseconds = timerState.value.currentTime + Calendar.getInstance().timeInMillis
            )
        }
    }

    fun cancelTimerNotification() {
        cancelNotification()
    }
    // endregion

    //region tmw
    private fun recreateActiveTimer(
        milliseconds: Long,
        inputHours: String,
        inputMinutes: String,
        inputSeconds: String
    ) {
        countDownTimerUtil.cancel()
        startCountDownTimer(timeToCountDown = milliseconds)
        _timerState.update {
            it.copy(
                isTimerPaused = false,
                isStart = false,
                isTimerActive = true,
                millisecondsFromTimerInput = milliseconds,
                inputHours = inputHours,
                inputMinutes = inputMinutes,
                inputSeconds = inputSeconds
            )
        }
    }

    private fun updateTimerStateToStarted(millisecondsToAdd: Long) {
        _timerState.update {
            it.copy(
                isTimerPaused = false,
                isStart = false,
                isTimerActive = true,
                millisecondsFromTimerInput = millisecondsToAdd
            )
        }
    }

    private fun checkIfOneInputValueGreaterThanZero(): Boolean {
        return listOf(
            timerState.value.inputHours,
            timerState.value.inputMinutes,
            timerState.value.inputSeconds
        ).any { it.toIntOrNull()?.let { num -> num > 0 } == true }
    }

    private fun startCountDownTimer(timeToCountDown: Long, totalForProgress: Long = timeToCountDown) {
        val progressTotal = totalForProgress.coerceAtLeast(1L)
        countDownTimerUtil.countdown(
            period = timeToCountDown,
            onTickAction = { millisUntilFinished ->
                _timerState.update {
                    it.copy(
                        currentTime = millisUntilFinished,
                        progress = millisUntilFinished.toFloat() / progressTotal.toFloat(),
                        time = DateUtils.formatTimeForTimer(
                            millis = millisUntilFinished
                        )
                    )
                }
            },
            onFinishAction = {
                _timerState.update {
                    it.copy(
                        isTimerPaused = false,
                        isTimerActive = false,
                        currentTime = GeneralConstants.ZERO_MILLIS,
                        inputHours = DateUtilsConstants.TIMER_INPUT_INITIAL_VALUE,
                        inputMinutes = "25",
                        inputSeconds = DateUtilsConstants.TIMER_INPUT_INITIAL_VALUE,
                        isStart = true,
                        progress = GeneralConstants.STARTING_CIRCULAR_PROGRESS,
                        time = "00:25:00",
                        millisecondsFromTimerInput = GeneralConstants.ZERO_MILLIS
                    )
                }
                resetTimerStateDataStore()
            }
        )
    }

    private fun recreatePausedTimer(
        milliseconds: Long,
        inputHours: String,
        inputMinutes: String,
        inputSeconds: String
    ) {
        _timerState.update {
            it.copy(
                isStart = false,
                isTimerActive = false,
                isTimerPaused = true,
                currentTime = milliseconds,
                millisecondsFromTimerInput = milliseconds,
                time = DateUtils.formatTimeForTimer(
                    millis = milliseconds
                ),
                inputHours = inputHours,
                inputMinutes = inputMinutes,
                inputSeconds = inputSeconds
            )
        }
    }

    private fun createTimerNotification(milliseconds: Long) {
        val startReceiverIntent =
            getStartReceiverIntent(
                alarmId = NotificationUtilsConstants.INTENT_TIMER_NOTIFICATION_ID,
                autoSilenceTime = NotificationUtilsConstants.ALARM_SOUND_TIMEOUT_DEFAULT_MINUTES,
                type = NotificationUtilsConstants.NOTIFICATION_TYPE_TIMER
            )

        val pendingIntent =
            PendingIntentUtils.getService(
                context = app.applicationContext,
                id = NotificationUtilsConstants.INTENT_REQUEST_CODE,
                intent = startReceiverIntent,
                flag = PendingIntent.FLAG_UPDATE_CURRENT
            )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            milliseconds,
            pendingIntent
        )
    }

    private fun getStartReceiverIntent(
        autoSilenceTime: Int,
        action: Int = NotificationUtilsConstants.PLAY,
        type: Int,
        alarmId: String? = null
    ) = Intent(app, TimerMediaService::class.java).apply {
        this.action = alarmId
        putExtra(NotificationUtilsConstants.INTENT_AUTO_SILENCE, autoSilenceTime)
        putExtra(NotificationUtilsConstants.SERVICE_ACTION, action)
        putExtra(NotificationUtilsConstants.NOTIFICATION_TYPE, type)
        putExtra(NotificationUtilsConstants.INTENT_ALARM_ID, alarmId)
    }

    private fun startTimerNotificationCountdown(milliseconds: Long) {
        val pauseReceiverIntent = app.applicationContext.getTimerReceiverIntent(intentAction = NotificationUtilsConstants.TIMER_RECEIVER_ACTION_PAUSE)
        val stopTimerReceiverIntent = app.applicationContext.getTimerReceiverIntent(intentAction = NotificationUtilsConstants.TIMER_RECEIVER_ACTION_STOP)

        val pauseTimerReceiverPendingIntent =
            PendingIntentUtils.getBroadcast(
                context = app.applicationContext,
                id = NotificationUtilsConstants.INTENT_REQUEST_CODE,
                intent = pauseReceiverIntent,
                flag = PendingIntent.FLAG_UPDATE_CURRENT
            )
        val stopTimerReceiverPendingIntent =
            PendingIntentUtils.getBroadcast(
                context = app.applicationContext,
                id = NotificationUtilsConstants.INTENT_REQUEST_CODE,
                intent = stopTimerReceiverIntent,
                flag = PendingIntent.FLAG_UPDATE_CURRENT
            )
        val title = app.applicationContext.getString(R.string.timer_notification_title_active)
        val pauseLabel = app.applicationContext.getString(R.string.notification_timer_pause_action_label)
        val stopLabel = app.applicationContext.getString(R.string.notification_timer_stop_action_label)
        val remainingMs = (milliseconds - System.currentTimeMillis()).coerceAtLeast(0L)
        val remainingLabel = DateUtils.formatTimeForTimer(millis = remainingMs)

        timerNotificationBuilder.clearActions()
        WakiHyperIsland.clearFocusExtras(timerNotificationBuilder)
        val notificationBuilder = timerNotificationBuilder.apply {
            setWhen(milliseconds)
            setShowWhen(true)
            setUsesChronometer(true)
            setChronometerCountDown(true)
            setAutoCancel(false)
            setTimeoutAfter(remainingMs)
            setOnlyAlertOnce(true)
            setCategory(NotificationCompat.CATEGORY_ALARM)
            setOngoing(true)
            setContentTitle(title)
            setContentText(remainingLabel)
            setSubText(app.applicationContext.getString(R.string.timer_notification_sub_text_active))
            addAction(0, pauseLabel, pauseTimerReceiverPendingIntent)
            addAction(0, stopLabel, stopTimerReceiverPendingIntent)
        }
        WakiHyperIsland.applyCountdown(
            context = app.applicationContext,
            builder = notificationBuilder,
            label = title,
            endTimeMillis = milliseconds,
            totalDurationMs = remainingMs,
            primary = WakiHyperIsland.IslandAction("pause", pauseLabel, pauseTimerReceiverPendingIntent),
            secondary = WakiHyperIsland.IslandAction("stop", stopLabel, stopTimerReceiverPendingIntent),
        )
        notificationManager.notify(NotificationUtilsConstants.TIMER_NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun pauseTimerNotificationCountdown() {
        val startTimerReceiverIntent =
            app.applicationContext.getTimerReceiverIntent(intentAction = NotificationUtilsConstants.TIMER_RECEIVER_ACTION_START).apply {
                putExtra(NotificationUtilsConstants.TIMER_RECEIVER_CURRENT_TIME_EXTRA, timerState.value.currentTime)
            }
        val stopTimerReceiverIntent = app.applicationContext.getTimerReceiverIntent(intentAction = NotificationUtilsConstants.TIMER_RECEIVER_ACTION_STOP)

        val playTimerReceiverPendingIntent =
            PendingIntentUtils.getBroadcast(
                context = app.applicationContext,
                id = NotificationUtilsConstants.INTENT_REQUEST_CODE,
                intent = startTimerReceiverIntent,
                flag = PendingIntent.FLAG_UPDATE_CURRENT
            )

        val stopTimerReceiverPendingIntent =
            PendingIntentUtils.getBroadcast(
                context = app.applicationContext,
                id = NotificationUtilsConstants.INTENT_REQUEST_CODE,
                intent = stopTimerReceiverIntent,
                flag = PendingIntent.FLAG_UPDATE_CURRENT
            )

        val title = app.applicationContext.getString(R.string.timer_notification_title_paused)
        val remainingLabel = DateUtils.formatTimeForTimer(millis = timerState.value.currentTime)
        val playLabel = app.applicationContext.getString(R.string.notification_timer_play_action_label)
        val stopLabel = app.applicationContext.getString(R.string.notification_timer_stop_action_label)

        timerNotificationBuilder.clearActions()
        WakiHyperIsland.clearFocusExtras(timerNotificationBuilder)
        val notificationBuilder = timerNotificationBuilder.apply {
            setWhen(System.currentTimeMillis())
            setShowWhen(false)
            setUsesChronometer(false)
            setChronometerCountDown(false)
            setOnlyAlertOnce(true)
            setOngoing(true)
            setContentTitle(title)
            setContentText(remainingLabel)
            setSubText(app.applicationContext.getString(R.string.notification_sub_text_paused))
            addAction(0, playLabel, playTimerReceiverPendingIntent)
            addAction(0, stopLabel, stopTimerReceiverPendingIntent)
        }
        WakiHyperIsland.applyStatic(
            context = app.applicationContext,
            builder = notificationBuilder,
            title = title,
            content = remainingLabel,
            business = "waki_timer_paused",
            primary = WakiHyperIsland.IslandAction("play", playLabel, playTimerReceiverPendingIntent),
            secondary = WakiHyperIsland.IslandAction("stop", stopLabel, stopTimerReceiverPendingIntent),
        )
        notificationManager.notify(NotificationUtilsConstants.TIMER_NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun cancelTimerAlarm() {
        val startReceiverIntent =
            Intent(app, TimerMediaService::class.java).apply {
                this.action = NotificationUtilsConstants.INTENT_TIMER_NOTIFICATION_ID
            }

        val pendingIntent =
            PendingIntentUtils.getService(
                context = app,
                id = NotificationUtilsConstants.INTENT_REQUEST_CODE,
                intent = startReceiverIntent,
                flag = PendingIntent.FLAG_UPDATE_CURRENT
            )

        alarmManager.cancel(pendingIntent)
    }

    private fun cancelNotification() {
        notificationManager.cancel(NotificationUtilsConstants.TIMER_NOTIFICATION_ID)
    }

    private fun resetTimerStateDataStore() {
        try {
            coroutineScope.launch {
                preferencesDatastore.saveTimerData(
                    state = TimerStateDataStore()
                )
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (throwable: Throwable) {
            logE(message = "resetTimerStateDataStore execution failed", throwable = throwable)
        } finally {
            // Nothing can be in the `finally` block after this, as this throws a
            // `CancellationException`
            coroutineScope.cancel()
        }
    }

    companion object {
        fun Context.getTimerReceiverIntent(intentAction: String): Intent {
            return Intent(this, TimerReceiver::class.java).apply {
                action = intentAction
            }
        }
    }
    // endregion
}
