package com.xingchen.xiaochengkebu.course.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.xingchen.xiaochengkebu.course.data.repository.TeachingRepository
import com.xingchen.xiaochengkebu.course.domain.model.Semester
import com.xingchen.xiaochengkebu.course.domain.model.TeachingRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

enum class StatisticsPeriod { MONTH, SEMESTER, YEAR, ALL }

data class StatisticsUiState(
    val records: List<TeachingRecord> = emptyList(),
    val semesters: List<Semester> = emptyList(),
    val currentSemester: Semester? = null,
    val period: StatisticsPeriod = StatisticsPeriod.MONTH,
    val month: YearMonth = YearMonth.now(),
    val isLoading: Boolean = true,
)

class StatisticsViewModel(private val repository: TeachingRepository) : ViewModel() {
    private val period = MutableStateFlow(StatisticsPeriod.MONTH)
    private val month = MutableStateFlow(YearMonth.now())
    val uiState: StateFlow<StatisticsUiState> = combine(repository.observeAllRecords(), repository.observeSemesters(), repository.observeCurrentSemester(), period, month) { records, semesters, current, selectedPeriod, selectedMonth ->
        StatisticsUiState(records, semesters, current, selectedPeriod, selectedMonth, false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState())

    fun setPeriod(value: StatisticsPeriod) { period.value = value }

    fun deleteRecord(dateEpochDay: Long) {
        viewModelScope.launch { repository.deleteRecord(dateEpochDay) }
    }

    class Factory(private val repository: TeachingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = StatisticsViewModel(repository) as T
    }
}
