package com.example.eventmanager.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("eventId")]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val name: String,
    val time: String,
    val speaker: String = "",
    val room: String = "",
    val type: String,
    val description: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
