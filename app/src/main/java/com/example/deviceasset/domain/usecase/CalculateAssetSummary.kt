package com.example.deviceasset.domain.usecase

import com.example.deviceasset.domain.model.AssetSummary
import com.example.deviceasset.domain.model.Device
import com.example.deviceasset.domain.model.DeviceStatus
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

class CalculateAssetSummary(
    private val calculateDeviceMetrics: CalculateDeviceMetrics = CalculateDeviceMetrics(),
) {
    operator fun invoke(devices: List<Device>, today: LocalDate? = null): AssetSummary {
        val activeCount = devices.count { it.status == DeviceStatus.ACTIVE }
        val retiredCount = devices.count { it.status == DeviceStatus.RETIRED }
        val soldCount = devices.count { it.status == DeviceStatus.SOLD }
        val currentHoldings = devices.filter {
            it.status == DeviceStatus.ACTIVE || it.status == DeviceStatus.RETIRED
        }

        val metrics = devices.map { device ->
            if (today == null) calculateDeviceMetrics(device) else calculateDeviceMetrics(device, today)
        }
        val currentMetrics = currentHoldings.map { device ->
            if (today == null) calculateDeviceMetrics(device) else calculateDeviceMetrics(device, today)
        }
        val currentOwnershipDays = currentMetrics.sumOf { it.ownershipDays }
        val currentNetCost = currentMetrics.sumOf { it.netCostCents }

        val currentEstimatedValues = currentHoldings.mapNotNull { it.estimatedCurrentValueCents }
        val currentDailyCost = if (currentOwnershipDays == 0L) {
            BigDecimal.ZERO.setScale(2)
        } else {
            BigDecimal.valueOf(currentNetCost, 2)
                .divide(BigDecimal.valueOf(currentOwnershipDays), 2, RoundingMode.HALF_UP)
        }

        return AssetSummary(
            totalDeviceCount = devices.size,
            activeCount = activeCount,
            retiredCount = retiredCount,
            soldCount = soldCount,
            historicalPurchaseTotalCents = devices.sumOf { it.purchasePriceCents },
            currentHoldingPurchaseTotalCents = currentHoldings.sumOf { it.purchasePriceCents },
            currentEstimatedValueCents = currentEstimatedValues.takeIf { it.isNotEmpty() }?.sum(),
            historicalNetCostCents = metrics.sumOf { it.netCostCents },
            currentDailyCost = currentDailyCost,
        )
    }
}
