package com.prafullkumar.chronos.di

import android.app.NotificationManager
import android.content.Context
import com.prafullkumar.chronos.data.managers.ChronosNotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {
    
    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    @Provides
    @Singleton
    fun provideChronosNotificationManager(
        @ApplicationContext context: Context,
        notificationManager: NotificationManager
    ): ChronosNotificationManager {
        return ChronosNotificationManager(context, notificationManager)
    }
}