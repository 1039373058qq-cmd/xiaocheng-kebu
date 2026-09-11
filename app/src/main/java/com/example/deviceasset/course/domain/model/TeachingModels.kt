package com.example.deviceasset.course.domain.model

data class TeachingRecord(
    val dateEpochDay: Long,
    val lessonCount: Int = 0,
    val eveningSupportCount: Int = 0,
    val note: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

data class Semester(
    val id: Long = 0L,
    val name: String,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val isCurrent: Boolean = false,
    val createdAt: Long = 0L,
)

data class QuickPreset(
    val id: Int,
    val name: String,
    val lessonCount: Int,
    val eveningSupportCount: Int,
)

data class PeriodStats(
    val lessonCount: Int = 0,
    val eveningSupportCount: Int = 0,
    val recordedDays: Int = 0,
    val teachingDays: Int = 0,
) {
    val averageLessonsPerTeachingDay: String
        get() = if (teachingDays == 0) "0" else "%.1f".format(java.util.Locale.ROOT, lessonCount.toDouble() / teachingDays)
}

fun TeachingRecord.requireValid() {
    require(lessonCount >= 0) { "lessonCount must not be negative" }
    require(eveningSupportCount >= 0) { "eveningSupportCount must not be negative" }
}

fun Semester.requireValid() {
    require(name.isNotBlank()) { "semester name must not be blank" }
    require(startEpochDay <= endEpochDay) { "semester start must not be after end" }
}
