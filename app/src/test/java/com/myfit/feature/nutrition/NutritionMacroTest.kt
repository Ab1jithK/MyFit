package com.myfit.feature.nutrition

import com.myfit.core.data.db.entity.FoodItemEntity
import com.myfit.core.data.db.entity.NutritionLogEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class NutritionMacroTest {

    private fun food(cal: Float, protein: Float, carbs: Float, fat: Float) = FoodItemEntity(
        id = UUID.randomUUID().toString(), name = "Test",
        caloriesPer100 = cal, proteinPer100 = protein,
        carbsPer100 = carbs, fatPer100 = fat, isEmbedded = true
    )

    private fun log(foodId: String, grams: Float) = NutritionLogEntity(
        id = UUID.randomUUID().toString(), foodId = foodId,
        loggedAt = System.currentTimeMillis(), quantityG = grams, mealSlot = null
    )

    @Test
    fun entry_macros_scaledByGrams() {
        val f = food(cal = 200f, protein = 20f, carbs = 10f, fat = 8f)
        val entry = NutritionLogEntry(log(f.id, 150f), f)
        assertEquals(300f, entry.calories, 0.1f)
        assertEquals(30f,  entry.proteinG, 0.1f)
        assertEquals(15f,  entry.carbsG,   0.1f)
        assertEquals(12f,  entry.fatG,     0.1f)
    }

    @Test
    fun dailyMacros_sumsAllEntries() {
        val f1 = food(100f, 10f, 5f, 4f)
        val f2 = food(200f, 20f, 8f, 6f)
        val entries = listOf(
            NutritionLogEntry(log(f1.id, 100f), f1),
            NutritionLogEntry(log(f2.id, 200f), f2)
        )
        val macros = DailyMacros.from(entries)
        assertEquals(500f, macros.calories, 0.1f)
        assertEquals(50f,  macros.proteinG, 0.1f)
        assertEquals(21f,  macros.carbsG,   0.1f)
        assertEquals(16f,  macros.fatG,     0.1f)
    }

    @Test
    fun dailyMacros_emptyList_returnsZero() {
        val macros = DailyMacros.from(emptyList())
        assertEquals(0f, macros.calories, 0f)
        assertEquals(0f, macros.proteinG, 0f)
    }
}
