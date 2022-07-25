package com.mtg.applock.data.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {
    companion object {
        const val DB_NAME = "applock_pattern.db"
    }

    @Provides
    @Singleton
    fun provideDatabase(context: Context) = Room.databaseBuilder(context, AppLockerDatabase::class.java, DB_NAME).fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    fun providePatternDao(db: AppLockerDatabase) = db.getPatternDao()
}