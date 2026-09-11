package com.example.deviceasset.domain.usecase

import com.example.deviceasset.domain.model.Device
import com.example.deviceasset.domain.model.DeviceMetrics
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.LocalDate

class CalculateDeviceMetrics(
    private val calculateOwnershipDays: CalculateOwnershipDays = CalculateOwnershipDays(),
) {
    operator fun invoke(device: Device, today: LocalDate? = null): DeviceMetrics {
        val ownershipDays = if (today == null) {
            calculateOwnershipDays(device)
        } else {
            calculateOwnershipDays(device, today)
        }
        val investedCostCents = device.purchasePriceCents + device.additionalCostCents
        val netCostCents = if (device.status == com.example.deviceasset.domain.model.DeviceStatus.SOLD) {
            investedCostCents - (device.soldPriceCents ?: 0L)
        } else {
            investedCostCents
        }
        val dailyCost = BigDecimal.valueOf(netCostCents, 2)
            .divide(BigDecimal.valueOf(ownershipDays), 2, RoundingMode.HALF_UP)

        return DeviceMetrics(
            ownershipDays = ownershipDays,
            investedCostCents = investedCostCents,
            netCostCents = netCostCents,
            dailyCost = dailyCost,
        )
    }

    companion object {
        fun withClock(clock: Clock): CalculateDeviceMetrics =
            CalculateDeviceMetrics(CalculateOwnershipDays(clock))
    }
}
