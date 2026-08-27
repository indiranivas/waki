package com.whakaara.feature.alarm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.whakaara.core.designsystem.WakiPrimaryButton
import com.whakaara.core.designsystem.WakiScaffold
import com.whakaara.feature.alarm.MapPickerMode
import com.whakaara.feature.alarm.MapPickerState
import com.whakaara.feature.alarm.SelectionEdge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickOnMapScreen(
    pickerState: MapPickerState,
    onBack: () -> Unit,
    onSearch: (String) -> Unit,
    onLocationPicked: (Double, Double) -> Unit,
    onModeChange: (MapPickerMode) -> Unit,
    onRangeRadiusChange: (Int) -> Unit,
    onEdgeExpand: (SelectionEdge) -> Unit,
    onLocateMe: () -> Unit,
    onConfirm: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val isExact = pickerState.mode == MapPickerMode.Exact

    WakiScaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pick on map", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                onSearch(searchQuery)
                            }
                        },
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                if (isExact) {
                    Text(
                        text = "${pickerState.widthMeters} × ${pickerState.heightMeters} m",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Range",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "${pickerState.rangeRadiusMeters}m",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Slider(
                        value = pickerState.rangeRadiusMeters.toFloat(),
                        onValueChange = { onRangeRadiusChange(it.toInt()) },
                        valueRange = 100f..1000f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("100m", style = MaterialTheme.typography.labelSmall)
                        Text("1km", style = MaterialTheme.typography.labelSmall)
                    }
                }
                WakiPrimaryButton(
                    text = if (pickerState.isConfirming) {
                        "Loading..."
                    } else if (isExact) {
                        "Use this area"
                    } else {
                        "Use this location"
                    },
                    onClick = onConfirm,
                    enabled = !pickerState.isConfirming,
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    onSearch(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("Search address or place...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                ),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = pickerState.mode == MapPickerMode.Range,
                    onClick = { onModeChange(MapPickerMode.Range) },
                    label = { Text("Range") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
                FilterChip(
                    selected = pickerState.mode == MapPickerMode.Exact,
                    onClick = { onModeChange(MapPickerMode.Exact) },
                    label = { Text("Exact location") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }

            Text(
                text = if (isExact) {
                    "Drag the square to move it. Tap a side dot to double that side."
                } else {
                    "Tap the map to place the pin, then set the range with the slider."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            )

            Box(modifier = Modifier.fillMaxSize()) {
                OsmdroidMapPickerView(
                    centerLatitude = pickerState.centerLatitude,
                    centerLongitude = pickerState.centerLongitude,
                    selectedLatitude = pickerState.selectionLatitude,
                    selectedLongitude = pickerState.selectionLongitude,
                    mode = pickerState.mode,
                    rangeRadiusMeters = pickerState.rangeRadiusMeters,
                    widthMeters = pickerState.widthMeters,
                    heightMeters = pickerState.heightMeters,
                    onLocationPicked = onLocationPicked,
                    onEdgeExpand = onEdgeExpand,
                    modifier = Modifier.fillMaxSize(),
                )

                MapFloatingButton(
                    icon = Icons.Default.MyLocation,
                    onClick = onLocateMe,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                )
            }
        }
    }
}
