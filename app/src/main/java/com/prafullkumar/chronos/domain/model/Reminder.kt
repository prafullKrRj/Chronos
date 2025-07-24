package com.prafullkumar.chronos.domain.model

import com.prafullkumar.chronos.presentation.navigation.Routes

data class Reminder(
    val id: String = "",
    val title: String = "",
    val dateTime: Long = 0L,
    val description: String = "",
    val emoji: String = "",
    val type: String = "general",
) {
    fun toReminderScreen() = Routes.ReminderDetailsScreen(
        id, title, dateTime, description, emoji, type
    )

    fun toEditScreen(): Routes.EditScreen {
        return Routes.EditScreen(
            id, title, dateTime, description, emoji, type
        )
    }
}