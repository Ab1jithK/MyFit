package com.myfit.wear.data.dao

import androidx.room.*
import com.myfit.wear.data.entity.SetEntity
import com.myfit.wear.data.entity.SyncEventEntity
import com.myfit.wear.data.entity.WorkoutSessionEntity

@Dao
interface WearSyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncEvent(event: SyncEventEntity)

    @Query("SELECT * FROM sync_events WHERE syncStatus = 'pending'")
    suspend fun getPendingSyncEvents(): List<SyncEventEntity>

    @Query("UPDATE sync_events SET syncStatus = 'merged', resolvedAt = :now WHERE id = :id")
    suspend fun markMerged(id: String, now: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: WorkoutSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSet(set: SetEntity)

    @Query("SELECT * FROM sets WHERE id = :id")
    suspend fun getSetById(id: String): SetEntity?

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): WorkoutSessionEntity?
}
