package com.whakaara.database.alarm.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.whakaara.model.location.LocationTriggerType
import java.util.Calendar

class AlarmConverter {
    @TypeConverter
    fun fromLocationTriggerType(value: LocationTriggerType): String {
        return value.name
    }

    @TypeConverter
    fun toLocationTriggerType(value: String): LocationTriggerType {
        return LocationTriggerType.valueOf(value)
    }

    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        return Gson().fromJson(value, object : TypeToken<List<Int>>() {}.type)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Calendar? {
        return value?.let { Calendar.getInstance().apply { timeInMillis = it } }
    }

    @TypeConverter
    fun dateToTimestamp(calendar: Calendar?): Long? {
        return calendar?.timeInMillis
    }

    @TypeConverter
    fun fromString(value: String): MutableList<Int> {
        return Gson().fromJson(value, object : TypeToken<ArrayList<Int>>() {}.type)
    }

    @TypeConverter
    fun listToString(value: MutableList<Int>): String {
        return Gson().toJson(value)
    }
}
