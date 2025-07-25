package com.prafullkumar.chronos.presentation.screens.reminder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prafullkumar.chronos.core.Resource
import com.prafullkumar.chronos.domain.model.Reminder
import com.prafullkumar.chronos.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ReminderDetailEvent {
    data object NavigateBack : ReminderDetailEvent()
}


@HiltViewModel
class ReminderDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderDetailUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ReminderDetailEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadReminderFromSavedState()
    }

    private fun loadReminderFromSavedState() {

        val reminder = Reminder(
            id = savedStateHandle.get<String>("id")!!,
            title = savedStateHandle.get<String>("title") ?: "",
            description = savedStateHandle.get<String>("description") ?: "",
            dateTime = savedStateHandle.get<Long>("dateTime") ?: 0L,
            type = savedStateHandle.get<String>("type") ?: "",
            emoji = savedStateHandle.get<String>("emoji") ?: "",
            imageUrl = savedStateHandle.get<String>("imageUrl") ?: ""
        )

        _uiState.update { it.copy(reminder = reminder, isLoading = false) }
    }

    fun showDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = show) }
    }

    fun deleteReminder() {
        val reminderId = _uiState.value.reminder?.id ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDeleteDialog = false) }
            reminderRepository.deleteReminder(reminderId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _eventFlow.emit(ReminderDetailEvent.NavigateBack)
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }

                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }
}

