package com.prafullkumar.chronos.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.prafullkumar.chronos.data.managers.ChronosNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: ChronosNotificationManager
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra("REMINDER_ID") ?: return
        val reminderTitle = intent.getStringExtra("REMINDER_TITLE") ?: "Reminder"
        val reminderNotes = intent.getStringExtra("REMINDER_NOTES")
        val emoji = intent.getStringExtra("REMINDER_EMOJI") ?: "🔔"
        Log.d("AlarmReceiver", "Received alarm for reminder: $reminderId")
        notificationManager.showNotification(
            reminderId,
            reminderTitle,
            reminderNotes ?: "",
            emoji
        )
    }
}

