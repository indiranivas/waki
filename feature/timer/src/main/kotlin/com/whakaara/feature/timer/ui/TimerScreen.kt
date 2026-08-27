package com.whakaara.feature.timer.ui

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterCenterFocus
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.whakaara.core.constants.DateUtilsConstants
import com.whakaara.core.designsystem.theme.FontScalePreviews
import com.whakaara.core.designsystem.theme.Spacings.spaceMedium
import com.whakaara.core.designsystem.theme.ThemePreviews
import com.whakaara.core.designsystem.theme.WakiTheme
import com.whakaara.core.designsystem.theme.wakiOrange
import com.whakaara.core.designsystem.theme.wakiPresetIdle
import com.whakaara.core.designsystem.theme.wakiRingTrack
import com.whakaara.core.designsystem.theme.wakiSuccess
import com.whakaara.feature.timer.R
import com.whakaara.feature.timer.util.DateUtils
import com.whakaara.model.preferences.TimeFormat
import com.whakaara.model.timer.TimerState
import java.util.Calendar
import java.util.Locale

private val TIMER_PRESETS_MINUTES = listOf(5, 10, 15, 25, 30, 60)

@Composable
fun TimerScreen(
    timerState: TimerState,
    updateHours: (newValue: String) -> Unit,
    updateMinutes: (newValue: String) -> Unit,
    updateSeconds: (newValue: String) -> Unit,
    timeFormat: TimeFormat,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
) {
    val focusManager = LocalFocusManager.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isLargeScreen = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM ||
        windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
    val isRunningOrPaused = timerState.isTimerActive || timerState.isTimerPaused

    val selectedPreset = remember(timerState.inputHours, timerState.inputMinutes, timerState.inputSeconds) {
        val hours = timerState.inputHours.toIntOrNull() ?: 0
        val minutes = timerState.inputMinutes.toIntOrNull() ?: 0
        val seconds = timerState.inputSeconds.toIntOrNull() ?: 0
        if (seconds != 0) {
            null
        } else {
            val totalMinutes = hours * 60 + minutes
            TIMER_PRESETS_MINUTES.firstOrNull { it == totalMinutes }
        }
    }

    LaunchedEffect(Unit) {
        val hours = timerState.inputHours.toIntOrNull() ?: 0
        val minutes = timerState.inputMinutes.toIntOrNull() ?: 0
        val seconds = timerState.inputSeconds.toIntOrNull() ?: 0
        if (!isRunningOrPaused && hours == 0 && minutes == 0 && seconds == 0) {
            updateHours("00")
            updateMinutes("25")
            updateSeconds("00")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(timerState) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                        if (timerState.inputHours.isBlank()) updateHours(DateUtilsConstants.TIMER_INPUT_INITIAL_VALUE)
                        if (timerState.inputMinutes.isBlank()) updateMinutes(DateUtilsConstants.TIMER_INPUT_INITIAL_VALUE)
                        if (timerState.inputSeconds.isBlank()) updateSeconds(DateUtilsConstants.TIMER_INPUT_INITIAL_VALUE)
                    },
                )
            }
            .padding(horizontal = spaceMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (isLandscape && isLargeScreen) {
            Arrangement.Center
        } else {
            Arrangement.SpaceBetween
        },
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        TimerFocusChip(
            label = stringResource(id = R.string.timer_focus_label_default),
        )

        Spacer(modifier = Modifier.height(20.dp))

        TimerRingDisplay(
            timerState = timerState,
            timeFormat = timeFormat,
            isLargeScreen = isLargeScreen,
            modifier = Modifier.weight(1f, fill = false),
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (!isRunningOrPaused) {
            TimerPresetRow(
                selectedMinutes = selectedPreset,
                onPresetSelected = { minutes ->
                    updateHours(String.format(Locale.ROOT, "%02d", minutes / 60))
                    updateMinutes(String.format(Locale.ROOT, "%02d", minutes % 60))
                    updateSeconds("00")
                },
            )
            Spacer(modifier = Modifier.height(12.dp))
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun TimerFocusChip(label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(wakiOrange.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.FilterCenterFocus,
            contentDescription = null,
            tint = wakiOrange,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = wakiOrange,
        )
    }
}

@Composable
private fun TimerRingDisplay(
    timerState: TimerState,
    timeFormat: TimeFormat,
    isLargeScreen: Boolean,
    modifier: Modifier = Modifier,
) {
    val isCompleted = !timerState.isStart &&
        !timerState.isTimerActive &&
        !timerState.isTimerPaused &&
        timerState.progress <= 0.01f &&
        timerState.time.startsWith("00:00")

    val displayTime = remember(
        timerState.isTimerActive,
        timerState.isTimerPaused,
        timerState.time,
        timerState.inputHours,
        timerState.inputMinutes,
        timerState.inputSeconds,
        isCompleted,
    ) {
        if (timerState.isTimerActive || timerState.isTimerPaused || isCompleted) {
            formatDisplayTime(timerState.time)
        } else {
            formatInputAsDisplay(
                hours = timerState.inputHours,
                minutes = timerState.inputMinutes,
                seconds = timerState.inputSeconds,
            )
        }
    }

    val targetProgress = when {
        isCompleted -> 1f
        timerState.isTimerActive || timerState.isTimerPaused -> timerState.progress.coerceIn(0f, 1f)
        else -> 1f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 80, easing = LinearEasing),
        label = "timerProgress",
    )
    val ringColor by animateColorAsState(
        targetValue = if (isCompleted) wakiSuccess else wakiOrange,
        label = "ringColor",
    )
    val trackColor = if (isCompleted) {
        wakiSuccess.copy(alpha = 0.2f)
    } else {
        wakiRingTrack.copy(alpha = 0.55f)
    }
    val ringSize = if (isLargeScreen) 300.dp else 280.dp

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.size(ringSize),
                progress = { animatedProgress },
                color = ringColor,
                strokeWidth = 10.dp,
                trackColor = trackColor,
                strokeCap = StrokeCap.Round,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = displayTime,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = if (displayTime.length > 5) 48.sp else 56.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(id = R.string.timer_focus_label_default),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                TimerFinishRow(
                    timerState = timerState,
                    timeFormat = timeFormat,
                    isCompleted = isCompleted,
                )
            }
        }
    }
}

