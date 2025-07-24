package com.prafullkumar.chronos.di

import android.app.AlarmManager
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.prafullkumar.chronos.data.cache.CacheManager
import com.prafullkumar.chronos.data.managers.ChronosAlarmManager
import com.prafullkumar.chronos.data.preferences.ThemePreferences
import com.prafullkumar.chronos.data.repository.HomeRepositoryImpl
import com.prafullkumar.chronos.data.repository.LoginRepositoryImpl
import com.prafullkumar.chronos.data.repository.ReminderRepositoryImpl
import com.prafullkumar.chronos.domain.repository.HomeRepository
import com.prafullkumar.chronos.domain.repository.LoginRepository
import com.prafullkumar.chronos.domain.repository.ReminderRepository
import com.prafullkumar.chronos.presentation.ui.theme.ThemeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Provides
    @Singleton
    fun provideThemePreferences(@ApplicationContext context: Context): ThemePreferences {
        return ThemePreferences(context)
    }

    @Provides
    @Singleton
    fun provideThemeManager(themePreferences: ThemePreferences): ThemeManager {
        return ThemeManager(themePreferences)
    }

    @Provides
    @Singleton
    fun providesCacheManager(): CacheManager {
        return CacheManager()
    }

    @Provides
    @Singleton
    fun providesFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun providesFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideLoginRepository(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth,
        fireStore: FirebaseFirestore
    ): LoginRepository {
        return LoginRepositoryImpl(
            context = context,
            firestore = fireStore,
            firebaseAuth = firebaseAuth
        )
    }

    @Provides
    @Singleton
    fun providesHomeRepository(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth,
        fireStore: FirebaseFirestore,
        cacheManager: CacheManager
    ): HomeRepository {
        return HomeRepositoryImpl(
            context = context,
            firestore = fireStore,
            firebaseAuth = firebaseAuth,
            cacheManager = cacheManager
        )
    }

    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    @Provides
    @Singleton
    fun provideChronosAlarmManager(
        @ApplicationContext context: Context,
        alarmManager: AlarmManager
    ): ChronosAlarmManager {
        return ChronosAlarmManager(context, alarmManager)
    }

    @Provides
    @Singleton
    fun provideReminderRepository(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth,
        fireStore: FirebaseFirestore,
        alarmManager: ChronosAlarmManager
    ): ReminderRepository {
        return ReminderRepositoryImpl(
            context = context,
            firebaseFirestore = fireStore,
            firebaseAuth = firebaseAuth,
            alarmManager = alarmManager
        )
    }
}