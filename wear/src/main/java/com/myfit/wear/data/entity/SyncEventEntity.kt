package com.myfit.wear.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_events",
    indices = [Index("syncStatus"), Index("recordedAt")]
)
data class SyncEventEntity(
    @PrimaryKey val id: String,
    val eventType: String,
    val entityId: String,
    val recordedAt: Long,
    val device: String,
    val syncStatus: String = "pending",
    val resolvedAt: Long?
)
