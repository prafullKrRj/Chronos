package com.prafullkumar.chronos.data.dtos

import com.google.firebase.Timestamp


data class ReminderDto(
    val uid: String = "",
    val title: String,
    val description: String,
    val dateTime: Timestamp,
    val emoji: String = "",
    val type: String,
    val imageUrl: String? = null
)