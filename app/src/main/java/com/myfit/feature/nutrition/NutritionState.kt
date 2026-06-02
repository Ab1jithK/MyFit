package com.myfit.feature.nutrition

import com.myfit.core.data.db.entity.FoodItemEntity
import com.myfit.core.data.db.entity.NutritionLogEntity

data class QuickItem(
    val food: FoodItemEntity,
    val defaultGrams: Float = 100f,
    val logCount: Int = 0
)

data class NutritionLogEntry(
    val log: NutritionLogEntity,
    val food: FoodItemEntity
) {
    val calories: Float get() = food.caloriesPer100 * log.quantityG / 100f
    val proteinG: Float get() = food.proteinPer100  * log.quantityG / 100f
    val carbsG:   Float get() = food.carbsPer100    * log.quantityG / 100f
    val fatG:     Float get() = food.fatPer100      * log.quantityG / 100f
}

data class DailyMacros(val calories: Float, val proteinG: Float, val carbsG: Float, val fatG: Float) {
    companion object {
        fun from(entries: List<NutritionLogEntry>) = DailyMacros(
            calories = entries.sumOf { it.calories.toDouble() }.toFloat(),
            proteinG = entries.sumOf { it.proteinG.toDouble() }.toFloat(),
            carbsG   = entries.sumOf { it.carbsG.toDouble()   }.toFloat(),
            fatG     = entries.sumOf { it.fatG.toDouble()     }.toFloat()
        )
    }
}

data class NutritionUiState(
    val quickItems: List<QuickItem> = emptyList(),
    val searchResults: List<FoodItemEntity> = emptyList(),
    val todayEntries: List<NutritionLogEntry> = emptyList(),
    val dailyMacros: DailyMacros = DailyMacros(0f, 0f, 0f, 0f),
    val pendingItem: QuickItem? = null,
    val isLoading: Boolean = true
)
