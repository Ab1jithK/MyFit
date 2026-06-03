package com.myfit.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan_overrides")
data class PlanOverrideEntity(
    @PrimaryKey val date: String,   // ISO: "2026-06-03"
    val isRestDay: Boolean = false,
    val note: String? = null
)
