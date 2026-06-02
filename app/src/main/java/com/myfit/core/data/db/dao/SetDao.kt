package com.myfit.core.data.db.dao

import androidx.room.*
import com.myfit.core.data.db.entity.SetBiometricsEntity
import com.myfit.core.data.db.entity.SetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: SetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBiometrics(biometrics: SetBiometricsEntity)

    @Query("SELECT * FROM sets WHERE sessionId = :sessionId ORDER BY setNumber")
    fun getSetsForSession(sessionId: String): Flow<List<SetEntity>>

    @Query("""
        SELECT * FROM sets
        WHERE exerciseId = :exerciseId
        ORDER BY recordedAt DESC
        LIMIT :limit
    """)
    suspend fun getRecentSetsForExercise(exerciseId: String, limit: Int = 10): List<SetEntity>

    @Query("""
        SELECT * FROM sets
        WHERE exerciseId = :exerciseId
          AND recordedAt BETWEEN :fromMs AND :toMs
        ORDER BY recordedAt DESC
    """)
    suspend fun getSetsForExerciseInRange(
        exerciseId: String,
        fromMs: Long,
        toMs: Long
    ): List<SetEntity>

    @Query("SELECT * FROM set_biometrics WHERE setId = :setId")
    suspend fun getBiometrics(setId: String): SetBiometricsEntity?
}
