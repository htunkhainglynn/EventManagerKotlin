package com.example.eventmanager.data

import androidx.room.Embedded

data class EventWithSessionCount(
    @Embedded val event: EventEntity,
    val sessionCount: Int
)
