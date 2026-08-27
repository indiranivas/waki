package com.whakaara.feature.alarm.ui

import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.outlined.AutoDelete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.dokar.sheets.BottomSheet
import com.dokar.sheets.rememberBottomSheetState
import com.whakaara.core.designsystem.WakiCard
import com.whakaara.core.designsystem.WakiTimeDisplay
import com.whakaara.core.designsystem.theme.AlarmPreviewProvider
import com.whakaara.core.designsystem.theme.FontScalePreviews
import com.whakaara.core.designsystem.theme.ThemePreviews
import com.whakaara.core.designsystem.theme.WakiTheme
import com.whakaara.feature.alarm.R
import com.whakaara.feature.alarm.utils.GeneralUtils.Companion.showToast
import com.whakaara.model.alarm.Alarm
import com.whakaara.model.preferences.TimeFormat
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@Composable
fun Card(
    modifier: Modifier = Modifier,
    alarm: Alarm,
    timeFormat: TimeFormat,
    disable: (alarm: Alarm) -> Unit,
    enable: (alarm: Alarm) -> Unit,
    reset: (alarm: Alarm) -> Unit,
    getInitialTimeToAlarm: (isEnabled: Boolean, time: Calendar) -> String,
    getTimeUntilAlarmFormatted: (date: Calendar) -> String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetState()
    val valueSlider by remember(alarm.isEnabled) { mutableStateOf(alarm.isEnabled) }
    var timeToAlarm by remember { mutableStateOf(getInitialTimeToAlarm(valueSlider, alarm.date)) }
    val alpha = if (valueSlider) 1f else 0.60f

    LaunchedEffect(key1 = alarm.date, key2 = valueSlider) {
        timeToAlarm = getInitialTimeToAlarm(valueSlider, alarm.date)
    }

    SystemBroadcastReceiver(
        IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
        }
    ) { _, _ ->
        timeToAlarm = getInitialTimeToAlarm(valueSlider, alarm.date)
    }

    WakiCard(
        modifier = modifier
            .padding(vertical = 8.dp)
            .clickable {
                scope.launch { sheetState.expand() }
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WakiTimeDisplay(
                        time = alarm.subTitle.filter { it.isDigit() || it == ':' },
                        amPm = alarm.subTitle.filter { it.isLetter() }
                    )
                }
                Text(
                    text = alarm.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.alpha(alpha)
                )

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.alpha(alpha)) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = getRepeatText(alarm),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = timeToAlarm,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.alpha(alpha).padding(top = 4.dp)
                )
            }

            Switch(
                checked = valueSlider,
                onCheckedChange = {
                    if (!it) {
                        disable(alarm)
                        context.showToast(message = context.getString(R.string.notification_action_cancelled, alarm.title))
                    } else {
                        enable(alarm)
                        context.showToast(
                            message = getTimeUntilAlarmFormatted(alarm.date)
                        )
                    }
                }
            )
        }
    }

    BottomSheet(
        backgroundColor = MaterialTheme.colorScheme.surface,
        state = sheetState,
        skipPeeked = true
    ) {
        BottomSheetDetailsContent(
            alarm = alarm,
            timeToAlarm = timeToAlarm,
            timeFormat = timeFormat,
            sheetState = sheetState,
            reset = reset,
            getTimeUntilAlarmFormatted = getTimeUntilAlarmFormatted
        )
    }
}

fun getRepeatText(alarm: Alarm): String {
    val days = alarm.daysOfWeek
    return when {
        alarm.repeatDaily || days.size == 7 -> "Everyday"
        days.isEmpty() -> "Tomorrow"
        days.size == 5 && !days.contains(5) && !days.contains(6) -> "Weekdays"
        else -> days.sorted().joinToString(" ") {
            DayOfWeek.of(if (it == 0) 7 else it).getDisplayName(TextStyle.SHORT, Locale.getDefault())
        }
    }
}

@Composable
@ThemePreviews
@FontScalePreviews
fun CardPreview(
    @PreviewParameter(AlarmPreviewProvider::class) alarm: Alarm
) {
    WakiTheme {
        Card(
            alarm = alarm,
            timeFormat = TimeFormat.TWENTY_FOUR_HOURS,
            disable = {},
            enable = {},
            reset = {},
            getInitialTimeToAlarm = { _, _ -> "getInitialTimeToAlarm" }
        ) { "getTimeUntilAlarmFormatted" }
    }
}
