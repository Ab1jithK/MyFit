package com.myfit.wear.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.myfit.wear.data.dao.WearSyncDao
import com.myfit.wear.data.dao.WearWorkoutDao
import com.myfit.wear.data.entity.*

@Database(
    entities = [
        WorkoutSessionEntity::class,
        SetEntity::class,
        ExerciseEntity::class,
        SyncEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WearDatabase : RoomDatabase() {
    abstract fun workoutDao(): WearWorkoutDao
    abstract fun syncDao(): WearSyncDao
}
