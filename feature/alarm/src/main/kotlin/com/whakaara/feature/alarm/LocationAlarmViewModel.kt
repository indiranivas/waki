package com.whakaara.feature.alarm

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.whakaara.core.di.IoDispatcher
import com.whakaara.core.di.MainDispatcher
import com.whakaara.core.location.GeocodingService
import com.whakaara.core.location.LocationService
import com.whakaara.data.location.GeofenceManager
import com.whakaara.data.location.LocationAlarmRepository
import com.whakaara.model.location.LocationAlarm
import com.whakaara.model.location.LocationTriggerType
import com.whakaara.model.location.Place
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

sealed class SavedPlaceDraft {
    data object Idle : SavedPlaceDraft()
    data object Adding : SavedPlaceDraft()
    data class Editing(val existingName: String) : SavedPlaceDraft()
}

@HiltViewModel
class LocationAlarmViewModel @Inject constructor(
    private val app: Application,
    private val repository: LocationAlarmRepository,
    private val geocodingService: GeocodingService,
    private val locationService: LocationService,
    private val geofenceManager: GeofenceManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val gson = Gson()
    private val prefs by lazy {
        app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _alarms = MutableStateFlow<List<LocationAlarm>>(emptyList())
    val alarms: StateFlow<List<LocationAlarm>> = _alarms.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Place>>(emptyList())
    val searchResults: StateFlow<List<Place>> = _searchResults.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _editorState = MutableStateFlow(LocationAlarmEditorState())
    val editorState: StateFlow<LocationAlarmEditorState> = _editorState.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchNearUser = MutableStateFlow(false)
    val searchNearUser: StateFlow<Boolean> = _searchNearUser.asStateFlow()

    private val _mapPickerState = MutableStateFlow<MapPickerState?>(null)
    val mapPickerState: StateFlow<MapPickerState?> = _mapPickerState.asStateFlow()

    private val _recentPlaces = MutableStateFlow<List<Place>>(emptyList())
    val recentPlaces: StateFlow<List<Place>> = _recentPlaces.asStateFlow()

    private val _ringingLocationAlarm = MutableStateFlow<LocationAlarm?>(null)
    val ringingLocationAlarm: StateFlow<LocationAlarm?> = _ringingLocationAlarm.asStateFlow()

    private val _savedPlaces = MutableStateFlow(loadSavedPlaces())
    val savedPlaces: StateFlow<List<Place>> = _savedPlaces.asStateFlow()

    private val _savedPlaceDraft = MutableStateFlow<SavedPlaceDraft>(SavedPlaceDraft.Idle)
    val savedPlaceDraft: StateFlow<SavedPlaceDraft> = _savedPlaceDraft.asStateFlow()

    val isManagingSavedPlace: Boolean
        get() = _savedPlaceDraft.value !is SavedPlaceDraft.Idle

    init {
        viewModelScope.launch(ioDispatcher) {
            repository.getAllLocationAlarmsFlow().collectLatest {
                _alarms.value = it
            }
        }
        viewModelScope.launch(ioDispatcher) {
            repository.triggerFlow.collect {
                recreateEnabledGeofences()
            }
        }
        viewModelScope.launch(ioDispatcher) {
            recreateEnabledGeofences()
        }
    }

    fun beginAddSavedPlace() {
        _savedPlaceDraft.value = SavedPlaceDraft.Adding
    }

    fun beginEditSavedPlace(place: Place) {
        _savedPlaceDraft.value = SavedPlaceDraft.Editing(existingName = place.name)
    }

    fun clearSavedPlaceDraft() {
        _savedPlaceDraft.value = SavedPlaceDraft.Idle
    }

    fun completeSavedPlaceSelection(place: Place, onReady: () -> Unit) {
        val draft = _savedPlaceDraft.value
        if (draft is SavedPlaceDraft.Idle) return

        val saved = when (draft) {
            is SavedPlaceDraft.Editing -> place.copy(name = draft.existingName)
            SavedPlaceDraft.Adding -> place
            SavedPlaceDraft.Idle -> place
        }
        upsertSavedPlace(saved)
        clearSavedPlaceDraft()
        onReady()
    }

    fun upsertSavedPlace(place: Place) {
        _savedPlaces.update { current ->
            val next = current.filterNot { it.name.equals(place.name, ignoreCase = true) } + place
            persistSavedPlaces(next)
            next
        }
    }

    fun deleteSavedPlace(place: Place) {
        _savedPlaces.update { current ->
            val next = current.filterNot { it.name.equals(place.name, ignoreCase = true) }
            persistSavedPlaces(next)
            next
        }
    }

    fun beginEditor(place: Place) {
        beginEditorForArrival(place)
    }

    fun beginEditorForArrival(place: Place) {
        addToRecentPlaces(place)
        _editorState.value = LocationAlarmEditorState(
            place = place,
            name = place.name,
            triggerType = LocationTriggerType.ARRIVE,
            radiusMeters = 500,
            alarmOnArrival = true,
            notifyBeforeArrival = true,
        )
    }

    fun beginEditorForDeparture(place: Place, radiusMeters: Int = 200) {
        addToRecentPlaces(place)
        _editorState.value = LocationAlarmEditorState(
            place = place,
            name = place.name,
            triggerType = LocationTriggerType.LEAVE,
            radiusMeters = radiusMeters.coerceIn(100, 5000),
            departureDelayMinutes = 0,
        )
    }

    private fun addToRecentPlaces(place: Place) {
        _recentPlaces.update { current ->
            (
                listOf(place) + current.filter {
                    it.latitude != place.latitude || it.longitude != place.longitude
                }
                ).take(5)
        }
    }

    fun updateAlarmOnArrival(enabled: Boolean) {
        _editorState.update { it.copy(alarmOnArrival = enabled) }
    }

    fun clearEditor() {
        _editorState.value = LocationAlarmEditorState()
    }

    fun setTriggerType(triggerType: LocationTriggerType) {
        _editorState.update { it.copy(triggerType = triggerType) }
    }

    fun updateEditorName(name: String) {
        _editorState.update { it.copy(name = name) }
    }

    fun updateEditorRadius(radiusMeters: Int) {
        _editorState.update { it.copy(radiusMeters = radiusMeters) }
    }

    fun updateNotifyBeforeArrival(enabled: Boolean) {
        _editorState.update { it.copy(notifyBeforeArrival = enabled) }
    }

    fun updateDepartureDelay(minutes: Int) {
        _editorState.update { it.copy(departureDelayMinutes = minutes) }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _searchNearUser.value = false
    }

    fun relocateEditorToCurrentLocation() {
        viewModelScope.launch(ioDispatcher) {
            val location = locationService.getCurrentLocation() ?: return@launch
            val place = geocodingService.reverseGeocode(location.latitude, location.longitude)
                ?: Place(
                    name = "Current Location",
                    address = "${location.latitude}, ${location.longitude}",
                    latitude = location.latitude,
                    longitude = location.longitude,
                )
            _editorState.update { state ->
                state.copy(
                    place = place,
                    name = state.name.ifBlank { place.name },
                )
            }
        }
    }

    fun searchPlaces(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _searchNearUser.value = false
            return
        }
        viewModelScope.launch(ioDispatcher) {
            _isSearching.value = true
            try {
                val location = locationService.getCurrentLocation()
                val results = geocodingService.search(
                    query = query,
                    userLatitude = location?.latitude,
                    userLongitude = location?.longitude,
                )
                _searchResults.value = results
                _searchNearUser.value = location != null
            } catch (_: Exception) {
                _searchResults.value = emptyList()
                _searchNearUser.value = false
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun openMapPicker(onReady: () -> Unit) {
        viewModelScope.launch(ioDispatcher) {
            val location = locationService.getCurrentLocation()
            val latitude = location?.latitude ?: 20.5937
            val longitude = location?.longitude ?: 78.9629
            _mapPickerState.value = MapPickerState(
                centerLatitude = latitude,
                centerLongitude = longitude,
                selectedLatitude = latitude,
                selectedLongitude = longitude,
            )
            withContext(mainDispatcher) {
                onReady()
            }
        }
    }

    fun recenterMapPicker() {
        viewModelScope.launch(ioDispatcher) {
            val location = locationService.getCurrentLocation()
            if (location == null) return@launch
            _mapPickerState.update {
                it?.copy(
                    centerLatitude = location.latitude,
                    centerLongitude = location.longitude,
                    selectedLatitude = location.latitude,
                    selectedLongitude = location.longitude,
                )
            }
        }
    }

    fun updateMapPickerSelection(latitude: Double, longitude: Double) {
        _mapPickerState.update { state ->
            state?.copy(
                selectedLatitude = latitude,
                selectedLongitude = longitude,
            )
        }
    }

    fun setMapPickerMode(mode: MapPickerMode) {
        _mapPickerState.update { state ->
            state?.copy(mode = mode)
        }
    }

    fun updateMapPickerRangeRadius(radiusMeters: Int) {
        _mapPickerState.update { state ->
            state?.copy(
                rangeRadiusMeters = radiusMeters.coerceIn(
                    MapPickerState.MIN_SIDE_METERS,
                    1000,
                ),
            )
        }
    }

    fun expandMapPickerEdge(edge: SelectionEdge) {
        _mapPickerState.update { state ->
            state?.expanded(edge)
        }
    }

    fun moveMapPickerToPlace(place: Place) {
        _mapPickerState.update { state ->
            state?.copy(
                centerLatitude = place.latitude,
                centerLongitude = place.longitude,
                selectedLatitude = place.latitude,
                selectedLongitude = place.longitude,
            )
        }
    }

    fun confirmMapPicker(onReady: () -> Unit) {
        val pickerState = _mapPickerState.value
        val latitude = pickerState?.selectedLatitude
        val longitude = pickerState?.selectedLongitude
        if (latitude == null || longitude == null || pickerState.isConfirming) return

        val radiusMeters = pickerState.effectiveRadiusMeters
        _mapPickerState.update { it?.copy(isConfirming = true) }

        viewModelScope.launch(ioDispatcher) {
            try {
                val place = geocodingService.reverseGeocode(latitude, longitude)
                    ?: Place(
                        name = "Selected location",
                        address = "$latitude, $longitude",
                        latitude = latitude,
                        longitude = longitude,
                    )
                if (isManagingSavedPlace) {
                    completeSavedPlaceSelection(place) {}
                    _mapPickerState.value = null
                    withContext(mainDispatcher) { onReady() }
                } else {
                    beginEditorForDeparture(place, radiusMeters = radiusMeters)
                    _mapPickerState.value = null
                    withContext(mainDispatcher) { onReady() }
                }
            } catch (_: Exception) {
                _mapPickerState.update { it?.copy(isConfirming = false) }
            }
        }
    }

    fun clearMapPicker() {
        _mapPickerState.value = null
    }

    fun resolveSavedPlace(
        place: Place,
        forDeparture: Boolean,
        onReady: () -> Unit,
    ) {
        viewModelScope.launch(ioDispatcher) {
            val resolved = if (place.latitude != 0.0 || place.longitude != 0.0) {
                place
            } else {
                val location = locationService.getCurrentLocation()
                val results = geocodingService.search(
                    query = "${place.name}, ${place.address}",
                    userLatitude = location?.latitude,
                    userLongitude = location?.longitude,
                )
                results.firstOrNull()
                    ?: Place(
                        name = place.name,
                        address = place.address,
                        latitude = location?.latitude ?: 0.0,
                        longitude = location?.longitude ?: 0.0,
                    )
            }
            if (resolved.latitude != 0.0 || resolved.longitude != 0.0) {
                upsertSavedPlace(resolved.copy(name = place.name))
            }
            if (forDeparture) beginEditorForDeparture(resolved) else beginEditorForArrival(resolved)
            withContext(mainDispatcher) { onReady() }
        }
    }

    fun useCurrentLocation(forDeparture: Boolean, onReady: () -> Unit) {
        viewModelScope.launch(ioDispatcher) {
            val location = locationService.getCurrentLocation() ?: return@launch
            val place = geocodingService.reverseGeocode(location.latitude, location.longitude)
                ?: Place(
                    name = "Current Location",
                    address = "${location.latitude}, ${location.longitude}",
                    latitude = location.latitude,
                    longitude = location.longitude,
                )
            if (isManagingSavedPlace) {
                withContext(mainDispatcher) {
                    completeSavedPlaceSelection(place, onReady)
                }
            } else if (forDeparture) {
                beginEditorForDeparture(place)
                withContext(mainDispatcher) { onReady() }
            } else {
                beginEditorForArrival(place)
                withContext(mainDispatcher) { onReady() }
            }
        }
    }

    fun saveEditor(onSuccess: () -> Unit) {
        val editor = _editorState.value
        val place = editor.place
        if (place == null || editor.isSaving) return

        _editorState.update { it.copy(isSaving = true, errorMessage = null) }

        val locationAlarm = LocationAlarm(
            name = editor.name.ifBlank { place.name },
            address = place.address,
            latitude = place.latitude,
            longitude = place.longitude,
            triggerType = editor.triggerType,
            radiusMeters = editor.radiusMeters.coerceIn(100, 5000),
            alarmSound = "",
            notifyBeforeArrival = editor.notifyBeforeArrival,
            departureDelayMinutes = editor.departureDelayMinutes,
        )

        viewModelScope.launch(ioDispatcher) {
            try {
                repository.insert(locationAlarm)
                if (locationAlarm.enabled) {
                    geofenceManager.addGeofence(locationAlarm)
                }
                clearEditor()
                withContext(mainDispatcher) {
                    onSuccess()
                }
            } catch (_: Exception) {
                _editorState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Could not save alarm. Try again.",
                    )
                }
            }
        }
    }

    fun toggleLocationAlarm(locationAlarm: LocationAlarm) {
        val updated = locationAlarm.copy(enabled = !locationAlarm.enabled)
        viewModelScope.launch(ioDispatcher) {
            repository.update(updated)
            if (updated.enabled) {
                geofenceManager.addGeofence(updated)
            } else {
                geofenceManager.removeGeofence(updated.id.toString())
            }
        }
    }

    fun deleteLocationAlarm(locationAlarm: LocationAlarm) {
        viewModelScope.launch(ioDispatcher) {
            repository.delete(locationAlarm)
            geofenceManager.removeGeofence(locationAlarm.id.toString())
        }
    }

    fun loadRingingLocationAlarm(alarmId: UUID) {
        viewModelScope.launch(ioDispatcher) {
            _ringingLocationAlarm.value = repository.getLocationAlarmById(alarmId)
        }
    }

    fun dismissLocationAlarm() {
        _ringingLocationAlarm.value = null
        clearEditor()
    }

    private suspend fun recreateEnabledGeofences() {
        geofenceManager.recreateGeofences(repository.getEnabledLocationAlarms())
    }

    private fun defaultSavedPlaces(): List<Place> = listOf(
        Place(
            name = "Home",
            address = "Tap Edit to set your home",
            latitude = 0.0,
            longitude = 0.0,
        ),
        Place(
            name = "Work",
            address = "Tap Edit to set your work",
            latitude = 0.0,
            longitude = 0.0,
        ),
    )

    private fun loadSavedPlaces(): List<Place> {
        val json = prefs.getString(KEY_PLACES, null) ?: return defaultSavedPlaces()
        return try {
            gson.fromJson(json, Array<Place>::class.java)?.toList()?.ifEmpty { defaultSavedPlaces() }
                ?: defaultSavedPlaces()
        } catch (_: Exception) {
            defaultSavedPlaces()
        }
    }

    private fun persistSavedPlaces(places: List<Place>) {
        prefs.edit().putString(KEY_PLACES, gson.toJson(places)).apply()
    }

    companion object {
        private const val PREFS_NAME = "waki_saved_places"
        private const val KEY_PLACES = "places"
    }
}
