package com.whakaara.database.alarm.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.whakaara.model.location.LocationAlarm
import com.whakaara.model.location.LocationTriggerType
import java.util.UUID

@Entity(tableName = "location_alarm_table")
data class LocationAlarmEntity(
    @PrimaryKey
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

fun LocationAlarmEntity.asExternalModel() = LocationAlarm(
    id = id,
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
    triggerType = triggerType,
    radiusMeters = radiusMeters,
    enabled = enabled,
    daysOfWeek = daysOfWeek,
    alarmSound = alarmSound,
    vibrationEnabled = vibrationEnabled,
    notifyBeforeArrival = notifyBeforeArrival,
    departureDelayMinutes = departureDelayMinutes
)

fun LocationAlarm.asInternalModel() = LocationAlarmEntity(
    id = id,
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
    triggerType = triggerType,
    radiusMeters = radiusMeters,
    enabled = enabled,
    daysOfWeek = daysOfWeek,
    alarmSound = alarmSound,
    vibrationEnabled = vibrationEnabled,
    notifyBeforeArrival = notifyBeforeArrival,
    departureDelayMinutes = departureDelayMinutes
)
