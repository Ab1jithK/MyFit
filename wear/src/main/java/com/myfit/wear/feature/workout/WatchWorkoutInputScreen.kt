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
private val TextDim     = Color(0xFF555555)

@Composable
fun WatchWorkoutInputScreen(
    inputState: InputState,
    onTapNumpad: (String) -> Unit,
    onBackspace: () -> Unit,
    onSetActiveInput: (NumpadTarget) -> Unit,
    onLogSet: () -> Unit,
    onDismissRest: () -> Unit,
    onPickExercise: () -> Unit,
    onViewLog: () -> Unit
) {
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        state               = listState,
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(horizontal = 10.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            InputExerciseHeader(
                name     = inputState.currentExercise?.name ?: "TAP TO SELECT",
                muscle   = inputState.currentExercise?.primaryMuscle?.uppercase() ?: "",
                setsDone = inputState.recentSets.size,
                onClick  = onPickExercise
            )
        }

        item {
            Text(
                "◯ rotate bezel to adjust",
                color     = TextDim,
                fontSize  = 8.sp,
                textAlign = TextAlign.Center
            )
        }

        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InputCell(
                    label    = "KG",
                    value    = inputState.weightInput.ifEmpty { "—" },
                    selected = inputState.activeInput == NumpadTarget.WEIGHT,
                    onClick  = { onSetActiveInput(NumpadTarget.WEIGHT) },
                    modifier = Modifier.weight(1f)
                )
                InputCell(
                    label    = "REPS",
                    value    = inputState.repsInput.ifEmpty { "—" },
                    selected = inputState.activeInput == NumpadTarget.REPS,
                    onClick  = { onSetActiveInput(NumpadTarget.REPS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        listOf(
            listOf("7", "8", "9"),
            listOf("4", "5", "6"),
            listOf("1", "2", "3"),
            listOf(".", "0", "⌫")
        ).forEach { row ->
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    row.forEach { key ->
                        Button(
                            onClick  = { if (key == "⌫") onBackspace() else onTapNumpad(key) },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors   = ButtonDefaults.buttonColors(backgroundColor = Surface2)
                        ) {
                            Text(
                                key,
                                color      = if (key == "⌫") Warning else TextPrimary,
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign  = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        item {
            if (inputState.restSecondsLeft > 0) {
                Button(
                    onClick  = onDismissRest,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors   = ButtonDefaults.buttonColors(backgroundColor = Active)
                ) {
                    Text(
                        "REST  ${inputState.restSecondsLeft}s  — tap to skip",
                        color      = Black,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center
                    )
                }
            } else {
                Button(
                    onClick  = onLogSet,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors   = ButtonDefaults.buttonColors(backgroundColor = Performance)
                ) {
                    Text("▶  LOG SET", color = Black, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Chip(
                onClick  = onViewLog,
                label    = { Text("VIEW LOG  (${inputState.setCount} sets)", fontSize = 10.sp, color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                colors   = ChipDefaults.chipColors(backgroundColor = Surface1)
            )
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun InputExerciseHeader(name: String, muscle: String, setsDone: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface1, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("NOW LOGGING", color = TextDim, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text(name.uppercase(), color = Performance, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (muscle.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(muscle, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    if (setsDone > 0) {
                        Text("·", color = TextDim, fontSize = 9.sp)
                        Text("$setsDone ${if (setsDone == 1) "set" else "sets"} done", color = Active, fontSize = 9.sp)
                    }
                }
            }
            Text("tap to change exercise", color = TextDim, fontSize = 8.sp)
        }
    }
}

@Composable
private fun InputCell(label: String, value: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier
            .height(58.dp)
            .background(if (selected) Active.copy(alpha = 0.18f) else Surface2, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(label, color = if (selected) Active else TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Text(value, color = if (selected) Active else TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        if (selected) Text("◯ bezel", color = Active.copy(alpha = 0.6f), fontSize = 7.sp)
    }
}
