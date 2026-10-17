package com.example.eventmanager

import com.example.eventmanager.data.DatabaseConverters
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class DatabaseConvertersTest {
    private val converters = DatabaseConverters()

    @Test
    fun dateRoundTripsAsEpochDay() {
        val date = LocalDate.of(2027, 2, 28)
        assertEquals(date, converters.epochDayToLocalDate(converters.localDateToEpochDay(date)))
    }

    @Test
    fun timeRoundTripsAsMinutesAfterMidnight() {
        val time = LocalTime.of(9, 30)
        assertEquals(time, converters.minutesToLocalTime(converters.localTimeToMinutes(time)))
    }
}
