package com.prafullkumar.chronos.presentation.screens.add

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class AddReminderUiState(
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
) {
    val isFormValid: Boolean
        get() {
            val dateTimeValid = if (selectedDate != null && selectedTime != null) {
                LocalDateTime.of(selectedDate, selectedTime).isAfter(LocalDateTime.now())
            } else {
                false
            }

            return title.isNotBlank() && dateTimeValid
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