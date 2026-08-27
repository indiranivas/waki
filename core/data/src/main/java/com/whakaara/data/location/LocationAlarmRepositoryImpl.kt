package com.whakaara.data.location

import com.whakaara.database.alarm.LocationAlarmDao
import com.whakaara.database.alarm.entity.asExternalModel
import com.whakaara.database.alarm.entity.asInternalModel
import com.whakaara.model.location.LocationAlarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class LocationAlarmRepositoryImpl @Inject constructor(
    private val locationAlarmDao: LocationAlarmDao
) : LocationAlarmRepository {

    private val _triggerFlow = MutableSharedFlow<Unit>(replay = 0)
    override val triggerFlow: SharedFlow<Unit> = _triggerFlow.asSharedFlow()

    override fun triggerAlarmsRecreation() {
        _triggerFlow.tryEmit(Unit)
    }
    override fun getAllLocationAlarmsFlow(): Flow<List<LocationAlarm>> {
        return locationAlarmDao.getAllLocationAlarmsFlow().map { list ->
            list.map { it.asExternalModel() }
        }
    }

    override suspend fun getAllLocationAlarms(): List<LocationAlarm> {
        return locationAlarmDao.getAllLocationAlarms().map { it.asExternalModel() }
    }

    override suspend fun getEnabledLocationAlarms(): List<LocationAlarm> {
        return locationAlarmDao.getEnabledLocationAlarms().map { it.asExternalModel() }
    }

    override suspend fun insert(locationAlarm: LocationAlarm) {
        locationAlarmDao.insert(locationAlarm.asInternalModel())
    }

    override suspend fun delete(locationAlarm: LocationAlarm) {
        locationAlarmDao.deleteLocationAlarm(locationAlarm.asInternalModel())
    }

    override suspend fun deleteLocationAlarmById(id: UUID) {
        locationAlarmDao.deleteLocationAlarmById(id)
    }

    override suspend fun update(locationAlarm: LocationAlarm) {
        locationAlarmDao.updateLocationAlarm(locationAlarm.asInternalModel())
    }

    override suspend fun getLocationAlarmById(id: UUID): LocationAlarm? {
        return locationAlarmDao.getLocationAlarmById(id)?.asExternalModel()
    }
}
