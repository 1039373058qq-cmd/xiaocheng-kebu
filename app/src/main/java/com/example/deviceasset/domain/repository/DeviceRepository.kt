package com.example.deviceasset.domain.repository

import com.example.deviceasset.domain.model.Device
import com.example.deviceasset.domain.model.DeviceStatus
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun observeAllDevices(): Flow<List<Device>>

    fun observeDeviceById(id: Long): Flow<Device?>

    fun observeDevicesByStatus(status: DeviceStatus): Flow<List<Device>>

    suspend fun insert(device: Device): Long

    suspend fun update(device: Device)

    suspend fun delete(device: Device)
}
