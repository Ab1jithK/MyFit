package com.myfit.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home       : Screen("home")
    data object Analytics  : Screen("analytics")
    data object Training   : Screen("training/{sessionId}") {
        fun withSession(id: String) = "training/$id"
    }
    data object Nutrition  : Screen("nutrition")
    data object Planner    : Screen("planner")
    data object Settings   : Screen("settings")
}
