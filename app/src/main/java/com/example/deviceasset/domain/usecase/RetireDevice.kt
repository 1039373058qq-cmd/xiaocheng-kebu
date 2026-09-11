package com.example.deviceasset.domain.usecase

import com.example.deviceasset.domain.model.Device
import com.example.deviceasset.domain.model.DeviceStatus
import com.example.deviceasset.domain.repository.DeviceRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

class RetireDevice(
    private val repository: DeviceRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    suspend operator fun invoke(device: Device, retiredDate: LocalDate = LocalDate.now(clock)): Device {
        require(device.status != DeviceStatus.SOLD) { "Sold devices cannot be retired" }
        require(!retiredDate.isBefore(device.purchaseDate)) { "Retired date cannot precede purchase date" }

        val retiredDevice = device.copy(
            status = DeviceStatus.RETIRED,
            retiredDate = retiredDate,
            updatedAt = Instant.now(clock),
        )
        repository.update(retiredDevice)
        return retiredDevice
    }
}
