package com.myfit.wear.feature.workout

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myfit.wear.ble.WearBleManager
import com.myfit.wear.data.dao.WearWorkoutDao
import com.myfit.wear.data.entity.WorkoutSessionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class WatchHomeViewModel @Inject constructor(
    private val dao: WearWorkoutDao,
    bleManager: WearBleManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val syncStatus = bleManager.syncStatus

    val lastSyncLabel = bleManager.syncStatus
        .map { readSyncLabel() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), readSyncLabel())

    private fun readSyncLabel(): String {
        val prefs = context.getSharedPreferences("myfit_prefs", Context.MODE_PRIVATE)
        val lastMs = prefs.getLong("last_sync_ms", 0L)
        if (lastMs == 0L) return ""
        val diff = System.currentTimeMillis() - lastMs
        return when {
            diff < 60_000     -> "just now"
            diff < 3_600_000  -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
            diff < 86_400_000 -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
            else              -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
        }
    }

    data class UiState(
        val lastSessionDate: String = "",
        val lastSessionSetCount: Int = 0,
        val activeSessionId: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init { loadState() }

    private fun loadState() {
        viewModelScope.launch {
            val active = dao.getActiveSession()
            if (active != null) {
                _state.update { it.copy(activeSessionId = active.id) }
            }
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
        viewModelScope.launch {
            val active = dao.getActiveSession()
            if (active != null) {
                onReady(active.id)
                return@launch
            }
            val sessionId = UUID.randomUUID().toString()
            dao.insertSession(WorkoutSessionEntity(
                id           = sessionId,
                templateId   = null,
                startedAt    = System.currentTimeMillis(),
                endedAt      = null,
                sourceDevice = "watch",
                notes        = null
            ))
            _state.update { it.copy(activeSessionId = sessionId) }
            onReady(sessionId)
        }
    }
}
