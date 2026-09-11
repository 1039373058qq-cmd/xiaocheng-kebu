package com.example.deviceasset.domain.usecase

import com.example.deviceasset.domain.model.Device
import com.example.deviceasset.domain.model.DeviceStatus
import java.time.Clock
import java.time.LocalDate

enum class DeviceValidationError {
    EMPTY_NAME,
    PURCHASE_DATE_IN_FUTURE,
    NEGATIVE_PURCHASE_PRICE,
    NEGATIVE_ADDITIONAL_COST,
    NEGATIVE_ESTIMATED_VALUE,
    RETIRED_DATE_MISSING,
    RETIRED_DATE_BEFORE_PURCHASE,
    SOLD_DATE_MISSING,
    SOLD_DATE_BEFORE_PURCHASE,
    SOLD_PRICE_MISSING,
    NEGATIVE_SOLD_PRICE,
}

data class DeviceValidationResult(
    val errors: Set<DeviceValidationError>,
) {
    val isValid: Boolean get() = errors.isEmpty()
}

class ValidateDevice(
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    operator fun invoke(device: Device, today: LocalDate = LocalDate.now(clock)): DeviceValidationResult {
        val errors = buildSet {
            if (device.name.isBlank()) add(DeviceValidationError.EMPTY_NAME)
            if (device.purchaseDate.isAfter(today)) add(DeviceValidationError.PURCHASE_DATE_IN_FUTURE)
            if (device.purchasePriceCents < 0L) add(DeviceValidationError.NEGATIVE_PURCHASE_PRICE)
            if (device.additionalCostCents < 0L) add(DeviceValidationError.NEGATIVE_ADDITIONAL_COST)
            if (device.estimatedCurrentValueCents != null && device.estimatedCurrentValueCents < 0L) {
                add(DeviceValidationError.NEGATIVE_ESTIMATED_VALUE)
            }

            if (device.status == DeviceStatus.RETIRED) {
                if (device.retiredDate == null) add(DeviceValidationError.RETIRED_DATE_MISSING)
                if (device.retiredDate != null && device.retiredDate.isBefore(device.purchaseDate)) {
                    add(DeviceValidationError.RETIRED_DATE_BEFORE_PURCHASE)
                }
            }

            if (device.status == DeviceStatus.SOLD) {
                if (device.soldDate == null) add(DeviceValidationError.SOLD_DATE_MISSING)
                if (device.soldDate != null && device.soldDate.isBefore(device.purchaseDate)) {
                    add(DeviceValidationError.SOLD_DATE_BEFORE_PURCHASE)
                }
                if (device.soldPriceCents == null) add(DeviceValidationError.SOLD_PRICE_MISSING)
                if (device.soldPriceCents != null && device.soldPriceCents < 0L) {
                    add(DeviceValidationError.NEGATIVE_SOLD_PRICE)
                }
            }
        }

        return DeviceValidationResult(errors)
    }
}
