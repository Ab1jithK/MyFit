package com.myfit.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Training : Screen("training/{sessionId}") {
        fun withSession(sessionId: String) = "training/$sessionId"
    }
    data object Analytics : Screen("analytics")
    data object Nutrition : Screen("nutrition")
    data object Settings : Screen("settings")
}
