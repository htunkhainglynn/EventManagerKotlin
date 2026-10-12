package com.example.eventmanager.data

import kotlinx.coroutines.flow.Flow

class EventRepository(private val database: EventManagerDatabase) {
    fun observeEvents(query: String, date: String, organiser: String): Flow<List<EventWithSessionCount>> =
        database.eventDao().observeEvents(query.trim(), date.trim(), organiser.trim())

    fun observeEvent(id: Long): Flow<EventEntity?> = database.eventDao().observeEvent(id)

    fun observeSessions(eventId: Long, query: String): Flow<List<SessionEntity>> =
        database.sessionDao().observeSessions(eventId, query.trim())

    suspend fun getEvent(id: Long): EventEntity? = database.eventDao().getEvent(id)

    suspend fun getSession(id: Long): SessionEntity? = database.sessionDao().getSession(id)

    suspend fun hasDuplicate(name: String, date: String, excludeId: Long = 0): Boolean =
        database.eventDao().duplicateCount(name.trim(), date.trim(), excludeId) > 0

    suspend fun saveEvent(event: EventEntity): Long {
        val fresh = event.copy(
            name = event.name.trim(),
            date = event.date.trim(),
            organiser = event.organiser.trim(),
            location = event.location.trim(),
            description = event.description.trim(),
            contactEmail = event.contactEmail.trim(),
            contactPhone = event.contactPhone.trim(),
            updatedAt = System.currentTimeMillis(),
            syncedAt = null
        )
        return if (fresh.id == 0L) database.eventDao().insert(fresh)
        else {
            database.eventDao().update(fresh)
            fresh.id
        }
    }

    suspend fun deleteEvent(event: EventEntity) = database.eventDao().delete(event)

    suspend fun saveSession(session: SessionEntity): Long {
        val fresh = session.copy(
            name = session.name.trim(),
            time = session.time.trim(),
            speaker = session.speaker.trim(),
            room = session.room.trim(),
            description = session.description.trim(),
            updatedAt = System.currentTimeMillis()
        )
        val id = if (fresh.id == 0L) database.sessionDao().insert(fresh)
        else {
            database.sessionDao().update(fresh)
            fresh.id
        }
        database.eventDao().getEvent(fresh.eventId)?.let {
            database.eventDao().update(it.copy(updatedAt = System.currentTimeMillis(), syncedAt = null))
        }
        return id
    }

    suspend fun deleteSession(session: SessionEntity) {
        database.sessionDao().delete(session)
        database.eventDao().getEvent(session.eventId)?.let {
            database.eventDao().update(it.copy(updatedAt = System.currentTimeMillis(), syncedAt = null))
        }
    }
}
