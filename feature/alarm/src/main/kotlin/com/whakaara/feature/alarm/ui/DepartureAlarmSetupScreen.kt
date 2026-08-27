package com.whakaara.feature.alarm.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RunCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.whakaara.core.designsystem.WakiCard
import com.whakaara.core.designsystem.WakiPrimaryButton
import com.whakaara.core.designsystem.WakiScaffold
import com.whakaara.core.designsystem.theme.wakiOrange
import com.whakaara.feature.alarm.LocationAlarmEditorState
import com.whakaara.model.location.Place

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartureAlarmSetupScreen(
    place: Place,
    editorState: LocationAlarmEditorState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onDepartureDelayChange: (Int) -> Unit,
    onRadiusChange: (Int) -> Unit,
    onLocateMe: () -> Unit,
    onSave: () -> Unit
) {
    val selectedTriggerIndex = if (editorState.departureDelayMinutes > 0) 1 else 0

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
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Departure",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Alert me when I leave this area",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            LocationMapWithControls(
                latitude = place.latitude,
                longitude = place.longitude,
                radiusMeters = editorState.radiusMeters,
                showGeofence = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                onLocateMe = onLocateMe
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Location Name", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = editorState.name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            WakiCard {
                TriggerOption(
                    title = "Immediate alert",
                    subtitle = "As soon as I leave",
                    icon = Icons.Default.RunCircle,
                    selected = selectedTriggerIndex == 0,
                    onClick = { onDepartureDelayChange(0) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                TriggerOption(
                    title = "Alert after 5 minutes",
                    subtitle = "To prevent false alarms",
                    icon = Icons.Default.History,
                    selected = selectedTriggerIndex == 1,
                    onClick = { onDepartureDelayChange(5) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Departure Radius", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
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
                valueRange = 100f..1000f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("100m", style = MaterialTheme.typography.labelSmall)
                Text("1km", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TriggerOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .padding(0.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = wakiOrange)
        )
    }
}
