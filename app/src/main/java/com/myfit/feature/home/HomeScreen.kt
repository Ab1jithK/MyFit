package com.myfit.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.myfit.core.ui.theme.*

@Composable
fun HomeScreen(
    navController: NavController,
    vm: HomeViewModel = hiltViewModel()
) {
    val state by vm.dashboardState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Black,
        contentColor = TextPrimary
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val s = state) {
                DashboardState.Idle -> IdleView()
                is DashboardState.PreWorkout -> PreWorkoutView(s)
                is DashboardState.ActiveTraining -> ActiveView(s)
                DashboardState.Analytical -> AnalyticalView()
            }
        }
    }
}

@Composable
private fun IdleView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "NO WORKOUT SCHEDULED",
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary
        )
    }
}

@Composable
private fun PreWorkoutView(state: DashboardState.PreWorkout) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // AI Briefing — top 25%
        Surface(
            color = Surface1,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.25f)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "AI BRIEFING",
                    style = MaterialTheme.typography.labelLarge,
                    color = Active
                )
                Spacer(Modifier.height(8.dp))
                val fatigueInt = (state.fatigueScore * 100).toInt()
                Text(
                    text = "⬤  CNS Load: $fatigueInt% — ${fatigueLabel(state.fatigueScore)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = fatigueColor(state.fatigueScore)
                )
                Text(
                    text = "⬤  Velocity baseline unavailable — log sets to calibrate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = "⬤  Complete onboarding to enable full AI briefing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Today's workout — middle 50%
        Surface(
            color = Surface1,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.50f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "TODAY",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    Text(
                        text = state.scheduledTemplateName.uppercase(),
                        style = MaterialTheme.typography.displayMedium
                    )
                }
                Button(
                    onClick = { /* Phase 2: start session */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Performance)
                ) {
                    Text(
                        text = "INITIALIZE WORKOUT",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Black
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Prehab player — bottom 25%
        Surface(
            color = Surface1,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.25f)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "PREHAB / MOBILITY",
                    style = MaterialTheme.typography.labelLarge,
                    color = Caution
                )
                Spacer(Modifier.height(8.dp))
                if (state.prehabFocusMuscles.isEmpty()) {
                    Text(
                        text = "Configure target muscles in settings",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    state.prehabFocusMuscles.forEach { muscle ->
                        Text(
                            text = "• ${muscle.uppercase()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Caution
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveView(state: DashboardState.ActiveTraining) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "SESSION ACTIVE",
                style = MaterialTheme.typography.displaySmall,
                color = Active
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (state.watchConnected) "WATCH CONNECTED" else "STANDALONE MODE",
                style = MaterialTheme.typography.labelLarge,
                color = if (state.watchConnected) Performance else TextSecondary
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Active training UI builds in Phase 2",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDisabled
            )
        }
    }
}

@Composable
private fun AnalyticalView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "REST DAY",
                style = MaterialTheme.typography.displaySmall,
                color = TextSecondary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Analytics charts build in Phase 4",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDisabled
            )
        }
    }
}

private fun fatigueLabel(score: Float) = when {
    score < 0.3f -> "recovery optimal"
    score < 0.6f -> "moderate load"
    score < 0.8f -> "elevated — monitor velocity"
    else -> "critical — reduce top-set load"
}

private fun fatigueColor(score: Float) = when {
    score < 0.3f -> Performance
    score < 0.6f -> Caution
    else -> Warning
}
