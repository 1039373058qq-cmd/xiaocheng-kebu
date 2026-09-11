package com.example.deviceasset.domain

import com.example.deviceasset.domain.model.Device
import com.example.deviceasset.domain.model.DeviceStatus
import com.example.deviceasset.domain.repository.DeviceRepository
import com.example.deviceasset.domain.usecase.CalculateDeviceMetrics
import com.example.deviceasset.domain.usecase.SellDevice
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SellDeviceTest {
    @Test
    fun `selling a device persists sale data used by lifecycle metrics`() = runBlocking {
        val repository = RecordingDeviceRepository()
        val original = device(
            purchaseDate = LocalDate.of(2025, 1, 1),
            status = DeviceStatus.ACTIVE,
        )

        val sold = SellDevice(repository).invoke(
            device = original,
            soldDate = LocalDate.of(2025, 1, 31),
            soldPriceCents = 8_000L,
        )
        val metrics = CalculateDeviceMetrics().invoke(sold)

        assertEquals(DeviceStatus.SOLD, repository.updated?.status)
        assertEquals(LocalDate.of(2025, 1, 31), repository.updated?.soldDate)
        assertEquals(8_000L, repository.updated?.soldPriceCents)
        assertEquals(2_500L, metrics.actualCostCents)
        assertEquals(30L, metrics.ownershipDays)
        assertEquals("0.83", metrics.dailyCost.toPlainString())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `selling before purchase date is rejected`() {
        runBlocking {
            SellDevice(RecordingDeviceRepository()).invoke(
                device = device(purchaseDate = LocalDate.of(2025, 1, 10)),
                soldDate = LocalDate.of(2025, 1, 9),
                soldPriceCents = 1_000L,
            )
        }
    }
}

private class RecordingDeviceRepository : DeviceRepository {
    var updated: Device? = null

    override fun observeAllDevices(): Flow<List<Device>> = flowOf(emptyList())

    override fun observeDeviceById(id: Long): Flow<Device?> = flowOf(updated)

    override fun observeDevicesByStatus(status: DeviceStatus): Flow<List<Device>> = flowOf(emptyList())

    override suspend fun insert(device: Device): Long = device.id

    override suspend fun update(device: Device) {
        updated = device
    }

    override suspend fun delete(device: Device) {
        updated = null
    }
}
