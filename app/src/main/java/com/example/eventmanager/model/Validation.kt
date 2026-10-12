package com.example.eventmanager.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

object Validation {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
        .withResolverStyle(ResolverStyle.STRICT)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        .withResolverStyle(ResolverStyle.STRICT)
    private val emailRegex = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)
    private val phoneRegex = Regex("^\\+?[0-9][0-9 ()-]{6,19}$")

    fun eventErrors(
        name: String,
        date: String,
        organiser: String,
        location: String,
        email: String,
        phone: String
    ): Map<String, String> = buildMap {
        if (name.isBlank()) put("name", "Please enter the event name")
        if (date.isBlank()) put("date", "Please enter the event date")
        else if (!isValidDate(date)) put("date", "Use a valid date in DD/MM/YYYY format")
        if (organiser.isBlank()) put("organiser", "Please enter the organiser")
        if (location.isBlank()) put("location", "Please enter the location")
        if (email.isNotBlank() && !emailRegex.matches(email.trim())) {
            put("email", "Enter a valid email address")
        }
        if (phone.isNotBlank() && !phoneRegex.matches(phone.trim())) {
            put("phone", "Enter a valid phone number")
        }
    }

    fun sessionErrors(name: String, time: String): Map<String, String> = buildMap {
        if (name.isBlank()) put("name", "Please enter the session name")
        if (time.isBlank()) put("time", "Please enter the session time")
        else if (!isValidTime(time)) put("time", "Use 24-hour HH:mm format")
    }

    fun isValidDate(value: String): Boolean = runCatching {
        LocalDate.parse(value.trim(), dateFormatter)
    }.isSuccess

    fun isValidTime(value: String): Boolean = runCatching {
        LocalTime.parse(value.trim(), timeFormatter)
    }.isSuccess
}
