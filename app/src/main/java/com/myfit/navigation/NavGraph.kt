package com.myfit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.myfit.feature.home.HomeScreen
import com.myfit.feature.onboarding.OnboardingScreen
import com.myfit.feature.onboarding.OnboardingViewModel
import com.myfit.feature.training.ActiveWorkoutScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Onboarding.route) {

        composable(Screen.Onboarding.route) {
            val vm: OnboardingViewModel = hiltViewModel()
            val completed by vm.onboardingCompleted.collectAsState()
            LaunchedEffect(completed) {
                if (completed) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            }
            if (!completed) {
                OnboardingScreen(onComplete = { vm.markOnboardingComplete() })
            }
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                onStartWorkout = { sessionId ->
                    navController.navigate(Screen.Training.withSession(sessionId))
                }
            )
        }

        composable(Screen.Training.route) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId")
                ?: return@composable
            ActiveWorkoutScreen(
                onFinish = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Training.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
