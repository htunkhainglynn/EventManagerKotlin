package com.example.eventmanager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query(
        """
        SELECT * FROM sessions
        WHERE eventId = :eventId
          AND (:query = '' OR name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        ORDER BY time, name
        """
    )
    fun observeSessions(eventId: Long, query: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSession(id: Long): SessionEntity?

    @Query("SELECT * FROM sessions ORDER BY eventId, time, name")
    suspend fun getAllSessions(): List<SessionEntity>

    @Insert
    suspend fun insert(session: SessionEntity): Long

    @Update
    suspend fun update(session: SessionEntity)

    @Delete
    suspend fun delete(session: SessionEntity)
}
