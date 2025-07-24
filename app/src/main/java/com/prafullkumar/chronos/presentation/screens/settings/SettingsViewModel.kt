package com.prafullkumar.chronos.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.prafullkumar.chronos.core.Resource
import com.prafullkumar.chronos.data.preferences.ThemeMode
import com.prafullkumar.chronos.domain.repository.HomeRepository
import com.prafullkumar.chronos.domain.repository.ReminderRepository
import com.prafullkumar.chronos.presentation.ui.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingsEvent {
    data object SignOut : SettingsEvent()
}

data class UserData(
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null
)

data class SettingsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val userData: UserData = UserData(),
    val currentTheme: ThemeMode = ThemeMode.SYSTEM
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val homeRepository: HomeRepository,
    private val firebaseAuth: FirebaseAuth,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _settingsEvent = MutableSharedFlow<SettingsEvent>()
    val settingsEvent = _settingsEvent.asSharedFlow()

    val themeMode: StateFlow<ThemeMode> = themeManager.isDarkTheme
        .map { isDark ->
            when (isDark) {
                true -> ThemeMode.DARK
                false -> ThemeMode.LIGHT
                null -> ThemeMode.SYSTEM
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    init {
        loadUserData()
        observeThemeMode()
    }

    private fun observeThemeMode() {
        viewModelScope.launch {
            themeMode.collect { mode ->
                _uiState.update { it.copy(currentTheme = mode) }
            }
        }
    }

    private fun loadUserData() {
        val user = firebaseAuth.currentUser
        _uiState.update {
            it.copy(
                userData = UserData(
                    displayName = user?.displayName,
                    email = user?.email,
                    photoUrl = user?.photoUrl?.toString()
                )
            )
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            themeManager.setThemeMode(themeMode)
        }
    }

    fun deleteAllReminders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }

            reminderRepository.deleteAllReminders().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "All reminders deleted"
                            )
                        }
                        homeRepository.invalidateCache()
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to delete reminders: ${result.message}"
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

    fun deleteOldReminders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }

            reminderRepository.deleteOldReminders().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Old reminders deleted"
                            )
                        }
                        homeRepository.invalidateCache()
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to delete old reminders: ${result.message}"
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

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            homeRepository.invalidateCache()

            firebaseAuth.signOut()

            _settingsEvent.emit(SettingsEvent.SignOut)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(successMessage = null, error = null) }
    }
}
