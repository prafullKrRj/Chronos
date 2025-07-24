package com.prafullkumar.chronos.domain.repository

import com.prafullkumar.chronos.core.Resource
import com.prafullkumar.chronos.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun saveReminder(reminder: Reminder): Flow<Resource<Unit>>
    fun updateReminder(reminder: Reminder): Flow<Resource<Unit>>
    fun deleteReminder(reminderId: String): Flow<Resource<Unit>>
    fun deleteAllReminders(): Flow<Resource<Unit>>
    fun deleteOldReminders(): Flow<Resource<Unit>>
    fun getReminderFromId(reminderId: String): Flow<Resource<Reminder>>
}
