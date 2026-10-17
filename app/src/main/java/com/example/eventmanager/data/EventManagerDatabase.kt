package com.example.eventmanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [EventEntity::class, SessionEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class EventManagerDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile private var instance: EventManagerDatabase? = null

        fun getInstance(context: Context): EventManagerDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    EventManagerDatabase::class.java,
                    "event_manager.db"
                ).addMigrations(MIGRATION_1_2)
                    .build().also { instance = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS events_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        organiser TEXT NOT NULL,
                        location TEXT NOT NULL,
                        eventType TEXT NOT NULL,
                        status TEXT NOT NULL,
                        description TEXT NOT NULL,
                        contactEmail TEXT NOT NULL,
                        contactPhone TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        syncedAt INTEGER
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO events_new (
                        id, name, date, organiser, location, eventType, status,
                        description, contactEmail, contactPhone, updatedAt, syncedAt
                    )
                    SELECT id, name,
                        CAST(julianday(substr(date, 7, 4) || '-' || substr(date, 4, 2) || '-' || substr(date, 1, 2)) - julianday('1970-01-01') AS INTEGER),
                        organiser, location, 'Conference', 'Upcoming', description,
                        contactEmail, contactPhone, updatedAt, syncedAt
                    FROM events
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sessions_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        eventId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        time INTEGER NOT NULL,
                        speaker TEXT NOT NULL,
                        room TEXT NOT NULL,
                        type TEXT NOT NULL,
                        description TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY(eventId) REFERENCES events_new(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO sessions_new (id, eventId, name, time, speaker, room, type, description, updatedAt)
                    SELECT id, eventId, name,
                        CAST(substr(time, 1, 2) AS INTEGER) * 60 + CAST(substr(time, 4, 2) AS INTEGER),
                        speaker, room,
                        CASE type
                            WHEN 'Panel Discussion' THEN 'PanelDiscussion'
                            ELSE type
                        END,
                        description, updatedAt
                    FROM sessions
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE sessions")
                database.execSQL("DROP TABLE events")
                database.execSQL("ALTER TABLE events_new RENAME TO events")
                database.execSQL("ALTER TABLE sessions_new RENAME TO sessions")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_events_name_date ON events(name, date)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_eventId ON sessions(eventId)")
            }
        }
    }
}
