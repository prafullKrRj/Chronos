package com.prafullkumar.chronos.domain.repository

import com.prafullkumar.chronos.core.Resource
import com.prafullkumar.chronos.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getUserDisplayName(): String


    /**
     * Fetch past reminders (before today)
     */
    fun getPastReminders(): Flow<Resource<List<Reminder>>>

    /**
     * Start real-time listener for upcoming and current day reminders
     */
    fun startUpcomingRemindersListener(): Flow<Resource<List<Reminder>>>

    /**
     * Stop all real-time listeners
     */
    fun stopAllListeners()

    /**
     * Invalidate the reminders cache
     * This should be called when reminders are modified
     */
    fun invalidateCache()
}