@Composable
private fun TimerFinishRow(
    timerState: TimerState,
    timeFormat: TimeFormat,
    isCompleted: Boolean,
) {
    val label = when {
        isCompleted -> stringResource(id = R.string.timer_screen_completed)
        timerState.isTimerPaused -> stringResource(id = R.string.timer_screen_paused)
        timerState.isTimerActive -> DateUtils.getTimerFinishFormatted(
            date = Calendar.getInstance().apply {
                add(Calendar.MILLISECOND, timerState.currentTime.toInt().coerceAtLeast(0))
            },
            timeFormat = timeFormat,
        )
        else -> stringResource(id = R.string.timer_screen_ready)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.NotificationsActive,
            contentDescription = stringResource(id = R.string.timer_countdown_finish_time_icon_content_description),
            tint = if (isCompleted) wakiSuccess else wakiOrange,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TimerPresetRow(
    selectedMinutes: Int?,
    onPresetSelected: (Int) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        contentPadding = PaddingValues(horizontal = 4.dp),
    ) {
        items(TIMER_PRESETS_MINUTES) { minutes ->
            TimerPresetChip(
                label = stringResource(id = R.string.timer_preset_minutes, minutes),
                selected = selectedMinutes == minutes,
                onClick = { onPresetSelected(minutes) },
            )
        }
    }
}

@Composable
private fun TimerPresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) {
        wakiOrange
    } else if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        wakiPresetIdle
    }
    val content = if (selected) {
        androidx.compose.ui.graphics.Color.White
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .then(
                if (!selected) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp),
                    )
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = content,
        )
    }
}

private fun androidx.compose.ui.graphics.Color.luminance(): Float {
    val r = red
    val g = green
    val b = blue
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}

private fun formatInputAsDisplay(hours: String, minutes: String, seconds: String): String {
    val h = hours.toIntOrNull() ?: 0
    val m = minutes.toIntOrNull() ?: 0
    val s = seconds.toIntOrNull() ?: 0
    return if (h > 0) {
        String.format(Locale.ROOT, "%02d:%02d:%02d", h, m, s)
    } else {
        String.format(Locale.ROOT, "%02d:%02d", m, s)
    }
}

private fun formatDisplayTime(time: String): String {
    val parts = time.split(":")
    return if (parts.size == 3 && (parts[0] == "00" || parts[0] == "0")) {
        "${parts[1]}:${parts[2]}"
    } else {
        time
    }
}

@Composable
@ThemePreviews
@FontScalePreviews
fun TimerPreview() {
    WakiTheme {
        TimerScreen(
            timerState = TimerState(
                inputHours = "00",
                inputMinutes = "25",
                inputSeconds = "00",
                time = "00:25:00",
            ),
            updateHours = {},
            updateMinutes = {},
            updateSeconds = {},
            timeFormat = TimeFormat.TWELVE_HOURS,
        )
    }
}
