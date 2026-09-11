package com.xingchen.xiaochengkebu.course.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.xingchen.xiaochengkebu.R
import com.xingchen.xiaochengkebu.course.data.repository.TeachingRepository
import com.xingchen.xiaochengkebu.course.data.settings.QuickPresetStore
import com.xingchen.xiaochengkebu.course.data.backup.BackupManager
import com.xingchen.xiaochengkebu.course.ui.calendar.CalendarRoute
import com.xingchen.xiaochengkebu.course.ui.settings.SettingsRoute
import com.xingchen.xiaochengkebu.course.ui.statistics.StatisticsRoute

private const val CALENDAR = "calendar"
private const val CALENDAR_WITH_DATE = "calendar?date={date}"
private const val STATISTICS = "statistics"
private const val SETTINGS = "settings"
private const val HISTORY = "history"

private data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun CourseApp(repository: TeachingRepository, presetStore: QuickPresetStore, backupManager: BackupManager) {
    val navController = rememberNavController()
    val tabs = listOf(
        Tab(CALENDAR, "日历", Icons.Outlined.CalendarMonth),
        Tab(STATISTICS, "统计", Icons.Outlined.BarChart),
        Tab(SETTINGS, "设置", Icons.Outlined.Settings),
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Image(
            painter = painterResource(R.drawable.ink_wash_home_overlay),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            alpha = 0.68f,
        )
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route || (tab.route == CALENDAR && currentRoute == CALENDAR_WITH_DATE),
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(CALENDAR) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { androidx.compose.material3.Text(tab.label) },
                        )
                    }
                }
            },
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = CALENDAR,
                modifier = Modifier.padding(paddingValues),
            ) {
                composable(CALENDAR) { CalendarRoute(repository, presetStore, onOpenSettings = { navController.navigate(SETTINGS) }) }
                composable(STATISTICS) { StatisticsRoute(repository, onHistory = { navController.navigate(HISTORY) }) }
                composable(SETTINGS) { SettingsRoute(repository, presetStore, backupManager) }
                composable(CALENDAR_WITH_DATE, arguments = listOf(androidx.navigation.navArgument("date") { type = androidx.navigation.NavType.LongType })) { entry ->
                    CalendarRoute(repository, presetStore, entry.arguments?.getLong("date"), onOpenSettings = { navController.navigate(SETTINGS) })
                }
                composable(HISTORY) {
                    StatisticsRoute(
                        repository,
                        historyOnly = true,
                        onBack = { navController.popBackStack() },
                        onRecordClick = { date -> navController.navigate("calendar?date=$date") },
                    )
                }
            }
        }
    }
}
