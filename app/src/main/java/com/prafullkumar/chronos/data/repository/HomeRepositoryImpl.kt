package com.prafullkumar.chronos.data.repository

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query.Direction.DESCENDING
import com.prafullkumar.chronos.core.Resource
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
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val cacheManager: CacheManager
) : HomeRepository {
    private val userDocRef = firestore.collection("users").document(firebaseAuth.currentUser!!.uid)
    private val reminderMapper = ReminderMapper()

    // Cache keys
    private val UPCOMING_REMINDERS_CACHE_KEY = "upcoming_reminders_${firebaseAuth.currentUser?.uid}"
    private val PAST_REMINDERS_CACHE_KEY = "past_reminders_${firebaseAuth.currentUser?.uid}"

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
            val cachedReminders = cacheManager.get<List<Reminder>>(UPCOMING_REMINDERS_CACHE_KEY)
            if (cachedReminders != null) {
                emit(Resource.Success(cachedReminders))
                return@flow
            }

            try {
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
                val mappedReminders = reminderMapper.mapListToDomain(reminders)

                cacheManager.put(
                    UPCOMING_REMINDERS_CACHE_KEY,
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
        cacheManager.clear(UPCOMING_REMINDERS_CACHE_KEY)
        cacheManager.clear(PAST_REMINDERS_CACHE_KEY)
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