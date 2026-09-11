package com.example.deviceasset.ui.home

import com.example.deviceasset.domain.model.AssetSummary
import com.example.deviceasset.domain.model.Device
import com.example.deviceasset.domain.model.DeviceMetrics
import com.example.deviceasset.domain.model.DeviceSort
import com.example.deviceasset.domain.model.DeviceStatus
import java.math.BigDecimal

enum class DeviceStatusFilter {
    ACTIVE,
    RETIRED,
}

data class DeviceListItem(
    val device: Device,
    val metrics: DeviceMetrics,
)

data class DeviceListUiState(
    val items: List<DeviceListItem> = emptyList(),
    val summary: AssetSummary = AssetSummary(
        totalDeviceCount = 0,
        activeCount = 0,
        retiredCount = 0,
        soldCount = 0,
        historicalPurchaseTotalCents = 0L,
        currentHoldingPurchaseTotalCents = 0L,
        currentEstimatedValueCents = null,
        historicalNetCostCents = 0L,
        currentDailyCost = BigDecimal.ZERO.setScale(2),
    ),
    val filter: DeviceStatusFilter = DeviceStatusFilter.ACTIVE,
    val sort: DeviceSort = DeviceSort.PURCHASE_DATE,
    val descending: Boolean = true,
    val isLoading: Boolean = true,
)

fun DeviceStatusFilter.matches(device: Device): Boolean = when (this) {
    DeviceStatusFilter.ACTIVE -> device.status == DeviceStatus.ACTIVE
    DeviceStatusFilter.RETIRED -> device.status == DeviceStatus.RETIRED || device.status == DeviceStatus.SOLD
}
