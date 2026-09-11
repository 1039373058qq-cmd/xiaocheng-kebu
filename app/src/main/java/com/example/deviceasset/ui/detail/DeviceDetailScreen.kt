package com.example.deviceasset.ui.detail

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deviceasset.domain.model.Device
import com.example.deviceasset.domain.model.DeviceMetrics
import com.example.deviceasset.domain.model.DeviceStatus
import com.example.deviceasset.domain.repository.DeviceRepository
import com.example.deviceasset.ui.theme.InkWashCard
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val DetailDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日")

@Composable
fun DeviceDetailRoute(
    repository: DeviceRepository,
    deviceId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
) {
    val viewModel: DeviceDetailViewModel = viewModel(
        key = "device-detail-$deviceId",
        factory = DeviceDetailViewModel.Factory(repository, deviceId),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.deleted) {
        if (state.deleted) onDeleted()
    }

    DeviceDetailScreen(
        state = state,
        onBack = onBack,
        onEdit = { state.device?.id?.let(onEdit) },
        onRetire = viewModel::retire,
        onSell = viewModel::sell,
        onDelete = viewModel::delete,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    state: DeviceDetailUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onRetire: () -> Unit,
    onSell: (LocalDate, Long) -> Unit,
    onDelete: () -> Unit,
) {
    var showSellSheet by remember { mutableStateOf(false) }
    var showRetireConfirmation by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val device = state.device

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("设备详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (device != null) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Outlined.Edit, contentDescription = "编辑设备")
                        }
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { paddingValues ->
        when {
            state.isLoading -> LoadingDetail(paddingValues = paddingValues)
            device == null -> NotFoundDetail(paddingValues = paddingValues)
            else -> DeviceDetailContent(
                device = device,
                metrics = state.metrics,
                isSaving = state.isSaving,
                actionError = state.actionError,
                paddingValues = paddingValues,
                onRetire = { showRetireConfirmation = true },
                onSell = { showSellSheet = true },
                onDelete = { showDeleteConfirmation = true },
            )
        }
    }

    if (showSellSheet && device != null) {
        SellDeviceSheet(
            device = device,
            isSaving = state.isSaving,
            onDismiss = { showSellSheet = false },
            onConfirm = { date, price ->
                onSell(date, price)
                showSellSheet = false
            },
        )
    }

    if (showRetireConfirmation) {
        AlertDialog(
            onDismissRequest = { showRetireConfirmation = false },
            title = { Text("退役设备") },
            text = { Text("退役后设备仍属于当前资产，并继续计算到今天的持有成本。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRetireConfirmation = false
                        onRetire()
                    },
                ) { Text("确认退役") }
            },
            dismissButton = {
                TextButton(onClick = { showRetireConfirmation = false }) { Text("取消") }
            },
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("删除设备") },
            text = { Text("删除后设备记录将从本机移除，且无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                ) { Text("确认删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun LoadingDetail(paddingValues: androidx.compose.foundation.layout.PaddingValues) {
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
}

@Composable
private fun NotFoundDetail(paddingValues: androidx.compose.foundation.layout.PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("设备不存在", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun DeviceDetailContent(
    device: Device,
    metrics: DeviceMetrics?,
    isSaving: Boolean,
    actionError: String?,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    onRetire: () -> Unit,
    onSell: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = listOfNotNull(device.brand, device.model, device.storage).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusBadge(device.status)
        }

        MetricsCard(device = device, metrics = metrics)

        DetailRow("分类", device.category.label())
        DetailRow("购买日期", device.purchaseDate.format(DetailDateFormatter))
        DetailRow("购买价格", formatCny(device.purchasePriceCents))
        device.retiredDate?.let { DetailRow("退役日期", it.format(DetailDateFormatter)) }
        device.soldDate?.let { DetailRow("出售日期", it.format(DetailDateFormatter)) }
        device.soldPriceCents?.let { DetailRow("出售价格", formatCny(it)) }
        if (device.additionalCostCents > 0L) {
            DetailRow("后续支出", formatCny(device.additionalCostCents))
        }
        device.note?.takeIf { it.isNotBlank() }?.let { note ->
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text("备注", style = MaterialTheme.typography.titleSmall)
            Text(note, style = MaterialTheme.typography.bodyMedium)
        }

        actionError?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        if (device.status != DeviceStatus.SOLD) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (device.status == DeviceStatus.ACTIVE) {
                    OutlinedButton(
                        onClick = onRetire,
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.extraLarge,
                    ) { Text("退役") }
                }
                Button(
                    onClick = onSell,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.extraLarge,
                ) { Text("出售") }
            }
        }
        OutlinedButton(
            onClick = onDelete,
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("删除设备")
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MetricsCard(device: Device, metrics: DeviceMetrics?) {
    InkWashCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("生命周期成本", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricValue(
                    label = "持有时间",
                    value = metrics?.let { "${it.ownershipDays} 天" } ?: "—",
                    modifier = Modifier.weight(1f),
                )
                MetricValue(
                    label = "累计成本",
                    value = metrics?.let { formatCny(it.actualCostCents) } ?: "—",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(14.dp))
            MetricValue(
                label = "日均成本",
                value = metrics?.let { "${formatYuan(it.dailyCost)} / 天" } ?: "—",
            )
        }
    }
}

@Composable
private fun MetricValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatusBadge(status: DeviceStatus) {
    val (label, color, onColor) = when (status) {
        DeviceStatus.ACTIVE -> Triple("服役中", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        DeviceStatus.RETIRED -> Triple("已退役", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        DeviceStatus.SOLD -> Triple("已卖出", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Surface(shape = MaterialTheme.shapes.extraLarge, color = color) {
        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = onColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellDeviceSheet(
    device: Device,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, Long) -> Unit,
) {
    var soldDate by remember(device.id) { mutableStateOf(LocalDate.now()) }
    var soldPrice by remember(device.id) { mutableStateOf("") }
    var error by remember(device.id) { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("出售设备", style = MaterialTheme.typography.headlineSmall)
            Text("出售后将按出售日期重新计算生命周期成本。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("出售日期")
                    Text(soldDate.format(DetailDateFormatter))
                }
            }
            OutlinedTextField(
                value = soldPrice,
                onValueChange = { next ->
                    if (next.count { it == '.' } <= 1 && next.all { it.isDigit() || it == '.' }) {
                        soldPrice = next
                        error = null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("出售价格（元）") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = error != null,
                supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.tertiary,
                ),
            )
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = {
                    val cents = parseMoney(soldPrice)
                    if (cents == null) {
                        error = "请输入有效的出售价格"
                    } else if (soldDate.isBefore(device.purchaseDate)) {
                        error = "出售日期不能早于购买日期"
                    } else {
                        onConfirm(soldDate, cents)
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("确认出售")
                }
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = soldDate.toUtcMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { soldDate = it.toLocalDate() }
                        showDatePicker = false
                    },
                ) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } },
        ) { DatePicker(state = pickerState, showModeToggle = false) }
    }
}

private fun DeviceStatus.label(): String = when (this) {
    DeviceStatus.ACTIVE -> "服役中"
    DeviceStatus.RETIRED -> "已退役"
    DeviceStatus.SOLD -> "已卖出"
}

private fun com.example.deviceasset.domain.model.DeviceCategory.label(): String = when (this) {
    com.example.deviceasset.domain.model.DeviceCategory.PHONE -> "手机"
    com.example.deviceasset.domain.model.DeviceCategory.TABLET -> "平板"
    com.example.deviceasset.domain.model.DeviceCategory.LAPTOP -> "笔记本电脑"
    com.example.deviceasset.domain.model.DeviceCategory.DESKTOP -> "台式电脑"
    com.example.deviceasset.domain.model.DeviceCategory.WATCH -> "手表"
    com.example.deviceasset.domain.model.DeviceCategory.HEADPHONE -> "耳机"
    com.example.deviceasset.domain.model.DeviceCategory.CAMERA -> "相机"
    com.example.deviceasset.domain.model.DeviceCategory.GAME_CONSOLE -> "其他设备"
    com.example.deviceasset.domain.model.DeviceCategory.OTHER -> "其他设备"
    com.example.deviceasset.domain.model.DeviceCategory.SUV -> "汽车"
    com.example.deviceasset.domain.model.DeviceCategory.ELECTRIC_BIKE -> "电动车"
    com.example.deviceasset.domain.model.DeviceCategory.SWITCH -> "Switch"
    com.example.deviceasset.domain.model.DeviceCategory.PS5 -> "PS5"
    com.example.deviceasset.domain.model.DeviceCategory.XBOX -> "Xbox"
    com.example.deviceasset.domain.model.DeviceCategory.GIMBAL_CAMERA -> "大疆云台相机"
    com.example.deviceasset.domain.model.DeviceCategory.ELLIPTICAL -> "椭圆机"
    com.example.deviceasset.domain.model.DeviceCategory.FLOOR_CLEANER -> "洗地机器人"
}

private fun LocalDate.toUtcMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private fun parseMoney(text: String): Long? = runCatching {
    BigDecimal(text.trim())
        .movePointRight(2)
        .setScale(0, RoundingMode.UNNECESSARY)
        .longValueExact()
}.getOrNull()

private fun formatCny(cents: Long): String =
    "¥" + BigDecimal.valueOf(cents, 2).setScale(2, RoundingMode.HALF_UP).toPlainString()

private fun formatYuan(amount: BigDecimal): String =
    "¥" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString()
