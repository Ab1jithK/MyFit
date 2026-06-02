package com.myfit.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "nutrition_log",
    foreignKeys = [ForeignKey(
        entity = FoodItemEntity::class,
        parentColumns = ["id"],
        childColumns = ["foodId"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index("loggedAt"), Index("foodId")]
)
data class NutritionLogEntity(
    @PrimaryKey val id: String,
    val foodId: String,
    val loggedAt: Long,    // Unix ms
    val quantityG: Float,
    val mealSlot: String?  // pre|intra|post|other
)
