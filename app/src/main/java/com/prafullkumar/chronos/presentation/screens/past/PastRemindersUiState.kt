package com.prafullkumar.chronos.presentation.screens.past

import com.prafullkumar.chronos.domain.model.Reminder

data class PastRemindersUiState(
    val reminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)