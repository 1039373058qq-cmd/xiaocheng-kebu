package com.example.deviceasset.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deviceasset.domain.model.DeviceCategory
import com.example.deviceasset.domain.model.DeviceStatus
import com.example.deviceasset.domain.repository.DeviceRepository
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.collectLatest

private val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日")

private val DeviceCategoryOptions: List<DeviceCategory> = buildList {
    addAll(DeviceCategory.entries.filter { it != DeviceCategory.GAME_CONSOLE && it != DeviceCategory.OTHER })
    add(DeviceCategory.OTHER)
}

@Composable
fun DeviceEditRoute(
    repository: DeviceRepository,
    deviceId: Long?,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
) {
    val viewModel: DeviceEditViewModel = viewModel(
        key = "device-edit-${deviceId ?: "new"}",
        factory = DeviceEditViewModel.Factory(repository, deviceId),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.uiState.collectLatest { uiState ->
            if (uiState.saved) onSaved()
        }
    }

    DeviceEditScreen(
        state = state,
        onNameChanged = viewModel::onNameChanged,
        onCategoryChanged = viewModel::onCategoryChanged,
        onBrandChanged = viewModel::onBrandChanged,
        onModelChanged = viewModel::onModelChanged,
        onStorageChanged = viewModel::onStorageChanged,
        onPurchaseDateChanged = viewModel::onPurchaseDateChanged,
        onPurchasePriceChanged = viewModel::onPurchasePriceChanged,
        onStatusChanged = viewModel::onStatusChanged,
        onRetiredDateChanged = viewModel::onRetiredDateChanged,
        onSoldDateChanged = viewModel::onSoldDateChanged,
        onSoldPriceChanged = viewModel::onSoldPriceChanged,
        onAdditionalCostChanged = viewModel::onAdditionalCostChanged,
        onEstimatedValueChanged = viewModel::onEstimatedValueChanged,
        onNoteChanged = viewModel::onNoteChanged,
        onSave = viewModel::save,
        onCancel = onCancel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceEditScreen(
    state: DeviceEditUiState,
    onNameChanged: (String) -> Unit,
    onCategoryChanged: (DeviceCategory) -> Unit,
    onBrandChanged: (String) -> Unit,
    onModelChanged: (String) -> Unit,
    onStorageChanged: (String) -> Unit,
    onPurchaseDateChanged: (LocalDate) -> Unit,
    onPurchasePriceChanged: (String) -> Unit,
    onStatusChanged: (DeviceStatus) -> Unit,
    onRetiredDateChanged: (LocalDate) -> Unit,
    onSoldDateChanged: (LocalDate) -> Unit,
    onSoldPriceChanged: (String) -> Unit,
    onAdditionalCostChanged: (String) -> Unit,
    onEstimatedValueChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (state.deviceId == null) "新增设备" else "编辑设备") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            Button(
                onClick = onSave,
                enabled = !state.isLoading && !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("保存设备")
                }
            }
        },
    ) { paddingValues ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("正在读取设备数据…")
            }
        } else {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                state.formError?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                FormSectionTitle("基本信息")
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("设备名称 *") },
                    singleLine = true,
                    isError = state.errorFor(DeviceEditField.NAME) != null,
                    supportingText = { FieldError(state.errorFor(DeviceEditField.NAME)) },
                    colors = inkFieldColors(),
                )
                SelectionField(
                    label = "分类",
                    value = state.category.label(),
                    options = DeviceCategoryOptions,
                    optionLabel = { it.label() },
                    onSelected = onCategoryChanged,
                )
                OutlinedTextField(
                    value = state.brand,
                    onValueChange = onBrandChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("品牌") },
                    singleLine = true,
                    colors = inkFieldColors(),
                )
                OutlinedTextField(
                    value = state.model,
                    onValueChange = onModelChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("型号") },
                    singleLine = true,
                    colors = inkFieldColors(),
                )
                OutlinedTextField(
                    value = state.storage,
                    onValueChange = onStorageChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("容量") },
                    singleLine = true,
                    colors = inkFieldColors(),
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                FormSectionTitle("购买与成本")
                DateField(
                    label = "购买日期 *",
                    date = state.purchaseDate,
                    error = state.errorFor(DeviceEditField.PURCHASE_DATE),
                    onDateSelected = onPurchaseDateChanged,
                )
                MoneyField(
                    value = state.purchasePrice,
                    onValueChange = onPurchasePriceChanged,
                    label = "购买价格（元）*",
                    error = state.errorFor(DeviceEditField.PURCHASE_PRICE),
                )
                MoneyField(
                    value = state.additionalCost,
                    onValueChange = onAdditionalCostChanged,
                    label = "后续支出（元）",
                    error = state.errorFor(DeviceEditField.ADDITIONAL_COST),
                )
                MoneyField(
                    value = state.estimatedCurrentValue,
                    onValueChange = onEstimatedValueChanged,
                    label = "当前估值（元）",
                    error = state.errorFor(DeviceEditField.ESTIMATED_VALUE),
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                FormSectionTitle("生命周期")
                SelectionField(
                    label = "设备状态",
                    value = state.status.label(),
                    options = DeviceStatus.entries,
                    optionLabel = { it.label() },
                    onSelected = onStatusChanged,
                )
                when (state.status) {
                    DeviceStatus.ACTIVE -> Unit
                    DeviceStatus.RETIRED -> DateField(
                        label = "退役日期 *",
                        date = state.retiredDate ?: state.purchaseDate,
                        error = state.errorFor(DeviceEditField.RETIRED_DATE),
                        onDateSelected = onRetiredDateChanged,
                    )

                    DeviceStatus.SOLD -> {
                        DateField(
                            label = "出售日期 *",
                            date = state.soldDate ?: state.purchaseDate,
                            error = state.errorFor(DeviceEditField.SOLD_DATE),
                            onDateSelected = onSoldDateChanged,
                        )
                        MoneyField(
                            value = state.soldPrice,
                            onValueChange = onSoldPriceChanged,
                            label = "出售价格（元）*",
                            error = state.errorFor(DeviceEditField.SOLD_PRICE),
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                FormSectionTitle("备注")
                OutlinedTextField(
                    value = state.note,
                    onValueChange = onNoteChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("备注") },
                    minLines = 3,
                    maxLines = 5,
                    colors = inkFieldColors(),
                )
                Spacer(Modifier.height(84.dp))
            }
        }
    }
}

