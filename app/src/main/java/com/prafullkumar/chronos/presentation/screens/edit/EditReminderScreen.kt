package com.prafullkumar.chronos.presentation.screens.edit

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun EditReminderScreen(
    reminderId: String,
    title: String,
    dateTime: Long,
    notes: String,
    emoji: String,
    type: String,
    imageUrl: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    viewModel: EditReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.initializeReminderData(
            id = reminderId,
            title = title,
            dateTime = dateTime,
            notes = notes,
            emoji = emoji,
            type = type,
            imageUrl = imageUrl
        )
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is EditReminderEvent.NavigateBack -> onNavigateBack()
                is EditReminderEvent.NavigateToHome -> onNavigateToHome()
                is EditReminderEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EditReminderTopBar(
                onNavigateBack = onNavigateBack,
                onSave = viewModel::updateReminder,
                isFormValid = uiState.isFormValid,
                isLoading = uiState.isLoading
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            EditReminderContent(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                uiState = uiState,
                onTitleChange = viewModel::onTitleChange,
                onNotesChange = viewModel::onNotesChange,
                onEmojiSelected = viewModel::onEmojiSelected,
                showEmojiPicker = viewModel::showEmojiPicker,
                onReminderTypeChange = viewModel::onReminderTypeChange,
                showDatePicker = viewModel::showDatePicker,
                showTimePicker = viewModel::showTimePicker,
                onDateSelected = viewModel::onDateSelected,
                onTimeSelected = viewModel::onTimeSelected,
                onImageSelected = viewModel::onImageSelected,
                onRemoveImage = viewModel::onRemoveImage,
                showImagePicker = viewModel::showImagePicker
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditReminderTopBar(
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    isFormValid: Boolean,
    isLoading: Boolean
) {
    TopAppBar(
        title = { Text("Edit Reminder") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        },
        actions = {
            TextButton(
                onClick = onSave,
                enabled = isFormValid && !isLoading
            ) {
                Text("Save")
            }
        }
    )
}

@Composable
private fun EditReminderContent(
    modifier: Modifier = Modifier,
    uiState: EditReminderUiState,
    onTitleChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onEmojiSelected: (String) -> Unit,
    showEmojiPicker: (Boolean) -> Unit,
    onReminderTypeChange: (String) -> Unit,
    showDatePicker: (Boolean) -> Unit,
    showTimePicker: (Boolean) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onRemoveImage: () -> Unit,
    showImagePicker: (Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val isEnabled = !uiState.isLoading

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        CircularImagePicker(
            selectedImageUri = uiState.selectedImageUri,
            currentImageUrl = uiState.currentImageUrl,
            selectedEmoji = uiState.emoji,
            onClick = { showImagePicker(true) },
            onRemoveImage = if (uiState.selectedImageUri != null || uiState.currentImageUrl != null) onRemoveImage else null,
            enabled = isEnabled
        )

        EmojiPicker(
            selectedEmoji = uiState.emoji,
            onClick = { showEmojiPicker(true) },
            enabled = isEnabled
        )

        TitleInputField(
            title = uiState.title,
            onTitleChange = onTitleChange,
            isEnabled = isEnabled,
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        )

        DateTimeInputFields(
            selectedDate = uiState.selectedDate,
            selectedTime = uiState.selectedTime,
            onDateClick = { showDatePicker(true) },
            onTimeClick = { showTimePicker(true) },
            enabled = isEnabled
        )

        ReminderTypeDropdown(
            selectedType = uiState.reminderType,
            onTypeSelected = onReminderTypeChange,
            enabled = isEnabled
        )

        NotesInputField(
            notes = uiState.notes,
            onNotesChange = onNotesChange,
            isEnabled = isEnabled,
            onDone = { focusManager.clearFocus() }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    DateTimePickerDialogs(
        uiState = uiState,
        showDatePicker = showDatePicker,
        showTimePicker = showTimePicker,
        onDateSelected = onDateSelected,
        onTimeSelected = onTimeSelected
    )

    if (uiState.showEmojiPicker) {
        EmojiPickerDialog(
            onDismiss = { showEmojiPicker(false) },
            onEmojiSelected = onEmojiSelected
        )
    }

    if (uiState.showImagePicker) {
        ImagePickerDialog(
            onDismiss = { showImagePicker(false) },
            onImageSelected = onImageSelected
        )
    }
}

@Composable
fun ImagePickerDialog(onDismiss: () -> Unit, onImageSelected: (Uri) -> Unit) {
    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // Create a temporary file for camera capture
    val tempImageFile = remember {
        File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onImageSelected(it)
            onDismiss()
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            onImageSelected(tempImageUri!!)
            onDismiss()
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tempImageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                tempImageFile
            )
            cameraLauncher.launch(tempImageUri!!)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Image Source",
                    style = MaterialTheme.typography.titleLarge
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Camera option
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                when (PackageManager.PERMISSION_GRANTED) {
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) -> {
                                        tempImageUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            tempImageFile
                                        )
                                        cameraLauncher.launch(tempImageUri!!)
                                    }

                                    else -> {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Camera",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Gallery option
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                galleryLauncher.launch("image/*")
                            },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Photo,
                                contentDescription = "Gallery",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Gallery",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun CircularImagePicker(
    selectedImageUri: Uri?,
    currentImageUrl: String?,
    selectedEmoji: String,
    onClick: () -> Unit,
    onRemoveImage: (() -> Unit)?,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            selectedImageUri != null -> {
                // Display selected image from URI using Coil (highest priority)
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Selected image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            !currentImageUrl.isNullOrBlank() -> {
                // Display current image from URL using Coil (second priority)
                AsyncImage(
                    model = currentImageUrl,
                    contentDescription = "Current image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            selectedEmoji.isNotBlank() -> {
                // Display emoji when no image is selected (third priority)
                Text(
                    text = selectedEmoji,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> {
                // Default placeholder (lowest priority)
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add image",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Remove button overlay (show when there's an image to remove)
        if (onRemoveImage != null && (selectedImageUri != null || !currentImageUrl.isNullOrBlank())) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
                    .clickable { onRemoveImage() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove image",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun TitleInputField(
    title: String,
    onTitleChange: (String) -> Unit,
    isEnabled: Boolean,
    onNext: () -> Unit
) {
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Title") },
        leadingIcon = { Icon(Icons.Default.Title, null) },
        singleLine = true,
        isError = title.isBlank(),
        enabled = isEnabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { onNext() })
    )
}

@Composable
private fun NotesInputField(
    notes: String,
    onNotesChange: (String) -> Unit,
    isEnabled: Boolean,
    onDone: () -> Unit
) {
    OutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        label = { Text("Notes") },
        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null) },
        enabled = isEnabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() })
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimePickerDialogs(
    uiState: EditReminderUiState,
    showDatePicker: (Boolean) -> Unit,
    showTimePicker: (Boolean) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (LocalTime) -> Unit
) {
    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.selectedDate?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()?.toEpochMilli()
                ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onDateSelected(selectedDate)
                            showDatePicker(false)
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker(false) }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (uiState.showTimePicker) {
        val currentTime = uiState.selectedTime ?: LocalTime.now()
        val timePickerState = rememberTimePickerState(
            initialHour = currentTime.hour,
            initialMinute = currentTime.minute,
            is24Hour = false
        )

        TimePickerDialog(
            onDismissRequest = { showTimePicker(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedTime = LocalTime.of(
                            timePickerState.hour,
                            timePickerState.minute
                        )
                        onTimeSelected(selectedTime)
                        showTimePicker(false)
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker(false) }) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
private fun EmojiPicker(
    selectedEmoji: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = selectedEmoji,
                style = MaterialTheme.typography.displayMedium
            )
        }
        Text(
            "Tap to change icon",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmojiPickerDialog(
    onDismiss: () -> Unit,
    onEmojiSelected: (String) -> Unit
) {
    val emojiList = listOf(
        "⏰", "📅", "✅", "🎉", "💼", "✈️", "❤️", "🎂", "🛒",
        "💊", "📞", "💡", "🏠", "🎁", "🧑‍💻", "🍽️", "💪", "📖"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Select an Icon",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(emojiList) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    onEmojiSelected(emoji)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTypeDropdown(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    enabled: Boolean
) {
    val reminderTypes = listOf("Personal", "Work", "Appointment", "Other")
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { if (enabled) isExpanded = it }
    ) {
        OutlinedTextField(
            value = selectedType,
            onValueChange = {},
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            label = { Text("Type") },
            leadingIcon = { Icon(Icons.Default.Category, null) },
            readOnly = true,
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) }
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            reminderTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        onTypeSelected(type)
                        isExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DateTimeInputFields(
    selectedDate: LocalDate?,
    selectedTime: LocalTime?,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    enabled: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Selection Field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = enabled, onClick = onDateClick)
            ) {
                OutlinedTextField(
                    value = selectedDate?.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))
                        ?: "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Date") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    readOnly = true,
                    enabled = false,
                    isError = selectedDate == null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // Time Selection Field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = enabled, onClick = onTimeClick)
            ) {
                OutlinedTextField(
                    value = selectedTime?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Time") },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    readOnly = true,
                    enabled = false,
                    isError = selectedTime == null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Error message if needed
        val isDateTimeError = (selectedDate != null && selectedTime != null) &&
                !LocalDateTime.of(selectedDate, selectedTime).isAfter(LocalDateTime.now())

        if (isDateTimeError) {
            Text(
                text = "Please select a future date and time",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun TimePickerDialog(
    title: String = "Select Time",
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title, style = MaterialTheme.typography.labelLarge) },
        text = {
            Box(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        },
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}