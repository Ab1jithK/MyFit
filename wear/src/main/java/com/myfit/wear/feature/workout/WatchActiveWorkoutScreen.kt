package com.myfit.wear.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.*

private val Black       = Color(0xFF000000)
private val Surface1    = Color(0xFF111111)
private val Surface2    = Color(0xFF1A1A1A)
private val Performance = Color(0xFF00FF87)
private val Active      = Color(0xFF00B4FF)
private val TextPrimary = Color(0xFFEEEEEE)
private val TextMuted   = Color(0xFF888888)

@Composable
fun WatchActiveWorkoutScreen(
    sessionId: String,
    onPickExercise: () -> Unit,
    onFinish: () -> Unit,
    vm: WatchWorkoutViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    // Receive exercise selection from picker
    val savedStateHandle = androidx.navigation.NavBackStackEntry::class
    LaunchedEffect(Unit) { /* savedStateHandle observer wired in NavGraph */ }

    Box(
        modifier = Modifier.fillMaxSize().background(Black),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Spacer(Modifier.height(6.dp))

            // Exercise name — tap to change
            Text(
                text      = state.currentExercise?.name?.uppercase() ?: "SELECT EXERCISE",
                color     = TextPrimary,
                fontSize  = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis,
                modifier  = Modifier
                    .fillMaxWidth()
                    .background(Surface1, RoundedCornerShape(4.dp))
                    .clickable(onClick = onPickExercise)
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                textAlign = TextAlign.Center
            )

            // Recent sets
            state.recentSets.forEach { set ->
                Text(
                    "${set.weightKg}kg × ${set.reps}",
                    color    = TextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(2.dp))

            // Input displays
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                InputCell(
                    label    = "KG",
                    value    = state.weightInput.ifEmpty { "—" },
                    selected = state.activeInput == NumpadTarget.WEIGHT,
                    onClick  = { vm.setActiveInput(NumpadTarget.WEIGHT) },
                    modifier = Modifier.weight(1f)
                )
                InputCell(
                    label    = "REPS",
                    value    = state.repsInput.ifEmpty { "—" },
                    selected = state.activeInput == NumpadTarget.REPS,
                    onClick  = { vm.setActiveInput(NumpadTarget.REPS) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Numpad 4×3
            val keys = listOf("7","8","9","4","5","6","1","2","3","0",".","⌫")
            keys.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    row.forEach { key ->
                        Button(
                            onClick  = { if (key == "⌫") vm.backspace() else vm.tapNumpad(key) },
                            modifier = Modifier.weight(1f).height(30.dp),
                            colors   = ButtonDefaults.buttonColors(backgroundColor = Surface2)
                        ) {
                            Text(key, color = TextPrimary, fontSize = 13.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            // LOG SET / rest timer
            if (state.restSecondsLeft > 0) {
                Button(
                    onClick  = vm::dismissRestTimer,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    colors   = ButtonDefaults.buttonColors(backgroundColor = Active)
                ) {
                    Text("REST ${state.restSecondsLeft}s", color = Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick  = vm::logSet,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    colors   = ButtonDefaults.buttonColors(backgroundColor = Performance)
                ) {
                    Text("LOG SET", color = Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Chip(
                onClick   = { vm.finishWorkout(onFinish) },
                label     = { Text("FINISH", fontSize = 10.sp, color = TextMuted) },
                modifier  = Modifier.fillMaxWidth(),
                colors    = ChipDefaults.chipColors(backgroundColor = Surface1)
            )
        }
    }
}

@Composable
private fun InputCell(label: String, value: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(if (selected) Active.copy(alpha = 0.2f) else Surface1, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = TextMuted, fontSize = 8.sp)
        Text(value, color = if (selected) Active else TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}
