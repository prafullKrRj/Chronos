package com.prafullkumar.chronos

import android.app.Application
import com.prafullkumar.chronos.data.managers.ChronosNotificationManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ChronosApplication : Application() {

    @Inject
    lateinit var notificationManager: ChronosNotificationManager
    override fun onCreate() {
        super.onCreate()
        notificationManager.createNotificationChannel()
    }
}
