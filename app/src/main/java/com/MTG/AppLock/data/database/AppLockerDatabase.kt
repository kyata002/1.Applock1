package com.MTG.AppLock.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.MTG.AppLock.data.database.pattern.PatternDao
import com.MTG.AppLock.data.database.pattern.PatternEntity

@Database(entities = [PatternEntity::class], version = 2, exportSchema = false)
abstract class AppLockerDatabase : RoomDatabase() {
    abstract fun getPatternDao(): PatternDao
}