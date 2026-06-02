package com.myfit.core.data.di

import android.content.Context
import androidx.room.Room
import com.myfit.core.data.db.MyFitDatabase
import com.myfit.core.data.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MyFitDatabase =
        Room.databaseBuilder(context, MyFitDatabase::class.java, "myfit.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideWorkoutDao(db: MyFitDatabase): WorkoutDao = db.workoutDao()
    @Provides fun provideSetDao(db: MyFitDatabase): SetDao = db.setDao()
    @Provides fun provideExerciseDao(db: MyFitDatabase): ExerciseDao = db.exerciseDao()
    @Provides fun provideNutritionDao(db: MyFitDatabase): NutritionDao = db.nutritionDao()
    @Provides fun provideCnsDao(db: MyFitDatabase): CnsDao = db.cnsDao()
}
