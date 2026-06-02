package com.myfit.core.domain

import com.myfit.core.data.db.entity.CnsSnapshotEntity
import com.myfit.core.domain.model.BulletColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class AiBriefingGeneratorTest {

    private fun snapshot(
        fatigue: Float,
        velocityDrop: Float? = null,
        volume7d: Float = 5000f
    ) = CnsSnapshotEntity(
        id              = UUID.randomUUID().toString(),
        recordedAt      = System.currentTimeMillis(),
        fatigueScore    = fatigue,
        hrvRmssd        = null,
        velocityDropPct = velocityDrop,
        totalVolume7d   = volume7d,
        computedAt      = System.currentTimeMillis()
    )

    @Test
    fun alwaysReturnsTwoBullets() {
        val bullets = AiBriefingGenerator.generate(snapshot(0.4f))
        assertEquals(2, bullets.size)
    }

    @Test
    fun lowFatigue_greenBullet() {
        val bullets = AiBriefingGenerator.generate(snapshot(0.15f))
        assertEquals(BulletColor.GREEN, bullets[0].color)
    }

    @Test
    fun highFatigue_redBullet() {
        val bullets = AiBriefingGenerator.generate(snapshot(0.85f))
        assertEquals(BulletColor.RED, bullets[0].color)
    }

    @Test
    fun velocityDrop_addsRedOrAmberBullet() {
        val bullets = AiBriefingGenerator.generate(snapshot(0.5f, velocityDrop = 18f))
        val velocityBullet = bullets[1]
        assertTrue(
            "Expected RED or AMBER for 18% velocity drop",
            velocityBullet.color == BulletColor.RED || velocityBullet.color == BulletColor.AMBER
        )
        val text = velocityBullet.text.lowercase()
        assertTrue(
            "Expected 'velocity' or 'speed' or 'bar' in: $text",
            "velocity" in text || "speed" in text || "bar" in text
        )
    }

    @Test
    fun noVelocityData_secondBulletIsBlue() {
        val bullets = AiBriefingGenerator.generate(snapshot(0.4f, velocityDrop = null))
        assertEquals(BulletColor.BLUE, bullets[1].color)
    }
}
