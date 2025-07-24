package com.prafullkumar.chronos.data.managers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.prafullkumar.chronos.MainActivity
import com.prafullkumar.chronos.R
import com.prafullkumar.chronos.domain.model.Reminder
import javax.inject.Inject

class ChronosNotificationManager @Inject constructor(
    private val context: Context,
    private val notificationManager: NotificationManager
) {
    companion object {
        private const val CHANNEL_ID = "chronos_reminders"
        private const val CHANNEL_NAME = "Chronos Reminders"
        private const val CHANNEL_DESCRIPTION = "Notifications for scheduled reminders"
    }

    init {
        createNotificationChannel()
    }

    fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableVibration(true)
            enableLights(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showNotification(reminderId: String, title: String, text: String, emoji: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("REMINDER_ID", reminderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Add emoji prefix and format the title
        val notificationTitle = "$emoji $title"
        val notificationText = when {
            !text.isNullOrBlank() -> "📝 $text"
            else -> "🔔 Reminder notification"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(notificationText)
                .setBigContentTitle(notificationTitle))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(reminderId.hashCode(), notification)
    }
}