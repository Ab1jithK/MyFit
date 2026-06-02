package com.myfit.core.data.db.dao

import androidx.room.*
import com.myfit.core.data.db.entity.FoodItemEntity
import com.myfit.core.data.db.entity.NutritionLogEntity
import com.myfit.core.data.db.entity.QuickStackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(item: FoodItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(items: List<FoodItemEntity>)

    @Query("SELECT * FROM food_items ORDER BY name")
    fun getAllFoodItems(): Flow<List<FoodItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun logNutrition(entry: NutritionLogEntity)

    @Query("""
        SELECT * FROM nutrition_log
        WHERE loggedAt BETWEEN :fromMs AND :toMs
        ORDER BY loggedAt DESC
    """)
    fun getLogForRange(fromMs: Long, toMs: Long): Flow<List<NutritionLogEntity>>

    @Query("""
        SELECT * FROM quick_stacks
        ORDER BY useCount DESC, lastUsedAt DESC
        LIMIT 10
    """)
    fun getTopQuickStacks(): Flow<List<QuickStackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuickStack(stack: QuickStackEntity)

    @Query("UPDATE quick_stacks SET useCount = useCount + 1, lastUsedAt = :now WHERE id = :id")
    suspend fun incrementQuickStackUse(id: String, now: Long)

    @Query("""
        SELECT foodId FROM nutrition_log
        GROUP BY foodId
        ORDER BY COUNT(id) DESC, MAX(loggedAt) DESC
        LIMIT 10
    """)
    suspend fun getTopLoggedFoodIds(): List<String>

    @Query("SELECT * FROM nutrition_log WHERE loggedAt >= :midnightMs ORDER BY loggedAt DESC")
    suspend fun getTodayLog(midnightMs: Long): List<NutritionLogEntity>

    @Query("SELECT * FROM food_items WHERE id IN (:ids)")
    suspend fun getFoodsByIds(ids: List<String>): List<FoodItemEntity>
}
