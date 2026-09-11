@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.xingchen.xiaochengkebu.course.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.xingchen.xiaochengkebu.course.data.repository.TeachingRepository
import com.xingchen.xiaochengkebu.course.data.settings.QuickPresetStore
import com.xingchen.xiaochengkebu.course.domain.model.PeriodStats
import com.xingchen.xiaochengkebu.course.domain.model.QuickPreset
import com.xingchen.xiaochengkebu.course.domain.model.Semester
import com.xingchen.xiaochengkebu.course.domain.model.TeachingRecord
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val records: Map<Long, TeachingRecord> = emptyMap(),
    val monthStats: PeriodStats = PeriodStats(),
    val semesterStats: PeriodStats = PeriodStats(),
    val currentSemester: Semester? = null,
    val presets: List<QuickPreset> = emptyList(),
    val isLoading: Boolean = true,
)

class CalendarViewModel(
    private val repository: TeachingRepository,
    presetStore: QuickPresetStore,
    initialDateEpochDay: Long? = null,
) : ViewModel() {
    private val month = MutableStateFlow(initialDateEpochDay?.let { YearMonth.from(LocalDate.ofEpochDay(it)) } ?: YearMonth.now())
    private val allRecords = repository.observeAllRecords()
    private val currentSemester = repository.observeCurrentSemester()
    private val presets = presetStore.presets
    private val monthStats = month.flatMapLatest { selectedMonth ->
        repository.observeStatsBetween(selectedMonth.atDay(1).toEpochDay(), selectedMonth.atEndOfMonth().toEpochDay())
    }
    private val semesterStats = currentSemester.flatMapLatest { semester ->
        semester?.let { repository.observeStatsBetween(it.startEpochDay, it.endEpochDay) } ?: flowOf(PeriodStats())
    }
    private val combinedStats = combine(monthStats, semesterStats) { selectedMonthStats, selectedSemesterStats ->
        selectedMonthStats to selectedSemesterStats
    }

    val uiState: StateFlow<CalendarUiState> = combine(month, allRecords, currentSemester, presets, combinedStats) { selectedMonth, records, semester, quickPresets, stats ->
        val (selectedMonthStats, selectedSemesterStats) = stats
        val monthRecords = records.filter { record ->
            val date = LocalDate.ofEpochDay(record.dateEpochDay)
            YearMonth.from(date) == selectedMonth
        }
        CalendarUiState(
            month = selectedMonth,
            records = monthRecords.associateBy { it.dateEpochDay },
            monthStats = selectedMonthStats,
            semesterStats = selectedSemesterStats,
            currentSemester = semester,
            presets = quickPresets,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    fun previousMonth() { month.value = month.value.minusMonths(1) }
    fun nextMonth() { month.value = month.value.plusMonths(1) }
    fun goToday() { month.value = YearMonth.now() }

    fun saveRecord(dateEpochDay: Long, lessonCount: Int, eveningSupportCount: Int, note: String, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            val cleanNote = note.trim()
            val old = repository.observeRecord(dateEpochDay).first()
            if (lessonCount == 0 && eveningSupportCount == 0 && cleanNote.isEmpty()) {
                repository.deleteRecord(dateEpochDay)
            } else {
                val now = System.currentTimeMillis()
                repository.upsertRecord(TeachingRecord(dateEpochDay, lessonCount.coerceAtLeast(0), eveningSupportCount.coerceAtLeast(0), cleanNote, old?.createdAt ?: now, now))
            }
            onSaved()
        }
    }

    fun deleteRecord(dateEpochDay: Long, onDeleted: () -> Unit = {}) {
        viewModelScope.launch { repository.deleteRecord(dateEpochDay); onDeleted() }
    }

    class Factory(private val repository: TeachingRepository, private val presetStore: QuickPresetStore, private val initialDateEpochDay: Long? = null) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CalendarViewModel(repository, presetStore, initialDateEpochDay) as T
    }
}