@Composable
private fun FormSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun inkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    cursorColor = MaterialTheme.colorScheme.tertiary,
)

@Composable
private fun MoneyField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { next ->
            if (next.count { it == '.' } <= 1 && next.all { it.isDigit() || it == '.' }) {
                onValueChange(next)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        isError = error != null,
        supportingText = { FieldError(error) },
        colors = inkFieldColors(),
    )
}

@Composable
private fun FieldError(error: String?) {
    if (error != null) {
        Text(error, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun <T> SelectionField(
    label: String,
    value: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(label, style = MaterialTheme.typography.labelSmall)
                    Text(
                        value,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text("⌄", style = MaterialTheme.typography.titleMedium)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    date: LocalDate,
    error: String?,
    onDateSelected: (LocalDate) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    OutlinedButton(
        onClick = { showPicker = true },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label)
            Text(date.format(DateFormatter))
        }
    }
    FieldError(error)

    if (showPicker) {
        val pickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = date.toUtcMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(millis.toLocalDate())
                        }
                        showPicker = false
                    },
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("取消") }
            },
        ) {
            DatePicker(state = pickerState, showModeToggle = false)
        }
    }
}

private fun LocalDate.toUtcMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDate(): LocalDate =
    java.time.Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private fun DeviceCategory.label(): String = when (this) {
    DeviceCategory.PHONE -> "手机"
    DeviceCategory.TABLET -> "平板"
    DeviceCategory.LAPTOP -> "笔记本电脑"
    DeviceCategory.DESKTOP -> "台式电脑"
    DeviceCategory.WATCH -> "手表"
    DeviceCategory.HEADPHONE -> "耳机"
    DeviceCategory.CAMERA -> "相机"
    DeviceCategory.GAME_CONSOLE -> "其他设备"
    DeviceCategory.OTHER -> "其他设备"
    DeviceCategory.SUV -> "汽车"
    DeviceCategory.ELECTRIC_BIKE -> "电动车"
    DeviceCategory.SWITCH -> "Switch"
    DeviceCategory.PS5 -> "PS5"
    DeviceCategory.XBOX -> "Xbox"
    DeviceCategory.GIMBAL_CAMERA -> "大疆云台相机"
    DeviceCategory.ELLIPTICAL -> "椭圆机"
    DeviceCategory.FLOOR_CLEANER -> "洗地机器人"
}

private fun DeviceStatus.label(): String = when (this) {
    DeviceStatus.ACTIVE -> "服役中"
    DeviceStatus.RETIRED -> "已退役"
    DeviceStatus.SOLD -> "已卖出"
}

private fun DeviceEditUiState.errorFor(field: DeviceEditField): String? = fieldErrors[field]
