package com.myfit.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_templates",
    foreignKeys = [ForeignKey(
        entity = MesocycleEntity::class,
        parentColumns = ["id"],
        childColumns = ["mesocycleId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("mesocycleId")]
)
data class WorkoutTemplateEntity(
    @PrimaryKey val id: String,
    val mesocycleId: String?,
    val dayOfWeek: Int?,   // 0=Mon … 6=Sun, null = one-off
    val name: String,
    val createdAt: Long
)
