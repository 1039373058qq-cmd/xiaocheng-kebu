package com.example.deviceasset.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.deviceasset.domain.repository.DeviceRepository
import com.example.deviceasset.ui.detail.DeviceDetailRoute
import com.example.deviceasset.ui.edit.DeviceEditRoute
import com.example.deviceasset.ui.home.DeviceListRoute
import com.example.deviceasset.ui.usage.UsageAssistantScreen

private object Routes {
    const val USAGE = "usage"
    const val DEVICES = "devices"
    const val NEW_DEVICE = "device/new"
    const val DETAIL_DEVICE = "device/{deviceId}"
    const val EDIT_DEVICE = "device/{deviceId}/edit"

    fun detailDevice(deviceId: Long): String = "device/$deviceId"
    fun editDevice(deviceId: Long): String = "device/$deviceId/edit"
}

@Composable
fun AppNavHost(repository: DeviceRepository) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.USAGE,
    ) {
        composable(Routes.USAGE) {
            UsageAssistantScreen()
        }
        composable(Routes.DEVICES) {
            DeviceListRoute(
                repository = repository,
                onAddDevice = { navController.navigate(Routes.NEW_DEVICE) },
                onDeviceClick = { deviceId -> navController.navigate(Routes.detailDevice(deviceId)) },
            )
        }
        composable(Routes.NEW_DEVICE) {
            DeviceEditRoute(
                repository = repository,
                deviceId = null,
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.DETAIL_DEVICE,
            arguments = listOf(navArgument("deviceId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getLong("deviceId") ?: return@composable
            DeviceDetailRoute(
                repository = repository,
                deviceId = deviceId,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Routes.editDevice(id)) },
                onDeleted = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.EDIT_DEVICE,
            arguments = listOf(navArgument("deviceId") { type = NavType.LongType }),
        ) { backStackEntry ->
            DeviceEditRoute(
                repository = repository,
                deviceId = backStackEntry.arguments?.getLong("deviceId"),
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
    }
}
