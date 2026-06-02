package com.myfit.wear.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.myfit.wear.data.WearDatabase
import com.myfit.wear.data.dao.WearSyncDao
import com.myfit.wear.data.dao.WearWorkoutDao
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
object WearDatabaseModule {

    @Provides
    @Singleton
    fun provideWearDatabase(@ApplicationContext context: Context): WearDatabase {
        var db: WearDatabase? = null
        val callback = object : RoomDatabase.Callback() {
            override fun onCreate(sqLiteDb: SupportSQLiteDatabase) {
                CoroutineScope(Dispatchers.IO).launch {
                    db?.workoutDao()?.insertExercises(WearSeedExercises.all)
                }
            }
        }
        db = Room.databaseBuilder(context, WearDatabase::class.java, "myfit_wear.db")
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()
        return db
    }

    @Provides fun provideWearWorkoutDao(db: WearDatabase): WearWorkoutDao = db.workoutDao()
    @Provides fun provideWearSyncDao(db: WearDatabase): WearSyncDao = db.syncDao()
}
