package com.example.deviceasset.domain.model

import java.math.BigDecimal

data class DeviceMetrics(
    val ownershipDays: Long,
    val investedCostCents: Long,
    val netCostCents: Long,
    val dailyCost: BigDecimal,
) {
    /** Net lifecycle cost; for sold devices this is purchase + extras - sale price. */
    val actualCostCents: Long get() = netCostCents
}

data class AssetSummary(
    val totalDeviceCount: Int,
    val activeCount: Int,
    val retiredCount: Int,
    val soldCount: Int,
    val historicalPurchaseTotalCents: Long,
    val currentHoldingPurchaseTotalCents: Long,
    val currentEstimatedValueCents: Long?,
    val historicalNetCostCents: Long,
    val currentDailyCost: BigDecimal,
)
