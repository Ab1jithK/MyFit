package com.myfit.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val primaryMuscle: String,
    val secondaryMuscles: String?,   // JSON array stored as text
    val equipment: String?,
    val movementPattern: String?     // push|pull|hinge|squat|carry
)
