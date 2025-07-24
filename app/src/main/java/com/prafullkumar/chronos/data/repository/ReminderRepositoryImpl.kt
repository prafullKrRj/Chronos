package com.prafullkumar.chronos.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.prafullkumar.chronos.core.Resource
import com.prafullkumar.chronos.data.managers.ChronosAlarmManager
import com.prafullkumar.chronos.data.mappers.ReminderMapper
import com.prafullkumar.chronos.domain.model.Reminder
import com.prafullkumar.chronos.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    private val context: Context,
    private val alarmManager: ChronosAlarmManager
) : ReminderRepository {
    private val reminderMapper = ReminderMapper()
    private val userReference =
        firebaseFirestore.collection("users").document(firebaseAuth.currentUser?.uid ?: "")

    override fun saveReminder(reminder: Reminder): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading)
            try {
                val docRef = userReference.collection("reminders").document()
                val reminderWithId = reminder.copy(id = docRef.id)
                docRef.set(reminderMapper.mapToData(reminderWithId)).await()
                alarmManager.setAlarm(reminderWithId)
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
                docRef.set(reminderMapper.mapToData(reminder)).await()
                alarmManager.setAlarm(reminder)
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
                userReference.collection("reminders")
                    .whereLessThan("dateTime", Timestamp.now())
                    .get().await().documents.forEach { document ->
                        alarmManager.cancelAlarm(document.id)
                        document.reference.delete().await()
                    }
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
}