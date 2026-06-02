package com.myfit.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "set_biometrics",
    foreignKeys = [ForeignKey(
        entity = SetEntity::class,
        parentColumns = ["id"],
        childColumns = ["setId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class SetBiometricsEntity(
    @PrimaryKey val setId: String,
    val meanVelocity: Float?,   // m/s bar velocity
    val peakVelocity: Float?,
    val hrPre: Int?,            // bpm before set
    val hrPost: Int?,           // bpm after set
    val hrRecovery60s: Int?,    // bpm at 60s post-set
    val recordedAt: Long
)
