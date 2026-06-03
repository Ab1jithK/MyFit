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
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*

private val Black       = Color(0xFF000000)
private val Surface1    = Color(0xFF111111)
private val Surface2    = Color(0xFF1A1A1A)
private val Performance = Color(0xFF00FF87)
private val Active      = Color(0xFF00B4FF)
private val Warning     = Color(0xFFFF4444)
private val TextPrimary = Color(0xFFEEEEEE)
private val TextMuted   = Color(0xFF888888)

@Composable
fun WatchActiveWorkoutScreen(
    sessionId: String,
    onPickExercise: () -> Unit,
    onFinish: () -> Unit,
    vm: WatchWorkoutViewModel = hiltViewModel()
) {
    val state    by vm.state.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()

    Scaffold(
        modifier    = Modifier.background(Black),
        timeText    = { TimeText() },
        vignette    = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            state          = listState,
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Exercise chip — tap to change
            item {
                Chip(
                    onClick = onPickExercise,
                    label   = {
                        Text(
                            state.currentExercise?.name?.uppercase() ?: "TAP TO SELECT",
                            fontSize  = 12.sp,
                            maxLines  = 1,
                            overflow  = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    secondaryLabel = {
                        Text("TAP TO CHANGE", fontSize = 9.sp, color = TextMuted)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ChipDefaults.chipColors(
                        backgroundColor    = Surface2,
                        contentColor       = TextPrimary,
                        secondaryContentColor = TextMuted
                    )
                )
            }

            // Recent sets
            if (state.recentSets.isNotEmpty()) {
                item {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        state.recentSets.forEach { set ->
                            Text(
                                "Set ${set.setNumber}: ${set.weightKg}kg × ${set.reps}",
                                color    = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Weight + Reps inputs
            item {
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
            }

            // Numpad rows
            val keys = listOf(
                listOf("7","8","9"),
                listOf("4","5","6"),
                listOf("1","2","3"),
                listOf(".","0","⌫")
            )
            keys.forEach { row ->
                item {
                    Row(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row.forEach { key ->
                            Button(
                                onClick  = { if (key == "⌫") vm.backspace() else vm.tapNumpad(key) },
                                modifier = Modifier.weight(1f).height(40.dp),
                                colors   = ButtonDefaults.buttonColors(backgroundColor = Surface2)
                            ) {
                                Text(
                                    key,
                                    color      = if (key == "⌫") Warning else TextPrimary,
                                    fontSize   = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign  = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // LOG SET / rest timer
            item {
                if (state.restSecondsLeft > 0) {
                    Button(
                        onClick  = vm::dismissRestTimer,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors   = ButtonDefaults.buttonColors(backgroundColor = Active)
                    ) {
                        Text(
                            "REST  ${state.restSecondsLeft}s",
                            color      = Black,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick  = vm::logSet,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors   = ButtonDefaults.buttonColors(backgroundColor = Performance)
                    ) {
                        Text(
                            "LOG SET",
                            color      = Black,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Finish + back hint
            item {
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Chip(
                        onClick  = { vm.finishWorkout(onFinish) },
                        label    = { Text("FINISH", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors   = ChipDefaults.chipColors(backgroundColor = Surface1)
                    )
                    Chip(
                        onClick  = onFinish,
                        label    = { Text("← BACK", fontSize = 11.sp, color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        colors   = ChipDefaults.chipColors(backgroundColor = Surface1)
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun InputCell(
    label: String, value: String, selected: Boolean,
    onClick: () -> Unit, modifier: Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .background(
                if (selected) Active.copy(alpha = 0.25f) else Surface1,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label,
                color    = if (selected) Active else TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                value,
                color      = if (selected) Active else TextPrimary,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
