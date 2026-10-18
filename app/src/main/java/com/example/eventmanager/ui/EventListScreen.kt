package com.example.eventmanager.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import com.example.eventmanager.data.EventWithSessionCount
import com.example.eventmanager.model.EventDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    viewModel: EventManagerViewModel,
    onAdd: () -> Unit,
    onEvent: (Long) -> Unit
) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val date by viewModel.dateFilter.collectAsStateWithLifecycle()
    val organiser by viewModel.organiserFilter.collectAsStateWithLifecycle()
    val uploadInfo by viewModel.uploadInfo.collectAsStateWithLifecycle()
    var filtersVisible by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(uploadInfo?.state) {
        val info = uploadInfo ?: return@LaunchedEffect
        when (info.state) {
            WorkInfo.State.SUCCEEDED -> snackbar.showSnackbar(
                info.outputData.getString("message") ?: "Upload complete"
            )
            WorkInfo.State.FAILED -> snackbar.showSnackbar(
                info.outputData.getString("message") ?: "Upload failed"
            )
            WorkInfo.State.ENQUEUED -> snackbar.showSnackbar("Upload queued until a network is available")
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Event Manager", fontWeight = FontWeight.Bold)
                        Text(
                            "Plan every session in one place",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { filtersVisible = !filtersVisible }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Advanced filters")
                    }
                    IconButton(onClick = viewModel::uploadAll) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Upload all events")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New event") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search events") },
                placeholder = { Text("Name or description") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
            AnimatedVisibility(filtersVisible) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DatePickerField(
                            value = date,
                            onValueChange = { viewModel.dateFilter.value = it },
                            modifier = Modifier.weight(1f),
                            label = "Date"
                        )
                        OutlinedTextField(
                            value = organiser,
                            onValueChange = { viewModel.organiserFilter.value = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Organiser") },
                            singleLine = true
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (events.isEmpty()) {
                EmptyEvents(hasSearch = query.isNotBlank() || date != null || organiser.isNotBlank())
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(events, key = { it.event.id }) { item ->
                        EventCard(item = item, onClick = { onEvent(item.event.id) })
                    }
                    item { Spacer(Modifier.height(88.dp)) }
                }
            }
        }
    }
}

@Composable
private fun EventCard(item: EventWithSessionCount, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.event.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    if (item.event.syncedAt == null) "Not synced" else "Synced",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (item.event.syncedAt == null) {
                        MaterialTheme.colorScheme.primary
                    } else MaterialTheme.colorScheme.tertiary
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Type: ${item.event.eventType.label}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Status: ${item.event.status.label}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SmallFact(Icons.Default.DateRange, EventDateTime.displayDate(item.event.date))
                SmallFact(Icons.Default.LocationOn, item.event.location)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SmallFact(Icons.Default.Person, item.event.organiser)
                Text(
                    "${item.sessionCount} session${if (item.sessionCount == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SmallFact(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
    }
}

@Composable
private fun EmptyEvents(hasSearch: Boolean) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (hasSearch) "No matching events" else "No events yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                if (hasSearch) "Try a different search or filter." else "Tap New event to add your first event.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
