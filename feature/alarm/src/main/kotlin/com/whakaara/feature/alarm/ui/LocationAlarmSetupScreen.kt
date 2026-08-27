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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.whakaara.core.designsystem.WakiCard
import com.whakaara.core.designsystem.WakiPrimaryButton
import com.whakaara.core.designsystem.WakiToggle
import com.whakaara.feature.alarm.LocationAlarmEditorState
import com.whakaara.model.location.LocationTriggerType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationAlarmSetupScreen(
    editorState: LocationAlarmEditorState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onRadiusChange: (Int) -> Unit,
    onNotifyBeforeArrivalChange: (Boolean) -> Unit,
    onDepartureDelayChange: (Int) -> Unit,
    onSave: () -> Unit
) {
    val place = editorState.place ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Up Alarm", fontWeight = FontWeight.Bold) },
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
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                OsmdroidMapView(
                    latitude = place.latitude,
                    longitude = place.longitude,
                    radiusMeters = editorState.radiusMeters,
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
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = place.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = place.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = when (editorState.triggerType) {
                        LocationTriggerType.ARRIVE -> "Arrival alarm"
                        LocationTriggerType.LEAVE -> "Departure alarm"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = editorState.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Alarm name") },
                    singleLine = true
                )

                WakiCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Radius",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${editorState.radiusMeters}m",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = editorState.radiusMeters.toFloat(),
                        onValueChange = { onRadiusChange(it.toInt()) },
                        valueRange = 100f..2000f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("100m", style = MaterialTheme.typography.labelSmall)
                        Text("2km", style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (editorState.triggerType == LocationTriggerType.ARRIVE) {
                    WakiCard {
                        WakiToggle(
                            checked = editorState.notifyBeforeArrival,
                            onCheckedChange = onNotifyBeforeArrivalChange,
                            title = "Early warning",
                            subtitle = "Get notified before you arrive"
                        )
                    }
                } else {
                    WakiCard {
                        Text(
                            text = "Departure delay",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DelayChip(
                                label = "None",
                                selected = editorState.departureDelayMinutes == 0,
                                onClick = { onDepartureDelayChange(0) }
                            )
                            DelayChip(
                                label = "5 min",
                                selected = editorState.departureDelayMinutes == 5,
                                onClick = { onDepartureDelayChange(5) }
                            )
                            DelayChip(
                                label = "10 min",
                                selected = editorState.departureDelayMinutes == 10,
                                onClick = { onDepartureDelayChange(10) }
                            )
                        }
                    }
                }

                editorState.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DelayChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    WakiCard(
        onClick = onClick,
        containerColor = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        }
    ) {
        Text(
            text = label,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
