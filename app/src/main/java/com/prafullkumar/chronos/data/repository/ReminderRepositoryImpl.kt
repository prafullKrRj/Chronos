package com.prafullkumar.chronos.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.prafullkumar.chronos.core.Resource
import com.prafullkumar.chronos.core.UPCOMING_AND_CURRENT_DAY_REMINDERS_CACHE_KEY
import com.prafullkumar.chronos.data.cache.CacheManager
import com.prafullkumar.chronos.data.managers.ChronosAlarmManager
import com.prafullkumar.chronos.data.mappers.ReminderMapper
import com.prafullkumar.chronos.data.storage.FirebaseStorageUploader
import com.prafullkumar.chronos.domain.model.Reminder
import com.prafullkumar.chronos.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    private val alarmManager: ChronosAlarmManager,
    private val cacheManager: CacheManager,
    private val storageUploader: FirebaseStorageUploader,
    private val firebaseStorage: FirebaseStorage
) : ReminderRepository {
    private val reminderMapper = ReminderMapper()
    private val userReference =
        firebaseFirestore.collection("users").document(firebaseAuth.currentUser?.uid ?: "")


    override fun saveReminder(reminder: Reminder, uri: Uri?): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading)
            try {
                val docRef = userReference.collection("reminders").document()
                var newReminder = reminder.copy(id = docRef.id)
                if (uri != null) {
                    // Upload image if URI is provided
                    val imageUrl = storageUploader.uploadImage(uri, docRef.id)
                    newReminder = newReminder.copy(imageUrl = imageUrl)
                }


                docRef.set(reminderMapper.mapToData(newReminder)).await()
                alarmManager.setAlarm(newReminder)
                updateCacheWithNewReminder(newReminder)
                emit(Resource.Success(Unit))
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Error"))
            }
        }
    }

    override fun updateReminder(reminder: Reminder): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading)
            try {
                Log.d("ReminderRepositoryImpl", "Updating reminder: ${reminder.id}")
                val docRef = userReference.collection("reminders").document(reminder.id)

                // If the reminder has an imageUrl, it means we want to keep/update the image
                // The image upload should have already happened in the ViewModel
                docRef.set(reminderMapper.mapToData(reminder)).await()
                alarmManager.setAlarm(reminder)

                // Update cache with updated reminder
                updateCacheWithUpdatedReminder(reminder)

                emit(Resource.Success(Unit))
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Failed to update reminder"))
            }
        }
    }

    override fun deleteReminder(reminderId: String): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading)
            try {
                alarmManager.cancelAlarm(reminderId)

                userReference.collection("reminders").document(reminderId).delete().await()

                // Remove from cache
                removeReminderFromCache(reminderId)

                emit(Resource.Success(Unit))
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Failed to delete reminder"))
            }
        }
    }

    override fun deleteAllReminders(): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading)
            try {
                userReference.collection("reminders").get().await().documents.forEach { document ->
                    alarmManager.cancelAlarm(document.id)

                    // Delete image from storage if it exists
                    try {
                        val imagePath =
                            "users/${firebaseAuth.currentUser?.uid}/${document.id}_image.jpg"
                        firebaseStorage.reference.child(imagePath).delete().await()
                    } catch (imageDeleteException: Exception) {
                        Log.w(
                            "ReminderRepositoryImpl",
                            "Failed to delete image for reminder ${document.id}: ${imageDeleteException.message}"
                        )
                    }

                    document.reference.delete().await()
                }
                emit(Resource.Success(Unit))
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Failed to delete all reminders"))
            }
        }
    }

    override fun deleteOldReminders(): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading)
            try {
                val oldReminders = userReference.collection("reminders")
                    .whereLessThan("dateTime", Timestamp.now()).get().await()

                oldReminders.documents.forEach { document ->
                        alarmManager.cancelAlarm(document.id)

                    // Delete image from storage if it exists
                    try {
                        val imagePath =
                            "users/${firebaseAuth.currentUser?.uid}/${document.id}_image.jpg"
                        firebaseStorage.reference.child(imagePath).delete().await()
                    } catch (imageDeleteException: Exception) {
                        Log.w(
                            "ReminderRepositoryImpl",
                            "Failed to delete image for reminder ${document.id}: ${imageDeleteException.message}"
                        )
                    }
                        document.reference.delete().await()
                    }

                // Clear cache to force reload
                cacheManager.clear(UPCOMING_AND_CURRENT_DAY_REMINDERS_CACHE_KEY)

                emit(Resource.Success(Unit))
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Failed to delete old reminders"))
            }
        }
    }

    override fun getReminderFromId(reminderId: String): Flow<Resource<Reminder>> {
        return flow {
            emit(Resource.Loading)
            try {
                // Try to get from cache first
                val cachedReminders =
                    cacheManager.get<List<Reminder>>(UPCOMING_AND_CURRENT_DAY_REMINDERS_CACHE_KEY)
                val cachedReminder = cachedReminders?.find { it.id == reminderId }

                if (cachedReminder != null) {
                    emit(Resource.Success(cachedReminder))
                    return@flow
                }

                // If not in cache, fetch from Firebase
                val document =
                    userReference.collection("reminders").document(reminderId).get().await()
                if (document.exists()) {
                    val reminder = document.toReminderDto()
                    emit(Resource.Success(reminderMapper.mapToDomain(reminder)))
                } else {
                    emit(Resource.Error("Reminder not found"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Failed to load reminder"))
            }
        }
    }

    private fun updateCacheWithNewReminder(reminder: Reminder) {
        val cachedReminders =
            cacheManager.get<List<Reminder>>(UPCOMING_AND_CURRENT_DAY_REMINDERS_CACHE_KEY)
                ?.toMutableList()
                ?: mutableListOf()
        cachedReminders.add(reminder)
        cacheManager.put(
            UPCOMING_AND_CURRENT_DAY_REMINDERS_CACHE_KEY,
            cachedReminders,
            Duration.ofMinutes(30)
        )
    }

    private fun updateCacheWithUpdatedReminder(reminder: Reminder) {
        val cachedReminders =
            cacheManager.get<List<Reminder>>(UPCOMING_AND_CURRENT_DAY_REMINDERS_CACHE_KEY)
                ?.toMutableList()
        if (cachedReminders != null) {
            val index = cachedReminders.indexOfFirst { it.id == reminder.id }
            if (index != -1) {
                cachedReminders[index] = reminder
                cacheManager.put(
                    UPCOMING_AND_CURRENT_DAY_REMINDERS_CACHE_KEY,
                    cachedReminders,
                    Duration.ofMinutes(30)
                )
            }
        }
    }

    private fun removeReminderFromCache(reminderId: String) {
        val cachedReminders =
            cacheManager.get<List<Reminder>>(UPCOMING_AND_CURRENT_DAY_REMINDERS_CACHE_KEY)
                ?.toMutableList()
        if (cachedReminders != null) {
            cachedReminders.removeAll { it.id == reminderId }
            cacheManager.put(
                UPCOMING_AND_CURRENT_DAY_REMINDERS_CACHE_KEY,
                cachedReminders,
                Duration.ofMinutes(30)
            )
        }
    }

    fun invalidateCache() {
        cacheManager.clear(UPCOMING_AND_CURRENT_DAY_REMINDERS_CACHE_KEY)
    }

    override suspend fun uploadImage(imageUri: Uri, reminderId: String): String {
        // This will overwrite any existing image with the same reminderId
        return storageUploader.uploadImage(imageUri, reminderId)
    }
}