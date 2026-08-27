package com.whakaara.feature.alarm.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.whakaara.core.designsystem.WakiCard
import com.whakaara.core.designsystem.WakiScaffold
import com.whakaara.core.designsystem.WakiPrimaryButton
import com.whakaara.core.designsystem.theme.wakiOrange
import com.whakaara.model.location.Place

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    query: String,
    results: List<Place>,
    isSearching: Boolean = false,
    nearUser: Boolean = false,
    onBack: () -> Unit,
    onClear: () -> Unit,
    onPlaceSelected: (Place) -> Unit,
    onMapViewClick: () -> Unit
) {
    WakiScaffold(
        topBar = {
            TopAppBar(
                title = { Text(query, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* filter */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        },
        floatingActionButton = {
            WakiPrimaryButton(
                text = "Map View",
                onClick = onMapViewClick,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .width(180.dp)
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        when {
            isSearching -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = wakiOrange)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = if (nearUser) "Showing results near you" else "Search results",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${results.size} locations found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Clear",
                                color = Color.Red.copy(alpha = 0.75f),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable { onClear() }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    itemsIndexed(results, key = { _, p -> "${p.latitude},${p.longitude},${p.name}" }) { index, place ->
                        SearchResultCard(
                            place = place,
                            statusLabel = when (index % 3) {
                                0 -> "Open"
                                1 -> "Closes Soon"
                                else -> "Closed"
                            },
                            statusColor = when (index % 3) {
                                0 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                1 -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
                                else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
                            },
                            onViewDetails = { onPlaceSelected(place) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    place: Place,
    statusLabel: String,
    statusColor: Color,
    onViewDetails: () -> Unit
) {
    WakiCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = place.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = statusColor, shape = RoundedCornerShape(12.dp)) {
                            Text(
                                text = statusLabel,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    Text(
                        text = place.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = place.distance?.split(" ")?.firstOrNull() ?: "—",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = wakiOrange
                    )
                    Text(
                        text = place.distance?.split(" ")?.drop(1)?.joinToString(" ").orEmpty().ifBlank { "miles" },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Directions")
                }
                Button(
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = wakiOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View Details")
                }
            }
        }
    }
}
