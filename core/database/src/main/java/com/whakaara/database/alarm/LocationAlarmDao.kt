package com.whakaara.database.alarm

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.whakaara.database.alarm.entity.LocationAlarmEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface LocationAlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(locationAlarmEntity: LocationAlarmEntity)

    @Delete
    suspend fun deleteLocationAlarm(locationAlarmEntity: LocationAlarmEntity)

    @Query("DELETE FROM location_alarm_table WHERE id = :id")
    suspend fun deleteLocationAlarmById(id: UUID)

    @Update
    suspend fun updateLocationAlarm(locationAlarmEntity: LocationAlarmEntity)

    @Query("SELECT * FROM location_alarm_table")
    fun getAllLocationAlarmsFlow(): Flow<List<LocationAlarmEntity>>

    @Query("SELECT * FROM location_alarm_table")
    suspend fun getAllLocationAlarms(): List<LocationAlarmEntity>

    @Query("SELECT * FROM location_alarm_table WHERE enabled = 1")
    suspend fun getEnabledLocationAlarms(): List<LocationAlarmEntity>

    @Query("SELECT * FROM location_alarm_table WHERE id = :id")
    suspend fun getLocationAlarmById(id: UUID): LocationAlarmEntity?
}
