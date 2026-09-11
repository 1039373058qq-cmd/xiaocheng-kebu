package com.example.deviceasset.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.DevicesOther
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Tablet
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.annotation.DrawableRes
import com.example.deviceasset.R
import com.example.deviceasset.domain.model.DeviceCategory
import com.example.deviceasset.domain.model.DeviceSort
import com.example.deviceasset.domain.model.DeviceStatus
import com.example.deviceasset.domain.model.AssetSummary
import com.example.deviceasset.domain.repository.DeviceRepository
import com.example.deviceasset.ui.theme.InkWashCard
import com.example.deviceasset.ui.theme.InkBrushTitle
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun DeviceListRoute(
    repository: DeviceRepository,
    onAddDevice: () -> Unit = {},
    onDeviceClick: (Long) -> Unit = {},
) {
    val viewModel: DeviceListViewModel = viewModel(
        factory = DeviceListViewModel.Factory(repository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DeviceListScreen(
        state = state,
        onFilterSelected = viewModel::setFilter,
        onSortSelected = viewModel::setSort,
        onToggleSortDirection = viewModel::toggleSortDirection,
        onAddDevice = onAddDevice,
        onDeviceClick = onDeviceClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceListScreen(
    state: DeviceListUiState,
    onFilterSelected: (DeviceStatusFilter) -> Unit,
    onSortSelected: (DeviceSort) -> Unit,
    onToggleSortDirection: () -> Unit,
    onAddDevice: () -> Unit = {},
    onDeviceClick: (Long) -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Image(
            painter = painterResource(R.drawable.ink_wash_home_overlay),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
            alpha = 0.72f,
        )
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            InkBrushTitle(
                                text = "小尘记账",
                                style = MaterialTheme.typography.labelLarge,
                                brushColor = MaterialTheme.colorScheme.secondary,
                            )
                            InkBrushTitle(
                                text = "设备",
                                style = MaterialTheme.typography.displaySmall,
                                brushColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.72f),
                            )
                        }
                    },
                    actions = {
                        SortMenu(
                            selectedSort = state.sort,
                            descending = state.descending,
                            onSortSelected = onSortSelected,
                            onToggleSortDirection = onToggleSortDirection,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    ),
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddDevice,
                    modifier = Modifier.semantics {
                        contentDescription = "新增设备"
                    },
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                }
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                AssetSummaryCard(summary = state.summary)
                StatusFilterRow(
                    selectedFilter = state.filter,
                    summary = state.summary,
                    onFilterSelected = onFilterSelected,
                )
                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("正在读取设备数据…")
                    }
                } else if (state.items.isEmpty()) {
                    EmptyDeviceState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = state.items,
                            key = { it.device.id },
                        ) { item ->
                            DeviceCard(item, onClick = { onDeviceClick(item.device.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssetSummaryCard(summary: AssetSummary) {
    InkWashCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("资产总览", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "${summary.totalDeviceCount} 台设备",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryMetric(
                    label = "当前持有价值",
                    value = summary.currentEstimatedValueCents?.let(::formatCny) ?: "未估值",
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(16.dp))
                SummaryMetric(
                    label = "日均成本",
                    value = formatYuan(summary.currentDailyCost),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatusSummary("服役中", summary.activeCount, MaterialTheme.colorScheme.primary)
                StatusSummary(
                    "已退役",
                    summary.retiredCount + summary.soldCount,
                    MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatusSummary(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = color, shape = androidx.compose.foundation.shape.CircleShape),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "$label $count",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatusFilterRow(
    selectedFilter: DeviceStatusFilter,
    summary: AssetSummary,
    onFilterSelected: (DeviceStatusFilter) -> Unit,
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedFilter == DeviceStatusFilter.ACTIVE,
            onClick = { onFilterSelected(DeviceStatusFilter.ACTIVE) },
            label = { Text("服役中 ${summary.activeCount}") },
            colors = filterChipColors(),
        )
        FilterChip(
            selected = selectedFilter == DeviceStatusFilter.RETIRED,
            onClick = { onFilterSelected(DeviceStatusFilter.RETIRED) },
            label = { Text("已退役 ${summary.retiredCount + summary.soldCount}") },
            colors = filterChipColors(),
        )
    }
}

@Composable
private fun filterChipColors() = androidx.compose.material3.FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
    selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
)

@Composable
private fun SortMenu(
    selectedSort: DeviceSort,
    descending: Boolean,
    onSortSelected: (DeviceSort) -> Unit,
    onToggleSortDirection: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Outlined.MoreVert, contentDescription = "排序")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DeviceSort.entries.forEach { sort ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (selectedSort == sort) "✓ ${sort.label()}" else sort.label(),
                        )
                    },
                    onClick = {
                        onSortSelected(sort)
                        expanded = false
                    },
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(if (descending) "降序排列" else "升序排列") },
                leadingIcon = {
                    Icon(
                        imageVector = if (descending) Icons.Outlined.ArrowDownward else Icons.Outlined.ArrowUpward,
                        contentDescription = null,
                    )
                },
                onClick = {
                    onToggleSortDirection()
                    expanded = false
                },
            )
        }
    }
}

