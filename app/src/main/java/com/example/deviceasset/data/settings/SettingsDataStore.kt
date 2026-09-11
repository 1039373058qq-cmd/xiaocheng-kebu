package com.example.deviceasset.data.settings

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

/** Settings storage is intentionally small; business records stay in Room. */
class SettingsDataStore(private val context: Context) {
    val dataStore get() = context.settingsDataStore
}
