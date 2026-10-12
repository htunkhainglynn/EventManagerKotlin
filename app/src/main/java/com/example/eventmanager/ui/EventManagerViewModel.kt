package com.example.eventmanager.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.eventmanager.data.EventEntity
import com.example.eventmanager.data.EventRepository
import com.example.eventmanager.data.EventWithSessionCount
import com.example.eventmanager.data.SessionEntity
import com.example.eventmanager.sync.CloudUploadWorker
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EventDraft(
    val id: Long = 0,
    val name: String = "",
    val date: String = "",
    val organiser: String = "",
    val location: String = "",
    val description: String = "",
    val contactEmail: String = "",
    val contactPhone: String = ""
) {
    fun toEntity() = EventEntity(
        id = id,
        name = name,
        date = date,
        organiser = organiser,
        location = location,
        description = description,
        contactEmail = contactEmail,
        contactPhone = contactPhone
    )
}

data class SessionDraft(
    val id: Long = 0,
    val eventId: Long,
    val name: String = "",
    val time: String = "",
    val speaker: String = "",
    val room: String = "",
    val type: String = "Presentation",
    val description: String = ""
) {
    fun toEntity() = SessionEntity(
        id = id,
        eventId = eventId,
        name = name,
        time = time,
        speaker = speaker,
        room = room,
        type = type,
        description = description
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
class EventManagerViewModel(
    application: Application,
    private val repository: EventRepository
) : AndroidViewModel(application) {
    val searchQuery = MutableStateFlow("")
    val dateFilter = MutableStateFlow("")
    val organiserFilter = MutableStateFlow("")
    val sessionSearchQuery = MutableStateFlow("")
    private val selectedEventId = MutableStateFlow(0L)

    val events: StateFlow<List<EventWithSessionCount>> =
        combine(searchQuery, dateFilter, organiserFilter) { query, date, organiser ->
            Triple(query, date, organiser)
        }.flatMapLatest { (query, date, organiser) ->
            repository.observeEvents(query, date, organiser)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedEvent: StateFlow<EventEntity?> = selectedEventId
        .flatMapLatest { id -> if (id == 0L) flowOf(null) else repository.observeEvent(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val sessions: StateFlow<List<SessionEntity>> =
        combine(selectedEventId, sessionSearchQuery) { id, query -> id to query }
            .flatMapLatest { (id, query) ->
                if (id == 0L) flowOf(emptyList()) else repository.observeSessions(id, query)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uploadInfo = MutableStateFlow<WorkInfo?>(null)
    val uploadInfo: StateFlow<WorkInfo?> = _uploadInfo

    fun selectEvent(id: Long) {
        selectedEventId.value = id
        sessionSearchQuery.value = ""
    }

    suspend fun event(id: Long): EventEntity? = repository.getEvent(id)

    suspend fun session(id: Long): SessionEntity? = repository.getSession(id)

    fun checkDuplicate(draft: EventDraft, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(repository.hasDuplicate(draft.name, draft.date, draft.id))
        }
    }

    fun saveEvent(draft: EventDraft, onSaved: (Long) -> Unit) {
        viewModelScope.launch { onSaved(repository.saveEvent(draft.toEntity())) }
    }

    fun deleteEvent(event: EventEntity, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            onDeleted()
        }
    }

    fun saveSession(draft: SessionDraft, onSaved: (Long) -> Unit) {
        viewModelScope.launch { onSaved(repository.saveSession(draft.toEntity())) }
    }

    fun deleteSession(session: SessionEntity) {
        viewModelScope.launch { repository.deleteSession(session) }
    }

    fun uploadAll() {
        val request = OneTimeWorkRequestBuilder<CloudUploadWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        val manager = WorkManager.getInstance(getApplication())
        manager.enqueueUniqueWork("upload-all-events", ExistingWorkPolicy.REPLACE, request)
        observeWork(manager, request.id)
    }

    private fun observeWork(manager: WorkManager, id: UUID) {
        viewModelScope.launch {
            manager.getWorkInfoByIdFlow(id).collect { _uploadInfo.value = it }
        }
    }
}

class EventManagerViewModelFactory(
    private val application: Application,
    private val repository: EventRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        EventManagerViewModel(application, repository) as T
}
