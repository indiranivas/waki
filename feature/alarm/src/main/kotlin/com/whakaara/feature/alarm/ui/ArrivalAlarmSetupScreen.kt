package com.whakaara.feature.alarm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.whakaara.core.designsystem.WakiCard
import com.whakaara.core.designsystem.WakiPrimaryButton
import com.whakaara.core.designsystem.WakiScaffold
import com.whakaara.core.designsystem.WakiToggle
import com.whakaara.feature.alarm.LocationAlarmEditorState
import com.whakaara.model.location.Place

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArrivalAlarmSetupScreen(
    place: Place,
    editorState: LocationAlarmEditorState,
    onBack: () -> Unit,
    onAlarmOnArrivalChange: (Boolean) -> Unit,
    onNotifyWhenNearChange: (Boolean) -> Unit,
    onRadiusChange: (Int) -> Unit,
    onSave: () -> Unit
) {
    WakiScaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Alarm", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            WakiPrimaryButton(
                text = if (editorState.isSaving) "Saving..." else "Save Alarm",
                onClick = onSave,
                enabled = !editorState.isSaving,
                modifier = Modifier.padding(24.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                OsmdroidMapView(
                    latitude = place.latitude,
                    longitude = place.longitude,
                    radiusMeters = if (editorState.notifyBeforeArrival) editorState.radiusMeters else 0,
                    modifier = Modifier.fillMaxSize()
                )

                WakiCard(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = place.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = place.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                WakiCard {
                    WakiToggle(
                        checked = editorState.alarmOnArrival,
                        onCheckedChange = onAlarmOnArrivalChange,
                        title = "Alarm on Arrival",
                        subtitle = "Wake me when I reach the stop"
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    WakiToggle(
                        checked = editorState.notifyBeforeArrival,
                        onCheckedChange = onNotifyWhenNearChange,
                        title = "Notify when near",
                        subtitle = "Trigger alarm before arrival"
                    )

                    if (editorState.notifyBeforeArrival) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Distance", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(
                                text = "${editorState.radiusMeters}m",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = editorState.radiusMeters.toFloat(),
                            onValueChange = { onRadiusChange(it.toInt()) },
                            valueRange = 100f..2000f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("100m", style = MaterialTheme.typography.labelSmall)
                            Text("2km", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                WakiCard(onClick = null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Alarm Sound", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(text = "Default from settings", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
