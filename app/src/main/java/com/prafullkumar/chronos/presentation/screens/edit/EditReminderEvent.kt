package com.prafullkumar.chronos.presentation.screens.edit

sealed interface EditReminderEvent {
    data object NavigateToHome : EditReminderEvent
    data object NavigateBack : EditReminderEvent
    data class ShowError(val message: String) : EditReminderEvent
}
