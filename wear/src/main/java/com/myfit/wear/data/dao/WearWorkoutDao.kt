package com.myfit.wear.data.dao

import androidx.room.*
import com.myfit.wear.data.entity.ExerciseEntity
import com.myfit.wear.data.entity.SetEntity
import com.myfit.wear.data.entity.WorkoutSessionEntity

@Dao
interface WearWorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: SetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Query("SELECT * FROM workout_sessions ORDER BY startedAt DESC LIMIT 1")
    suspend fun getLastSession(): WorkoutSessionEntity?

    @Query("SELECT * FROM sets WHERE sessionId = :sessionId ORDER BY setNumber")
    suspend fun getSetsForSession(sessionId: String): List<SetEntity>

    @Query("SELECT * FROM sets WHERE exerciseId = :exerciseId AND sessionId = :sessionId ORDER BY setNumber DESC LIMIT 3")
    suspend fun getLastSetsForExerciseInSession(exerciseId: String, sessionId: String): List<SetEntity>

    @Query("SELECT * FROM sets WHERE recordedAt >= :fromMs ORDER BY recordedAt DESC")
    suspend fun getAllSetsSince(fromMs: Long): List<SetEntity>

    @Query("SELECT * FROM workout_sessions WHERE startedAt >= :fromMs")
    suspend fun getAllSessionsSince(fromMs: Long): List<WorkoutSessionEntity>

    @Query("SELECT * FROM exercises ORDER BY name")
    suspend fun getExercises(): List<ExerciseEntity>

    @Query("UPDATE workout_sessions SET endedAt = :endedAt WHERE id = :id")
    suspend fun endSession(id: String, endedAt: Long)
}
