package com.prafullkumar.chronos.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.prafullkumar.chronos.data.preferences.ThemeMode
import com.prafullkumar.chronos.presentation.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentTheme by viewModel.themeMode.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.settingsEvent) {
        viewModel.settingsEvent.collect { event ->
            when (event) {
                is SettingsEvent.SignOut -> {
                    navController.navigate(Routes.LoginScreen) {
                        popUpTo(Routes.HomeScreen) { inclusive = true }
                    }
                }
            }
        }
    }

    // Show error and success messages
    LaunchedEffect(uiState.error, uiState.successMessage) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                withDismissAction = true
            )
            viewModel.clearMessage()
        }
        uiState.successMessage?.let { success ->
            snackbarHostState.showSnackbar(
                message = success,
                withDismissAction = true
            )
            viewModel.clearMessage()
        }
    }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showDeleteOldDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = navController::popBackStack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Image
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.userData.photoUrl != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(uiState.userData.photoUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = uiState.userData.displayName?.firstOrNull()?.toString()
                                        ?: "?",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // User Name
                        Text(
                            text = uiState.userData.displayName ?: "User",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // User Email
                        Text(
                            text = uiState.userData.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            item {
                SettingsSectionHeader("Appearance")
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.BrightnessMedium,
                    title = "Theme",
                    subtitle = when (currentTheme) {
                        ThemeMode.LIGHT -> "Light"
                        ThemeMode.DARK -> "Dark"
                        ThemeMode.SYSTEM -> "System"
                    },
                    onClick = { showThemeDialog = true }
                )
            }

            item {
                SettingsSectionHeader("Data Management")
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.DeleteSweep,
                    title = "Delete older reminders",
                    subtitle = "Remove reminders before this time",
                    onClick = { showDeleteOldDialog = true }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Warning,
                    title = "Delete all reminders",
                    subtitle = "Permanently remove all data",
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { showDeleteAllDialog = true }
                )
            }

            item {
                SettingsSectionHeader("Account")
            }
            item {
                SettingsItem(
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    title = "Sign Out",
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { showSignOutDialog = true }
                )
            }

            item {
                SettingsSectionHeader("About")
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = "App Version",
                    subtitle = "1.0.0"
                )
            }
        }
    }

    if (showThemeDialog) {
        ThemeChooserDialog(
            currentTheme = currentTheme,
            onThemeSelected = { newTheme ->
                viewModel.setThemeMode(newTheme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Sign Out Confirmation Dialog
    if (showSignOutDialog) {
        ConfirmationDialog(
            title = "Sign Out?",
            text = "Are you sure you want to sign out?",
            confirmButtonText = "Sign Out",
            onConfirm = {
                showSignOutDialog = false
                viewModel.signOut()
            },
            onDismiss = { showSignOutDialog = false }
        )
    }

    // Delete Old Reminders Confirmation Dialog
    if (showDeleteOldDialog) {
        ConfirmationDialog(
            title = "Delete Old Reminders?",
            text = "This will permanently delete all reminders older than 30 days. This action cannot be undone.",
            confirmButtonText = "Delete",
            onConfirm = {
                showDeleteOldDialog = false
                viewModel.deleteOldReminders()
            },
            onDismiss = { showDeleteOldDialog = false }
        )
    }

    // Delete All Reminders Confirmation Dialog
    if (showDeleteAllDialog) {
        ConfirmationDialog(
            title = "Delete All Reminders?",
            text = "This will permanently delete all of your reminders. This action cannot be undone.",
            confirmButtonText = "Delete All",
            onConfirm = {
                showDeleteAllDialog = false
                viewModel.deleteAllReminders()
            },
            onDismiss = { showDeleteAllDialog = false }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) {
    ListItem(
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
        headlineContent = {
            Text(title, color = titleColor, fontWeight = FontWeight.Normal)
        },
        supportingContent = {
            if (subtitle != null) {
                Text(subtitle)
            }
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@Composable
fun ConfirmationDialog(
    title: String,
    text: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ThemeChooserDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val themes = listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose theme") },
        text = {
            Column(Modifier.selectableGroup()) {
                themes.forEach { theme ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (theme == currentTheme),
                                onClick = {
                                    onThemeSelected(theme)
                                },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == currentTheme),
                            onClick = null // Recommended for accessibility
                        )
                        Text(
                            text = theme.name.capitalize(),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Extension function to capitalize the first letter of a string
private fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar { it.uppercase() }
}
