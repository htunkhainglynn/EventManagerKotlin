package com.example.eventmanager.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

private object Routes {
    const val EVENTS = "events"
    const val EVENT_FORM = "event-form/{eventId}"
    const val EVENT_DETAIL = "event/{eventId}"
    const val SESSION_FORM = "session-form/{eventId}/{sessionId}"

    fun eventForm(id: Long = 0) = "event-form/$id"
    fun eventDetail(id: Long) = "event/$id"
    fun sessionForm(eventId: Long, sessionId: Long = 0) = "session-form/$eventId/$sessionId"
}

@Composable
fun EventManagerApp(viewModel: EventManagerViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.EVENTS) {
        composable(Routes.EVENTS) {
            EventListScreen(
                viewModel = viewModel,
                onAdd = { navController.navigate(Routes.eventForm()) },
                onEvent = { navController.navigate(Routes.eventDetail(it)) }
            )
        }
        composable(
            route = Routes.EVENT_FORM,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) { entry ->
            EventFormScreen(
                viewModel = viewModel,
                eventId = entry.arguments?.getLong("eventId") ?: 0,
                onBack = { navController.popBackStack() },
                onSaved = { id ->
                    navController.navigate(Routes.eventDetail(id)) {
                        popUpTo(Routes.EVENTS)
                    }
                }
            )
        }
        composable(
            route = Routes.EVENT_DETAIL,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) { entry ->
            val eventId = entry.arguments?.getLong("eventId") ?: return@composable
            EventDetailScreen(
                viewModel = viewModel,
                eventId = eventId,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.eventForm(eventId)) },
                onDeleted = { navController.popBackStack(Routes.EVENTS, false) },
                onAddSession = { navController.navigate(Routes.sessionForm(eventId)) },
                onEditSession = { navController.navigate(Routes.sessionForm(eventId, it)) }
            )
        }
        composable(
            route = Routes.SESSION_FORM,
            arguments = listOf(
                navArgument("eventId") { type = NavType.LongType },
                navArgument("sessionId") { type = NavType.LongType }
            )
        ) { entry ->
            SessionFormScreen(
                viewModel = viewModel,
                eventId = entry.arguments?.getLong("eventId") ?: 0,
                sessionId = entry.arguments?.getLong("sessionId") ?: 0,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
    }
}
