package com.example.deviceasset.di

import android.content.Context
import androidx.room.Room
import com.example.deviceasset.data.local.AppDatabase
import com.example.deviceasset.data.repository.RoomDeviceRepository
import com.example.deviceasset.data.settings.SettingsDataStore
import com.example.deviceasset.data.storage.ImageStorage
import com.example.deviceasset.domain.repository.DeviceRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "device_asset.db",
    ).build()

    val deviceRepository: DeviceRepository = RoomDeviceRepository(database.deviceDao())
    val settingsDataStore = SettingsDataStore(appContext)
    val imageStorage = ImageStorage(appContext)
}
