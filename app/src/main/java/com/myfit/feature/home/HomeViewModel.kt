package com.myfit.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myfit.core.data.db.dao.CnsDao
import com.myfit.core.data.db.dao.WorkoutDao
import com.myfit.core.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val cnsDao: CnsDao,
    private val repo: WorkoutRepository
) : ViewModel() {

    private val _navigateToTraining = MutableSharedFlow<String>()
    val navigateToTraining = _navigateToTraining.asSharedFlow()

    val dashboardState = flow {
        val activeSession = workoutDao.getActiveSession()
        if (activeSession != null) {
            emit(DashboardState.ActiveTraining(activeSession.id, watchConnected = false))
            return@flow
        }
        val cal = Calendar.getInstance()
        val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        val todayTemplates = workoutDao.getTemplatesForDay(dayOfWeek)
        val latestCns = cnsDao.getLatestSnapshot()
        if (todayTemplates.isNotEmpty()) {
            emit(DashboardState.PreWorkout(
                scheduledTemplateName = todayTemplates.first().name,
                fatigueScore = latestCns?.fatigueScore ?: 0f,
                velocityTarget = null,
                prehabFocusMuscles = emptyList()
            ))
        } else {
            emit(DashboardState.Analytical)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState.Idle)

    fun startWorkout() {
        viewModelScope.launch {
            val sessionId = repo.createBlankSession()
            _navigateToTraining.emit(sessionId)
        }
    }
}
