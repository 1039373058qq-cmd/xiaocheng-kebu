package com.xingchen.xiaochengkebu.course

import com.xingchen.xiaochengkebu.course.data.backup.BackupCodec
import com.xingchen.xiaochengkebu.course.domain.model.TeachingRecord
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BackupCodecTest {
    @Test
    fun csvEscapesCommaQuoteAndNewlineAndAddsBom() {
        val record = TeachingRecord(LocalDate.of(2026, 9, 11).toEpochDay(), 4, 1, "代课,\"临时\"\n备注")
        val csv = BackupCodec.csv(listOf(record), emptyList())

        assertTrue(csv.startsWith("\uFEFF日期,星期,授课节数,晚辅节数,备注,所属学期\n"))
        assertTrue(csv.contains("\"代课,\"\"临时\"\"\n备注\""))
    }

}
