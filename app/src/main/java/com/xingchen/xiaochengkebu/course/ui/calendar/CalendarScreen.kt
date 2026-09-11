@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xingchen.xiaochengkebu.course.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xingchen.xiaochengkebu.course.data.repository.TeachingRepository
import com.xingchen.xiaochengkebu.course.data.settings.QuickPresetStore
import com.xingchen.xiaochengkebu.course.domain.model.PeriodStats
import com.xingchen.xiaochengkebu.course.domain.model.QuickPreset
import com.xingchen.xiaochengkebu.course.domain.model.TeachingRecord
import com.xingchen.xiaochengkebu.course.util.calendarDays
import com.xingchen.xiaochengkebu.course.util.formatDate
import com.xingchen.xiaochengkebu.course.util.periodStats
import com.xingchen.xiaochengkebu.course.util.weekdayLabel
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun CalendarRoute(
    repository: TeachingRepository,
    presetStore: QuickPresetStore,
    initialDateEpochDay: Long? = null,
    onOpenSettings: (() -> Unit)? = null,
) {
    val viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory(repository, presetStore, initialDateEpochDay))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CalendarScreen(state, viewModel::previousMonth, viewModel::nextMonth, viewModel::goToday, viewModel::saveRecord, viewModel::deleteRecord, initialDateEpochDay, onOpenSettings)
}

@Composable
fun CalendarScreen(
    state: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit,
    onSaveRecord: (Long, Int, Int, String, () -> Unit) -> Unit,
    onDeleteRecord: (Long, () -> Unit) -> Unit,
    initialDateEpochDay: Long? = null,
    onOpenSettings: (() -> Unit)? = null,
) {
    var selectedDate by remember { mutableStateOf(initialDateEpochDay) }
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("小尘课簿", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                        Text("日历", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CalendarSummaryCard(state.monthStats, state.semesterStats, state.currentSemester, onOpenSettings)
            MonthHeader(state.month, onPreviousMonth, onNextMonth, onToday)
            MonthCalendar(state.month, state.records, selectedDate, onDateClick = { selectedDate = it })
            if (state.records.isEmpty()) EmptyMonthHint()
            Spacer(Modifier.height(8.dp))
        }
    }
    selectedDate?.let { dateEpochDay ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()
        RecordBottomSheet(
            dateEpochDay = dateEpochDay,
            record = state.records[dateEpochDay],
            presets = state.presets,
            sheetState = sheetState,
            onDismiss = { selectedDate = null },
            onSave = { lessons, evening, note ->
                onSaveRecord(dateEpochDay, lessons, evening, note) { scope.launch { sheetState.hide(); selectedDate = null } }
            },
            onDelete = {
                onDeleteRecord(dateEpochDay) { scope.launch { sheetState.hide(); selectedDate = null } }
            },
        )
    }
}

@Composable
private fun CalendarSummaryCard(
    monthStats: PeriodStats,
    semesterStats: PeriodStats,
    currentSemester: com.xingchen.xiaochengkebu.course.domain.model.Semester?,
    onOpenSettings: (() -> Unit)?,
) {
    com.xingchen.xiaochengkebu.ui.theme.InkWashCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryLine("本月", monthStats)
            androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
            if (currentSemester == null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(enabled = onOpenSettings != null) { onOpenSettings?.invoke() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("尚未设置当前学期", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("设置 →", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                }
            } else SummaryLine("本学期", semesterStats)
        }
    }
}

