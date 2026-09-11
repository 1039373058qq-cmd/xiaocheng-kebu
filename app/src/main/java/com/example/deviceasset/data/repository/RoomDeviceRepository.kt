package com.example.deviceasset.data.repository

import com.example.deviceasset.data.local.DeviceDao
import com.example.deviceasset.data.local.toDomain
import com.example.deviceasset.data.local.toEntity
import com.example.deviceasset.domain.model.Device
import com.example.deviceasset.domain.model.DeviceStatus
import com.example.deviceasset.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDeviceRepository(
    private val dao: DeviceDao,
) : DeviceRepository {
    override fun observeAllDevices(): Flow<List<Device>> =
        dao.getAllDevices().map { entities -> entities.map { it.toDomain() } }

    override fun observeDeviceById(id: Long): Flow<Device?> =
        dao.getDeviceById(id).map { it?.toDomain() }

    override fun observeDevicesByStatus(status: DeviceStatus): Flow<List<Device>> =
        dao.getDevicesByStatus(status).map { entities -> entities.map { it.toDomain() } }

    override suspend fun insert(device: Device): Long = dao.insert(device.toEntity())

    override suspend fun update(device: Device) {
        dao.update(device.toEntity())
    }

    override suspend fun delete(device: Device) {
        dao.delete(device.toEntity())
    }
}
