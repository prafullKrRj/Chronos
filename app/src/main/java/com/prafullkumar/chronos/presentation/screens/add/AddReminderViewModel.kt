package com.prafullkumar.chronos.presentation.screens.add

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prafullkumar.chronos.core.Resource
import com.prafullkumar.chronos.domain.model.Reminder
import com.prafullkumar.chronos.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject


sealed interface AddReminderEvent {
    data object NavigateBack : AddReminderEvent
    data class ShowError(val message: String) : AddReminderEvent
}

@HiltViewModel
class AddReminderViewModel @Inject constructor(
    private val repository: ReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddReminderUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddReminderEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onNotesChange(newNotes: String) {
        _uiState.update { it.copy(notes = newNotes) }
    }

    fun onReminderTypeChange(type: String) {
        _uiState.update { it.copy(reminderType = type) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { currentState ->
            val time = currentState.selectedTime ?: LocalTime.now()
            val dateTime = LocalDateTime.of(date, time)

            currentState.copy(
                selectedDate = date,
                selectedDateTime = dateTime
            )
        }
    }

    fun onTimeSelected(time: LocalTime) {
        _uiState.update { currentState ->
            val date = currentState.selectedDate ?: LocalDate.now()
            val dateTime = LocalDateTime.of(date, time)

            currentState.copy(
                selectedTime = time,
                selectedDateTime = dateTime
            )
        }
    }

    fun onDateTimeSelected(dateTime: LocalDateTime) {
        _uiState.update {
            it.copy(
                selectedDateTime = dateTime,
                selectedDate = dateTime.toLocalDate(),
                selectedTime = dateTime.toLocalTime(),
                showTimePicker = false,
                showDatePicker = false
            )
        }
    }

    fun showDatePicker(show: Boolean) {
        _uiState.update { it.copy(showDatePicker = show) }
    }

    fun showTimePicker(show: Boolean) {
        _uiState.update { it.copy(showTimePicker = show) }
    }

    // Functions for emoji picker
    fun onEmojiSelected(emoji: String) {
        _uiState.update { it.copy(emoji = emoji, showEmojiPicker = false) }
    }

    fun showEmojiPicker(show: Boolean) {
        _uiState.update { it.copy(showEmojiPicker = show) }
    }

    fun onImageSelected(uri: Uri) {
        _uiState.update {
            it.copy(
                selectedImageUri = uri,
                showImagePicker = false,
                emoji = "🖼️" // Set a default emoji when image is selected
            )
        }
    }

    fun onRemoveImage() {
        _uiState.update {
            it.copy(
                selectedImageUri = null,
                emoji = "⏰" // Reset to default emoji
            )
        }
    }

    fun showImagePicker(show: Boolean) {
        _uiState.update { it.copy(showImagePicker = show) }
    }

    fun saveReminder() {
        if (!_uiState.value.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val currentState = _uiState.value

            val dateTime = currentState.getDateTime()
            if (dateTime == null) {
                _uiState.update { it.copy(isLoading = false) }
                _eventFlow.emit(AddReminderEvent.ShowError("Please select a valid date and time"))
                return@launch
            }

            try {
                val reminder = Reminder(
                    title = currentState.title.trim(),
                    description = currentState.notes.trim(),
                    dateTime = dateTime.atZone(ZoneId.systemDefault())
                        .toInstant().toEpochMilli(),
                    emoji = currentState.emoji,
                )

                repository.saveReminder(reminder, currentState.selectedImageUri).collectLatest { response ->
                    when (response) {
                        is Resource.Error -> {
                            _uiState.update { it.copy(isLoading = false) }
                            val errorMessage = when {
                                response.message?.contains("network", ignoreCase = true) == true ->
                                    "No internet connection. Please check your network and try again."

                                response.message?.contains("timeout", ignoreCase = true) == true ->
                                    "Request timed out. Please try again."

                                else -> "Failed to save reminder: ${response.message ?: "Unknown error"}"
                            }
                            _eventFlow.emit(AddReminderEvent.ShowError(errorMessage))
                        }

                        Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }

                        is Resource.Success<*> -> {
                            _uiState.update { it.copy(isLoading = false) }
                            _eventFlow.emit(AddReminderEvent.NavigateBack)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _eventFlow.emit(AddReminderEvent.ShowError("Failed to upload image: ${e.message}"))
            }
        }
    }
}