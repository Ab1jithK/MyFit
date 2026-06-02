package com.myfit.core.data.repository

import com.myfit.core.data.db.dao.ExerciseDao
import com.myfit.core.data.db.dao.SetDao
import com.myfit.core.data.db.dao.WorkoutDao
import com.myfit.core.data.db.entity.ExerciseEntity
import com.myfit.core.data.db.entity.SetEntity
import com.myfit.core.data.db.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val setDao: SetDao,
    private val exerciseDao: ExerciseDao
) {
    suspend fun createBlankSession(): String {
        val id = UUID.randomUUID().toString()
        workoutDao.insertSession(
            WorkoutSessionEntity(
                id = id,
                templateId = null,
                startedAt = System.currentTimeMillis(),
                endedAt = null,
                sourceDevice = "phone",
                notes = null
            )
        )
        return id
    }

    suspend fun endSession(sessionId: String) {
        val session = workoutDao.getSession(sessionId) ?: return
        workoutDao.updateSession(session.copy(endedAt = System.currentTimeMillis()))
    }

    suspend fun logSet(
        sessionId: String,
        exerciseId: String,
        setNumber: Int,
        weightKg: Float,
        reps: Int,
        setType: String
    ): SetEntity {
        val set = SetEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            exerciseId = exerciseId,
            setNumber = setNumber,
            recordedAt = System.currentTimeMillis(),
            weightKg = weightKg,
            reps = reps,
            rpe = null,
            setType = setType,
            restSeconds = null,
            sourceDevice = "phone"
        )
        setDao.insertSet(set)
        return set
    }

    fun getSetsForSession(sessionId: String): Flow<List<SetEntity>> =
        setDao.getSetsForSession(sessionId)

    fun getAllExercises(): Flow<List<ExerciseEntity>> =
        exerciseDao.getAllExercises()

    suspend fun searchExercises(query: String): List<ExerciseEntity> =
        exerciseDao.searchExercises(query)

    suspend fun getExercise(id: String): ExerciseEntity? =
        exerciseDao.getExerciseById(id)
}
