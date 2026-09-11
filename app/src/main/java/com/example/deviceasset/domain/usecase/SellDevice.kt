package com.example.deviceasset.domain.usecase

import com.example.deviceasset.domain.model.Device
import com.example.deviceasset.domain.model.DeviceStatus
import com.example.deviceasset.domain.repository.DeviceRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

class SellDevice(
    private val repository: DeviceRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    suspend operator fun invoke(
        device: Device,
        soldDate: LocalDate,
        soldPriceCents: Long,
    ): Device {
        require(device.status != DeviceStatus.SOLD) { "Device has already been sold" }
        require(!soldDate.isBefore(device.purchaseDate)) { "Sold date cannot precede purchase date" }
        require(soldPriceCents >= 0L) { "Sold price cannot be negative" }

        val soldDevice = device.copy(
            status = DeviceStatus.SOLD,
            soldDate = soldDate,
            soldPriceCents = soldPriceCents,
            updatedAt = Instant.now(clock),
        )
        repository.update(soldDevice)
        return soldDevice
    }
}
