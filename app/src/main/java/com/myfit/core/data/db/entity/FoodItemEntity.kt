package com.myfit.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val caloriesPer100: Float,
    val proteinPer100: Float,
    val carbsPer100: Float,
    val fatPer100: Float,
    val isEmbedded: Boolean = true   // true = bundled DB, false = user-added
)
