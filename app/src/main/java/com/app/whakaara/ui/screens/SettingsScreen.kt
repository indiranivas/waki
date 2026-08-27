package com.app.whakaara.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.app.whakaara.R
import com.app.whakaara.ui.navigation.BottomNavItem
import com.app.whakaara.ui.settings.AlarmSettings
import com.app.whakaara.ui.settings.AppInfoDisplay
import com.app.whakaara.ui.settings.GeneralSettings
import com.app.whakaara.ui.settings.TimerSettings
import com.whakaara.core.designsystem.theme.FontScalePreviews
import com.whakaara.core.designsystem.theme.RoutePreviewProvider
import com.whakaara.core.designsystem.theme.Spacings.space20
import com.whakaara.core.designsystem.theme.Spacings.spaceMedium
import com.whakaara.core.designsystem.theme.ThemePreviews
import com.whakaara.core.designsystem.theme.WakiTheme
import com.whakaara.model.preferences.Preferences
import com.whakaara.model.preferences.PreferencesState
import com.whakaara.model.preferences.TimeFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    route: String,
    preferencesState: PreferencesState,
    updatePreferences: (preferences: Preferences) -> Unit,
    updateAllAlarmSubtitles: (format: TimeFormat) -> Unit,
    updateCurrentAlarmsToAddOrRemoveUpcomingAlarmNotification: (shouldEnableUpcomingAlarmNotification: Boolean) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = stringResource(id = R.string.settings_screen_title),
                modifier = Modifier
                    .padding(vertical = 24.dp),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            GeneralSettings(
                preferencesState = preferencesState,
                updatePreferences = updatePreferences,
                updateAllAlarmSubtitles = updateAllAlarmSubtitles
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (route) {
                BottomNavItem.Alarm.route -> {
                    AlarmSettings(
                        preferencesState = preferencesState,
                        updatePreferences = updatePreferences,
                        updateCurrentAlarmsToAddOrRemoveUpcomingAlarmNotification = updateCurrentAlarmsToAddOrRemoveUpcomingAlarmNotification
                    )
                }
                BottomNavItem.Timer.route -> {
                    TimerSettings(
                        preferencesState = preferencesState,
                        updatePreferences = updatePreferences
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            AppInfoDisplay()
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
@ThemePreviews
@FontScalePreviews
fun SettingsScreenPreview(
    @PreviewParameter(RoutePreviewProvider::class) route: String
) {
    WakiTheme {
        SettingsScreen(
            route = route,
            preferencesState = PreferencesState(),
            updatePreferences = {},
            updateAllAlarmSubtitles = {},
            updateCurrentAlarmsToAddOrRemoveUpcomingAlarmNotification = {}
        )
    }
}
