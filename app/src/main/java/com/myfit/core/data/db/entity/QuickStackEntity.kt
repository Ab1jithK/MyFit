package com.myfit.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quick_stacks")
data class QuickStackEntity(
    @PrimaryKey val id: String,
    val name: String?,
    val foodIdsJson: String,  // JSON: [{"foodId":"x","quantityG":150.0}, ...]
    val useCount: Int = 0,
    val lastUsedAt: Long?
)
