package com.myfit.wear.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
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
private val Surface3    = Color(0xFF222222)
private val Performance = Color(0xFF00FF87)
private val Active      = Color(0xFF00B4FF)
private val Warning     = Color(0xFFFF4444)
private val Caution     = Color(0xFFFFB800)
private val TextPrimary = Color(0xFFEEEEEE)
private val TextMuted   = Color(0xFF888888)
private val TextDim     = Color(0xFF555555)

@Composable
fun WatchActiveWorkoutScreen(
    sessionId: String,
    onPickExercise: () -> Unit,
    onFinish: () -> Unit,
    vm: WatchWorkoutViewModel = hiltViewModel()
) {
    val state     by vm.state.collectAsStateWithLifecycle()
    val listState  = rememberScalingLazyListState()
    val focusReq   = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusReq.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .onRotaryScrollEvent { event ->
                vm.rotateBezel(event.verticalScrollPixels)
                true
            }
            .focusRequester(focusReq)
            .focusable()
    ) {
        Scaffold(
            modifier          = Modifier.background(Black),
            timeText          = { TimeText() },
            vignette          = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
            positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
        ) {
            ScalingLazyColumn(
                state               = listState,
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(horizontal = 10.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                // ── Exercise header card ─────────────────────────────────────
                item {
                    ExerciseHeaderCard(
                        name       = state.currentExercise?.name ?: "TAP TO SELECT",
                        muscle     = state.currentExercise?.primaryMuscle?.uppercase() ?: "",
                        setsDone   = state.recentSets.size,
                        onClick    = onPickExercise
                    )
                }

                // ── Session log ──────────────────────────────────────────────
                if (state.sessionGroups.isNotEmpty()) {
                    item {
                        Text(
                            "SESSION LOG",
                            color         = TextDim,
                            fontSize      = 8.sp,
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                    state.sessionGroups.forEach { group ->
                        item {
                            ExerciseGroupHeader(
                                name       = group.exerciseName,
                                isCurrent  = group.exerciseId == state.currentExercise?.id
                            )
                        }
                        group.sets.forEach { set ->
                            item {
                                SessionSetRow(set, onTap = { vm.selectSetForEdit(set) })
                            }
                        }
                    }
                    item { Spacer(Modifier.height(2.dp)) }
                }

                // ── Bezel hint ───────────────────────────────────────────────
                item {
                    Text(
                        "◯ rotate bezel to adjust",
                        color     = TextDim,
                        fontSize  = 8.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // ── Weight + Reps inputs ─────────────────────────────────────
                item {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
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

                // ── Numpad ───────────────────────────────────────────────────
                listOf(
                    listOf("7","8","9"),
                    listOf("4","5","6"),
                    listOf("1","2","3"),
                    listOf(".","0","⌫")
                ).forEach { row ->
                    item {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            row.forEach { key ->
                                Button(
                                    onClick  = { if (key == "⌫") vm.backspace() else vm.tapNumpad(key) },
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

                // ── LOG SET / rest timer ─────────────────────────────────────
                item {
                    if (state.restSecondsLeft > 0) {
                        Button(
                            onClick  = vm::dismissRestTimer,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors   = ButtonDefaults.buttonColors(backgroundColor = Active)
                        ) {
                            Text(
                                "REST  ${state.restSecondsLeft}s  — tap to skip",
                                color      = Black,
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign  = TextAlign.Center
                            )
                        }
                    } else {
                        Button(
                            onClick  = vm::logSet,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors   = ButtonDefaults.buttonColors(backgroundColor = Performance)
                        ) {
                            Text(
                                "▶  LOG SET",
                                color      = Black,
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // ── Finish / back ────────────────────────────────────────────
                item {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Chip(
                            onClick  = { vm.finishWorkout(onFinish) },
                            label    = { Text("FINISH", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            colors   = ChipDefaults.chipColors(backgroundColor = Surface1)
                        )
                        Chip(
                            onClick  = onPickExercise,
                            label    = { Text("+ EXERCISE", fontSize = 10.sp, color = Performance) },
                            modifier = Modifier.weight(1f),
                            colors   = ChipDefaults.chipColors(backgroundColor = Surface1)
                        )
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }

        // ── Edit overlay ─────────────────────────────────────────────────────
        state.editingSet?.let { editing ->
            EditSetOverlay(
                setNumber     = editing.setNumber,
                weightInput   = state.editWeightInput,
                repsInput     = state.editRepsInput,
                activeInput   = state.editActiveInput,
                onSelectInput = vm::setEditActiveInput,
                onTap         = vm::tapEditNumpad,
                onBackspace   = vm::backspaceEdit,
                onSave        = vm::confirmEdit,
                onDelete      = vm::deleteEditingSet,
                onDismiss     = vm::dismissEdit
            )
        }
    }
}

// ── Exercise header card ──────────────────────────────────────────────────────

@Composable
private fun ExerciseHeaderCard(
    name: String,
    muscle: String,
    setsDone: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface1, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            // Current exercise label
            Text(
                "NOW LOGGING",
                color         = TextDim,
                fontSize      = 8.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                name.uppercase(),
                color      = Performance,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            if (muscle.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        muscle,
                        color      = TextMuted,
                        fontSize   = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (setsDone > 0) {
                        Text("·", color = TextDim, fontSize = 9.sp)
                        Text(
                            "$setsDone ${if (setsDone == 1) "set" else "sets"} done",
                            color    = Active,
                            fontSize = 9.sp
                        )
                    }
                }
            }
            Text(
                "tap to change exercise",
                color    = TextDim,
                fontSize = 8.sp
            )
        }
    }
}

// ── Session log helpers ───────────────────────────────────────────────────────

@Composable
private fun ExerciseGroupHeader(name: String, isCurrent: Boolean) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(if (isCurrent) Performance else TextDim, RoundedCornerShape(3.dp))
        )
        Text(
            name.uppercase(),
            color      = if (isCurrent) Performance else TextMuted,
            fontSize   = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SessionSetRow(
    set: com.myfit.wear.data.entity.SetEntity,
    onTap: () -> Unit = {}
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .background(Surface1, RoundedCornerShape(5.dp))
            .clickable(onClick = onTap)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            "Set ${set.setNumber}",
            color    = TextDim,
            fontSize = 9.sp
        )
        Text(
            "${formatSetWeight(set.weightKg)} × ${set.reps}",
            color      = TextPrimary,
            fontSize   = 11.sp,
            fontWeight = FontWeight.Bold
        )
        if (set.sourceDevice == "phone") {
            Text("📱", fontSize = 9.sp)
        } else {
            Text("✓", color = Performance, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatSetWeight(kg: Float): String =
    if (kg == kg.toLong().toFloat()) "${kg.toLong()}kg" else "${"%.1f".format(kg)}kg"

// ── Input cell ────────────────────────────────────────────────────────────────

@Composable
private fun InputCell(
    label: String, value: String, selected: Boolean,
    onClick: () -> Unit, modifier: Modifier
) {
    Column(
        modifier = modifier
            .height(58.dp)
            .background(
                if (selected) Active.copy(alpha = 0.18f) else Surface2,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            label,
            color      = if (selected) Active else TextMuted,
            fontSize   = 8.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            value,
            color      = if (selected) Active else TextPrimary,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold
        )
        if (selected) {
            Text(
                "◯ bezel",
                color    = Active.copy(alpha = 0.6f),
                fontSize = 7.sp
            )
        }
    }
}

// ── Edit set overlay ──────────────────────────────────────────────────────────

@Composable
private fun EditSetOverlay(
    setNumber: Int,
    weightInput: String,
    repsInput: String,
    activeInput: NumpadTarget,
    onSelectInput: (NumpadTarget) -> Unit,
    onTap: (String) -> Unit,
    onBackspace: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    Box(Modifier.fillMaxSize().background(Black)) {
        ScalingLazyColumn(
            state               = listState,
            modifier            = Modifier.fillMaxSize(),
            contentPadding      = PaddingValues(horizontal = 10.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Text(
                    "EDIT SET $setNumber",
                    color         = Active,
                    fontSize      = 13.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    InputCell("KG",   weightInput.ifEmpty { "—" }, activeInput == NumpadTarget.WEIGHT, { onSelectInput(NumpadTarget.WEIGHT) }, Modifier.weight(1f))
                    InputCell("REPS", repsInput.ifEmpty { "—" },   activeInput == NumpadTarget.REPS,   { onSelectInput(NumpadTarget.REPS) },   Modifier.weight(1f))
                }
            }
            listOf(
                listOf("7","8","9"), listOf("4","5","6"),
                listOf("1","2","3"), listOf(".","0","⌫")
            ).forEach { row ->
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        row.forEach { key ->
                            Button(
                                onClick  = { if (key == "⌫") onBackspace() else onTap(key) },
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors   = ButtonDefaults.buttonColors(backgroundColor = Surface2)
                            ) {
                                Text(key, color = if (key == "⌫") Warning else TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
            item {
                Button(
                    onClick  = onSave,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors   = ButtonDefaults.buttonColors(backgroundColor = Performance)
                ) { Text("SAVE CHANGES", color = Black, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Chip(
                        onClick  = onDelete,
                        label    = { Text("DELETE", fontSize = 10.sp, color = Warning) },
                        modifier = Modifier.weight(1f),
                        colors   = ChipDefaults.chipColors(backgroundColor = Surface1)
                    )
                    Chip(
                        onClick  = onDismiss,
                        label    = { Text("CANCEL", fontSize = 10.sp, color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        colors   = ChipDefaults.chipColors(backgroundColor = Surface1)
                    )
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
