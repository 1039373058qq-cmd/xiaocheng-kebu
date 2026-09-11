package com.xingchen.xiaochengkebu.course.util

import com.xingchen.xiaochengkebu.course.domain.model.PeriodStats
import com.xingchen.xiaochengkebu.course.domain.model.TeachingRecord
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

fun LocalDate.epochDay(): Long = toEpochDay()

fun formatDate(dateEpochDay: Long): String = LocalDate.ofEpochDay(dateEpochDay).let { "%04d年%d月%d日".format(it.year, it.monthValue, it.dayOfMonth) }

fun formatShortDate(dateEpochDay: Long): String = LocalDate.ofEpochDay(dateEpochDay).let { "%d月%d日".format(it.monthValue, it.dayOfMonth) }

fun weekdayLabel(date: LocalDate): String = when (date.dayOfWeek) {
    DayOfWeek.MONDAY -> "周一"
    DayOfWeek.TUESDAY -> "周二"
    DayOfWeek.WEDNESDAY -> "周三"
    DayOfWeek.THURSDAY -> "周四"
    DayOfWeek.FRIDAY -> "周五"
    DayOfWeek.SATURDAY -> "周六"
    DayOfWeek.SUNDAY -> "周日"
}

fun periodStats(records: Iterable<TeachingRecord>): PeriodStats {
    val list = records.toList()
    return PeriodStats(
        lessonCount = list.sumOf { it.lessonCount },
        eveningSupportCount = list.sumOf { it.eveningSupportCount },
        recordedDays = list.size,
        teachingDays = list.count { it.lessonCount > 0 },
    )
}

fun YearMonth.calendarDays(): List<LocalDate?> {
    val first = atDay(1)
    val leading = (first.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    return buildList {
        repeat(leading) { add(null) }
        for (day in 1..lengthOfMonth()) add(atDay(day))
        while (size % 7 != 0) add(null)
    }
}
