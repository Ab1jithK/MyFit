package com.myfit.core.data.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.myfit.core.data.db.MyFitDatabase
import com.myfit.core.data.db.entity.ExerciseEntity
import com.myfit.core.data.db.entity.SetEntity
import com.myfit.core.data.db.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SetDaoTest {

    private lateinit var db: MyFitDatabase
    private lateinit var setDao: SetDao
    private lateinit var workoutDao: WorkoutDao
    private lateinit var exerciseDao: ExerciseDao

    private val testExercise = ExerciseEntity(
        id = "ex_test",
        name = "Test Squat",
        primaryMuscle = "quadriceps",
        secondaryMuscles = null,
        equipment = "barbell",
        movementPattern = "squat"
    )

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyFitDatabase::class.java
        ).allowMainThreadQueries().build()
        setDao = db.setDao()
        workoutDao = db.workoutDao()
        exerciseDao = db.exerciseDao()
    }

    @After fun teardown() { db.close() }

    private suspend fun seedSession(): WorkoutSessionEntity {
        exerciseDao.insertExercise(testExercise)
        val session = WorkoutSessionEntity(
            id = UUID.randomUUID().toString(),
            templateId = null,
            startedAt = System.currentTimeMillis(),
            endedAt = null,
            sourceDevice = "phone",
            notes = null
        )
        workoutDao.insertSession(session)
        return session
    }

    @Test
    fun insertAndQuerySetsForSession() = runTest {
        val session = seedSession()
        val set = SetEntity(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            exerciseId = testExercise.id,
            setNumber = 1,
            recordedAt = System.currentTimeMillis(),
            weightKg = 100f,
            reps = 8,
            rpe = null,
            setType = "working",
            restSeconds = null,
            sourceDevice = "phone"
        )
        setDao.insertSet(set)
        val sets = setDao.getSetsForSession(session.id).first()
        assertEquals(1, sets.size)
        assertEquals(100f, sets[0].weightKg)
        assertEquals(8, sets[0].reps)
    }

    @Test
    fun getRecentSetsForExercise_returnsLatestFirst() = runTest {
        val session = seedSession()
        val now = System.currentTimeMillis()
        repeat(3) { i ->
            setDao.insertSet(SetEntity(
                id = UUID.randomUUID().toString(),
                sessionId = session.id,
                exerciseId = testExercise.id,
                setNumber = i + 1,
                recordedAt = now + i,
                weightKg = 80f + i * 5,
                reps = 5,
                rpe = null,
                setType = "working",
                restSeconds = null,
                sourceDevice = "phone"
            ))
        }
        val recent = setDao.getRecentSetsForExercise(testExercise.id, limit = 2)
        assertEquals(2, recent.size)
        assertEquals(90f, recent[0].weightKg)  // highest recordedAt first
    }
}
