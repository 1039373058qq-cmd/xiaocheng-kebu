package com.example.deviceasset.data.storage

import android.content.Context
import java.io.File
import java.util.UUID

class ImageStorage(private val context: Context) {
    private val imageDirectory: File
        get() = File(context.filesDir, "device-images").apply { mkdirs() }

    fun createDestination(extension: String = "jpg"): File =
        File(imageDirectory, "${UUID.randomUUID()}.$extension")
}
