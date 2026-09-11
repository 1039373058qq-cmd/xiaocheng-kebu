package com.example.deviceasset.data.local

import com.example.deviceasset.domain.model.Device

fun DeviceEntity.toDomain(): Device = Device(
    id = id,
    name = name,
    category = category,
    brand = brand,
    model = model,
    storage = storage,
    purchaseDate = purchaseDate,
    purchasePriceCents = purchasePriceCents,
    status = status,
    retiredDate = retiredDate,
    soldDate = soldDate,
    soldPriceCents = soldPriceCents,
    additionalCostCents = additionalCostCents,
    estimatedCurrentValueCents = estimatedCurrentValueCents,
    note = note,
    imagePath = imagePath,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Device.toEntity(): DeviceEntity = DeviceEntity(
    id = id,
    name = name,
    category = category,
    brand = brand,
    model = model,
    storage = storage,
    purchaseDate = purchaseDate,
    purchasePriceCents = purchasePriceCents,
    status = status,
    retiredDate = retiredDate,
    soldDate = soldDate,
    soldPriceCents = soldPriceCents,
    additionalCostCents = additionalCostCents,
    estimatedCurrentValueCents = estimatedCurrentValueCents,
    note = note,
    imagePath = imagePath,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
