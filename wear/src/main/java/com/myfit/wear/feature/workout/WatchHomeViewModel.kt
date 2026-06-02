package com.myfit.wear.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myfit.wear.data.dao.WearWorkoutDao
import com.myfit.wear.data.entity.WorkoutSessionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class WatchHomeViewModel @Inject constructor(
    private val dao: WearWorkoutDao
) : ViewModel() {

    data class UiState(
        val lastSessionDate: String = "",
        val lastSessionSetCount: Int = 0
    )

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init { loadLastSession() }

    private fun loadLastSession() {
        viewModelScope.launch {
            val session = dao.getLastSession() ?: return@launch
            val sets = dao.getSetsForSession(session.id)
            val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
            _state.update { it.copy(
                lastSessionDate     = fmt.format(Date(session.startedAt)),
                lastSessionSetCount = sets.size
            ) }
        }
    }

    fun startWorkout(onReady: (String) -> Unit) {
        val sessionId = UUID.randomUUID().toString()
        viewModelScope.launch {
            dao.insertSession(WorkoutSessionEntity(
                id           = sessionId,
                templateId   = null,
                startedAt    = System.currentTimeMillis(),
                endedAt      = null,
                sourceDevice = "watch",
                notes        = null
            ))
            onReady(sessionId)
        }
    }
}
