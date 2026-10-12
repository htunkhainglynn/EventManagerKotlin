package com.example.eventmanager.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    indices = [Index(value = ["name", "date"], unique = true)]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val date: String,
    val organiser: String,
    val location: String,
    val description: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)
