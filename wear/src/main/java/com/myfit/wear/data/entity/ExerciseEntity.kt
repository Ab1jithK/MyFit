package com.myfit.wear.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val primaryMuscle: String,
    val secondaryMuscles: String?,
    val equipment: String?,
    val movementPattern: String?
)
