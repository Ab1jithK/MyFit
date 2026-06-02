package com.myfit.feature.home

import com.myfit.core.domain.PrehabMove
import com.myfit.core.domain.model.BriefingBullet

sealed class DashboardState {
    data class PreWorkout(
        val scheduledTemplateName: String,
        val briefingBullets: List<BriefingBullet>,
        val prehabMoves: List<PrehabMove>
    ) : DashboardState()

    data class ActiveTraining(
        val sessionId: String,
        val watchConnected: Boolean
    ) : DashboardState()

    data object Analytical : DashboardState()
    data object Idle : DashboardState()
}
