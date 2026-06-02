package com.myfit.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myfit.core.data.db.dao.CnsDao
import com.myfit.core.data.db.dao.WorkoutDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val cnsDao: CnsDao
) : ViewModel() {

    val dashboardState = flow {
        val activeSession = workoutDao.getActiveSession()
        if (activeSession != null) {
            emit(DashboardState.ActiveTraining(activeSession.id, watchConnected = false))
            return@flow
        }

        // Calendar.DAY_OF_WEEK: Sun=1 … Sat=7. Convert to 0=Mon … 6=Sun.
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
}
