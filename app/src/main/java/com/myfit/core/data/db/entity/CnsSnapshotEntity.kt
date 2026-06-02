package com.myfit.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cns_snapshots",
    indices = [Index("recordedAt")]
)
data class CnsSnapshotEntity(
    @PrimaryKey val id: String,
    val recordedAt: Long,
    val fatigueScore: Float,         // 0.0–1.0
    val hrvRmssd: Float?,
    val velocityDropPct: Float?,      // % below historical baseline
    val totalVolume7d: Float?,
    val computedAt: Long
)
