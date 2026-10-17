package com.example.eventmanager.model

import java.time.LocalTime
import java.time.LocalDate

object Validation {
    private val emailRegex = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)
    private val phoneRegex = Regex("^\\+?[0-9][0-9 ()-]{6,19}$")

    fun eventErrors(
        name: String,
        date: LocalDate?,
        organiser: String,
        location: String,
        email: String,
        phone: String
    ): Map<String, String> = buildMap {
        if (name.isBlank()) put("name", "Please enter the event name")
        if (date == null) put("date", "Please select the event date")
        if (organiser.isBlank()) put("organiser", "Please enter the organiser")
        if (location.isBlank()) put("location", "Please enter the location")
        if (email.isNotBlank() && !emailRegex.matches(email.trim())) {
            put("email", "Enter a valid email address")
        }
        if (phone.isNotBlank() && !phoneRegex.matches(phone.trim())) {
            put("phone", "Enter a valid phone number")
        }
    }

    fun sessionErrors(name: String, time: LocalTime?): Map<String, String> = buildMap {
        if (name.isBlank()) put("name", "Please enter the session name")
        if (time == null) put("time", "Please select the session time")
    }

    fun isValidDate(value: String): Boolean = EventDateTime.parseDisplayDate(value) != null

    fun isValidTime(value: String): Boolean = EventDateTime.parseDisplayTime(value) != null
}
