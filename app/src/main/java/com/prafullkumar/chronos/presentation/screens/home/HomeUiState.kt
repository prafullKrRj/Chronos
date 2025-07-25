package com.prafullkumar.chronos.presentation.screens.home

import com.prafullkumar.chronos.domain.model.Reminder

data class HomeUiState(
    val userName: String = "",
    val groupedReminders: Map<String, List<Reminder>> = emptyMap(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isListening: Boolean = false,
    val error: String? = null
)
