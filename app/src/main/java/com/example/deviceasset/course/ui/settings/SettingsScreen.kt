@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.deviceasset.course.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deviceasset.course.data.backup.BackupManager
import com.example.deviceasset.course.data.backup.BackupPayload
import com.example.deviceasset.course.data.repository.TeachingRepository
import com.example.deviceasset.course.data.settings.QuickPresetStore
import com.example.deviceasset.course.domain.model.QuickPreset
import com.example.deviceasset.course.domain.model.Semester
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsRoute(repository: TeachingRepository, presetStore: QuickPresetStore, backupManager: BackupManager) {
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(repository, presetStore))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(state, backupManager, viewModel::saveSemester, viewModel::setCurrentSemester, viewModel::deleteSemester, viewModel::savePresets, viewModel::restorePresets, viewModel::merge, viewModel::replaceAll)
}

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    backupManager: BackupManager,
    onSaveSemester: (Semester) -> Unit,
    onSetCurrentSemester: (Long) -> Unit,
    onDeleteSemester: (Long) -> Unit,
    onSavePresets: (List<QuickPreset>) -> Unit,
    onRestorePresets: () -> Unit,
    onMerge: (BackupPayload) -> Unit,
    onReplace: (BackupPayload) -> Unit,
) {
    var semesterDialog by remember { mutableStateOf<Semester?>(null) }
    var newSemesterDialog by remember { mutableStateOf(false) }
    var showDeleteId by remember { mutableStateOf<Long?>(null) }
    var presetDialog by remember { mutableStateOf<QuickPreset?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var importPayload by remember { mutableStateOf<BackupPayload?>(null) }
    var replacePayload by remember { mutableStateOf<BackupPayload?>(null) }
    val scope = rememberCoroutineScope()
    val exportJson = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? -> uri?.let { runCatching { backupManager.exportJson(it, state.records, state.semesters, state.presets) }.onFailure { errorMessage = "导出备份失败" } } }
    val exportCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? -> uri?.let { runCatching { backupManager.exportCsv(it, state.records, state.semesters) }.onFailure { errorMessage = "导出 CSV 失败" } } }
    val importJson = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? -> uri?.let { selected -> scope.launch { runCatching { withContext(Dispatchers.IO) { backupManager.importJson(selected) } }.onSuccess { importPayload = it }.onFailure { errorMessage = it.message ?: "备份文件格式不正确" } } } }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { TopAppBar(title = { Column { Text("小尘课簿", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary); Text("设置", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent)) },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { SectionTitle("当前学期") }
            item {
                val current = state.semesters.firstOrNull { it.isCurrent }
                if (current == null) Text("尚未设置当前学期", color = MaterialTheme.colorScheme.onSurfaceVariant)
                else Column { Text(current.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); Text("${dateText(current.startEpochDay)} — ${dateText(current.endEpochDay)}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                OutlinedButton(onClick = { newSemesterDialog = true }) { Text("新建学期") }
            }
            items(state.semesters) { semester ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) { Text(semester.name); Text("${dateText(semester.startEpochDay)} — ${dateText(semester.endEpochDay)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    if (!semester.isCurrent) TextButton(onClick = { onSetCurrentSemester(semester.id) }) { Text("设为当前") }
                    TextButton(onClick = { semesterDialog = semester }) { Text("编辑") }
                    TextButton(onClick = { showDeleteId = semester.id }) { Text("删除") }
                }
            }
            item { SectionTitle("快速记录") }
            items(state.presets) { preset ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("${preset.name}（${preset.lessonCount} / ${preset.eveningSupportCount}）"); TextButton(onClick = { presetDialog = preset }) { Text("编辑") } }
            }
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = onRestorePresets) { Text("恢复默认") }; if (state.presets.size < 6) OutlinedButton(onClick = { presetDialog = QuickPreset((state.presets.maxOfOrNull { it.id } ?: -1) + 1, "常用组合", 0, 0) }) { Text("新增") } } }
            item { SectionTitle("数据") }
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { exportJson.launch("xiaocheng-kebu-backup.json") }) { Text("导出完整备份") }; OutlinedButton(onClick = { importJson.launch(arrayOf("application/json")) }) { Text("导入") } } }
            item { OutlinedButton(onClick = { exportCsv.launch("xiaocheng-kebu-records.csv") }) { Text("导出 CSV") } }
            item { SectionTitle("关于") }
            item { Text("小尘课簿\nVersion 1.0.0\n一个安静、离线的教师课时记录工具。", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    semesterDialog?.let { semester ->
        SemesterDialog(semester, onDismiss = { semesterDialog = null }, onSave = { onSaveSemester(it); semesterDialog = null })
    }
    if (newSemesterDialog) {
        SemesterDialog(null, onDismiss = { newSemesterDialog = false }, onSave = { onSaveSemester(it); newSemesterDialog = false })
    }
    presetDialog?.let { preset -> PresetDialog(preset, onDismiss = { presetDialog = null }, onSave = { list -> onSavePresets((state.presets.filterNot { it.id == preset.id } + list).sortedBy { it.id }); presetDialog = null }) }
    showDeleteId?.let { id -> AlertDialog(onDismissRequest = { showDeleteId = null }, title = { Text("删除学期") }, text = { Text("删除学期不会删除课时记录。") }, confirmButton = { TextButton(onClick = { onDeleteSemester(id); showDeleteId = null }) { Text("删除") } }, dismissButton = { TextButton(onClick = { showDeleteId = null }) { Text("取消") } }) }
    importPayload?.let { payload -> AlertDialog(onDismissRequest = { importPayload = null }, title = { Text("导入备份") }, text = { Text("选择合并或覆盖当前数据。覆盖会清空当前课时和学期。") }, confirmButton = { TextButton(onClick = { onMerge(payload); importPayload = null }) { Text("合并") } }, dismissButton = { Row { TextButton(onClick = { replacePayload = payload; importPayload = null }) { Text("覆盖") }; TextButton(onClick = { importPayload = null }) { Text("取消") } } }) }
    replacePayload?.let { payload -> AlertDialog(onDismissRequest = { replacePayload = null }, title = { Text("确认覆盖") }, text = { Text("这会清空当前课时、学期和常用组合，且不可撤销。确定继续吗？") }, confirmButton = { TextButton(onClick = { onReplace(payload); replacePayload = null }) { Text("确认覆盖") } }, dismissButton = { TextButton(onClick = { replacePayload = null }) { Text("取消") } }) }
    errorMessage?.let { message -> AlertDialog(onDismissRequest = { errorMessage = null }, title = { Text("操作失败") }, text = { Text(message) }, confirmButton = { TextButton(onClick = { errorMessage = null }) { Text("知道了") } }) }
}

@Composable
private fun SectionTitle(text: String) { Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)) }

