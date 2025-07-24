package com.prafullkumar.chronos.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.prafullkumar.chronos.data.preferences.ThemeMode
import com.prafullkumar.chronos.data.preferences.ThemePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemeManager(private val themePreferences: ThemePreferences) {
    val isDarkTheme: Flow<Boolean?> = themePreferences.themeMode.map { themeMode ->
        when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> null
        }
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        themePreferences.setThemeMode(themeMode)
    }
}

@Composable
fun shouldUseDarkTheme(themeManager: ThemeManager): Boolean {
    val themePreference by themeManager.isDarkTheme.collectAsState(initial = null)
    val isSystemInDarkTheme = isSystemInDarkTheme()

    return themePreference ?: isSystemInDarkTheme
}

