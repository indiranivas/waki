package com.whakaara.feature.alarm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.whakaara.core.designsystem.WakiCard
import com.whakaara.core.designsystem.WakiScaffold
import com.whakaara.model.location.Place

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchLocationScreen(
    savedPlaces: List<Place>,
    recentPlaces: List<Place>,
    savedPlaceBanner: String? = null,
    onBack: () -> Unit,
    onSearch: (String) -> Unit,
    onCurrentLocationClick: () -> Unit,
    onPickOnMapClick: () -> Unit,
    onSavedPlaceClick: (Place, Boolean) -> Unit,
    onEditSavedPlace: (Place) -> Unit,
    onRecentPlaceClick: (Place) -> Unit,
    onAddSavedPlace: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    WakiScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = savedPlaceBanner ?: "Set Location",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (savedPlaceBanner != null) {
                item {
                    Text(
                        text = "Search or pick a place on the map to save it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearch(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search address or place...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                )
            }

            item {
                WakiCard(onClick = onCurrentLocationClick) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Use current location",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            item {
                WakiCard(onClick = onPickOnMapClick) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Pick on map",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            if (savedPlaceBanner == null) {
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Saved Places",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Add",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable { onAddSavedPlace() },
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (savedPlaces.isEmpty()) {
                            Text(
                                text = "No saved places yet. Tap Add to create one.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(savedPlaces) { place ->
                                    val icon = when (place.name.lowercase()) {
                                        "home" -> Icons.Default.Home
                                        "work" -> Icons.Default.Work
                                        else -> Icons.Default.LocationOn
                                    }
                                    SavedPlaceCard(
                                        title = place.name,
                                        address = place.address,
                                        icon = icon,
                                        onClick = {
                                            val isWork = place.name.equals("Work", ignoreCase = true)
                                            onSavedPlaceClick(place, isWork)
                                        },
                                        onEditClick = { onEditSavedPlace(place) },
                                    )
                                }
                            }
                        }
                    }
                }

                if (recentPlaces.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                text = "Recent Places",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            recentPlaces.forEach { place ->
                                RecentPlaceItem(
                                    name = place.name,
                                    address = place.address,
                                    onClick = { onRecentPlaceClick(place) },
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedPlaceCard(
    title: String,
    address: String,
    icon: ImageVector,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    WakiCard(modifier = Modifier.width(168.dp), onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
                }
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit saved place",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(
                text = address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        }
    }
}

@Composable
fun RecentPlaceItem(
    name: String,
    address: String,
    onClick: () -> Unit
) {
    WakiCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = name, fontWeight = FontWeight.Bold)
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}
