package com.prafullkumar.chronos.domain.repository

import com.prafullkumar.chronos.core.Resource
import com.prafullkumar.chronos.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getUserDisplayName(): String

    /**
     * Fetch reminders for today and upcoming days
     */
    fun upcomingAndCurrentDayReminders(): Flow<Resource<List<Reminder>>>

    /**
     * Fetch past reminders (before today)
     */
    fun getPastReminders(): Flow<Resource<List<Reminder>>>

    /**
     * Invalidate the reminders cache
     * This should be called when reminders are modified
     */
    fun invalidateCache()
}