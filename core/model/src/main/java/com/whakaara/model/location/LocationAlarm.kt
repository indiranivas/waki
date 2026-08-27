package com.whakaara.model.location

import java.util.UUID

data class LocationAlarm(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val triggerType: LocationTriggerType,
    val radiusMeters: Int,
    val enabled: Boolean = true,
    val daysOfWeek: List<Int> = emptyList(),
    val alarmSound: String = "",
    val vibrationEnabled: Boolean = true,
    val notifyBeforeArrival: Boolean = false,
    val departureDelayMinutes: Int = 0
)

enum class LocationTriggerType {
    ARRIVE, LEAVE
}
