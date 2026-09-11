package com.example.deviceasset.course.data.repository

import com.example.deviceasset.course.data.local.SemesterDao
import com.example.deviceasset.course.data.local.SemesterEntity
import com.example.deviceasset.course.data.local.TeachingRecordDao
import com.example.deviceasset.course.data.local.TeachingRecordEntity
import com.example.deviceasset.course.data.local.TeachingRecordAggregate
import com.example.deviceasset.course.domain.model.Semester
import com.example.deviceasset.course.domain.model.TeachingRecord
import com.example.deviceasset.course.domain.model.PeriodStats
import com.example.deviceasset.course.domain.model.requireValid
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface TeachingRepository {
    fun observeRecord(dateEpochDay: Long): Flow<TeachingRecord?>
    fun observeRecordsBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<TeachingRecord>>
    fun observeStatsBetween(startEpochDay: Long, endEpochDay: Long): Flow<PeriodStats>
    fun observeAllRecords(): Flow<List<TeachingRecord>>
    fun observeAllStats(): Flow<PeriodStats>
    fun observeSemesters(): Flow<List<Semester>>
    fun observeCurrentSemester(): Flow<Semester?>
    suspend fun upsertRecord(record: TeachingRecord)
    suspend fun deleteRecord(dateEpochDay: Long)
    suspend fun upsertSemester(semester: Semester): Long
    suspend fun setCurrentSemester(id: Long)
    suspend fun deleteSemester(id: Long)
    suspend fun clearRecords()
    suspend fun clearSemesters()
    suspend fun replaceData(records: List<TeachingRecord>, semesters: List<Semester>)
}

class RoomTeachingRepository(
    private val recordDao: TeachingRecordDao,
    private val semesterDao: SemesterDao,
    private val database: com.example.deviceasset.course.data.local.TeachingDatabase,
) : TeachingRepository {
    override fun observeRecord(dateEpochDay: Long) = recordDao.observeByDate(dateEpochDay).map { it?.toModel() }

    override fun observeRecordsBetween(startEpochDay: Long, endEpochDay: Long) =
        recordDao.observeBetween(startEpochDay, endEpochDay).map { list -> list.map(TeachingRecordEntity::toModel) }

    override fun observeStatsBetween(startEpochDay: Long, endEpochDay: Long) =
        recordDao.observeAggregateBetween(startEpochDay, endEpochDay).map(TeachingRecordAggregate::toStats)

    override fun observeAllRecords() = recordDao.observeAll().map { list -> list.map(TeachingRecordEntity::toModel) }

    override fun observeAllStats() = recordDao.observeAggregateAll().map(TeachingRecordAggregate::toStats)

    override fun observeSemesters() = semesterDao.observeAll().map { list -> list.map(SemesterEntity::toModel) }

    override fun observeCurrentSemester() = semesterDao.observeCurrent().map { it?.toModel() }

    override suspend fun upsertRecord(record: TeachingRecord) {
        record.requireValid()
        recordDao.upsert(record.toEntity())
    }

    override suspend fun deleteRecord(dateEpochDay: Long) = recordDao.deleteByDate(dateEpochDay)

    override suspend fun upsertSemester(semester: Semester): Long {
        semester.requireValid()
        if (semester.isCurrent) semesterDao.clearCurrentExcept(semester.id)
        val id = semesterDao.upsert(semester.toEntity())
        if (semester.isCurrent && semester.id == 0L) semesterDao.clearCurrentExcept(id)
        return id
    }

    override suspend fun setCurrentSemester(id: Long) {
        semesterDao.clearCurrentExcept(id)
        semesterDao.setCurrent(id)
    }

    override suspend fun deleteSemester(id: Long) = semesterDao.deleteById(id)
    override suspend fun clearRecords() = recordDao.clearAll()
    override suspend fun clearSemesters() = semesterDao.clearAll()
    override suspend fun replaceData(records: List<TeachingRecord>, semesters: List<Semester>) {
        records.forEach(TeachingRecord::requireValid)
        semesters.forEach(Semester::requireValid)
        database.withTransaction {
            recordDao.clearAll()
            semesterDao.clearAll()
            records.forEach { recordDao.upsert(it.toEntity()) }
            var currentId: Long? = null
            semesters.forEach { semester ->
                val insertedId = semesterDao.upsert(semester.copy(id = 0L, isCurrent = false).toEntity())
                if (semester.isCurrent) currentId = insertedId
            }
            currentId?.let { semesterDao.setCurrent(it) }
        }
    }
}

private fun TeachingRecordEntity.toModel() = TeachingRecord(dateEpochDay, lessonCount, eveningSupportCount, note, createdAt, updatedAt)
private fun TeachingRecord.toEntity() = TeachingRecordEntity(dateEpochDay, lessonCount, eveningSupportCount, note, createdAt, updatedAt)
private fun SemesterEntity.toModel() = Semester(id, name, startEpochDay, endEpochDay, isCurrent, createdAt)
private fun Semester.toEntity() = SemesterEntity(id, name, startEpochDay, endEpochDay, isCurrent, createdAt)
private fun TeachingRecordAggregate.toStats() = PeriodStats(lessonCount, eveningSupportCount, recordedDays, teachingDays)
