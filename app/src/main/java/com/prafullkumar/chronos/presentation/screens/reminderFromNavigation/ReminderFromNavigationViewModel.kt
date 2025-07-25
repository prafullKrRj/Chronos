package com.prafullkumar.chronos.presentation.screens.reminderFromNavigation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prafullkumar.chronos.core.Resource
import com.prafullkumar.chronos.domain.model.Reminder
import com.prafullkumar.chronos.domain.repository.GreetingRepository
import com.prafullkumar.chronos.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ReminderFromNavigationEvent {
    data class ShowError(val message: String) : ReminderFromNavigationEvent
}

data class ReminderFromNavigationUiState(
    val reminder: Reminder? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val customMessage: String = "",
    val isComposingMessage: Boolean = false,
    val greetingPrompt: String = "",
    val isGeneratingGreeting: Boolean = false,
    val isComposingGreeting: Boolean = false
)

@HiltViewModel
class ReminderFromNavigationViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val greetingRepository: GreetingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reminderId: String = savedStateHandle.get<String>("id") ?: ""

    private val _uiState = MutableStateFlow(ReminderFromNavigationUiState())
    val uiState: StateFlow<ReminderFromNavigationUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ReminderFromNavigationEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadReminder()
    }

    private fun loadReminder() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getReminderFromId(reminderId).collectLatest { response ->

                when (response) {
                    is Resource.Error -> {
                        val errorMessage = when {
                            response.message?.contains("network", ignoreCase = true) == true ->
                                "No internet connection. Please check your network and try again."
                            response.message?.contains("not found", ignoreCase = true) == true ->
                                "Reminder not found or may have been deleted."
                            else -> response.message ?: "Failed to load reminder"
                        }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                        _eventFlow.emit(ReminderFromNavigationEvent.ShowError(errorMessage))
                    }

                    Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }

                    is Resource.Success<*> -> {
                        val reminder = response.data as? Reminder
                        if (reminder != null) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                reminder = reminder,
                                error = null
                            )
                        } else {
                            val errorMsg = "Reminder not found or may have been deleted."
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = errorMsg
                            )
                            _eventFlow.emit(ReminderFromNavigationEvent.ShowError(errorMsg))
                        }
                    }
                }
            }
        }
    }

    fun updateCustomMessage(message: String) {
        _uiState.value = _uiState.value.copy(customMessage = message)
    }

    fun updateGreetingPrompt(prompt: String) {
        _uiState.value = _uiState.value.copy(greetingPrompt = prompt)
    }

    fun startComposingMessage() {
        _uiState.value = _uiState.value.copy(
            isComposingMessage = true,
            isComposingGreeting = false,
            customMessage = ""
        )
    }

    fun startComposingGreeting() {
        _uiState.value = _uiState.value.copy(
            isComposingGreeting = true,
            isComposingMessage = false,
            greetingPrompt = ""
        )
    }

    fun cancelComposingMessage() {
        _uiState.value = _uiState.value.copy(
            isComposingMessage = false,
            customMessage = ""
        )
    }

    fun cancelComposingGreeting() {
        _uiState.value = _uiState.value.copy(
            isComposingGreeting = false,
            greetingPrompt = ""
        )
    }

    fun generateGreeting() {
        if (_uiState.value.greetingPrompt.isBlank()) return

        viewModelScope.launch {
            greetingRepository.generateGreeting(_uiState.value.greetingPrompt).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isGeneratingGreeting = true)
                    }
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isGeneratingGreeting = false,
                            customMessage = resource.data,
                            isComposingMessage = true,
                            isComposingGreeting = false
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(isGeneratingGreeting = false)
                        _eventFlow.emit(ReminderFromNavigationEvent.ShowError(
                            resource.message ?: "Failed to generate greeting"
                        ))
                    }
                }
            }
        }
    }

    fun retryLoadReminder() {
        loadReminder()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}