package com.myfit.core.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.myfit.core.data.db.MyFitDatabase
import com.myfit.core.data.db.dao.*
import com.myfit.core.data.repository.WorkoutRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MyFitDatabase {
        var db: MyFitDatabase? = null
        val callback = object : RoomDatabase.Callback() {
            override fun onCreate(sqLiteDb: SupportSQLiteDatabase) {
                CoroutineScope(Dispatchers.IO).launch {
                    db?.exerciseDao()?.insertExercises(SeedExercises.all)
                }
            }
        }
        db = Room.databaseBuilder(context, MyFitDatabase::class.java, "myfit.db")
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()
        return db
    }

    @Provides fun provideWorkoutDao(db: MyFitDatabase): WorkoutDao = db.workoutDao()
    @Provides fun provideSetDao(db: MyFitDatabase): SetDao = db.setDao()
    @Provides fun provideExerciseDao(db: MyFitDatabase): ExerciseDao = db.exerciseDao()
    @Provides fun provideNutritionDao(db: MyFitDatabase): NutritionDao = db.nutritionDao()
    @Provides fun provideCnsDao(db: MyFitDatabase): CnsDao = db.cnsDao()

    @Provides
    @Singleton
    fun provideWorkoutRepository(
        workoutDao: WorkoutDao,
        setDao: SetDao,
        exerciseDao: ExerciseDao
    ): WorkoutRepository = WorkoutRepository(workoutDao, setDao, exerciseDao)
}
