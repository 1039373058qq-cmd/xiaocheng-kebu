package com.example.deviceasset.domain.usecase

import com.example.deviceasset.domain.model.Device
import com.example.deviceasset.domain.repository.DeviceRepository

class DeleteDevice(
    private val repository: DeviceRepository,
) {
    suspend operator fun invoke(device: Device) {
        repository.delete(device)
    }
}
