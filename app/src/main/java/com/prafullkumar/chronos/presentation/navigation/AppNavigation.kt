package com.prafullkumar.chronos.presentation.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.google.firebase.auth.FirebaseAuth
import com.prafullkumar.chronos.presentation.screens.add.AddReminderScreen
import com.prafullkumar.chronos.presentation.screens.edit.EditReminderScreen
import com.prafullkumar.chronos.presentation.screens.home.HomeScreen
import com.prafullkumar.chronos.presentation.screens.onBoarding.LoginScreen
import com.prafullkumar.chronos.presentation.screens.past.PastRemindersScreen
import com.prafullkumar.chronos.presentation.screens.reminder.ReminderDetailScreen
import com.prafullkumar.chronos.presentation.screens.reminderFromNavigation.ReminderFromNavigationScreen
import com.prafullkumar.chronos.presentation.screens.settings.SettingsScreen

@Composable
fun AppNavigation(intent: Intent) {
    val navController = rememberNavController()
    val startDestination =
        if (FirebaseAuth.getInstance().currentUser == null) Routes.LoginScreen else Routes.HomeScreen

    LaunchedEffect(intent) {
        intent.getStringExtra("REMINDER_ID")?.let {
            navController.navigate(Routes.ReminderFromNavigation(it))
        }
    }
    NavHost(navController = navController, startDestination = startDestination) {
        composable<Routes.LoginScreen> {
            LoginScreen(navController)
        }
        composable<Routes.HomeScreen> {
            HomeScreen(
                onReminderClick = {
                    navController.navigate(it.toReminderScreen())
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SettingsScreen)
                },
                onNavigateToAddReminder = {
                    navController.navigate(Routes.AddScreen)
                },
                onNavigateToPastReminders = {
                    navController.navigate(Routes.PastRemindersScreen)
                }
            )
        }
        composable<Routes.PastRemindersScreen> {
            PastRemindersScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onReminderClick = {
                    navController.navigate(it.toReminderScreen())
                }
            )
        }
        composable<Routes.EditScreen> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.EditScreen>()
            EditReminderScreen(
                reminderId = route.id,
                title = route.title,
                dateTime = route.dateTime,
                notes = route.description,
                emoji = route.emoji,
                type = route.type,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.popBackStack(Routes.HomeScreen, inclusive = false)
                }
            )
        }
        composable<Routes.AddScreen> {
            AddReminderScreen(navController)
        }
        composable<Routes.SettingsScreen> {
            SettingsScreen(navController)
        }
        composable<Routes.ReminderDetailsScreen> {
            ReminderDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEdit = { reminder ->
                    navController.navigate(reminder.toEditScreen())
                }
            )
        }
        composable<Routes.ReminderFromNavigation> {
            ReminderFromNavigationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}