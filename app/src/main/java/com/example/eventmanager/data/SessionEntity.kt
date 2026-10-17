package com.example.eventmanager.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.eventmanager.model.SessionType
import java.time.LocalTime

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
    val time: LocalTime,
    val speaker: String = "",
    val room: String = "",
    val type: SessionType,
    val description: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
