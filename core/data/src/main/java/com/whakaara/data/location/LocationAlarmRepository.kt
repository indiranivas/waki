package com.whakaara.data.location

import com.whakaara.model.location.LocationAlarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import java.util.UUID

interface LocationAlarmRepository {
    fun getAllLocationAlarmsFlow(): Flow<List<LocationAlarm>>
    suspend fun getAllLocationAlarms(): List<LocationAlarm>
    suspend fun getEnabledLocationAlarms(): List<LocationAlarm>
    suspend fun insert(locationAlarm: LocationAlarm)
    suspend fun delete(locationAlarm: LocationAlarm)
    suspend fun deleteLocationAlarmById(id: UUID)
    suspend fun update(locationAlarm: LocationAlarm)
    suspend fun getLocationAlarmById(id: UUID): LocationAlarm?

    val triggerFlow: SharedFlow<Unit>

    fun triggerAlarmsRecreation()
}
