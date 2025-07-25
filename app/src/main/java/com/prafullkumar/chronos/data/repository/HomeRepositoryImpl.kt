package com.prafullkumar.chronos.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query.Direction.DESCENDING
import com.prafullkumar.chronos.core.PAST_REMINDERS_CACHE_KEY
import com.prafullkumar.chronos.core.Resource
import com.prafullkumar.chronos.core.UPCOMING_AND_CURRENT_DAY_REMINDERS_CACHE_KEY
import com.prafullkumar.chronos.data.cache.CacheManager
import com.prafullkumar.chronos.data.dtos.ReminderDto
import com.prafullkumar.chronos.data.mappers.ReminderMapper
import com.prafullkumar.chronos.domain.model.Reminder
import com.prafullkumar.chronos.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.time.Duration
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val cacheManager: CacheManager
) : HomeRepository {
    private val userDocRef = firestore.collection("users").document(firebaseAuth.currentUser!!.uid)
    private val reminderMapper = ReminderMapper()

    // Cache keys


    // Cache duration - 5 minutes for upcoming reminders (as they are more time-sensitive)
    private val UPCOMING_CACHE_DURATION = Duration.ofMinutes(5)

    // 15 minutes for past reminders (as they don't change as frequently)
    private val PAST_CACHE_DURATION = Duration.ofMinutes(15)

    private fun getTodayStartTime(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    override fun getUserDisplayName(): String {
        return firebaseAuth.currentUser?.displayName ?: "User"
    }

    override fun upcomingAndCurrentDayReminders(): Flow<Resource<List<Reminder>>> {
        return flow {
            emit(Resource.Loading)

            // Try to get from cache first
            val cachedReminders = cacheManager.get<List<Reminder>>(
                UPCOMING_AND_CURRENT_DAY_REMINDERS_CACHE_KEY
            )
            if (cachedReminders != null) {
                emit(Resource.Success(cachedReminders))
                return@flow
            }

            try {
                val currentTime = System.currentTimeMillis()

                val responses = userDocRef.collection("reminders")
                    .whereGreaterThanOrEqualTo(
                        "dateTime",
                        Timestamp(getTodayStartTime() / 1000, 0)
                    )
                    .orderBy("dateTime")
                    .get()
                    .await()
                val reminders = responses.documents.mapNotNull { doc ->
                    doc.toReminderDto()
                }

                // Filter to include only current and upcoming reminders
                val currentAndUpcomingReminders = reminders.filter { reminder ->
                    reminder.dateTime.toDate().time >= currentTime
                }

                val mappedReminders = reminderMapper.mapListToDomain(currentAndUpcomingReminders)

                cacheManager.put(
                    UPCOMING_AND_CURRENT_DAY_REMINDERS_CACHE_KEY,
                    mappedReminders,
                    UPCOMING_CACHE_DURATION
                )

                emit(Resource.Success(mappedReminders))
            } catch (e: Exception) {
                emit(Resource.Error("Failed to fetch reminders: ${e.message}"))
            }
        }
    }

    override fun getPastReminders(): Flow<Resource<List<Reminder>>> {
        return flow {
            emit(Resource.Loading)

            val cachedReminders = cacheManager.get<List<Reminder>>(PAST_REMINDERS_CACHE_KEY)
            if (cachedReminders != null) {
                emit(Resource.Success(cachedReminders))
                return@flow
            }

            try {
                val responses = userDocRef.collection("reminders")
                    .whereLessThan(
                        "dateTime",
                        Timestamp(getTodayStartTime() / 1000, 0)
                    )
                    .orderBy("dateTime", DESCENDING)
                    .get()
                    .await()
                val reminders = responses.documents.mapNotNull { doc ->
                    doc.toReminderDto()
                }
                val mappedReminders = reminderMapper.mapListToDomain(reminders)

                cacheManager.put(PAST_REMINDERS_CACHE_KEY, mappedReminders, PAST_CACHE_DURATION)

                emit(Resource.Success(mappedReminders))
            } catch (e: Exception) {
                emit(Resource.Error("Failed to fetch past reminders: ${e.message}"))
            }
        }
    }


    override fun invalidateCache() {
        cacheManager.clear(UPCOMING_AND_CURRENT_DAY_REMINDERS_CACHE_KEY)
        cacheManager.clear(PAST_REMINDERS_CACHE_KEY)
    }

    override suspend fun refresh() {
        // Invalidate cache first
        invalidateCache()

        // Then fetch fresh data
        upcomingAndCurrentDayReminders().collect { result ->
            when (result) {
                is Resource.Success -> {
                    // Successfully refreshed, no action needed
                }

                is Resource.Error -> {
                    // Handle error if needed
                }

                is Resource.Loading -> {
                    // Loading state, can be used to show loading indicators
                }
            }
        }
    }
}

fun DocumentSnapshot.toReminderDto(): ReminderDto {
    return ReminderDto(
        uid = getString("uid") ?: "",
        title = getString("title") ?: "",
        description = getString("description") ?: "",
        dateTime = getTimestamp("dateTime") ?: Timestamp.now(),
        emoji = getString("emoji") ?: "",
        type = getString("type") ?: ""
    )
}