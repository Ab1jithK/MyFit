package com.myfit.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myfit.core.data.db.dao.CnsDao
import com.myfit.core.data.db.dao.ExerciseDao
import com.myfit.core.data.db.dao.SetDao
import com.myfit.core.domain.AnalyticsEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MS_PER_DAY = 86_400_000L

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val setDao: SetDao,
    private val exerciseDao: ExerciseDao,
    private val cnsDao: CnsDao
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsUiState())
    val state = _state.asStateFlow()

    init { loadAll() }

    private fun loadAll() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val allExercises = exerciseDao.searchExercises("")
            val sets28d = setDao.getAllSetsSince(now - 28 * MS_PER_DAY)
            val volumeBars = AnalyticsEngine.muscleVolumeBars(sets28d, allExercises)
            val snaps14d = cnsDao.getSnapshotsSince(now - 14 * MS_PER_DAY)
            val cnsPoints = AnalyticsEngine.cnsTimeline(snaps14d)

            val defaultId = sets28d.groupBy { it.exerciseId }
                .maxByOrNull { it.value.size }?.key

            val e1rmPoints = if (defaultId != null) {
                val sets90d = setDao.getAllSetsSince(now - 90 * MS_PER_DAY)
                AnalyticsEngine.e1rmOverTime(sets90d, defaultId)
            } else emptyList()

            _state.update {
                it.copy(
                    volumeBars         = volumeBars,
                    e1rmPoints         = e1rmPoints,
                    cnsPoints          = cnsPoints,
                    availableExercises = allExercises,
                    selectedExerciseId = defaultId,
                    isLoading          = false
                )
            }
        }
    }

    fun selectExercise(exerciseId: String) {
        _state.update { it.copy(selectedExerciseId = exerciseId, isLoading = true) }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val sets90d = setDao.getAllSetsSince(now - 90 * MS_PER_DAY)
            _state.update {
                it.copy(
                    e1rmPoints = AnalyticsEngine.e1rmOverTime(sets90d, exerciseId),
                    isLoading  = false
                )
            }
        }
    }
}
