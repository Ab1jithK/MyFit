package com.myfit.core.domain

import com.myfit.core.data.db.entity.CnsSnapshotEntity
import com.myfit.core.data.db.entity.SetEntity
import java.util.UUID

object CnsEngine {

    /**
     * Computes a CNS fatigue snapshot from set history.
     *
     * @param recent7d  Working sets recorded in the last 7 days.
     * @param history28d Working sets recorded in the last 28 days (includes recent7d).
     */
    fun compute(recent7d: List<SetEntity>, history28d: List<SetEntity>): CnsSnapshotEntity {
        val vol7d  = totalVolume(recent7d)
        val vol28d = totalVolume(history28d)

        // Weekly average from 28-day window; 0 on fresh install
        val weeklyAvg = if (vol28d == 0f) 0f else vol28d / 4f

        val ratio = if (weeklyAvg == 0f) 0f else (vol7d / weeklyAvg).coerceIn(0f, 2.5f)

        val fatigueScore = when {
            ratio <= 0f   -> 0f
            ratio <= 0.5f -> lerp(0f,    0.15f, ratio / 0.5f)
            ratio <= 0.9f -> lerp(0.15f, 0.35f, (ratio - 0.5f) / 0.4f)
            ratio <= 1.2f -> lerp(0.35f, 0.55f, (ratio - 0.9f) / 0.3f)
            ratio <= 1.5f -> lerp(0.55f, 0.78f, (ratio - 1.2f) / 0.3f)
            else          -> lerp(0.78f, 1.0f,  (ratio - 1.5f) / 1.0f).coerceAtMost(1f)
        }

        val velocityDropPct = velocityDropFromRecent(recent7d)
        // Each >15% velocity drop adds 0.08 to fatigue score
        val velocityPenalty = if (velocityDropPct != null && velocityDropPct > 15f) 0.08f else 0f

        return CnsSnapshotEntity(
            id             = UUID.randomUUID().toString(),
            recordedAt     = System.currentTimeMillis(),
            fatigueScore   = (fatigueScore + velocityPenalty).coerceIn(0f, 1f),
            hrvRmssd       = null,
            velocityDropPct = velocityDropPct,
            totalVolume7d  = vol7d,
            computedAt     = System.currentTimeMillis()
        )
    }

    /** kg × reps summed across all sets. */
    fun totalVolume(sets: List<SetEntity>): Float =
        sets.sumOf { (it.weightKg * it.reps).toDouble() }.toFloat()

    /**
     * Epley 1RM proxy per exercise: weight × (1 + reps/30).
     * Used as a velocity-baseline substitute until watch IMU data is available.
     */
    fun velocityBaseline(sets: List<SetEntity>): Map<String, Float> =
        sets.filter { it.setType == "working" }
            .groupBy { it.exerciseId }
            .mapValues { (_, group) ->
                group.maxOf { it.weightKg * (1f + it.reps / 30f) }
            }

    /**
     * Estimates % drop in bar-speed proxy for the most recent working set
     * vs the 9-set rolling average. Returns null if fewer than 3 sets exist.
     */
    private fun velocityDropFromRecent(sets: List<SetEntity>): Float? {
        val working = sets.filter { it.setType == "working" }
            .sortedByDescending { it.recordedAt }
        if (working.size < 3) return null

        val recentE1rm   = working.first().let { it.weightKg * (1f + it.reps / 30f) }
        val baselineE1rm = working.drop(1).take(9)
            .map { it.weightKg * (1f + it.reps / 30f) }
            .average().toFloat()

        if (baselineE1rm == 0f) return null
        return ((baselineE1rm - recentE1rm) / baselineE1rm * 100f).coerceAtLeast(0f)
    }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t.coerceIn(0f, 1f)
}
