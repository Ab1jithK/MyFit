package com.myfit.wear.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sessions",
    indices = [Index("startedAt")]
)
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val templateId: String?,
    val startedAt: Long,
    val endedAt: Long?,
    val sourceDevice: String = "watch",
    val notes: String?
)
