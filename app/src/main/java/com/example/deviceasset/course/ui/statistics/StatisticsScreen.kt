@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.deviceasset.course.ui.statistics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deviceasset.course.data.repository.TeachingRepository
import com.example.deviceasset.course.domain.model.PeriodStats
import com.example.deviceasset.course.domain.model.Semester
import com.example.deviceasset.course.domain.model.TeachingRecord
import com.example.deviceasset.course.util.formatDate
import com.example.deviceasset.course.util.periodStats
import com.example.deviceasset.course.util.weekdayLabel
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun StatisticsRoute(repository: TeachingRepository, onHistory: (() -> Unit)? = null, historyOnly: Boolean = false, onBack: (() -> Unit)? = null, onRecordClick: ((Long) -> Unit)? = null) {
    val viewModel: StatisticsViewModel = viewModel(factory = StatisticsViewModel.Factory(repository))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (historyOnly) HistoryScreen(state, onBack = onBack ?: onHistory ?: {}, onRecordClick = onRecordClick ?: {}, onDeleteRecord = viewModel::deleteRecord) else StatisticsScreen(state, viewModel::setPeriod, onHistory)
}

@Composable
fun StatisticsScreen(state: StatisticsUiState, onPeriodSelected: (StatisticsPeriod) -> Unit, onHistory: (() -> Unit)?) {
    val stats = state.selectedStats()
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Column { Text("小尘课簿", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary); Text("统计", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
            )
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatisticsPeriod.entries.forEach { period -> FilterChip(selected = state.period == period, onClick = { onPeriodSelected(period) }, label = { Text(period.label()) }) }
                }
            }
            item {
                val detail = when (state.period) {
                    StatisticsPeriod.SEMESTER -> state.currentSemester?.let { "${formatDate(it.startEpochDay)} — ${formatDate(it.endEpochDay)}" }
                    StatisticsPeriod.ALL -> state.records.minByOrNull { it.dateEpochDay }?.let { "首次记录 ${formatDate(it.dateEpochDay)}" } ?: "尚无记录"
                    else -> null
                }
                StatsCard(title = state.period.title(state), stats = stats, detail = detail)
            }
            if (state.period != StatisticsPeriod.MONTH) {
                item { Text("按月汇总", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                items(monthGroups(state.records, state.period, state)) { (month, value) -> MonthRow(month, value) }
            }
            item {
                Spacer(Modifier.height(4.dp))
                if (onHistory != null) androidx.compose.material3.TextButton(onClick = onHistory) { Text("查看全部记录 →") }
            }
        }
    }
}

@Composable
private fun StatsCard(title: String, stats: PeriodStats, detail: String?) {
    com.example.deviceasset.ui.theme.InkWashCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            detail?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatValue("授课", "${stats.lessonCount} 节")
                StatValue("晚辅", "${stats.eveningSupportCount} 节", MaterialTheme.colorScheme.tertiary)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatValue("有授课天数", "${stats.teachingDays} 天")
                StatValue("日均授课", "${stats.averageLessonsPerTeachingDay} 节")
            }
            Text("有记录 ${stats.recordedDays} 天", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatValue(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column { Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color) }
}

@Composable
private fun MonthRow(month: YearMonth, stats: PeriodStats) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("${month.monthValue}月", style = MaterialTheme.typography.bodyMedium)
        Text("授课 ${stats.lessonCount} · 晚辅 ${stats.eveningSupportCount}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HistoryScreen(state: StatisticsUiState, onBack: () -> Unit, onRecordClick: (Long) -> Unit, onDeleteRecord: (Long) -> Unit) {
    var deleteDate by remember { mutableStateOf<Long?>(null) }
    Scaffold(
        containerColor = Color.Transparent,
        topBar = { TopAppBar(title = { Text("历史记录") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent)) },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.records.isEmpty()) {
                item { Text("还没有历史记录", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            groupedRecords(state.records).forEach { (month, records) ->
                item { Text("${month.year}年${month.monthValue}月", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(records) { record ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).clickable { onRecordClick(record.dateEpochDay) }.padding(vertical = 4.dp)) {
                            val date = LocalDate.ofEpochDay(record.dateEpochDay)
                            Text("${formatDate(record.dateEpochDay)} ${weekdayLabel(date)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text("授课 ${record.lessonCount} · 晚辅 ${record.eveningSupportCount}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (record.note.isNotBlank()) Text(record.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                        IconButton(onClick = { deleteDate = record.dateEpochDay }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "删除 ${formatDate(record.dateEpochDay)}")
                        }
                    }
                }
            }
        }
    }
    deleteDate?.let { dateEpochDay ->
        AlertDialog(
            onDismissRequest = { deleteDate = null },
            title = { Text("删除这条记录？") },
            text = { Text("删除后将移除当天的授课、晚辅和备注。") },
            confirmButton = { TextButton(onClick = { onDeleteRecord(dateEpochDay); deleteDate = null }) { Text("删除") } },
            dismissButton = { TextButton(onClick = { deleteDate = null }) { Text("取消") } },
        )
    }
}

private fun StatisticsPeriod.label() = when (this) { StatisticsPeriod.MONTH -> "本月"; StatisticsPeriod.SEMESTER -> "本学期"; StatisticsPeriod.YEAR -> "本年度"; StatisticsPeriod.ALL -> "全部" }
private fun StatisticsPeriod.title(state: StatisticsUiState): String = when (this) {
    StatisticsPeriod.MONTH -> "${state.month.year}年${state.month.monthValue}月"
    StatisticsPeriod.SEMESTER -> state.currentSemester?.name ?: "尚未设置当前学期"
    StatisticsPeriod.YEAR -> "${state.month.year}年度"
    StatisticsPeriod.ALL -> "自首次记录以来"
}

private fun StatisticsUiState.selectedStats(): PeriodStats = when (period) {
    StatisticsPeriod.MONTH -> periodStats(records.filter { YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) == month })
    StatisticsPeriod.SEMESTER -> currentSemester?.let { semester -> periodStats(records.filter { it.dateEpochDay in semester.startEpochDay..semester.endEpochDay }) } ?: PeriodStats()
    StatisticsPeriod.YEAR -> periodStats(records.filter { LocalDate.ofEpochDay(it.dateEpochDay).year == month.year })
    StatisticsPeriod.ALL -> periodStats(records)
}

private fun monthGroups(records: List<TeachingRecord>, period: StatisticsPeriod, state: StatisticsUiState): List<Pair<YearMonth, PeriodStats>> {
    val filtered = when (period) {
        StatisticsPeriod.MONTH -> records.filter { YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) == state.month }
        StatisticsPeriod.SEMESTER -> state.currentSemester?.let { semester -> records.filter { it.dateEpochDay in semester.startEpochDay..semester.endEpochDay } }.orEmpty()
        StatisticsPeriod.YEAR -> records.filter { LocalDate.ofEpochDay(it.dateEpochDay).year == state.month.year }
        StatisticsPeriod.ALL -> records
    }
    return filtered.groupBy { YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) }.toSortedMap(compareByDescending { it }).map { it.key to periodStats(it.value) }
}

private fun groupedRecords(records: List<TeachingRecord>): List<Pair<YearMonth, List<TeachingRecord>>> = records.groupBy { YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) }.toSortedMap(compareByDescending { it }).map { it.key to it.value.sortedByDescending { record -> record.dateEpochDay } }
