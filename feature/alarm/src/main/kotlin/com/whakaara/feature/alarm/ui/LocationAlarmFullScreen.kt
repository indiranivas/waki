package com.whakaara.feature.alarm.ui

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.whakaara.core.designsystem.WakiTimeDisplay
import com.whakaara.core.designsystem.theme.wakiOrange
import com.whakaara.model.location.LocationAlarm
import com.whakaara.model.preferences.TimeFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun LocationAlarmFullScreen(
    alarm: LocationAlarm,
    onStop: () -> Unit,
    onSnooze: () -> Unit,
    timeFormat: TimeFormat
) {
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
            modifier = Modifier
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
                    text = "$greeting, Nivas",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = alarm.name,
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
                    onClick = onStop,
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

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    onClick = onSnooze
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
