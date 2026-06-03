package com.myfit.core.data.db.dao

import androidx.room.*
import com.myfit.core.data.db.entity.PlanOverrideEntity

@Dao
interface PlanOverrideDao {
    @Query("SELECT * FROM plan_overrides WHERE date = :date")
    suspend fun getOverride(date: String): PlanOverrideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setOverride(override: PlanOverrideEntity)

    @Query("DELETE FROM plan_overrides WHERE date = :date")
    suspend fun clearOverride(date: String)

    @Query("SELECT * FROM plan_overrides WHERE date >= :from AND date <= :to")
    suspend fun getOverridesForRange(from: String, to: String): List<PlanOverrideEntity>
}
