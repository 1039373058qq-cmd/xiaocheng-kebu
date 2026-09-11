package com.example.deviceasset.course

import com.example.deviceasset.course.domain.model.TeachingRecord
import com.example.deviceasset.course.domain.model.Semester
import com.example.deviceasset.course.domain.model.requireValid
import com.example.deviceasset.course.util.calendarDays
import com.example.deviceasset.course.util.periodStats
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Assert.assertThrows

class CourseUtilsTest {
    @Test
    fun leapFebruaryStartsOnSundayWithCorrectCellCount() {
        val days = YearMonth.of(2028, 2).calendarDays()
        assertEquals(35, days.size)
        assertEquals(LocalDate.of(2028, 2, 1), days[1])
    }

    @Test
    fun calendarSupportsShortAndLongMonthsAcrossYearBoundary() {
        assertEquals(35, YearMonth.of(2026, 2).calendarDays().size)
        assertEquals(35, YearMonth.of(2026, 4).calendarDays().size)
        assertEquals(35, YearMonth.of(2026, 9).calendarDays().size)
        assertEquals(35, YearMonth.of(2026, 12).calendarDays().size)
        assertEquals(LocalDate.of(2027, 1, 1), YearMonth.of(2027, 1).calendarDays().first { it != null })
    }

    @Test
    fun periodStatsCountsTeachingDaysNotNaturalDays() {
        val records = listOf(
            TeachingRecord(1, lessonCount = 4, eveningSupportCount = 1),
            TeachingRecord(2, lessonCount = 0, eveningSupportCount = 2),
            TeachingRecord(3, lessonCount = 3),
        )
        val stats = periodStats(records)
        assertEquals(7, stats.lessonCount)
        assertEquals(3, stats.eveningSupportCount)
        assertEquals(3, stats.recordedDays)
        assertEquals(2, stats.teachingDays)
        assertEquals("3.5", stats.averageLessonsPerTeachingDay)
    }

    @Test
    fun validationRejectsNegativeCountsAndReversedSemester() {
        assertThrows(IllegalArgumentException::class.java) {
            TeachingRecord(1, lessonCount = -1).requireValid()
        }
        assertThrows(IllegalArgumentException::class.java) {
            Semester(name = "秋季", startEpochDay = 10, endEpochDay = 9).requireValid()
        }
    }

    @Test
    fun periodStatsIncludesBothRangeBoundaries() {
        val records = listOf(
            TeachingRecord(100, lessonCount = 2),
            TeachingRecord(101, lessonCount = 3),
            TeachingRecord(102, lessonCount = 4),
        )
        val selected = records.filter { it.dateEpochDay in 100L..101L }
        assertEquals(5, periodStats(selected).lessonCount)
        assertEquals(2, periodStats(selected).recordedDays)
    }
}
