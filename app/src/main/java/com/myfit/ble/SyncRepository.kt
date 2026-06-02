package com.myfit.ble

import com.myfit.core.data.db.dao.SetDao
import com.myfit.core.data.db.dao.SyncEventDao
import com.myfit.core.data.db.dao.WorkoutDao
import com.myfit.core.data.db.entity.SyncEventEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val setDao: SetDao,
    private val syncEventDao: SyncEventDao
) {
    suspend fun mergeIncoming(data: SyncData) {
        data.sessions.forEach { incoming ->
            workoutDao.insertSession(incoming)
            syncEventDao.insertSyncEvent(SyncEventEntity(
                id         = UUID.randomUUID().toString(),
                eventType  = "session_start",
                entityId   = incoming.id,
                recordedAt = incoming.startedAt,
                device     = "watch",
                syncStatus = "merged",
                resolvedAt = System.currentTimeMillis()
            ))
        }
        data.sets.forEach { incoming ->
            val existing = setDao.getSetById(incoming.id)
            if (SyncPayload.shouldMergeSet(incoming.recordedAt, existing?.recordedAt)) {
                setDao.insertSet(incoming)
                syncEventDao.insertSyncEvent(SyncEventEntity(
                    id         = UUID.randomUUID().toString(),
                    eventType  = "set_recorded",
                    entityId   = incoming.id,
                    recordedAt = incoming.recordedAt,
                    device     = "watch",
                    syncStatus = "merged",
                    resolvedAt = System.currentTimeMillis()
                ))
            }
        }
    }
}
