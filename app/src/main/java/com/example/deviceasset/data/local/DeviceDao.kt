package com.example.deviceasset.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.deviceasset.domain.model.DeviceStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY purchase_date DESC, id DESC")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE id = :id LIMIT 1")
    fun getDeviceById(id: Long): Flow<DeviceEntity?>

    @Query("SELECT * FROM devices WHERE status = :status ORDER BY purchase_date DESC, id DESC")
    fun getDevicesByStatus(status: DeviceStatus): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE status = 'ACTIVE' ORDER BY purchase_date DESC, id DESC")
    fun getActiveDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE status = 'RETIRED' ORDER BY purchase_date DESC, id DESC")
    fun getRetiredDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE status = 'SOLD' ORDER BY purchase_date DESC, id DESC")
    fun getSoldDevices(): Flow<List<DeviceEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(device: DeviceEntity): Long

    @Upsert
    suspend fun upsert(device: DeviceEntity)

    @Update
    suspend fun update(device: DeviceEntity)

    @Delete
    suspend fun delete(device: DeviceEntity)
}
