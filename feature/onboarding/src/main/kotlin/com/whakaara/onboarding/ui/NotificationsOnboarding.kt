package com.whakaara.onboarding.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.whakaara.core.AppPermissions
import com.whakaara.core.designsystem.WakiCard
import com.whakaara.core.designsystem.theme.FontScalePreviews
import com.whakaara.core.designsystem.theme.ThemePreviews
import com.whakaara.core.designsystem.theme.WakiTheme
import com.whakaara.core.rememberPermissionStateSafe

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationsOnboarding(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val notificationPermissionState = rememberPermissionStateSafe(permission = Manifest.permission.POST_NOTIFICATIONS)
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    var backgroundLocationGranted by remember {
        mutableStateOf(AppPermissions.hasBackgroundLocation(context))
    }
    val batteryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        backgroundLocationGranted = AppPermissions.hasBackgroundLocation(context)
    }
    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        backgroundLocationGranted = granted || AppPermissions.hasBackgroundLocation(context)
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !notificationPermissionState.status.isGranted
        ) {
            notificationPermissionState.launchPermissionRequest()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Let's set things up",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "These permissions help Waki work perfectly for you.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PermissionRow(
                title = "Notifications",
                subtitle = "Send you alarms and alerts",
                icon = Icons.Default.Notifications,
                isGranted = notificationPermissionState.status.isGranted,
                onRequest = { notificationPermissionState.launchPermissionRequest() }
            )

            PermissionRow(
                title = "Location",
                subtitle = "Required for location alarms in the background",
                icon = Icons.Default.LocationOn,
                isGranted = locationPermissions.allPermissionsGranted && backgroundLocationGranted,
                onRequest = {
                    if (!locationPermissions.allPermissionsGranted) {
                        locationPermissions.launchMultiplePermissionRequest()
                    } else if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                        !AppPermissions.hasBackgroundLocation(context)
                    ) {
                        backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                }
            )

            PermissionRow(
                title = "Exact Alarms",
                subtitle = "Ensure alarms trigger on time",
                icon = Icons.Default.Star,
                isGranted = AppPermissions.canScheduleExactAlarms(context)
            )

            PermissionRow(
                title = "Battery Optimization",
                subtitle = "Allow Waki to run reliably in the background",
                icon = Icons.Default.Settings,
                isGranted = AppPermissions.isIgnoringBatteryOptimizations(context),
                onRequest = {
                    batteryLauncher.launch(
                        AppPermissions.requestIgnoreBatteryOptimizationsIntent(context)
                    )
                }
            )
        }
    }
}

@Composable
fun PermissionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isGranted: Boolean,
    onRequest: (() -> Unit)? = null
) {
    WakiCard(onClick = if (!isGranted && onRequest != null) onRequest else null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = Color(0xFF2ECC71),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
@ThemePreviews
@FontScalePreviews
fun NotificationsOnboardingPreview() {
    WakiTheme {
        NotificationsOnboarding(
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
