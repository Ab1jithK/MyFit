package com.myfit.feature.training

import com.myfit.core.data.db.entity.ExerciseEntity
import com.myfit.core.data.db.entity.SetEntity

data class ActiveWorkoutUiState(
    val sessionId: String = "",
    val exerciseSlots: List<ExerciseSlot> = emptyList(),
    val currentSlotIndex: Int = 0,
    val weightInput: String = "0",
    val repsInput: String = "0",
    val activeInput: NumpadTarget = NumpadTarget.WEIGHT,
    val restState: RestState = RestState.Idle,
    val showExercisePicker: Boolean = false,
    val exercisePickerQuery: String = "",
    val exerciseSearchResults: List<ExerciseEntity> = emptyList(),
    val isFinishing: Boolean = false
) {
    val currentSlot: ExerciseSlot? get() = exerciseSlots.getOrNull(currentSlotIndex)
    val nextSetNumber: Int get() = (currentSlot?.sets?.size ?: 0) + 1
}

data class ExerciseSlot(
    val exercise: ExerciseEntity,
    val sets: List<LoggedSet> = emptyList()
)

data class LoggedSet(
    val entity: SetEntity,
    val isNew: Boolean = false  // true briefly after logging — drives flash animation
)

sealed class RestState {
    data object Idle : RestState()
    data class Counting(val totalSeconds: Int, val remainingSeconds: Int) : RestState() {
        val progress: Float get() = remainingSeconds.toFloat() / totalSeconds.toFloat()
    }
    data object Elapsed : RestState()
}

enum class NumpadTarget { WEIGHT, REPS }

enum class SetType(val label: String) {
    WORKING("Working"),
    WARMUP("Warm-up"),
    DROPSET("Drop-set"),
    MYOREP("Myo-rep")
}
