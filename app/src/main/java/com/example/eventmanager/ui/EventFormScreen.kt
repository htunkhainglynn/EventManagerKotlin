package com.example.eventmanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.eventmanager.model.EventStatus
import com.example.eventmanager.model.EventType
import com.example.eventmanager.model.EventDateTime
import com.example.eventmanager.model.Validation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFormScreen(
    viewModel: EventManagerViewModel,
    eventId: Long,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit
) {
    var draft by remember { mutableStateOf(EventDraft(id = eventId)) }
    var errors by remember { mutableStateOf(emptyMap<String, String>()) }
    var showConfirmation by remember { mutableStateOf(false) }
    var typeMenuOpen by remember { mutableStateOf(false) }
    var statusMenuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        if (eventId != 0L) {
            viewModel.event(eventId)?.let { event ->
                draft = EventDraft(
                    id = event.id,
                    name = event.name,
                    date = event.date,
                    organiser = event.organiser,
                    location = event.location,
                    eventType = event.eventType,
                    status = event.status,
                    description = event.description,
                    contactEmail = event.contactEmail,
                    contactPhone = event.contactPhone
                )
            }
        }
    }

    fun validateAndReview() {
        errors = Validation.eventErrors(
            draft.name,
            draft.date,
            draft.organiser,
            draft.location,
            draft.contactEmail,
            draft.contactPhone
        )
        if (errors.isEmpty()) {
            viewModel.checkDuplicate(draft) { duplicate ->
                if (duplicate) errors = mapOf("name" to "An event with this name and date already exists")
                else showConfirmation = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (eventId == 0L) "Create event" else "Edit event") },
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
                Text(
                    "Required details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "You will review these details before they are saved.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                FormField(
                    value = draft.name,
                    onValueChange = { draft = draft.copy(name = it); errors = errors - "name" },
                    label = "Event name *",
                    error = errors["name"]
                )
            }
            item {
                DatePickerField(
                    value = draft.date,
                    onValueChange = { draft = draft.copy(date = it); errors = errors - "date" },
                    label = "Event date *",
                    error = errors["date"]
                )
            }
            item {
                FormField(
                    value = draft.organiser,
                    onValueChange = { draft = draft.copy(organiser = it); errors = errors - "organiser" },
                    label = "Organiser *",
                    error = errors["organiser"]
                )
            }
            item {
                ExposedDropdownMenuBox(
                    expanded = typeMenuOpen,
                    onExpandedChange = { typeMenuOpen = !typeMenuOpen }
                ) {
                    OutlinedTextField(
                        value = draft.eventType.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Event type *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeMenuOpen) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = typeMenuOpen,
                        onDismissRequest = { typeMenuOpen = false }
                    ) {
                        EventType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label) },
                                onClick = {
                                    draft = draft.copy(eventType = type)
                                    typeMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }
            item {
                ExposedDropdownMenuBox(
                    expanded = statusMenuOpen,
                    onExpandedChange = { statusMenuOpen = !statusMenuOpen }
                ) {
                    OutlinedTextField(
                        value = draft.status.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Event status *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statusMenuOpen) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = statusMenuOpen,
                        onDismissRequest = { statusMenuOpen = false }
                    ) {
                        EventStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.label) },
                                onClick = {
                                    draft = draft.copy(status = status)
                                    statusMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }
            item {
                FormField(
                    value = draft.location,
                    onValueChange = { draft = draft.copy(location = it); errors = errors - "location" },
                    label = "Location *",
                    error = errors["location"]
                )
            }
            item {
                Text(
                    "Optional details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                FormField(
                    value = draft.description,
                    onValueChange = { draft = draft.copy(description = it) },
                    label = "Event description",
                    singleLine = false,
                    minLines = 3
                )
            }
            item {
                FormField(
                    value = draft.contactEmail,
                    onValueChange = { draft = draft.copy(contactEmail = it); errors = errors - "email" },
                    label = "Contact email",
                    error = errors["email"],
                    keyboardType = KeyboardType.Email
                )
            }
            item {
                FormField(
                    value = draft.contactPhone,
                    onValueChange = { draft = draft.copy(contactPhone = it); errors = errors - "phone" },
                    label = "Contact phone",
                    error = errors["phone"],
                    keyboardType = KeyboardType.Phone
                )
            }
            item {
                Button(onClick = ::validateAndReview, modifier = Modifier.fillMaxWidth()) {
                    Text("Review event")
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Confirm event details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ConfirmationRow("Name", draft.name)
                    ConfirmationRow("Date", draft.date?.let(EventDateTime::displayDate).orEmpty())
                    ConfirmationRow("Organiser", draft.organiser)
                    ConfirmationRow("Location", draft.location)
                    ConfirmationRow("Type", draft.eventType.label)
                    ConfirmationRow("Status", draft.status.label)
                    if (draft.description.isNotBlank()) ConfirmationRow("Description", draft.description)
                    if (draft.contactEmail.isNotBlank()) ConfirmationRow("Email", draft.contactEmail)
                    if (draft.contactPhone.isNotBlank()) ConfirmationRow("Phone", draft.contactPhone)
                }
            },
            confirmButton = {
                Button(onClick = {
                    showConfirmation = false
                    viewModel.saveEvent(draft, onSaved)
                }) { Text("Confirm and save") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmation = false }) { Text("Go back and edit") }
            }
        )
    }
}

@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = minLines,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
private fun ConfirmationRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.35f))
        Text(value, modifier = Modifier.weight(0.65f))
    }
}
