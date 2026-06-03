package com.myfit.feature.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myfit.core.ui.theme.*
import com.myfit.feature.training.component.*

@Composable
fun ActiveWorkoutScreen(
    onFinish: () -> Unit,
    vm: ActiveWorkoutViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    if (state.showExercisePicker) {
        ExercisePickerSheet(
            query          = state.exercisePickerQuery,
            results        = state.exerciseSearchResults,
            selectedFilter = state.exercisePickerFilter,
            onQueryChange  = vm::searchExercises,
            onFilterChange = vm::setExerciseFilter,
            onSelect       = vm::selectExercise,
            onDismiss      = vm::dismissExercisePicker
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
    ) {
        WorkoutTopBar(
            onAddExercise = vm::showExercisePicker,
            onFinish = { vm.finishWorkout(onFinish) }
        )

        RestTimerBar(
            restState = state.restState,
            onDismiss = vm::dismissRestTimer
        )

        if (state.exerciseSlots.isNotEmpty()) {
            ExerciseTabRow(
                slots = state.exerciseSlots,
                currentIndex = state.currentSlotIndex,
                onTabSelect = vm::switchToSlot,
                onAddExercise = vm::showExercisePicker
            )
        }

        val currentSlot = state.currentSlot
        if (currentSlot != null) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                item {
                    ExerciseHeader(
                        name = currentSlot.exercise.name,
                        setCount = currentSlot.sets.size,
                        muscle = currentSlot.exercise.primaryMuscle
                    )
                }
                items(currentSlot.sets, key = { it.entity.id }) { loggedSet ->
                    SwipeableSetRow(
                        loggedSet = loggedSet,
                        onComplete = {},
                        onSetTypeChange = {}
                    )
                    HorizontalDivider(color = Border, thickness = 0.5.dp)
                }
                if (currentSlot.sets.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Enter weight + reps, then LOG SET",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextDisabled
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NO EXERCISE SELECTED",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = vm::showExercisePicker,
                        colors = ButtonDefaults.buttonColors(containerColor = Active),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Black)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "ADD EXERCISE",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Black
                        )
                    }
                }
            }
        }

        WorkoutNumpad(
            weightInput = state.weightInput,
            repsInput = state.repsInput,
            activeInput = state.activeInput,
            onKeyTap = vm::tapNumpad,
            onBackspace = vm::backspace,
            onWeightFocus = { vm.setActiveInput(NumpadTarget.WEIGHT) },
            onRepsFocus = { vm.setActiveInput(NumpadTarget.REPS) },
            onLogSet = { vm.logSet() },
            modifier = Modifier.navigationBarsPadding()
        )
    }
}

@Composable
private fun WorkoutTopBar(onAddExercise: () -> Unit, onFinish: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "ACTIVE SESSION",
            style = MaterialTheme.typography.labelLarge,
            color = Active
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onAddExercise) {
                Icon(Icons.Default.Add, contentDescription = "Add exercise", tint = TextPrimary)
            }
            TextButton(onClick = onFinish) {
                Text(
                    text = "FINISH",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Performance
                )
            }
        }
    }
}

@Composable
private fun ExerciseTabRow(
    slots: List<ExerciseSlot>,
    currentIndex: Int,
    onTabSelect: (Int) -> Unit,
    onAddExercise: () -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().background(Surface1).padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(slots) { index, slot ->
            val isSelected = index == currentIndex
            Box(
                modifier = Modifier
                    .background(
                        if (isSelected) Active.copy(alpha = 0.2f) else Surface2,
                        RoundedCornerShape(4.dp)
                    )
                    .clickable { onTabSelect(index) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = slot.exercise.name.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) Active else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        item {
            Box(
                modifier = Modifier
                    .background(Performance.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .clickable(onClick = onAddExercise)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("+ ADD", style = MaterialTheme.typography.labelMedium, color = Performance)
            }
        }
    }
}

@Composable
private fun ExerciseHeader(name: String, setCount: Int, muscle: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "$setCount sets logged",
                style = MaterialTheme.typography.labelLarge,
                color = if (setCount > 0) Performance else TextDisabled
            )
            Text(
                text = muscle.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
        }
    }
}
