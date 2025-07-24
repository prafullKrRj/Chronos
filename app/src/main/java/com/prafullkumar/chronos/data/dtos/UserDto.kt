package com.prafullkumar.chronos.data.dtos

import com.google.firebase.Timestamp

data class UserDto(
    val uid: String,
    val displayName: String,
    val createdAt: Timestamp = Timestamp.now(),
    val lastLogin: Timestamp,
    val photoUrl: String?,
    val numberOfReminders: Int = 0,
)
