package com.example.deviceasset.course.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teaching_records")
data class TeachingRecordEntity(
    @PrimaryKey val dateEpochDay: Long,
    val lessonCount: Int = 0,
    val eveningSupportCount: Int = 0,
    val note: String = "",
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "semesters")
data class SemesterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val isCurrent: Boolean = false,
    val createdAt: Long,
)
