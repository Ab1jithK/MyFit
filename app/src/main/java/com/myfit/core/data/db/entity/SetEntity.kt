package com.myfit.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("sessionId"), Index("exerciseId"), Index("recordedAt")]
)
data class SetEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val exerciseId: String,
    val setNumber: Int,
    val recordedAt: Long,         // Unix ms precise — primary conflict resolution key
    val weightKg: Float,
    val reps: Int,
    val rpe: Float?,
    val setType: String = "working",  // working|warmup|dropset|myorep
    val restSeconds: Int?,
    val sourceDevice: String = "phone"
)
