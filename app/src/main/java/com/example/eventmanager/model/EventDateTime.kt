package com.example.eventmanager.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

object EventDateTime {
    private val displayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
        .withResolverStyle(ResolverStyle.STRICT)
    private val displayTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        .withResolverStyle(ResolverStyle.STRICT)

    fun displayDate(value: LocalDate): String = value.format(displayDateFormatter)

    fun displayTime(value: LocalTime): String = value.format(displayTimeFormatter)

    fun parseDisplayDate(value: String): LocalDate? = runCatching {
        LocalDate.parse(value.trim(), displayDateFormatter)
    }.getOrNull()

    fun parseDisplayTime(value: String): LocalTime? = runCatching {
        LocalTime.parse(value.trim(), displayTimeFormatter)
    }.getOrNull()
}
