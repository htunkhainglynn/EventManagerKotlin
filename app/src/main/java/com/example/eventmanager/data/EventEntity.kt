package com.example.eventmanager.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.eventmanager.model.EventStatus
import com.example.eventmanager.model.EventType
import java.time.LocalDate

@Entity(
    tableName = "events",
    indices = [Index(value = ["name", "date"], unique = true)]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val date: LocalDate,
    val organiser: String,
    val location: String,
    val eventType: EventType = EventType.Conference,
    val status: EventStatus = EventStatus.Upcoming,
    val description: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)