@Composable
private fun SemesterDialog(initial: Semester?, onDismiss: () -> Unit, onSave: (Semester) -> Unit) {
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var start by remember(initial) { mutableStateOf(initial?.let { dateText(it.startEpochDay) } ?: LocalDate.now().withDayOfMonth(1).toString()) }
    var end by remember(initial) { mutableStateOf(initial?.let { dateText(it.endEpochDay) } ?: LocalDate.now().plusMonths(4).withDayOfMonth(1).minusDays(1).toString()) }
    var isCurrent by remember(initial) { mutableStateOf(initial?.isCurrent ?: false) }
    var error by remember { mutableStateOf<String?>(null) }
    var pickerField by remember { mutableStateOf<SemesterDateField?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "新建学期" else "编辑学期") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("名称") }, singleLine = true)
                OutlinedTextField(
                    start,
                    { start = it },
                    label = { Text("开始日期 yyyy-MM-dd") },
                    singleLine = true,
                    trailingIcon = { TextButton(onClick = { pickerField = SemesterDateField.START }) { Text("选择") } },
                )
                OutlinedTextField(
                    end,
                    { end = it },
                    label = { Text("结束日期 yyyy-MM-dd") },
                    singleLine = true,
                    trailingIcon = { TextButton(onClick = { pickerField = SemesterDateField.END }) { Text("选择") } },
                )
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Checkbox(checked = isCurrent, onCheckedChange = { isCurrent = it }); Text("设为当前学期") }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                runCatching {
                    val s = LocalDate.parse(start)
                    val e = LocalDate.parse(end)
                    if (name.isBlank() || s > e) error("请填写合法的学期信息")
                    else onSave(Semester(initial?.id ?: 0L, name.trim(), s.toEpochDay(), e.toEpochDay(), isCurrent, initial?.createdAt ?: System.currentTimeMillis()))
                }.onFailure { error = "日期格式应为 yyyy-MM-dd" }
            }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
    pickerField?.let { field ->
        key(field) {
            val currentDate = runCatching { LocalDate.parse(if (field == SemesterDateField.START) start else end) }.getOrNull() ?: LocalDate.now()
            val pickerState = androidx.compose.material3.rememberDatePickerState(
                initialSelectedDateMillis = currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            )
            DatePickerDialog(
                onDismissRequest = { pickerField = null },
                confirmButton = {
                    TextButton(onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val selected = java.time.Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().toString()
                            if (field == SemesterDateField.START) start = selected else end = selected
                        }
                        pickerField = null
                    }) { Text("确定") }
                },
                dismissButton = { TextButton(onClick = { pickerField = null }) { Text("取消") } },
            ) { DatePicker(state = pickerState) }
        }
    }
}

private enum class SemesterDateField { START, END }

@Composable
private fun PresetDialog(initial: QuickPreset, onDismiss: () -> Unit, onSave: (QuickPreset) -> Unit) {
    var name by remember(initial) { mutableStateOf(initial.name) }
    var lessons by remember(initial) { mutableStateOf(initial.lessonCount.toString()) }
    var evening by remember(initial) { mutableStateOf(initial.eveningSupportCount.toString()) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("编辑常用组合") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(name, { name = it }, label = { Text("名称") }, singleLine = true); OutlinedTextField(lessons, { lessons = it.filter(Char::isDigit) }, label = { Text("授课节数") }, singleLine = true); OutlinedTextField(evening, { evening = it.filter(Char::isDigit) }, label = { Text("晚辅节数") }, singleLine = true) } }, confirmButton = { TextButton(onClick = { onSave(initial.copy(name = name.ifBlank { "常用组合" }, lessonCount = lessons.toIntOrNull() ?: 0, eveningSupportCount = evening.toIntOrNull() ?: 0)) }) { Text("保存") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } })
}

private fun dateText(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).toString()
