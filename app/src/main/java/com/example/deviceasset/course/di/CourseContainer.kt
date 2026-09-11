package com.example.deviceasset.course.di

import android.content.Context
import androidx.room.Room
import com.example.deviceasset.course.data.backup.BackupManager
import com.example.deviceasset.course.data.local.TeachingDatabase
import com.example.deviceasset.course.data.repository.RoomTeachingRepository
import com.example.deviceasset.course.data.repository.TeachingRepository
import com.example.deviceasset.course.data.settings.QuickPresetStore

class CourseContainer(context: Context) {
    private val appContext = context.applicationContext
    val database: TeachingDatabase = Room.databaseBuilder(
        appContext,
        TeachingDatabase::class.java,
        "xiaocheng_kebu.db",
    ).build()
    val repository: TeachingRepository = RoomTeachingRepository(database.teachingRecordDao(), database.semesterDao(), database)
    val presetStore = QuickPresetStore(appContext)
    val backupManager = BackupManager(appContext)
}
