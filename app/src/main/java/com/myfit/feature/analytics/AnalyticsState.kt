package com.myfit.feature.analytics

import com.myfit.core.data.db.entity.ExerciseEntity

data class BarData(
    val label: String,
    val value: Float,
    val maxValue: Float
)

data class LinePoint(
    val xMs: Long,
    val xLabel: String,
    val value: Float
)

data class AnalyticsUiState(
    val volumeBars: List<BarData> = emptyList(),
    val e1rmPoints: List<LinePoint> = emptyList(),
    val cnsPoints: List<LinePoint> = emptyList(),
    val availableExercises: List<ExerciseEntity> = emptyList(),
    val selectedExerciseId: String? = null,
    val isLoading: Boolean = true
)
