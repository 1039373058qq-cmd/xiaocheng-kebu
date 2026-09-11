package com.example.deviceasset

import android.app.Application
import com.example.deviceasset.course.di.CourseContainer
import com.example.deviceasset.di.AppContainer

class App : Application() {
    val appContainer: AppContainer by lazy { AppContainer(this) }
    val courseContainer: CourseContainer by lazy { CourseContainer(this) }
}
