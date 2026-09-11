package com.example.deviceasset.domain

import com.example.deviceasset.domain.model.DeviceStatus
import com.example.deviceasset.domain.usecase.CalculateDeviceMetrics
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateDeviceMetricsTest {
    @Test
    fun `same day ownership is one day`() {
        val metrics = CalculateDeviceMetrics().invoke(
            device(purchaseDate = LocalDate.of(2025, 1, 1)),
            today = LocalDate.of(2025, 1, 1),
        )

        assertEquals(1L, metrics.ownershipDays)
        assertEquals(10_500L, metrics.netCostCents)
        assertEquals("105.00", metrics.dailyCost.toPlainString())
    }

    @Test
    fun `sold device subtracts sale price`() {
        val metrics = CalculateDeviceMetrics().invoke(
            device(
                status = DeviceStatus.SOLD,
                soldDate = LocalDate.of(2025, 1, 11),
                soldPriceCents = 8_000L,
            ),
        )

        assertEquals(10L, metrics.ownershipDays)
        assertEquals(2_500L, metrics.netCostCents)
        assertEquals(2_500L, metrics.actualCostCents)
        assertEquals("2.50", metrics.dailyCost.toPlainString())
    }

    @Test
    fun `sold device calculates actual cost days and daily cost from lifecycle dates`() {
        val metrics = CalculateDeviceMetrics().invoke(
            device(
                status = DeviceStatus.SOLD,
                purchaseDate = LocalDate.of(2025, 1, 1),
                soldDate = LocalDate.of(2025, 1, 31),
                soldPriceCents = 8_000L,
            ),
        )

        assertEquals(30L, metrics.ownershipDays)
        assertEquals(2_500L, metrics.actualCostCents)
        assertEquals("0.83", metrics.dailyCost.toPlainString())
    }

    @Test
    fun `sale profit produces negative net cost`() {
        val metrics = CalculateDeviceMetrics().invoke(
            device(
                status = DeviceStatus.SOLD,
                soldDate = LocalDate.of(2025, 1, 11),
                soldPriceCents = 20_000L,
            ),
        )

        assertEquals(-9_500L, metrics.netCostCents)
        assertEquals("-9.50", metrics.dailyCost.toPlainString())
    }
}
