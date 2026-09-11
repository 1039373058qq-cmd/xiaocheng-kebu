package com.example.deviceasset.domain

import com.example.deviceasset.domain.model.DeviceStatus
import com.example.deviceasset.domain.usecase.DeviceValidationError
import com.example.deviceasset.domain.usecase.ValidateDevice
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateDeviceTest {
    @Test
    fun `sold device requires valid sale fields`() {
        val result = ValidateDevice().invoke(
            device(
                status = DeviceStatus.SOLD,
                soldDate = LocalDate.of(2024, 12, 31),
                soldPriceCents = -1L,
            ),
        )

        assertFalse(result.isValid)
        assertTrue(DeviceValidationError.SOLD_DATE_BEFORE_PURCHASE in result.errors)
        assertTrue(DeviceValidationError.NEGATIVE_SOLD_PRICE in result.errors)
    }

    @Test
    fun `valid active device passes validation`() {
        val result = ValidateDevice().invoke(
            device(purchaseDate = LocalDate.of(2025, 1, 1)),
            today = LocalDate.of(2025, 1, 2),
        )

        assertTrue(result.isValid)
    }
}
