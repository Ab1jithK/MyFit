package com.myfit.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.myfit.core.data.db.dao.*
import com.myfit.core.data.db.entity.*

@Database(
    entities = [
        MesocycleEntity::class,
        WorkoutTemplateEntity::class,
        ExerciseEntity::class,
        TemplateExerciseEntity::class,
        WorkoutSessionEntity::class,
        SetEntity::class,
        SetBiometricsEntity::class,
        CnsSnapshotEntity::class,
        FoodItemEntity::class,
        NutritionLogEntity::class,
        QuickStackEntity::class,
        SyncEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MyFitDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun setDao(): SetDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun cnsDao(): CnsDao
    abstract fun syncEventDao(): SyncEventDao
}
