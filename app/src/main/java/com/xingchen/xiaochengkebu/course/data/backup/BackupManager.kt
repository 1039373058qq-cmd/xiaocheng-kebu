package com.xingchen.xiaochengkebu.course.data.backup

import android.content.Context
import android.net.Uri
import com.xingchen.xiaochengkebu.course.domain.model.QuickPreset
import com.xingchen.xiaochengkebu.course.domain.model.Semester
import com.xingchen.xiaochengkebu.course.domain.model.TeachingRecord
import com.xingchen.xiaochengkebu.course.domain.model.stableKey
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class BackupPayload(
    val records: List<TeachingRecord>,
    val semesters: List<Semester>,
    val quickPresets: List<QuickPreset>,
)

/** File-format code is kept pure so parsing/escaping can be tested without Android runtime. */
internal object BackupCodec {
    fun encode(records: List<TeachingRecord>, semesters: List<Semester>, presets: List<QuickPreset>): String {
        val root = JSONObject().apply {
            put("schemaVersion", 1)
            put("app", "小尘课簿")
            put("exportedAt", Instant.now().toString())
            put("records", JSONArray().apply { records.forEach { put(recordJson(it)) } })
            put("semesters", JSONArray().apply { semesters.forEach { put(semesterJson(it)) } })
            put("settings", JSONObject())
            put("quickPresets", JSONArray().apply { presets.take(6).forEach { put(presetJson(it)) } })
        }
        return root.toString(2)
    }

    fun decode(raw: String): BackupPayload {
        val root = runCatching { JSONObject(raw) }.getOrElse { error("备份文件格式不正确") }
        if (root.optInt("schemaVersion", -1) != 1) error("不支持的备份版本")
        return runCatching {
            val records = root.optJSONArray("records").toModels { recordFromJson(it) }
            val semesters = root.optJSONArray("semesters").toModels { semesterFromJson(it) }
            val presets = root.optJSONArray("quickPresets").toModels { presetFromJson(it) }
            if (records.map { it.dateEpochDay }.toSet().size != records.size) error("备份文件格式不正确")
            if (semesters.count { it.isCurrent } > 1) error("备份文件格式不正确")
            if (semesters.map { it.stableKey() }.toSet().size != semesters.size) error("备份文件格式不正确")
            if (presets.map { it.id }.toSet().size != presets.size) error("备份文件格式不正确")
            BackupPayload(records, semesters, presets.ifEmpty { defaultPresets() })
        }.getOrElse { error("备份文件格式不正确") }
    }

    fun csv(records: List<TeachingRecord>, semesters: List<Semester>): String = buildString {
        append("\uFEFF日期,星期,授课节数,晚辅节数,备注,所属学期\n")
        records.sortedByDescending { it.dateEpochDay }.forEach { record ->
            val date = LocalDate.ofEpochDay(record.dateEpochDay)
            val semester = semesters.firstOrNull { record.dateEpochDay in it.startEpochDay..it.endEpochDay }?.name.orEmpty()
            append(
                listOf(date.toString(), weekday(date), record.lessonCount.toString(), record.eveningSupportCount.toString(), record.note, semester)
                    .joinToString(",", transform = ::csvEscape),
            ).append('\n')
        }
    }

    private fun recordJson(record: TeachingRecord) = JSONObject().apply {
        put("date", LocalDate.ofEpochDay(record.dateEpochDay).toString())
        put("lessonCount", record.lessonCount)
        put("eveningSupportCount", record.eveningSupportCount)
        put("note", record.note)
        put("createdAt", record.createdAt)
        put("updatedAt", record.updatedAt)
    }

    private fun semesterJson(semester: Semester) = JSONObject().apply {
        put("id", semester.id)
        put("name", semester.name)
        put("startDate", LocalDate.ofEpochDay(semester.startEpochDay).toString())
        put("endDate", LocalDate.ofEpochDay(semester.endEpochDay).toString())
        put("isCurrent", semester.isCurrent)
        put("createdAt", semester.createdAt)
    }

    private fun presetJson(preset: QuickPreset) = JSONObject().apply {
        put("id", preset.id)
        put("name", preset.name)
        put("lessonCount", preset.lessonCount)
        put("eveningSupportCount", preset.eveningSupportCount)
    }

    private fun recordFromJson(item: JSONObject): TeachingRecord {
        val date = LocalDate.parse(item.getString("date"))
        val lessons = item.optInt("lessonCount", -1)
        val evening = item.optInt("eveningSupportCount", -1)
        if (lessons < 0 || evening < 0) error("备份文件格式不正确")
        return TeachingRecord(date.toEpochDay(), lessons, evening, item.optString("note"), item.optLong("createdAt"), item.optLong("updatedAt"))
    }

    private fun semesterFromJson(item: JSONObject): Semester {
        val start = LocalDate.parse(item.getString("startDate")).toEpochDay()
        val end = LocalDate.parse(item.getString("endDate")).toEpochDay()
        val name = item.optString("name").trim()
        if (name.isEmpty() || start > end) error("备份文件格式不正确")
        return Semester(item.optLong("id"), name, start, end, item.optBoolean("isCurrent"), item.optLong("createdAt"))
    }

    private fun presetFromJson(item: JSONObject): QuickPreset {
        val lessonCount = item.optInt("lessonCount", -1)
        val eveningSupportCount = item.optInt("eveningSupportCount", -1)
        if (lessonCount < 0 || eveningSupportCount < 0) error("备份文件格式不正确")
        return QuickPreset(item.optInt("id"), item.optString("name", "常用组合"), lessonCount, eveningSupportCount)
    }

    private fun weekday(date: LocalDate): String = when (date.dayOfWeek.value) {
        1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"; 5 -> "周五"; 6 -> "周六"; else -> "周日"
    }

    private fun csvEscape(value: String): String = if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) "\"${value.replace("\"", "\"\"")}\"" else value

    private fun <T> JSONArray?.toModels(transform: (JSONObject) -> T): List<T> = if (this == null) emptyList() else buildList {
        for (index in 0 until length()) add(transform(getJSONObject(index)))
    }

    private fun defaultPresets() = listOf(
        QuickPreset(0, "4节", 4, 0), QuickPreset(1, "5节", 5, 0), QuickPreset(2, "4节 + 晚辅", 4, 1), QuickPreset(3, "3节 + 晚辅", 3, 1),
    )
}

class BackupManager(private val context: Context) {
    suspend fun exportJson(uri: Uri, records: List<TeachingRecord>, semesters: List<Semester>, presets: List<QuickPreset>) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(BackupCodec.encode(records, semesters, presets).toByteArray(Charsets.UTF_8))
            } ?: error("无法写入备份文件")
        }
    }

    suspend fun importJson(uri: Uri): BackupPayload = withContext(Dispatchers.IO) {
        val raw = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            ?: error("无法读取备份文件")
        BackupCodec.decode(raw)
    }

    suspend fun exportCsv(uri: Uri, records: List<TeachingRecord>, semesters: List<Semester>) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { it.write(BackupCodec.csv(records, semesters).toByteArray(Charsets.UTF_8)) }
                ?: error("无法写入 CSV 文件")
        }
    }
}
