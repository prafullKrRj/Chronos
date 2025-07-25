package com.prafullkumar.chronos.presentation.screens.edit

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

@HiltViewModel
class EditReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditReminderUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<EditReminderEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var reminderId: String = ""

    fun initializeReminderData(
        id: String,
        title: String,
        dateTime: Long,
        notes: String,
        emoji: String,
        type: String,
        imageUrl: String? = null
    ) {
        reminderId = id
        val localDateTime = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(dateTime),
            ZoneId.systemDefault()
        )
        Log.d("EditReminderViewModel", "initializeReminderData: $imageUrl")
        _uiState.update {
            it.copy(
                title = title,
                notes = notes,
                emoji = emoji,
                reminderType = type,
                selectedDateTime = localDateTime,
                selectedDate = localDateTime.toLocalDate(),
                selectedTime = localDateTime.toLocalTime(),
                currentImageUrl = imageUrl,
                isInitialized = true
            )
        }
    }

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

    fun showDatePicker(show: Boolean) {
        _uiState.update { it.copy(showDatePicker = show) }
    }

    fun showTimePicker(show: Boolean) {
        _uiState.update { it.copy(showTimePicker = show) }
    }

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
                showImagePicker = false
            )
        }
    }

    fun onRemoveImage() {
        _uiState.update {
            it.copy(
                selectedImageUri = null,
                currentImageUrl = null
            )
        }
    }

    fun showImagePicker(show: Boolean) {
        _uiState.update { it.copy(showImagePicker = show) }
    }

    fun updateReminder() {
        if (!_uiState.value.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val currentState = _uiState.value

            val dateTime = currentState.getDateTime()
            if (dateTime == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            try {
                var imageUrl = currentState.currentImageUrl

                // Upload new image if selected, this will overwrite existing image
                if (currentState.selectedImageUri != null) {
                    imageUrl = reminderRepository.uploadImage(currentState.selectedImageUri, reminderId)
                }
                // If image was removed
                else if (currentState.selectedImageUri == null && currentState.currentImageUrl == null) {
                    imageUrl = null
                }

                val reminder = Reminder(
                    id = reminderId,
                    title = currentState.title.trim(),
                    description = currentState.notes.trim(),
                    dateTime = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    emoji = currentState.emoji,
                    type = currentState.reminderType,
                    imageUrl = imageUrl
                )

                reminderRepository.updateReminder(reminder).collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            _uiState.update { it.copy(isLoading = false) }
                            _eventFlow.emit(EditReminderEvent.NavigateToHome)
                        }

                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = result.message ?: "Failed to update reminder"
                                )
                            }
                            _eventFlow.emit(
                                EditReminderEvent.ShowError(
                                    result.message ?: "Failed to update reminder"
                                )
                            )
                        }

                        Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _eventFlow.emit(EditReminderEvent.ShowError("Failed to upload image: ${e.message}"))
            }
        }
    }
}
