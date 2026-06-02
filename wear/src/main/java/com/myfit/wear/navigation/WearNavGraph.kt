package com.myfit.wear.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.myfit.wear.feature.workout.*

@Composable
fun WearNavGraph() {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(navController = navController, startDestination = "home") {

        composable("home") {
            WatchHomeScreen(onStartWorkout = { sessionId ->
                navController.navigate("workout/$sessionId")
            })
        }

        composable(
            route = "workout/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { back ->
            val sessionId = back.arguments!!.getString("sessionId")!!
            WatchActiveWorkoutScreen(
                sessionId      = sessionId,
                onPickExercise = { navController.navigate("picker/$sessionId") },
                onFinish       = { navController.popBackStack("home", false) }
            )
        }

        composable(
            route = "picker/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) {
            WatchExercisePickerScreen(onSelect = { exerciseId ->
                navController.previousBackStackEntry
                    ?.savedStateHandle?.set("selectedExerciseId", exerciseId)
                navController.popBackStack()
            })
        }
    }
}
