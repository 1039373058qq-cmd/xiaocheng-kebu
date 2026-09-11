package com.example.deviceasset

import android.os.Bundle
import android.graphics.Color as AndroidColor
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.deviceasset.course.navigation.CourseApp
import com.example.deviceasset.ui.theme.DeviceAssetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val paper = AndroidColor.rgb(245, 240, 229)
        window.statusBarColor = paper
        window.navigationBarColor = paper
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        val app = application as App
        setContent {
            DeviceAssetTheme {
                CourseApp(
                    repository = app.courseContainer.repository,
                    presetStore = app.courseContainer.presetStore,
                    backupManager = app.courseContainer.backupManager,
                )
            }
        }
    }
}
