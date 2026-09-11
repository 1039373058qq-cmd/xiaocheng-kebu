package com.xingchen.xiaochengkebu.course.data.repository

import com.xingchen.xiaochengkebu.course.data.local.SemesterDao
import com.xingchen.xiaochengkebu.course.data.local.SemesterEntity
import com.xingchen.xiaochengkebu.course.data.local.TeachingRecordDao
import com.xingchen.xiaochengkebu.course.data.local.TeachingRecordEntity
import com.xingchen.xiaochengkebu.course.data.local.TeachingRecordAggregate
import com.xingchen.xiaochengkebu.course.domain.model.Semester
import com.xingchen.xiaochengkebu.course.domain.model.TeachingRecord
import com.xingchen.xiaochengkebu.course.domain.model.PeriodStats
import com.xingchen.xiaochengkebu.course.domain.model.requireValid
import com.xingchen.xiaochengkebu.course.domain.model.stableKey
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
    suspend fun mergeData(records: List<TeachingRecord>, semesters: List<Semester>)
}

class RoomTeachingRepository(
    private val recordDao: TeachingRecordDao,
    private val semesterDao: SemesterDao,
    private val database: com.xingchen.xiaochengkebu.course.data.local.TeachingDatabase,
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
        val normalized = semester.copy(name = semester.name.trim())
        normalized.requireValid()
        return database.withTransaction {
            if (normalized.isCurrent) semesterDao.clearCurrentExcept(normalized.id)
            val id = semesterDao.upsert(normalized.toEntity())
            if (normalized.isCurrent) {
                semesterDao.clearCurrentExcept(id)
                semesterDao.setCurrent(id)
            }
            id
        }
    }

    override suspend fun setCurrentSemester(id: Long) {
        database.withTransaction {
            semesterDao.clearCurrentExcept(id)
            semesterDao.setCurrent(id)
        }
    }

    override suspend fun deleteSemester(id: Long) = semesterDao.deleteById(id)
    override suspend fun clearRecords() = recordDao.clearAll()
    override suspend fun clearSemesters() = semesterDao.clearAll()
    override suspend fun replaceData(records: List<TeachingRecord>, semesters: List<Semester>) {
        records.forEach(TeachingRecord::requireValid)
        require(records.map { it.dateEpochDay }.toSet().size == records.size) { "备份包含重复日期" }
        val normalizedSemesters = semesters.map { it.copy(name = it.name.trim()) }
        normalizedSemesters.forEach(Semester::requireValid)
        validateSemesters(normalizedSemesters)
        database.withTransaction {
            recordDao.clearAll()
            semesterDao.clearAll()
            records.forEach { recordDao.upsert(it.toEntity()) }
            var currentId: Long? = null
            normalizedSemesters.forEach { semester ->
                val insertedId = semesterDao.upsert(semester.copy(id = 0L, isCurrent = false).toEntity())
                if (semester.isCurrent) currentId = insertedId
            }
            currentId?.let { semesterDao.setCurrent(it) }
        }
    }

    override suspend fun mergeData(records: List<TeachingRecord>, semesters: List<Semester>) {
        records.forEach(TeachingRecord::requireValid)
        require(records.map { it.dateEpochDay }.toSet().size == records.size) { "备份包含重复日期" }
        val normalizedSemesters = semesters.map { it.copy(name = it.name.trim()) }
        normalizedSemesters.forEach(Semester::requireValid)
        validateSemesters(normalizedSemesters)
        database.withTransaction {
            val existingSemesters = semesterDao.getAllOnce()
            normalizedSemesters.forEach { imported ->
                val existing = existingSemesters.firstOrNull { it.toModel().stableKey() == imported.stableKey() }
                val entity = imported.copy(
                    id = existing?.id ?: 0L,
                    // A non-current import must not unset an existing current semester.
                    isCurrent = imported.isCurrent || existing?.isCurrent == true,
                ).toEntity()
                val storedId = semesterDao.upsert(entity)
                if (imported.isCurrent) {
                    semesterDao.clearCurrentExcept(storedId)
                    semesterDao.setCurrent(storedId)
                }
            }
            records.forEach { recordDao.upsert(it.toEntity()) }
        }
    }
}

private fun TeachingRecordEntity.toModel() = TeachingRecord(dateEpochDay, lessonCount, eveningSupportCount, note, createdAt, updatedAt)
private fun TeachingRecord.toEntity() = TeachingRecordEntity(dateEpochDay, lessonCount, eveningSupportCount, note, createdAt, updatedAt)
private fun SemesterEntity.toModel() = Semester(id, name, startEpochDay, endEpochDay, isCurrent, createdAt)
private fun Semester.toEntity() = SemesterEntity(id, name, startEpochDay, endEpochDay, isCurrent, createdAt)
private fun TeachingRecordAggregate.toStats() = PeriodStats(lessonCount, eveningSupportCount, recordedDays, teachingDays)

private fun validateSemesters(semesters: List<Semester>) {
    require(semesters.count { it.isCurrent } <= 1) { "备份只能包含一个当前学期" }
    require(semesters.map(Semester::stableKey).toSet().size == semesters.size) { "备份包含重复学期" }
}
