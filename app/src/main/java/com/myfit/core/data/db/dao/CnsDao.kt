package com.myfit.core.data.db.dao

import androidx.room.*
import com.myfit.core.data.db.entity.CnsSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CnsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: CnsSnapshotEntity)

    @Query("SELECT * FROM cns_snapshots ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatestSnapshot(): CnsSnapshotEntity?

    @Query("""
        SELECT * FROM cns_snapshots
        WHERE recordedAt BETWEEN :fromMs AND :toMs
        ORDER BY recordedAt ASC
    """)
    fun getSnapshotsForRange(fromMs: Long, toMs: Long): Flow<List<CnsSnapshotEntity>>

    @Query("SELECT * FROM cns_snapshots WHERE recordedAt >= :fromMs ORDER BY recordedAt ASC")
    suspend fun getSnapshotsSince(fromMs: Long): List<CnsSnapshotEntity>
}
