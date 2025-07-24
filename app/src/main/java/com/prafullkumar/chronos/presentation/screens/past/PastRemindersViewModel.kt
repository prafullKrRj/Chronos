package com.prafullkumar.chronos.presentation.screens.past

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prafullkumar.chronos.core.Resource
import com.prafullkumar.chronos.domain.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PastRemindersViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PastRemindersUiState())
    val uiState: StateFlow<PastRemindersUiState> = _uiState.asStateFlow()

    init {
        loadPastReminders()
    }

    fun loadPastReminders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            homeRepository.getPastReminders().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                reminders = result.data,
                                isLoading = false,
                                error = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update { currentState ->
                            currentState.copy(
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

