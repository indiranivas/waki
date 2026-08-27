package com.whakaara.feature.timer.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.res.stringResource
import com.whakaara.core.designsystem.theme.FontScalePreviews
import com.whakaara.core.designsystem.theme.Spacings.space275
import com.whakaara.core.designsystem.theme.Spacings.spaceNone
import com.whakaara.core.designsystem.theme.Spacings.spaceXLarge
import com.whakaara.core.designsystem.theme.Spacings.spaceXSmall
import com.whakaara.core.designsystem.theme.Spacings.spaceXxSmall
import com.whakaara.core.designsystem.theme.ThemePreviews
import com.whakaara.core.designsystem.theme.WakiTheme
import com.whakaara.core.designsystem.theme.wakiOrange
import com.whakaara.feature.timer.R
import com.whakaara.feature.timer.util.DateUtils
import com.whakaara.model.preferences.TimeFormat
import java.util.Calendar

@Composable
fun TimerCountdownDisplay(
    modifier: Modifier = Modifier,
    progress: Float,
    time: String,
    isPaused: Boolean,
    isStart: Boolean,
    millisecondsFromTimerInput: Long,
    timeFormat: TimeFormat,
    isSplitMode: Boolean = false,
    isLargeScreen: Boolean = false
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 50,
            delayMillis = 0,
            easing = LinearEasing
        ),
        label = ""
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (!isSplitMode || isLargeScreen) {
                CircularProgressIndicator(
                    modifier = Modifier.size(280.dp),
                    progress = { animatedProgress },
                    color = wakiOrange,
                    strokeWidth = 12.dp,
                    trackColor = wakiOrange.copy(alpha = 0.1f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 64.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    text = time
                )
                Text(
                    text = "Timer active", // Label like "Work focus"
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Outlined.NotificationsActive,
                contentDescription = stringResource(id = R.string.timer_countdown_finish_time_icon_content_description),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isPaused) {
                    stringResource(R.string.timer_screen_paused)
                } else if (isStart) {
                    stringResource(R.string.timer_screen_no_timer_set)
                } else {
                    DateUtils.getTimerFinishFormatted(
                        date = Calendar.getInstance().apply {
                            add(
                                Calendar.MILLISECOND,
                                millisecondsFromTimerInput.toInt()
                            )
                        },
                        timeFormat = timeFormat
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
        }
    }
}

@Composable
@ThemePreviews
@FontScalePreviews
fun TimerCountdownDisplayPreview() {
    WakiTheme {
        TimerCountdownDisplay(
            progress = 1.0F,
            time = "00:00:00",
            isPaused = false,
            isStart = false,
            millisecondsFromTimerInput = 0,
            timeFormat = TimeFormat.TWELVE_HOURS
        )
    }
}
