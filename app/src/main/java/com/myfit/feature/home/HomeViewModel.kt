package com.myfit.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myfit.core.data.db.dao.CnsDao
import com.myfit.core.data.db.dao.ExerciseDao
import com.myfit.core.data.db.dao.SetDao
import com.myfit.core.data.db.dao.WorkoutDao
import com.myfit.core.data.repository.WorkoutRepository
import com.myfit.core.domain.AiBriefingGenerator
import com.myfit.core.domain.CnsEngine
import com.myfit.core.domain.PrehabLibrary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

private const val MS_PER_DAY = 86_400_000L

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val setDao: SetDao,
    private val exerciseDao: ExerciseDao,
    private val cnsDao: CnsDao,
    private val repo: WorkoutRepository
) : ViewModel() {

    private val _navigateToTraining = MutableSharedFlow<String>()
    val navigateToTraining = _navigateToTraining.asSharedFlow()

    val dashboardState = flow {
        val now = System.currentTimeMillis()

        val activeSession = workoutDao.getActiveSession()
        if (activeSession != null) {
            emit(DashboardState.ActiveTraining(activeSession.id, watchConnected = false))
            return@flow
        }

        val cal = Calendar.getInstance()
        val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        val todayTemplates = workoutDao.getTemplatesForDay(dayOfWeek)

        if (todayTemplates.isEmpty()) {
            emit(DashboardState.Idle)
            return@flow
        }

        // Compute CNS snapshot from local set history
        val sets7d  = setDao.getAllSetsSince(now - 7  * MS_PER_DAY)
        val sets28d = setDao.getAllSetsSince(now - 28 * MS_PER_DAY)
        val snapshot = CnsEngine.compute(sets7d, sets28d)
        cnsDao.insertSnapshot(snapshot)

        val bullets = AiBriefingGenerator.generate(snapshot)

        // Prehab: primary muscles from today's template exercises (max 2 moves × 3 muscles)
        val template = todayTemplates.first()
        val templateExercises = exerciseDao.getExercisesForTemplateOnce(template.id)
        val prehabMoves = templateExercises
            .map { it.primaryMuscle }
            .distinct()
            .flatMap { PrehabLibrary.movesFor(it).take(2) }
            .take(6)

        emit(DashboardState.PreWorkout(
            scheduledTemplateName = template.name,
            briefingBullets       = bullets,
            prehabMoves           = prehabMoves
        ))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState.Idle)

    fun startWorkout() {
        viewModelScope.launch {
            val sessionId = repo.createBlankSession()
            _navigateToTraining.emit(sessionId)
        }
    }
}
