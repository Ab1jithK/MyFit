package com.myfit.core.domain

import com.myfit.core.data.db.entity.CnsSnapshotEntity
import com.myfit.core.data.db.entity.ExerciseEntity
import com.myfit.core.data.db.entity.SetEntity
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class AnalyticsEngineTest {

    private fun set(exerciseId: String, weightKg: Float, reps: Int, daysAgo: Int) = SetEntity(
        id = UUID.randomUUID().toString(), sessionId = "s1",
        exerciseId = exerciseId, setNumber = 1,
        recordedAt = System.currentTimeMillis() - daysAgo * 86_400_000L,
        weightKg = weightKg, reps = reps, rpe = null,
        setType = "working", restSeconds = null, sourceDevice = "phone"
    )

    private fun exercise(id: String, muscle: String) = ExerciseEntity(
        id = id, name = id, primaryMuscle = muscle,
        secondaryMuscles = null, equipment = null, movementPattern = null
    )

    private fun snapshot(fatigueScore: Float, daysAgo: Int) = CnsSnapshotEntity(
        id = UUID.randomUUID().toString(),
        recordedAt = System.currentTimeMillis() - daysAgo * 86_400_000L,
        fatigueScore = fatigueScore, hrvRmssd = null,
        velocityDropPct = null, totalVolume7d = 0f,
        computedAt = System.currentTimeMillis()
    )

    @Test
    fun muscleVolume_sumsCorrectly() {
        val sets = listOf(
            set("ex_squat", 100f, 10, 1),
            set("ex_squat", 100f, 10, 2),
            set("ex_bench", 80f,   8, 1)
        )
        val exercises = listOf(exercise("ex_squat", "quadriceps"), exercise("ex_bench", "chest"))
        val bars = AnalyticsEngine.muscleVolumeBars(sets, exercises)
        val quad  = bars.first { it.label == "QUADRICEPS" }
        val chest = bars.first { it.label == "CHEST" }
        assertEquals(2000f, quad.value,  0.1f)
        assertEquals(640f,  chest.value, 0.1f)
    }

    @Test
    fun muscleVolume_maxValueIsLargestBar() {
        val sets = listOf(set("ex_squat", 100f, 10, 1), set("ex_bench", 50f, 5, 1))
        val exercises = listOf(exercise("ex_squat", "quadriceps"), exercise("ex_bench", "chest"))
        val bars = AnalyticsEngine.muscleVolumeBars(sets, exercises)
        val maxValue = bars.maxOf { it.value }
        assertTrue(bars.all { it.maxValue == maxValue })
    }

    @Test
    fun muscleVolume_emptyInput_returnsEmpty() {
        assertTrue(AnalyticsEngine.muscleVolumeBars(emptyList(), emptyList()).isEmpty())
    }

    @Test
    fun e1rmOverTime_appliesEpleyFormula() {
        val sets = listOf(
            set("ex_squat", 110f, 3, 2),
            set("ex_squat", 100f, 5, 1)
        )
        val points = AnalyticsEngine.e1rmOverTime(sets, "ex_squat")
        assertEquals(2, points.size)
        // Epley: 110 × (1 + 3/30) = 121
        val oldest = points.minByOrNull { it.xMs }!!
        assertEquals(121f, oldest.value, 0.5f)
    }

    @Test
    fun e1rmOverTime_wrongExercise_returnsEmpty() {
        val sets = listOf(set("ex_squat", 100f, 5, 1))
        assertTrue(AnalyticsEngine.e1rmOverTime(sets, "ex_bench").isEmpty())
    }

    @Test
    fun cnsTimeline_sortedOldestFirst() {
        val snaps = listOf(snapshot(0.6f, 1), snapshot(0.3f, 2), snapshot(0.4f, 0))
        val points = AnalyticsEngine.cnsTimeline(snaps)
        assertEquals(3, points.size)
        assertEquals(0.3f, points.first().value, 0.01f)
        assertEquals(0.4f, points.last().value,  0.01f)
    }
}
