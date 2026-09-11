package com.xingchen.xiaochengkebu

import android.os.Bundle
import android.graphics.Color as AndroidColor
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.xingchen.xiaochengkebu.course.navigation.CourseApp
import com.xingchen.xiaochengkebu.ui.theme.XiaoChenKebuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val paper = AndroidColor.rgb(245, 240, 229)
        window.statusBarColor = paper
        window.navigationBarColor = paper
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        val app = application as App
        setContent {
            XiaoChenKebuTheme {
                CourseApp(
                    repository = app.courseContainer.repository,
                    presetStore = app.courseContainer.presetStore,
                    backupManager = app.courseContainer.backupManager,
                )
            }
        }
    }
}
