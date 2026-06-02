package com.myfit.core.domain

import com.myfit.core.data.db.entity.SetEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class CnsEngineTest {

    private fun makeSet(
        weightKg: Float,
        reps: Int,
        daysAgo: Int,
        exerciseId: String = "ex_squat"
    ): SetEntity {
        val now = System.currentTimeMillis()
        return SetEntity(
            id = UUID.randomUUID().toString(),
            sessionId = "s1",
            exerciseId = exerciseId,
            setNumber = 1,
            recordedAt = now - daysAgo * 86_400_000L,
            weightKg = weightKg,
            reps = reps,
            rpe = null,
            setType = "working",
            restSeconds = null,
            sourceDevice = "phone"
        )
    }

    @Test
    fun freshUser_zeroHistory_returnsZeroFatigue() {
        val result = CnsEngine.compute(emptyList(), emptyList())
        assertEquals(0f, result.fatigueScore, 0.01f)
    }

    @Test
    fun moderateLoad_returnsScoreBetween30And65Percent() {
        val history28d = (0 until 28).flatMap { day ->
            listOf(makeSet(100f, 10, day), makeSet(80f, 8, day))
        }
        val recent7d = (0 until 7).flatMap { day ->
            listOf(makeSet(100f, 10, day), makeSet(80f, 8, day))
        }
        val result = CnsEngine.compute(recent7d, history28d)
        assertTrue(
            "Expected score 0.3–0.65, got ${result.fatigueScore}",
            result.fatigueScore in 0.3f..0.65f
        )
    }

    @Test
    fun extremeOverload_returnsHighFatigue() {
        val history28d = (0 until 28).map { day -> makeSet(100f, 5, day) }
        val recent7d = (0 until 7).flatMap { day ->
            listOf(
                makeSet(150f, 12, day),
                makeSet(150f, 12, day),
                makeSet(150f, 12, day)
            )
        }
        val result = CnsEngine.compute(recent7d, history28d)
        assertTrue(
            "Expected score > 0.7, got ${result.fatigueScore}",
            result.fatigueScore > 0.7f
        )
    }

    @Test
    fun velocityBaseline_computedPerExercise() {
        val sets = listOf(
            makeSet(100f, 5, 1, "ex_squat"),
            makeSet(100f, 5, 2, "ex_squat"),
            makeSet(60f, 8, 1, "ex_bench")
        )
        val baseline = CnsEngine.velocityBaseline(sets)
        assertTrue(baseline.containsKey("ex_squat"))
        assertTrue(baseline.containsKey("ex_bench"))
    }

    @Test
    fun totalVolume_computedCorrectly() {
        val sets = listOf(makeSet(100f, 10, 0), makeSet(80f, 5, 0))
        // 100×10 + 80×5 = 1400
        assertEquals(1400f, CnsEngine.totalVolume(sets), 0.1f)
    }
}
