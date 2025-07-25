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
import java.time.LocalDateTime
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
        startRealtimeListening()
    }

    private fun startRealtimeListening() {
        viewModelScope.launch {
            _uiState.update { it.copy(isListening = true, isLoading = true, error = null) }

            homeRepository.startUpcomingRemindersListener().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val grouped = groupReminders(result.data)
                        _uiState.update { currentState ->
                            currentState.copy(
                                groupedReminders = grouped,
                                isLoading = false,
                                isRefreshing = false,
                                error = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                isRefreshing = false,
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

    fun loadReminders() {
        // If already listening, don't start another listener
        if (_uiState.value.isListening) {
            return
        }
        startRealtimeListening()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }

            // First invalidate the cache
            homeRepository.invalidateCache()

            // The real-time listener will automatically receive fresh data
            // No need to manually fetch as the listener handles updates
        }
    }

    override fun onCleared() {
        super.onCleared()
        homeRepository.stopAllListeners()
        _uiState.update { it.copy(isListening = false) }
    }

    /**
     * Groups a list of reminders into "Overdue", "Today" and "Upcoming".
     */
    private fun groupReminders(reminders: List<Reminder>): Map<String, List<Reminder>> {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()

        // Grouping logic based on the reminder's date and time
        val groups = reminders.groupBy {
            val reminderDateTime = Instant.ofEpochMilli(it.dateTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
            val reminderDate = reminderDateTime.toLocalDate()

            when {
                reminderDate.isEqual(today) -> {
                    if (reminderDateTime.isAfter(now)) "Today" else "Overdue Today"
                }

                reminderDate.isBefore(today) -> "Overdue"
                else -> "Upcoming"
            }
        }

        // Sort reminders within each group
        val sortedGroups = groups.mapValues { (groupName, reminderList) ->
            when (groupName) {
                "Today", "Upcoming" -> reminderList.sortedBy { it.dateTime }
                "Overdue Today", "Overdue" -> reminderList.sortedByDescending { it.dateTime }
                else -> reminderList
            }
        }

        // Ensure the desired order: Today (upcoming), Overdue Today, Overdue, Upcoming
        return linkedMapOf<String, List<Reminder>>().apply {
            sortedGroups["Today"]?.let { put("Today", it) }
            sortedGroups["Overdue Today"]?.let { put("Overdue Today", it) }
            sortedGroups["Overdue"]?.let { put("Overdue", it) }
            sortedGroups["Upcoming"]?.let { put("Upcoming", it) }
        }
    }
}