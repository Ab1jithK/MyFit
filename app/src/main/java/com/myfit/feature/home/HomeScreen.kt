package com.myfit.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.myfit.core.ui.theme.*
import com.myfit.feature.home.component.AiBriefingCard
import com.myfit.feature.home.component.PrehabPlayer

@Composable
fun HomeScreen(
    navController: NavController,
    onStartWorkout: (sessionId: String) -> Unit,
    vm: HomeViewModel = hiltViewModel()
) {
    val state by vm.dashboardState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.navigateToTraining.collect { sessionId -> onStartWorkout(sessionId) }
    }

    Scaffold(containerColor = Black, contentColor = TextPrimary) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                DashboardState.Idle              -> IdleView(vm::startWorkout)
                is DashboardState.PreWorkout     -> PreWorkoutView(s, vm::startWorkout)
                is DashboardState.ActiveTraining -> ActiveView(s)
                DashboardState.Analytical        -> AnalyticalView()
            }
        }
    }
}

@Composable
private fun IdleView(onStartWorkout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("NO WORKOUT SCHEDULED", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onStartWorkout,
            modifier = Modifier.fillMaxWidth(0.7f).height(64.dp),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Active)
        ) {
            Text("QUICK START", style = MaterialTheme.typography.headlineMedium, color = Black)
        }
    }
}

@Composable
private fun PreWorkoutView(state: DashboardState.PreWorkout, onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // Top 25% — AI Briefing with real CNS bullets
        AiBriefingCard(
            bullets = state.briefingBullets,
            modifier = Modifier.fillMaxWidth().weight(0.25f)
        )

        Spacer(Modifier.height(8.dp))

        // Middle 50% — Today's workout + CTA
        Surface(color = Surface1, modifier = Modifier.fillMaxWidth().weight(0.50f)) {
            Column(
                Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("TODAY", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Text(
                        state.scheduledTemplateName.uppercase(),
                        style = MaterialTheme.typography.displayMedium
                    )
                }
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Performance)
                ) {
                    Text(
                        "INITIALIZE WORKOUT",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Black
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Bottom 25% — Prehab player with muscle-targeted moves
        PrehabPlayer(
            moves = state.prehabMoves,
            modifier = Modifier.fillMaxWidth().weight(0.25f)
        )
    }
}

@Composable
private fun ActiveView(state: DashboardState.ActiveTraining) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SESSION ACTIVE", style = MaterialTheme.typography.displaySmall, color = Active)
            Spacer(Modifier.height(8.dp))
            Text(
                if (state.watchConnected) "WATCH CONNECTED" else "STANDALONE MODE",
                style = MaterialTheme.typography.labelLarge,
                color = if (state.watchConnected) Performance else TextSecondary
            )
        }
    }
}

@Composable
private fun AnalyticalView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("REST DAY — Analytics Phase 4", style = MaterialTheme.typography.bodyMedium, color = TextDisabled)
    }
}
