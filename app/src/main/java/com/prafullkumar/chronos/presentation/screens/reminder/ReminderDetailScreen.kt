package com.prafullkumar.chronos.presentation.screens.reminder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.prafullkumar.chronos.domain.model.Reminder
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    viewModel: ReminderDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onEdit: (Reminder) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var isMenuExpanded by remember { mutableStateOf(false) }
    val reminder = uiState.reminder
    val isLoading = uiState.isLoading

    LaunchedEffect(true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ReminderDetailEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !isLoading) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { isMenuExpanded = true }, enabled = !isLoading) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options"
                            )
                        }
                        DropdownMenu(
                            expanded = isMenuExpanded && !isLoading,
                            onDismissRequest = { isMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    isMenuExpanded = false
                                    reminder?.let { onEdit(it) }
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Edit, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Delete",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    isMenuExpanded = false
                                    viewModel.showDeleteDialog(true)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (reminder != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Emoji and Title Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Image or Emoji Container
                        if (reminder.imageUrl != null) {
                            AsyncImage(
                                model = reminder.imageUrl,
                                contentDescription = "Reminder image",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = reminder.emoji,
                                    fontSize = 42.sp
                                )
                            }
                        }

                        // Title
                        Text(
                            text = reminder.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Date and Time
                            val formattedDateTime = remember(reminder.dateTime) {
                                val instant = Instant.ofEpochMilli(reminder.dateTime)
                                val formatter =
                                    DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy • h:mm a")
                                        .withZone(ZoneId.systemDefault())
                                formatter.format(instant)
                            }
                            DetailItem(label = "When", value = formattedDateTime)

                            // Type
                            if (!reminder.type.isNullOrBlank()) {
                                DetailItem(label = "Type", value = reminder.type)
                            }

                            // Description
                            if (reminder.description.isNotBlank()) {
                                DetailItem(label = "Notes", value = reminder.description)
                            }
                        }
                    }
                }
            }

            // Loading overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // Delete Dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showDeleteDialog(false) },
            title = { Text("Delete Reminder?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::deleteReminder
                ) {
                Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error
                )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showDeleteDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}