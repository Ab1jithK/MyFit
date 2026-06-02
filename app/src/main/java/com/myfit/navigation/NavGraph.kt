package com.myfit.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.myfit.core.ui.theme.*
import com.myfit.feature.analytics.AnalyticsScreen
import com.myfit.feature.home.HomeScreen
import com.myfit.feature.nutrition.NutritionScreen
import com.myfit.feature.planner.PlannerScreen
import com.myfit.feature.onboarding.OnboardingScreen
import com.myfit.feature.onboarding.OnboardingViewModel
import com.myfit.feature.training.ActiveWorkoutScreen

private data class BottomNavItem(val screen: Screen, val icon: ImageVector, val label: String)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home,      Icons.Default.Home,          "HOME"),
    BottomNavItem(Screen.Analytics, Icons.Default.ShowChart,     "ANALYTICS"),
    BottomNavItem(Screen.Nutrition, Icons.Default.Restaurant,    "NUTRITION"),
    BottomNavItem(Screen.Planner,   Icons.Default.CalendarMonth, "PLANNER")
)

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val showBottomBar = currentRoute in setOf(
        Screen.Home.route, Screen.Analytics.route, Screen.Nutrition.route, Screen.Planner.route
    )

    Scaffold(
        containerColor = Black,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Surface1, tonalElevation = 0.dp) {
                    val currentDest = currentBackStack?.destination
                    bottomNavItems.forEach { item ->
                        val selected = currentDest?.hierarchy
                            ?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    tint = if (selected) Active else TextSecondary
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) Active else TextSecondary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Active.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Onboarding.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                val vm: OnboardingViewModel = hiltViewModel()
                val completed by vm.onboardingCompleted.collectAsState()
                LaunchedEffect(completed) {
                    if (completed) navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
                if (!completed) OnboardingScreen(onComplete = { vm.markOnboardingComplete() })
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    navController  = navController,
                    onStartWorkout = { sessionId ->
                        navController.navigate(Screen.Training.withSession(sessionId))
                    }
                )
            }

            composable(Screen.Analytics.route) {
                AnalyticsScreen()
            }

            composable(Screen.Nutrition.route) {
                NutritionScreen()
            }

            composable(Screen.Planner.route) {
                PlannerScreen()
            }

            composable(Screen.Training.route) {
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
}
