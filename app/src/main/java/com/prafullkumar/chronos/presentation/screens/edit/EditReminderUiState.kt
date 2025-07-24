package com.prafullkumar.chronos.presentation.screens.edit

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class EditReminderUiState(
    val title: String = "",
    val notes: String = "",
    val emoji: String = "⏰",
    val selectedDateTime: LocalDateTime? = null,
    val selectedDate: LocalDate? = null,
    val selectedTime: LocalTime? = null,
    val reminderType: String = "Personal",
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val showEmojiPicker: Boolean = false,
    val isLoading: Boolean = false,
    val isInitialized: Boolean = false,
    val error: String? = null
) {
    val isFormValid: Boolean
        get() {
            val dateTimeValid = if (selectedDate != null && selectedTime != null) {
                LocalDateTime.of(selectedDate, selectedTime).isAfter(LocalDateTime.now())
            } else {
                false
            }

            return title.isNotBlank() && dateTimeValid && isInitialized
        }

    // Helper function to get the combined DateTime
    fun getDateTime(): LocalDateTime? {
        return if (selectedDate != null && selectedTime != null) {
            LocalDateTime.of(selectedDate, selectedTime)
        } else {
            null
        }
    }
}

sealed interface EditReminderEvent {
    data object NavigateBack : EditReminderEvent
    data class ShowError(val message: String) : EditReminderEvent
}

