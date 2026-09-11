package com.example.deviceasset.domain.model

import java.time.Instant
import java.time.LocalDate

data class Device(
    val id: Long = 0L,
    val name: String,
    val category: DeviceCategory,
    val brand: String? = null,
    val model: String? = null,
    val storage: String? = null,
    val purchaseDate: LocalDate,
    val purchasePriceCents: Long,
    val status: DeviceStatus = DeviceStatus.ACTIVE,
    val retiredDate: LocalDate? = null,
    val soldDate: LocalDate? = null,
    val soldPriceCents: Long? = null,
    val additionalCostCents: Long = 0L,
    val estimatedCurrentValueCents: Long? = null,
    val note: String? = null,
    val imagePath: String? = null,
    val createdAt: Instant = Instant.EPOCH,
    val updatedAt: Instant = Instant.EPOCH,
)
