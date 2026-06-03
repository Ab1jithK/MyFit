package com.myfit.wear.feature.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myfit.wear.data.dao.WearSyncDao
import com.myfit.wear.data.dao.WearWorkoutDao
import com.myfit.wear.data.entity.ExerciseEntity
import com.myfit.wear.data.entity.SetEntity
import com.myfit.wear.data.entity.SyncEventEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val REST_SECONDS = 120

enum class NumpadTarget { WEIGHT, REPS }

data class ExerciseSetGroup(
    val exerciseId: String,
    val exerciseName: String,
    val sets: List<SetEntity>
)

data class WatchWorkoutUiState(
    val sessionId: String,
    val currentExercise: ExerciseEntity? = null,
    val exercises: List<ExerciseEntity> = emptyList(),
    val allExercises: List<ExerciseEntity> = emptyList(),
    val exerciseFilter: String = "ALL",
    val recentSets: List<SetEntity> = emptyList(),
    val sessionGroups: List<ExerciseSetGroup> = emptyList(),   // all sets in session grouped by exercise
    val weightInput: String = "",
    val repsInput: String = "",
    val activeInput: NumpadTarget = NumpadTarget.WEIGHT,
    val restSecondsLeft: Int = 0,
    val setCount: Int = 0,
    val editingSet: SetEntity? = null,
    val editWeightInput: String = "",
    val editRepsInput: String = "",
    val editActiveInput: NumpadTarget = NumpadTarget.WEIGHT
)

val WATCH_FILTERS = listOf("ALL","CHEST","BACK","SHOULDERS","LEGS","ARMS","CORE")

