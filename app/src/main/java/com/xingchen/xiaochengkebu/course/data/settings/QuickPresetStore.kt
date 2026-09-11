package com.xingchen.xiaochengkebu.course.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.xingchen.xiaochengkebu.course.domain.model.QuickPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.courseDataStore by preferencesDataStore(name = "course_settings")

class QuickPresetStore(private val context: Context) {
    private val presetsKey = stringPreferencesKey("quick_presets")

    val presets: Flow<List<QuickPreset>> = context.courseDataStore.data.map { preferences ->
        preferences[presetsKey]?.let(::decode) ?: defaultPresets()
    }

    suspend fun save(presets: List<QuickPreset>) {
        context.courseDataStore.edit { preferences -> preferences[presetsKey] = encode(presets.take(6)) }
    }

    suspend fun restoreDefaults() = save(defaultPresets())

    private fun encode(presets: List<QuickPreset>): String = JSONArray().apply {
        presets.forEach { preset ->
            put(JSONObject().apply {
                put("id", preset.id)
                put("name", preset.name)
                put("lessonCount", preset.lessonCount.coerceAtLeast(0))
                put("eveningSupportCount", preset.eveningSupportCount.coerceAtLeast(0))
            })
        }
    }.toString()

    private fun decode(raw: String): List<QuickPreset> = runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    QuickPreset(
                        id = item.optInt("id", index),
                        name = item.optString("name", "常用 ${index + 1}"),
                        lessonCount = item.optInt("lessonCount", 0).coerceAtLeast(0),
                        eveningSupportCount = item.optInt("eveningSupportCount", 0).coerceAtLeast(0),
                    ),
                )
            }
        }.take(6)
    }.getOrElse { defaultPresets() }

    private fun defaultPresets() = listOf(
        QuickPreset(0, "4节", 4, 0),
        QuickPreset(1, "5节", 5, 0),
        QuickPreset(2, "4节 + 晚辅", 4, 1),
        QuickPreset(3, "3节 + 晚辅", 3, 1),
    )
}
