package com.myfit.core.data.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.myfit.core.data.db.MyFitDatabase
import com.myfit.core.data.db.entity.WorkoutSessionEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class WorkoutDaoTest {

    private lateinit var db: MyFitDatabase
    private lateinit var dao: WorkoutDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyFitDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.workoutDao()
    }

    @After
    fun teardown() { db.close() }

    @Test
    fun insertAndRetrieveSession() = runTest {
        val session = WorkoutSessionEntity(
            id = UUID.randomUUID().toString(),
            templateId = null,
            startedAt = System.currentTimeMillis(),
            endedAt = null,
            sourceDevice = "phone",
            notes = null
        )
        dao.insertSession(session)
        val result = dao.getSession(session.id)
        assertEquals(session, result)
    }

    @Test
    fun getActiveSession_returnsNullWhenNoneActive() = runTest {
        val result = dao.getActiveSession()
        assertNull(result)
    }

    @Test
    fun getActiveSession_returnsSessionWithNullEndedAt() = runTest {
        val session = WorkoutSessionEntity(
            id = UUID.randomUUID().toString(),
            templateId = null,
            startedAt = System.currentTimeMillis(),
            endedAt = null,
            sourceDevice = "phone",
            notes = null
        )
        dao.insertSession(session)
        val active = dao.getActiveSession()
        assertEquals(session.id, active?.id)
    }

    @Test
    fun getActiveSession_ignoresCompletedSessions() = runTest {
        val completed = WorkoutSessionEntity(
            id = UUID.randomUUID().toString(),
            templateId = null,
            startedAt = System.currentTimeMillis() - 3600_000,
            endedAt = System.currentTimeMillis(),
            sourceDevice = "phone",
            notes = null
        )
        dao.insertSession(completed)
        val active = dao.getActiveSession()
        assertNull(active)
    }
}