@HiltViewModel
class WatchWorkoutViewModel @Inject constructor(
    private val workoutDao: WearWorkoutDao,
    private val syncDao: WearSyncDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: String = checkNotNull(savedStateHandle["sessionId"])
    private val _state = MutableStateFlow(WatchWorkoutUiState(sessionId = sessionId))
    val state = _state.asStateFlow()
    private var restTimerJob: Job? = null

    init {
        viewModelScope.launch {
            val exercises = workoutDao.getExercises()
            _state.update { it.copy(
                exercises = exercises, allExercises = exercises,
                currentExercise = exercises.firstOrNull()
            ) }
            refreshSets()
        }
    }

    fun setExerciseFilter(category: String) {
        _state.update { s ->
            s.copy(exerciseFilter = category, exercises = applyFilter(s.allExercises, category))
        }
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

    // --- Log edit ---

    fun selectSetForEdit(set: SetEntity) {
        _state.update { it.copy(
            editingSet      = set,
            editWeightInput = formatNum(set.weightKg),
            editRepsInput   = set.reps.toString(),
            editActiveInput = NumpadTarget.WEIGHT
        ) }
    }

    fun dismissEdit() = _state.update { it.copy(editingSet = null) }

    fun setEditActiveInput(target: NumpadTarget) = _state.update { it.copy(editActiveInput = target) }

    fun tapEditNumpad(key: String) {
        _state.update { s ->
            when (s.editActiveInput) {
                NumpadTarget.WEIGHT -> s.copy(editWeightInput = appendNumpad(s.editWeightInput, key))
                NumpadTarget.REPS   -> s.copy(editRepsInput   = appendNumpad(s.editRepsInput,   key))
            }
        }
    }

    fun backspaceEdit() {
        _state.update { s ->
            when (s.editActiveInput) {
                NumpadTarget.WEIGHT -> s.copy(editWeightInput = deleteNumpad(s.editWeightInput))
                NumpadTarget.REPS   -> s.copy(editRepsInput   = deleteNumpad(s.editRepsInput))
            }
        }
    }

    fun confirmEdit() {
        val s      = _state.value
        val set    = s.editingSet ?: return
        val weight = s.editWeightInput.toFloatOrNull() ?: return
        val reps   = s.editRepsInput.toIntOrNull()?.takeIf { it > 0 } ?: return
        viewModelScope.launch {
            workoutDao.updateSet(set.id, weight, reps)
            _state.update { it.copy(editingSet = null) }
            refreshSets()
        }
    }

    fun deleteEditingSet() {
        val set = _state.value.editingSet ?: return
        viewModelScope.launch {
            workoutDao.deleteSet(set.id)
            _state.update { it.copy(editingSet = null) }
            refreshSets()
        }
    }

    private fun formatNum(v: Float): String =
        if (v == v.toLong().toFloat()) v.toLong().toString() else v.toString()

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

    fun setActiveInput(target: NumpadTarget) = _state.update { it.copy(activeInput = target) }

    fun selectExercise(exercise: ExerciseEntity) {
        _state.update { it.copy(currentExercise = exercise) }
        viewModelScope.launch { refreshSets() }
    }

    fun logSet() {
        val s        = _state.value
        val exercise = s.currentExercise ?: return
        val weight   = s.weightInput.toFloatOrNull() ?: return
        val reps     = s.repsInput.toIntOrNull()?.takeIf { it > 0 } ?: return
        val now      = System.currentTimeMillis()
        val set = SetEntity(
            id           = UUID.randomUUID().toString(),
            sessionId    = sessionId,
            exerciseId   = exercise.id,
            setNumber    = s.setCount + 1,
            recordedAt   = now,
            weightKg     = weight,
            reps         = reps,
            rpe          = null,
            setType      = "working",
            restSeconds  = null,
            sourceDevice = "watch"
        )
        viewModelScope.launch {
            workoutDao.insertSet(set)
            syncDao.insertSyncEvent(SyncEventEntity(
                id = UUID.randomUUID().toString(), eventType = "set_recorded",
                entityId = set.id, recordedAt = now, device = "watch",
                syncStatus = "pending", resolvedAt = null
            ))
            refreshSets()
            startRestTimer()
        }
        _state.update { it.copy(weightInput = "", repsInput = "", setCount = it.setCount + 1) }
    }

    fun dismissRestTimer() {
        restTimerJob?.cancel()
        _state.update { it.copy(restSecondsLeft = 0) }
    }

    fun finishWorkout(onDone: () -> Unit) {
        viewModelScope.launch {
            workoutDao.endSession(sessionId, System.currentTimeMillis())
            onDone()
        }
    }

    fun rotateBezel(delta: Float) {
        if (delta == 0f) return
        _state.update { s ->
            when (s.activeInput) {
                NumpadTarget.WEIGHT -> {
                    val step    = if (delta > 0) 2.5f else -2.5f
                    val current = s.weightInput.toFloatOrNull() ?: 0f
                    val newVal  = (current + step).coerceAtLeast(0f)
                    s.copy(weightInput = formatNum(newVal))
                }
                NumpadTarget.REPS -> {
                    val step    = if (delta > 0) 1 else -1
                    val current = s.repsInput.toIntOrNull() ?: 0
                    val newVal  = (current + step).coerceAtLeast(0)
                    s.copy(repsInput = if (newVal == 0) "" else newVal.toString())
                }
            }
        }
    }

    private suspend fun refreshSets() {
        val exercise = _state.value.currentExercise ?: return
        val recent   = workoutDao.getLastSetsForExerciseInSession(exercise.id, sessionId)
        _state.update { it.copy(recentSets = recent) }
        refreshSessionGroups()
    }

    private suspend fun refreshSessionGroups() {
        val allSets   = workoutDao.getAllSetsForSession(sessionId)
        val exercises = _state.value.allExercises
        val groups    = allSets
            .groupBy { it.exerciseId }
            .map { (exId, sets) ->
                val name = exercises.find { it.id == exId }?.name ?: exId
                ExerciseSetGroup(exId, name, sets)
            }
        _state.update { it.copy(sessionGroups = groups) }
    }

    private fun startRestTimer() {
        restTimerJob?.cancel()
        _state.update { it.copy(restSecondsLeft = REST_SECONDS) }
        restTimerJob = viewModelScope.launch {
            for (i in REST_SECONDS downTo 1) {
                _state.update { it.copy(restSecondsLeft = i) }
                delay(1_000)
            }
            _state.update { it.copy(restSecondsLeft = 0) }
        }
    }

    private fun appendNumpad(current: String, key: String): String {
        if (key == "." && current.contains(".")) return current
        if (key == "." && current.isEmpty()) return "0."
        if (current.length >= 6) return current
        return current + key
    }

    private fun deleteNumpad(current: String): String =
        if (current.length <= 1) "" else current.dropLast(1)
}
