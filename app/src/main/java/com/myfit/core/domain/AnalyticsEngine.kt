package com.myfit.core.domain

import com.myfit.core.data.db.entity.CnsSnapshotEntity
import com.myfit.core.data.db.entity.ExerciseEntity
import com.myfit.core.data.db.entity.SetEntity
import com.myfit.feature.analytics.BarData
import com.myfit.feature.analytics.LinePoint
import java.text.SimpleDateFormat
import java.util.*

object AnalyticsEngine {

    /** Aggregate volume (kg × reps) per primary muscle, sorted descending. */
    fun muscleVolumeBars(sets: List<SetEntity>, exercises: List<ExerciseEntity>): List<BarData> {
        if (sets.isEmpty() || exercises.isEmpty()) return emptyList()
        val muscleById = exercises.associate { it.id to it.primaryMuscle }
        val volumeByMuscle = sets
            .filter { it.setType == "working" }
            .groupBy { muscleById[it.exerciseId] }
            .filterKeys { !it.isNullOrEmpty() }
            .mapKeys { it.key!! }
            .mapValues { (_, group) ->
                group.sumOf { (it.weightKg * it.reps).toDouble() }.toFloat()
            }
        if (volumeByMuscle.isEmpty()) return emptyList()
        val maxVol = volumeByMuscle.values.max()
        return volumeByMuscle.entries
            .sortedByDescending { it.value }
            .map { (muscle, vol) -> BarData(muscle.uppercase(), vol, maxVol) }
    }

    /** Epley e1RM estimate over time for one exercise. Sorted oldest → newest. */
    fun e1rmOverTime(sets: List<SetEntity>, exerciseId: String): List<LinePoint> {
        val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
        return sets
            .filter { it.exerciseId == exerciseId && it.setType == "working" && it.reps > 0 }
            .sortedBy { it.recordedAt }
            .map { s ->
                LinePoint(
                    xMs    = s.recordedAt,
                    xLabel = fmt.format(Date(s.recordedAt)),
                    value  = s.weightKg * (1f + s.reps / 30f)
                )
            }
    }

    /** CNS fatigue score timeline. Sorted oldest → newest. */
    fun cnsTimeline(snapshots: List<CnsSnapshotEntity>): List<LinePoint> {
        val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
        return snapshots
            .sortedBy { it.recordedAt }
            .map { snap ->
                LinePoint(
                    xMs    = snap.recordedAt,
                    xLabel = fmt.format(Date(snap.recordedAt)),
                    value  = snap.fatigueScore
                )
            }
    }
}
