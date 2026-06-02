package com.myfit.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_events",
    indices = [Index("syncStatus"), Index("recordedAt")]
)
data class SyncEventEntity(
    @PrimaryKey val id: String,
    val eventType: String,       // set_recorded|session_start|session_end
    val entityId: String,
    val recordedAt: Long,        // source device timestamp — conflict anchor
    val device: String,          // phone|watch
    val syncStatus: String = "pending",  // pending|merged|conflict|resolved
    val resolvedAt: Long?
)
