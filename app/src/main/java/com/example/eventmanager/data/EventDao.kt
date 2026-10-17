package com.example.eventmanager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query(
        """
        SELECT events.*, COUNT(sessions.id) AS sessionCount
        FROM events
        LEFT JOIN sessions ON events.id = sessions.eventId
        WHERE (:query = '' OR events.name LIKE '%' || :query || '%' OR events.description LIKE '%' || :query || '%')
          AND (:date IS NULL OR events.date = :date)
          AND (:organiser = '' OR events.organiser LIKE '%' || :organiser || '%')
        GROUP BY events.id
        ORDER BY events.updatedAt DESC
        """
    )
    fun observeEvents(query: String, date: LocalDate?, organiser: String): Flow<List<EventWithSessionCount>>

    @Query("SELECT * FROM events WHERE id = :id")
    fun observeEvent(id: Long): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEvent(id: Long): EventEntity?

    @Query("SELECT * FROM events ORDER BY date, name")
    suspend fun getAllEvents(): List<EventEntity>

    @Query("SELECT COUNT(*) FROM events WHERE LOWER(name) = LOWER(:name) AND date = :date AND id != :excludeId")
    suspend fun duplicateCount(name: String, date: LocalDate, excludeId: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(event: EventEntity): Long

    @Update
    suspend fun update(event: EventEntity)

    @Delete
    suspend fun delete(event: EventEntity)

    @Query("UPDATE events SET syncedAt = :syncedAt WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>, syncedAt: Long)
}
