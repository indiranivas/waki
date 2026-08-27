package com.whakaara.feature.alarm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.whakaara.core.designsystem.liquidGlass

@Composable
fun LocationMapWithControls(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    radiusMeters: Int = 0,
    showGeofence: Boolean = radiusMeters > 0,
    zoom: Double = 15.0,
    onLocateMe: (() -> Unit)? = null
) {
    var currentZoom by remember(latitude, longitude) { mutableDoubleStateOf(zoom) }

    Box(modifier = modifier) {
        OsmdroidMapView(
            latitude = latitude,
            longitude = longitude,
            radiusMeters = if (showGeofence) radiusMeters else 0,
            zoom = currentZoom,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MapFloatingButton(
                icon = Icons.Default.MyLocation,
                onClick = {
                    onLocateMe?.invoke()
                    currentZoom = 16.0
                }
            )
            MapFloatingButton(
                icon = Icons.Default.Add,
                onClick = { currentZoom = (currentZoom + 1).coerceAtMost(20.0) }
            )
            MapFloatingButton(
                icon = Icons.Default.Remove,
                onClick = { currentZoom = (currentZoom - 1).coerceAtLeast(5.0) }
            )
        }
    }
}

@Composable
fun MapFloatingButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .liquidGlass(shape = CircleShape, surfaceAlpha = 0.9f, elevation = 6.dp),
        shape = CircleShape,
        color = Color.Transparent,
        shadowElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
