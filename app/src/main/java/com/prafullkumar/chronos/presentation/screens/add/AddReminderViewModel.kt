package com.prafullkumar.chronos.presentation.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prafullkumar.chronos.core.Resource
import com.prafullkumar.chronos.domain.model.Reminder
import com.prafullkumar.chronos.domain.repository.HomeRepository
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
}

@HiltViewModel
class AddReminderViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val homeRepository: HomeRepository
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

    fun saveReminder() {
        if (!_uiState.value.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val currentState = _uiState.value

            // Get the combined date and time
            val dateTime = currentState.getDateTime()

            if (dateTime == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            val reminder = Reminder(
                title = currentState.title.trim(),
                description = currentState.notes.trim(),
                dateTime = dateTime.atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli(),
                emoji = currentState.emoji
            )

            repository.saveReminder(reminder).collectLatest { response ->
                when (response) {
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }

                    Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }

                    is Resource.Success<*> -> {
                        _uiState.update { it.copy(isLoading = false) }
                        // Invalidate cache after adding a new reminder
                        homeRepository.invalidateCache()
                        _eventFlow.emit(AddReminderEvent.NavigateBack)
                    }
                }
            }
        }
    }
}