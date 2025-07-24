package com.prafullkumar.chronos.presentation.screens.reminderFromNavigation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prafullkumar.chronos.core.Resource
import com.prafullkumar.chronos.domain.model.Reminder
import com.prafullkumar.chronos.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderFromNavigationUiState(
    val reminder: Reminder? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val customMessage: String = "",
    val isComposingMessage: Boolean = false
)

@HiltViewModel
class ReminderFromNavigationViewModel @Inject constructor(
    private val repository: ReminderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reminderId: String = savedStateHandle.get<String>("id") ?: ""

    private val _uiState = MutableStateFlow(ReminderFromNavigationUiState())
    val uiState: StateFlow<ReminderFromNavigationUiState> = _uiState.asStateFlow()

    init {
        loadReminder()
    }

    private fun loadReminder() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getReminderFromId(reminderId).collectLatest { response ->

                when (response) {
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message ?: "Failed to load reminder"
                        )
                    }

                    Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }

                    is Resource.Success<*> -> {
                        val reminder = response.data as? Reminder
                        if (reminder != null) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                reminder = reminder
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Reminder not found"
                            )
                        }
                    }
                }
            }
        }
    }

    fun updateCustomMessage(message: String) {
        _uiState.value = _uiState.value.copy(customMessage = message)
    }

    fun startComposingMessage() {
        _uiState.value = _uiState.value.copy(
            isComposingMessage = true,
            customMessage = ""
        )
    }

    fun cancelComposingMessage() {
        _uiState.value = _uiState.value.copy(
            isComposingMessage = false,
            customMessage = ""
        )
    }


    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}