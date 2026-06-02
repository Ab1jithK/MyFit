package com.myfit.core.domain

import com.myfit.core.data.db.entity.CnsSnapshotEntity
import com.myfit.core.domain.model.BriefingBullet
import com.myfit.core.domain.model.BulletColor

object AiBriefingGenerator {

    /**
     * Generates exactly 2 briefing bullets from a CNS snapshot.
     * Bullet 1: fatigue / CNS load status.
     * Bullet 2: velocity warning (if drop > 10%) or calibration note.
     */
    fun generate(snapshot: CnsSnapshotEntity): List<BriefingBullet> =
        listOf(fatigueBullet(snapshot), velocityBullet(snapshot))

    private fun fatigueBullet(s: CnsSnapshotEntity): BriefingBullet {
        val pct = (s.fatigueScore * 100).toInt()
        return when {
            s.fatigueScore < 0.3f  -> BriefingBullet(
                "CNS Load: $pct% — recovery optimal. Target 90–95% 1RM today.",
                BulletColor.GREEN
            )
            s.fatigueScore < 0.55f -> BriefingBullet(
                "CNS Load: $pct% — moderate accumulation. Work up to top set, gauge bar speed.",
                BulletColor.GREEN
            )
            s.fatigueScore < 0.75f -> BriefingBullet(
                "CNS Load: $pct% — elevated. Cap at 85% 1RM. Extend rest intervals by 30s.",
                BulletColor.AMBER
            )
            else                   -> BriefingBullet(
                "CNS Load: $pct% — critical. Reduce top-set load 5–10%. Prioritise technique over load.",
                BulletColor.RED
            )
        }
    }

    private fun velocityBullet(s: CnsSnapshotEntity): BriefingBullet {
        val drop = s.velocityDropPct
        return when {
            drop != null && drop > 20f -> BriefingBullet(
                "Bar speed down ${drop.toInt()}% vs baseline — neuromuscular fatigue detected. Confirm warm-up velocity before loading.",
                BulletColor.RED
            )
            drop != null && drop > 10f -> BriefingBullet(
                "Bar speed tracking ${drop.toInt()}% below rolling average — monitor first working set closely.",
                BulletColor.AMBER
            )
            drop != null               -> BriefingBullet(
                "Velocity within normal range. Proceed to working sets.",
                BulletColor.GREEN
            )
            else                       -> BriefingBullet(
                "Velocity baseline unavailable — log sets to calibrate local AI model.",
                BulletColor.BLUE
            )
        }
    }
}
