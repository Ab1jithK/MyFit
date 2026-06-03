package com.myfit.wear.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.*
import com.myfit.wear.ble.SyncStatus

private val Black       = Color(0xFF000000)
private val Surface1    = Color(0xFF111111)
private val Performance = Color(0xFF00FF87)
private val Active      = Color(0xFF00B4FF)
private val Caution     = Color(0xFFFFB800)
private val Warning     = Color(0xFFFF4444)
private val TextMuted   = Color(0xFF888888)

@Composable
fun WatchHomeScreen(
    onStartWorkout: (sessionId: String) -> Unit,
    vm: WatchHomeViewModel = hiltViewModel()
) {
    val state         by vm.state.collectAsStateWithLifecycle()
    val syncStatus    by vm.syncStatus.collectAsStateWithLifecycle()
    val lastSyncLabel by vm.lastSyncLabel.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.background(Black),
        timeText = { TimeText() }
    ) {
        Column(
            modifier            = Modifier.fillMaxSize().background(Black).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ConnectionPill(syncStatus)
            if (lastSyncLabel.isNotEmpty()) {
                Text(
                    "Synced $lastSyncLabel",
                    color     = TextMuted,
                    fontSize  = 9.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(6.dp))

            Text(
                "MYFIT",
                color         = Performance,
                fontSize      = 16.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            Spacer(Modifier.height(10.dp))

            val hasActive = state.activeSessionId != null
            Chip(
                onClick  = { vm.startWorkout(onStartWorkout) },
                label    = {
                    Text(
                        if (hasActive) "RESUME WORKOUT" else "START WORKOUT",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors   = ChipDefaults.chipColors(
                    backgroundColor = if (hasActive) Active else Performance,
                    contentColor    = Black
                )
            )

            if (state.lastSessionDate.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Last: ${state.lastSessionDate} · ${state.lastSessionSetCount} sets",
                    color     = TextMuted,
                    fontSize  = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ConnectionPill(status: SyncStatus) {
    val (dot, label) = when (status) {
        SyncStatus.Idle        -> TextMuted   to "STANDALONE"
        SyncStatus.Advertising -> Caution     to "WAITING"
        SyncStatus.Syncing     -> Active      to "SYNCING"
        SyncStatus.Done        -> Performance to "SYNCED"
        SyncStatus.Error       -> Warning     to "BT OFF"
    }
    Row(
        modifier = Modifier
            .background(Surface1, CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(Modifier.size(6.dp).background(dot, CircleShape))
        Text(label, color = dot, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}
