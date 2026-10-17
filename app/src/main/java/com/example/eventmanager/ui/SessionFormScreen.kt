package com.example.eventmanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.eventmanager.model.SessionType
import com.example.eventmanager.model.Validation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionFormScreen(
    viewModel: EventManagerViewModel,
    eventId: Long,
    sessionId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var draft by remember { mutableStateOf(SessionDraft(id = sessionId, eventId = eventId)) }
    var errors by remember { mutableStateOf(emptyMap<String, String>()) }
    var typeMenuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(sessionId) {
        if (sessionId != 0L) {
            viewModel.session(sessionId)?.let { session ->
                draft = SessionDraft(
                    id = session.id,
                    eventId = session.eventId,
                    name = session.name,
                    time = session.time,
                    speaker = session.speaker,
                    room = session.room,
                    type = session.type,
                    description = session.description
                )
            }
        }
    }

    fun save() {
        errors = Validation.sessionErrors(draft.name, draft.time)
        if (errors.isEmpty()) viewModel.saveSession(draft) { onSaved() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (sessionId == 0L) "Add session" else "Edit session") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Session details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "The session ID is generated automatically.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                FormField(
                    value = draft.name,
                    onValueChange = { draft = draft.copy(name = it); errors = errors - "name" },
                    label = "Session name *",
                    error = errors["name"]
                )
            }
            item {
                TimePickerField(
                    value = draft.time,
                    onValueChange = { draft = draft.copy(time = it); errors = errors - "time" },
                    label = "Session time *",
                    error = errors["time"]
                )
            }
            item {
                ExposedDropdownMenuBox(
                    expanded = typeMenuOpen,
                    onExpandedChange = { typeMenuOpen = !typeMenuOpen }
                ) {
                    OutlinedTextField(
                        value = draft.type.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Session type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeMenuOpen) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = typeMenuOpen,
                        onDismissRequest = { typeMenuOpen = false }
                    ) {
                        SessionType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label) },
                                onClick = {
                                    draft = draft.copy(type = type)
                                    typeMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }
            item {
                FormField(
                    value = draft.speaker,
                    onValueChange = { draft = draft.copy(speaker = it) },
                    label = "Speaker"
                )
            }
            item {
                FormField(
                    value = draft.room,
                    onValueChange = { draft = draft.copy(room = it) },
                    label = "Room"
                )
            }
            item {
                FormField(
                    value = draft.description,
                    onValueChange = { draft = draft.copy(description = it) },
                    label = "Description",
                    singleLine = false,
                    minLines = 3
                )
            }
            item {
                Button(onClick = ::save, modifier = Modifier.fillMaxWidth()) {
                    Text(if (sessionId == 0L) "Add session" else "Save changes")
                }
            }
        }
    }
}
