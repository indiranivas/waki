package com.whakaara.feature.alarm.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.whakaara.core.AppPermissions
import com.whakaara.core.designsystem.navigation.wakiComposable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.whakaara.core.LeafScreen
import com.whakaara.feature.alarm.LocationAlarmViewModel
import com.whakaara.feature.alarm.ui.ArrivalAlarmSetupScreen
import com.whakaara.feature.alarm.ui.DepartureAlarmSetupScreen
import com.whakaara.feature.alarm.ui.LocationAlarmsScreen
import com.whakaara.feature.alarm.ui.PickOnMapScreen
import com.whakaara.feature.alarm.ui.SearchLocationScreen
import com.whakaara.feature.alarm.ui.SearchResultsScreen

@OptIn(ExperimentalPermissionsApi::class)

fun NavGraphBuilder.locationAlarmScreen(

    viewModel: LocationAlarmViewModel,

    navController: NavController

) {

    val locationPermissions = listOf(

        Manifest.permission.ACCESS_FINE_LOCATION,

        Manifest.permission.ACCESS_COARSE_LOCATION

    )

    wakiComposable(route = LeafScreen.SmartAlarm.route) {
        val context = LocalContext.current
        val alarms by viewModel.alarms.collectAsStateWithLifecycle()
        val permissionState = rememberMultiplePermissionsState(locationPermissions)
        val backgroundLocationLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { _ ->
            if (AppPermissions.hasBackgroundLocation(context)) {
                navController.navigate(LeafScreen.SearchLocation.route)
            }
        }

        fun proceedWithLocationAccess(onReady: () -> Unit) {
            when {
                !permissionState.allPermissionsGranted -> {
                    permissionState.launchMultiplePermissionRequest()
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    !AppPermissions.hasBackgroundLocation(context) -> {
                    backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
                else -> onReady()
            }
        }

        LocationAlarmsScreen(
            alarms = alarms,
            onAddClick = {
                proceedWithLocationAccess {
                    navController.navigate(LeafScreen.SearchLocation.route)
                }
            },
            onToggleAlarm = { alarm ->
                proceedWithLocationAccess {
                    viewModel.toggleLocationAlarm(alarm)
                }
            },
            onDeleteAlarm = { viewModel.deleteLocationAlarm(it) }
        )
    }

    wakiComposable(route = LeafScreen.SearchLocation.route) {
        val permissionState = rememberMultiplePermissionsState(locationPermissions)
        val recentPlaces by viewModel.recentPlaces.collectAsStateWithLifecycle()
        val savedPlaces by viewModel.savedPlaces.collectAsStateWithLifecycle()
        val savedPlaceDraft by viewModel.savedPlaceDraft.collectAsStateWithLifecycle()
        val managingSavedPlace = savedPlaceDraft !is com.whakaara.feature.alarm.SavedPlaceDraft.Idle
        val banner = when (val draft = savedPlaceDraft) {
            is com.whakaara.feature.alarm.SavedPlaceDraft.Adding -> "Add saved place"
            is com.whakaara.feature.alarm.SavedPlaceDraft.Editing -> "Edit ${draft.existingName}"
            else -> null
        }

        SearchLocationScreen(
            savedPlaces = savedPlaces,
            recentPlaces = recentPlaces,
            savedPlaceBanner = banner,
            onBack = {
                if (managingSavedPlace) {
                    viewModel.clearSavedPlaceDraft()
                } else {
                    navController.popBackStack()
                }
            },
            onSearch = { query ->
                viewModel.searchPlaces(query)
                if (query.length >= 3) {
                    navController.navigate(LeafScreen.SearchResults.route)
                }
            },
            onCurrentLocationClick = {
                if (permissionState.allPermissionsGranted) {
                    viewModel.useCurrentLocation(forDeparture = false) {
                        if (managingSavedPlace) {
                            navController.popBackStack(LeafScreen.SearchLocation.route, inclusive = false)
                        } else {
                            navController.navigate(LeafScreen.ArrivalSetup.route)
                        }
                    }
                } else {
                    permissionState.launchMultiplePermissionRequest()
                }
            },
            onPickOnMapClick = {
                if (permissionState.allPermissionsGranted) {
                    viewModel.openMapPicker {
                        navController.navigate(LeafScreen.PickOnMap.route)
                    }
                } else {
                    permissionState.launchMultiplePermissionRequest()
                }
            },
            onSavedPlaceClick = { place, forDeparture ->
                viewModel.resolveSavedPlace(
                    place = place,
                    forDeparture = forDeparture,
                    onReady = {
                        if (forDeparture) {
                            navController.navigate(LeafScreen.DepartureSetup.route)
                        } else {
                            navController.navigate(LeafScreen.ArrivalSetup.route)
                        }
                    },
                )
            },
            onEditSavedPlace = { place ->
                viewModel.beginEditSavedPlace(place)
            },
            onRecentPlaceClick = { place ->
                viewModel.beginEditorForArrival(place)
                navController.navigate(LeafScreen.ArrivalSetup.route)
            },
            onAddSavedPlace = {
                viewModel.beginAddSavedPlace()
            },
        )
    }

    wakiComposable(route = LeafScreen.SearchResults.route) {
        val results by viewModel.searchResults.collectAsStateWithLifecycle()
        val query by viewModel.searchQuery.collectAsStateWithLifecycle()
        val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
        val nearUser by viewModel.searchNearUser.collectAsStateWithLifecycle()
        val mapPickerState by viewModel.mapPickerState.collectAsStateWithLifecycle()
        val savedPlaceDraft by viewModel.savedPlaceDraft.collectAsStateWithLifecycle()
        val managingSavedPlace = savedPlaceDraft !is com.whakaara.feature.alarm.SavedPlaceDraft.Idle
        val permissionState = rememberMultiplePermissionsState(locationPermissions)

        SearchResultsScreen(
            query = query.ifBlank { "Search Results" },
            results = results,
            isSearching = isSearching,
            nearUser = nearUser,
            onBack = { navController.popBackStack() },
            onClear = {
                viewModel.clearSearch()
                navController.popBackStack()
            },
            onPlaceSelected = { place ->
                when {
                    mapPickerState != null -> {
                        viewModel.moveMapPickerToPlace(place)
                        navController.popBackStack()
                    }
                    managingSavedPlace -> {
                        viewModel.completeSavedPlaceSelection(place) {
                            viewModel.clearSearch()
                            navController.popBackStack(LeafScreen.SearchLocation.route, inclusive = false)
                        }
                    }
                    else -> {
                        viewModel.beginEditorForArrival(place)
                        navController.navigate(LeafScreen.ArrivalSetup.route)
                    }
                }
            },
            onMapViewClick = {
                if (permissionState.allPermissionsGranted) {
                    viewModel.openMapPicker {
                        navController.navigate(LeafScreen.PickOnMap.route)
                    }
                } else {
                    permissionState.launchMultiplePermissionRequest()
                }
            },
        )
    }

    wakiComposable(route = LeafScreen.PickOnMap.route) {
        val pickerState by viewModel.mapPickerState.collectAsStateWithLifecycle()
        val savedPlaceDraft by viewModel.savedPlaceDraft.collectAsStateWithLifecycle()
        val managingSavedPlace = savedPlaceDraft !is com.whakaara.feature.alarm.SavedPlaceDraft.Idle
        val state = pickerState

        if (state == null) {
            LaunchedEffect(Unit) {
                navController.popBackStack()
            }
        } else {
            PickOnMapScreen(
                pickerState = state,
                onBack = {
                    viewModel.clearMapPicker()
                    navController.popBackStack()
                },
                onSearch = { query ->
                    viewModel.searchPlaces(query)
                    if (query.length >= 3) {
                        navController.navigate(LeafScreen.SearchResults.route)
                    }
                },
                onLocationPicked = { latitude, longitude ->
                    viewModel.updateMapPickerSelection(latitude, longitude)
                },
                onModeChange = { mode ->
                    viewModel.setMapPickerMode(mode)
                },
                onRangeRadiusChange = { radius ->
                    viewModel.updateMapPickerRangeRadius(radius)
                },
                onEdgeExpand = { edge ->
                    viewModel.expandMapPickerEdge(edge)
                },
                onLocateMe = { viewModel.recenterMapPicker() },
                onConfirm = {
                    viewModel.confirmMapPicker {
                        if (managingSavedPlace) {
                            navController.popBackStack(LeafScreen.SearchLocation.route, inclusive = false)
                        } else {
                            navController.navigate(LeafScreen.DepartureSetup.route) {
                                popUpTo(LeafScreen.PickOnMap.route) { inclusive = true }
                            }
                        }
                    }
                },
            )
        }
    }

    wakiComposable(route = LeafScreen.ArrivalSetup.route) {

        val editorState by viewModel.editorState.collectAsStateWithLifecycle()

        val place = editorState.place

        if (place == null) {

            LaunchedEffect(Unit) {

                navController.popBackStack()

            }

        } else {

            ArrivalAlarmSetupScreen(

                place = place,

                editorState = editorState,

                onBack = {

                    viewModel.clearEditor()

                    navController.popBackStack()

                },

                onAlarmOnArrivalChange = viewModel::updateAlarmOnArrival,

                onNotifyWhenNearChange = viewModel::updateNotifyBeforeArrival,

                onRadiusChange = viewModel::updateEditorRadius,

                onSave = {

                    viewModel.saveEditor {

                        navController.popBackStack(LeafScreen.SmartAlarm.route, inclusive = false)

                    }

                }

            )

        }

    }

    wakiComposable(route = LeafScreen.DepartureSetup.route) {

        val editorState by viewModel.editorState.collectAsStateWithLifecycle()

        val place = editorState.place

        if (place == null) {

            LaunchedEffect(Unit) {

                navController.popBackStack()

            }

        } else {

            DepartureAlarmSetupScreen(

                place = place,

                editorState = editorState,

                onBack = {

                    viewModel.clearEditor()

                    navController.popBackStack()

                },

                onNameChange = viewModel::updateEditorName,

                onDepartureDelayChange = viewModel::updateDepartureDelay,

                onRadiusChange = viewModel::updateEditorRadius,

                onLocateMe = { viewModel.relocateEditorToCurrentLocation() },

                onSave = {

                    viewModel.saveEditor {

                        navController.popBackStack(LeafScreen.SmartAlarm.route, inclusive = false)

                    }

                }

            )

        }

    }

}

