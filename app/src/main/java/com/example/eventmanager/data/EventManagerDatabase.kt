package com.example.eventmanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [EventEntity::class, SessionEntity::class],
    version = 1,
    exportSchema = false
)
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
                ).build().also { instance = it }
            }
    }
}