@Composable
private fun EmptyMonthHint() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("·", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.secondary)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("这个月还没有记录", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("点一天，记下第一节课", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("·", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun SummaryLine(title: String, stats: PeriodStats) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("授课 ${stats.lessonCount}", style = MaterialTheme.typography.bodyMedium)
            Text("晚辅 ${stats.eveningSupportCount}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun MonthHeader(month: java.time.YearMonth, onPrevious: () -> Unit, onNext: () -> Unit, onToday: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        IconButton(onClick = onPrevious) { Icon(Icons.Outlined.ChevronLeft, contentDescription = "上个月") }
        Text("${month.year}年${month.monthValue}月", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        IconButton(onClick = onNext) { Icon(Icons.Outlined.ChevronRight, contentDescription = "下个月") }
        TextButton(onClick = onToday) { Text("今天") }
    }
}

@Composable
private fun MonthCalendar(month: java.time.YearMonth, records: Map<Long, TeachingRecord>, selectedDate: Long?, onDateClick: (Long) -> Unit) {
    val weekdays = listOf("一", "二", "三", "四", "五", "六", "日")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth()) { weekdays.forEach { Text(it, Modifier.weight(1f).padding(vertical = 4.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        month.calendarDays().chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { date ->
                    if (date == null) Spacer(Modifier.weight(1f).aspectRatio(0.78f)) else DayCell(date, records[date.toEpochDay()], selectedDate == date.toEpochDay(), onDateClick, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, record: TeachingRecord?, selected: Boolean, onDateClick: (Long) -> Unit, modifier: Modifier = Modifier) {
    val today = date == LocalDate.now()
    val borderColor = when {
        selected -> MaterialTheme.colorScheme.primary
        today -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
        else -> Color.Transparent
    }
    Column(
        modifier = modifier
            .aspectRatio(0.78f)
            .clip(MaterialTheme.shapes.small)
            .border(BorderStroke(if (borderColor == Color.Transparent) 0.dp else 1.5.dp, borderColor), MaterialTheme.shapes.small)
            .clickable { onDateClick(date.toEpochDay()) }
            .semantics {
                contentDescription = buildString {
                    append("${date.monthValue}月${date.dayOfMonth}日")
                    record?.let { append("，授课${it.lessonCount}节，晚辅${it.eveningSupportCount}节") }
                }
                role = Role.Button
            }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = if (today) FontWeight.Bold else FontWeight.Normal)
        val countText = record?.let { if (it.eveningSupportCount > 0) "${it.lessonCount}·${it.eveningSupportCount}" else if (it.lessonCount > 0) it.lessonCount.toString() else "" }.orEmpty()
        if (countText.isNotEmpty()) Text(countText, style = MaterialTheme.typography.labelSmall, color = if (record?.eveningSupportCount ?: 0 > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun RecordBottomSheet(
    dateEpochDay: Long,
    record: TeachingRecord?,
    presets: List<QuickPreset>,
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onSave: (Int, Int, String) -> Unit,
    onDelete: () -> Unit,
) {
    var lessonCount by remember(dateEpochDay, record) { mutableIntStateOf(record?.lessonCount ?: 0) }
    var eveningCount by remember(dateEpochDay, record) { mutableIntStateOf(record?.eveningSupportCount ?: 0) }
    var note by remember(dateEpochDay, record) { mutableStateOf(record?.note.orEmpty()) }
    var confirmDelete by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${formatDate(dateEpochDay)} · ${weekdayLabel(LocalDate.ofEpochDay(dateEpochDay))}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (record != null) TextButton(onClick = { confirmDelete = true }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            }
            CounterRow("授课", lessonCount, { lessonCount = (lessonCount - 1).coerceAtLeast(0) }, { lessonCount++ })
            CounterRow("晚辅", eveningCount, { eveningCount = (eveningCount - 1).coerceAtLeast(0) }, { eveningCount++ }, accent = MaterialTheme.colorScheme.tertiary)
            if (presets.isNotEmpty()) {
                Text("常用", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 8.dp)) {
                    items(presets) { preset ->
                        FilterChip(selected = false, onClick = { lessonCount = preset.lessonCount; eveningCount = preset.eveningSupportCount }, label = { Text(preset.name) })
                    }
                }
            }
            OutlinedTextField(value = note, onValueChange = { note = it }, modifier = Modifier.fillMaxWidth(), label = { Text("备注（可选）") }, minLines = 2, maxLines = 3)
            Button(onClick = { onSave(lessonCount, eveningCount, note) }, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("记下") }
            Spacer(Modifier.height(12.dp))
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("删除这天的记录？") },
            text = { Text("删除后可以重新记录，但本次课时和备注会被移除。") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("删除") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("取消") } },
        )
    }
}

@Composable
private fun CounterRow(label: String, value: Int, onMinus: () -> Unit, onPlus: () -> Unit, accent: Color = MaterialTheme.colorScheme.primary) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onMinus, modifier = Modifier.size(48.dp).semantics { contentDescription = "$label 减少"; role = Role.Button }, shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) { Box(contentAlignment = Alignment.Center) { Text("−", style = MaterialTheme.typography.headlineSmall) } }
            Text(value.toString(), modifier = Modifier.width(64.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.headlineSmall, color = accent, fontWeight = FontWeight.Bold)
            Surface(onClick = onPlus, modifier = Modifier.size(48.dp).semantics { contentDescription = "$label 增加"; role = Role.Button }, shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) { Box(contentAlignment = Alignment.Center) { Text("＋", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer) } }
        }
    }
}
