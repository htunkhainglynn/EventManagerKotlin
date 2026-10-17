package com.example.eventmanager.data

import androidx.room.TypeConverter
import com.example.eventmanager.model.EventStatus
import com.example.eventmanager.model.EventType
import com.example.eventmanager.model.SessionType
import java.time.LocalDate
import java.time.LocalTime

class DatabaseConverters {
    @TypeConverter
    fun localDateToEpochDay(value: LocalDate): Long = value.toEpochDay()

    @TypeConverter
    fun epochDayToLocalDate(value: Long): LocalDate = LocalDate.ofEpochDay(value)

    @TypeConverter
    fun localTimeToMinutes(value: LocalTime): Int = value.hour * 60 + value.minute

    @TypeConverter
    fun minutesToLocalTime(value: Int): LocalTime = LocalTime.of(value / 60, value % 60)

    @TypeConverter
    fun eventTypeToString(value: EventType): String = value.name

    @TypeConverter
    fun stringToEventType(value: String): EventType = EventType.valueOf(value)

    @TypeConverter
    fun eventStatusToString(value: EventStatus): String = value.name

    @TypeConverter
    fun stringToEventStatus(value: String): EventStatus = EventStatus.valueOf(value)

    @TypeConverter
    fun sessionTypeToString(value: SessionType): String = value.name

    @TypeConverter
    fun stringToSessionType(value: String): SessionType = SessionType.valueOf(value)
}