private fun DeviceSort.label(): String = when (this) {
    DeviceSort.PURCHASE_DATE -> "时间（购买/持有）"
    DeviceSort.PURCHASE_PRICE -> "设备价格"
    DeviceSort.DAILY_COST -> "日均成本"
    DeviceSort.NAME -> "设备名称"
}

@Composable
private fun DeviceCard(item: DeviceListItem, onClick: () -> Unit) {
    val device = item.device
    InkWashCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 238.dp)
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                DeviceIllustration(
                    category = device.category,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(104.dp),
                ) {
                    StatusChip(
                        status = device.status,
                        modifier = Modifier.align(Alignment.TopEnd),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = device.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${formatCny(device.purchasePriceCents)} · ${item.metrics.ownershipDays}天",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "${formatYuan(item.metrics.dailyCost)}/天",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (item.metrics.netCostCents < 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

@Composable
private fun StatusChip(status: DeviceStatus, modifier: Modifier = Modifier) {
    val (label, containerColor, contentColor) = when (status) {
        DeviceStatus.ACTIVE -> Triple("服役中", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        DeviceStatus.RETIRED -> Triple("已退役", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        DeviceStatus.SOLD -> Triple("已卖出", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}

@Composable
private fun DeviceIllustration(
    category: DeviceCategory,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val illustrationRes = category.illustrationRes()
    Box(modifier = modifier) {
        if (illustrationRes != null) {
            Image(
                painter = painterResource(illustrationRes),
                contentDescription = category.label(),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        } else {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = category.icon(),
                        contentDescription = category.label(),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
        overlay()
    }
}

@DrawableRes
private fun DeviceCategory.illustrationRes(): Int? = when (this) {
    DeviceCategory.PHONE -> R.drawable.device_phone
    DeviceCategory.TABLET -> R.drawable.device_tablet
    DeviceCategory.LAPTOP -> R.drawable.device_laptop
    DeviceCategory.DESKTOP -> R.drawable.device_desktop
    DeviceCategory.CAMERA -> R.drawable.device_camera
    DeviceCategory.WATCH -> R.drawable.device_watch
    DeviceCategory.HEADPHONE -> R.drawable.device_earbuds
    DeviceCategory.SUV -> R.drawable.device_suv
    DeviceCategory.ELECTRIC_BIKE -> R.drawable.device_electric_bike
    DeviceCategory.SWITCH -> R.drawable.device_switch
    DeviceCategory.PS5 -> R.drawable.device_ps5
    DeviceCategory.XBOX -> R.drawable.device_xbox
    DeviceCategory.GIMBAL_CAMERA -> R.drawable.device_gimbal_camera
    DeviceCategory.ELLIPTICAL -> R.drawable.device_elliptical
    DeviceCategory.FLOOR_CLEANER -> R.drawable.device_floor_cleaner
    DeviceCategory.GAME_CONSOLE,
    DeviceCategory.OTHER -> null
}

@Composable
private fun EmptyDeviceState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.DevicesOther,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Text("暂无设备", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "点击右下角按钮添加你的第一台设备",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

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

private fun DeviceCategory.icon(): ImageVector = when (this) {
    DeviceCategory.PHONE -> Icons.Outlined.PhoneAndroid
    DeviceCategory.TABLET -> Icons.Outlined.Tablet
    DeviceCategory.LAPTOP -> Icons.Outlined.Laptop
    DeviceCategory.DESKTOP -> Icons.Outlined.Computer
    DeviceCategory.WATCH -> Icons.Outlined.Watch
    DeviceCategory.HEADPHONE -> Icons.Outlined.Headphones
    DeviceCategory.CAMERA -> Icons.Outlined.CameraAlt
    DeviceCategory.GAME_CONSOLE -> Icons.Outlined.DevicesOther
    DeviceCategory.OTHER -> Icons.Outlined.DevicesOther
    DeviceCategory.SUV,
    DeviceCategory.ELECTRIC_BIKE,
    DeviceCategory.SWITCH,
    DeviceCategory.PS5,
    DeviceCategory.XBOX,
    DeviceCategory.GIMBAL_CAMERA,
    DeviceCategory.ELLIPTICAL,
    DeviceCategory.FLOOR_CLEANER -> Icons.Outlined.DevicesOther
}

private fun formatCny(cents: Long): String =
    "¥" + BigDecimal.valueOf(cents, 2).setScale(2, RoundingMode.HALF_UP).toPlainString()

private fun formatYuan(amount: BigDecimal): String =
    "¥" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString()
