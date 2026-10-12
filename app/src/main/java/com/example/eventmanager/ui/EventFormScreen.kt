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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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

    LaunchedEffect(eventId) {
        if (eventId != 0L) {
            viewModel.event(eventId)?.let { event ->
                draft = EventDraft(
                    id = event.id,
                    name = event.name,
                    date = event.date,
                    organiser = event.organiser,
                    location = event.location,
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
                FormField(
                    value = draft.date,
                    onValueChange = { draft = draft.copy(date = it); errors = errors - "date" },
                    label = "Date *",
                    placeholder = "DD/MM/YYYY",
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
                    ConfirmationRow("Date", draft.date)
                    ConfirmationRow("Organiser", draft.organiser)
                    ConfirmationRow("Location", draft.location)
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
