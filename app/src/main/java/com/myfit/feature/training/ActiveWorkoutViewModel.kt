package com.myfit.feature.training

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myfit.core.data.db.entity.ExerciseEntity
import com.myfit.core.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val REST_SECONDS = 120

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    private val repo: WorkoutRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    private val _state = MutableStateFlow(ActiveWorkoutUiState(sessionId = sessionId))
    val state = _state.asStateFlow()

    private var restTimerJob: Job? = null

    // --- Numpad ---

    fun tapNumpad(key: String) {
        _state.update { s ->
            when (s.activeInput) {
                NumpadTarget.WEIGHT -> s.copy(weightInput = appendNumpad(s.weightInput, key))
                NumpadTarget.REPS   -> s.copy(repsInput   = appendNumpad(s.repsInput,   key))
            }
        }
    }

    fun backspace() {
        _state.update { s ->
            when (s.activeInput) {
                NumpadTarget.WEIGHT -> s.copy(weightInput = deleteNumpad(s.weightInput))
                NumpadTarget.REPS   -> s.copy(repsInput   = deleteNumpad(s.repsInput))
            }
        }
    }

    fun setActiveInput(target: NumpadTarget) {
        _state.update { it.copy(activeInput = target) }
    }

    // --- Set logging ---

    fun logSet(setType: SetType = SetType.WORKING) {
        val s = _state.value
        val slot = s.currentSlot ?: return
        val weightKg = s.weightInput.toFloatOrNull() ?: return
        val reps = s.repsInput.toIntOrNull()?.takeIf { it > 0 } ?: return

        viewModelScope.launch {
            val entity = repo.logSet(
                sessionId = sessionId,
                exerciseId = slot.exercise.id,
                setNumber = s.nextSetNumber,
                weightKg = weightKg,
                reps = reps,
                setType = setType.name.lowercase()
            )
            val newSet = LoggedSet(entity, isNew = true)
            val updatedSlots = _state.value.exerciseSlots.toMutableList()
            val updatedSlot = updatedSlots[_state.value.currentSlotIndex]
            updatedSlots[_state.value.currentSlotIndex] = updatedSlot.copy(
                sets = updatedSlot.sets + newSet
            )
            _state.update { it.copy(exerciseSlots = updatedSlots) }

            // Clear isNew flash after 600ms
            delay(600)
            val clearedSlots = _state.value.exerciseSlots.toMutableList()
            val ci = _state.value.currentSlotIndex
            clearedSlots[ci] = clearedSlots[ci].copy(
                sets = clearedSlots[ci].sets.map { it.copy(isNew = false) }
            )
            _state.update { it.copy(exerciseSlots = clearedSlots) }
        }
        startRestTimer(REST_SECONDS)
    }

    // --- Rest timer ---

    private fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            for (remaining in seconds downTo 0) {
                _state.update { it.copy(restState = RestState.Counting(seconds, remaining)) }
                if (remaining > 0) delay(1000)
            }
            _state.update { it.copy(restState = RestState.Elapsed) }
        }
    }

    fun dismissRestTimer() {
        restTimerJob?.cancel()
        _state.update { it.copy(restState = RestState.Idle) }
    }

    // --- Exercise picker ---

    fun showExercisePicker() {
        viewModelScope.launch {
            val all = repo.searchExercises("")
            _state.update { it.copy(showExercisePicker = true, allExercises = all, exerciseSearchResults = all, exercisePickerFilter = "ALL") }
        }
    }

    fun searchExercises(query: String) {
        _state.update { it.copy(exercisePickerQuery = query) }
        viewModelScope.launch {
            val all  = repo.searchExercises(query)
            val results = applyFilter(all, _state.value.exercisePickerFilter)
            _state.update { it.copy(allExercises = all, exerciseSearchResults = results) }
        }
    }

    fun setExerciseFilter(category: String) {
        val results = applyFilter(_state.value.allExercises, category)
        _state.update { it.copy(exercisePickerFilter = category, exerciseSearchResults = results) }
    }

    private fun applyFilter(exercises: List<ExerciseEntity>, category: String): List<ExerciseEntity> {
        val muscles = when (category) {
            "CHEST"     -> setOf("chest")
            "BACK"      -> setOf("lats", "rear_delt", "traps", "erectors")
            "SHOULDERS" -> setOf("front_delt", "side_delt", "rear_delt")
            "LEGS"      -> setOf("quadriceps", "hamstrings", "glutes", "calves", "adductors")
            "ARMS"      -> setOf("biceps", "triceps", "brachialis")
            "CORE"      -> setOf("core", "obliques")
            else        -> emptySet()
        }
        return if (muscles.isEmpty()) exercises else exercises.filter { it.primaryMuscle in muscles }
    }

    fun dismissExercisePicker() {
        _state.update { it.copy(showExercisePicker = false, exercisePickerQuery = "", exercisePickerFilter = "ALL") }
    }

    fun selectExercise(exercise: ExerciseEntity) {
        _state.update { s ->
            val existing = s.exerciseSlots.indexOfFirst { it.exercise.id == exercise.id }
            if (existing >= 0) {
                s.copy(currentSlotIndex = existing, showExercisePicker = false)
            } else {
                val newSlots = s.exerciseSlots + ExerciseSlot(exercise)
                s.copy(
                    exerciseSlots = newSlots,
                    currentSlotIndex = newSlots.lastIndex,
                    showExercisePicker = false
                )
            }
        }
    }

    fun switchToSlot(index: Int) {
        _state.update { it.copy(currentSlotIndex = index) }
    }

    // --- Finish ---

    fun finishWorkout(onFinished: () -> Unit) {
        viewModelScope.launch {
            repo.endSession(sessionId)
            onFinished()
        }
    }

    // --- Numpad helpers ---

    private fun appendNumpad(current: String, key: String): String {
        if (key == "." && current.contains(".")) return current
        if (key == "." && current.isEmpty()) return "0."
        if (current.length >= 6) return current
        return current + key
    }

    private fun deleteNumpad(current: String): String =
        if (current.length <= 1) "" else current.dropLast(1)
}
