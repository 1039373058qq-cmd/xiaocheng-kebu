package com.example.deviceasset.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.example.deviceasset.domain.model.DeviceCategory
import com.example.deviceasset.domain.model.DeviceStatus
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "devices",
    indices = [
        Index(value = ["status"]),
        Index(value = ["purchase_date"]),
    ],
)
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val category: DeviceCategory,
    val brand: String?,
    val model: String?,
    val storage: String?,
    @ColumnInfo(name = "purchase_date")
    val purchaseDate: LocalDate,
    @ColumnInfo(name = "purchase_price_cents")
    val purchasePriceCents: Long,
    val status: DeviceStatus,
    @ColumnInfo(name = "retired_date")
    val retiredDate: LocalDate?,
    @ColumnInfo(name = "sold_date")
    val soldDate: LocalDate?,
    @ColumnInfo(name = "sold_price_cents")
    val soldPriceCents: Long?,
    @ColumnInfo(name = "additional_cost_cents")
    val additionalCostCents: Long,
    @ColumnInfo(name = "estimated_current_value_cents")
    val estimatedCurrentValueCents: Long?,
    val note: String?,
    @ColumnInfo(name = "image_path")
    val imagePath: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
)
