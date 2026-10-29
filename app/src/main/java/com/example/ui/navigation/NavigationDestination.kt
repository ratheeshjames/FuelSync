package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String
) {
    data object Dashboard : Screen(
        route = "dashboard",
        title = "Dashboard",
        selectedIcon = Icons.Filled.Speed,
        unselectedIcon = Icons.Outlined.Speed,
        contentDescription = "Fuel consumption and vehicle health dashboard"
    )

    data object Logs : Screen(
        route = "logs",
        title = "Logs",
        selectedIcon = Icons.Filled.LocalGasStation,
        unselectedIcon = Icons.Outlined.LocalGasStation,
        contentDescription = "Chronological fuel fill-ups and expense ledger"
    )

    data object Garage : Screen(
        route = "garage",
        title = "Garage",
        selectedIcon = Icons.Filled.DirectionsCar,
        unselectedIcon = Icons.Outlined.DirectionsCar,
        contentDescription = "Multi-vehicle fleet and garage management"
    )

    data object Analytics : Screen(
        route = "analytics",
        title = "Analytics",
        selectedIcon = Icons.Filled.Analytics,
        unselectedIcon = Icons.Outlined.Analytics,
        contentDescription = "Efficiency degradation curves and cost projections"
    )

    data object Settings : Screen(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        contentDescription = "App preferences, unit matrix, and data export"
    )

    companion object {
        val bottomNavItems: List<Screen> = listOf(
            Dashboard,
            Logs,
            Garage,
            Analytics,
            Settings
        )
    }
}

const val ROUTE_ONBOARDING = "onboarding"
const val ROUTE_LOG_ENTRY = "log_entry"
