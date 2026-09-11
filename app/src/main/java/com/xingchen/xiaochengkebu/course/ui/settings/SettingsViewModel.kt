package com.xingchen.xiaochengkebu.course.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.xingchen.xiaochengkebu.course.data.repository.TeachingRepository
import com.xingchen.xiaochengkebu.course.data.settings.QuickPresetStore
import com.xingchen.xiaochengkebu.course.domain.model.QuickPreset
import com.xingchen.xiaochengkebu.course.domain.model.Semester
import com.xingchen.xiaochengkebu.course.domain.model.TeachingRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val semesters: List<Semester> = emptyList(),
    val presets: List<QuickPreset> = emptyList(),
    val records: List<TeachingRecord> = emptyList(),
    val isLoading: Boolean = true,
)

class SettingsViewModel(
    private val repository: TeachingRepository,
    private val presetStore: QuickPresetStore,
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = combine(repository.observeSemesters(), presetStore.presets, repository.observeAllRecords()) { semesters, presets, records ->
        SettingsUiState(semesters, presets, records, false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun saveSemester(semester: Semester) = viewModelScope.launch { repository.upsertSemester(semester) }
    fun setCurrentSemester(id: Long) = viewModelScope.launch { repository.setCurrentSemester(id) }
    fun deleteSemester(id: Long) = viewModelScope.launch { repository.deleteSemester(id) }
    fun savePresets(presets: List<QuickPreset>) = viewModelScope.launch { presetStore.save(presets) }
    fun restorePresets() = viewModelScope.launch { presetStore.restoreDefaults() }
    fun replaceAll(payload: com.xingchen.xiaochengkebu.course.data.backup.BackupPayload) = viewModelScope.launch {
        repository.replaceData(payload.records, payload.semesters)
        presetStore.save(payload.quickPresets)
    }
    fun merge(payload: com.xingchen.xiaochengkebu.course.data.backup.BackupPayload) = viewModelScope.launch {
        repository.mergeData(payload.records, payload.semesters)
        if (payload.quickPresets.isNotEmpty()) presetStore.save(payload.quickPresets)
    }

    class Factory(private val repository: TeachingRepository, private val presetStore: QuickPresetStore) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(repository, presetStore) as T
    }
}
