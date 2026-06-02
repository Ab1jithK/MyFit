package com.myfit.core.data.db.dao

import androidx.room.*
import com.myfit.core.data.db.entity.WorkoutSessionEntity
import com.myfit.core.data.db.entity.WorkoutTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity)

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSession(id: String): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE endedAt IS NULL LIMIT 1")
    suspend fun getActiveSession(): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkoutTemplateEntity)

    @Query("SELECT * FROM workout_templates ORDER BY name")
    fun getAllTemplates(): Flow<List<WorkoutTemplateEntity>>

    @Query("SELECT * FROM workout_templates WHERE dayOfWeek = :dayOfWeek")
    suspend fun getTemplatesForDay(dayOfWeek: Int): List<WorkoutTemplateEntity>

    @Update
    suspend fun updateTemplate(template: WorkoutTemplateEntity)

    @Query("DELETE FROM workout_templates WHERE id = :id")
    suspend fun deleteTemplate(id: String)

    @Query("SELECT * FROM workout_sessions WHERE startedAt >= :fromMs")
    suspend fun getAllSessionsSince(fromMs: Long): List<WorkoutSessionEntity>
}
