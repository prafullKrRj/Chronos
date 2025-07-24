package com.prafullkumar.chronos.presentation.screens.reminder

import com.prafullkumar.chronos.domain.model.Reminder

data class ReminderDetailUiState(
    val reminder: Reminder? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteDialog: Boolean = false
)