package com.myfit.core.data.db.dao

import androidx.room.*
import com.myfit.core.data.db.entity.SyncEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncEvent(event: SyncEventEntity)

    @Query("SELECT * FROM sync_events WHERE syncStatus = 'pending'")
    suspend fun getPendingSyncEvents(): List<SyncEventEntity>

    @Query("UPDATE sync_events SET syncStatus = 'merged', resolvedAt = :now WHERE id = :id")
    suspend fun markMerged(id: String, now: Long)

    @Query("SELECT * FROM sync_events ORDER BY recordedAt DESC")
    fun getAllSyncEvents(): Flow<List<SyncEventEntity>>
}
