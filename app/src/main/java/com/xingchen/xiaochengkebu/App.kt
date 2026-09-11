package com.xingchen.xiaochengkebu

import android.app.Application
import com.xingchen.xiaochengkebu.course.di.CourseContainer

class App : Application() {
    val courseContainer: CourseContainer by lazy { CourseContainer(this) }
}
