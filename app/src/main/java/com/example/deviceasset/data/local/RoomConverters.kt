package com.example.deviceasset.data.local

import androidx.room.TypeConverter
import com.example.deviceasset.domain.model.DeviceCategory
import com.example.deviceasset.domain.model.DeviceStatus
import java.time.Instant
import java.time.LocalDate

class RoomConverters {
    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun instantToEpochMillis(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun epochMillisToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun deviceStatusToString(value: DeviceStatus?): String? = value?.name

    @TypeConverter
    fun stringToDeviceStatus(value: String?): DeviceStatus? = value?.let(DeviceStatus::valueOf)

    @TypeConverter
    fun deviceCategoryToString(value: DeviceCategory?): String? = value?.name

    @TypeConverter
    fun stringToDeviceCategory(value: String?): DeviceCategory? = value?.let(DeviceCategory::valueOf)
}
