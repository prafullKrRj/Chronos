package com.prafullkumar.chronos.presentation.screens.reminderFromNavigation

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.prafullkumar.chronos.domain.model.Reminder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatDateTime(dateTime: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return formatter.format(Date(dateTime))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderFromNavigationScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReminderFromNavigationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle error events
    LaunchedEffect(viewModel.eventFlow) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ReminderFromNavigationEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        withDismissAction = true
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminder") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.error != null) {
                        IconButton(onClick = { viewModel.retryLoadReminder() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retry")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.reminder != null -> {
                    MinimalistReminderContent(
                        reminder = uiState.reminder!!,
                        uiState = uiState,
                        viewModel = viewModel,
                        onShareMessage = { message ->
                            shareMessage(context, message)
                        }
                    )
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error ?: "An error occurred",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(onClick = { viewModel.retryLoadReminder() }) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Retry")
                            }
                            Button(onClick = onNavigateBack) {
                                Text("Go Back")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MinimalistReminderContent(
    reminder: Reminder,
    uiState: ReminderFromNavigationUiState,
    viewModel: ReminderFromNavigationViewModel,
    onShareMessage: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Simple title display
        Text(
            text = "${reminder.emoji} ${reminder.title}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = formatDateTime(reminder.dateTime),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (reminder.description.isNotBlank()) {
            Text(
                text = reminder.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Message Generation Section
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                uiState.isComposingMessage -> {
                    // Show message composition
                    OutlinedTextField(
                        value = uiState.customMessage,
                        onValueChange = viewModel::updateCustomMessage,
                        label = { Text("Your message") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = viewModel::cancelComposingMessage,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = { onShareMessage(uiState.customMessage) },
                            enabled = uiState.customMessage.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Share")
                        }
                    }
                }

                uiState.isComposingGreeting -> {
                    // Show greeting composition
                    OutlinedTextField(
                        value = uiState.greetingPrompt,
                        onValueChange = viewModel::updateGreetingPrompt,
                        label = { Text("Enter prompt (e.g., 'wish John happy birthday')") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("wish someone happy birthday") }
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = viewModel::cancelComposingGreeting,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = viewModel::generateGreeting,
                            enabled = uiState.greetingPrompt.isNotBlank() && !uiState.isGeneratingGreeting,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.isGeneratingGreeting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Generate")
                            }
                        }
                    }
                }

                else -> {
                    // Show action buttons
                    OutlinedButton(
                        onClick = viewModel::startComposingMessage,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Create, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Write Custom Message")
                    }

                    OutlinedButton(
                        onClick = viewModel::startComposingGreeting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate AI Greeting")
                    }
                }
            }
        }
    }
}


private fun shareMessage(context: Context, message: String) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
        putExtra(Intent.EXTRA_SUBJECT, "Message from Chronos")
    }

    val chooserIntent = Intent.createChooser(shareIntent, "Share Message")
    context.startActivity(chooserIntent)
}