package com.prafullkumar.chronos.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prafullkumar.chronos.core.Resource
import com.prafullkumar.chronos.domain.model.Reminder
import com.prafullkumar.chronos.domain.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val userName = homeRepository.getUserDisplayName()
        _uiState.update { it.copy(userName = userName) }
        loadReminders()
    }

    fun loadReminders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            homeRepository.upcomingAndCurrentDayReminders().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val grouped = groupReminders(result.data)
                        _uiState.update { currentState ->
                            currentState.copy(
                                groupedReminders = grouped,
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

    /**
     * Groups a list of reminders into "Today" and "Upcoming".
     */
    private fun groupReminders(reminders: List<Reminder>): Map<String, List<Reminder>> {
        val today = LocalDate.now()

        // Grouping logic based on the reminder's date
        val groups = reminders.groupBy {
            val reminderDate = Instant.ofEpochMilli(it.dateTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            when {
                reminderDate.isEqual(today) -> "Today"
                else -> "Upcoming"
            }
        }

        // Ensure the desired order: Today, Upcoming
        return linkedMapOf<String, List<Reminder>>().apply {
            groups["Today"]?.let { put("Today", it) }
            groups["Upcoming"]?.let { put("Upcoming", it) }
        }
    }
}