package com.example.eventmanager.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eventmanager.data.EventEntity
import com.example.eventmanager.data.SessionEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    viewModel: EventManagerViewModel,
    eventId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
    onAddSession: () -> Unit,
    onEditSession: (Long) -> Unit
) {
    LaunchedEffect(eventId) { viewModel.selectEvent(eventId) }
    val event by viewModel.selectedEvent.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val query by viewModel.sessionSearchQuery.collectAsStateWithLifecycle()
    var showDeleteEvent by remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<SessionEntity?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.name ?: "Event details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val selected = event ?: return@IconButton
                            val schedule = buildSchedule(selected, sessions)
                            context.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "${selected.name} schedule")
                                        putExtra(Intent.EXTRA_TEXT, schedule)
                                    },
                                    "Share schedule"
                                )
                            )
                        }
                    ) { Icon(Icons.Default.Share, contentDescription = "Share schedule") }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit event")
                    }
                    IconButton(onClick = { showDeleteEvent = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete event")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddSession,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add session") }
            )
        }
    ) { padding ->
        val selected = event
        if (selected == null) {
            Column(
                Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { EventSummary(selected) }
                item {
                    Spacer(Modifier.height(4.dp))
                    Text("Sessions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "${sessions.size} shown • sessions are deleted automatically with this event",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.sessionSearchQuery.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search sessions") },
                        placeholder = { Text("Name or description") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )
                }
                if (sessions.isEmpty()) {
                    item {
                        Text(
                            if (query.isBlank()) "No sessions yet. Add the first session." else "No matching sessions.",
                            modifier = Modifier.padding(vertical = 24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(sessions, key = { it.id }) { session ->
                        SessionCard(
                            session = session,
                            onEdit = { onEditSession(session.id) },
                            onDelete = { sessionToDelete = session }
                        )
                    }
                }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }
    }

    if (showDeleteEvent) {
        AlertDialog(
            onDismissRequest = { showDeleteEvent = false },
            title = { Text("Delete this event?") },
            text = { Text("The event and all of its sessions will be permanently deleted.") },
            confirmButton = {
                Button(onClick = {
                    showDeleteEvent = false
                    event?.let { viewModel.deleteEvent(it, onDeleted) }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteEvent = false }) { Text("Cancel") }
            }
        )
    }

    sessionToDelete?.let { selectedSession ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Delete session?") },
            text = { Text("${selectedSession.name} will be removed from this event.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteSession(selectedSession)
                    sessionToDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun EventSummary(event: EventEntity) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(event.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${event.date} • ${event.location}", style = MaterialTheme.typography.titleMedium)
            Text("Organised by ${event.organiser}")
            if (event.description.isNotBlank()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .18f))
                Text(event.description)
            }
            if (event.contactEmail.isNotBlank()) DetailLine(Icons.Default.Email, event.contactEmail)
            if (event.contactPhone.isNotBlank()) DetailLine(Icons.Default.Phone, event.contactPhone)
            Text(
                if (event.syncedAt == null) "Changes waiting to upload" else "Uploaded to cloud",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DetailLine(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null)
        Text(value)
    }
}

@Composable
private fun SessionCard(session: SessionEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(session.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(session.type, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete ${session.name}")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                DetailLine(Icons.Default.Schedule, session.time)
                if (session.room.isNotBlank()) Text("Room ${session.room}")
            }
            if (session.speaker.isNotBlank()) Text("Speaker: ${session.speaker}")
            if (session.description.isNotBlank()) Text(
                session.description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun buildSchedule(event: EventEntity, sessions: List<SessionEntity>): String = buildString {
    appendLine(event.name)
    appendLine("${event.date} • ${event.location}")
    appendLine("Organiser: ${event.organiser}")
    appendLine()
    appendLine("Schedule")
    if (sessions.isEmpty()) appendLine("No sessions added.")
    sessions.forEach { session ->
        append("${session.time} — ${session.name} (${session.type})")
        if (session.room.isNotBlank()) append(" • Room ${session.room}")
        if (session.speaker.isNotBlank()) append(" • ${session.speaker}")
        appendLine()
    }
}
