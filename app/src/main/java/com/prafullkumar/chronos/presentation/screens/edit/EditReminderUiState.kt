package com.prafullkumar.chronos.presentation.screens.edit

import android.net.Uri
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class EditReminderUiState(
    val title: String = "",
    val notes: String = "",
    val emoji: String = "⏰",
    val reminderType: String = "Personal",
    val selectedDate: LocalDate? = null,
    val selectedTime: LocalTime? = null,
    val selectedDateTime: LocalDateTime? = null,
    val selectedImageUri: Uri? = null,
    val currentImageUrl: String? = null,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val showEmojiPicker: Boolean = false,
    val showImagePicker: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isInitialized: Boolean = false
) {
    val isFormValid: Boolean
        get() = title.isNotBlank() &&
                selectedDate != null &&
                selectedTime != null &&
                getDateTime()?.isAfter(LocalDateTime.now()) == true

    fun getDateTime(): LocalDateTime? {
        return if (selectedDate != null && selectedTime != null) {
            LocalDateTime.of(selectedDate, selectedTime)
        } else {
            selectedDateTime
        }
    }
}
