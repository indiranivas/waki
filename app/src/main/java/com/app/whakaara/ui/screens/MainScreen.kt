package com.app.whakaara.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldValue
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.rememberNavController
import com.app.whakaara.state.events.AppViewModels
import com.app.whakaara.state.events.PreferencesEventCallbacks
import com.app.whakaara.ui.navigation.BottomNavItem
import com.app.whakaara.ui.navigation.NavGraph
import com.app.whakaara.ui.navigation.navigateToRootScreen
import com.dokar.sheets.BottomSheet
import com.dokar.sheets.rememberBottomSheetState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.shouldShowRationale
import com.marosseleng.compose.material3.datetimepickers.time.domain.noSeconds
import com.marosseleng.compose.material3.datetimepickers.time.ui.dialog.TimePickerDialog
import com.whakaara.core.LeafScreen
import com.whakaara.core.NotificationUtils
import com.whakaara.core.RootScreen
import com.whakaara.core.constants.DateUtilsConstants
import com.whakaara.core.designsystem.FloatingActionButtonRow
import com.whakaara.core.designsystem.WakiMotion
import com.whakaara.core.designsystem.theme.LocalWakiDarkTheme
import com.whakaara.core.designsystem.WakiScreenBackground
import com.whakaara.core.designsystem.WakiCard
import com.whakaara.core.designsystem.WakiTimeDisplay
import com.whakaara.core.designsystem.theme.FontScalePreviews
import com.whakaara.core.designsystem.theme.ThemePreviews
import com.whakaara.core.designsystem.theme.WakiTheme
import com.whakaara.core.rememberPermissionStateSafe
import com.whakaara.feature.alarm.R
import com.whakaara.feature.alarm.utils.GeneralUtils.Companion.showToast
import com.whakaara.model.alarm.AlarmState
import com.whakaara.model.preferences.AppTheme
import com.whakaara.model.preferences.Preferences
import com.whakaara.model.preferences.PreferencesState
import com.whakaara.model.preferences.TimeFormat
import com.whakaara.model.timer.TimerState
import kotlinx.coroutines.launch
import java.time.LocalTime

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    preferencesState: PreferencesState,
    preferencesEventCallbacks: PreferencesEventCallbacks,
    viewModels: AppViewModels,
    timerState: TimerState,
) {
    val navController = rememberNavController()
    val currentSelectedScreen by navController.currentScreenAsState()
    val currentRoute by navController.currentRouteAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val navigationSuiteState = rememberNavigationSuiteScaffoldState(initialValue = NavigationSuiteScaffoldValue.Hidden)
    val context = LocalContext.current
    val isDialogShown = rememberSaveable { mutableStateOf(false) }
    val settingsSheetState = rememberBottomSheetState()
    val notificationPermissionState = rememberPermissionStateSafe(permission = Manifest.permission.POST_NOTIFICATIONS)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { wasGranted ->
        when (currentRoute) {
            LeafScreen.Timer.route -> {
                if (wasGranted) {
                    viewModels.timer.startTimer()
                }
            }

            LeafScreen.Alarm.route -> {
                if (wasGranted) {
                    isDialogShown.value = !isDialogShown.value
                }
            }
        }
    }
    val navItems = listOf(
        BottomNavItem.Alarm,
        BottomNavItem.Timer,
        BottomNavItem.Stopwatch,
        BottomNavItem.Locations
    )

    val alarmState by viewModels.alarm.alarmState.collectAsStateWithLifecycle()
    val nextAlarm = remember(alarmState) {
        if (alarmState is AlarmState.Success) {
            (alarmState as AlarmState.Success).alarms
                .filter { it.isEnabled }
                .minByOrNull { it.date.timeInMillis }
        } else null
    }

    val locationAlarms by viewModels.location.alarms.collectAsStateWithLifecycle()
    val nextLocationAlarm = remember(locationAlarms) {
        locationAlarms.filter { it.enabled }.firstOrNull() // Simplified logic
    }

    LaunchedEffect(key1 = preferencesState.preferences.shouldShowOnboarding) {
        scope.launch {
            if (preferencesState.preferences.shouldShowOnboarding) {
                navigationSuiteState.hide()
            } else {
                navigationSuiteState.show()
            }
        }
    }

    WakiScreenBackground {
        NavigationSuiteScaffold(
            state = navigationSuiteState,
            containerColor = Color.Transparent,
            navigationSuiteColors = NavigationSuiteDefaults.colors(
                navigationBarContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                navigationBarContentColor = MaterialTheme.colorScheme.onSurface
            ),
            navigationSuiteItems = {
            navItems.forEachIndexed { _, bottomNavItem ->
                item(
                    selected = bottomNavItem.rootScreen == currentSelectedScreen,
                    icon = { Icon(imageVector = bottomNavItem.icon, contentDescription = bottomNavItem.title) },
                    label = { Text(text = bottomNavItem.title) },
                    onClick = {
                        navController.navigateToRootScreen(bottomNavItem.rootScreen)
                    }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (!preferencesState.preferences.shouldShowOnboarding) {
                    AnimatedContent(
                        targetState = currentRoute,
                        transitionSpec = {
                            WakiMotion.tabEnter() togetherWith WakiMotion.tabExit()
                        },
                        label = "mainTopBar"
                    ) { route ->
                        when (route) {
                        LeafScreen.Alarm.route -> {
                            Column(
                                modifier = Modifier
                                    .statusBarsPadding()
                                    .padding(horizontal = 24.dp, vertical = 20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = getGreeting(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Nivas", // Placeholder as per mockup
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val isDarkTheme = LocalWakiDarkTheme.current
                                        val themeIcon = when (preferencesState.preferences.appTheme) {
                                            AppTheme.MODE_DAY -> Icons.Default.LightMode
                                            AppTheme.MODE_NIGHT -> Icons.Default.DarkMode
                                            AppTheme.MODE_AUTO -> Icons.Default.BrightnessAuto
                                        }
                                        IconButton(
                                            onClick = {
                                                val newTheme = when (preferencesState.preferences.appTheme) {
                                                    AppTheme.MODE_AUTO -> AppTheme.MODE_DAY
                                                    AppTheme.MODE_DAY -> AppTheme.MODE_NIGHT
                                                    AppTheme.MODE_NIGHT -> AppTheme.MODE_AUTO
                                                }
                                                preferencesEventCallbacks.updatePreferences(
                                                    preferencesState.preferences.copy(appTheme = newTheme)
                                                )
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = themeIcon,
                                                contentDescription = "Toggle Theme",
                                                tint = if (!isDarkTheme) Color(0xFFF1C40F) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        IconButton(
                                            onClick = {
                                                scope.launch { settingsSheetState.expand() }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = "Settings",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                WakiCard(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                ) {
                                    Column {
                                        Text(
                                            text = "NEXT ALARM",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 1.sp
                                        )
                                        if (nextAlarm != null) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    WakiTimeDisplay(
                                                        time = nextAlarm.subTitle.filter { it.isDigit() || it == ':' },
                                                        amPm = nextAlarm.subTitle.filter { it.isLetter() }
                                                    )
                                                    Text(
                                                        text = nextAlarm.title,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = "Tomorrow",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Switch(
                                                    checked = true,
                                                    onCheckedChange = { viewModels.alarm.disable(nextAlarm) }
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "No alarms set",
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier.padding(vertical = 12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        LeafScreen.SmartAlarm.route, LeafScreen.Timer.route, LeafScreen.Stopwatch.route -> {
                            Box(
                                modifier = Modifier
                                    .statusBarsPadding()
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (route) {
                                        LeafScreen.Timer.route -> "Timer"
                                        LeafScreen.Stopwatch.route -> "Stopwatch"
                                        else -> "Locations"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        else -> Unit
                        }
                    }
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = currentRoute == LeafScreen.Timer.route,
                    enter = scaleIn(WakiMotion.gentleSpring) + fadeIn(WakiMotion.gentleSpring),
                    exit = scaleOut(WakiMotion.softSpring) + fadeOut(WakiMotion.softSpring)
                ) {
                    FloatingActionButtonRow(
                        isPlaying = timerState.isTimerActive,
                        isStart = timerState.isStart,
                        isPlayButtonVisible = timerState.inputHours != DateUtilsConstants.TIMER_INPUT_INITIAL_VALUE ||
                            timerState.inputMinutes != DateUtilsConstants.TIMER_INPUT_INITIAL_VALUE ||
                            timerState.inputSeconds != DateUtilsConstants.TIMER_INPUT_INITIAL_VALUE ||
                            timerState.isTimerActive ||
                            timerState.isTimerPaused,
                        stopIcon = Icons.Filled.Refresh,
                        extraIcon = Icons.Filled.Add,
                        isExtraButtonVisible = timerState.isTimerActive || timerState.isTimerPaused,
                        onStop = viewModels.timer::resetTimer,
                        onPlayPause = {
                            if (timerState.isTimerActive) {
                                viewModels.timer.pauseTimer()
                            } else {
                                when (notificationPermissionState.status) {
                                    PermissionStatus.Granted -> {
                                        viewModels.timer.startTimer()
                                    }

                                    else -> {
                                        if (notificationPermissionState.status.shouldShowRationale) {
                                            NotificationUtils.snackBarPromptPermission(
                                                scope = scope,
                                                snackBarHostState = snackbarHostState,
                                                context = context
                                            )
                                        } else {
                                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    }
                                }
                            }
                        },
                        onExtraButtonClicked = {
                            viewModels.timer.addTime(minutes = 1)
                        }
                    )
                }

                AnimatedVisibility(
                    visible = currentRoute == LeafScreen.Alarm.route,
                    enter = scaleIn(WakiMotion.gentleSpring) + fadeIn(WakiMotion.gentleSpring),
                    exit = scaleOut(WakiMotion.softSpring) + fadeOut(WakiMotion.softSpring)
                ) {
                    FloatingActionButton(
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp
                        ),
                        onClick = {
                            when (notificationPermissionState.status) {
                                PermissionStatus.Granted -> {
                                    isDialogShown.value = !isDialogShown.value
                                }

                                else -> {
                                    if (notificationPermissionState.status.shouldShowRationale) {
                                        NotificationUtils.snackBarPromptPermission(
                                            scope = scope,
                                            snackBarHostState = snackbarHostState,
                                            context = context
                                        )
                                    } else {
                                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(id = R.string.floating_action_button_icon_description)
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            contentWindowInsets = WindowInsets.safeContent
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                NavGraph(
                    navController = navController,
                    shouldShowOnboarding = preferencesState.preferences.shouldShowOnboarding,
                    viewModels = viewModels
                )
            }
        }
    }
    }

    AnimatedVisibility(isDialogShown.value) {
        TimePickerDialog(
            onDismissRequest = { isDialogShown.value = false },
            initialTime = LocalTime.now().plusMinutes(1).noSeconds(),
            onTimeChange = { localTime ->
                viewModels.alarm.create(
                    localTime = localTime,
                )
                isDialogShown.value = false
                context.showToast(
                    message = viewModels.alarm.getTimeUntilAlarmFormatted(localTime = localTime)
                )
            },
            title = { Text(text = stringResource(id = R.string.time_picker_dialog_title)) },
            is24HourFormat = preferencesState.preferences.timeFormat == TimeFormat.TWENTY_FOUR_HOURS
        )
    }

    BottomSheet(
        backgroundColor = MaterialTheme.colorScheme.surface,
        state = settingsSheetState,
        skipPeeked = true
    ) {
        SettingsScreen(
            route = currentRoute ?: "",
            preferencesState = preferencesState,
            updatePreferences = preferencesEventCallbacks::updatePreferences,
            updateAllAlarmSubtitles = preferencesEventCallbacks::updateAllAlarmSubtitles,
            updateCurrentAlarmsToAddOrRemoveUpcomingAlarmNotification = preferencesEventCallbacks::updateCurrentAlarmsToAddOrRemoveUpcomingAlarmNotification
        )
    }
}

@Composable
fun getGreeting(): String {
    val hour = LocalTime.now().hour
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Good night"
    }
}

@Composable
@ThemePreviews
@FontScalePreviews
fun MainPreview() {
    WakiTheme {
        MainScreen(
            preferencesState = PreferencesState(),
            preferencesEventCallbacks = object : PreferencesEventCallbacks {
                override fun updatePreferences(preferences: Preferences) {}

                override fun updateAllAlarmSubtitles(format: TimeFormat) {}

                override fun updateCurrentAlarmsToAddOrRemoveUpcomingAlarmNotification(
                    shouldEnableUpcomingAlarmNotification: Boolean
                ) {
                }
            },
            viewModels = AppViewModels(
                main = hiltViewModel(),
                timer = hiltViewModel(),
                stopwatch = hiltViewModel(),
                alarm = hiltViewModel(),
                location = hiltViewModel()
            ),
            timerState = TimerState(),
        )
    }
}

@Stable
@Composable
private fun NavController.currentScreenAsState(): State<RootScreen> {
    val selectedItem = remember { mutableStateOf<RootScreen>(RootScreen.Alarm) }
    DisposableEffect(key1 = this) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            when {
                destination.hierarchy.any { it.route == RootScreen.Alarm.route } -> {
                    selectedItem.value = RootScreen.Alarm
                }

                destination.hierarchy.any { it.route == RootScreen.Stopwatch.route } -> {
                    selectedItem.value = RootScreen.Stopwatch
                }

                destination.hierarchy.any { it.route == RootScreen.Timer.route } -> {
                    selectedItem.value = RootScreen.Timer
                }

                destination.hierarchy.any { it.route == RootScreen.SmartAlarm.route } -> {
                    selectedItem.value = RootScreen.SmartAlarm
                }

//                destination.hierarchy.any { it.route == RootScreen.Settings.route } -> {
//                    selectedItem.value = RootScreen.Settings
//                }
            }
        }
        addOnDestinationChangedListener(listener)
        onDispose {
            removeOnDestinationChangedListener(listener)
        }
    }
    return selectedItem
}

@Stable
@Composable
private fun NavController.currentRouteAsState(): State<String?> {
    val selectedItem = remember { mutableStateOf<String?>(null) }
    DisposableEffect(this) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            selectedItem.value = destination.route
        }
        addOnDestinationChangedListener(listener)

        onDispose {
            removeOnDestinationChangedListener(listener)
        }
    }
    return selectedItem
}
