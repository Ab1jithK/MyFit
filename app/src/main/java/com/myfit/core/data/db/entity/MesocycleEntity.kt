package com.myfit.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mesocycles")
data class MesocycleEntity(
    @PrimaryKey val id: String,
    val name: String,
    val startDate: Long,      // Unix ms
    val endDate: Long?,
    val weeks: Int,
    val status: String = "active"  // active|complete|draft
)
