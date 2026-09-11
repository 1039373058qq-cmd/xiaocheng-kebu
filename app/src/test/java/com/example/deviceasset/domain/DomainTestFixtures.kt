package com.example.deviceasset.domain

import com.example.deviceasset.domain.model.Device
import com.example.deviceasset.domain.model.DeviceCategory
import com.example.deviceasset.domain.model.DeviceStatus
import java.time.LocalDate

internal fun device(
    status: DeviceStatus = DeviceStatus.ACTIVE,
    purchaseDate: LocalDate = LocalDate.of(2025, 1, 1),
    soldDate: LocalDate? = null,
    soldPriceCents: Long? = null,
    purchasePriceCents: Long = 10_000L,
    additionalCostCents: Long = 500L,
    estimatedCurrentValueCents: Long? = null,
) = Device(
    name = "Test device",
    category = DeviceCategory.PHONE,
    purchaseDate = purchaseDate,
    purchasePriceCents = purchasePriceCents,
    status = status,
    soldDate = soldDate,
    soldPriceCents = soldPriceCents,
    additionalCostCents = additionalCostCents,
    estimatedCurrentValueCents = estimatedCurrentValueCents,
)
