package com.myfit.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.myfit.ble.PhoneSyncStatus
import com.myfit.core.ui.theme.*
import com.myfit.feature.home.component.AiBriefingCard
import com.myfit.feature.home.component.PrehabPlayer
import com.myfit.navigation.Screen

@Composable
fun HomeScreen(
    navController: NavController,
    onStartWorkout: (sessionId: String) -> Unit,
    vm: HomeViewModel = hiltViewModel()
) {
    val state          by vm.dashboardState.collectAsStateWithLifecycle()
    val bleStatus      by vm.bleSyncStatus.collectAsStateWithLifecycle()
    val watchConnected by vm.watchConnected.collectAsStateWithLifecycle()
    val lastSyncLabel  by vm.lastSyncLabel.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.navigateToTraining.collect { sessionId -> onStartWorkout(sessionId) }
    }

    Scaffold(containerColor = Black, contentColor = TextPrimary) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ConnectionCard(
                status     = bleStatus,
                lastSync   = lastSyncLabel,
                onConnect  = vm::startScan,
                onOpenLogs = { navController.navigate(Screen.SyncLog.route) }
            )
            Box(modifier = Modifier.weight(1f)) {
                when (val s = state) {
                    DashboardState.Idle              -> IdleView(vm::startWorkout)
                    is DashboardState.PreWorkout     -> PreWorkoutView(s, vm::startWorkout)
                    is DashboardState.ActiveTraining -> ActiveView(s, watchConnected, onStartWorkout)
                    DashboardState.Analytical        -> AnalyticalView()
                }
            }
        }
    }
}

@Composable
private fun ConnectionCard(
    status: PhoneSyncStatus,
    lastSync: String,
    onConnect: () -> Unit,
    onOpenLogs: () -> Unit
) {
    val (dotColor, label) = when (status) {
        PhoneSyncStatus.Done       -> Performance to lastSync
        PhoneSyncStatus.Syncing    -> Active      to "Syncing…"
        PhoneSyncStatus.Connecting -> Caution     to "Connecting…"
        PhoneSyncStatus.Scanning   -> Caution     to "Scanning for watch…"
        PhoneSyncStatus.Error      -> Warning     to "Sync error"
        PhoneSyncStatus.Idle       -> TextDisabled to lastSync
    }
    val showConnect = status == PhoneSyncStatus.Idle || status == PhoneSyncStatus.Error

    Surface(onClick = onOpenLogs, color = Surface1, tonalElevation = 0.dp) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(Modifier.size(8.dp).background(dotColor, CircleShape))
                Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.weight(1f))
                if (showConnect) {
                    TextButton(
                        onClick        = onConnect,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("CONNECT", style = MaterialTheme.typography.labelSmall, color = Active)
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("View sync logs", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
                Text("→", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
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
private fun ActiveView(
    state: DashboardState.ActiveTraining,
    watchConnected: Boolean,
    onResumeWorkout: (String) -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("SESSION ACTIVE", style = MaterialTheme.typography.displaySmall, color = Active)
            Text(
                if (watchConnected) "WATCH CONNECTED" else "STANDALONE MODE",
                style = MaterialTheme.typography.labelLarge,
                color = if (watchConnected) Performance else TextSecondary
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onResumeWorkout(state.sessionId) },
                modifier = Modifier.fillMaxWidth(0.7f).height(64.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Active)
            ) {
                Text("RESUME WORKOUT", style = MaterialTheme.typography.headlineMedium, color = Black)
            }
        }
    }
}

@Composable
private fun AnalyticalView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("REST DAY — Analytics Phase 4", style = MaterialTheme.typography.bodyMedium, color = TextDisabled)
    }
}
