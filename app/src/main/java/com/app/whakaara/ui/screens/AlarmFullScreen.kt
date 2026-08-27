package com.app.whakaara.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.whakaara.R
import com.whakaara.core.GeneralUtils.Companion.showToast
import com.whakaara.core.designsystem.WakiTimeDisplay
import com.whakaara.core.designsystem.theme.AlarmPreviewProvider
import com.whakaara.core.designsystem.theme.FontScalePreviews
import com.whakaara.core.designsystem.theme.ThemePreviews
import com.whakaara.core.designsystem.theme.WakiTheme
import com.whakaara.core.designsystem.theme.wakiOrange
import com.whakaara.model.alarm.Alarm
import com.whakaara.model.preferences.TimeFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AlarmFullScreen(
    modifier: Modifier = Modifier,
    alarm: Alarm,
    snooze: (alarm: Alarm) -> Unit,
    disable: (alarm: Alarm) -> Unit,
    timeFormat: TimeFormat
) {
    val context = LocalContext.current
    val activity = (context as? Activity)
    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Good night"
    }

    Scaffold { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxSize()
                .background(wakiOrange)
                .padding(innerPadding)
                .padding(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                WakiTimeDisplay(
                    time = LocalTime.now().format(DateTimeFormatter.ofPattern(if (timeFormat == TimeFormat.TWENTY_FOUR_HOURS) "HH:mm" else "hh:mm")),
                    amPm = if (timeFormat == TimeFormat.TWENTY_FOUR_HOURS) null else LocalTime.now().format(DateTimeFormatter.ofPattern("a")),
                    modifier = Modifier.padding(top = 48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$greeting, Nivas", // Mockup name
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = alarm.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        disable(alarm)
                        context.showToast(message = context.getString(R.string.notification_action_cancelled, alarm.title))
                        activity?.finish()
                    },
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Stop",
                        tint = wakiOrange,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Text(
                    text = "Stop",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                if (alarm.isSnoozeEnabled) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        onClick = {
                            snooze(alarm)
                            context.showToast(
                                message = context.getString(R.string.notification_action_snoozed, alarm.title)
                            )
                            activity?.finish()
                        }
                    ) {
                        Text(
                            text = "Snooze 10 min",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
@ThemePreviews
@FontScalePreviews
private fun NotificationFullScreenPreview(
    @PreviewParameter(AlarmPreviewProvider::class) alarm: Alarm
) {
    WakiTheme {
        AlarmFullScreen(
            alarm = alarm,
            snooze = {},
            disable = {},
            timeFormat = TimeFormat.TWENTY_FOUR_HOURS
        )
    }
}
