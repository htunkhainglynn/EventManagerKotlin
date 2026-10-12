package com.example.eventmanager

import android.app.Application
import com.example.eventmanager.data.EventManagerDatabase
import com.example.eventmanager.data.EventRepository

class EventManagerApplication : Application() {
    val database by lazy { EventManagerDatabase.getInstance(this) }
    val repository by lazy { EventRepository(database) }
}
