package com.xingchen.xiaochengkebu.course.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TeachingRecordEntity::class, SemesterEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class TeachingDatabase : RoomDatabase() {
    abstract fun teachingRecordDao(): TeachingRecordDao
    abstract fun semesterDao(): SemesterDao
}
