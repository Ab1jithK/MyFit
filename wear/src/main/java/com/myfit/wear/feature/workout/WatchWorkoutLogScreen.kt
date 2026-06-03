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
import com.myfit.wear.data.entity.SetEntity

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
fun WatchWorkoutLogScreen(
    logState: LogState,
    currentExerciseId: String?,
    onSelectSetForEdit: (SetEntity) -> Unit,
    onTapEditNumpad: (String) -> Unit,
    onBackspaceEdit: () -> Unit,
    onSetEditActiveInput: (NumpadTarget) -> Unit,
    onConfirmEdit: () -> Unit,
    onDeleteEditingSet: () -> Unit,
    onDismissEdit: () -> Unit,
    onPickExercise: () -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberScalingLazyListState()

    Box(Modifier.fillMaxSize().background(Black)) {
        ScalingLazyColumn(
            state               = listState,
            modifier            = Modifier.fillMaxSize(),
            contentPadding      = PaddingValues(horizontal = 10.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Text("SESSION LOG", color = TextDim, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            }

            if (logState.sessionGroups.isEmpty()) {
                item {
                    Text("No sets logged yet", color = TextMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            } else {
                logState.sessionGroups.forEach { group ->
                    item(key = "header_${group.exerciseId}") {
                        LogExerciseGroupHeader(
                            name      = group.exerciseName,
                            isCurrent = group.exerciseId == currentExerciseId
                        )
                    }
                    group.sets.forEach { set ->
                        item(key = set.id) {
                            LogSetRow(set, onTap = { onSelectSetForEdit(set) })
                        }
                    }
                }
            }

            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Chip(
                        onClick  = onFinish,
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

            item {
                Chip(
                    onClick  = onBack,
                    label    = { Text("← BACK TO INPUT", fontSize = 10.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ChipDefaults.chipColors(backgroundColor = Surface1)
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }

        logState.editingSet?.let { editing ->
            LogEditSetOverlay(
                setNumber     = editing.setNumber,
                weightInput   = logState.editWeightInput,
                repsInput     = logState.editRepsInput,
                activeInput   = logState.editActiveInput,
                onSelectInput = onSetEditActiveInput,
                onTap         = onTapEditNumpad,
                onBackspace   = onBackspaceEdit,
                onSave        = onConfirmEdit,
                onDelete      = onDeleteEditingSet,
                onDismiss     = onDismissEdit
            )
        }
    }
}

@Composable
private fun LogExerciseGroupHeader(name: String, isCurrent: Boolean) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.size(6.dp).background(if (isCurrent) Performance else TextDim, RoundedCornerShape(3.dp)))
        Text(
            name.uppercase(),
            color         = if (isCurrent) Performance else TextMuted,
            fontSize      = 9.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 1.sp,
            maxLines      = 1,
            overflow      = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LogSetRow(set: SetEntity, onTap: () -> Unit) {
    val weight = if (set.weightKg == set.weightKg.toLong().toFloat())
        "${set.weightKg.toLong()}kg" else "${"%.1f".format(set.weightKg)}kg"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface1, RoundedCornerShape(5.dp))
            .clickable(onClick = onTap)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text("Set ${set.setNumber}", color = TextDim, fontSize = 9.sp)
        Text("$weight × ${set.reps}", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        if (set.sourceDevice == "phone") Text("P", color = Active, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        else Text("✓", color = Performance, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LogEditSetOverlay(
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
                Text("EDIT SET $setNumber", color = Active, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LogInputCell("KG",   weightInput.ifEmpty { "—" }, activeInput == NumpadTarget.WEIGHT, { onSelectInput(NumpadTarget.WEIGHT) }, Modifier.weight(1f))
                    LogInputCell("REPS", repsInput.ifEmpty { "—" },   activeInput == NumpadTarget.REPS,   { onSelectInput(NumpadTarget.REPS) },   Modifier.weight(1f))
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
                ) {
                    Text("SAVE CHANGES", color = Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Chip(onClick = onDelete, label = { Text("DELETE", fontSize = 10.sp, color = Warning) }, modifier = Modifier.weight(1f), colors = ChipDefaults.chipColors(backgroundColor = Surface1))
                    Chip(onClick = onDismiss, label = { Text("CANCEL", fontSize = 10.sp, color = TextMuted) }, modifier = Modifier.weight(1f), colors = ChipDefaults.chipColors(backgroundColor = Surface1))
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun LogInputCell(label: String, value: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
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
    }
}
