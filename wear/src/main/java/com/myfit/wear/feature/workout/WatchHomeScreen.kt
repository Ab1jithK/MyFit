package com.myfit.wear.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

private val Black       = Color(0xFF000000)
private val Performance = Color(0xFF00FF87)
private val TextMuted   = Color(0xFF888888)

@Composable
fun WatchHomeScreen(
    onStartWorkout: (sessionId: String) -> Unit,
    vm: WatchHomeViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxSize().background(Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "MYFIT",
                color         = Performance,
                fontSize      = 14.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 4.sp
            )

            Chip(
                onClick  = { vm.startWorkout(onStartWorkout) },
                label    = { Text("START WORKOUT", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors   = ChipDefaults.chipColors(backgroundColor = Performance)
            )

            if (state.lastSessionDate.isNotEmpty()) {
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
