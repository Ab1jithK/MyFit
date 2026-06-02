package com.myfit.feature.home

sealed class DashboardState {
    data class PreWorkout(
        val scheduledTemplateName: String,
        val fatigueScore: Float,
        val velocityTarget: Float?,
        val prehabFocusMuscles: List<String>
    ) : DashboardState()

    data class ActiveTraining(
        val sessionId: String,
        val watchConnected: Boolean
    ) : DashboardState()

    data object Analytical : DashboardState()
    data object Idle : DashboardState()
}
