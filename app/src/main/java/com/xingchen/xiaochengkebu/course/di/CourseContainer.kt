package com.xingchen.xiaochengkebu.course.di

import android.content.Context
import androidx.room.Room
import com.xingchen.xiaochengkebu.course.data.backup.BackupManager
import com.xingchen.xiaochengkebu.course.data.local.TeachingDatabase
import com.xingchen.xiaochengkebu.course.data.repository.RoomTeachingRepository
import com.xingchen.xiaochengkebu.course.data.repository.TeachingRepository
import com.xingchen.xiaochengkebu.course.data.settings.QuickPresetStore

class CourseContainer(context: Context) {
    private val appContext = context.applicationContext
    val database: TeachingDatabase = Room.databaseBuilder(
        appContext,
        TeachingDatabase::class.java,
        "xiaochen_kebu.db",
    ).build()
    val repository: TeachingRepository = RoomTeachingRepository(database.teachingRecordDao(), database.semesterDao(), database)
    val presetStore = QuickPresetStore(appContext)
    val backupManager = BackupManager(appContext)
}
