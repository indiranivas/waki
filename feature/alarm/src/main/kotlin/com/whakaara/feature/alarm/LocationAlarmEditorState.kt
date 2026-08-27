package com.whakaara.feature.alarm

import com.whakaara.model.location.LocationTriggerType
import com.whakaara.model.location.Place

data class LocationAlarmEditorState(
    val place: Place? = null,
    val triggerType: LocationTriggerType = LocationTriggerType.ARRIVE,
    val name: String = "",
    val radiusMeters: Int = 500,
    val alarmOnArrival: Boolean = true,
    val notifyBeforeArrival: Boolean = true,
    val departureDelayMinutes: Int = 0,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)
