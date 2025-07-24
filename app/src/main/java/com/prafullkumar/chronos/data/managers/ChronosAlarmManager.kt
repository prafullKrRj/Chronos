package com.prafullkumar.chronos.data.managers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.prafullkumar.chronos.data.receiver.AlarmReceiver
import com.prafullkumar.chronos.domain.model.Reminder
import javax.inject.Inject


class ChronosAlarmManager @Inject constructor(
    private val context: Context,
    private val alarmManager: AlarmManager
) {
    companion object {
        private const val TAG = "ChronosAlarmManager"
    }

    fun setAlarm(reminder: Reminder) {
        // Check if we can schedule exact alarms on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "Cannot schedule exact alarms. Permission required.")
                return
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminder.id)
            putExtra("REMINDER_TITLE", reminder.title)
            putExtra("REMINDER_NOTES", reminder.description)
            putExtra("REMINDER_EMOJI", reminder.emoji)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Ensure dateTime is in milliseconds since epoch
        val triggerTime = if (reminder.dateTime < System.currentTimeMillis()) {
            Log.w(TAG, "Reminder time is in the past: ${reminder.dateTime}")
            System.currentTimeMillis() + 5000 // Schedule 5 seconds from now for testing
        } else {
            reminder.dateTime
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d(TAG, "Alarm set for reminder: ${reminder.id} at $triggerTime")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set alarm: ${e.message}")
        }
    }

    fun cancelAlarm(reminderId: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Alarm cancelled for reminder: $reminderId")
    }
}