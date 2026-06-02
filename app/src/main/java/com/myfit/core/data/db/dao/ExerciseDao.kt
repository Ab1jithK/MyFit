package com.myfit.core.data.db.dao

import androidx.room.*
import com.myfit.core.data.db.entity.ExerciseEntity
import com.myfit.core.data.db.entity.TemplateExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateExercise(te: TemplateExerciseEntity)

    @Query("SELECT * FROM exercises ORDER BY name")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE primaryMuscle = :muscle ORDER BY name")
    fun getExercisesByMuscle(muscle: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY name")
    suspend fun searchExercises(query: String): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: String): ExerciseEntity?

    @Query("""
        SELECT e.* FROM exercises e
        INNER JOIN template_exercises te ON e.id = te.exerciseId
        WHERE te.templateId = :templateId
        ORDER BY te.position
    """)
    fun getExercisesForTemplate(templateId: String): Flow<List<ExerciseEntity>>

    @Query("""
        SELECT e.* FROM exercises e
        INNER JOIN template_exercises te ON e.id = te.exerciseId
        WHERE te.templateId = :templateId
        ORDER BY te.position
    """)
    suspend fun getExercisesForTemplateOnce(templateId: String): List<ExerciseEntity>

    @Query("SELECT * FROM template_exercises WHERE templateId = :templateId ORDER BY position")
    suspend fun getTemplateExercisesOnce(templateId: String): List<TemplateExerciseEntity>

    @Query("DELETE FROM template_exercises WHERE id = :id")
    suspend fun deleteTemplateExercise(id: String)

    @Query("DELETE FROM template_exercises WHERE templateId = :templateId")
    suspend fun deleteAllTemplateExercises(templateId: String)
}
