package com.prafullkumar.chronos.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Routes {

    @Serializable
    data object SplashScreen : Routes

    @Serializable
    data object LoginScreen : Routes

    @Serializable
    data object HomeScreen : Routes

    @Serializable
    data object AddScreen : Routes

    @Serializable
    data object PastRemindersScreen : Routes

    @Serializable
    data class EditScreen(
        val id: String = "",
        val title: String = "",
        val dateTime: Long = 0L,
        val description: String = "",
        val emoji: String = "",
        val type: String = "general",
    ) : Routes

    @Serializable
    data class ReminderDetailsScreen(
        val id: String = "",
        val title: String = "",
        val dateTime: Long = 0L,
        val description: String = "",
        val emoji: String = "",
        val type: String = "general",
    ) : Routes

    @Serializable
    data class ReminderFromNavigation(
        val id: String
    ) : Routes

    @Serializable
    data object SettingsScreen : Routes
}