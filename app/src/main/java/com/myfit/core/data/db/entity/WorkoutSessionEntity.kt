package com.myfit.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [ForeignKey(
        entity = WorkoutTemplateEntity::class,
        parentColumns = ["id"],
        childColumns = ["templateId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("templateId"), Index("startedAt")]
)
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val templateId: String?,
    val startedAt: Long,           // Unix ms — BLE conflict resolution anchor
    val endedAt: Long?,
    val sourceDevice: String = "phone",  // phone|watch|merged
    val notes: String?
)
