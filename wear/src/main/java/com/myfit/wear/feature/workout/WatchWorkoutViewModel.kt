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

data class WatchWorkoutUiState(
    val sessionId: String,
    val currentExercise: ExerciseEntity? = null,
    val exercises: List<ExerciseEntity> = emptyList(),
    val recentSets: List<SetEntity> = emptyList(),
    val weightInput: String = "",
    val repsInput: String = "",
    val activeInput: NumpadTarget = NumpadTarget.WEIGHT,
    val restSecondsLeft: Int = 0,
    val setCount: Int = 0
)

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
            _state.update { it.copy(exercises = exercises, currentExercise = exercises.firstOrNull()) }
            refreshSets()
        }
    }

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

    private suspend fun refreshSets() {
        val exercise = _state.value.currentExercise ?: return
        val recent   = workoutDao.getLastSetsForExerciseInSession(exercise.id, sessionId)
        _state.update { it.copy(recentSets = recent) }
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
