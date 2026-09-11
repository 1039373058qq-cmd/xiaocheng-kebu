package com.example.deviceasset.course.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class TeachingRecordAggregate(
    val lessonCount: Int,
    val eveningSupportCount: Int,
    val recordedDays: Int,
    val teachingDays: Int,
)

@Dao
interface TeachingRecordDao {
    @Query("SELECT * FROM teaching_records WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    fun observeByDate(dateEpochDay: Long): Flow<TeachingRecordEntity?>

    @Query("SELECT * FROM teaching_records WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY dateEpochDay DESC")
    fun observeBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<TeachingRecordEntity>>

    @Query("SELECT CAST(COALESCE(SUM(lessonCount), 0) AS INTEGER) AS lessonCount, CAST(COALESCE(SUM(eveningSupportCount), 0) AS INTEGER) AS eveningSupportCount, CAST(COUNT(*) AS INTEGER) AS recordedDays, CAST(COALESCE(SUM(CASE WHEN lessonCount > 0 THEN 1 ELSE 0 END), 0) AS INTEGER) AS teachingDays FROM teaching_records WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay")
    fun observeAggregateBetween(startEpochDay: Long, endEpochDay: Long): Flow<TeachingRecordAggregate>

    @Query("SELECT * FROM teaching_records ORDER BY dateEpochDay DESC")
    fun observeAll(): Flow<List<TeachingRecordEntity>>

    @Query("SELECT CAST(COALESCE(SUM(lessonCount), 0) AS INTEGER) AS lessonCount, CAST(COALESCE(SUM(eveningSupportCount), 0) AS INTEGER) AS eveningSupportCount, CAST(COUNT(*) AS INTEGER) AS recordedDays, CAST(COALESCE(SUM(CASE WHEN lessonCount > 0 THEN 1 ELSE 0 END), 0) AS INTEGER) AS teachingDays FROM teaching_records")
    fun observeAggregateAll(): Flow<TeachingRecordAggregate>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TeachingRecordEntity)

    @Query("DELETE FROM teaching_records WHERE dateEpochDay = :dateEpochDay")
    suspend fun deleteByDate(dateEpochDay: Long)

    @Query("DELETE FROM teaching_records")
    suspend fun clearAll()
}

@Dao
interface SemesterDao {
    @Query("SELECT * FROM semesters ORDER BY startEpochDay DESC")
    fun observeAll(): Flow<List<SemesterEntity>>

    @Query("SELECT * FROM semesters WHERE isCurrent = 1 LIMIT 1")
    fun observeCurrent(): Flow<SemesterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SemesterEntity): Long

    @Query("UPDATE semesters SET isCurrent = 0 WHERE id != :id")
    suspend fun clearCurrentExcept(id: Long)

    @Query("UPDATE semesters SET isCurrent = 1 WHERE id = :id")
    suspend fun setCurrent(id: Long)

    @Query("DELETE FROM semesters WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM semesters")
    suspend fun clearAll()
}
