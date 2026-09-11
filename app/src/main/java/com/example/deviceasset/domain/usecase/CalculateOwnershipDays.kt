package com.example.deviceasset.domain.usecase

import com.example.deviceasset.domain.model.Device
import com.example.deviceasset.domain.model.DeviceStatus
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.max

class CalculateOwnershipDays(
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    operator fun invoke(device: Device, today: LocalDate = LocalDate.now(clock)): Long {
        val endDate = if (device.status == DeviceStatus.SOLD) {
            device.soldDate ?: today
        } else {
            today
        }

        return max(1L, ChronoUnit.DAYS.between(device.purchaseDate, endDate))
    }

    companion object {
        fun systemDefault(): CalculateOwnershipDays =
            CalculateOwnershipDays(Clock.system(ZoneId.systemDefault()))
    }
}
